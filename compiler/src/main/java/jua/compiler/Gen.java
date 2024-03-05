package jua.compiler;

import jua.compiler.Code.Chain;
import jua.compiler.Items.CondItem;
import jua.compiler.Items.Item;
import jua.compiler.Tree.*;
import jua.compiler.utils.Assert;
import jua.compiler.utils.Flow;
import jua.compiler.utils.IntArrayList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static jua.compiler.Code.mergeChains;
import static jua.compiler.InstructionUtils.*;
import static jua.compiler.TreeInfo.isNull;
import static jua.compiler.TreeInfo.stripParens;

public class Gen extends Scanner {

    static class FlowEnv {
        final FlowEnv parent;

        Chain contChain = null;
        Chain exitChain = null;

        FlowEnv(FlowEnv parent) {
            this.parent = parent;
        }
    }

    static class SwitchEnv extends FlowEnv {

        /** Индексы констант из ключей кейзов. Равно null когда isSwitch=false */
        final IntArrayList caseLabelsConstantIndexes = new IntArrayList();
        /** Точка входа (IP) для каждого кейза. Равно null когда isSwitch=false */
        final IntArrayList switchCaseOffsets = new IntArrayList();
        /** Указатель на точку входа в default-case */
        int switchDefaultOffset = -1;

        /** label constant index => chain */
        final Map<Integer, Chain> caseChains = new HashMap<>();
        Chain elseCaseChain;

        SwitchEnv(FlowEnv parent) {
            super(parent);
        }
    }

    // Set from Code.<init>
    public Code code;
    public Source source;

    private Item result;

    private Items items;

    private FlowEnv flow;

    // Set from JuaCompiler.compile
    public boolean genJvmLoops;

    private Item genExpr(Expr tree) {
        Item prevItem = result;
        try {
            tree.accept(this);
            return result;
        } finally {
            result = prevItem;
        }
    }

    /**
     * Генерирует код оператора в изолированном блоке.
     *
     * @return {@code true}, если изолированный блок жив, {@code false}, если нет.
     */
    private boolean genBlock(Stmt statement) {
        boolean alive = code.isAlive();
        int tos = code.tos();

        try {
            statement.accept(this);
            return code.isAlive();
        } finally {
            code.setAlive(alive);
            code.checkTosConvergence(tos);
        }
    }

    @Override
    public void visitDocument(Document tree) {
        // Jua, начиная с версии 3.1 от 10/3/2023 не поддерживает выполняемые инструкции вне функций.
//        scan(tree.stats);

        scan(tree.functions);
    }

    @Override
    public void visitFuncDef(FuncDef tree) {
        code = tree.sym.code;
        code.sym = tree.sym;
        code.putPos(tree.pos);
        items = new Items(code);

        // todo: Не использовать типы из runtime
        // todo: Повысить качество кода: понизить связность, распределить ответственность.

        java.util.List<Object> defaults = new ArrayList<>();
        Flow.forEach(tree.params, param -> {
            if (param.expr != null) {
                Literal literal = (Literal) stripParens(param.expr);
                defaults.add(literal.value);
            }
        });

        assert tree.body != null;

        if (tree.body.hasTag(Tag.BLOCK)) {
            genBlock(tree.body);
            code.emitSingle(OPCodes.Leave);
        } else {
            Assert.check(tree.body.hasTag(Tag.DISCARDED), "Function body neither block ner expression");
            genExpr(((Discarded) tree.body).expr).load();
            code.emitSingle(OPCodes.Return);
        }
        code.setAlive(false);

        tree.sym.defs = defaults.toArray();
        tree.sym.executable = code.toExecutable();
    }

    @Override
    public void visitIf(If tree) {
        CondItem condItem = genExpr(tree.cond).asCond();
        Chain falseJumps = condItem.falseJumps();
        code.resolve(condItem.trueChain);
        boolean tbState = genBlock(tree.thenbody); // then branch state
        if (tree.elsebody == null) {
            code.resolve(falseJumps);
        } else {
            Chain avoidElseBranchChain = tbState ? code.branch(OPCodes.Goto) : null;
            code.resolve(falseJumps);
            boolean ebState = genBlock(tree.elsebody); // else branch state
            code.resolve(avoidElseBranchChain);

            if (!tbState && !ebState) code.setAlive(false);
        }
    }

