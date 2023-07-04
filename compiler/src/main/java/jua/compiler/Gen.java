package jua.compiler;

import jua.compiler.Code.Chain;
import jua.compiler.Items.CondItem;
import jua.compiler.Items.Item;
import jua.compiler.Items.SafeItem;
import jua.compiler.ProgramScope.ConstantSymbol;
import jua.compiler.Tree.Return;
import jua.compiler.Tree.*;
import jua.interpreter.Address;
import jua.interpreter.AddressUtils;
import jua.interpreter.instruction.*;
import jua.runtime.Function;
import jua.utils.Assert;
import jua.utils.List;

import java.util.ArrayList;

import static jua.compiler.Code.mergeChains;
import static jua.compiler.InstructionFactory.*;
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

        /** Указатель на инструкцию, где находится switch. */
        int switchStartPC;
        /** Индексы констант из ключей кейзов. Равно null когда isSwitch=false */
        final ArrayList<Integer> caseLabelsConstantIndexes = new ArrayList<>();
        /** Точка входа (IP) для каждого кейза. Равно null когда isSwitch=false */
        final ArrayList<Integer> switchCaseOffsets = new ArrayList<>();
        /** Указатель на точку входа в default-case */
        int switchDefaultOffset = -1;

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

    private Item genExpr(Expression tree) {
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
    private boolean genBlock(Statement statement) {
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
    public void visitCompilationUnit(CompilationUnit tree) {
        code = tree.sym.code;
        code.putPos(0);
        items = new Items(code);
        scan(tree.stats);
        code.addInstruction(leave);
        tree.sym.runtimefunc = new Function(
                "<main>",
                source.fileName,
                0,
                0,
                new String[0],
                new Address[0],
                0L,
                code.buildCodeSegment(),
                null
        );
    }

    @Override
    public void visitFuncDef(FuncDef tree) {
        code = tree.sym.code;
        code.putPos(tree.pos);
        items = new Items(code);

        // todo: Не использовать типы из runtime
        // todo: Повысить качество кода: понизить связность, распределить ответственность.

        List<Address> defaults = new List<>();
        for (FuncDef.Parameter param : tree.params) {
            code.resolveLocal(param.name);
            if (param.expr != null) {
                Literal literal = (Literal) stripParens(param.expr);
                Address address = new Address();
                AddressUtils.assignObject(address, literal.value);
                defaults.add(address);
            }
        }

        assert tree.body != null;

        if (tree.body.hasTag(Tag.BLOCK)) {
            genBlock(tree.body);
            code.addInstruction(leave);
        } else {
            Assert.check(tree.body.hasTag(Tag.DISCARDED), "Function body neither block ner expression");
            genExpr(((Discarded) tree.body).expr).load();
            code.addInstruction(return_);
        }
        code.setAlive(false);

        tree.sym.runtimefunc = new Function(
                tree.name.toString(),
                source.fileName,
                tree.params.count() - defaults.count(),
                tree.params.count(),
                tree.params.map(param -> param.name.toString()).toArray(String[]::new),
                defaults.toArray(Address[]::new),
                0L,
                code.buildCodeSegment(),
                null
        );
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
            Chain avoidElseBranchChain = tbState ? code.branch(new Goto()) : null;
            code.resolve(falseJumps);
            boolean ebState = genBlock(tree.elsebody); // else branch state
            code.resolve(avoidElseBranchChain);

            if (!tbState && !ebState) code.setAlive(false);
        }
    }

    @Override
    public void visitWhileLoop(WhileLoop tree) {
        genLoop(tree, List.empty(), tree.cond, List.empty(), tree.body, true);
    }

    @Override
    public void visitDoLoop(DoLoop tree) {
        genLoop(tree, List.empty(), tree.cond, List.empty(), tree.body, false);
    }

    @Override
    public void visitForLoop(ForLoop tree) {
        genLoop(tree, tree.init, tree.cond, tree.step, tree.body, true);
    }

    private void genLoop(Statement tree, List<Statement> init, Expression cond, List<Expression> step, Statement body, boolean testFirst) {
        FlowEnv parentFlow = flow;
        flow = new FlowEnv(parentFlow);

        code.markTreePos(tree);
        scan(init);
        Chain skipBodyChain = testFirst ? code.branch(new Goto()) : null;
        int loopStartPC = code.pc();
        scan(body);
        step.forEach(s -> genExpr(s).drop());
        code.resolve(skipBodyChain);
        code.resolve(flow.contChain);
        CondItem condItem = genExpr(cond).asCond();
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
        env.switchStartPC = code.pc();
        code.putPos(tree.pos);
        code.addInstruction(new Fake(-1)); // Резервируем место под инструкцию

        boolean codeAlive = true;

        for (Case c : tree.cases) {
            codeAlive &= genBlock(c);
            code.resolve(env.contChain);
            env.contChain = null;
        }

        if (env.switchDefaultOffset == -1) {
            // Явного default-case не было
            env.switchDefaultOffset = code.pc() - env.switchStartPC;
        }

        int[] literals = env.caseLabelsConstantIndexes.stream().mapToInt(a -> a).toArray();
        int[] destIps = env.switchCaseOffsets.stream().mapToInt(a -> a).toArray();
        code.setInstruction(env.switchStartPC, (env.caseLabelsConstantIndexes.size() <= 16)
                ? new Linearswitch(literals, destIps, env.switchDefaultOffset)
                : new Binaryswitch(literals, destIps, env.switchDefaultOffset));

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
            env.switchDefaultOffset = code.pc() - env.switchStartPC;
        } else {
            for (Expression label : tree.labels) {
                env.caseLabelsConstantIndexes.add(genExpr(label).constantIndex());
                // Это не ошибка. Следующая строчка должна находиться именно в цикле
                // Потому что инструкция switch ассоциирует значения к переходам в масштабе 1 к 1.
                env.switchCaseOffsets.add(code.pc() - env.switchStartPC);
            }
        }

        // Весь кейз целиком это один из дочерних бранчей switch.
        scan(tree.body);

        flow.exitChain = mergeChains(flow.exitChain, code.branch(new Goto()));
    }

    @Override
    public void visitBreak(Break tree) {
        FlowEnv env = searchEnv(false);
        Assert.checkNonNull(env);
        code.putPos(tree.pos);
        env.exitChain = mergeChains(env.exitChain, code.branch(new Goto()));
        code.setAlive(false);
    }

    @Override
    public void visitContinue(Continue tree) {
        FlowEnv env = searchEnv(false);
        Assert.checkNonNull(env);
        code.putPos(tree.pos);
        env.contChain = mergeChains(env.contChain, code.branch(new Goto()));
        code.setAlive(false);
    }

    @Override
    public void visitFallthrough(Fallthrough tree) {
        FlowEnv env = searchEnv(true);
        Assert.checkNonNull(env);
        code.putPos(tree.pos);
        env.contChain = mergeChains(env.contChain, code.branch(new Goto()));
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
        for (VarDef.Definition def : tree.defs) {
            code.putPos(def.name.pos);
            if (def.init == null) {
                items.mkLiteral(null).load();
            } else {
                genExpr(def.init).load();
            }
            items.makeAssignItem(items.makeLocal(code.resolveLocal(def.name))).drop();
        }
    }

    @Override
    public void visitReturn(Return tree) {
        code.putPos(tree.pos);
        if (tree.expr == null || isNull(tree.expr)) {
            code.addInstruction(leave);
        } else {
            genExpr(tree.expr).load();
            code.addInstruction(return_);
        }
        code.setAlive(false);
    }

    @Override
    public void visitDiscarded(Discarded tree) {
        genExpr(tree.expr).drop();
    }

    @Override
    public void visitLiteral(Literal tree) {
        result = items.mkLiteral(tree.value);
    }

    @Override
    public void visitListLiteral(ListLiteral tree) {
        code.putPos(tree.pos);
        items.mkLiteral((long) tree.entries.count()).load();
        code.addInstruction(newlist);
        int index = 0;
        for (Expression entry : tree.entries) {
            items.mkStackItem().duplicate();
            items.mkLiteral((long) index++).load();
            genExpr(entry).load();
            items.mkAccessItem().store();
        }
        result = items.mkStackItem();
    }

    @Override
    public void visitMapLiteral(MapLiteral tree) {
        code.putPos(tree.pos);
        code.addInstruction(newmap);
        for (MapLiteral.Entry entry : tree.entries) {
            items.mkStackItem().duplicate();
            genExpr(entry.key).load();
            genExpr(entry.value).load();
            code.putPos(entry.pos);
            items.mkAccessItem().store();
        }
        result = items.mkStackItem();
    }

    @Override
    public void visitVariable(Var tree) {
        if (tree.sym instanceof ConstantSymbol) {
            code.putPos(tree.pos);
            code.addInstruction(new Getconst(tree.sym.id));
            result = items.mkStackItem();
        } else {
            result = items.makeLocal(code.resolveLocal(tree.name)).t(tree);
        }
    }

    @Override
    public void visitMemberAccess(MemberAccess tree) {
        genAccess(tree, tree.expr, new Literal(tree.member.pos, tree.member.toString()), Tag.MEMACCSF);
    }

    @Override
    public void visitArrayAccess(ArrayAccess tree) {
        genAccess(tree, tree.expr, tree.index, Tag.ARRACCSF);
    }

    private void genAccess(Expression tree, Expression expr, Expression key, Tag safeTag) {
        if (tree.hasTag(safeTag)) {
            SafeItem exprSafeItem = genExpr(expr).asSafe(null, null);
            Item childItem = exprSafeItem.child.load();
            childItem.duplicate();
            CondItem nonNullCond = childItem.asNonNullCond();
            Chain ifNullJumps = nonNullCond.falseJumps();
            code.resolve(nonNullCond.trueChain);
            genExpr(key).load();
            result = items.mkAccessItem().asSafe(items.mkLiteral(null),
                    mergeChains(exprSafeItem.coalesceChain, ifNullJumps)).t(tree);
        } else {
            genExpr(expr).load();
            genExpr(key).load();
            result = items.mkAccessItem().t(tree);
        }
    }

    @Override
    public void visitInvocation(Invocation tree) {
        Assert.check(tree.target instanceof MemberAccess);
        Name callee = ((MemberAccess) tree.target).member;

        switch (callee.toString()) {
            case "length":
                genExpr(tree.args.first().expr).load();
                code.putPos(tree.pos);
                code.addInstruction(length);
                result = items.mkStackItem();
                break;
            case "list":
                genExpr(tree.args.first().expr).load();
                code.putPos(tree.pos);
                code.addInstruction(newlist);
                result = items.mkStackItem();
                break;
            default:
                tree.args.forEach(a -> genExpr(a.expr).load());
                code.putPos(tree.pos);
                code.addInstruction(new Call(tree.sym.id, tree.args.count()));
                result = items.mkStackItem();
        }
    }

    @Override
    public void visitAssign(Assign tree) {
        Item varItem = genExpr(tree.var);
        genExpr(tree.expr).load();
        result = items.makeAssignItem(varItem).t(tree);
    }

    @Override
    public void visitCompoundAssign(CompoundAssign tree) {
        Item varItem = genExpr(tree.var);
        if (tree.hasTag(Tag.ASG_COALESCE)) {
            varItem.duplicate();
            CondItem presentCond = varItem.asPresentCond();
            Chain skipCoalesceChain = presentCond.trueJumps();
            code.resolve(presentCond.falseChain);
            genExpr(tree.expr).load();
            result = varItem.coalesceAsg(skipCoalesceChain).t(tree);
        } else {
            varItem.load();
            genExpr(tree.expr).load();
            code.addInstruction(fromBinaryAsgOpTag(tree.tag));
            result = items.makeAssignItem(varItem).t(tree);
        }
    }

    @Override
    public void visitTernaryOp(TernaryOp tree) {
        CondItem condItem = genExpr(tree.cond).asCond();
        Chain falseJumps = condItem.falseJumps();
        code.resolve(condItem.trueChain);
        genExpr(tree.ths).load();
        Chain trueJumps = code.branch(new Goto());
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
                result = items.makeCondItem(rhsCond.opcode, rhsCond.trueChain, skipOtherConditionsChain).t(tree);
                break;
            }

            case OR: {
                CondItem lhsCond = genExpr(tree.lhs).asCond();
                Chain trueJumps = lhsCond.trueJumps();
                code.resolve(lhsCond.falseChain);
                CondItem rhsCond = genExpr(tree.rhs).asCond();
                Chain skipOtherConditionsChain = mergeChains(trueJumps, rhsCond.trueChain);
                result = items.makeCondItem(rhsCond.opcode, skipOtherConditionsChain, rhsCond.falseChain).t(tree);
                break;
            }

            case EQ: case NE:
            case GT: case GE:
            case LT: case LE:
                genExpr(tree.lhs).load();
                genExpr(tree.rhs).load();
                result = items.makeCondItem(fromComparisonOpTag(tree.tag)).t(tree);
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
                code.addInstruction(fromBinaryOpTag(tree.tag));
                result = items.mkStackItem();
        }
    }

    @Override
    public void visitUnaryOp(UnaryOp tree) {
        switch (tree.tag) {
            case POSTINC: case POSTDEC:
            case PREINC: case PREDEC:
                result = genExpr(tree.expr).increase(tree.tag).t(tree);
                break;

            case NOT:
                result = genExpr(tree.expr).asCond().negated().t(tree);
                break;

            case NULLCHK:
                result = genExpr(tree.expr).asNonNullCond().t(tree);
                break;

            default:
                genExpr(tree.expr).load();
                code.markTreePos(tree);
                code.addInstruction(fromUnaryOpTag(tree.tag));
                result = items.mkStackItem();
                // break is unnecessary
        }
    }
}