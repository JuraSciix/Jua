package jua.compiler;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import jua.compiler.Tree.*;
import jua.compiler.Types.Type;
import jua.interpreter.instruction.*;
import jua.runtime.JuaFunction;
import jua.util.Assert;

import java.util.List;

import static jua.compiler.InstructionUtils.*;
import static jua.compiler.TreeInfo.*;
import static jua.util.Collections.mergeIntLists;

public final class Gen extends Scanner {

    private final ProgramLayout programLayout;

    Code code;

    Log log;

    Gen(ProgramLayout programLayout) {
        this.programLayout = programLayout;
    }

    @Override
    public void visitCompilationUnit(CompilationUnit tree) {
        code = tree.code;
        log = tree.source.getLog();
        scan(tree.stats);
        emitLeave();
    }

    private void genFlowAnd(BinaryOp tree) {
        CondItem lcond = genCond(tree.lhs);
        lcond.resolveTrueJumps();
        CondItem rcond = genCond(tree.rhs);
        result = new CondItem(rcond.opcodePC, rcond.truejumps, mergeIntLists(lcond.falsejumps, rcond.falsejumps));
    }

    private void genFlowOr(BinaryOp tree) {
        CondItem lcond = genCond(tree.lhs).negate();
        lcond.resolveTrueJumps();
        CondItem rcond = genCond(tree.rhs);
        result = new CondItem(rcond.opcodePC, mergeIntLists(lcond.falsejumps, rcond.falsejumps), rcond.truejumps);
    }

    private final StackItem stackItem = new StackItem();

    @Override
    public void visitArrayAccess(ArrayAccess tree) {
        genExpr(tree.expr).load();
        genExpr(tree.index).load();
        result = new AccessItem(tree.pos);
    }

    @Override
    public void visitArrayLiteral(ArrayLiteral tree) {
        code.putPos(tree.pos);
        emitNewArray();
        result = genArrayInitializr(tree.entries);
    }

    Item genArrayInitializr(List<ArrayLiteral.Entry> entries) {
        long implicitIndex = 0L;
        Item item = stackItem;
        for (ArrayLiteral.Entry entry : entries) {
            item.duplicate();
            if (entry.key == null) {
                code.putPos(entry.pos);
                emitPushLong(implicitIndex++);
            } else {
                genExpr(entry.key).load();
            }
            genExpr(entry.value).load();
            new AccessItem(entry.pos).store();
        }
        return item;
    }

    private void emitNewArray() {
        code.addInstruction(Newarray.INSTANCE);
    }

    private void genBinary(BinaryOp tree) {
        genExpr(tree.lhs).load();
        genExpr(tree.rhs).load();
        code.putPos(tree.pos);
        code.addInstruction(fromBinaryOpTag(tree.tag));
        result = stackItem;
    }

    @Override
    public void visitBreak(Break tree) {
        // todo: Эта проверка должна находиться в другом этапе
        FlowEnv env = flow;
        if (env == null) {
            cError(tree.pos, "'break' is not allowed outside of loop/switch.");
            return;
        }
        code.putPos(tree.pos);
        env.exitjumps.add(emitGoto());
        code.dead();
        env.interrupted = true;
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

        if (!flow.interrupted) {
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
                // todo: Эта проверка должна находиться в другом этапе
                if (!stripParens(label).hasTag(Tag.LITERAL)) {
                    log.error(label.pos, "Constant expected");
                    continue;
                }
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
            flow.interrupted = true;
        }
    }

    @Override
    public void visitConstDef(ConstDef tree) {
        cError(tree.pos, "constants declaration is not allowed here.");
    }

    @Override
    public void visitContinue(Continue tree) {
        // todo: Эта проверка должна находиться в другом этапе
        FlowEnv env = searchEnv(false);
        if (env == null) {
            cError(tree.pos, "'continue' is not allowed outside of loop.");
            return;
        }
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
        genLoop(tree, null, tree.cond, null, tree.body, false);
    }