    @Override
    public void visitWhileLoop(WhileLoop tree) {
        genLoop(tree, Flow.empty(), tree.cond, Flow.empty(), tree.body, true);
    }

    @Override
    public void visitDoLoop(DoLoop tree) {
        genLoop(tree, Flow.empty(), tree.cond, Flow.empty(), tree.body, false);
    }

    @Override
    public void visitForLoop(ForLoop tree) {
        genLoop(tree, tree.init, tree.cond, tree.step, tree.body, true);
    }

    private void genLoop(Stmt tree, Flow<Stmt> init, Expr cond, Flow<Expr> step, Stmt body, boolean testFirst) {
        FlowEnv parentFlow = flow;
        flow = new FlowEnv(parentFlow);

        code.markTreePos(tree);
        scan(init);
        Chain skipBodyChain = testFirst ? code.branch(OPCodes.Goto) : null;
        int loopStartPC = code.pc();
        scan(body);
        code.resolve(flow.contChain);
        Flow.forEach(step, s -> genExpr(s).drop());
        code.resolve(skipBodyChain);
        CondItem condItem = cond == null
                ? items.mkLiteral(true).asCond()
                : genExpr(cond).asCond();
        code.resolve(condItem.trueJumps(), loopStartPC);
        code.resolve(condItem.falseChain);
        code.resolve(flow.exitChain);

        Assert.check(flow.parent == parentFlow);
        flow = parentFlow;
    }

    @Override
    public void visitSwitch(Switch tree) {
        genExpr(tree.expr).load();
        SwitchEnv env = new SwitchEnv(flow);
        flow = env;
        code.putPos(tree.pos);
        int opcode = Flow.count(tree.cases) >= 16
                ? OPCodes.BinarySwitch
                : OPCodes.LinearSwitch;
        SwitchInstrNode node = new SwitchInstrNode(opcode);
        code.emitNode(node);

        boolean codeAlive = Flow.reduce(tree.cases, false, (c, state) -> {
            boolean alive = genBlock(c);
            code.resolve(env.contChain);
            env.contChain = null;
            return state | alive;
        });

        if (env.switchDefaultOffset == -1) {
            // Явного default-case не было
            env.switchDefaultOffset = code.pc() - 1;
        }

        // resolving fallthrough
        for (Map.Entry<Integer, Chain> entry : env.caseChains.entrySet()) {
            int labelIndex = entry.getKey();
            Chain chain = entry.getValue();
            int index = env.caseLabelsConstantIndexes.indexOf(labelIndex);
            if (index >= 0) {
                int offset = env.switchCaseOffsets.get(index);
                code.resolve(chain, offset);
            } else {
                code.resolve(chain);
            }
        }
        if (env.elseCaseChain != null && env.switchDefaultOffset >= 0) {
            code.resolve(env.elseCaseChain, env.switchDefaultOffset);
        }

        node.literals = env.caseLabelsConstantIndexes.toArray();
        node.dstIps = env.switchCaseOffsets.toArray();
        node.defCp = env.switchDefaultOffset;

        code.resolve(env.exitChain);

        if (!codeAlive) {
            code.setAlive(false);
        }

        flow = env.parent;
    }

    @Override
    public void visitCase(Case tree) {
        Assert.check(flow instanceof SwitchEnv);
        SwitchEnv env = (SwitchEnv) flow;
        if (tree.labels == null) {
            // default case
            env.switchDefaultOffset = code.pc();
        } else {
            Flow.forEach(tree.labels, label -> {
                int labelIndex = genExpr(label).constantIndex();
                env.caseLabelsConstantIndexes.add(labelIndex);
                // Это не ошибка. Следующая строчка должна находиться именно в цикле
                // Потому что инструкция switch ассоциирует значения к переходам в масштабе 1 к 1.
                env.switchCaseOffsets.add(code.pc());
            });
        }

        // Весь кейз целиком это один из дочерних бранчей switch.
        scan(tree.body);

        flow.exitChain = mergeChains(flow.exitChain, code.branch(OPCodes.Goto));
    }

