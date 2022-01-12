package jua.compiler;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;
import jua.interpreter.instructions.*;
import jua.interpreter.runtime.ArrayOperand;
import jua.interpreter.runtime.Constant;
import jua.interpreter.runtime.Operand;
import jua.interpreter.runtime.ScriptRuntimeFunction;
import jua.parser.Tree.*;

import java.util.*;

import static jua.interpreter.instructions.Switch.Part;

public final class Gen implements Visitor {

    private final CodeData codeData;
    
    private final Code code;

    private final IntStack breakChains;

    private final IntStack continueChains;

    private final IntStack fallthroughChains;

    // todo: Это дикий костыль.
    private final Deque<List<Part>> switchPartsStack;

    private final IntStack conditionalChains;

    // todo: Избавиться от ниже определенных полей

    private int statementDepth = 0;

    private int expressionDepth = 0;

    private int conditionDepth = 0;

    private boolean invertCond = false;
    
    private boolean loopInfinity = false;

    public Gen(CodeData codeData) {
        this.codeData = codeData;
        code = new Code();
        breakChains = new IntArrayList();
        continueChains = new IntArrayList();
        fallthroughChains = new IntArrayList();
        switchPartsStack = new ArrayDeque<>();
        conditionalChains = new IntArrayList();
    }

    // todo: исправить этот low-cohesion
    public Result getResult() {
        return new Result(codeData, code.toProgram());
    }

    @Override
    public void visitAdd(AddExpression expression) {
        visitBinary(expression);
        emitAdd(TreeInfo.line(expression));
    }

    @Override
    public void visitAnd(AndExpression expression) {
        beginCondition();
        if (invertCond) {
            if (expression.rhs == null) {
                generateCondition(expression.lhs);
            } else {
                int fa = pushMakeConditionChain();
                invertCond = false;
                generateCondition(expression.lhs);
                popConditionChain();
                invertCond = true;
                generateCondition(expression.rhs);
                code.resolveChain(fa);
            }
        } else {
            generateCondition(expression.lhs);
            generateCondition(expression.rhs);
        }
        endCondition();
    }

    @Override
    public void visitArrayAccess(ArrayAccessExpression expression) {
        visitExpression(expression.hs);
        visitExpression(expression.key);
        emitALoad(TreeInfo.line(expression));
    }

    @Override
    public void visitArray(ArrayExpression expression) {
//        code.incStack();
//        code.addState(Newarray.INSTANCE);
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

        emitNewArray(TreeInfo.line(expression));
        generateArrayCreation(expression.map);
    }

