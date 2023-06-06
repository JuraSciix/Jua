package jua.compiler;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import jua.compiler.Code.Chain;
import jua.compiler.Items.*;
import jua.compiler.ProgramScope.ConstantSymbol;
import jua.compiler.Tree.*;
import jua.compiler.Types.LongType;
import jua.interpreter.Address;
import jua.interpreter.instruction.*;
import jua.runtime.Function;
import jua.utils.Assert;
import jua.utils.List;

import java.util.Objects;

import static jua.compiler.Code.mergeChains;
import static jua.compiler.InstructionFactory.*;
import static jua.compiler.InstructionUtils.*;
import static jua.compiler.TreeInfo.*;

public final class Gen extends Scanner {

    boolean genJvmLoops;

    Code code;

    Items items;

    Source source;

    FlowEnv flow;

    Item result;

    Item genExpr(Expression tree) {
        Item prevItem = result;
        try {
            tree.accept(this);
            return result;
        } finally {
            result = prevItem;
        }
    }

    @Override
    public void visitCompilationUnit(CompilationUnit tree) {
        code = tree.sym.code;
        code.putPos(0);
        items = new Items(code);
        scan(tree.stats);
        emitLeave();
        tree.sym.runtimefunc = new Function(
                "<main>",
                source.fileName,
                0,
                0,
                new String[0],
                new Address[0],
                0L,
                code.buildCodeSegment()
        );
    }

    @Override
    public void visitArrayAccess(ArrayAccess tree) {
        genAccess(tree, tree.expr, tree.index, Tag.ARRACCSF);
    }

    @Override
    public void visitMemberAccess(MemberAccess tree) {
        genAccess(tree, tree.expr, tree.member.toLiteral(), Tag.MEMACCSF);
    }

    private void genAccess(Expression tree, Expression expr, Expression key, Tag coalescingTag) {
        Item exprItem = genExpr(expr);
        Item resultItem = items.makeAccess();
        if (tree.hasTag(coalescingTag)) {
            SafeItem exprSafeItem = exprItem.asSafe();
            SafeItem resultSafeItem = items.makeNullSafe(resultItem);
            resultItem = resultSafeItem;
            Item safeChildItem = exprSafeItem.child.load();
            safeChildItem.duplicate();
            CondItem nonNullCond = safeChildItem.nullCheck();
            resultSafeItem.exitChain =
                    Code.mergeChains(exprSafeItem.exitChain,
                            nonNullCond.falseJumps());
            code.resolve(nonNullCond.trueChain);
        } else {
            exprItem.load();
        }
        genExpr(key).load();
        result = Items.treeify(resultItem, tree);
    }

    @Override
    public void visitListLiteral(ListLiteral tree) {
        code.putPos(tree.pos);
        items.makeLiteral(new LongType(tree.entries.count())).load();
        code.addInstruction(newlist);
        long index = 0L;
        for (Expression entry : tree.entries) {
            items.makeStack().duplicate();
            items.makeLiteral(new LongType(index++)).load();
            genExpr(entry).load();
            items.makeAccess().store();
        }
        result = items.makeStack();
    }

    @Override
    public void visitMapLiteral(MapLiteral tree) {
        code.putPos(tree.pos);
        code.addInstruction(newmap);
        for (MapLiteral.Entry entry : tree.entries) {
            items.makeStack().duplicate();
            genExpr(entry.key).load();
            genExpr(entry.value).load();
            code.putPos(entry.pos);
            items.makeAccess().store();
        }
        result = items.makeStack();
    }

    @Override
    public void visitBreak(Break tree) {
        FlowEnv env = flow;
        Objects.requireNonNull(env);
        code.putPos(tree.pos);
        env.exitjumps = mergeChains(env.exitjumps, code.branch(new Goto()));
        code.dead();
    }