    @Override
    public void visitBreak(Break tree) {
        FlowEnv env = searchEnv(false);
        Assert.checkNonNull(env);
        code.putPos(tree.pos);
        env.exitChain = mergeChains(env.exitChain, code.branch(OPCodes.Goto));
        code.setAlive(false);
    }

    @Override
    public void visitContinue(Continue tree) {
        FlowEnv env = searchEnv(false);
        Assert.checkNonNull(env);
        code.putPos(tree.pos);
        env.contChain = mergeChains(env.contChain, code.branch(OPCodes.Goto));
        code.setAlive(false);
    }

    @Override
    public void visitFallthrough(Fallthrough tree) {
        SwitchEnv env = (SwitchEnv) searchEnv(true);
        Assert.checkNonNull(env);
        code.putPos(tree.pos);
        Chain branch = code.branch(OPCodes.Goto);
        if (tree.hasTarget) {
            if (tree.target == null) {
                // fallthrough else;
                env.elseCaseChain = mergeChains(env.elseCaseChain, branch);
            } else {
                int labelIndex = genExpr(tree.target).constantIndex();
                env.caseChains.put(labelIndex,
                        mergeChains(env.caseChains.get(labelIndex), branch));
            }
        } else {
            env.contChain = mergeChains(env.contChain, branch);
        }
        code.setAlive(false);
    }

    private FlowEnv searchEnv(boolean isSwitch) {
        for (FlowEnv env = flow; env != null; env = env.parent)
            if ((env instanceof SwitchEnv) == isSwitch)
                return env;
        return null;
    }

    @Override
    public void visitVarDef(VarDef tree) {
        code.putPos(tree.pos);
        Flow.forEach(tree.defs, def -> {
            code.putPos(def.pos);
            if (def.init == null) {
                items.mkLiteral(null).load();
            } else {
                genExpr(def.init).load();
            }
            items.makeAssignItem(items.makeLocal(def.sym.id)).drop();
        });
    }

    @Override
    public void visitReturn(Return tree) {
        code.putPos(tree.pos);
        if (tree.expr == null || isNull(tree.expr)) {
            code.emitSingle(OPCodes.Leave);
        } else {
            genExpr(tree.expr).load();
            code.emitSingle(OPCodes.Return);
        }
        code.setAlive(false);
    }

    @Override
    public void visitDiscarded(Discarded tree) {
        genExpr(tree.expr).drop();
    }

    @Override
    public void visitLiteral(Literal tree) {
        result = items.t(tree).mkLiteral(tree.value);
    }

    @Override
    public void visitListLiteral(ListLiteral tree) {
        code.putPos(tree.pos);
        items.mkLiteral((long) Flow.count(tree.entries)).load();
        code.emitSingle(OPCodes.NewList);
        Flow.reduce(tree.entries, 0L, (entry, index) -> {
            items.mkStackItem().duplicate();
            items.mkLiteral(index).load();
            genExpr(entry).load();
            items.mkAccessItem().store();
            return index + 1;
        });
        result = items.mkStackItem();
    }

    @Override
    public void visitVariable(Var tree) {
        result = items.t(tree).makeLocal(tree.sym.id);
    }

    @Override
    public void visitMember(Member tree) {
        genAccess(tree, tree.expr, new Literal(tree.memberPos, tree.member));
    }

    @Override
    public void visitIndex(Index tree) {
        genAccess(tree, tree.expr, tree.index);
    }

    private void genAccess(Expr tree, Expr expr, Expr key) {
        genExpr(expr).load();
        genExpr(key).load();
        result = items.t(tree).mkAccessItem();
    }

    @Override
    public void visitInvocation(Invocation tree) {
        Assert.check(tree.target instanceof Member);
        Flow.forEach(tree.args, a -> genExpr(a.expr).load());
        code.putPos(tree.pos);
        if (tree.sym.opcode == 0) {
            // Обычный вызов функции
            int calleeId = code.resolveCallee(tree.sym.name);
            code.emitCall(calleeId, Flow.count(tree.args));
        } else {
            // Языковая конструкция
            code.emitSingle(tree.sym.opcode);
        }
        result = items.mkStackItem();
    }