    private void generateArrayCreation(Map<Expression, Expression> entries) {
        long implicitIndex = 0;
        Iterator<Map.Entry<Expression, Expression>> iterator
                = entries.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Expression, Expression> entry = iterator.next();
            emitDup();
            if (entry.getKey().isEmpty()) {
                emitPushLong(TreeInfo.line(entry.getValue()), implicitIndex++);
            } else {
                visitExpression(entry.getKey());
            }
            visitExpression(entry.getValue());
            emitAStore(entry.getKey().isEmpty()
                    ? TreeInfo.line(entry.getValue())
                    : TreeInfo.line(entry.getKey()));
        }
    }

    private void emitNewArray(int line) {
        code.addState(line, Newarray.INSTANCE, 1);
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
        visitExpression(expression.lhs);
        visitExpression(expression.rhs);
        emitAnd(TreeInfo.line(expression));
    }

    @Override
    public void visitBitNot(BitNotExpression expression) {
        visitUnary(expression);
        code.addState(TreeInfo.line(expression), Not.INSTANCE);
    }

    @Override
    public void visitBitOr(BitOrExpression expression) {
        visitBinary(expression);
        emitOr(TreeInfo.line(expression));
    }

    @Override
    public void visitBitXor(BitXorExpression expression) {
        visitBinary(expression);
        emitXor(TreeInfo.line(expression));
    }

    @Override
    public void visitBlock(BlockStatement statement) {
        if (statementDepth == 0) { // is root?
            code.pushContext(TreeInfo.sourceName(statement));
            code.pushScope();
            generateStatementsWhileAlive(statement.statements);
            code.addState(1, Halt.INSTANCE);
            code.popScope();
        } else {
            generateStatementsWhileAlive(statement.statements);
        }
    }
    
    private void generateStatementsWhileAlive(List<Statement> statements) {
        statementDepth++;
        for (Statement statement : statements) {
            if (!code.isAlive()) {
                break;
            }
            statement.accept(this);
        }
        statementDepth--;
    }

    @Override
    public void visitBreak(BreakStatement statement) {
        if (breakChains.isEmpty()) {
            cError(statement.getPosition(), "'break' is not allowed outside of loop/switch.");
            return;
        }
        emitGoto(TreeInfo.line(statement), breakChains.topInt());
        loopInfinity = false;
    }

    @Override
    public void visitCase(CaseStatement statement) {
        switchPartsStack.getLast().add(new Part(code.currentBci(), statement.expressions.stream()
                .mapToInt(a -> {
                    assert a instanceof LiteralExpression;
                    return TreeInfo.resolveLiteral(code, (LiteralExpression) a);
                })
                .toArray()));
        emitCaseBody(statement.body);
    }

    @Override
    public void visitClone(CloneExpression expression) {
        visitUnary(expression);
        code.addState(Clone.INSTANCE);
    }

    @Override
    public void visitConstantDeclare(ConstantDeclareStatement statement) {
        if (statementDepth > 1) {
            cError(statement.getPosition(), "constants declaration is not allowed here.");
        }
        for (int i = 0; i < statement.names.size(); i++) {
            String name = statement.names.get(i);
            if (codeData.testConstant(name)) {
                cError(statement.getPosition(), "constant '" + name + "' already declared");
            }
            Expression expr = statement.expressions.get(i);
            Operand value;
            if (expr instanceof ArrayExpression) {
                value = new ArrayOperand();
                code.addState(new Getconst(name), 1);
                generateArrayCreation(((ArrayExpression) expr).map);
            } else {
                assert expr instanceof LiteralExpression;
                value = TreeInfo.resolveLiteral((LiteralExpression) expr);
            }
            codeData.setConstant(name, new Constant(value, false));
        }
    }

    @Override
    public void visitContinue(ContinueStatement statement) {
        if (continueChains.isEmpty()) {
            cError(statement.getPosition(), "'continue' is not allowed outside of loop.");
            return;
        }
        emitGoto(TreeInfo.line(statement), continueChains.topInt());
        code.dead();
    }

    @Override
    public void visitDivide(DivideExpression expression) {
        visitBinary(expression);
        emitDiv(TreeInfo.line(expression));

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
//            code.addChainedState(line,
//                    invertCond ? new Ifnull() : new Ifnonnull(),
//                    peekConditionChain(), -1);
//        } else if (rhs instanceof NullExpression) {
//            visitExpression(lhs);
//            code.addChainedState(line,
//                    (invertCond ? new Ifnull() : new Ifnonnull()),
//                    peekConditionChain(), -1);
//        } else if (TreeInfo.resolveShort(lhs) >= 0) {
//            visitExpression(rhs);
//            int shortVal = TreeInfo.resolveShort(lhs);
//            code.addChainedState(line,
//                    (invertCond ? new Ifeq(shortVal) : new Ifne(shortVal)),
//                    peekConditionChain(), -1);
//        } else if (TreeInfo.resolveShort(rhs) >= 0) {
//            visitExpression(rhs);
//            int shortVal = TreeInfo.resolveShort(lhs);
//            code.addChainedState(line,
//                    (invertCond ? new Ifeq(shortVal) : new Ifne(shortVal)),
//                    peekConditionChain(), -1);
//        } else {
//            visitExpression(lhs);
//            visitExpression(rhs);
//            code.addChainedState(line,
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
            cError(statement.getPosition(), "'fallthrough' is not allowed outside of switch.");
            return;
        }
        emitGoto(TreeInfo.line(statement), fallthroughChains.topInt());
        loopInfinity = false; // for cases
        code.dead();
    }

    @Override
    public void visitFalse(FalseExpression expression) {
        emitPushFalse(TreeInfo.line(expression));
    }

    @Override
    public void visitFloat(FloatExpression expression) {
        emitPushDouble(TreeInfo.line(expression), expression.value);
    }

    @Override
    public void visitFor(ForStatement statement) {
        generateLoop(statement, statement.init, statement.cond, statement.step, statement.body, true);
    }

    @Override
    public void visitFunctionCall(FunctionCallExpression expression) {
        if (expression.args.size() > 0xff) {
            cError(expression.position, "too many parameters.");
        }
        visitList(expression.args);
        code.addState(TreeInfo.line(expression),
                new Call(expression.name, (byte) expression.args.size()),
                Math.max(1, expression.args.size()));
    }

    @Override
    public void visitFunctionDefine(FunctionDefineStatement statement) {
        if (statementDepth > 1)
            cError(statement.getPosition(), "function declaration is not allowed here.");
        if (codeData.testFunction(statement.name))
            cError(statement.getPosition(), "function '" + statement.name + "' already declared.");
        code.pushContext(TreeInfo.sourceName(statement));
        code.pushScope();
        int[] locals = statement.names.stream().mapToInt(n -> {
            if (statement.names.indexOf(n) != statement.names.lastIndexOf(n)) {
                cError(statement.getPosition(), "duplicate argument '" + n + "'.");
            }
            return code.resolveLocal(n);
        }).toArray();
        visitStatement(statement.body);
        emitRetnull(TreeInfo.line(statement));
        codeData.setFunction(statement.name, new ScriptRuntimeFunction(
                locals, // function arguments must be first at local list
                statement.optionals.stream().mapToInt(a -> {
                    assert a instanceof LiteralExpression;
                    return TreeInfo.resolveLiteral(code, (LiteralExpression) a);
                }).toArray(),
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
            boolean thenAlive = visitBody(statement.body);
            emitGoto(0, ex);
            code.resolveChain(el);
            boolean elseAlive = visitBody(statement.elseBody);
            code.resolveChain(ex);
            if (!thenAlive && !elseAlive) code.dead();
        }
    }

    @Override
    public void visitInt(IntExpression expression) {
        emitPushLong(TreeInfo.line(expression), expression.value);
    }

    @Override
    public void visitLeftShift(ShiftLeftExpression expression) {
        visitBinary(expression);
        emitLhs(TreeInfo.line(expression));

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
//            code.addChainedState(line,
//                    (invertCond ? new Ifge(shortVal) : new Iflt(shortVal)),
//                    peekConditionChain(), -1);
//        } else if (TreeInfo.resolveShort(rhs) >= 0) {
//            visitExpression(rhs);
//            int shortVal = TreeInfo.resolveShort(lhs);
//            code.addChainedState(line,
//                    (invertCond ? new Ifle(shortVal) : new Ifgt(shortVal)),
//                    peekConditionChain(), -1);
//        } else {
//            visitExpression(lhs);
//            visitExpression(rhs);
//            code.addChainedState(line,
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
        ChainInstruction resultState;
        int resultStackAdjustment;
        int shortVal = Integer.MIN_VALUE;
        boolean lhsNull = (lhs instanceof NullExpression);
        boolean rhsNull = (rhs instanceof NullExpression);
        boolean lhsShort = TreeInfo.testShort(lhs);
        boolean rhsShort = TreeInfo.testShort(rhs);
        if (lhsShort || rhsShort) {
            shortVal = (int) ((IntExpression) (lhsShort ? lhs : rhs)).value;
            visitExpression(lhsShort ? rhs : lhs);
        }
        switch (expression.tag) {
            case EQ:
                if (lhsNull || rhsNull) {
                    visitExpression(lhsNull ? rhs : lhs);
                    resultState = (invertCond ? new Ifnull() : new Ifnonnull());
                    resultStackAdjustment = -1;
                    break;
                } else if (lhsShort || rhsShort) {
                    resultState = (shortVal == 0) ?
                            (invertCond ? new Ifeq(0) : new Ifne(0)) :
                            (invertCond ? new Ifeq(shortVal) : new Ifne(shortVal));
                    resultStackAdjustment = -1;
                } else {
                    visitExpression(lhs);
                    visitExpression(rhs);
                    resultState = (invertCond ? new Ifcmpeq() : new Ifcmpne());
                    resultStackAdjustment = -2;
                }
                break;
            case NEQ:
                if (lhsNull || rhsNull) {
                    visitExpression(lhsNull ? rhs : lhs);
                    resultState = (invertCond ? new Ifnonnull() : new Ifnull());
                    resultStackAdjustment = -1;
                    break;
                } else if (lhsShort || rhsShort) {
                    resultState = (shortVal == 0) ?
                            (invertCond ? new Ifne(0) : new Ifeq(0)) :
                            (invertCond ? new Ifne(shortVal) : new Ifeq(shortVal));
                    resultStackAdjustment = -1;
                } else {
                    visitExpression(lhs);
                    visitExpression(rhs);
                    resultState = (invertCond ? new Ifcmpne() : new Ifcmpeq());
                    resultStackAdjustment = -2;
                }
                break;
            case LT:
                if (lhsShort || rhsShort) {
                    resultState = lhsShort ?
                            (invertCond ? new Ifgt(shortVal) : new Iflt(shortVal)) :
                            (invertCond ? new Iflt(shortVal) : new Ifge(shortVal));
                    resultStackAdjustment = -1;
                } else {
                    visitExpression(lhs);
                    visitExpression(rhs);
                    resultState = (invertCond ? new Ifcmplt() : new Ifcmpge());
                    resultStackAdjustment = -2;
                }
                break;
            case LE:
                if (lhsShort || rhsShort) {
                    resultState = lhsShort ?
                            (invertCond ? new Ifge(shortVal) : new Ifle(shortVal)) :
                            (invertCond ? new Ifle(shortVal) : new Ifgt(shortVal));
                    resultStackAdjustment = -1;
                } else {
                    visitExpression(lhs);
                    visitExpression(rhs);
                    resultState = (invertCond ? new Ifcmple() : new Ifcmpgt());
                    resultStackAdjustment = -2;
                }
                break;
            case GT:
                if (lhsShort || rhsShort) {
                    resultState = lhsShort ?
                            (invertCond ? new Iflt(shortVal) : new Ifgt(shortVal)) :
                            (invertCond ? new Ifgt(shortVal) : new Ifle(shortVal));
                    resultStackAdjustment = -1;
                } else {
                    visitExpression(lhs);
                    visitExpression(rhs);
                    resultState = (invertCond ? new Ifcmpgt() : new Ifcmple());
                    resultStackAdjustment = -2;
                }
                break;
            case GE:
                if (lhsShort || rhsShort) {
                    resultState = lhsShort ?
                            (invertCond ? new Ifle(shortVal) : new Ifge(shortVal)) :
                            (invertCond ? new Ifge(shortVal) : new Iflt(shortVal));
                    resultStackAdjustment = -1;
                } else {
                    visitExpression(lhs);
                    visitExpression(rhs);
                    resultState = (invertCond ? new Ifcmpge() : new Ifcmplt());
                    resultStackAdjustment = -2;
                }
                break;
            default: throw new AssertionError();
        }
        code.addChainedState(TreeInfo.line(expression),
                resultState,
                peekConditionChain(),
                resultStackAdjustment);
        endCondition();
    }

    @Override
    public void visitMultiply(MultiplyExpression expression) {
        visitBinary(expression);
        emitMul(TreeInfo.line(expression));
    }

    @Override
    public void visitNegative(NegativeExpression expression) {
        visitUnary(expression);
        code.addState(TreeInfo.line(expression), Neg.INSTANCE);
    }

    @Override
    public void visitNotEqual(NotEqualExpression expression) {
//        beginCondition();
//        Expression lhs = expression.lhs;
//        Expression rhs = expression.rhs;
//        int line = TreeInfo.line(expression);
//        if (lhs instanceof NullExpression) {
//            visitExpression(rhs);
//            code.addChainedState(line,
//                    invertCond ? new Ifnonnull() : new Ifnull(),
//                    peekConditionChain(), -1);
//        } else if (rhs instanceof NullExpression) {
//            visitExpression(lhs);
//            code.addChainedState(line,
//                    (invertCond ? new Ifnonnull() : new Ifnull()),
//                    peekConditionChain(), -1);
//        } else if (TreeInfo.resolveShort(lhs) >= 0) {
//            visitExpression(rhs);
//            int shortVal = TreeInfo.resolveShort(lhs);
//            code.addChainedState(line,
//                    (invertCond ? new Ifne(shortVal) : new Ifeq(shortVal)),
//                    peekConditionChain(), -1);
//        } else if (TreeInfo.resolveShort(rhs) >= 0) {
//            visitExpression(rhs);
//            int shortVal = TreeInfo.resolveShort(lhs);
//            code.addChainedState(line,
//                    (invertCond ? new Ifne(shortVal) : new Ifeq(shortVal)),
//                    peekConditionChain(), -1);
//        } else {
//            visitExpression(lhs);
//            visitExpression(rhs);
//            code.addChainedState(line,
//                    (invertCond ? new Ifcmpne() : new Ifcmpeq()),
//                    peekConditionChain(), -2);
//        }
//        endCondition();

        generateComparison(expression);
    }

    @Override
    public void visitNot(NotExpression expression) {
        beginCondition();
        if (invertCond) {
            invertCond = false;
            generateCondition(expression.hs);
            invertCond = true;
        } else {
            invertCond = true;
            generateCondition(expression.hs);
            invertCond = false;
        }
        endCondition();
    }

    @Override
    public void visitNullCoalesce(NullCoalesceExpression expression) {
        // todo: Это очевидно неполноценная реализация.
        visitExpression(expression.lhs);
        emitDup();
        int el = code.makeChain();
        code.addChainedState(new Ifnonnull(), el, -1);
        code.addState(Pop.INSTANCE, -1);
        visitExpression(expression.rhs);
        code.resolveChain(el);
    }

    @Override
    public void visitNull(NullExpression expression) {
        code.addState(PushNull.INSTANCE, 1);
    }

    @Override
    public void visitOr(OrExpression expression) {
        beginCondition();
        if (invertCond) {
            generateCondition(expression.lhs);
            generateCondition(expression.rhs);
        } else {
            if (expression.rhs == null) {
                generateCondition(expression.lhs);
            } else {
                int tr = pushMakeConditionChain();
                invertCond = true;
                generateCondition(expression.lhs);
                popConditionChain();
                invertCond = false;
                generateCondition(expression.rhs);
                code.resolveChain(tr);
            }
        }
        endCondition();
    }

    @Override
    public void visitParens(ParensExpression expression) {
        throw new AssertionError(
                "all brackets should have been removed in ConstantFolder");
    }

    @Override
    public void visitPositive(PositiveExpression expression) {
        visitUnary(expression);
        code.addState(TreeInfo.line(expression), Pos.INSTANCE);
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

    @Override
    public void visitPrintln(PrintlnStatement statement) {
        visitList(statement.expressions);
        int count = statement.expressions.size();
        code.addState(TreeInfo.line(statement), new Println(count), -count);
    }

    @Override
    public void visitPrint(PrintStatement statement) {
        visitList(statement.expressions);
        int count = statement.expressions.size();
        code.addState(TreeInfo.line(statement), new Print(count), -count);
    }

    @Override
    public void visitRemainder(RemainderExpression expression) {
        visitBinary(expression);
        emitRem(TreeInfo.line(expression));
    }

    @Override
    public void visitReturn(ReturnStatement statement) {
        if (isNull(statement.expr)) {
            emitRetnull(TreeInfo.line(statement));
        } else {
            visitExpression(statement.expr);
            emitReturn(TreeInfo.line(statement));
        }
    }

    private void emitRetnull(int line) {
        code.addState(line, Retnull.INSTANCE);
        code.dead();
    }

    private static boolean isNull(Expression expression) {
        Expression expr = TreeInfo.removeParens(expression);
        return expr == null || expr instanceof NullExpression;
    }

    @Override
    public void visitRightShift(ShiftRightExpression expression) {
        visitBinary(expression);
        emitRhs(TreeInfo.line(expression));
    }

    @Override
    public void visitString(StringExpression expression) {
        emitPushString(TreeInfo.line(expression), expression.value);
    }

    @Override
    public void visitSubtract(SubtractExpression expression) {
        visitBinary(expression);
        emitSub(TreeInfo.line(expression));
    }

    @Override
    public void visitSwitch(SwitchStatement statement) {
        int count = 0;
        for (CaseStatement _case : statement.cases) {
            if (_case.expressions != null) count++;
        }
        visitExpression(statement.selector);
        Part[] parts = new Part[count];
        Switch _switch = new Switch(parts);
        int a = code.makeChain();
        breakChains.push(a);
        code.addChainedState(TreeInfo.line(statement), _switch, a);
        fallthroughChains.push(code.makeChain());
        switchPartsStack.add(new ArrayList<>(count));
        for (CaseStatement _case : statement.cases) {
            if (_case.expressions == null) {
                _switch.setDefault(new Part(code.currentBci(), null));
                emitCaseBody(_case.body);
            } else {
                visitStatement(_case);
            }
        }
        List<Part> parts0 = switchPartsStack.pop();
        for (int i = 0; i < parts0.size(); i++) {
            parts[i] = parts0.get(i);
        }
        code.resolveChain(fallthroughChains.popInt());
        code.resolveChain(breakChains.popInt());
    }

    @Override
    public void visitTernary(TernaryExpression expression) {
        int el = pushMakeConditionChain();
        int ex = code.makeChain();
        if (invertCond) {
            invertCond = false;
            generateCondition(expression.cond);
            invertCond = true;
        } else {
            generateCondition(expression.cond);
        }
        popConditionChain();
        visitExpression(expression.lhs);
        emitGoto(0, ex);
        code.resolveChain(el);
        visitExpression(expression.rhs);
        code.resolveChain(ex);
    }

    @Override
    public void visitTrue(TrueExpression expression) {
        emitPushTrue(TreeInfo.line(expression));
    }

    @Override
    public void visitVariable(VariableExpression expression) {
        String name = expression.name;
        if (codeData.testConstant(name)) {
            code.addState(new Getconst(name), 1);
        } else {
            emitVLoad(TreeInfo.line(expression), expression.name);
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
        
        boolean prevLoopInfinity = loopInfinity;
        loopInfinity = false;
        
        if (initials != null) {
            initials.forEach(this::visitStatement);
        }
        if (testFirst && condition != null) {
            emitGoto(TreeInfo.line(loop), cdc);
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
            emitGoto(0, bgc);
        } else {
            conditionalChains.push(bgc);
            invertCond = true;
            generateCondition(condition);
            invertCond = false;
            popConditionChain();
        }
        code.resolveChain(exc);
        if (loopInfinity) code.dead();
        loopInfinity = prevLoopInfinity;
    }

    private void visitExpression(Expression expression) {
        enableUsed();
        visitStatement(expression);
        disableUsed();
    }

    private void visitBinary(BinaryExpression expression) {
        enableUsed();
        visitStatement(expression.lhs);
        visitStatement(expression.rhs);
        disableUsed();
    }

    private void visitUnary(UnaryExpression expression) {
        enableUsed();
        visitStatement(expression.hs);
        disableUsed();
    }

    private void visitList(List<? extends Expression> expressions) {
        enableUsed();
        expressions.forEach(this::visitStatement);
        disableUsed();
    }

    private void generateCondition(Expression expression) {
        if (expression == null) {
            return;
        }
        conditionDepth++;
        visitExpression(expression);
        conditionDepth--;
        if (expression.isCondition()) {
            return;
        }
        // todo: Здешний код отвратителен. Следует переписать всё с нуля...
        code.addState(Bool.INSTANCE);
        code.addChainedState(TreeInfo.line(expression),
                invertCond ? new Ifne(0) : new Ifeq(0),
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
                code.addState(Pop.INSTANCE, -1);
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
                visitExpression(arrayAccess.hs);
                visitExpression(arrayAccess.key);
                if (expression.isTag(Tag.ASG_NULLCOALESCE)) {
                    int el = code.makeChain();
                    int ex = code.makeChain();
                    emitDup2();
                    emitALoad(TreeInfo.line(arrayAccess));
                    code.addChainedState(new Ifnonnull(), el, -1);
                    visitExpression(rhs);
                    if (isUsed()) {
                        emitDupX2();
                    }
                    emitAStore(TreeInfo.line(arrayAccess));
                    emitGoto(0, ex);
                    code.resolveChain(el);
                    if (isUsed()) {
                        emitALoad(TreeInfo.line(arrayAccess));
                    } else {
                        code.addState(Pop2.INSTANCE, -2);
                    }
                    code.resolveChain(ex);
                } else {
                    if (!expression.isTag(Tag.ASG)) {
                        emitDup2();
                        emitALoad(TreeInfo.line(arrayAccess));
                        visitExpression(rhs);
                        code.addState(TreeInfo.line(expression), asg2state(expression.tag), -1);
                    } else {
                        visitExpression(rhs);
                    }
                    if (isUsed()) {
                        emitDupX2();
                    }
                    emitAStore(TreeInfo.line(arrayAccess));
                }
                break;
            }
            case VARIABLE: {
                VariableExpression variable = (VariableExpression) lhs;
                if (expression.isTag(Tag.ASG_NULLCOALESCE)) {
                    int ex = code.makeChain();
                    visitExpression(lhs);
                    code.addChainedState(new Ifnonnull(), ex, -1);
                    visitExpression(rhs);
                    if (isUsed()) {
                        emitDup();
                    }
                    emitVStore(TreeInfo.line(expression), variable.name);
                    if (isUsed()) {
                        int el = code.makeChain();
                        emitGoto(0, el);
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
                        code.addState(TreeInfo.line(expression), asg2state(expression.tag), -1);
                    } else {
                        visitExpression(rhs);
                    }
                    if (isUsed()) {
                        emitDup();
                    }
                    emitVStore(TreeInfo.line(expression), variable.name);
                }
                break;
            }
            default: cError(lhs.position, "assignable expression expected.");
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
        return expressionDepth > 0;
    }
    private void enableUsed() {
        expressionDepth++;
    }
    private void disableUsed() {
        expressionDepth--;
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
//            code.addState(line, isIncrement
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
//            code.addState(line, isIncrement
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
                visitExpression(arrayAccess.hs);
                visitExpression(arrayAccess.key);
                emitDup2();
                emitALoad(TreeInfo.line(arrayAccess));
                if (isUsed() && (expression.isTag(Tag.POST_INC) || expression.isTag(Tag.POST_DEC))) {
                    emitDupX2();
                }
                code.addState(TreeInfo.line(expression), increase2state(expression.tag, -1));
                if (isUsed() && (expression.isTag(Tag.PRE_INC) || expression.isTag(Tag.PRE_DEC))) {
                    emitDupX2();
                }
                emitAStore(TreeInfo.line(arrayAccess));
                break;
            }
            case VARIABLE: {
                VariableExpression variable = (VariableExpression) hs;
                if (isUsed() && (expression.isTag(Tag.POST_INC) || expression.isTag(Tag.POST_DEC))) {
                    emitVLoad(TreeInfo.line(variable), variable.name);
                }
                code.addState(TreeInfo.line(expression),
                        increase2state(expression.tag, code.resolveLocal(variable.name)));
                if (isUsed() && (expression.isTag(Tag.PRE_INC) || expression.isTag(Tag.PRE_DEC))) {
                    emitVLoad(TreeInfo.line(variable), variable.name);
                }
                break;
            }
            default: cError(hs.position, "assignable expression expected.");
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
        statementDepth++;
        statement.accept(this);
        statementDepth--;
    }

    private void beginCondition() {
        if (conditionDepth != 0) {
            return;
        }
        pushMakeConditionChain();
    }

    private void endCondition() {
        if (conditionDepth != 0) {
            return;
        }
        int ex = code.makeChain();
        emitPushTrue(0);
        emitGoto(0, ex);
        code.resolveChain(popConditionChain());
        emitPushFalse(0);
        code.resolveChain(ex);
    }

    private void emitPushLong(int line, long value) {
        if (isShort(value)) {
            code.addState(line, new Push(Operand.Type.LONG, (short) value), 1);
        } else {
            code.addState(line, new Ldc(code.resolveConstant(value)), 1);
        }
    }

    private void emitPushDouble(int line, double value) {
        long lv = (long) value;
        if (lv == value && isShort(lv)) {
            code.addState(line, new Push(Operand.Type.LONG, (short) lv), 1);
        } else {
            code.addState(line, new Ldc(code.resolveConstant(value)), 1);
        }
    }

    private void emitPushString(int line, String value) {
        code.addState(line, new Ldc(code.resolveConstant(value)), 1);
    }

    private static boolean isShort(long value) {
        return value >= Short.MIN_VALUE && value <= Short.MAX_VALUE;
    }

    // emit methods

    private void emitPushTrue(int line) { code.addState(line, Push.PUSH_TRUE, 1); }
    private void emitPushFalse(int line) { code.addState(line, Push.PUSH_FALSE, 1); }
    private void emitGoto(int line, int chainId) { code.addChainedState(line, new Goto(), chainId); }
    private void emitDup() { code.addState(Dup.INSTANCE, 1); }
    private void emitDupX1() { code.addState(Dup_x1.INSTANCE, 1); }
    private void emitDupX2() { code.addState(Dup_x2.INSTANCE, 1); }
    private void emitDup2() { code.addState(Dup2.INSTANCE, 2); }
    private void emitDup2X1() { code.addState(Dup2_x1.INSTANCE, 2); }
    private void emitDup2X2() { code.addState(Dup2_x2.INSTANCE, 2); }
    private void emitAdd(int line) { code.addState(line, Add.INSTANCE, -1); }
    private void emitAnd(int line) { code.addState(line, And.INSTANCE, -1); }
    private void emitOr(int line) { code.addState(line, Or.INSTANCE, -1); }
    private void emitXor(int line) { code.addState(line, Xor.INSTANCE, -1); }
    private void emitDiv(int line) { code.addState(line, Div.INSTANCE, -1); }
    private void emitLhs(int line) { code.addState(line, Shl.INSTANCE, -1); }
    private void emitMul(int line) { code.addState(line, Mul.INSTANCE, -1); }
    private void emitRem(int line) { code.addState(line, Rem.INSTANCE, -1); }
    private void emitRhs(int line) { code.addState(line, Shr.INSTANCE, -1); }
    private void emitSub(int line) { code.addState(line, Sub.INSTANCE, -1); }
    private void emitALoad(int line) { code.addState(line, Aload.INSTANCE, -1); }
    private void emitVLoad(int line, String name) { code.addState(line, new Vload(code.resolveLocal(name)), 1); }
    private void emitAStore(int line) { code.addState(line, Astore.INSTANCE, -3); }
    private void emitVStore(int line, String name) { code.addState(line, new Vstore(code.resolveLocal(name)), -1); }
    private void emitCaseBody(Statement body) {
        code.resolveChain(fallthroughChains.popInt());
        fallthroughChains.push(code.makeChain());
        boolean prevLoopInfinity = loopInfinity;
        loopInfinity = false;
        visitStatement(body);
        if (loopInfinity) emitGoto(TreeInfo.line(body), breakChains.topInt());
        loopInfinity = prevLoopInfinity;
    }
    private void emitReturn(int line) {
        code.addState(line, Ret.INSTANCE, -1);
        code.dead();
    }

    private void cError(Position position, String message) {
        throw new CompileError(message, position);
    }
}