package jua.compiler;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import jua.interpreter.lang.*;
import jua.interpreter.states.*;
import jua.parser.Tree.*;

import java.util.*;

import static jua.interpreter.states.Switch.Part;

public class Gen implements Visitor {

    @FunctionalInterface
    private interface AssignmentState {

        void insert(int line);
    }

    private static class Loop {

        private boolean infinity;

        void setInfinity(boolean infinity) {
            this.infinity = infinity;
        }

        boolean isInfinity() {
            return infinity;
        }
    }

    private final BuiltIn builtIn;

    private final Code code;

    private final IntList breakChains;

    private final IntList continueChains;

    private final IntList fallthroughChains;

    private final Deque<List<Part>> switchPartsStack;

    private final IntList conditionalChains;

    private final Deque<Loop> loops;

    // todo: Избавиться от ниже определенных полей

    private int statementDepth = 0;

    private int expressionDepth = 0;

    private int conditionDepth = 0;

    private boolean conditionInvert = false;

    public Gen(BuiltIn builtIn) {
        this.builtIn = builtIn;
        code = new Code();
        breakChains = new IntArrayList();
        continueChains = new IntArrayList();
        fallthroughChains = new IntArrayList();
        switchPartsStack = new ArrayDeque<>();
        conditionalChains = new IntArrayList();
        loops = new ArrayDeque<>();
    }

    // todo: исправить этот low-cohesion
    public Result getResult() {
        return new Result(builtIn, code.getBuilder());
    }

    @Override
    public void visitAdd(AddExpression expression) {
        visitBinary(expression);
        insertAdd(line(expression));

    }

    @Override
    public void visitAnd(AndExpression expression) {
        beginCondition();
        if (conditionInvert) {
            if (expression.rhs == null) {
                visitCondition(expression.lhs);
            } else {
                int fa = pushNewFlow();
                conditionInvert = false;
                visitCondition(expression.lhs);
                popFlow();
                conditionInvert = true;
                visitCondition(expression.rhs);
                code.resolveFlow(fa);
            }
        } else {
            visitCondition(expression.lhs);
            visitCondition(expression.rhs);
        }
        endCondition(line(expression));
    }

    private int pushNewFlow() {
        int newFlow = code.createFlow();
        conditionalChains.add(newFlow);
        return newFlow;
    }

    private int popFlow() {
        return conditionalChains.removeInt(conditionalChains.size() - 1);
    }

    @Override
    public void visitArrayAccess(ArrayAccessExpression expression) {
        checkAccessible(expression.hs);
        visitExpression(expression.hs);
        visitExpression(expression.key);
        insertALoad(line(expression));
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
//                insertPush(index.longValue(), IntOperand::valueOf);
//            } else {
//                line = key.getPosition().line;
//                visitStatement(key);
//            }
//            visitStatement(value);
//            insertAStore(line);
//            index.incrementAndGet();
//        });
//        disableUsed();
//        insertNecessaryPop();

        insertNewArray(line(expression));
        compileArrayCreation(expression.map);
    }