    @Override
    public void visitAssign(Assign tree) {
        Item varItem = genExpr(tree.var);
        genExpr(tree.expr).load();
        result = items.makeAssignItem(varItem);
    }

    @Override
    public void visitEnhancedAssign(EnhancedAssign tree) {
        Item varItem = genExpr(tree.var);
        varItem.duplicate();
        if (tree.hasTag(Tag.ASG_COALESCE)) {
            CondItem presentCond = varItem.asPresentCond();
            Chain skipCoalesceChain = presentCond.trueJumps();
            code.resolve(presentCond.falseChain);
            genExpr(tree.expr).load();
            items.t(tree);
            result = varItem.coalesceAsg(skipCoalesceChain);
        } else {
            varItem.load();
            genExpr(tree.expr).load();
            code.emitSingle(fromBinaryAsgOpTag(tree.tag));
            result = items.t(tree).makeAssignItem(varItem);
        }
    }

    @Override
    public void visitConditional(Conditional tree) {
        CondItem condItem = genExpr(tree.cond).asCond();
        Chain falseJumps = condItem.falseJumps();
        code.resolve(condItem.trueChain);
        genExpr(tree.ths).load();
        Chain trueJumps = code.branch(OPCodes.Goto);
        code.resolve(falseJumps);
        genExpr(tree.fhs).load();
        code.resolve(trueJumps);
        result = items.mkStackItem();
    }

    @Override
    public void visitBinaryOp(BinaryOp tree) {
        switch (tree.tag) {
            case AND: {
                CondItem lhsCond = genExpr(tree.lhs).asCond();
                Chain falseJumps = lhsCond.falseJumps();
                code.resolve(lhsCond.trueChain);
                CondItem rhsCond = genExpr(tree.rhs).asCond();
                Chain skipOtherConditionsChain = mergeChains(falseJumps, rhsCond.falseChain);
                result = items.t(tree).makeCondItem(rhsCond.opcode, rhsCond.trueChain, skipOtherConditionsChain);
                break;
            }

            case OR: {
                CondItem lhsCond = genExpr(tree.lhs).asCond();
                Chain trueJumps = lhsCond.trueJumps();
                code.resolve(lhsCond.falseChain);
                CondItem rhsCond = genExpr(tree.rhs).asCond();
                Chain skipOtherConditionsChain = mergeChains(trueJumps, rhsCond.trueChain);
                result = items.t(tree).makeCondItem(rhsCond.opcode, skipOtherConditionsChain, rhsCond.falseChain);
                break;
            }

            case EQ: case NE:
            case GT: case GE:
            case LT: case LE:
                genExpr(tree.lhs).load();
                genExpr(tree.rhs).load();
                result = items.t(tree).makeCondItem(fromComparisonOpTag(tree.tag));
                break;
                
            case COALESCE: {
                Item a = genExpr(tree.lhs).load();
                a.duplicate();
                CondItem c = a.asNonNullCond();
                Chain b = c.trueJumps();
                code.resolve(c.falseChain);
                a.drop();
                genExpr(tree.rhs).load();
                code.resolve(b);
                result = items.mkStackItem();
                break;
            }

            default:
                genExpr(tree.lhs).load();
                genExpr(tree.rhs).load();
                code.markTreePos(tree);
                code.emitSingle(fromBinaryOpTag(tree.tag));
                result = items.mkStackItem();
        }
    }

    @Override
    public void visitUnaryOp(UnaryOp tree) {
        Item item = genExpr(tree.expr);
        items.t(tree);
        switch (tree.tag) {
            case POSTINC: case POSTDEC:
            case PREINC: case PREDEC:
                result = item.increase(tree.tag);
                break;

            case NOT:
                result = item.asCond().negated();
                break;

            case NULLCHK:
                result = item.asNonNullCond();
                break;

            case EVACUATE:
                result = item.wrapEvacuate();
                break;

            default:
                item.load();
                code.markTreePos(tree);
                code.emitSingle(fromUnaryOpTag(tree.tag));
                result = items.mkStackItem();
                // break is unnecessary
        }
    }
}