    @Override
    public void visitFallthrough(Fallthrough tree) {
        // todo: Эта проверка должна находиться в другом этапе
        FlowEnv env = searchEnv(true);
        if (env == null) {
            cError(tree.pos, "'fallthrough' is not allowed outside of switch.");
            return;
        }
        code.putPos(tree.pos);
        env.contjumps.add(emitGoto());
        code.dead();
    }

    @Override
    public void visitFor(ForLoop tree) {
        genLoop(tree, tree.init, tree.cond, tree.step, tree.body, true);
    }

    @Override
    public void visitInvocation(Invocation tree) {
        if (!tree.callee.hasTag(Tag.MEMACCESS) || ((MemberAccess) tree.callee).expr != null) {
            log.error(tree.pos, "Only a function calling allowed");
            return;
        }
        Name callee = ((MemberAccess) tree.callee).member;

        for (Invocation.Argument argument : tree.args) {
            if (argument.name != null) {
                log.error(argument.name.pos, "Named arguments not supported yet");
                return;
            }
        }

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
                require_nargs(tree, 1);
                genExpr(tree.args.get(0).expr).load();
                code.putPos(tree.pos);
                code.addInstruction(Length.INSTANCE);
                result = stackItem;
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
                if (nargs > 0xff) {
                    cError(tree.pos, "Too many arguments");
                }
                code.addInstruction(new Call((short) fn_idx, (byte) nargs));
                result = stackItem;
        }
    }

    private void require_nargs(Invocation tree, int nargs) {
        if (tree.args.size() != nargs) {
            log.error(tree.pos, "Required arguments count mismatch (" +
                    "required: " + nargs + ", " +
                    "provided: " + tree.args.size() +
                    ")");
        }
    }

    private void visitInvocationArgs(List<Invocation.Argument> args) {
        args.forEach(argument -> genExpr(argument.expr).load());
    }

    Source funcSource;

    JuaFunction resultFunc;

    @Override
    public void visitFuncDef(FuncDef tree) {
        if (code != null) {
            cError(tree.pos, "Function declaration is not allowed here");
            return;
        }
        code = tree.code;
        log = funcSource.getLog();

        int nOptionals = 0;
        for (FuncDef.Parameter param : tree.params) {
            Name name = param.name;
            if (code.localExists(name.value)) {
                cError(name.pos, "Duplicate parameter named '" + name.value + "'.");
            }
            int localIdx = code.resolveLocal(name.value);
            if (param.expr != null) {
                Expression expr = stripParens(param.expr);
                if (!expr.hasTag(Tag.LITERAL)) {
                    cError(expr.pos, "The values of the optional parameters can only be literals");
                    continue;
                }

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
            Assert.check(tree.body instanceof Expression, "Function body neither block ner expression");
            genExpr((Expression) tree.body).load();
            code.addInstruction(jua.interpreter.instruction.Return.RETURN);
            code.dead();
        }

        resultFunc = JuaFunction.fromCode(
                tree.name.value,
                tree.params.size() - nOptionals,
                tree.params.size(),
                code.buildCodeSegment(),
                funcSource.name
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
                resolveJump(skipperPC);
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
        result = new CondItem(code.addInstruction(opcode));
    }

    private void genNullCoalescing(BinaryOp tree) {
        genExpr(tree.lhs).load();
        code.addInstruction(Dup.INSTANCE);
        code.putPos(tree.pos);
        int condPC = code.addInstruction(new Ifnonnull());
        code.addInstruction(Pop.INSTANCE);
        genExpr(tree.rhs).load();
        resolveJump(condPC);
        result = stackItem;
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
                result = new AssignItem(tree.pos, varitem);
                break;

            default:
                log.error(var.pos, "Assignable expression expected");
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
                result = stackItem;
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
        code.addInstruction(Leave.INSTANCE);
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
        resolveJump(exiterPC);
        assertStacktopEquality(limitstacktop + 1);
        result = stackItem;
    }

    @Override
    public void visitVariable(Var tree) {
        Name name = tree.name;
        if (programLayout.hasConstant(name)) {
            result = new ConstantItem(tree.pos, name);
        } else {
            result = new LocalItem(tree.pos, name);
        }
    }

    @Override
    public void visitMemberAccess(MemberAccess tree) {
        genExpr(tree.expr).load();
        emitPushString(tree.member.value);
        result = new AccessItem(tree.pos);
    }

    @Override
    public void visitWhileLoop(WhileLoop tree) {
        genLoop(tree, null, tree.cond, null, tree.body, true);
    }

    FlowEnv flow;

    private void genLoop(
            Statement loop,
            List<Expression> init, Expression cond, List<Expression> steps,
            Statement body, boolean testFirst) {
        flow = new FlowEnv(flow, false);

        if (init != null) init.forEach(expr -> genExpr(expr).drop());

        boolean truecond = (cond == null) || isLiteralTrue(cond);
        int loopstartPC, limitstacktop;
        if (testFirst && !truecond) {
            code.putPos(loop.pos);
            int skipBodyPC = emitGoto();
            loopstartPC = code.currentIP();
            limitstacktop = code.curStackTop();
            genBranch(body);
            if (steps != null) steps.forEach(expr -> genExpr(expr).drop());
            resolveJump(skipBodyPC);
        } else {
            loopstartPC = code.currentIP();
            limitstacktop = code.curStackTop();
            genBranch(body);
            if (steps != null) steps.forEach(expr -> genExpr(expr).drop());
        }
        flow.resolveCont();
        if (truecond) {
            resolveJump(emitGoto(), loopstartPC);
            if (!flow.interrupted) code.dead();
        } else {
            CondItem condItem = genCond(cond).negate();
            condItem.resolveTrueJumps();
            condItem.resolveFalseJumps(loopstartPC);
        }
        flow.resolveExit();

        assertStacktopEquality(limitstacktop);

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
        Expression var = stripParens(tree.dst);

        switch (var.getTag()) {
            case MEMACCESS:
            case ARRAYACCESS:
            case VARIABLE:
                break;

            default:
                log.error(var.pos, "Assignable expression expected");
        }

        Item varitem = genExpr(tree.dst);
        varitem.duplicate();
        if (tree.hasTag(Tag.ASG_NULLCOALESCE)) {
            varitem.load().duplicate();
            String tmp = code.acquireSyntheticName(); // synthetic0
            code.addInstruction(new Vstore(code.resolveLocal(tmp)));
            code.putPos(tree.pos);
            int cPC = code.addInstruction(new Ifnonnull());
            int sp1 = code.curStackTop();
            genExpr(tree.src).load().duplicate();
            code.addInstruction(new Vstore(code.resolveLocal(tmp)));
            varitem.store();
            int sp2 = code.curStackTop();
            int ePC = emitGoto();
            resolveJump(cPC);
            code.curStackTop(sp1);
            varitem.drop();
            resolveJump(ePC);
            assertStacktopEquality(sp2);
            result = new Item() {
                @Override
                Item load() {
                    code.addInstruction(new Vload(code.resolveLocal(tmp)));
                    drop();
                    return stackItem;
                }

                @Override
                void drop() {
                    code.addInstruction(ConstNull.INSTANCE);
                    code.addInstruction(new Vstore(code.resolveLocal(tmp)));
                    code.releaseSyntheticName(tmp);
                }
            };
        } else {
            varitem.load();
            genExpr(tree.src).load();
            code.addInstruction(fromBinaryAsgOpTag(tree.tag));
            result = new AssignItem(tree.pos, varitem);
        }
    }

    @Override
    public void visitLiteral(Literal tree) {
        result = new LiteralItem(tree.pos, tree.type);
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
                    result = new Item() {
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
                            return stackItem;
                        }

                        @Override
                        void drop() {
                            if (inc) {
                                code.putPos(tree.pos);
                                localitem.inc();
                            } else {
                                code.putPos(tree.pos);
                                localitem.dec();
                            }
                        }
                    };
                } else {
                    varitem.duplicate();
                    varitem.load();
                    result = new Item() {
                        @Override
                        Item load() {
                            if (post) {
                                varitem.stash();
                                drop();
                            } else {
                                code.putPos(tree.pos);
                                code.addInstruction(fromUnaryOpTag(tree.tag));
                                new AssignItem(tree.pos, varitem).load();
                            }
                            return stackItem;
                        }

                        @Override
                        void drop() {
                            code.putPos(tree.pos);
                            code.addInstruction(fromUnaryOpTag(tree.tag));
                            new AssignItem(tree.pos, varitem).drop();
                        }
                    };
                }
                break;

            default:
                log.error(tree.expr.pos, "Assignable expression expected");
        }
    }

    /**
     * Генерирует код оператора в дочерней ветке и возвращает жива ли она.
     */
    private boolean genBranch(Statement statement) {
        Assert.check(!(statement instanceof Expression));

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

    private void cError(int position, String message) {
        log.error(position, message);
    }

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

    CondItem genCond(Expression tree) {
        return genExpr(tree).toCond();
    }

    abstract class Item {

        Item load() {
            throw new AssertionError(this);
        }

        void store() {
            throw new AssertionError(this);
        }

        void drop() {
            throw new AssertionError(this);
        }

        void duplicate() {
            throw new AssertionError(this);
        }

        void stash() {
            throw new AssertionError(this);
        }

        CondItem toCond() {
            load();
            return new CondItem(code.addInstruction(new Ifz()));
        }

        int constantIndex() {
            throw new AssertionError(this);
        }
    }

    /**
     * Динамическое значение.
     */
    class StackItem extends Item {

        @Override
        Item load() {
            return this;
        }

        @Override
        void drop() {
            load();
            code.addInstruction(Pop.INSTANCE);
        }

        @Override
        void duplicate() {
            code.addInstruction(Dup.INSTANCE);
        }

        @Override
        void stash() {
            code.addInstruction(Dup.INSTANCE);
        }
    }

    /**
     * Литерал.
     */
    class LiteralItem extends Item {

        final int pos;

        final Type type;

        LiteralItem(int pos, Type type) {
            this.pos = pos;
            this.type = type;
        }

        @Override
        Item load() {
            code.putPos(pos);
            if (type.isLong()) {
                emitPushLong(type.longValue());
            } else if (type.isDouble()) {
                emitPushDouble(type.doubleValue());
            } else if (type.isBoolean()) {
                if (type.booleanValue()) {
                    code.addInstruction(ConstTrue.CONST_TRUE);
                } else {
                    code.addInstruction(ConstFalse.CONST_FALSE);
                }
            } else if (type.isString()) {
                emitPushString(type.stringValue());
            } else if (type.isNull()) {
                code.addInstruction(ConstNull.INSTANCE);
            } else {
                throw new AssertionError(type);
            }
            return stackItem;
        }

        @Override
        void drop() { /* no-op */ }

        @Override
        int constantIndex() {
            return type.resolvePoolConstant(code);
        }
    }

    /**
     * Обращение к локальной переменной.
     */
    class LocalItem extends Item {

        final int pos;
        final Name name;

        LocalItem(int pos, Name name) {
            this.pos = pos;
            this.name = name;
        }

        @Override
        Item load() {
            code.putPos(pos);
            code.addInstruction(new Vload(code.resolveLocal(name.value)));
            return stackItem;
        }

        @Override
        void drop() {
            load();
            code.addInstruction(Pop.INSTANCE);
        }

        @Override
        void duplicate() { /* no-op */ }

        @Override
        void store() {
            code.addInstruction(new Vstore(code.resolveLocal(name.value)));
        }

        @Override
        void stash() {
            code.addInstruction(Dup.INSTANCE);
        }

        void inc() {
            code.addInstruction(new Vinc(code.resolveLocal(name)));
        }

        void dec() {
            code.addInstruction(new Vdec(code.resolveLocal(name)));
        }
    }

    /**
     * Обращение к элементу массива.
     */
    class AccessItem extends Item {

        final int pos;

        AccessItem(int pos) {
            this.pos = pos;
        }

        @Override
        Item load() {
            code.putPos(pos);
            code.addInstruction(Aload.INSTANCE);
            return stackItem;
        }

        @Override
        void store() {
            code.addInstruction(Astore.INSTANCE);
        }

        @Override
        void drop() {
            code.addInstruction(Pop2.INSTANCE);
        }

        @Override
        void duplicate() {
            code.addInstruction(Dup2.INSTANCE);
        }

        @Override
        void stash() {
            code.addInstruction(Dup_x2.INSTANCE);
        }
    }

    /**
     * Обращение к рантайм константе.
     */
    class ConstantItem extends Item {

        final int pos;
        final Name name;

        ConstantItem(int pos, Name name) {
            this.pos = pos;
            this.name = name;
        }

        @Override
        Item load() {
            code.putPos(pos);
            code.addInstruction(new Getconst(programLayout.tryFindConst(name)));
            return stackItem;
        }
    }

    /**
     * Присвоение.
     */
    class AssignItem extends Item {

        final int pos;
        final Item var;

        AssignItem(int pos, Item var) {
            this.pos = pos;
            this.var = var;
        }

        @Override
        Item load() {
            var.stash();
            code.putPos(pos);
            var.store();
            return stackItem;
        }

        @Override
        void drop() {
            code.putPos(pos);
            var.store();
        }

        @Override
        void stash() {
            var.stash();
        }
    }

    /**
     * Условное разветвление.
     */
    class CondItem extends Item {

        final int opcodePC;
        final IntArrayList truejumps;
        final IntArrayList falsejumps;

        CondItem(int opcodePC) {
            this(opcodePC, new IntArrayList(), new IntArrayList());
        }

        CondItem(int opcodePC, IntArrayList truejumps, IntArrayList falsejumps) {
            this.opcodePC = opcodePC;
            this.truejumps = truejumps;
            this.falsejumps = falsejumps;

            falsejumps.add(0, opcodePC);
        }

        @Override
        Item load() {
            resolveTrueJumps();
            code.addInstruction(ConstTrue.CONST_TRUE);
            int skipPC = emitGoto();
            resolveFalseJumps();
            code.addInstruction(ConstFalse.CONST_FALSE);
            resolveJump(skipPC);
            return stackItem;
        }

        @Override
        void drop() {
            load();
            code.addInstruction(Pop.INSTANCE);
        }

        @Override
        CondItem toCond() {
            return this;
        }

        CondItem negate() {
            code.setInstruction(opcodePC, code.getJump(opcodePC).negate());
            CondItem condItem = new CondItem(opcodePC, falsejumps, truejumps);
            condItem.truejumps.removeInt(0);
            return condItem;
        }

        void resolveTrueJumps() {
            resolveTrueJumps(code.currentIP());
        }

        void resolveTrueJumps(int pc) {
            resolveChain(truejumps, pc);
        }

        void resolveFalseJumps() {
            resolveFalseJumps(code.currentIP());
        }

        void resolveFalseJumps(int pc) {
            resolveChain(falsejumps, pc);
        }
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

        /**
         * Истинно, если в цикле присутствуют break. Нужно, чтобы определять вечные циклы
         * Истинно, если в switch присутствует break. Нужно, чтобы определять живой код после switch.
         */
        boolean interrupted = false;

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
            resolveChain(contjumps, pc);
        }

        void resolveExit() {
            resolveExit(code.currentIP());
        }

        void resolveExit(int pc) {
            resolveChain(exitjumps, pc);
        }
    }

    void resolveChain(IntArrayList sequence, int pc) {
        for (int jumpPC : sequence) resolveJump(jumpPC, pc);
    }

    void resolveJump(int jumpPC) {
        resolveJump(jumpPC, code.currentIP());
    }

    void resolveJump(int jumpPC, int pc) {
        code.getJump(jumpPC).offset = pc - jumpPC;
    }
}