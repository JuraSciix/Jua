package jua.compiler;

import it.unimi.dsi.fastutil.ints.Int2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;
import jua.interpreter.instructions.*;
import jua.runtime.ArrayOperand;
import jua.runtime.Operand;
import jua.runtime.ScriptRuntimeFunction;
import jua.compiler.Tree.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class Gen implements Visitor {

    private final CodeData codeData;

    private final Code code;

    private final IntStack breakChains;

    private final IntStack continueChains;

    private final IntStack fallthroughChains;

    private final IntStack conditionalChains;

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


//    private int statementDepth = 0;
//
//    private int expressionDepth = 0;
//
//    private int conditionDepth = 0;

    /**
     * Состояние кода.
     */
    private int state = 0; // unassigned state
//
//    private boolean invertCond = false;
//
//    private boolean loopInfinity = false;

    public Gen(CodeData codeData, LineMap lineMap) {
        this.codeData = codeData;

        code = new Code(codeData.filename, lineMap);
        breakChains = new IntArrayList();
        continueChains = new IntArrayList();
        fallthroughChains = new IntArrayList();
        conditionalChains = new IntArrayList();
    }

    // todo: исправить этот low-cohesion
    public Result getResult() {
        return new Result(codeData, code.toProgram());
    }

    @Override
    public void visitAdd(AddExpression expression) {
        generateBinary(expression);
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

    @Override
    public void visitAnd(AndExpression expression) {
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

    @Override
    public void visitOr(OrExpression expression) {
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
    public void visitArrayAccess(ArrayAccessExpression expression) {
        visitExpression(expression.hs);
        visitExpression(expression.key);
        code.putPos(expression.pos);
        emitALoad();
    }

    @Override
    public void visitArray(ArrayExpression expression) {
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

        code.putPos(expression.pos);
        emitNewArray();
        generateArrayCreation(expression.map);
    }

    private void generateArrayCreation(Map<Expression, Expression> entries) {
        long implicitIndex = 0;
        Iterator<Map.Entry<Expression, Expression>> iterator
                = entries.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Expression, Expression> entry = iterator.next();
            if (iterator.hasNext() || isUsed()) emitDup();
            if (entry.getKey().isEmpty()) {
                code.putPos(entry.getValue().pos);
                emitPushLong(implicitIndex++);
            } else {
                visitExpression(entry.getKey());
            }
            visitExpression(entry.getValue());
            emitAStore();
        }
    }

    private void emitNewArray() {
        code.addInstruction(Newarray.INSTANCE, 1);
    }

    @Override
    public void visitAssignAdd(AssignAddExpression expression) {
        generateAssignment(expression);
    }

    @Override
    public void visitAssignBitAnd(AssignBitAndExpression expression) {
        generateAssignment(expression);
    }

    @Override
    public void visitAssignBitOr(AssignBitOrExpression expression) {
        generateAssignment(expression);
    }

    @Override
    public void visitAssignBitXor(AssignBitXorExpression expression) {
        generateAssignment(expression);
    }

    @Override
    public void visitAssignDivide(AssignDivideExpression expression) {
        generateAssignment(expression);
    }

    @Override
    public void visitAssignLeftShift(AssignShiftLeftExpression expression) {
        generateAssignment(expression);
    }

    @Override
    public void visitAssign(AssignExpression expression) {
        generateAssignment(expression);
    }

    @Override
    public void visitAssignMultiply(AssignMultiplyExpression expression) {
        generateAssignment(expression);
    }

    @Override
    public void visitAssignNullCoalesce(AssignNullCoalesceExpression expression) {
//        int el = code.createFlow();
//        int ex = code.createFlow();
//        boolean isArray = (expression.var.child() instanceof ArrayAccessExpression);
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

    @Override
    public void visitAssignRemainder(AssignRemainderExpression expression) {
        generateAssignment(expression);
    }

    @Override
    public void visitAssignRightShift(AssignShiftRightExpression expression) {
        generateAssignment(expression);
    }

    @Override
    public void visitAssignSubtract(AssignSubtractExpression expression) {
        generateAssignment(expression);
    }

    @Override
    public void visitBitAnd(BitAndExpression expression) {
        generateBinary(expression);
    }

    @Override
    public void visitBitNot(BitNotExpression expression) {
        generateUnary(expression);
    }

    @Override
    public void visitBitOr(BitOrExpression expression) {
        generateBinary(expression);
    }

    @Override
    public void visitBitXor(BitXorExpression expression) {
        generateBinary(expression);
    }

    private void generateBinary(BinaryExpression tree) {
        tree.lhs.accept(this);
        if (tree.isTag(Tag.NULLCOALESCE)) {
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
        code.addInstruction(bin2instr(tree.tag));
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
            case BITAND: return And.INSTANCE;
            case BITOR: return Or.INSTANCE;
            case BITXOR: return Xor.INSTANCE;
            default: throw new AssertionError();
        }
    }

    @Override
    public void visitBlock(BlockStatement statement) {
        if (!isState(STATE_ROOTED)) { // is root?
            code.pushContext(statement.pos);
            code.pushScope();
            int prev_state = state;
            setState(STATE_ROOTED);
            generateStatementsWhileAlive(statement.statements);
            state = prev_state;
            code.addInstruction(Halt.INSTANCE);
            code.popScope();
        } else {
            generateStatementsWhileAlive(statement.statements);
        }
    }

    private void generateStatementsWhileAlive(List<Statement> statements) {
        for (Statement statement : statements) {
            // Таким образом, мы упускаем ошибки в мертвом коде
//            if (!code.isAlive()) {
//                break;
//            }
            statement.accept(this);
        }
    }

    @Override
    public void visitBreak(BreakStatement statement) {
        if (breakChains.isEmpty()) {
            cError(statement.pos, "'break' is not allowed outside of loop/switch.");
            return;
        }
        code.putPos(statement.pos);
        emitGoto(breakChains.topInt());
        unsetState(STATE_INFINITY_LOOP);
    }

    private int switch_start_ip;

    private Int2IntMap cases;

    private int default_case;

    @Override
    public void visitCase(CaseStatement tree) {
        if (tree.expressions != null) { // is not default case?
            for (Expression expr : tree.expressions) {
                int cp;
                if (expr.getClass() == StringExpression.class)
                    cp = code.resolveString(((StringExpression) expr).value);
                else if (expr.getClass() == IntExpression.class)
                    cp = code.resolveLong(((IntExpression) expr).value);
                else if (expr.getClass() == FloatExpression.class)
                    cp = code.resolveDouble(((FloatExpression) expr).value);
                else {
                    cError(expr.pos, "constant expected.");
                    continue;
                }
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
    public void visitSwitch(SwitchStatement tree) {
        visitExpression(tree.selector);
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
        for (CaseStatement _case : tree.cases) {
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
    public void visitClone(CloneExpression expression) {
        generateUnary(expression);
    }

    @Override
    public void visitConstantDeclare(ConstantDeclareStatement statement) {
        if (isState(STATE_NO_DECLS)) {
            cError(statement.pos, "constants declaration is not allowed here.");
        }
        for (int i = 0; i < statement.names.size(); i++) {
            String name = statement.names.get(i);
            if (codeData.testConstant(name)) {
                cError(statement.pos, "constant '" + name + "' already declared");
            }
            Expression expr = statement.expressions.get(i);
            Operand value;
            if (expr instanceof ArrayExpression) {
                value = new ArrayOperand();
                if (((ArrayExpression) expr).map.size() > 0) {
                    code.addInstruction(new Getconst(name), 1);
                    generateArrayCreation(((ArrayExpression) expr).map);
                }
            } else {
                assert expr instanceof LiteralExpression;
                value = TreeInfo.resolveLiteral((LiteralExpression) expr);
            }
            codeData.setConstant(name, value);
        }
    }

    @Override
    public void visitContinue(ContinueStatement statement) {
        if (continueChains.isEmpty()) {
            cError(statement.pos, "'continue' is not allowed outside of loop.");
            return;
        }
        code.putPos(statement.pos);
        emitGoto(continueChains.topInt());
        code.dead();
    }

    @Override
    public void visitDivide(DivideExpression expression) {
        generateBinary(expression);
    }

    @Override
    public void visitDo(DoStatement statement) {
        generateLoop(statement, null, statement.cond, null, statement.body, false);
    }

    @Override
    public void visitEqual(EqualExpression expression) {
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
    public void visitFallthrough(FallthroughStatement statement) {
        if (fallthroughChains.isEmpty()) {
            cError(statement.pos, "'fallthrough' is not allowed outside of switch.");
            return;
        }
        code.putPos(statement.pos);
        emitGoto(fallthroughChains.topInt());
        unsetState(STATE_INFINITY_LOOP); // for cases
        code.dead();
    }

    @Override
    public void visitFalse(FalseExpression expression) {
        code.putPos(expression.pos);
        emitPushFalse();
    }

    @Override
    public void visitFloat(FloatExpression expression) {
        code.putPos(expression.pos);
        emitPushDouble(expression.value);
    }

    @Override
    public void visitFor(ForStatement statement) {
        generateLoop(statement, statement.init, statement.cond, statement.step, statement.body, true);
    }

    @Override
    public void visitFunctionCall(FunctionCallExpression expression) {
        Instruction instruction;
        int stack = 0;
        boolean noReturnValue = false;
        switch (expression.name) {
            case "bool":
                if (expression.args.size() != 1) {
                    cError(expression.pos, "mismatch call parameters: 1 expected, " + expression.args.size() + " got.");
                }
                visitExpression(expression.args.get(0));
                instruction = Bool.INSTANCE;
                break;
            case "print":
                visitExprList(expression.args);
                instruction = new Print(expression.args.size());
                stack = -expression.args.size();
                noReturnValue = true;
                break;
            case "println":
                visitExprList(expression.args);
                instruction = new Println(expression.args.size());
                stack = -expression.args.size();
                noReturnValue = true;
                break;
            case "typeof":
            case "gettype":
                if (expression.args.size() != 1) {
                    cError(expression.pos, "mismatch call parameters: 1 expected, " + expression.args.size() + " got.");
                }
                visitExpression(expression.args.get(0));
                instruction = Gettype.INSTANCE;
                break;
            case "ns_time":
                if (expression.args.size() != 0) {
                    cError(expression.pos, "mismatch call parameters: 0 expected, " + expression.args.size() + " got.");
                }
                instruction = NsTime.INSTANCE;
                stack = 1;
                break;
            case "length":
                if (expression.args.size() != 1) {
                    cError(expression.pos, "mismatch call parameters: 1 expected, " + expression.args.size() + " got.");
                }
                visitExpression(expression.args.get(0));
                instruction = Length.INSTANCE;
                break;
            default:
                if (expression.args.size() > 0xff) {
                    cError(expression.pos, "too many parameters.");
                }
                visitExprList(expression.args);
                instruction = new Call(expression.name, (byte) expression.args.size());
                stack = -expression.args.size() + 1;
                break;
        }
        code.putPos(expression.pos);
        code.addInstruction(instruction, stack);
        if (noReturnValue)
            code.addInstruction(ConstNull.INSTANCE, 1);
    }

    @Override
    public void visitFunctionDefine(FunctionDefineStatement statement) {
        if (isState(STATE_NO_DECLS))
            cError(statement.pos, "function declaration is not allowed here.");
        if (codeData.testFunction(statement.name))
            cError(statement.pos, "function '" + statement.name + "' already declared.");
        code.pushContext(statement.pos);
        code.pushScope();
        int[] locals = statement.names.stream().mapToInt(n -> {
            if (statement.names.indexOf(n) != statement.names.lastIndexOf(n)) {
                cError(statement.pos, "duplicate argument '" + n + "'.");
            }
            return code.resolveLocal(n);
        }).toArray();
        int[] optionals = statement.optionals.stream().mapToInt(a -> {
            assert (a instanceof LiteralExpression);
            return TreeInfo.resolveLiteral(code, (LiteralExpression) a);
        }).toArray();
        visitStatement(statement.body);
        if (!statement.body.isTag(Tag.COMPOUND))
            emitReturn();
        else
            emitRetnull();
        codeData.setFunction(statement.name, new ScriptRuntimeFunction(
                locals, // function arguments must be first at local list
                optionals,
                code.toProgram()));
        code.popScope();
        code.popContext();
    }

    @Override
    public void visitGreaterEqual(GreaterEqualExpression expression) {
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
//            visitBinary(expression);
//            code.addFlow(TreeInfo.line(expression), ListDequeUtils.peekLastInt(conditionalChains),
//                    invertCond ? new Ifcmpge() : new Ifcmplt());
//            code.decStack(2);
//        }
//        endCondition();

        generateComparison(expression);
    }

    @Override
    public void visitGreater(GreaterExpression expression) {
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
//            visitBinary(expression);
//            code.addFlow(TreeInfo.line(expression), ListDequeUtils.peekLastInt(conditionalChains),
//                    invertCond ? new Ifcmpgt() : new Ifcmple());
//            code.decStack(2);
//        }
//        endCondition();

        generateComparison(expression);
    }

    @Override
    public void visitIf(IfStatement statement) {
        if (statement.elseBody == null) {
            pushMakeConditionChain();
            generateCondition(statement.cond);
            visitBody(statement.body);
            code.resolveChain(popConditionChain());
        } else {
            int el = pushMakeConditionChain();
            int ex = code.makeChain();
            generateCondition(statement.cond);
            int cached_sp = code.getSp();
            boolean thenAlive = visitBody(statement.body);
            emitGoto(ex);
            code.resolveChain(el);
            int body_sp = code.getSp();
            code.setSp(cached_sp);
            boolean elseAlive = visitBody(statement.elseBody);
            code.setSp(Math.max(body_sp, code.getSp()));
            code.resolveChain(ex);
            if (!thenAlive && !elseAlive) code.dead();
        }
    }

    @Override
    public void visitInt(IntExpression expression) {
        code.putPos(expression.pos);
        emitPushLong(expression.value);
    }

    @Override
    public void visitLeftShift(ShiftLeftExpression expression) {
        generateBinary(expression);
    }

    @Override
    public void visitLessEqual(LessEqualExpression expression) {
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

    @Override
    public void visitLess(LessExpression expression) {
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
//            visitBinary(expression);
//            code.addFlow(TreeInfo.line(expression), ListDequeUtils.peekLastInt(conditionalChains),
//                    invertCond ? new Ifcmplt() : new Ifcmpge());
//            code.decStack(2);
//        }
//        endCondition();

        generateComparison(expression);
    }

    private void generateComparison(ConditionalExpression expression) {
        beginCondition();
        Expression lhs = expression.lhs;
        Expression rhs = expression.rhs;
        Code.ChainInstructionFactory resultState;
        int resultStackAdjustment;
        int shortVal;
        boolean lhsNull = (lhs instanceof NullExpression);
        boolean rhsNull = (rhs instanceof NullExpression);
        boolean lhsShort = TreeInfo.testShort(lhs);
        boolean rhsShort = TreeInfo.testShort(rhs);
        if (lhsShort || rhsShort) {
            shortVal = ((Number) ((IntExpression) (lhsShort ? lhs : rhs)).value).shortValue();
            visitExpression(lhsShort ? rhs : lhs);
        } else {
            shortVal = Integer.MIN_VALUE;
        }
        boolean invert = isState(STATE_COND_INVERT);
        switch (expression.tag) {
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
            case NEQ:
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

    @Override
    public void visitMultiply(MultiplyExpression expression) {
        generateBinary(expression);
    }

    @Override
    public void visitNegative(NegativeExpression expression) {
        generateUnary(expression);
    }

    @Override
    public void visitNotEqual(NotEqualExpression expression) {
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

    @Override
    public void visitNot(NotExpression expression) {
        generateUnary(expression);
    }

    @Override
    public void visitNullCoalesce(NullCoalesceExpression expression) {
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
    public void visitNull(NullExpression expression) {
        code.addInstruction(ConstNull.INSTANCE, 1);
    }

    @Override
    public void visitParens(ParensExpression expression) {
        throw new AssertionError(
                "all brackets should have been removed in ConstantFolder");
    }

    @Override
    public void visitPositive(PositiveExpression expression) {
        generateUnary(expression);
    }

    private void generateUnary(UnaryExpression tree) {
        if (tree.isTag(Tag.LOGCMPL)) {
            beginCondition();
            int prev_state = state;
            state ^= STATE_COND_INVERT;
            generateCondition(tree.hs);
            state = prev_state;
            endCondition();
            return;
        }
        tree.hs.accept(this);
        code.putPos(tree.pos);
        code.addInstruction(unary2instr(tree.tag));
    }

    public static Instruction unary2instr(Tag tag) {
        switch (tag) {
            case POS: return Pos.INSTANCE;
            case NEG: return Neg.INSTANCE;
            case BITCMPL: return Not.INSTANCE;
            default: throw new AssertionError();
        }
    }

    @Override
    public void visitPostDecrement(PostDecrementExpression expression) {
        generateIncrease(expression, false, true);
    }

    @Override
    public void visitPostIncrement(PostIncrementExpression expression) {
        generateIncrease(expression, true, true);
    }

    @Override
    public void visitPreDecrement(PreDecrementExpression expression) {
        generateIncrease(expression, false, false);
    }

    @Override
    public void visitPreIncrement(PreIncrementExpression expression) {
        generateIncrease(expression, true, false);
    }

    @Deprecated
    @Override
    public void visitPrintln(PrintlnStatement statement) {
//        visitExprList(statement.expressions);
//        int count = statement.expressions.size();
//        code.putPos(statement.position);
//        code.addInstruction(new Println(count), -count);
        throw new AssertionError("deprecated");
    }

    @Deprecated
    @Override
    public void visitPrint(PrintStatement statement) {
//        visitExprList(statement.expressions);
//        int count = statement.expressions.size();
//        code.putPos(statement.position);
//        code.addInstruction(new Print(count), -count);
        throw new AssertionError("deprecated");
    }

    @Override
    public void visitRemainder(RemainderExpression expression) {
        generateBinary(expression);
    }

    @Override
    public void visitReturn(ReturnStatement statement) {
        if (isNull(statement.expr)) {
            emitRetnull();
        } else {
            visitExpression(statement.expr);
            emitReturn();
        }
    }

    private void emitRetnull() {
        code.addInstruction(ReturnNull.INSTANCE);
        code.dead();
    }

    private static boolean isNull(Expression expression) {
        return TreeInfo.isNull(expression);
    }

    @Override
    public void visitRightShift(ShiftRightExpression expression) {
        generateBinary(expression);
    }

    @Override
    public void visitString(StringExpression expression) {
        code.putPos(expression.pos);
        emitPushString(expression.value);
    }

    @Override
    public void visitSubtract(SubtractExpression expression) {
        generateBinary(expression);
    }

    @Override
    public void visitTernary(TernaryExpression expression) {
        int el = pushMakeConditionChain();
        int ex = code.makeChain();

        int prev_state = state;
        unsetState(STATE_COND_INVERT);
        generateCondition(expression.cond);
        state = prev_state;

        int cached_sp = code.getSp();
        popConditionChain();
        visitExpression(expression.lhs);
        int lhs_sp = code.getSp();
        code.setSp(cached_sp);
        emitGoto(ex);
        code.resolveChain(el);
        int rhs_sp = code.getSp();
        visitExpression(expression.rhs);
        code.resolveChain(ex);
        code.setSp(Math.max(lhs_sp, rhs_sp));
    }

    @Override
    public void visitTrue(TrueExpression expression) {
        code.putPos(expression.pos);
        emitPushTrue();
    }

    @Override
    public void visitVariable(VariableExpression expression) {
        String name = expression.name;
        code.putPos(expression.pos);
        if (codeData.testConstant(name)) {
            code.addInstruction(new Getconst(name), 1);
        } else {
            emitVLoad(expression.name);
        }
    }

    @Override
    public void visitWhile(WhileStatement statement) {
        generateLoop(statement, null, statement.cond, null, statement.body, true);
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
    private void visitBinary(BinaryExpression expression) {
        enableUsed();
        visitStatement(expression.lhs);
        visitStatement(expression.rhs);
        disableUsed();
    }

    @Deprecated
    private void visitUnary(UnaryExpression expression) {
        enableUsed();
        visitStatement(expression.hs);
        disableUsed();
    }

    private void visitExprList(List<? extends Expression> expressions) {
        int prev_state = state;
        setState(STATE_RESIDUAL);
        for (Expression expr : expressions)
            expr.accept(this);
        state = prev_state;
    }

    private void generateCondition(Expression expression) {
        assert expression != null;
        int prev_state = state;
        setState(STATE_COND);
        visitExpression(expression);
        state = prev_state;
        if (expression.isCondition()) {
            return;
        }
        // todo: Здешний код отвратителен. Следует переписать всё с нуля...
//        code.addInstruction(Bool.INSTANCE);
        code.putPos(expression.pos);
        code.addChainedInstruction(!isState(STATE_COND_INVERT) ?
                        dest_ip -> new Iffalse(dest_ip) :
                        dest_ip -> new Iftrue(dest_ip),
                peekConditionChain(), -1);
    }

    @Override
    public void visitUnused(UnusedExpression expression) {
        visitStatement(expression.expression);
        switch (expression.expression.tag) {
            case ASG: case ASG_ADD: case ASG_SUB: case ASG_MUL:
            case ASG_DIV: case ASG_REM: case ASG_BITAND: case ASG_BITOR:
            case ASG_BITXOR: case ASG_SL: case ASG_SR: case ASG_NULLCOALESCE:
            case PRE_INC: case PRE_DEC: case POST_INC: case POST_DEC:
            case PRINT: case PRINTLN:
                break;
            default:
                code.addInstruction(Pop.INSTANCE, -1);
        }
    }

    private void generateAssignment(AssignmentExpression expression) {
//        Expression var = expression.var.child();
//        checkAssignable(var);
//        int line = line(expression);
//        if (var instanceof ArrayAccessExpression) {
//            ArrayAccessExpression var0 = (ArrayAccessExpression) var;
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
//        } else if (var instanceof VariableExpression) {
//            if (state != null) {
//                visitExpression(var);
//                state.emit(line);
//            } else {
//                visitExpression(expression.expr);
//                if (isUsed()) {
//                    emitDup(line(var));
//                }
//            }
//            emitVStore(line, ((VariableExpression) var).name);
//        }

        Expression lhs = expression.var;
        Expression rhs = expression.expr;

        switch (lhs.tag) {
            case ARRAY_ACCESS: {
                ArrayAccessExpression arrayAccess = (ArrayAccessExpression) lhs;
                code.putPos(arrayAccess.pos);
                visitExpression(arrayAccess.hs);
                visitExpression(arrayAccess.key);
                if (expression.isTag(Tag.ASG_NULLCOALESCE)) {
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
                    if (!expression.isTag(Tag.ASG)) {
                        emitDup2();
                        code.putPos(arrayAccess.pos);
                        emitALoad();
                        visitExpression(rhs);
                        code.putPos(expression.pos);
                        code.addInstruction(asg2state(expression.tag), -1);
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
                VariableExpression variable = (VariableExpression) lhs;
                if (expression.isTag(Tag.ASG_NULLCOALESCE)) {
                    int ex = code.makeChain();
                    visitExpression(lhs);
                    code.addChainedInstruction(Ifnonnull::new, ex, -1);
                    visitExpression(rhs);
                    if (isUsed()) {
                        emitDup();
                    }
                    code.putPos(expression.pos);
                    emitVStore(variable.name);
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
                    if (!expression.isTag(Tag.ASG)) {
                        visitExpression(lhs);
                        visitExpression(rhs);
                        code.putPos(expression.pos);
                        code.addInstruction(asg2state(expression.tag), -1);
                    } else {
                        visitExpression(rhs);
                    }
                    if (isUsed()) {
                        emitDup();
                    }
                    code.putPos(expression.pos);
                    emitVStore(variable.name);
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
            case ASG_BITAND: return And.INSTANCE;
            case ASG_BITOR: return Or.INSTANCE;
            case ASG_BITXOR: return Xor.INSTANCE;
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

    private void generateIncrease(IncreaseExpression expression,
                                  @Deprecated boolean isIncrement,
                                  @Deprecated boolean isPost) {
//        Expression hs = expression.hs.child();
//        checkAssignable(hs);
//        int line = line(expression);
//        if (hs instanceof ArrayAccessExpression) {
//            ArrayAccessExpression hs0 = (ArrayAccessExpression) hs;
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
//        } else if (hs instanceof VariableExpression) {
//            String name = ((VariableExpression) hs).name;
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

        Expression hs = expression.hs;

        switch (hs.tag) {
            case ARRAY_ACCESS: {
                ArrayAccessExpression arrayAccess = (ArrayAccessExpression) hs;
                code.putPos(arrayAccess.pos);
                visitExpression(arrayAccess.hs);
                visitExpression(arrayAccess.key);
                emitDup2();
                emitALoad();
                if (isUsed() && (expression.isTag(Tag.POST_INC) || expression.isTag(Tag.POST_DEC))) {
                    emitDupX2();
                }
                code.putPos(expression.pos);
                code.addInstruction(increase2state(expression.tag, -1));
                if (isUsed() && (expression.isTag(Tag.PRE_INC) || expression.isTag(Tag.PRE_DEC))) {
                    emitDupX2();
                }
                code.putPos(arrayAccess.pos);
                emitAStore();
                break;
            }
            case VARIABLE: {
                VariableExpression variable = (VariableExpression) hs;
                if (isUsed() && (expression.isTag(Tag.POST_INC) || expression.isTag(Tag.POST_DEC))) {
                    variable.accept(this);
                }
                code.putPos(expression.pos);
                code.addInstruction(increase2state(expression.tag, code.resolveLocal(variable.name)));
                if (isUsed() && (expression.isTag(Tag.PRE_INC) || expression.isTag(Tag.PRE_DEC))) {
                    variable.accept(this);
                }
                break;
            }
            default: cError(hs.pos, "assignable expression expected.");
        }
    }

    public static Instruction increase2state(Tag tag, int id) {
        switch (tag) {
            case PRE_INC: case POST_INC:
                return id >= 0 ? new Vinc(id) : Inc.INSTANCE;
            case PRE_DEC: case POST_DEC:
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
        code.addInstruction(Return.RETURN, -1);
        code.dead();
    }

    private void cError(int position, String message) {
        throw new CompileError(message, position);
    }
}