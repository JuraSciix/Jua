package jua.compiler;

import jua.compiler.Code.Chain;
import jua.compiler.Items.CondItem;
import jua.compiler.Items.Item;
import jua.compiler.Tree.*;
import jua.compiler.utils.Assert;
import jua.compiler.utils.IntArrayList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static jua.compiler.Code.mergeChains;
import static jua.compiler.InstructionUtils.*;
import static jua.compiler.CompHelper.isNull;
import static jua.compiler.CompHelper.stripParens;

public class Gen extends Scanner {

    static class FlowEnv {
        final FlowEnv parent;

        Chain contChain = null;
        Chain exitChain = null;

        FlowEnv(FlowEnv parent) {
            this.parent = parent;
        }

        void ontoNext(Chain opcode, boolean loop) {
            if (loop) {
                contChain = mergeChains(contChain, opcode);
            } else {
                parent.ontoNext(opcode, false);
            }
        }

        void ontoExit(Chain opcode) {
            exitChain = mergeChains(exitChain, opcode);
        }

        void ontoElse(Chain opcode) {
            parent.ontoElse(opcode);
        }

        void ontoCase(int labelIndex, Chain opcode) {
            parent.ontoCase(labelIndex, opcode);
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

        @Override
        void ontoNext(Chain opcode, boolean loop) {
            if (loop) {
                parent.ontoNext(opcode, true);
            } else {
                contChain = mergeChains(contChain, opcode);
            }
        }

        @Override
        void ontoElse(Chain opcode) {
            elseCaseChain = mergeChains(elseCaseChain, opcode);
        }

        @Override
        void ontoCase(int labelIndex, Chain opcode) {
            caseChains.computeIfPresent(labelIndex, (i, prev) -> mergeChains(prev, opcode));
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

        List<Object> defaults = new ArrayList<>();
        tree.params.forEach((Consumer<? super FuncDef.Parameter>) param -> {
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
        genLoop(tree, TList.empty(), tree.cond, TList.empty(), tree.body, true);
    }

    @Override
    public void visitDoLoop(DoLoop tree) {
        genLoop(tree, TList.empty(), tree.cond, TList.empty(), tree.body, false);
    }

    @Override
    public void visitForLoop(ForLoop tree) {
        genLoop(tree, tree.init, tree.cond, tree.step, tree.body, true);
    }

    private void genLoop(Stmt tree, TList<Stmt> init, Expr cond, TList<Expr> step, Stmt body, boolean testFirst) {
        FlowEnv parentFlow = flow;
        flow = new FlowEnv(parentFlow);

        code.markTreePos(tree);
        scan(init);
        Chain skipBodyChain = testFirst ? code.branch(OPCodes.Goto) : null;
        int loopStartPC = code.pc();
        scan(body);
        code.resolve(flow.contChain);
        step.forEach((Consumer<? super Expr>) s -> genExpr(s).drop());
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
    public void visitBreak(Break tree) {
        Assert.checkNonNull(flow);
        code.putPos(tree.pos);
        flow.ontoExit(code.branch(OPCodes.Goto));
        code.setAlive(false);
    }

    @Override
    public void visitContinue(Continue tree) {
        Assert.checkNonNull(flow);
        code.putPos(tree.pos);
        flow.ontoNext(code.branch(OPCodes.Goto), true);
        code.setAlive(false);
    }

    @Override
    public void visitVarDef(VarDef tree) {
        code.putPos(tree.pos);
        tree.defs.forEach((Consumer<? super VarDef.Definition>) def -> {
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
        items.mkLiteral((long) tree.entries.size()).load();
        code.emitSingle(OPCodes.NewList);

        for (int i = 0; i < tree.entries.size(); i++) {
            items.mkStackItem().duplicate();
            items.mkLiteral((long) i).load();
            genExpr(tree.entries.get(i)).load();
            items.mkAccessItem().store();
        }
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
        tree.args.forEach((Consumer<? super Invocation.Argument>) a -> genExpr(a.expr).load());
        code.putPos(tree.pos);
        if (tree.sym.opcode < 0) {
            // Обычный вызов функции
            int calleeId = code.resolveCallee(tree.sym.name);
            code.emitCall(calleeId, tree.args.size());
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