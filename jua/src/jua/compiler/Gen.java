package jua.compiler;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import jua.compiler.Tree.*;
import jua.compiler.Items.*;
import jua.interpreter.instruction.*;
import jua.runtime.JuaFunction;
import jua.util.Assert;

import java.util.List;

import static jua.compiler.InstructionFactory.*;
import static jua.compiler.InstructionUtils.*;
import static jua.compiler.TreeInfo.*;
import static jua.util.Collections.mergeIntLists;

public final class Gen extends Scanner {

    @Deprecated
    private static final boolean GEN_JVM_LOOPS = false;

    private final ProgramLayout programLayout;

    Code code;

    Log log;
    
    Items items;

    Gen(ProgramLayout programLayout) {
        this.programLayout = programLayout;
    }

    @Override
    public void visitCompilationUnit(CompilationUnit tree) {
        code = tree.code;
        code.putPos(0);
        items = new Items(code);
        log = tree.source.getLog();
        scan(tree.stats);
        emitLeave();
        resultFunc = JuaFunction.fromCode(
                "<main>",
                0,
                0,
                code.buildCodeSegment(),
                source.name
        );
    }

    private void genFlowAnd(BinaryOp tree) {
        CondItem lcond = genCond(tree.lhs);
        lcond.resolveTrueJumps();
        CondItem rcond = genCond(tree.rhs);
        result = items.makeCond(rcond.opcodePC, rcond.truejumps, mergeIntLists(lcond.falsejumps, rcond.falsejumps));
    }

    private void genFlowOr(BinaryOp tree) {
        CondItem lcond = genCond(tree.lhs).negate();
        lcond.resolveTrueJumps();
        CondItem rcond = genCond(tree.rhs);
        result = items.makeCond(rcond.opcodePC, mergeIntLists(lcond.falsejumps, rcond.falsejumps), rcond.truejumps);
    }

    @Override
    public void visitArrayAccess(ArrayAccess tree) {
        genExpr(tree.expr).load();
        genExpr(tree.index).load();
        result = items.makeAccess(tree.pos);
    }

    @Override
    public void visitArrayLiteral(ArrayLiteral tree) {
        code.putPos(tree.pos);
        emitNewArray();
        result = genArrayInitializr(tree.entries);
    }

    Item genArrayInitializr(List<ArrayLiteral.Entry> entries) {
        long implicitIndex = 0L;
        Item item = items.makeStack();
        for (ArrayLiteral.Entry entry : entries) {
            item.duplicate();
            if (entry.key == null) {
                code.putPos(entry.pos);
                emitPushLong(implicitIndex++);
            } else {
                genExpr(entry.key).load();
            }
            genExpr(entry.value).load();
            items.makeAccess(entry.pos).store();
        }
        return item;
    }

    private void emitNewArray() {
        code.addInstruction(newarray);
    }

    private void genBinary(BinaryOp tree) {
        genExpr(tree.lhs).load();
        genExpr(tree.rhs).load();
        code.putPos(tree.pos);
        code.addInstruction(fromBinaryOpTag(tree.tag));
        result = items.makeStack();
    }

    @Override
    public void visitBreak(Break tree) {
        FlowEnv env = flow;
        Assert.notNull(env);
        code.putPos(tree.pos);
        env.exitjumps.add(emitGoto());
        code.dead();
    }

    private int emitGoto() {
        return code.addInstruction(new Goto());
    }