    @Override
    public void visitSwitch(Switch tree) {
        genExpr(tree.expr).load();
        SwitchEnv env = new SwitchEnv(flow);
        flow = env;
        env.switchStartPC = code.currentIP();
        code.putPos(tree.pos);
        code.addInstruction(new Fake(-1)); // Резервируем место под инструкцию

        for (Case c : tree.cases) {
            c.accept(this);
            env.resolveCont();
            env.contjumps = null;
        }

        if (env.switchDefaultOffset == -1) {
            // Явного default-case не было
            env.switchDefaultOffset = code.currentIP() - env.switchStartPC;
        }

        if (env.caseLabelsConstantIndexes.size() <= 16) {
            code.setInstruction(env.switchStartPC,
                    new Linearswitch(
                            env.caseLabelsConstantIndexes.toIntArray(),
                            env.switchCaseOffsets.toIntArray(),
                            env.switchDefaultOffset
                    )
            );
        } else {
            code.setInstruction(env.switchStartPC,
                    new Binaryswitch(
                            env.caseLabelsConstantIndexes.toIntArray(),
                            env.switchCaseOffsets.toIntArray(),
                            env.switchDefaultOffset
                    )
            );
        }


        env.resolveExit();

//        if (tree._final) {
//            // Ни один кейз не был закрыт с помощью break.
//            // Это значит, что после switch находится недостижимый код.
//            code.dead();
//        }

        flow = env.parent;
    }

    @Override
    public void visitCase(Case tree) {
        Assert.check(flow instanceof SwitchEnv);
        SwitchEnv env = (SwitchEnv) flow;
        if (tree.labels == null) {
            // default case
            env.switchDefaultOffset = code.currentIP() - env.switchStartPC;
        } else {
            for (Expression label : tree.labels) {
                env.caseLabelsConstantIndexes.add(genExpr(label).constantIndex());
                // Это не ошибка. Следующая строчка должна находиться именно в цикле
                // Потому что инструкция switch ассоциирует значения к переходам в масштабе 1 к 1.
                env.switchCaseOffsets.add(code.currentIP() - env.switchStartPC);
            }
        }

        boolean caseBodyAlive = genBranch(tree.body);

        if (caseBodyAlive) {
            // Неявный break
            flow.exitjumps = mergeChains(flow.exitjumps, code.branch(new Goto()));
        }
    }

    @Override
    public void visitContinue(Continue tree) {
        FlowEnv env = searchEnv(false);
        Objects.requireNonNull(env);
        code.putPos(tree.pos);
        env.contjumps = mergeChains(env.contjumps, code.branch(new Goto()));
        code.dead();
    }

    private FlowEnv searchEnv(boolean isSwitch) {
        for (FlowEnv env = flow; env != null; env = env.parent)
            if ((env instanceof SwitchEnv) == isSwitch)
                return env;
        return null;
    }

    @Override
    public void visitDoLoop(DoLoop tree) {
        genLoop(tree.pos, null, tree.cond, null, tree.body, false);
    }

    @Override
    public void visitFallthrough(Fallthrough tree) {
        FlowEnv env = searchEnv(true);
        Objects.requireNonNull(env);
        code.putPos(tree.pos);
        env.contjumps = mergeChains(env.contjumps, code.branch(new Goto()));
        code.dead();
    }

    @Override
    public void visitVarDef(VarDef tree) {
        code.putPos(tree.pos);
        for (VarDef.Definition def : tree.defs) {
            if (def.init == null) {
                items.makeLiteral(Types.TYPE_NULL).load();
            } else {
                genExpr(def.init).load();
            }
            Items.treeify(
                    items.makeAssign(
                            items.makeLocal(code.resolveLocal(def.name))),
                    tree).drop();
        }
    }

