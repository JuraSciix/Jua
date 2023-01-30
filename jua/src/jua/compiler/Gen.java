package jua.compiler;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import jua.compiler.Items.*;
import jua.compiler.Tree.*;
import jua.compiler.Types.LongType;
import jua.interpreter.Address;
import jua.interpreter.instruction.*;
import jua.runtime.Function;
import jua.utils.Assert;
import jua.utils.List;

import static jua.compiler.InstructionFactory.*;
import static jua.compiler.InstructionUtils.*;
import static jua.compiler.TreeInfo.*;
import static jua.utils.CollectionUtils.mergeIntLists;

public final class Gen extends Scanner {

    @Deprecated
    private static final boolean GEN_JVM_LOOPS = false;

    private final ProgramScope programScope;

    Code code;

    Log log;
    
    Items items;

    Source source;

    Function resultFunc;

    Gen(ProgramScope programScope) {
        this.programScope = programScope;
    }

    @Override
    public void visitCompilationUnit(CompilationUnit tree) {
        code = tree.code;
        code.putPos(0);
        items = new Items(code);
        log = tree.source.log;
        scan(tree.stats);
        emitLeave();
        resultFunc = new Function(
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
        code.putPos(tree.pos);
        result = items.makeAccess();
    }

    @Override
    public void visitListInit(ListInit tree) {
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
    public void visitMapInit(MapInit tree) {
        code.putPos(tree.pos);
        code.addInstruction(newmap);
        for (MapInit.Entry entry : tree.entries) {
            items.makeStack().duplicate();
            genExpr(entry.key).load();
            genExpr(entry.value).load();
            code.putPos(entry.pos);
            items.makeAccess().store();
        }
        result = items.makeStack();
    }

    Item genArrayInitializr(List<MapInit.Entry> entries) {
        Item item = items.makeStack();
        for (MapInit.Entry entry : entries) {
            item.duplicate();
            genExpr(entry.key).load();
            genExpr(entry.value).load();
            code.putPos(entry.pos);
            items.makeAccess().store();
        }
        return item;
    }

    private void emitNewArray() {
        code.addInstruction(newmap);
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
        Assert.notNull((Object) env);
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
        SwitchEnv env = new SwitchEnv(flow);
        flow = env;
        env.switchStartPC = code.currentIP();
        code.putPos(tree.pos);
        code.addInstruction(new Fake(-1)); // Резервируем место под инструкцию

        for (Case c : tree.cases) {
            c.accept(this);
            env.resolveCont();
            env.contjumps.clear();
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

        if (tree._final) {
            // Ни один кейз не был закрыт с помощью break.
            // Это значит, что после switch находится недостижимый код.
            code.dead();
        }

        flow = env.parent;
    }

    @Override
    public void visitCase(Case tree) {
        boolean cond = flow instanceof SwitchEnv;
        Assert.ensure(cond);
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
            flow.exitjumps.add(emitGoto());
        }
    }

    @Override
    public void visitContinue(Continue tree) {
        FlowEnv env = searchEnv(false);
        Assert.notNull((Object) env);
        code.putPos(tree.pos);
        env.contjumps.add(emitGoto());
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
        genLoop(tree._infinite, null, tree.cond, null, tree.body, false);
    }

    @Override
    public void visitFallthrough(Fallthrough tree) {
        FlowEnv env = searchEnv(true);
        Assert.notNull((Object) env);
        code.putPos(tree.pos);
        env.contjumps.add(emitGoto());
        code.dead();
    }

    @Override
    public void visitVarDef(VarDef tree) {
        for (VarDef.Definition def : tree.defs) {
            // Эта строчка находится вне условия специально
            // Переменная должна регистрироваться независимо от того,
            // сразу она инициализируется или нет.
            Item var_item = items.makeLocal(code.resolveLocal(def.name));
            if (def.init == null) {
                items.makeLiteral(Types.TYPE_NULL).load();
            } else {
                genExpr(def.init).load();
            }
            items.makeAssign(var_item).drop();
        }
    }

    @Override
    public void visitForLoop(ForLoop tree) {
        genLoop(tree._infinite, tree.init, tree.cond, tree.step, tree.body, true);
    }

    @Override
    public void visitInvocation(Invocation tree) {
        boolean cond = tree.callee instanceof MemberAccess;
        Assert.ensure(cond);
        Name callee = ((MemberAccess) tree.callee).member;

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
                int fn_idx = programScope.lookupFunction(callee).id;
                visitInvocationArgs(tree.args);
                code.putPos(tree.pos);
                result = items.makeCall(fn_idx, tree.args.count());
        }
    }

    private void visitInvocationArgs(List<Invocation.Argument> args) {
        args.forEach(argument -> genExpr(argument.expr).load());
    }

    @Override
    public void visitFuncDef(FuncDef tree) {
        code = tree.code;
        code.putPos(tree.pos);
        items = new Items(code);
        log = source.log;

        List<Address> defaults = new List<>();
        for (FuncDef.Parameter param : tree.params) {
            code.resolveLocal(param.name);
            if (param.expr != null) {
                Literal literal = (Literal) stripParens(param.expr);
                Address address = new Address();
                literal.type.toOperand().writeToAddress(address);
                defaults.add(address);
            }
        }

        assert tree.body != null;

        if (tree.body.hasTag(Tag.BLOCK)) {
            if (genBranch(tree.body)) {
                emitLeave();
            }
        } else {
            Assert.ensure(tree.body.hasTag(Tag.DISCARDED), "Function body neither block ner expression");
            genExpr(((Discarded) tree.body).expr).load();
            code.addInstruction(jua.interpreter.instruction.Return.RETURN);
            code.dead();
        }

        resultFunc = new Function(
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
        Assert.ensure(code.curStackTop() == limitstacktop, "limitstacktop mismatch (" +
                "before: " + limitstacktop + ", " +
                "after: " + code.curStackTop() + ", " +
                "code line num: " + code.lastLineNum() +
                ")");
    }

    private void genCmp(BinaryOp tree) {
        if (isLiteralNull(tree.lhs)) {
            Item expr = genExpr(tree.rhs).load();
            code.putPos(tree.pos);
            result = expr.isNull();
        } else if (isLiteralNull(tree.rhs)) {
            Item expr = genExpr(tree.lhs).load();
            code.putPos(tree.pos);
            result = expr.isNull();
        } else {
            genExpr(tree.lhs).load();
            genExpr(tree.rhs).load();
            code.putPos(tree.pos);
            result = items.makeCond(code.addInstruction(fromComparisonOpTag(tree.tag)));
        }
    }

    private void genNullCoalescing(BinaryOp tree) {
        Item lhs = genExpr(tree.lhs).load();
        lhs.duplicate();
        CondItem nonNull = lhs.isNull();
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
                code.putPos(tree.pos);
                result = items.makeAssign(varitem);
                break;

            default:
                Assert.error();
        }
    }

    private void generateUnary(UnaryOp tree) {
        switch (tree.tag) {
            case POSTDEC: case PREDEC:
            case POSTINC: case PREINC:
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
        if (programScope.isConstantDefined(name)) {
            code.putPos(tree.pos);
            code.addInstruction(new Getconst(programScope.lookupConstant(name).id));
            result = items.makeStack();
        } else {
            code.putPos(tree.pos);
            result = items.makeLocal(code.resolveLocal(name));
        }
    }

    @Override
    public void visitMemberAccess(MemberAccess tree) {
        genExpr(tree.expr).load();
        emitPushString(tree.member.toString());
        code.putPos(tree.pos);
        result = items.makeAccess();
    }

    @Override
    public void visitWhileLoop(WhileLoop tree) {
        genLoop(tree._infinite, null, tree.cond, null, tree.body, true);
    }

    FlowEnv flow;

    private static boolean isInfiniteLoopCond(Expression tree) {
        return tree == null || isLiteralTrue(tree);
    }

    private void genLoop(
            boolean _infinite,
            List<Statement> init,
            Expression cond,
            List<Discarded> update,
            Statement body,
            boolean testFirst
    ) {
        scan(init);

        flow = new FlowEnv(flow);

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
                    flow.resolveCont(loopstartPC);
                    scan(update);
                    code.resolveJump(emitGoto(), loopstartPC);
                    condItem.resolveFalseJumps();
                } else {
                    genBranch(body);
                    flow.resolveCont();
                    scan(update);
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
                flow.resolveCont();
                scan(update);
                code.resolveJump(skipBodyPC);
            } else {
                loopstartPC = code.currentIP();
                genBranch(body);
                flow.resolveCont();
                scan(update);
            }
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
            case AND:
                genFlowAnd(tree);
                break;
            case OR:
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
            case NULLCOALSC:
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
        Item varitem = genExpr(tree.var);
        varitem.duplicate();
        if (tree.hasTag(Tag.ASG_NULLCOALSC)) {
            Item a = varitem.load();
            a.duplicate();
            TempItem tmp = items.makeTemp();
            tmp.store();
            code.putPos(tree.pos);
            CondItem nonNull = a.isNull();
            nonNull.resolveTrueJumps();
            int sp1 = code.curStackTop();
            genExpr(tree.expr).load().duplicate();
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
            genExpr(tree.expr).load();
            code.addInstruction(fromBinaryAsgOpTag(tree.tag));
            code.putPos(tree.pos);
            result = items.makeAssign(varitem);
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

    private void genIncrease(UnaryOp tree) {
        Item item = genExpr(tree.expr);
        if (item instanceof LocalItem) {
            LocalItem localItem = (LocalItem) item;
            result = items.new Item() {
                @Override
                Item load() {
                    if (tree.hasTag(Tag.POSTINC) || tree.hasTag(Tag.POSTDEC)) {
                        localItem.load();
                        if (tree.hasTag(Tag.POSTINC)) {
                            localItem.inc();
                        } else {
                            localItem.dec();
                        }
                    } else {
                        if (tree.hasTag(Tag.PREINC)) {
                            localItem.inc();
                        } else {
                            localItem.dec();
                        }
                        localItem.load();
                    }
                    return items.makeStack();
                }

                @Override
                void drop() {
                    if (tree.hasTag(Tag.POSTINC) || tree.hasTag(Tag.PREINC)) {
                        localItem.inc();
                    } else {
                        localItem.dec();
                    }
                }
            };
        } else {
            Assert.ensure(item instanceof AccessItem);
            result = items.new Item() {
                @Override
                Item load() {
                    if (tree.hasTag(Tag.POSTINC)||tree.hasTag(Tag.POSTDEC)) {
                        code.addInstruction(
                                tree.hasTag(Tag.POSTINC) || tree.hasTag(Tag.PREINC) ? ainc : adec);
                    } else {
                        item.duplicate();
                        code.addInstruction(
                                tree.hasTag(Tag.POSTINC) || tree.hasTag(Tag.PREINC) ? ainc : adec);
                        code.addInstruction(pop);
                        item.load();
                    }
                    return items.makeStack();
                }

                @Override
                void drop() {
                    code.addInstruction(
                            tree.hasTag(Tag.POSTINC) || tree.hasTag(Tag.PREINC) ? ainc : adec);
                    code.addInstruction(pop);
                }
            };
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
        items.makeLiteral(new LongType(value)).load();
    }

    private void emitPushString(String value) {
        items.makeLiteral(new Types.StringType(value)).load();
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
        return genExpr(tree).isTrue();
    }


    class FlowEnv {

        final FlowEnv parent;

        final IntArrayList contjumps = new IntArrayList();
        final IntArrayList exitjumps = new IntArrayList();

        FlowEnv(FlowEnv parent) {
            this.parent = parent;
        }

        void resolveCont() { code.resolveChain(contjumps); }
        void resolveCont(int cp) { code.resolveChain(contjumps, cp); }
        void resolveExit() { code.resolveChain(exitjumps); }
        void resolveExit(int cp) { code.resolveChain(exitjumps, cp); }
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