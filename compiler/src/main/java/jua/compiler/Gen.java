package jua.compiler;

import jua.compiler.Code.Chain;
import jua.compiler.Items.*;
import jua.compiler.ProgramScope.ConstantSymbol;
import jua.compiler.Tree.*;
import jua.compiler.Tree.Return;
import jua.compiler.Types.LongType;
import jua.interpreter.Address;
import jua.interpreter.instruction.*;
import jua.runtime.Function;
import jua.utils.Assert;
import jua.utils.List;

import java.util.ArrayList;

import static jua.compiler.InstructionFactory.*;
import static jua.compiler.InstructionUtils.*;
import static jua.compiler.TreeInfo.isLiteralNull;
import static jua.compiler.TreeInfo.stripParens;

public class Gen extends Scanner {

    class FlowEnv {

        final FlowEnv parent;

        Chain contChain = null;
        Chain exitChain = null;

        FlowEnv(FlowEnv parent) {
            this.parent = parent;
        }

        void resolveCont() { code.resolve(contChain); }
        void resolveCont(int cp) { code.resolve(contChain, cp); }
        void resolveExit() { code.resolve(exitChain); }
        void resolveExit(int cp) { code.resolve(exitChain, cp); }
    }

    class SwitchEnv extends FlowEnv {

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
     * Генерирует код оператора в дочерней ветке и возвращает жива ли она.
     */
    private boolean genBranch(Statement statement) {
        boolean alive = code.isAlive();
        int tos = code.tos();

        try {
            statement.accept(this);
            return code.isAlive();
        } finally {
            if (alive) {
                code.setAlive();
            }
            code.assertTosEquality(tos);
        }
    }

    @Override
    public void scan(Tree tree) {
        try {
            tree.accept(this);
        } catch (Throwable e) {
            System.err.printf("ERROR: PC=%d, LN=%d %n", code.pc(), code.lineNum());
            throw e;
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
                code.buildCodeSegment()
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
        CondItem condItem = genExpr(tree.cond).asCond();
        Chain falseJumps = condItem.elseJumps();
        code.resolve(condItem.thenChain);
        boolean tbState = genBranch(tree.thenbody); // then branch state
        if (tree.elsebody == null) {
            code.resolve(falseJumps);
        } else {
            Chain avoidElseBranchChain = tbState ? code.branch(new Goto()) : null;
            code.resolve(falseJumps);
            boolean ebState = genBranch(tree.elsebody); // else branch state
            code.resolve(avoidElseBranchChain);

            if (!tbState && !ebState) code.dead();
        }
    }

    @Override
    public void visitWhileLoop(WhileLoop tree) {
        genLoop(tree.pos, null, tree.cond, null, tree.body, true);
    }

    @Override
    public void visitDoLoop(DoLoop tree) {
        genLoop(tree.pos, null, tree.cond, null, tree.body, false);
    }

    @Override
    public void visitForLoop(ForLoop tree) {
        genLoop(tree.pos, tree.init, tree.cond, tree.step, tree.body, true);
    }

    private void genLoop(int pos, List<Statement> init, Expression cond, List<Discarded> update, Statement body, boolean testFirst) {
        code.putPos(pos);

        scan(init);

        flow = new FlowEnv(flow);

        boolean condInfinite = (cond == null || TreeInfo.isLiteralTrue(cond));

        if (genJvmLoops) {
            genJvmLoop(cond, update, body, testFirst, condInfinite);
        } else {
            genJuaLoop(cond, update, body, testFirst, condInfinite);
        }

        flow.resolveExit();
        flow = flow.parent;
    }

    private void genJvmLoop(Expression cond,
                            List<Discarded> update, Statement body, boolean testFirst,
                            boolean infinitecond) {
        int loopstartPC = code.pc();
        if (infinitecond) {
            genBranch(body);
            flow.resolveCont(loopstartPC);
            code.resolve(code.branch(new Goto()), loopstartPC);
        } else {
            if (testFirst) {
                CondItem condItem = genExpr(cond).asCond();
                Chain falseJumps = condItem.elseJumps();
                code.resolve(condItem.thenChain);
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
                code.resolve(condItem.thenJumps(), loopstartPC);
                code.resolve(condItem.elseChain);
            }
        }
    }

    private void genJuaLoop(Expression cond, List<Discarded> update,
                            Statement body, boolean testFirst, boolean infinitecond) {
        int loopstartPC;
        if (testFirst && !infinitecond) {
            Chain skipBodyPC = code.branch(new Goto());
            loopstartPC = code.pc();
            genBranch(body);
            flow.resolveCont();
            scan(update);
            code.resolve(skipBodyPC);
        } else {
            loopstartPC = code.pc();
            genBranch(body);
            flow.resolveCont();
            scan(update);
        }
        if (infinitecond) {
            code.resolve(code.branch(new Goto()), loopstartPC);
        } else {
            CondItem condItem = genExpr(cond).asCond();
            code.resolve(condItem.thenJumps(), loopstartPC);
            code.resolve(condItem.elseChain);
        }
    }

    @Override
    public void visitSwitch(Switch tree) {
        genExpr(tree.expr).load();
        SwitchEnv env = new SwitchEnv(flow);
        flow = env;
        env.switchStartPC = code.pc();
        code.putPos(tree.pos);
        code.addInstruction(new Fake(-1)); // Резервируем место под инструкцию

        for (Case c : tree.cases) {
            c.accept(this);
            env.resolveCont();
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
            env.switchDefaultOffset = code.pc() - env.switchStartPC;
        } else {
            for (Expression label : tree.labels) {
                env.caseLabelsConstantIndexes.add(genExpr(label).constantIndex());
                // Это не ошибка. Следующая строчка должна находиться именно в цикле
                // Потому что инструкция switch ассоциирует значения к переходам в масштабе 1 к 1.
                env.switchCaseOffsets.add(code.pc() - env.switchStartPC);
            }
        }

        boolean caseBodyAlive = genBranch(tree.body);

        if (caseBodyAlive) {
            // Неявный break
            flow.exitChain = Code.mergeChains(flow.exitChain, code.branch(new Goto()));
        }
    }

    @Override
    public void visitBreak(Break tree) {
        FlowEnv env = searchEnv(false);
        Assert.checkNonNull(env);
        code.putPos(tree.pos);
        env.exitChain = Code.mergeChains(env.exitChain, code.branch(new Goto()));
        code.dead();
    }

    @Override
    public void visitContinue(Continue tree) {
        FlowEnv env = searchEnv(false);
        Assert.checkNonNull(env);
        code.putPos(tree.pos);
        env.contChain = Code.mergeChains(env.contChain, code.branch(new Goto()));
        code.dead();
    }

    @Override
    public void visitFallthrough(Fallthrough tree) {
        FlowEnv env = searchEnv(true);
        Assert.checkNonNull(env);
        code.putPos(tree.pos);
        env.contChain = Code.mergeChains(env.contChain, code.branch(new Goto()));
        code.dead();
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
                items.makeLiteral(Types.TYPE_NULL).load();
            } else {
                genExpr(def.init).load();
            }
            items.makeAssign(items.makeLocal(code.resolveLocal(def.name))).drop();
        }
    }