    @Override
    public void visitForLoop(ForLoop tree) {
        genLoop(tree.pos, tree.init, tree.cond, tree.step, tree.body, true);
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
                result = items.makeStack();
                break;
            case "list":
                genExpr(tree.args.first().expr).load();
                code.putPos(tree.pos);
                code.addInstruction(newlist);
                result = items.makeStack();
                break;
            default:
                visitInvocationArgs(tree.args);
                code.putPos(tree.pos);
                code.addInstruction(new Call(tree.sym.id, tree.args.count()));
                result = items.makeStack();
        }
    }

    private void visitInvocationArgs(List<Invocation.Argument> args) {
        args.forEach(argument -> genExpr(argument.expr).load());
    }

    @Override
    public void visitFuncDef(FuncDef tree) {
        code = tree.sym.code;
        code.putPos(tree.pos);
        items = new Items(code);

        List<Address> defaults = new List<>();
        for (FuncDef.Parameter param : tree.params) {
            code.resolveLocal(param.name);
            if (param.expr != null) {
                Literal literal = (Literal) stripParens(param.expr);
                Address address = new Address();
                literal.type.write2address(address);
                defaults.add(address);
            }
        }

        assert tree.body != null;

        if (tree.body.hasTag(Tag.BLOCK)) {
            genBranch(tree.body);
            code.addInstruction(leave);
        } else {
            Assert.check(tree.body.hasTag(Tag.DISCARDED), "Function body neither block ner expression");
            genExpr(((Discarded) tree.body).expr).load();
            code.addInstruction(return_);
        }
        code.dead();

        tree.sym.runtimefunc = new Function(
                tree.name.toString(),
                source.fileName,
                tree.params.count() - defaults.count(),
                tree.params.count(),
                tree.params.map(param -> param.name.toString()).toArray(String[]::new),
                defaults.toArray(Address[]::new),
                0L,
                code.buildCodeSegment()
        );
    }

    @Override
    public void visitIf(If tree) {
        CondItem cond = genExpr(tree.cond).asCond();
        Chain falseJumps = cond.falseJumps();
        code.resolve(cond.trueChain);
        boolean thenalive = genBranch(tree.thenbody);
        if (tree.elsebody == null) {
            code.resolve(falseJumps);
        } else {
            Chain trueChain = thenalive ? code.branch(new Goto()) : null;
            code.resolve(falseJumps);
            boolean elsealive = genBranch(tree.elsebody);
            code.resolve(trueChain);

            if (!thenalive && !elsealive) code.dead();
        }
    }

    private void assertStacktopEquality(int limitstacktop) {
        Assert.check(code.curStackTop() == limitstacktop, "limitstacktop mismatch (" +
                "before: " + limitstacktop + ", " +
                "after: " + code.curStackTop() + ", " +
                "current CP: " + code.currentIP() + ", " +
                "current line num: " + code.lastLineNum() +
                ")");
    }

    @Override
    public void visitParens(Parens tree) {
        tree.expr.accept(this);
    }

    @Override
    public void visitReturn(Tree.Return tree) {
        code.putPos(tree.pos);
        if (tree.expr == null || isLiteralNull(tree.expr)) {
            code.addInstruction(leave);
        } else {
            genExpr(tree.expr).load();
            code.addInstruction(return_);
        }
        code.dead();
    }

    private void emitLeave() {
        code.addInstruction(leave);
        code.dead();
    }

    @Override
    public void visitTernaryOp(TernaryOp tree) {
        CondItem cond = genExpr(tree.cond).asCond();
        Chain falseJumps = cond.falseJumps();
        code.resolve(cond.trueChain);
        genExpr(tree.thenexpr).load();
        Chain trueJumps = code.branch(new Goto());
        code.resolve(falseJumps);
        genExpr(tree.elseexpr).load();
        code.resolve(trueJumps);
        result = items.makeStack();
    }

    @Override
    public void visitVariable(Var tree) {
        if (tree.sym instanceof ConstantSymbol) {
            code.putPos(tree.pos);
            code.addInstruction(new Getconst(tree.sym.id));
            result = items.makeStack();
        } else {
            result = Items.treeify(
                    items.makeLocal(code.resolveLocal(tree.name)), tree);
        }
    }

    @Override
    public void visitWhileLoop(WhileLoop tree) {
        genLoop(tree.pos, null, tree.cond, null, tree.body, true);
    }

    private static boolean isInfiniteLoopCond(Expression tree) {
        return tree == null || isLiteralTrue(tree);
    }

    private void genLoop(
            int pos,
            List<Statement> init,
            Expression cond,
            List<Discarded> update,
            Statement body,
            boolean testFirst
    ) {
        code.putPos(pos);

        scan(init);

        flow = new FlowEnv(flow);

        boolean infinitecond = isInfiniteLoopCond(cond);

        if (genJvmLoops) {
            genJvmLoop(cond, update, body, testFirst, infinitecond);
        } else {
            genJuaLoop(cond, update, body, testFirst, infinitecond);
        }

        flow.resolveExit();
        flow = flow.parent;
    }

    private void genJvmLoop(Expression cond,
                            List<Discarded> update, Statement body, boolean testFirst,
                            boolean infinitecond) {
        int loopstartPC = code.currentIP();
        if (infinitecond) {
            genBranch(body);
            flow.resolveCont(loopstartPC);
            code.resolve(code.branch(new Goto()), loopstartPC);
        } else {
            if (testFirst) {
                CondItem condItem = genExpr(cond).asCond();
                Chain falseJumps = condItem.falseJumps();
                code.resolve(condItem.trueChain);
                genBranch(body);
                flow.resolveCont(loopstartPC);
                scan(update);
                code.resolve(code.branch(new Goto()), loopstartPC);
                code.resolve(falseJumps);
            } else {
                genBranch(body);
                flow.resolveCont();
                scan(update);
                CondItem condItem = genExpr(cond).asCond();
                code.resolve(condItem.trueJumps(), loopstartPC);
                code.resolve(condItem.falseChain);
            }
        }
    }

    private void genJuaLoop(Expression cond, List<Discarded> update,
                            Statement body, boolean testFirst, boolean infinitecond) {
        int loopstartPC;
        if (testFirst && !infinitecond) {
            Chain skipBodyPC = code.branch(new Goto());
            loopstartPC = code.currentIP();
            genBranch(body);
            flow.resolveCont();
            scan(update);
            code.resolve(skipBodyPC);
        } else {
            loopstartPC = code.currentIP();
            genBranch(body);
            flow.resolveCont();
            scan(update);
        }
        if (infinitecond) {
            code.resolve(code.branch(new Goto()), loopstartPC);
        } else {
            CondItem condItem = genExpr(cond).asCond();
            code.resolve(condItem.trueJumps(), loopstartPC);
            code.resolve(condItem.falseChain);
        }
    }

    public void visitBinaryOp(BinaryOp tree) {
        switch (tree.tag) {
            case AND: {
                CondItem lcond = Items.treeify(genExpr(tree.lhs).asCond(), tree);
                Chain falseJumps = lcond.falseJumps();
                code.resolve(lcond.trueChain);
                CondItem rcond = Items.treeify(genExpr(tree.rhs).asCond(), tree);
                result = items.makeCond(rcond.opcode,
                        rcond.trueChain,
                        mergeChains(falseJumps, rcond.falseChain));
                break;
            }

            case OR: {
                CondItem lcond = Items.treeify(genExpr(tree.lhs).asCond(), tree);
                Chain trueJumps = lcond.trueJumps();
                code.resolve(lcond.falseChain);
                CondItem rcond = Items.treeify(genExpr(tree.rhs).asCond(), tree);
                result = items.makeCond(rcond.opcode,
                        mergeChains(trueJumps, rcond.trueChain),
                        rcond.falseChain);
                break;
            }

            case EQ: case NE:
                if (isLiteralNull(tree.rhs)) {
                    Item expr = genExpr(tree.lhs).load();
                    result = Items.treeify(tree.hasTag(Tag.NE)
                            ? expr.nullCheck()
                            : expr.nullCheck().negate(), tree);
                    break;
                }
                if (isLiteralNull(tree.lhs)) {
                    Item expr = genExpr(tree.rhs).load();
                    result = Items.treeify(tree.hasTag(Tag.NE)
                            ? expr.nullCheck()
                            : expr.nullCheck().negate(), tree);
                    break;
                }
                // fallthrough

            case GT: case GE:
            case LT: case LE:
                genExpr(tree.lhs).load();
                genExpr(tree.rhs).load();
                result = Items.treeify(items.makeCond(fromComparisonOpTag(tree.tag)), tree);
                break;

            case COALESCE: {
                SafeItem lhsSafeItem = genExpr(tree.lhs).asSafe();
                Item item = lhsSafeItem.child.load();
                item.duplicate();
                CondItem nonNullCond = item.nullCheck(); // treeify is unnecessary
                Chain whenNonNullChain = nonNullCond.trueJumps();
                lhsSafeItem.exitChain =
                        Code.mergeChains(
                                lhsSafeItem.exitChain,
                                whenNonNullChain);
                code.resolve(nonNullCond.falseChain);
                item.drop();
                genExpr(tree.rhs).load();
                code.resolve(lhsSafeItem.exitChain);
                result = items.makeStack();
                break;
            }

            default:
                genExpr(tree.lhs).load();
                genExpr(tree.rhs).load();
                code.putPos(tree.pos);
                code.addInstruction(fromBinaryOpTag(tree.tag));
                result = items.makeStack();
        }
    }

    @Override
    public void visitUnaryOp(UnaryOp tree) {
        switch (tree.tag) {
            case POSTDEC: case PREDEC:
            case POSTINC: case PREINC:
                result = genExpr(tree.expr).increase(tree.tag);
                break;

            case NOT:
                CondItem item = genExpr(tree.expr).asCond();
                result = Items.treeify(item.negate(), tree);
                break;

            default:
                genExpr(tree.expr).load();
                code.putPos(tree.pos);
                code.addInstruction(fromUnaryOpTag(tree.tag));
                result = items.makeStack();
        }
    }

    @Override
    public void visitAssign(Assign tree) {
        Item varItem = genExpr(tree.var);
        genExpr(tree.expr).load();
        result = Items.treeify(items.makeAssign(varItem), tree);
    }

    @Override
    public void visitCompoundAssign(CompoundAssign tree) {
        Item varItem = genExpr(tree.var);
        if (tree.hasTag(Tag.ASG_COALESCE)) {
            varItem.duplicate();
            CondItem presentCond = varItem.presentCheck();
            Chain whenItemPresentChain = presentCond.trueJumps();
            code.resolve(presentCond.falseChain);
            result = varItem.coalesce(genExpr(tree.expr), whenItemPresentChain);
        } else {
            varItem.load();
            genExpr(tree.expr).load();
            code.addInstruction(fromBinaryAsgOpTag(tree.tag));
            result = Items.treeify(items.makeAssign(varItem), tree);
        }
    }

    @Override
    public void visitLiteral(Literal tree) {
        result = items.makeLiteral(tree.type);
    }

    @Override
    public void visitDiscarded(Discarded tree) {
        genExpr(tree.expr).drop();
    }

    /**
     * Генерирует код оператора в дочерней ветке и возвращает жива ли она.
     */
    private boolean genBranch(Statement statement) {
        int backedStackTop = code.curStackTop();

        try {
            statement.accept(this);
            return code.isAlive();
        } finally {
            code.setAlive();
            assertStacktopEquality(backedStackTop);
        }
    }

    class FlowEnv {

        final FlowEnv parent;

        Chain contjumps = null;
        Chain exitjumps = null;

        FlowEnv(FlowEnv parent) {
            this.parent = parent;
        }

        void resolveCont() { code.resolve(contjumps); }
        void resolveCont(int cp) { code.resolve(contjumps, cp); }
        void resolveExit() { code.resolve(exitjumps); }
        void resolveExit(int cp) { code.resolve(exitjumps, cp); }
    }

    class SwitchEnv extends FlowEnv {

        /** Указатель на инструкцию, где находится switch. */
        int switchStartPC;
        /** Индексы констант из ключей кейзов. Равно null когда isSwitch=false */
        final IntArrayList caseLabelsConstantIndexes = new IntArrayList();
        /** Точка входа (IP) для каждого кейза. Равно null когда isSwitch=false */
        final IntArrayList switchCaseOffsets = new IntArrayList();
        /** Указатель на точку входа в default-case */
        int switchDefaultOffset = -1;

        SwitchEnv(FlowEnv parent) {
            super(parent);
        }
    }
}