    @Override
    public void visitSwitch(Tree.Switch tree) {
        genExpr(tree.expr).load();
        flow = new FlowEnv(flow, true);
        flow.switchStartPC = code.currentIP();
        code.putPos(tree.pos);
        code.addInstruction(new Fake(-1)); // Резервируем место под инструкцию

        for (Case c : tree.cases) {
            c.accept(this);
            flow.resolveCont();
            flow.contjumps.clear();
        }

        if (flow.switchDefaultOffset == -1) {
            // Явного default-case не было
            flow.switchDefaultOffset = code.currentIP() - flow.switchStartPC;
        }

        if (flow.caseLabelsConstantIndexes.size() <= 16) {
            code.setInstruction(flow.switchStartPC,
                    new Linearswitch(
                            flow.caseLabelsConstantIndexes.toIntArray(),
                            flow.switchCaseOffsets.toIntArray(),
                            flow.switchDefaultOffset
                    )
            );
        } else {
            code.setInstruction(flow.switchStartPC,
                    new Binaryswitch(
                            flow.caseLabelsConstantIndexes.toIntArray(),
                            flow.switchCaseOffsets.toIntArray(),
                            flow.switchDefaultOffset
                    )
            );
        }


        flow.resolveExit();

        if (tree._final) {
            // Ни один кейз не был закрыт с помощью break.
            // Это значит, что после switch находится недостижимый код.
            code.dead();
        }

        flow = flow.parent;
    }

    @Override
    public void visitCase(Case tree) {
        if (tree.labels == null) {
            // default
            flow.switchDefaultOffset = code.currentIP() - flow.switchStartPC;
        } else {
            for (Expression label : tree.labels) {
                flow.caseLabelsConstantIndexes.add(genExpr(label).constantIndex());
                // Это не ошибка. Следующая строчка должна находиться именно в цикле
                // Потому что инструкция switch ассоциирует значения к переходам в масштабе 1 к 1.
                flow.switchCaseOffsets.add(code.currentIP() - flow.switchStartPC);
            }
        }

        boolean caseBodyAlive = genBranch(tree.body);

        if (caseBodyAlive) {
            // Неявный break
            flow.exitjumps.add(emitGoto());
        }
    }

    @Override
    public void visitContinue(Continue tree) {
        FlowEnv env = searchEnv(false);
        Assert.notNull(env);
        code.putPos(tree.pos);
        env.contjumps.add(emitGoto());
        code.dead();
    }

    private FlowEnv searchEnv(boolean isSwitch) {
        for (FlowEnv env = flow; env != null; env = env.parent)
            if (env.isSwitch == isSwitch)
                return env;
        return null;
    }

    @Override
    public void visitDoLoop(DoLoop tree) {
        genLoop(tree._infinite, null, tree.cond, null, tree.body, false);
    }

    @Override
    public void visitFallthrough(Fallthrough tree) {
        FlowEnv env = searchEnv(true);
        Assert.notNull(env);
        code.putPos(tree.pos);
        env.contjumps.add(emitGoto());
        code.dead();
    }

    @Override
    public void visitForLoop(ForLoop tree) {
        genLoop(tree._infinite, tree.init, tree.cond, tree.step, tree.body, true);
    }

    @Override
    public void visitInvocation(Invocation tree) {
        Assert.check(tree.callee instanceof MemberAccess);
        Name callee = ((MemberAccess) tree.callee).member;

        int nargs = tree.args.size();

        switch (callee.value) {
//            case "print":
//                visitInvocationArgs(tree.args);
//                code.putPos(tree.pos);
//                code.addInstruction(new Print(nargs));
//                result = emptyItem;
//                break;
//
//            case "println":
//                visitInvocationArgs(tree.args);
//                code.putPos(tree.pos);
//                code.addInstruction(new Println(nargs));
//                result = emptyItem;
//                break;
//
            case "length":
            case "sizeof":

                genExpr(tree.args.get(0).expr).load();
                code.putPos(tree.pos);
                code.addInstruction(length);
                result = items.makeStack();
                break;
//
//            case "gettype":
//            case "typeof":
//                require_nargs(tree, 1);
//                genExpr(tree.args.get(0).expr).load();
//                code.putPos(tree.pos);
//                code.addInstruction(Gettype.INSTANCE);
//                result = stackItem;
//                break;
//
//            case "ns_time":
//                require_nargs(tree, 0);
//                code.putPos(tree.pos);
//                code.addInstruction(NsTime.INSTANCE);
//                result = stackItem;
//                break;

            default:
                int fn_idx = programLayout.tryFindFunc(callee);
                visitInvocationArgs(tree.args);
                code.putPos(tree.pos);
                code.addInstruction(new Call((short) fn_idx, (byte) nargs));
                result = items.makeStack();
        }
    }

