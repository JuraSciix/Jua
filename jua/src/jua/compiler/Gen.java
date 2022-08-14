package jua.compiler;

import it.unimi.dsi.fastutil.ints.Int2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;
import jua.interpreter.instruction.*;
import jua.interpreter.instruction.Switch;
import jua.runtime.JuaFunction;
import jua.runtime.heap.*;
import jua.compiler.Tree.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public final class Gen extends Scanner {

    /**
     *
     */
    static final int STATE_ROOTED = (1 << 0);

    /**
     * Состояние кода, в котором нельзя определять функции и константы.
     */
    static final int STATE_NO_DECLS = (1 << 1);

    /**
     * Состояние кода, в котором любое обрабатываемое выражение должно оставлять за собой какое-либо значение.
     */
    static final int STATE_RESIDUAL = (1 << 2);

    /**
     * Состояние кода, в котором любое обрабатываемое выражение должно приводиться к логическому виду.
     */
    static final int STATE_COND = (1 << 3);

    /**
     * Состояние кода, в котором все логические выражения должны инвертироваться.
     */
    static final int STATE_COND_INVERT = (1 << 4);

    /**
     * Состояние кода, в котором текущий обрабатываемый цикл считается бесконечным.
     */
    static final int STATE_INFINITY_LOOP = (1 << 5);

    /**
     * Состояние кода, в котором оператор switch не является конечным.
     */
    static final int STATE_ALIVE_SWITCH = (1 << 6);

    private final CodeLayout codeLayout;

    private Code code;

    private final IntStack breakChains;

    private final IntStack continueChains;

    private final IntStack fallthroughChains;

    private final IntStack conditionalChains;

    /**
     * Состояние кода.
     */
    private int state = 0; // unassigned state


    public Gen(CodeLayout codeLayout) {
        this.codeLayout = codeLayout;

        breakChains = new IntArrayList();
        continueChains = new IntArrayList();
        fallthroughChains = new IntArrayList();
        conditionalChains = new IntArrayList();
    }

    // todo: исправить этот low-cohesion
    public CompileResult getResult() {
        return new CompileResult(codeLayout, code.buildCodeSegment(), codeLayout.source.filename());
    }

    @Override
    public void visitCompilationUnit(CompilationUnit tree) {
        try {
            code = codeLayout.getCode();
        } catch (IOException e) {
            e.printStackTrace();
            return;//todo
        }
        code.pushContext(0);
        code.pushScope();
        int prev_state = state;
        setState(STATE_ROOTED);
        acceptTreeList(tree.trees);
        state = prev_state;
        code.addInstruction(Halt.INSTANCE);
        code.popScope();
    }

    private boolean isState(int state_flag) {
        return (state & state_flag) != 0;
    }

    private void unsetState(int state_flag) {
        state &= ~state_flag;
    }

    private void setState(int state_flag) {
        state |= state_flag;
    }

    public void visitAnd(BinaryOp expression) {
//        beginCondition();
//        if (invertCond) {
//            if (expression.rhs == null) {
//                generateCondition(expression.lhs);
//            } else {
//                int fa = pushMakeConditionChain();
//                invertCond = false;
//                generateCondition(expression.lhs);
//                popConditionChain();
//                invertCond = true;
//                generateCondition(expression.rhs);
//                code.resolveChain(fa);
//            }
//        } else {
//            generateCondition(expression.lhs);
//            generateCondition(expression.rhs);
//        }
//        endCondition();

        beginCondition();
        if (isState(STATE_COND_INVERT)) {
            int fa = pushMakeConditionChain();
            int prev_state = state;
            unsetState(STATE_COND_INVERT);
            generateCondition(expression.lhs);
            popConditionChain();
            state = prev_state;
            generateCondition(expression.rhs);
            code.resolveChain(fa);
        } else {
            generateCondition(expression.lhs);
            generateCondition(expression.rhs);
        }
        endCondition();
    }

    public void visitOr(BinaryOp expression) {
//        beginCondition();
//        if (invertCond) {
//            generateCondition(expression.lhs);
//            generateCondition(expression.rhs);
//        } else {
//            if (expression.rhs == null) {
//                generateCondition(expression.lhs);
//            } else {
//                int tr = pushMakeConditionChain();
//                invertCond = true;
//                generateCondition(expression.lhs);
//                popConditionChain();
//                invertCond = false;
//                generateCondition(expression.rhs);
//                code.resolveChain(tr);
//            }
//        }
//        endCondition();

        beginCondition();
        if (isState(STATE_COND_INVERT)) {
            generateCondition(expression.lhs);
            generateCondition(expression.rhs);
        } else {
            int tr = pushMakeConditionChain();
            int prev_state = state;
            setState(STATE_COND_INVERT);
            generateCondition(expression.lhs);
            popConditionChain();
            state = prev_state;
            generateCondition(expression.rhs);
            code.resolveChain(tr);
        }
        endCondition();
    }

    @Override
    public void visitArrayAccess(ArrayAccess tree) {
        visitExpression(tree.expr);
        visitExpression(tree.index);
        code.putPos(tree.pos);
        emitALoad();
    }

    @Override
    public void visitArrayLiteral(ArrayLiteral tree) {
//        code.incStack();
//        code.addInstruction(Newarray.INSTANCE);
//        AtomicInteger index = new AtomicInteger();
//        enableUsed();
//        expression.map.forEach((key, value) -> {
//            int line;
//            if (key.isEmpty()) {
//                line = value.getPosition().line;
//                emitPush(index.longValue(), IntOperand::valueOf);
//            } else {
//                line = key.getPosition().line;
//                visitStatement(key);
//            }
//            visitStatement(value);
//            emitAStore(line);
//            index.incrementAndGet();
//        });
//        disableUsed();
//        emitNecessaryPop();

        code.putPos(tree.pos);
        emitNewArray();
        generateArrayCreation(tree.entries);
    }

    private void generateArrayCreation(List<ArrayLiteral.Entry> entries) {
        long implicitIndex = 0;
        Iterator<ArrayLiteral.Entry> iterator = entries.iterator();
        while (iterator.hasNext()) {
            ArrayLiteral.Entry entry = iterator.next();
            if (iterator.hasNext() || isUsed()) emitDup();
            if (entry.key == null) {
                code.putPos(entry.value.pos);
                emitPushLong(implicitIndex++);
            } else {
                visitExpression(entry.key);
            }
            visitExpression(entry.value);
            emitAStore();
        }
    }

    private void emitNewArray() {
        code.addInstruction(Newarray.INSTANCE, 1);
    }

    public void visitAssignNullCoalesce(AssignOp expression) {
//        int el = code.createFlow();
//        int ex = code.createFlow();
//        boolean isArray = (expression.var.child() instanceof ArrayAccess);
//        visitAssignment(expression, line -> {
//            code.addFlow(el, new Ifnonnull());
//            code.decStack();
//            visitExpression(expression.expr);
//        });
//        insertGoto(0, ex);
//        code.resolveFlow(el);
//        if (isArray) {
//            insertALoad(line(expression));
//        } else {
//            visitExpression(expression.var);
//        }
//        code.resolveFlow(ex);

        generateAssignment(expression);
    }

    private void generateBinary(BinaryOp tree) {
        if (TreeInfo.isConditionalTag(tree.tag)) {
            generateComparison(tree);
            return;
        }
        tree.lhs.accept(this);
        if (tree.getTag() == Tag.NULLCOALESCE) {
            emitDup();
            int el = code.makeChain();
            code.putPos(tree.pos);
            code.addChainedInstruction(Ifnonnull::new, el, -1);
            code.addInstruction(Pop.INSTANCE, -1);
            visitExpression(tree.rhs);
            code.resolveChain(el);
            return;
        }
        tree.rhs.accept(this);
        code.putPos(tree.pos);
        code.addInstruction(bin2instr(tree.getTag()));
    }

    public static Instruction bin2instr(Tag tag) {
        switch (tag) {
            case ADD: return Add.INSTANCE;
            case SUB: return Sub.INSTANCE;
            case MUL: return Mul.INSTANCE;
            case DIV: return Div.INSTANCE;
            case REM: return Rem.INSTANCE;
            case SL: return Shl.INSTANCE;
            case SR: return Shr.INSTANCE;
            case AND: return And.INSTANCE;
            case OR: return Or.INSTANCE;
            case XOR: return Xor.INSTANCE;
            default: throw new AssertionError();
        }
    }

    @Override
    public void visitBlock(Block tree) {
        acceptTreeList(tree.stats);
    }

    private void acceptTreeList(List<? extends Tree> statements) {
        for (Tree tree : statements) {
            // Таким образом, мы упускаем ошибки в мертвом коде
//            if (!code.isAlive()) {
//                break;
//            }
            tree.accept(this);
        }
    }

    @Override
    public void visitBreak(Break tree) {
        if (breakChains.isEmpty()) {
            cError(tree.pos, "'break' is not allowed outside of loop/switch.");
            return;
        }
        code.putPos(tree.pos);
        emitGoto(breakChains.topInt());
        unsetState(STATE_INFINITY_LOOP);
    }

    private int switch_start_ip;

    private Int2IntMap cases;

    private int default_case;

    @Override
    public void visitCase(Case tree) {
        if (tree.labels != null) { // is not default case?
            for (Expression expr : tree.labels) {
                if (expr.getTag() != Tag.LITERAL) {
                    cError(expr.pos, "constant expected");
                    continue;
                }
                int cp = ((Literal) expr).type.getConstantIndex();
                cases.put(cp, code.currentIP() - switch_start_ip);
            }

        } else {
            if (default_case != -1) {
                code.resolveChain(default_case);
                default_case = -1;
            }
        }
        int f = code.makeChain();
        fallthroughChains.push(f);
        boolean alive = visitBody(tree.body);
        if (alive) {
            setState(STATE_ALIVE_SWITCH);
            emitGoto(breakChains.topInt());
        }
        fallthroughChains.popInt();
        code.resolveChain(f);
    }

    @Override
    public void visitSwitch(Tree.Switch tree) {
        visitExpression(tree.expr);
        // emit switch
        int b = code.makeChain();
        breakChains.push(b);
        default_case = code.makeChain();
        Int2IntMap _cases = new Int2IntLinkedOpenHashMap();
        cases = _cases;
        // todo: Координация по кейзам должна основываться на Code.Chain. В этот раз сделать лучше не получилось.
        switch_start_ip = code.currentIP();
        code.addChainedInstruction(dest_ip -> {
            int[] literals = _cases.keySet().toIntArray();
            int[] destIps = _cases.values().toIntArray();
            return new Switch(literals, destIps, dest_ip /* default ip */);
        }, default_case);
        int cached_sp = code.getSp();
        int max_sp = cached_sp;
        int prev_state = state;
        unsetState(STATE_ALIVE_SWITCH);
        for (Case _case : tree.cases) {
            code.setSp(cached_sp);
            _case.accept(this);
            if (code.getSp() > max_sp) max_sp = code.getSp();
        }
        code.setSp(max_sp);
        breakChains.popInt();
        if (default_case != -1)
            code.resolveChain(default_case);
        code.resolveChain(b);
        cases = null;
        if (!isState(STATE_ALIVE_SWITCH)) {
            code.dead();
        }
        state = prev_state;
    }

    @Override
    public void visitConstDef(ConstDef tree) {
        if (declarationsUnallowedHere()) {
            cError(tree.pos, "constants declaration is not allowed here.");
        }

        for (ConstDef.Definition def : tree.defs) {
            Name name = def.name;
            Expression expr = def.expr;
            if (expr.getTag() == Tag.ARRAYLITERAL) {
                ArrayLiteral arrayLiteral = (ArrayLiteral) expr;
                codeLayout.setConstant(name.value, new ArrayOperand());
                if (!arrayLiteral.entries.isEmpty())
                    generateArrayCreation(arrayLiteral.entries);
            } else if (expr.getTag() == Tag.LITERAL) {
                Literal literal = (Literal) expr;
                codeLayout.setConstant(name.value, resolveOperand(literal));
            } else {
                // todo: Более детальное сообщение
                cError(expr.pos, "Literal expected.");
            }
        }
    }

    private Operand resolveOperand(Literal literal) {
        Object value = literal.type;
        if (value instanceof Long || value instanceof Integer) return LongOperand.valueOf(((Number) value).longValue());
        if (value instanceof Double || value instanceof Float)
            return DoubleOperand.valueOf(((Number) value).doubleValue());
        if (value instanceof String) return StringOperand.valueOf((String) value);
        if (value instanceof Boolean) return BooleanOperand.valueOf(((Boolean) value));
        assert value == null;
        return NullOperand.NULL;
    }

    @Override
    public void visitContinue(Continue tree) {
        if (continueChains.isEmpty()) {
            cError(tree.pos, "'continue' is not allowed outside of loop.");
            return;
        }
        code.putPos(tree.pos);
        emitGoto(continueChains.topInt());
        code.dead();
    }

    @Override
    public void visitDoLoop(DoLoop tree) {
        generateLoop(tree, null, tree.cond, null, tree.body, false);
    }

    public void visitEqual(BinaryOp expression) {
//        beginCondition();
//        Expression lhs = expression.lhs;
//        Expression rhs = expression.rhs;
//        int line = TreeInfo.line(expression);
//        if (lhs instanceof NullExpression) {
//            visitExpression(rhs);
//            code.addChainedInstruction(line,
//                    invertCond ? new Ifnull() : new Ifnonnull(),
//                    peekConditionChain(), -1);
//        } else if (rhs instanceof NullExpression) {
//            visitExpression(lhs);
//            code.addChainedInstruction(line,
//                    (invertCond ? new Ifnull() : new Ifnonnull()),
//                    peekConditionChain(), -1);
//        } else if (TreeInfo.resolveShort(lhs) >= 0) {
//            visitExpression(rhs);
//            int shortVal = TreeInfo.resolveShort(lhs);
//            code.addChainedInstruction(line,
//                    (invertCond ? new Ifeq(shortVal) : new Ifne(shortVal)),
//                    peekConditionChain(), -1);
//        } else if (TreeInfo.resolveShort(rhs) >= 0) {
//            visitExpression(rhs);
//            int shortVal = TreeInfo.resolveShort(lhs);
//            code.addChainedInstruction(line,
//                    (invertCond ? new Ifeq(shortVal) : new Ifne(shortVal)),
//                    peekConditionChain(), -1);
//        } else {
//            visitExpression(lhs);
//            visitExpression(rhs);
//            code.addChainedInstruction(line,
//                    (invertCond ? new Ifcmpeq() : new Ifcmpne()),
//                    peekConditionChain(), -2);
//        }
//        endCondition();

        generateComparison(expression);
    }

    private int pushMakeConditionChain() {
        int newChain = code.makeChain();
        conditionalChains.push(newChain);
        return newChain;
    }

    private int popConditionChain() {
        return conditionalChains.popInt();
    }

    private int peekConditionChain() {
        return conditionalChains.topInt();
    }

    @Override
    public void visitFallthrough(Fallthrough tree) {
        if (fallthroughChains.isEmpty()) {
            cError(tree.pos, "'fallthrough' is not allowed outside of switch.");
            return;
        }
        code.putPos(tree.pos);
        emitGoto(fallthroughChains.topInt());
        unsetState(STATE_INFINITY_LOOP); // for cases
        code.dead();
    }

    @Override
    public void visitFor(ForLoop tree) {
        generateLoop(tree, tree.init, tree.cond, tree.step, tree.body, true);
    }

    @Override
    public void visitInvocation(Invocation tree) {
        Instruction instruction;
        int stack = 0;
        boolean noReturnValue = false;
        switch (tree.name.value) {
            case "bool":
                if (tree.args.size() != 1) {
                    cError(tree.pos, "mismatch call parameters: 1 expected, " + tree.args.size() + " got.");
                }
                visitExpression(tree.args.get(0).expr);
                instruction = Bool.INSTANCE;
                break;
            case "print":
                visitInvocationArgs(tree.args);
                instruction = new Print(tree.args.size());
                stack = -tree.args.size();
                noReturnValue = true;
                break;
            case "println":
                visitInvocationArgs(tree.args);
                instruction = new Println(tree.args.size());
                stack = -tree.args.size();
                noReturnValue = true;
                break;
            case "typeof":
            case "gettype":
                if (tree.args.size() != 1) {
                    cError(tree.pos, "mismatch call parameters: 1 expected, " + tree.args.size() + " got.");
                }
                visitExpression(tree.args.get(0).expr);
                instruction = Gettype.INSTANCE;
                break;
            case "ns_time":
                if (tree.args.size() != 0) {
                    cError(tree.pos, "mismatch call parameters: 0 expected, " + tree.args.size() + " got.");
                }
                instruction = NsTime.INSTANCE;
                stack = 1;
                break;
            case "length":
                if (tree.args.size() != 1) {
                    cError(tree.pos, "mismatch call parameters: 1 expected, " + tree.args.size() + " got.");
                }
                visitExpression(tree.args.get(0).expr);
                instruction = Length.INSTANCE;
                break;
            default:
                if (tree.args.size() > 0xff) {
                    cError(tree.pos, "too many parameters.");
                }
                visitInvocationArgs(tree.args);
                instruction = new Call(codeLayout.functionIndex(tree.name.value), (byte) tree.args.size(), tree.name);
                stack = -tree.args.size() + 1;
                break;
        }
        code.putPos(tree.pos);
        code.addInstruction(instruction, stack);
        if (noReturnValue)
            code.addInstruction(ConstNull.INSTANCE, 1);
    }

    private void visitInvocationArgs(List<Invocation.Argument> args) {
        for (Invocation.Argument arg : args) {
            visitExpression(arg.expr);
        }
    }

    @Override
    public void visitFuncDef(FuncDef tree) {
        if (declarationsUnallowedHere()) {
            cError(tree.pos, "Function declaration is not allowed here");
        }

        code.pushContext(tree.pos);
        code.pushScope();

        {
            int nOptionals = 0;
            for (FuncDef.Parameter param : tree.params) {
                Name name = param.name;
                if (code.localExists(name.value)) {
                    cError(name.pos, "Duplicate parameter named '" + name.value + "'.");
                }
                int localIdx = code.resolveLocal(name.value);
                if (param.expr != null) {
                    Expression expr = param.expr;
                    if (expr.getTag() != Tag.LITERAL) {
                        cError(expr.pos, "The values of the optional parameters can only be literals");
                    }
                    code.get_cpb().putDefaultLocalEntry(localIdx, ((Literal) expr).type.getConstantIndex());
                    nOptionals++;
                }
            }

            assert tree.body != null;

            Statement body = tree.body;
            body.accept(this);

            if (body.getTag() == Tag.BLOCK) {
                if (code.isAlive()) {
                    emitRetnull();
                }
            } else {
                assert body instanceof Expression;
                emitReturn();
            }

            codeLayout.setFunction(tree.name.value, JuaFunction.fromCode(
                    tree.name.value,
                    tree.params.size() - nOptionals,
                    tree.params.size(),
                    code.buildCodeSegment(),
                    codeLayout.source.filename()
            ));
        }

        code.popScope();
        code.popContext();
    }

    private boolean declarationsUnallowedHere() {
        return isState(STATE_NO_DECLS);
    }

    public void visitGreaterEqual(BinaryOp expression) {
//        beginCondition();
//        if (expression.lhs instanceof IntExpression) {
//            visitExpression(expression.rhs);
//            code.addFlow(ListDequeUtils.peekLastInt(conditionalChains), invertCond
//                    ? new Ifle(((IntExpression) expression.lhs).value)
//                    : new Ifgt(((IntExpression) expression.lhs).value));
//            code.decStack();
//        } else if (expression.rhs instanceof IntExpression) {
//            visitExpression(expression.lhs);
//            code.addFlow(ListDequeUtils.peekLastInt(conditionalChains), invertCond
//                    ? new Ifge(((IntExpression) expression.rhs).value)
//                    : new Iflt(((IntExpression) expression.rhs).value));
//            code.decStack();
//        } else {
//            visitBinaryOp(expression);
//            code.addFlow(TreeInfo.line(expression), ListDequeUtils.peekLastInt(conditionalChains),
//                    invertCond ? new Ifcmpge() : new Ifcmplt());
//            code.decStack(2);
//        }
//        endCondition();

        generateComparison(expression);
    }

    public void visitGreater(BinaryOp expression) {
//        beginCondition();
//        if (expression.lhs instanceof IntExpression) {
//            visitExpression(expression.rhs);
//            code.addFlow(ListDequeUtils.peekLastInt(conditionalChains), invertCond
//                    ? new Ifge(((IntExpression) expression.lhs).value)
//                    : new Iflt(((IntExpression) expression.lhs).value));
//            code.decStack();
//        } else if (expression.rhs instanceof IntExpression) {
//            visitExpression(expression.lhs);
//            code.addFlow(ListDequeUtils.peekLastInt(conditionalChains), invertCond
//                    ? new Ifle(((IntExpression) expression.rhs).value)
//                    : new Ifgt(((IntExpression) expression.rhs).value));
//            code.decStack();
//        } else {
//            visitBinaryOp(expression);
//            code.addFlow(TreeInfo.line(expression), ListDequeUtils.peekLastInt(conditionalChains),
//                    invertCond ? new Ifcmpgt() : new Ifcmple());
//            code.decStack(2);
//        }
//        endCondition();

        generateComparison(expression);
    }

    @Override
    public void visitIf(If tree) {
        if (tree.elsebody == null) {
            pushMakeConditionChain();
            generateCondition(tree.cond);
            visitBody(tree.thenbody);
            code.resolveChain(popConditionChain());
        } else {
            int el = pushMakeConditionChain();
            int ex = code.makeChain();
            generateCondition(tree.cond);
            int cached_sp = code.getSp();
            boolean thenAlive = visitBody(tree.thenbody);
            emitGoto(ex);
            code.resolveChain(el);
            int body_sp = code.getSp();
            code.setSp(cached_sp);
            boolean elseAlive = visitBody(tree.elsebody);
            code.setSp(Math.max(body_sp, code.getSp()));
            code.resolveChain(ex);
            if (!thenAlive && !elseAlive) code.dead();
        }
    }

    public void visitLessEqual(BinaryOp expression) {
//        beginCondition();
//        Expression lhs = expression.lhs;
//        Expression rhs = expression.rhs;
//        int line = TreeInfo.line(expression);
//        if (TreeInfo.resolveShort(lhs) >= 0) {
//            visitExpression(rhs);
//            int shortVal = TreeInfo.resolveShort(lhs);
//            code.addChainedInstruction(line,
//                    (invertCond ? new Ifge(shortVal) : new Iflt(shortVal)),
//                    peekConditionChain(), -1);
//        } else if (TreeInfo.resolveShort(rhs) >= 0) {
//            visitExpression(rhs);
//            int shortVal = TreeInfo.resolveShort(lhs);
//            code.addChainedInstruction(line,
//                    (invertCond ? new Ifle(shortVal) : new Ifgt(shortVal)),
//                    peekConditionChain(), -1);
//        } else {
//            visitExpression(lhs);
//            visitExpression(rhs);
//            code.addChainedInstruction(line,
//                    (invertCond ? new Ifcmple() : new Ifcmpgt()),
//                    peekConditionChain(), -2);
//        }
//        endCondition();

        generateComparison(expression);
    }

    public void visitLess(BinaryOp expression) {
//        beginCondition();
//        if (expression.lhs instanceof IntExpression) {
//            visitExpression(expression.rhs);
//            code.addFlow(ListDequeUtils.peekLastInt(conditionalChains), invertCond
//                    ? new Ifgt(((IntExpression) expression.lhs).value)
//                    : new Ifle(((IntExpression) expression.lhs).value));
//            code.decStack();
//        } else if (expression.rhs instanceof IntExpression) {
//            visitExpression(expression.lhs);
//            code.addFlow(ListDequeUtils.peekLastInt(conditionalChains), invertCond
//                    ? new Iflt(((IntExpression) expression.rhs).value)
//                    : new Ifge(((IntExpression) expression.rhs).value));
//            code.decStack();
//        } else {
//            visitBinaryOp(expression);
//            code.addFlow(TreeInfo.line(expression), ListDequeUtils.peekLastInt(conditionalChains),
//                    invertCond ? new Ifcmplt() : new Ifcmpge());
//            code.decStack(2);
//        }
//        endCondition();

        generateComparison(expression);
    }

    static boolean isShortIntegerLiteral(Expression tree) {
        if (tree == null) return false;
        if (!hasTag(tree, Tag.LITERAL)) return false;
        Literal literal = (Literal) tree;
        if (!literal.type.isLong()) return false;
        long value = literal.type.longValue();
        return (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE);
    }


    private void generateComparison(BinaryOp expression) {
        beginCondition();
        Expression lhs = expression.lhs;
        Expression rhs = expression.rhs;
        Code.ChainInstructionFactory resultState;
        // todo: Отрефакторить
        int resultStackAdjustment;
        int shortVal;
        boolean lhsNull = lhs instanceof Literal && ((Literal) lhs).type.isNull();
        boolean rhsNull = rhs instanceof Literal && ((Literal) rhs).type.isNull();
        boolean lhsShort = isShortIntegerLiteral(lhs);
        boolean rhsShort = isShortIntegerLiteral(rhs);
        if (lhsShort || rhsShort) {
            shortVal = (int) ((Literal) (lhsShort ? lhs : rhs)).type.longValue();
            visitExpression(lhsShort ? rhs : lhs);
        } else {
            shortVal = Integer.MIN_VALUE;
        }
        boolean invert = isState(STATE_COND_INVERT);
        switch (expression.getTag()) {
            case EQ:
                if (lhsNull || rhsNull) {
                    visitExpression(lhsNull ? rhs : lhs);
                    resultState = (invert ? Ifnull::new : Ifnonnull::new);
                    resultStackAdjustment = -1;
                } else if (lhsShort || rhsShort) {
                    resultState = (dest_ip -> invert ? new Ifeq(dest_ip, shortVal) : new Ifne(dest_ip, shortVal));
                    resultStackAdjustment = -1;
                } else {
                    visitExpression(lhs);
                    visitExpression(rhs);
                    resultState = (invert ? Ifcmpeq::new : Ifcmpne::new);
                    resultStackAdjustment = -2;
                }
                break;
            case NE:
                if (lhsNull || rhsNull) {
                    visitExpression(lhsNull ? rhs : lhs);
                    resultState = (invert ? Ifnonnull::new : Ifnull::new);
                    resultStackAdjustment = -1;
                } else if (lhsShort || rhsShort) {
                    resultState = (dest_ip -> invert ? new Ifne(dest_ip, shortVal) : new Ifeq(dest_ip, shortVal));
                    resultStackAdjustment = -1;
                } else {
                    visitExpression(lhs);
                    visitExpression(rhs);
                    resultState = (invert ? Ifcmpne::new : Ifcmpeq::new);
                    resultStackAdjustment = -2;
                }
                break;
            case LT:
                if (lhsShort || rhsShort) {
                    resultState = lhsShort ?
                            (dest_ip -> invert ? new Ifgt(dest_ip, shortVal) : new Iflt(dest_ip, shortVal)) :
                            (dest_ip -> invert ? new Iflt(dest_ip, shortVal) : new Ifge(dest_ip, shortVal));
                    resultStackAdjustment = -1;
                } else {
                    visitExpression(lhs);
                    visitExpression(rhs);
                    resultState = (invert ? Ifcmplt::new : Ifcmpge::new);
                    resultStackAdjustment = -2;
                }
                break;
            case LE:
                if (lhsShort || rhsShort) {
                    resultState = lhsShort ?
                            (dest_ip -> invert ? new Ifge(dest_ip, shortVal) : new Ifle(dest_ip, shortVal)) :
                            (dest_ip -> invert ? new Ifle(dest_ip, shortVal) : new Ifgt(dest_ip, shortVal));
                    resultStackAdjustment = -1;
                } else {
                    visitExpression(lhs);
                    visitExpression(rhs);
                    resultState = (invert ? Ifcmple::new : Ifcmpgt::new);
                    resultStackAdjustment = -2;
                }
                break;
            case GT:
                if (lhsShort || rhsShort) {
                    resultState = lhsShort ?
                            (dest_ip -> invert ? new Iflt(dest_ip, shortVal) : new Ifgt(dest_ip, shortVal)) :
                            (dest_ip -> invert ? new Ifgt(dest_ip, shortVal) : new Ifle(dest_ip, shortVal));
                    resultStackAdjustment = -1;
                } else {
                    visitExpression(lhs);
                    visitExpression(rhs);
                    resultState = (invert ? Ifcmpgt::new : Ifcmple::new);
                    resultStackAdjustment = -2;
                }
                break;
            case GE:
                if (lhsShort || rhsShort) {
                    resultState = lhsShort ?
                            (dest_ip -> invert ? new Ifle(dest_ip, shortVal) : new Ifge(dest_ip, shortVal)) :
                            (dest_ip -> invert ? new Ifge(dest_ip, shortVal) : new Iflt(dest_ip, shortVal));
                    resultStackAdjustment = -1;
                } else {
                    visitExpression(lhs);
                    visitExpression(rhs);
                    resultState = (invert ? Ifcmpge::new : Ifcmplt::new);
                    resultStackAdjustment = -2;
                }
                break;
            default: throw new AssertionError();
        }
        code.putPos(expression.pos);
        code.addChainedInstruction(resultState, peekConditionChain(), resultStackAdjustment);
        endCondition();
    }

    public void visitNotEqual(BinaryOp expression) {
//        beginCondition();
//        Expression lhs = expression.lhs;
//        Expression rhs = expression.rhs;
//        int line = TreeInfo.line(expression);
//        if (lhs instanceof NullExpression) {
//            visitExpression(rhs);
//            code.addChainedInstruction(line,
//                    invertCond ? new Ifnonnull() : new Ifnull(),
//                    peekConditionChain(), -1);
//        } else if (rhs instanceof NullExpression) {
//            visitExpression(lhs);
//            code.addChainedInstruction(line,
//                    (invertCond ? new Ifnonnull() : new Ifnull()),
//                    peekConditionChain(), -1);
//        } else if (TreeInfo.resolveShort(lhs) >= 0) {
//            visitExpression(rhs);
//            int shortVal = TreeInfo.resolveShort(lhs);
//            code.addChainedInstruction(line,
//                    (invertCond ? new Ifne(shortVal) : new Ifeq(shortVal)),
//                    peekConditionChain(), -1);
//        } else if (TreeInfo.resolveShort(rhs) >= 0) {
//            visitExpression(rhs);
//            int shortVal = TreeInfo.resolveShort(lhs);
//            code.addChainedInstruction(line,
//                    (invertCond ? new Ifne(shortVal) : new Ifeq(shortVal)),
//                    peekConditionChain(), -1);
//        } else {
//            visitExpression(lhs);
//            visitExpression(rhs);
//            code.addChainedInstruction(line,
//                    (invertCond ? new Ifcmpne() : new Ifcmpeq()),
//                    peekConditionChain(), -2);
//        }
//        endCondition();

        generateComparison(expression);
    }

    public void visitNullCoalesce(BinaryOp expression) {
        // todo: Это очевидно неполноценная реализация.
//        visitExpression(expression.lhs);
//        emitDup();
//        int el = code.makeChain();
//        code.addChainedInstruction(new Ifnonnull(), el, -1);
//        code.addInstruction(Pop.INSTANCE, -1);
//        visitExpression(expression.rhs);
//        code.resolveChain(el);

        generateBinary(expression);
    }

    @Override
    public void visitParens(Parens tree) {
        tree.expr.accept(this);
        // todo: выбрасывается AssertionError
        //throw new AssertionError(
        //        "all brackets should have been removed in ConstantFolder");
    }

    private void generateUnary(UnaryOp tree) {
        switch (tree.getTag()) {
            case POSTDEC:
            case PREDEC:
            case POSTINC:
            case PREINC:
                generateIncrease(tree);
                return;
        }

        System.out.println(tree);
        if (hasTag(tree, Tag.NOT)) {
            beginCondition();
            int prev_state = state;
            state ^= STATE_COND_INVERT;
            generateCondition(tree.expr);
            state = prev_state;
            endCondition();
            return;
        }
        tree.expr.accept(this);
        code.putPos(tree.pos);
        code.addInstruction(unary2instr(tree.getTag()));
    }

    public static Instruction unary2instr(Tag tag) {
        switch (tag) {
            case POS: return Pos.INSTANCE;
            case NEG: return Neg.INSTANCE;
            case INVERSE: return Not.INSTANCE;
            default: throw new AssertionError();
        }
    }

    @Override
    public void visitReturn(Tree.Return tree) {
        if (isNull(tree.expr)) {
            emitRetnull();
        } else {
            visitExpression(tree.expr);
            emitReturn();
        }
    }

    private void emitRetnull() {
        code.addInstruction(ReturnNull.INSTANCE);
        code.dead();
    }

    private static boolean isNull(Expression tree) {
        return tree == null || tree.getTag() == Tag.LITERAL && ((Literal) tree).type == null;
    }

    @Override
    public void visitTernaryOp(TernaryOp tree) {
        int el = pushMakeConditionChain();
        int ex = code.makeChain();

        int prev_state = state;
        unsetState(STATE_COND_INVERT);
        generateCondition(tree.cond);
        state = prev_state;

        int cached_sp = code.getSp();
        popConditionChain();
        visitExpression(tree.thenexpr);
        int lhs_sp = code.getSp();
        code.setSp(cached_sp);
        emitGoto(ex);
        code.resolveChain(el);
        int rhs_sp = code.getSp();
        visitExpression(tree.elseexpr);
        code.resolveChain(ex);
        code.setSp(Math.max(lhs_sp, rhs_sp));
    }

    @Override
    public void visitVariable(Var tree) {
        Name name = tree.name;
        code.putPos(tree.pos);
        if (codeLayout.testConstant(name.value)) {
            code.addInstruction(new Getconst(codeLayout.constantIndex(name.value), name), 1);
        } else {
            emitVLoad(tree.name.value);
        }
    }

    @Override
    public void visitWhileLoop(WhileLoop tree) {
        generateLoop(tree, null, tree.cond, null, tree.body, true);
    }

    private void generateLoop(Statement loop, List<Expression> initials, Expression condition, List<Expression> steps,
                             Statement body, boolean testFirst) {
        // cond chain
        int cdc = code.makeChain();
        // begin chain
        int bgc = code.makeChain();
        // exit chain
        int exc = code.makeChain();

        int prev_state = state;
        setState(STATE_INFINITY_LOOP);

        if (initials != null) {
            initials.forEach(this::visitStatement);
        }
        if (condition != null) unsetState(STATE_INFINITY_LOOP);
        if (testFirst && condition != null) {
            code.putPos(loop.pos);
            emitGoto(cdc);
        }
        code.resolveChain(bgc);
        breakChains.push(exc);
        continueChains.push(cdc);
        visitBody(body);
        if (steps != null) {
            steps.forEach(this::visitStatement);
        }
        code.resolveChain(cdc);
        if (condition == null) {
            emitGoto(bgc);
        } else {
            conditionalChains.push(bgc);
            setState(STATE_COND_INVERT);
            generateCondition(condition);
            unsetState(STATE_COND_INVERT);
            popConditionChain();
        }
        code.resolveChain(exc);
        if (isState(STATE_INFINITY_LOOP)) code.dead();
        state = prev_state;
    }

    private void visitExpression(Expression expression) {
        int prev_state = state;
        setState(STATE_RESIDUAL);
        expression.accept(this);
        state = prev_state;
    }

    @Deprecated
    public void visitBinaryOp(BinaryOp expression) {
        switch (expression.tag) {
            case FLOW_AND: visitAnd(expression);                  break;
            case LT:     visitLess(expression);                 break;
            case EQ:     visitEqual(expression);                break;
            case GE:     visitGreaterEqual(expression);         break;
            case GT:     visitGreater(expression);              break;
            case LE:     visitLessEqual(expression);            break;
            case NE:    visitNotEqual(expression);             break;
            case FLOW_OR:  visitOr(expression);                   break;
            case NULLCOALESCE: visitNullCoalesce(expression);   break;
        }

        generateBinary(expression);
    }

    @Deprecated
    public void visitUnaryOp(UnaryOp expression) {
        generateUnary(expression);
    }

    @Override
    public void visitAssignOp(AssignOp tree) {
        if (tree.tag == Tag.ASG_NULLCOALESCE) visitAssignNullCoalesce(tree);

        generateAssignment(tree);
    }

    @Override
    public void visitLiteral(Literal tree) {
        if (tree.type.isLong()) {
            emitPushLong(tree.type.longValue());
        } else if (tree.type.isDouble()) {
            emitPushDouble(tree.type.doubleValue());
        } else if (tree.type.isBoolean()) {
            if (tree.type.booleanValue()) {
                emitPushTrue();
            } else {
                emitPushFalse();
            }
        } else if (tree.type.isString()) {
            emitPushString(tree.type.stringValue());
        } else if (tree.type.isNull()) {
            code.addInstruction(ConstNull.INSTANCE, 1);
        } else {
            throw new AssertionError();
        }
    }

    private void visitList(List<? extends Tree> expressions) {
        int prev_state = state;
        setState(STATE_RESIDUAL);
        for (Tree expr : expressions)
            expr.accept(this);
        state = prev_state;
    }

    private void generateCondition(Expression tree) {
        assert tree != null;
        int prev_state = state;
        setState(STATE_COND);
        visitExpression(tree);
        state = prev_state;
        if (TreeInfo.isConditionalTag(tree.getTag())) {
            return;
        }
        // todo: Здешний код отвратителен. Следует переписать всё с нуля...
//        code.addInstruction(Bool.INSTANCE);
        code.putPos(tree.pos);
        code.addChainedInstruction(isState(STATE_COND_INVERT) ? Iftrue::new : Iffalse::new,
                peekConditionChain(), -1);
    }

    @Override
    public void visitDiscarded(Discarded tree) {
        visitStatement(tree.expr);
        switch (tree.expr.getTag()) {
            case ASSIGN: case ASG_ADD: case ASG_SUB: case ASG_MUL:
            case ASG_DIV: case ASG_REM: case ASG_AND: case ASG_OR:
            case ASG_XOR: case ASG_SL: case ASG_SR: case ASG_NULLCOALESCE:
            case PREINC: case PREDEC: case POSTINC: case POSTDEC:
            default:
                code.addInstruction(Pop.INSTANCE, -1);
        }
    }

    private void generateAssignment(AssignOp expression) {
//        Expression var = expression.var.child();
//        checkAssignable(var);
//        int line = line(expression);
//        if (var instanceof ArrayAccess) {
//            ArrayAccess var0 = (ArrayAccess) var;
//            visitExpression(var0.hs);
//            visitExpression(var0.key);
//            if (state != null) {
//                emitDup2(line);
//                emitALoad(line(var0.key));
//                state.emit(line);
//            } else {
//                visitExpression(expression.expr);
//            }
//            if (isUsed())
//                // Здесь используется var0.key потому что
//                // он может быть дальше, чем var0, а если бы он был ближе
//                // к началу файла, то это было бы некорректно для таблицы линий
//                emitDup_x2(line(var0.key));
//            emitAStore(line);
//        } else if (var instanceof Var) {
//            if (state != null) {
//                visitExpression(var);
//                state.emit(line);
//            } else {
//                visitExpression(expression.expr);
//                if (isUsed()) {
//                    emitDup(line(var));
//                }
//            }
//            emitVStore(line, ((Var) var).name);
//        }

        Expression lhs = expression.dst;
        Expression rhs = expression.src;

        switch (lhs.getTag()) {
            case ARRAYACCESS: {
                ArrayAccess arrayAccess = (ArrayAccess) lhs;
                code.putPos(arrayAccess.pos);
                visitExpression(arrayAccess.expr);
                visitExpression(arrayAccess.index);
                if (hasTag(expression, Tag.ASG_NULLCOALESCE)) {
                    int el = code.makeChain();
                    int ex = code.makeChain();
                    emitDup2();
                    emitALoad();
                    code.addChainedInstruction(Ifnonnull::new, el, -1);
                    visitExpression(rhs);
                    if (isUsed()) {
                        emitDupX2();
                    }
                    code.putPos(arrayAccess.pos);
                    emitAStore();
                    emitGoto(ex);
                    code.resolveChain(el);
                    if (isUsed()) {
                        code.putPos(arrayAccess.pos);
                        emitALoad();
                    } else {
                        code.addInstruction(Pop2.INSTANCE, -2);
                    }
                    code.resolveChain(ex);
                } else {
                    if (!hasTag(expression, Tag.ASSIGN)) {
                        emitDup2();
                        code.putPos(arrayAccess.pos);
                        emitALoad();
                        visitExpression(rhs);
                        code.putPos(expression.pos);
                        code.addInstruction(asg2state(expression.getTag()), -1);
                    } else {
                        visitExpression(rhs);
                    }
                    if (isUsed()) {
                        emitDupX2();
                    }
                    code.putPos(arrayAccess.pos);
                    emitAStore();
                }
                break;
            }
            case VARIABLE: {
                Var variable = (Var) lhs;
                if (hasTag(expression, Tag.ASG_NULLCOALESCE)) {
                    int ex = code.makeChain();
                    visitExpression(lhs);
                    code.addChainedInstruction(Ifnonnull::new, ex, -1);
                    visitExpression(rhs);
                    if (isUsed()) {
                        emitDup();
                    }
                    code.putPos(expression.pos);
                    emitVStore(variable.name.value);
                    if (isUsed()) {
                        int el = code.makeChain();
                        emitGoto(el);
                        code.resolveChain(ex);
                        visitExpression(lhs);
                        code.resolveChain(el);
                    } else {
                        code.resolveChain(ex);
                    }
                } else {
                    if (!hasTag(expression, Tag.ASSIGN)) {
                        visitExpression(lhs);
                        visitExpression(rhs);
                        code.putPos(expression.pos);
                        code.addInstruction(asg2state(expression.getTag()), -1);
                    } else {
                        visitExpression(rhs);
                    }
                    if (isUsed()) {
                        emitDup();
                    }
                    code.putPos(expression.pos);
                    emitVStore(variable.name.value);
                }
                break;
            }
            default: cError(lhs.pos, "assignable expression expected.");
        }
    }

    public static Instruction asg2state(Tag tag) {
        switch (tag) {
            case ASG_ADD: return Add.INSTANCE;
            case ASG_SUB: return Sub.INSTANCE;
            case ASG_MUL: return Mul.INSTANCE;
            case ASG_DIV: return Div.INSTANCE;
            case ASG_REM: return Rem.INSTANCE;
            case ASG_SL: return Shl.INSTANCE;
            case ASG_SR: return Shr.INSTANCE;
            case ASG_AND: return And.INSTANCE;
            case ASG_OR: return Or.INSTANCE;
            case ASG_XOR: return Xor.INSTANCE;
            default: throw new AssertionError();
        }
    }

    // todo: В будущем планируется заменить поле expressionDepth на более удобный механизм.
    private boolean isUsed() {
        return isState(STATE_RESIDUAL);
    }
    @Deprecated
    private void enableUsed() {

    }
    @Deprecated
    private void disableUsed() {

    }

    private void generateIncrease(UnaryOp expression) {
//        Expression hs = expression.hs.child();
//        checkAssignable(hs);
//        int line = line(expression);
//        if (hs instanceof ArrayAccess) {
//            ArrayAccess hs0 = (ArrayAccess) hs;
//            visitExpression(hs0.hs);
//            visitExpression(hs0.key);
//            emitDup2(line(expression));
//            emitALoad(line(hs0.key));
//            if (isPost && (isUsed())) {
//                emitDupX2(line(hs0.key));
//            }
//            code.addInstruction(line, isIncrement
//                    ? Inc.INSTANCE
//                    : Dec.INSTANCE);
//            if (!isPost && (isUsed())) {
//                emitDupX2(line(hs0.key));
//            }
//            emitAStore(line);
//        } else if (hs instanceof Var) {
//            String name = ((Var) hs).name;
//            if (isPost && (isUsed())) {
//                emitVLoad(line, name);
//            }
//            code.addInstruction(line, isIncrement
//                    ? new Vinc(name, code.getLocal(name))
//                    : new Vinc(name, code.getLocal(name)));
//            if (!isPost && (isUsed())) {
//                emitVLoad(line, name);
//            }
//        }

        Expression hs = expression.expr;

        switch (hs.getTag()) {
            case ARRAYACCESS: {
                ArrayAccess arrayAccess = (ArrayAccess) hs;
                code.putPos(arrayAccess.pos);
                visitExpression(arrayAccess.expr);
                visitExpression(arrayAccess.index);
                emitDup2();
                emitALoad();
                if (isUsed() && (hasTag(expression, Tag.POSTINC) || hasTag(expression, Tag.POSTDEC))) {
                    emitDupX2();
                }
                code.putPos(expression.pos);
                code.addInstruction(increase2state(expression.getTag(), -1));
                if (isUsed() && (hasTag(expression, Tag.PREINC) || hasTag(expression, Tag.PREDEC))) {
                    emitDupX2();
                }
                code.putPos(arrayAccess.pos);
                emitAStore();
                break;
            }
            case VARIABLE: {
                Var variable = (Var) hs;
                if (isUsed() && (hasTag(expression, Tag.POSTINC) || hasTag(expression, Tag.POSTDEC))) {
                    variable.accept(this);
                }
                code.putPos(expression.pos);
                code.addInstruction(increase2state(expression.getTag(), code.resolveLocal(variable.name.value)));
                if (isUsed() && (hasTag(expression, Tag.PREINC) || hasTag(expression, Tag.PREDEC))) {
                    variable.accept(this);
                }
                break;
            }
            default: cError(hs.pos, "assignable expression expected.");
        }
    }
    
    private static final boolean hasTag(Tree tree, Tag tag) {
        return tree.getTag() == tag;
    }

    public static Instruction increase2state(Tag tag, int id) {
        switch (tag) {
            case PREINC: case POSTINC:
                return id >= 0 ? new Vinc(id) : Inc.INSTANCE;
            case PREDEC: case POSTDEC:
                return id >= 0 ? new Vdec(id) : Dec.INSTANCE;
            default: throw new AssertionError();
        }
    }

    private boolean visitBody(Statement statement) {
        code.pushScope();
        visitStatement(statement);
        boolean alive = code.isAlive();
        code.popScope();
        return alive;
    }

    private void visitStatement(Statement statement) {
        if (statement == null) return;
        int prev_state = state;
        setState(STATE_NO_DECLS);
        statement.accept(this);
        state = prev_state;
    }

    private void beginCondition() {
        if (isState(STATE_COND)) {
            return;
        }
        pushMakeConditionChain();
    }

    private void endCondition() {
        if (isState(STATE_COND)) {
            return;
        }
        int ex = code.makeChain();
        emitPushTrue();
        emitGoto(ex);
        code.resolveChain(popConditionChain());
        emitPushFalse();
        code.resolveChain(ex);
    }

    private void emitPushLong(long value) {
        if (isShort(value)) {
            code.addInstruction(new Push((short) value), 1);
        } else {
            code.addInstruction(new Ldc(code.resolveLong(value)), 1);
        }
    }

    private void emitPushDouble(double value) {
        long lv = (long) value;
        if (false && lv == value && isShort(lv)) {
//            code.addInstruction(new Push(Operand.Type.LONG, (short) lv), 1);
        } else {
            code.addInstruction(new Ldc(code.resolveDouble(value)), 1);
        }
    }

    private void emitPushString(String value) {
        code.addInstruction(new Ldc(code.resolveString(value)), 1);
    }

    private static boolean isShort(long value) {
        return value >= Short.MIN_VALUE && value <= Short.MAX_VALUE;
    }

    // emit methods

    private void emitPushTrue() { code.addInstruction(ConstTrue.CONST_TRUE, 1); }
    private void emitPushFalse() { code.addInstruction(ConstFalse.CONST_FALSE, 1); }
    private void emitGoto(int chainId) { code.addChainedInstruction(Goto::new, chainId); }
    private void emitDup() { code.addInstruction(Dup.INSTANCE, 1); }
    private void emitDupX1() { code.addInstruction(Dup_x1.INSTANCE, 1); }
    private void emitDupX2() { code.addInstruction(Dup_x2.INSTANCE, 1); }
    private void emitDup2() { code.addInstruction(Dup2.INSTANCE, 2); }
    private void emitDup2X1() { code.addInstruction(Dup2_x1.INSTANCE, 2); }
    private void emitDup2X2() { code.addInstruction(Dup2_x2.INSTANCE, 2); }
    private void emitAdd() { code.addInstruction(Add.INSTANCE, -1); }
    private void emitAnd() { code.addInstruction(And.INSTANCE, -1); }
    private void emitOr() { code.addInstruction(Or.INSTANCE, -1); }
    private void emitXor() { code.addInstruction(Xor.INSTANCE, -1); }
    private void emitDiv() { code.addInstruction(Div.INSTANCE, -1); }
    private void emitLhs() { code.addInstruction(Shl.INSTANCE, -1); }
    private void emitMul() { code.addInstruction(Mul.INSTANCE, -1); }
    private void emitRem() { code.addInstruction(Rem.INSTANCE, -1); }
    private void emitRhs() { code.addInstruction(Shr.INSTANCE, -1); }
    private void emitSub() { code.addInstruction(Sub.INSTANCE, -1); }
    private void emitALoad() { code.addInstruction(Aload.INSTANCE, -1); }
    private void emitVLoad(String name) { code.addInstruction(new Vload(code.resolveLocal(name)), 1); }
    private void emitAStore() { code.addInstruction(Astore.INSTANCE, -3); }
    private void emitVStore(String name) { code.addInstruction(new Vstore(code.resolveLocal(name)), -1); }
    private void emitCaseBody(Statement body) {
        code.resolveChain(fallthroughChains.popInt());
        fallthroughChains.push(code.makeChain());
        int prev_state = state;
        setState(STATE_INFINITY_LOOP);
        visitStatement(body);
        if (!isState(STATE_INFINITY_LOOP)) emitGoto(breakChains.topInt());
        state = prev_state;
    }
    private void emitReturn() {
        code.addInstruction(jua.interpreter.instruction.Return.RETURN, -1);
        code.dead();
    }

    private void cError(int position, String message) {
        throw new CompileError(message, position);
    }
}