    @Override
    public void visitReturn(Return tree) {
        code.putPos(tree.pos);
        if (tree.expr == null || isLiteralNull(tree.expr)) {
            code.addInstruction(leave);
        } else {
            genExpr(tree.expr).load();
            code.addInstruction(return_);
        }
        code.dead();
    }

    @Override
    public void visitDiscarded(Discarded tree) {
        genExpr(tree.expr).drop();
    }

    @Override
    public void visitLiteral(Literal tree) {
        result = items.makeLiteral(tree.type);
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
    public void visitVariable(Var tree) {
        if (tree.sym instanceof ConstantSymbol) {
            code.putPos(tree.pos);
            code.addInstruction(new Getconst(tree.sym.id));
            result = items.makeStack();
        } else {
            result = items.makeLocal(code.resolveLocal(tree.name)).treeify(tree);
        }
    }

    @Override
    public void visitMemberAccess(MemberAccess tree) {
        genAccess(tree, tree.expr, tree.member.toLiteral(), Tag.MEMACCSF);
    }

    @Override
    public void visitArrayAccess(ArrayAccess tree) {
        genAccess(tree, tree.expr, tree.index, Tag.ARRACCSF);
    }

    private void genAccess(Expression tree, Expression expr, Expression key, Tag safeTag) {
        Item exprItem = genExpr(expr);
        Item resultItem = items.makeAccess();
        if (tree.hasTag(safeTag)) {
            SafeItem exprSafeItem = exprItem.asSafe();
            SafeItem resultSafeItem = items.makeNullSafe(resultItem);
            resultItem = resultSafeItem;
            Item safeChildItem = exprSafeItem.child.load();
            safeChildItem.duplicate();
            CondItem nonNullCond = safeChildItem.nonNullCheck();
            resultSafeItem.whenNullChain = nonNullCond.elseJumps();
            resultSafeItem.whenNonNullChain = nonNullCond.thenChain;
        } else {
            exprItem.load();
        }
        genExpr(key).load();
        result = resultItem.treeify(tree);
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
                tree.args.forEach(a -> genExpr(a.expr).load());
                code.putPos(tree.pos);
                code.addInstruction(new Call(tree.sym.id, tree.args.count()));
                result = items.makeStack();
        }
    }