    private void visitInvocationArgs(List<Invocation.Argument> args) {
        args.forEach(argument -> genExpr(argument.expr).load());
    }

    Source source;

    JuaFunction resultFunc;

    @Override
    public void visitFuncDef(FuncDef tree) {
        code = tree.code;
        code.putPos(tree.pos);
        items = new Items(code);
        log = source.getLog();

        int nOptionals = 0;
        for (FuncDef.Parameter param : tree.params) {
            Name name = param.name;
            code.resolveLocal(name.value);
            if (param.expr != null) {
                Expression expr = stripParens(param.expr);
                code.setLocalDefaultPCI(name, ((Literal) expr).type.resolvePoolConstant(code));
                nOptionals++;
            }
        }

        assert tree.body != null;

        if (tree.body.hasTag(Tag.BLOCK)) {
            if (genBranch(tree.body)) {
                emitLeave();
            }
        } else {
            Assert.check(tree.body.hasTag(Tag.DISCARDED), "Function body neither block ner expression");
            genExpr(((Discarded) tree.body).expr).load();
            code.addInstruction(jua.interpreter.instruction.Return.RETURN);
            code.dead();
        }

        resultFunc = JuaFunction.fromCode(
                tree.name.value,
                tree.params.size() - nOptionals,
                tree.params.size(),
                code.buildCodeSegment(),
                source.name
        );
    }

    @Override
    public void visitIf(If tree) {
        CondItem cond = genCond(tree.cond);
        cond.resolveTrueJumps();
        boolean alive = genBranch(tree.thenbody);
        if (tree.elsebody != null) {
            if (alive) {
                int skipperPC = emitGoto();
                cond.resolveFalseJumps();
                genBranch(tree.elsebody);
                code.resolveJump(skipperPC);
            } else {
                cond.resolveFalseJumps();
                alive = genBranch(tree.elsebody);
            }
        } else {
            cond.resolveFalseJumps();
        }

        if (!alive) {
            code.dead();
        }
    }

    private void assertStacktopEquality(int limitstacktop) {
        Assert.check(code.curStackTop() == limitstacktop, "limitstacktop mismatch (" +
                "before: " + limitstacktop + ", " +
                "after: " + code.curStackTop() + ", " +
                "code line num: " + code.lastLineNum() +
                ")");
    }

    private void genCmp(BinaryOp tree) {
        JumpInstruction opcode;

        if (isLiteralShort(tree.lhs)) {
            genExpr(tree.rhs).load();
            opcode = fromConstComparisonOpTag(tree.tag, getLiteralShort(tree.lhs));
        } else if (isLiteralShort(tree.rhs)) {
            genExpr(tree.lhs).load();
            opcode = fromConstComparisonOpTag(tree.tag, getLiteralShort(tree.rhs));
        } else if (isLiteralNull(tree.lhs)) {
            genExpr(tree.rhs).load();
            opcode = new Ifnonnull();
        } else if (isLiteralNull(tree.rhs)) {
            genExpr(tree.lhs).load();
            opcode = new Ifnonnull();
        } else {
            genExpr(tree.lhs).load();
            genExpr(tree.rhs).load();
            opcode = fromComparisonOpTag(tree.tag);
        }

        code.putPos(tree.pos);
        result = items.makeCond(code.addInstruction(opcode));
    }

    private void genNullCoalescing(BinaryOp tree) {
        Item lhs = genExpr(tree.lhs).load();
        lhs.duplicate();
        CondItem nonNull = lhs.nonNull();
        nonNull.resolveTrueJumps();
        code.addInstruction(pop);
        genExpr(tree.rhs).load();
        nonNull.resolveFalseJumps();
        result = items.makeStack();
    }