    private void compileArrayCreation(Map<Expression, Expression> entries) {
        long implicitIndex = 0;
        Iterator<Map.Entry<Expression, Expression>> iterator
                = entries.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Expression, Expression> entry = iterator.next();
            insertDup();
            if (entry.getKey().isEmpty()) {
                insertPushLong(line(entry.getValue()), implicitIndex++);
            } else {
                visitExpression(entry.getKey());
            }
            visitExpression(entry.getValue());
            insertAStore(entry.getKey().isEmpty()
                    ? line(entry.getValue())
                    : line(entry.getKey()));
        }
    }

    private void insertNewArray(int line) {
        code.incStack();
        code.addState(line, Newarray.INSTANCE);
    }

    @Override
    public void visitAssignAdd(AssignAddExpression expression) {
        visitAssignment(expression, line -> {
            visitExpression(expression.expr);
            insertAdd(line);
        });
    }

    @Override
    public void visitAssignBitAnd(AssignBitAndExpression expression) {
        visitAssignment(expression, line -> {
            visitExpression(expression.expr);
            insertAnd(line);
        });
    }

    @Override
    public void visitAssignBitOr(AssignBitOrExpression expression) {
        visitAssignment(expression, line -> {
            visitExpression(expression.expr);
            insertOr(line);
        });
    }

    @Override
    public void visitAssignBitXor(AssignBitXorExpression expression) {
        visitAssignment(expression, line -> {
            visitExpression(expression.expr);
            insertXor(line);
        });
    }

    @Override
    public void visitAssignDivide(AssignDivideExpression expression) {
        visitAssignment(expression, line -> {
            visitExpression(expression.expr);
            insertDiv(line);
        });
    }

    @Override
    public void visitAssignLeftShift(AssignShiftLeftExpression expression) {
        visitAssignment(expression, line -> {
            visitExpression(expression.expr);
            insertLhs(line);
        });
    }

    @Override
    public void visitAssign(AssignExpression expression) {
        visitAssignment(expression, null);
    }

    @Override
    public void visitAssignMultiply(AssignMultiplyExpression expression) {
        visitAssignment(expression, line -> {
            visitExpression(expression.expr);
            insertMul(line);
        });
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

        visitAssignment(expression, null);
    }

    @Override
    public void visitAssignRemainder(AssignRemainderExpression expression) {
        visitAssignment(expression, line -> {
            visitExpression(expression.expr);
            insertRem(line);
        });
    }

    @Override
    public void visitAssignRightShift(AssignShiftRightExpression expression) {
        visitAssignment(expression, line -> {
            visitExpression(expression.expr);
            insertRhs(line);
        });
    }

    @Override
    public void visitAssignSubtract(AssignSubtractExpression expression) {
        visitAssignment(expression, line -> {
            visitExpression(expression.expr);
            insertSub(line);
        });
    }

    @Override
    public void visitBitAnd(BitAndExpression expression) {
        visitBinary(expression);
        insertAdd(line(expression));

    }

    @Override
    public void visitBitNot(BitNotExpression expression) {
        visitUnary(expression);
        code.addState(line(expression), Not.INSTANCE);

    }

    @Override
    public void visitBitOr(BitOrExpression expression) {
        visitBinary(expression);
        insertOr(line(expression));

    }

    @Override
    public void visitBitXor(BitXorExpression expression) {
        visitBinary(expression);
        insertXor(line(expression));

    }

    @Override
    public void visitBlock(BlockStatement statement) {
        boolean root = false;
        if (code.empty()) {
            code.enterContext(statement.getPosition().filename);
            code.enterScope();
            root = true;
        }
        for (Statement childStatement : statement.statements) {
            if (!code.scopeAlive()) {
                break;
            }
            visitStatement(childStatement);
        }

        if (root) {
            code.addState(Halt.INSTANCE);
        }
    }

    @Override
    public void visitBreak(BreakStatement statement) {
        if (breakChains.isEmpty()) {
            cError(statement.getPosition(), "'break' is not allowed outside of loop/switch.");
            return;
        }
        insertGoto(line(statement), peekInt(breakChains));
        code.deathScope();
        loops.getLast().setInfinity(false);
    }

    private static int peekInt(IntList integers) {
        return integers.getInt(integers.size() - 1);
    }

    @Override
    public void visitCase(CaseStatement statement) {
        switchPartsStack.getLast().add(new Part(code.statesCount(), statement.expressions.stream()
                .mapToInt(a -> {
                    assert a instanceof LiteralExpression;
                    return TreeInfo.resolveLiteral(code, (LiteralExpression) a);
                })
                .toArray()));
        insertCaseBody(statement.body);
    }

    @Override
    public void visitClone(CloneExpression expression) {
        checkCloneable(expression.hs);
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
            if (builtIn.testConstant(name)) {
                cError(statement.getPosition(), "constant '" + name + "' already declared");
            }
            Expression expr = statement.expressions.get(i);
            Operand value;
            if (expr instanceof ArrayExpression) {
                value = new ArrayOperand();
                code.incStack();
                code.addState(new Getconst(name));
                compileArrayCreation(((ArrayExpression) expr).map);
            } else {
                assert expr instanceof LiteralExpression;
                value = TreeInfo.resolveLiteral((LiteralExpression) expr);
            }
            builtIn.setConstant(name, new Constant(
                    value, false));
        }
    }

    @Override
    public void visitContinue(ContinueStatement statement) {
        if (continueChains.isEmpty()) {
            cError(statement.getPosition(), "'continue' is not allowed outside of loop.");
            return;
        }
        insertGoto(line(statement), peekInt(continueChains));
        code.deathScope();
    }

    @Override
    public void visitDivide(DivideExpression expression) {
        visitBinary(expression);
        insertDiv(line(expression));

    }

    @Override
    public void visitDo(DoStatement statement) {
        compileLoop(null, statement.cond, null, statement.body, false);
    }

    @Override
    public void visitEqual(EqualExpression expression) {
        beginCondition();
        Expression rhs = expression.rhs.child();
        if (rhs instanceof NullExpression) {
            visitExpression(expression.lhs);
            code.addFlow(peekInt(conditionalChains), conditionInvert ? new Ifnull() : new Ifnonnull());
            code.decStack();
        } else if (expression.lhs instanceof IntExpression) {
            visitExpression(expression.rhs);
            code.addFlow(peekInt(conditionalChains), conditionInvert
                    ? new Ifeq(((IntExpression) expression.lhs).value)
                    : new Ifne(((IntExpression) expression.lhs).value));
            code.decStack();
        } else if (rhs instanceof IntExpression) {
            visitExpression(expression.lhs);
            code.addFlow(peekInt(conditionalChains), conditionInvert
                    ? new Ifeq(((IntExpression) rhs).value)
                    : new Ifne(((IntExpression) rhs).value));
            code.decStack();
        } else {
            visitBinary(expression);
            code.addFlow(peekInt(conditionalChains), conditionInvert ? new Ifcmpeq() : new Ifcmpne());
            code.decStack(2);
        }
        endCondition(line(expression));
    }

    @Override
    public void visitFallthrough(FallthroughStatement statement) {
        if (fallthroughChains.isEmpty()) {
            cError(statement.getPosition(), "'fallthrough' is not allowed outside of switch.");
            return;
        }
        insertGoto(line(statement), peekInt(fallthroughChains));
        code.deathScope();
        loops.getLast().setInfinity(false); // for cases
    }

    @Override
    public void visitFalse(FalseExpression expression) {
        insertFalse(line(expression));
    }

    @Override
    public void visitFloat(FloatExpression expression) {
        insertPushDouble(line(expression), expression.value);
    }

    @Override
    public void visitFor(ForStatement statement) {
        compileLoop(statement.init, statement.cond, statement.step, statement.body, true);
    }

    @Override
    public void visitFunctionCall(FunctionCallExpression expression) {
        if (expression.args.isEmpty()) {
            code.incStack();
        } else {
            visitList(expression.args);
        }
        code.addState(line(expression), new Call(expression.name, expression.args.size()));
        code.decStack(expression.args.size() - 1);

    }

    @Override
    public void visitFunctionDefine(FunctionDefineStatement statement) {
        if (statementDepth > 1)
            cError(statement.getPosition(), "function declaration is not allowed here.");
        if (builtIn.testFunction(statement.name))
            cError(statement.getPosition(), "function '" + statement.name + "' already declared.");
        code.enterContext(statement.getPosition().filename);
        code.enterScope();
        int[] locals = statement.names.stream().mapToInt(n -> {
            if (statement.names.indexOf(n) != statement.names.lastIndexOf(n)) {
                cError(statement.getPosition(), "duplicate argument '" + n + "'.");
            }
            return code.getLocal(n);
        }).toArray();
        statement.body.accept(this);
        insertRetnull();
        builtIn.setFunction(statement.name, new ScriptFunction(
                statement.names.toArray(new String[0]),
                locals, // function arguments must be first at local list
                statement.optionals.stream().mapToInt(a -> {
                    assert a instanceof LiteralExpression;
                    return TreeInfo.resolveLiteral(code, (LiteralExpression) a);
                }).toArray(),
                code.getBuilder()));
        code.exitScope();
        code.exitContext();
    }

    @Override
    public void visitGreaterEqual(GreaterEqualExpression expression) {
        beginCondition();
        if (expression.lhs instanceof IntExpression) {
            visitExpression(expression.rhs);
            code.addFlow(peekInt(conditionalChains), conditionInvert
                    ? new Ifle(((IntExpression) expression.lhs).value)
                    : new Ifgt(((IntExpression) expression.lhs).value));
            code.decStack();
        } else if (expression.rhs instanceof IntExpression) {
            visitExpression(expression.lhs);
            code.addFlow(peekInt(conditionalChains), conditionInvert
                    ? new Ifge(((IntExpression) expression.rhs).value)
                    : new Iflt(((IntExpression) expression.rhs).value));
            code.decStack();
        } else {
            visitBinary(expression);
            code.addFlow(peekInt(conditionalChains), line(expression),
                    conditionInvert ? new Ifcmpge() : new Ifcmplt());
            code.decStack(2);
        }
        endCondition(line(expression));
    }

    @Override
    public void visitGreater(GreaterExpression expression) {
        beginCondition();
        if (expression.lhs instanceof IntExpression) {
            visitExpression(expression.rhs);
            code.addFlow(peekInt(conditionalChains), conditionInvert
                    ? new Ifge(((IntExpression) expression.lhs).value)
                    : new Iflt(((IntExpression) expression.lhs).value));
            code.decStack();
        } else if (expression.rhs instanceof IntExpression) {
            visitExpression(expression.lhs);
            code.addFlow(peekInt(conditionalChains), conditionInvert
                    ? new Ifle(((IntExpression) expression.rhs).value)
                    : new Ifgt(((IntExpression) expression.rhs).value));
            code.decStack();
        } else {
            visitBinary(expression);
            code.addFlow(peekInt(conditionalChains), line(expression),
                    conditionInvert ? new Ifcmpgt() : new Ifcmple());
            code.decStack(2);
        }
        endCondition(line(expression));
    }

    @Override
    public void visitIf(IfStatement statement) {
        if (statement.elseBody == null) {
            conditionalChains.add(code.createFlow());
            visitCondition(statement.cond);
            visitBody(statement.body);
            code.resolveFlow(popFlow());
        } else {
            int el = pushNewFlow();
            int ex = code.createFlow();
            visitCondition(statement.cond);
            boolean thenAlive = visitBody(statement.body);
            insertGoto(0, ex);
            code.resolveFlow(el);
            boolean elseAlive = visitBody(statement.elseBody);
            code.resolveFlow(ex);
            if (!thenAlive && !elseAlive) {
                code.deathScope();
            }
        }
    }

    @Override
    public void visitInt(IntExpression expression) {
        insertPushLong(line(expression), expression.value);
    }

    @Override
    public void visitLeftShift(ShiftLeftExpression expression) {
        visitBinary(expression);
        insertLhs(line(expression));

    }

    @Override
    public void visitLessEqual(LessEqualExpression expression) {
        beginCondition();
        if (expression.lhs instanceof IntExpression) {
            visitExpression(expression.rhs);
            code.addFlow(peekInt(conditionalChains), conditionInvert
                    ? new Ifge(((IntExpression) expression.lhs).value)
                    : new Iflt(((IntExpression) expression.lhs).value));
            code.decStack();
        } else if (expression.rhs instanceof IntExpression) {
            visitExpression(expression.lhs);
            code.addFlow(peekInt(conditionalChains), conditionInvert
                    ? new Ifle(((IntExpression) expression.rhs).value)
                    : new Ifgt(((IntExpression) expression.rhs).value));
            code.decStack();
        } else {
            visitBinary(expression);
            code.addFlow(peekInt(conditionalChains), line(expression),
                    conditionInvert ? new Ifcmple() : new Ifcmpgt());
            code.decStack(2);
        }
        endCondition(line(expression));
    }

    @Override
    public void visitLess(LessExpression expression) {
        beginCondition();
        if (expression.lhs instanceof IntExpression) {
            visitExpression(expression.rhs);
            code.addFlow(peekInt(conditionalChains), conditionInvert
                    ? new Ifgt(((IntExpression) expression.lhs).value)
                    : new Ifle(((IntExpression) expression.lhs).value));
            code.decStack();
        } else if (expression.rhs instanceof IntExpression) {
            visitExpression(expression.lhs);
            code.addFlow(peekInt(conditionalChains), conditionInvert
                    ? new Iflt(((IntExpression) expression.rhs).value)
                    : new Ifge(((IntExpression) expression.rhs).value));
            code.decStack();
        } else {
            visitBinary(expression);
            code.addFlow(peekInt(conditionalChains), line(expression),
                    conditionInvert ? new Ifcmplt() : new Ifcmpge());
            code.decStack(2);
        }
        endCondition(line(expression));
    }

    @Override
    public void visitMultiply(MultiplyExpression expression) {
        visitBinary(expression);
        insertMul(line(expression));

    }

    @Override
    public void visitNegative(NegativeExpression expression) {
        visitUnary(expression);
        code.addState(line(expression), Neg.INSTANCE);

    }

    @Override
    public void visitNotEqual(NotEqualExpression expression) {
        beginCondition();
        Expression rhs = expression.rhs.child();
        if (rhs instanceof NullExpression) {
            visitExpression(expression.lhs);
            code.addFlow(peekInt(conditionalChains), conditionInvert ? new Ifnonnull() : new Ifnull());
            code.decStack();
        } else if (expression.lhs instanceof IntExpression) {
            visitExpression(expression.rhs);
            code.addFlow(peekInt(conditionalChains), conditionInvert
                    ? new Ifne(((IntExpression) expression.lhs).value)
                    : new Ifeq(((IntExpression) expression.lhs).value));
            code.decStack();
        } else if (rhs instanceof IntExpression) {
            visitExpression(expression.lhs);
            code.addFlow(peekInt(conditionalChains), conditionInvert
                    ? new Ifne(((IntExpression) rhs).value)
                    : new Ifeq(((IntExpression) rhs).value));
            code.decStack();
        } else {
            visitBinary(expression);
            code.addFlow(peekInt(conditionalChains), conditionInvert ? new Ifcmpne() : new Ifcmpeq());
            code.decStack(2);
        }
        endCondition(line(expression));
    }

    @Override
    public void visitNot(NotExpression expression) {
        beginCondition();
        if (conditionInvert) {
            conditionInvert = false;
            visitCondition(expression.hs);
            conditionInvert = true;
        } else {
            conditionInvert = true;
            visitCondition(expression.hs);
            conditionInvert = false;
        }
        endCondition(line(expression));
    }

    @Override
    public void visitNullCoalesce(NullCoalesceExpression expression) {
        // todo: Это очевидно неполноценная реализация.
        visitExpression(expression.lhs);
        insertDup();
        int el = code.createFlow();
        code.addFlow(el, new Ifnonnull());
        code.decStack();
        code.addState(Pop.INSTANCE);
        code.decStack();
        visitExpression(expression.rhs);
        code.resolveFlow(el);

    }

    @Override
    public void visitNull(NullExpression expression) {
        code.incStack();
        code.addState(PushNull.INSTANCE);

    }

    @Override
    public void visitOr(OrExpression expression) {
        beginCondition();
        if (conditionInvert) {
            visitCondition(expression.lhs);
            visitCondition(expression.rhs);
        } else {
            if (expression.rhs == null) {
                visitCondition(expression.lhs);
            } else {
                int tr = pushNewFlow();
                conditionInvert = true;
                visitCondition(expression.lhs);
                popFlow();
                conditionInvert = false;
                visitCondition(expression.rhs);
                code.resolveFlow(tr);
            }
        }
        endCondition(line(expression));
    }

    @Override
    public void visitParens(ParensExpression expression) {
        throw new AssertionError(
                "all brackets should have been removed in ConstantFolder");
    }

    @Override
    public void visitPositive(PositiveExpression expression) {
        visitUnary(expression);
        code.addState(line(expression), Pos.INSTANCE);
    }

    @Override
    public void visitPostDecrement(PostDecrementExpression expression) {
        visitIncrease(expression, false, true);
    }

    @Override
    public void visitPostIncrement(PostIncrementExpression expression) {
        visitIncrease(expression, true, true);
    }

    @Override
    public void visitPreDecrement(PreDecrementExpression expression) {
        visitIncrease(expression, false, false);
    }

    @Override
    public void visitPreIncrement(PreIncrementExpression expression) {
        visitIncrease(expression, true, false);
    }

    @Override
    public void visitPrintln(PrintlnStatement statement) {
        visitList(statement.expressions);
        int count = statement.expressions.size();
        code.addState(line(statement), new Println(count));
        code.decStack(count);
    }

    @Override
    public void visitPrint(PrintStatement statement) {
        visitList(statement.expressions);
        int count = statement.expressions.size();
        code.addState(line(statement), new Print(count));
        code.decStack(count);
    }

    @Override
    public void visitRemainder(RemainderExpression expression) {
        visitBinary(expression);
        insertRem(line(expression));

    }

    @Override
    public void visitReturn(ReturnStatement statement) {
        if (isNull(statement.expr)) {
            insertRetnull();
        } else {
            visitExpression(statement.expr);
            insertReturn();
        }
    }

    private void insertRetnull() {
        code.addState(Retnull.INSTANCE);
        code.deathScope();
    }

    private static boolean isNull(Expression expression) {
        Expression expr = TreeInfo.removeParens(expression);
        return expr == null || expr instanceof NullExpression;
    }

    @Override
    public void visitRightShift(ShiftRightExpression expression) {
        visitBinary(expression);
        insertRhs(line(expression));

    }

    @Override
    public void visitString(StringExpression expression) {
        insertPushString(line(expression), expression.value);
    }

    @Override
    public void visitSubtract(SubtractExpression expression) {
        visitBinary(expression);
        insertSub(line(expression));

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
        int a = code.createFlow();
        breakChains.add(a);
        code.addFlow(a, _switch);
        fallthroughChains.add(code.createFlow());
        switchPartsStack.add(new ArrayList<>(count));
        for (CaseStatement _case : statement.cases) {
            if (_case.expressions == null) {
                _switch.setDefault(new Part(code.statesCount(), null));
                insertCaseBody(_case.body);
            } else {
                visitStatement(_case);
            }
        }
        List<Part> parts0 = switchPartsStack.pop();
        for (int i = 0; i < parts0.size(); i++) {
            parts[i] = parts0.get(i);
        }
        code.resolveFlow(popInt(fallthroughChains));
        code.resolveFlow(popInt(breakChains));
    }

    private static int popInt(IntList integers) {
        return integers.removeInt(integers.size() - 1);
    }

    @Override
    public void visitTernary(TernaryExpression expression) {
        int el = pushNewFlow();
        int ex = code.createFlow();
        if (conditionInvert) {
            conditionInvert = false;
            visitCondition(expression.cond);
            conditionInvert = true;
        } else {
            visitCondition(expression.cond);
        }
        popFlow();
        visitExpression(expression.lhs);
        insertGoto(0, ex);
        code.resolveFlow(el);
        visitExpression(expression.rhs);
        code.resolveFlow(ex);
    }

    @Override
    public void visitTrue(TrueExpression expression) {
        insertTrue(line(expression));
    }

    @Override
    public void visitVariable(VariableExpression expression) {
        String name = expression.name;
        if (builtIn.testConstant(name)) {
            code.addState(new Getconst(name));
            code.incStack();
        } else {
            insertVLoad(line(expression), expression.name);
        }
    }

    @Override
    public void visitWhile(WhileStatement statement) {
        compileLoop(null, statement.cond, null, statement.body, true);
    }

    private void compileLoop(List<Expression> initials, Expression condition, List<Expression> steps,
                             Statement body, boolean testFirst) {
        // cond pc
        int cond = code.createFlow();
        // begin pc
        int begin = code.createFlow();
        // exit pc
        int exit = code.createFlow();

        addLoop(condition == null);
        if (initials != null) {
            initials.forEach(this::visitStatement);
        }
        if (testFirst && condition != null) {
            insertGoto(0, cond);
        }
        code.resolveFlow(begin);
        breakChains.add(exit);
        continueChains.add(cond);
        visitBody(body);
        if (steps != null) {
            steps.forEach(this::visitStatement);
        }
        code.resolveFlow(cond);
        if (condition == null) {
            insertGoto(0, begin);
        } else {
            conditionalChains.add(begin);
            conditionInvert = true;
            visitCondition(condition);
            conditionInvert = false;
            popFlow();
        }
        code.resolveFlow(exit);
        checkInfinity();
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

    private void visitCondition(Expression expression) {
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
        code.addFlow(peekInt(conditionalChains), line(expression),
                conditionInvert ? new Ifeq(0) : new Ifne(0));
        code.decStack(1);
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
                code.addState(Pop.INSTANCE);
                code.decStack();
        }
    }

    private void visitAssignment(AssignmentExpression expression,
                                 @Deprecated AssignmentState state) {
        //        Expression var = expression.var.child();
//        checkAssignable(var);
//        int line = line(expression);
//        if (var instanceof ArrayAccessExpression) {
//            ArrayAccessExpression var0 = (ArrayAccessExpression) var;
//            visitExpression(var0.hs);
//            visitExpression(var0.key);
//            if (state != null) {
//                insertDup2(line);
//                insertALoad(line(var0.key));
//                state.insert(line);
//            } else {
//                visitExpression(expression.expr);
//            }
//            if (isUsed())
//                // Здесь используется var0.key потому что
//                // он может быть дальше, чем var0, а если бы он был ближе
//                // к началу файла, то это было бы некорректно для таблицы линий
//                insertDup_x2(line(var0.key));
//            insertAStore(line);
//        } else if (var instanceof VariableExpression) {
//            if (state != null) {
//                visitExpression(var);
//                state.insert(line);
//            } else {
//                visitExpression(expression.expr);
//                if (isUsed()) {
//                    insertDup(line(var));
//                }
//            }
//            insertVStore(line, ((VariableExpression) var).name);
//        }

        Expression lhs = expression.var;
        Expression rhs = expression.expr;

        switch (lhs.tag) {
            case ARRAY_ACCESS: {
                ArrayAccessExpression arrayAccess = (ArrayAccessExpression) lhs;
                visitExpression(arrayAccess.hs);
                visitExpression(arrayAccess.key);
                if (expression.isTag(Tag.ASG_NULLCOALESCE)) {
                    int el = code.createFlow();
                    int ex = code.createFlow();
                    insertDup2();
                    insertALoad(line(arrayAccess));
                    code.addFlow(el, new Ifnonnull());
                    code.decStack();
                    visitExpression(rhs);
                    if (isUsed()) {
                        insertDupX2();
                    }
                    insertAStore(line(arrayAccess));
                    insertGoto(0, ex);
                    code.resolveFlow(el);
                    if (isUsed()) {
                        insertALoad(line(arrayAccess));
                    } else {
                        code.addState(Pop2.INSTANCE);
                        code.decStack(2);
                    }
                    code.resolveFlow(ex);
                } else {
                    if (!expression.isTag(Tag.ASG)) {
                        insertDup2();
                        insertALoad(line(arrayAccess));
                        visitExpression(rhs);
                        code.addState(line(expression), asg2state(expression.tag));
                        code.decStack();
                    } else {
                        visitExpression(rhs);
                    }
                    if (isUsed()) {
                        insertDupX2();
                    }
                    insertAStore(line(arrayAccess));
                }
                break;
            }
            case VARIABLE: {
                VariableExpression variable = (VariableExpression) lhs;
                if (expression.isTag(Tag.ASG_NULLCOALESCE)) {
                    int ex = code.createFlow();
                    visitExpression(lhs);
                    code.addFlow(ex, new Ifnonnull());
                    code.decStack();
                    visitExpression(rhs);
                    if (isUsed()) {
                        insertDup();
                    }
                    insertVStore(line(expression), variable.name);
                    if (isUsed()) {
                        int el = code.createFlow();
                        insertGoto(0, el);
                        code.resolveFlow(ex);
                        visitExpression(lhs);
                        code.resolveFlow(el);
                    } else {
                        code.resolveFlow(ex);
                    }
                } else {
                    if (!expression.isTag(Tag.ASG)) {
                        visitExpression(lhs);
                        visitExpression(rhs);
                        code.addState(line(expression), asg2state(expression.tag));
                        code.decStack();
                    } else {
                        visitExpression(rhs);
                    }
                    if (isUsed()) {
                        insertDup();
                    }
                    insertVStore(line(expression), variable.name);
                }
                break;
            }
            default: cError(lhs.position, "assignable expression expected.");
        }
    }

    public static State asg2state(Tag tag) {
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

    private void visitIncrease(IncreaseExpression expression,
                               @Deprecated boolean isIncrement,
                               @Deprecated boolean isPost) {
//        Expression hs = expression.hs.child();
//        checkAssignable(hs);
//        int line = line(expression);
//        if (hs instanceof ArrayAccessExpression) {
//            ArrayAccessExpression hs0 = (ArrayAccessExpression) hs;
//            visitExpression(hs0.hs);
//            visitExpression(hs0.key);
//            insertDup2(line(expression));
//            insertALoad(line(hs0.key));
//            if (isPost && (isUsed())) {
//                insertDupX2(line(hs0.key));
//            }
//            code.addState(line, isIncrement
//                    ? Inc.INSTANCE
//                    : Dec.INSTANCE);
//            if (!isPost && (isUsed())) {
//                insertDupX2(line(hs0.key));
//            }
//            insertAStore(line);
//        } else if (hs instanceof VariableExpression) {
//            String name = ((VariableExpression) hs).name;
//            if (isPost && (isUsed())) {
//                insertVLoad(line, name);
//            }
//            code.addState(line, isIncrement
//                    ? new Vinc(name, code.getLocal(name))
//                    : new Vinc(name, code.getLocal(name)));
//            if (!isPost && (isUsed())) {
//                insertVLoad(line, name);
//            }
//        }

        Expression hs = expression.hs;

        switch (hs.tag) {
            case ARRAY_ACCESS: {
                ArrayAccessExpression arrayAccess = (ArrayAccessExpression) hs;
                visitExpression(arrayAccess.hs);
                visitExpression(arrayAccess.key);
                insertDup2();
                insertALoad(line(arrayAccess));
                if (isUsed() && (expression.isTag(Tag.POST_INC) || expression.isTag(Tag.POST_DEC))) {
                    insertDupX2();
                }
                code.addState(line(expression), increase2state(expression.tag, -1));
                if (isUsed() && (expression.isTag(Tag.PRE_INC) || expression.isTag(Tag.PRE_DEC))) {
                    insertDupX2();
                }
                insertAStore(line(arrayAccess));
                break;
            }
            case VARIABLE: {
                VariableExpression variable = (VariableExpression) hs;
                if (isUsed() && (expression.isTag(Tag.POST_INC) || expression.isTag(Tag.POST_DEC))) {
                    insertVLoad(line(variable), variable.name);
                }
                code.addState(line(expression), increase2state(expression.tag, code.getLocal(variable.name)));
                if (isUsed() && (expression.isTag(Tag.PRE_INC) || expression.isTag(Tag.PRE_DEC))) {
                    insertVLoad(line(variable), variable.name);
                }
                break;
            }
            default: cError(hs.position, "assignable expression expected.");
        }
    }

    public static State increase2state(Tag tag, int id) {
        switch (tag) {
            case PRE_INC: case POST_INC:
                return id >= 0 ? new Vinc(id) : Inc.INSTANCE;
            case PRE_DEC: case POST_DEC:
                return id >= 0 ? new Vdec(id) : Dec.INSTANCE;
            default: throw new AssertionError();
        }
    }

    private boolean visitBody(Statement statement) {
        code.enterScope();
        visitStatement(statement);
        boolean alive = code.scopeAlive();
        code.exitScope();
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
        pushNewFlow();
    }

    private void endCondition(int line) {
        if (conditionDepth != 0) {
            return;
        }
        int ex = code.createFlow();
        insertTrue(line);
        insertGoto(0, ex);
        code.resolveFlow(popFlow());
        insertFalse(line);
        code.resolveFlow(ex);

    }

    private void insertPushLong(int line, long value) {
        code.incStack();
        // todo: ldc instruction
        code.addState(line, new Push(code.resolveLong(value)));
    }

    private void insertPushDouble(int line, double value) {
        code.incStack();
        // todo: ldc instruction
        code.addState(line, new Push(code.resolveDouble(value)));
    }

    private void insertPushString(int line, String value) {
        code.incStack();
        // todo: ldc instruction
        code.addState(line, new Push(code.resolveString(value)));
    }

    private static boolean isShort(long value) {
        return value >= Short.MIN_VALUE && value <= Short.MAX_VALUE;
    }

    private void insertTrue(int line) {
        code.incStack();
        code.addState(line, PushTrue.INSTANCE);
    }

    private void insertFalse(int line) {
        code.incStack();
        code.addState(line, PushFalse.INSTANCE);

    }

    private void insertGoto(int line, int flow) {
        code.addFlow(flow, line, new Goto());
    }

    private void insertDup() {
        code.incStack();
        code.addState(Dup.INSTANCE);
    }

    private void insertDupX1() {
        code.incStack();
        code.addState(Dup_x1.INSTANCE);
    }

    private void insertDupX2() {
        code.incStack();
        code.addState(Dup_x2.INSTANCE);
    }

    private void insertDup2() {
        code.incStack(2);
        code.addState(Dup2.INSTANCE);
    }

    private void insertDup2X1() {
        code.incStack(2);
        code.addState(Dup2_x1.INSTANCE);
    }

    private void insertDup2X2() {
        code.incStack(2);
        code.addState(Dup2_x2.INSTANCE);
    }

    private void insertAdd(int line) {
        code.addState(line, Add.INSTANCE);
        code.decStack();
    }

    private void insertAnd(int line) {
        code.addState(line, And.INSTANCE);
        code.decStack();
    }

    private void insertOr(int line) {
        code.addState(line, Or.INSTANCE);
        code.decStack();
    }

    private void insertXor(int line) {
        code.addState(line, Xor.INSTANCE);
        code.decStack();
    }

    private void insertDiv(int line) {
        code.addState(line, Div.INSTANCE);
        code.decStack();
    }

    private void insertLhs(int line) {
        code.addState(line, Shl.INSTANCE);
        code.decStack();
    }

    private void insertMul(int line) {
        code.addState(line, Mul.INSTANCE);
        code.decStack();
    }

    private void insertRem(int line) {
        code.addState(line, Rem.INSTANCE);
        code.decStack();
    }

    private void insertRhs(int line) {
        code.addState(line, Shr.INSTANCE);
        code.decStack();
    }

    private void insertSub(int line) {
        code.addState(line, Sub.INSTANCE);
        code.decStack();
    }

    private void insertALoad(int line) {
        code.addState(line, Aload.INSTANCE);
        code.decStack();
    }

    private void insertVLoad(int line, String name) {
        code.incStack();
        code.addState(line, new Vload(code.getLocal(name)));
    }

    private void insertAStore(int line) {
        code.addState(line, Astore.INSTANCE);
        code.decStack(3);
    }

    private void insertVStore(int line, String name) {
        code.addState(line, new Vstore(code.getLocal(name)));
        code.decStack();
    }

    private void insertCaseBody(Statement body) {
        code.resolveFlow(popInt(fallthroughChains));
        fallthroughChains.add(code.createFlow());
        addLoop(false);
        visitStatement(body);
        if (loops.pop().isInfinity()) insertGoto(line(body), peekInt(breakChains));
    }

    private void insertReturn() {
        code.addState(Return.INSTANCE);
        code.decStack();
        code.deathScope();
    }

    private void checkAssignable(Expression expr) {
        expr = expr.child();
        checkConstant(expr);
        if (!expr.isAssignable()) {
            cError(expr.getPosition(), "assignable expected.");
        } else if (expr instanceof ArrayAccessExpression) {
            checkAccessible(((ArrayAccessExpression) expr).hs);
        }
    }

    private void checkCloneable(Expression expr) {
//        expr = expr.child();
//        if (!expr.isCloneable()) {
//            cError(expr.getPosition(), "cloneable expected.");
//        }
    }

    private void checkAccessible(Expression expr) {
        switch (expr.tag) {
            case FUNC_CALL: case ARRAY_ACCESS:
            case VARIABLE: case PARENS:
                return;
        }
        cError(expr.position, "accessible expression expected.");
    }

    private void checkConstant(Expression expr) {
        expr = expr.child();
        if (!(expr instanceof VariableExpression)) {
            return;
        }
        if (builtIn.testConstant(((VariableExpression) expr).name)) {
            cError(expr.getPosition(), "assignment to constant is not allowed.");
        }
    }

    private void addLoop(boolean infinity) {
        Loop loop = new Loop();
        loop.setInfinity(infinity);
        loops.add(loop);
    }

    private void checkInfinity() {
        if (loops.pop().isInfinity()) code.deathScope();
    }

    private int line(Statement stmt) {
        return TreeInfo.line(stmt);
    }

    private void cError(Position position, String message) {
        throw new CompileError(message, position);
    }
}