    @Override
    public void visitAssign(Assign tree) {
        Item varItem = genExpr(tree.var);
        genExpr(tree.expr).load();
        result = items.makeAssign(varItem).treeify(tree);
    }

    @Override
    public void visitCompoundAssign(CompoundAssign tree) {
        Item varItem = genExpr(tree.var);
        if (tree.hasTag(Tag.ASG_COALESCE)) {
            varItem.duplicate();
            CondItem presentCond = varItem.presentCheck();
            Chain whenItemPresentChain = presentCond.thenJumps();
            code.resolve(presentCond.elseChain);
            result = varItem.coalesce(genExpr(tree.expr), whenItemPresentChain);
        } else {
            varItem.load();
            genExpr(tree.expr).load();
            code.addInstruction(fromBinaryAsgOpTag(tree.tag));
            result = items.makeAssign(varItem).treeify(tree);
        }
    }

    @Override
    public void visitTernaryOp(TernaryOp tree) {
        CondItem condItem = genExpr(tree.cond).asCond();
        Chain falseJumps = condItem.elseJumps();
        code.resolve(condItem.thenChain);
        genExpr(tree.thenexpr).load();
        Chain trueJumps = code.branch(new Goto());
        code.resolve(falseJumps);
        genExpr(tree.elseexpr).load();
        code.resolve(trueJumps);
        result = items.makeStack();
    }

    @Override
    public void visitBinaryOp(BinaryOp tree) {
        switch (tree.tag) {
            case AND: {
                CondItem lcond = genExpr(tree.lhs).asCond();
                Chain falseJumps = lcond.elseJumps();
                code.resolve(lcond.thenChain);
                CondItem rcond = genExpr(tree.rhs).asCond();
                result = items.makeCond(rcond.opcode,
                        rcond.thenChain,
                        Code.mergeChains(falseJumps, rcond.elseChain))
                        .treeify(tree);
                break;
            }

            case OR: {
                CondItem lcond = genExpr(tree.lhs).asCond();
                Chain trueJumps = lcond.thenJumps();
                code.resolve(lcond.elseChain);
                CondItem rcond = genExpr(tree.rhs).asCond();
                result = items.makeCond(rcond.opcode,
                        Code.mergeChains(trueJumps, rcond.thenChain),
                        rcond.elseChain)
                        .treeify(tree);
                break;
            }

            case EQ: case NE:
                if (isLiteralNull(tree.rhs)) {
                    Item lhsItem = genExpr(tree.lhs).load();
                    result = (tree.hasTag(Tag.NE)
                            ? lhsItem.nonNullCheck()
                            : lhsItem.nonNullCheck().negate())
                            .treeify(tree);
                    break;
                }
                if (isLiteralNull(tree.lhs)) {
                    Item rhsItem = genExpr(tree.rhs).load();
                    result = (tree.hasTag(Tag.NE)
                            ? rhsItem.nonNullCheck()
                            : rhsItem.nonNullCheck().negate())
                            .treeify(tree);
                    break;
                }
                // fallthrough

            case GT: case GE:
            case LT: case LE:
                genExpr(tree.lhs).load();
                genExpr(tree.rhs).load();
                result = items.makeCond(fromComparisonOpTag(tree.tag)).treeify(tree);
                break;

            case COALESCE: {
                SafeItem lhsSafeItem = genExpr(tree.lhs).asSafe();
                Item item = lhsSafeItem.child.load();
                item.duplicate();
                CondItem nonNullCond = item.nonNullCheck(); // treeify is unnecessary
                Chain whenNonNullChain = nonNullCond.thenJumps();
                code.resolve(nonNullCond.elseChain);
                code.resolve(lhsSafeItem.whenNullChain);
                item.drop();
                genExpr(tree.rhs).load();
                code.resolve(whenNonNullChain);
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
                result = genExpr(tree.expr).increase(tree.tag).treeify(tree);
                break;

            case NOT:
                CondItem exprItem = genExpr(tree.expr).asCond();
                result = exprItem.negate().treeify(tree);
                break;

            default:
                genExpr(tree.expr).load();
                code.putPos(tree.pos);
                code.addInstruction(fromUnaryOpTag(tree.tag));
                result = items.makeStack();
                // break is unnecessary
        }
    }
}