    @Override
    public void visitParens(Parens tree) {
        tree.expr.accept(this);
    }

    @Override
    public void visitAssign(Assign tree) {
        Expression var = stripParens(tree.var);
        switch (var.getTag()) {
            case MEMACCESS:
            case ARRAYACCESS:
            case VARIABLE:
                Item varitem = genExpr(tree.var);
                genExpr(tree.expr).load();
                result = items.makeAssign(tree.pos, varitem);
                break;

            default:
                Assert.error();
        }
    }

    private void generateUnary(UnaryOp tree) {
        switch (tree.tag) {
            case POSTDEC:
            case PREDEC:
            case POSTINC:
            case PREINC:
                genIncrease(tree);
                break;
            case NOT:
                result = genCond(tree.expr).negate();
                break;
            default:
                genExpr(tree.expr).load();
                code.putPos(tree.pos);
                code.addInstruction(InstructionUtils.fromUnaryOpTag(tree.tag));
                result = items.makeStack();
        }
    }

    @Override
    public void visitReturn(Tree.Return tree) {
        code.putPos(tree.pos);
        if (tree.expr == null || isLiteralNull(tree.expr)) {
            emitLeave();
        } else {
            genExpr(tree.expr).load();
            code.addInstruction(jua.interpreter.instruction.Return.RETURN);
            code.dead();
        }
    }

    private void emitLeave() {
        code.addInstruction(leave);
        code.dead();
    }

    @Override
    public void visitTernaryOp(TernaryOp tree) {
        int limitstacktop = code.curStackTop();
        code.putPos(tree.pos);
        CondItem cond = genCond(tree.cond);
        cond.resolveTrueJumps();
        int a = code.curStackTop();
        genExpr(tree.thenexpr).load();
        int exiterPC = emitGoto();
        cond.resolveFalseJumps();
        code.curStackTop(a);
        genExpr(tree.elseexpr).load();
        code.resolveJump(exiterPC);
        assertStacktopEquality(limitstacktop + 1);
        result = items.makeStack();
    }

    @Override
    public void visitVariable(Var tree) {
        Name name = tree.name;
        if (programLayout.hasConstant(name)) {
            code.putPos(tree.pos);
            code.addInstruction(new Getconst(programLayout.tryFindConst(name)));
            result = items.makeStack();
        } else {
            result = items.makeLocal(tree.pos, name, tree._defined);
        }
    }

    @Override
    public void visitMemberAccess(MemberAccess tree) {
        genExpr(tree.expr).load();
        emitPushString(tree.member.value);
        result = items.makeAccess(tree.pos);
    }

    @Override
    public void visitWhileLoop(WhileLoop tree) {
        genLoop(tree._infinite, null, tree.cond, null, tree.body, true);
    }

    FlowEnv flow;

    private void visitOptionalExpressionList(List<Expression> trees) {
        if (trees != null) {
            trees.forEach(tree -> genExpr(tree).drop());
        }
    }

    private static boolean isInfiniteLoopCond(Expression tree) {
        return tree == null || isLiteralTrue(tree);
    }
    
    private void genLoop(
            boolean _infinite,
            List<Expression> init,
            Expression cond,
            List<Expression> steps,
            Statement body,
            boolean testFirst
    ) {
        visitOptionalExpressionList(init);

        flow = new FlowEnv(flow, false);

        boolean infinitecond = isInfiniteLoopCond(cond);

        if (GEN_JVM_LOOPS) {
            int loopstartPC = code.currentIP();
            if (infinitecond) {
                genBranch(body);
                flow.resolveCont(loopstartPC);
                code.resolveJump(emitGoto(), loopstartPC);
                if (_infinite) code.dead(); // Подлинный вечный цикл.
            } else {
                if (testFirst) {
                    CondItem condItem = genCond(cond);
                    condItem.resolveTrueJumps();
                    genBranch(body);
                    visitOptionalExpressionList(steps);
                    flow.resolveCont(loopstartPC);
                    code.resolveJump(emitGoto(), loopstartPC);
                    condItem.resolveFalseJumps();
                } else {
                    genBranch(body);
                    visitOptionalExpressionList(steps);
                    flow.resolveCont();
                    CondItem condItem = genCond(cond).negate();
                    condItem.resolveTrueJumps();
                    condItem.resolveFalseJumps(loopstartPC);
                }
            }
        } else {
            int loopstartPC;
            if (testFirst && !infinitecond) {
                int skipBodyPC = emitGoto();
                loopstartPC = code.currentIP();
                genBranch(body);
                if (steps != null) steps.forEach(expr -> genExpr(expr).drop());
                code.resolveJump(skipBodyPC);
            } else {
                loopstartPC = code.currentIP();
                genBranch(body);
                if (steps != null) steps.forEach(expr -> genExpr(expr).drop());
            }
            flow.resolveCont();
            if (infinitecond) {
                code.resolveJump(emitGoto(), loopstartPC);
                if (_infinite) code.dead(); // Подлинный вечный цикл.
            } else {
                CondItem condItem = genCond(cond).negate();
                condItem.resolveTrueJumps();
                condItem.resolveFalseJumps(loopstartPC);
            }
        }

        flow.resolveExit();
        flow = flow.parent;
    }

    public void visitBinaryOp(BinaryOp tree) {
        switch (tree.tag) {
            case FLOW_AND:
                genFlowAnd(tree);
                break;
            case FLOW_OR:
                genFlowOr(tree);
                break;
            case EQ:
            case NE:
            case GT:
            case GE:
            case LT:
            case LE:
                genCmp(tree);
                break;
            case NULLCOALESCE:
                genNullCoalescing(tree);
                break;
            default:
                genBinary(tree);
        }
    }

    @Override
    public void visitUnaryOp(UnaryOp tree) {
        generateUnary(tree);
    }

    @Override
    public void visitCompoundAssign(CompoundAssign tree) {
        Item varitem = genExpr(tree.dst);
        varitem.duplicate();
        if (tree.hasTag(Tag.ASG_NULLCOALESCE)) {
            Item a = varitem.load();
            a.duplicate();
            TempItem tmp = items.makeTemp();
            tmp.store();
            code.putPos(tree.pos);
            CondItem nonNull = a.nonNull();
            nonNull.resolveTrueJumps();
            int sp1 = code.curStackTop();
            genExpr(tree.src).load().duplicate();
            tmp.store();
            varitem.store();
            int sp2 = code.curStackTop();
            int ePC = emitGoto();
            nonNull.resolveFalseJumps();
            code.curStackTop(sp1);
            varitem.drop();
            code.resolveJump(ePC);
            assertStacktopEquality(sp2);
            result = tmp;
        } else {
            varitem.load();
            genExpr(tree.src).load();
            code.addInstruction(fromBinaryAsgOpTag(tree.tag));
            result = items.makeAssign(tree.pos, varitem);
        }
    }

    @Override
    public void visitLiteral(Literal tree) {
        result = items.makeLiteral(tree.pos, tree.type);
    }

    @Override
    public void visitDiscarded(Discarded tree) {
        genExpr(tree.expr).drop();
    }

    private void genIncrease(UnaryOp tree) {
        Expression var = stripParens(tree.expr);

        // todo: Делать нормально уже лень. Рефакторинг

        switch (var.getTag()) {
            case MEMACCESS:
            case ARRAYACCESS:
            case VARIABLE:
                Item varitem = genExpr(tree.expr);
                boolean post = tree.hasTag(Tag.POSTINC) || tree.hasTag(Tag.POSTDEC);
                boolean inc = tree.hasTag(Tag.POSTINC) || tree.hasTag(Tag.PREINC);

                if (varitem instanceof LocalItem) {
                    result = items.new Item() {
                        final LocalItem localitem = (LocalItem) varitem;

                        @Override
                        Item load() {
                            if (post) {
                                localitem.load();
                                drop();
                            } else {
                                drop();
                                localitem.load();
                            }
                            return items.makeStack();
                        }

                        @Override
                        void drop() {
                            code.putPos(tree.pos);
                            if (inc) {
                                localitem.inc();
                            } else {
                                localitem.dec();
                            }
                        }
                    };
                } else {
                    varitem.duplicate();
                    varitem.load();
                    result = items.new Item() {
                        @Override
                        Item load() {
                            if (post) {
                                varitem.stash();
                                drop();
                            } else {
                                code.putPos(tree.pos);
                                code.addInstruction(fromUnaryOpTag(tree.tag));
                                items.makeAssign(tree.pos, varitem).load();
                            }
                            return items.makeStack();
                        }

                        @Override
                        void drop() {
                            code.putPos(tree.pos);
                            code.addInstruction(fromUnaryOpTag(tree.tag));
                            items.makeAssign(tree.pos, varitem).drop();
                        }
                    };
                }
                break;

            default:
                Assert.error();
        }
    }

    /**
     * Генерирует код оператора в дочерней ветке и возвращает жива ли она.
     */
    private boolean genBranch(Statement statement) {
        int savedstacktop = code.curStackTop();

        try {
            statement.accept(this);
            return code.isAlive();
        } finally {
            code.setAlive();
            assertStacktopEquality(savedstacktop);
        }
    }

    private void emitPushLong(long value) {
        if (isShort(value)) {
            code.addInstruction(new Push((short) value));
        } else {
            code.addInstruction(new Ldc(code.resolveLong(value)));
        }
    }

    private void emitPushDouble(double value) {
        code.addInstruction(new Ldc(code.resolveDouble(value)));
    }

    private void emitPushString(String value) {
        code.addInstruction(new Ldc(code.resolveString(value)));
    }

    private static boolean isShort(long value) {
        return (value >>> 16) == 0;
    }

    @Deprecated
    private void cError(int position, String message) {
        log.error(position, message);
    }

    Item result;

    Item genExpr(Expression tree) {
        if (Code.USE_KOSTYL && !code.isAlive()) return items.makeStack();
        Item prevItem = result;
        try {
            tree.accept(this);
            return result;
        } finally {
            result = prevItem;
        }
    }

    CondItem genCond(Expression tree) {
        return genExpr(tree).toCond();
    }


    class FlowEnv {

        final FlowEnv parent;
        final boolean isSwitch;

        /**
         * continue-прыжки, если isSwitch=true, то fallthrough
         */
        final IntArrayList contjumps = new IntArrayList();
        /**
         * break-прыжки
         */
        final IntArrayList exitjumps = new IntArrayList();

        /**
         * Указатель на инструкцию, где находится switch.
         */
        int switchStartPC;
        /**
         * Индексы констант из ключей кейзов. Равно null когда isSwitch=false
         */
        final IntList caseLabelsConstantIndexes;
        /**
         * Точка входа (IP) для каждого кейза. Равно null когда isSwitch=false
         */
        final IntList switchCaseOffsets;
        /**
         * Указатель на точку входа в default-case
         */
        int switchDefaultOffset = -1;

        FlowEnv(FlowEnv parent, boolean isSwitch) {
            this.parent = parent;
            this.isSwitch = isSwitch;

            switchCaseOffsets = isSwitch ? new IntArrayList() : null;
            caseLabelsConstantIndexes = isSwitch ? new IntArrayList() : null;
        }

        void resolveCont() {
            resolveCont(code.currentIP());
        }

        void resolveCont(int pc) {
            code.resolveChain(contjumps, pc);
        }

        void resolveExit() {
            resolveExit(code.currentIP());
        }

        void resolveExit(int pc) {
            code.resolveChain(exitjumps, pc);
        }
    }
}