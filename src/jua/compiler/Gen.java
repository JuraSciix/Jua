package jua.compiler;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import jua.interpreter.lang.*;
import jua.interpreter.states.*;
import jua.parser.tree.*;

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
        insertNecessaryPop();
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
        endCondition();
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
        visitUnary(expression);
        for (Expression key: expression.keys) {
            visitExpression(key);
            insertALoad(line(key));
        }
        insertNecessaryPop();
    }

    @Override
    public void visitArray(ArrayExpression expression) {
//        code.incStack();
//        code.addState(Newarray.INSTANCE);
//        AtomicInteger index = new AtomicInteger();
//        expressionDepth++;
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
//        expressionDepth--;
//        insertNecessaryPop();

        insertNewArray();
        long implicitIndex = 0;
        Iterator<Map.Entry<Expression, Expression>> iterator
                = expression.map.entrySet().iterator();
        while (iterator.hasNext()) {
            insertDup(0);
            Map.Entry<Expression, Expression> entry = iterator.next();
            if (entry.getKey().isEmpty()) {
                insertPush(implicitIndex++, IntOperand::valueOf);
            } else {
                visitExpression(entry.getKey());
            }
            visitExpression(entry.getValue());
            insertAStore(entry.getKey().isEmpty()
                    ? line(entry.getValue())
                    : line(entry.getKey()));
        }
        insertNecessaryPop();
    }

    private void insertNewArray() {
        code.incStack();
        code.addState(Newarray.INSTANCE);
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
        int el = code.createFlow();
        int ex = code.createFlow();
        boolean isArray = (expression.var.child() instanceof ArrayAccessExpression);
        visitAssignment(expression, line -> {
            code.addFlow(el, new Ifnonnull());
            code.decStack();
            visitExpression(expression.expr);
        });
        insertGoto(ex);
        code.resolveFlow(el);
        if (isArray) {
            insertALoad(line(expression));
        } else {
            visitExpression(expression.var);
        }
        code.resolveFlow(ex);
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
        insertNecessaryPop();
    }

    @Override
    public void visitBitNot(BitNotExpression expression) {
        visitUnary(expression);
        code.addState(line(expression), Not.NOT);
        insertNecessaryPop();
    }

    @Override
    public void visitBitOr(BitOrExpression expression) {
        visitBinary(expression);
        insertOr(line(expression));
        insertNecessaryPop();
    }

    @Override
    public void visitBitXor(BitXorExpression expression) {
        visitBinary(expression);
        insertXor(line(expression));
        insertNecessaryPop();
    }

    @Override
    public void visitBlock(BlockStatement statement) {
        boolean root = false;
        if (code.empty()) {
            code.enterContext(statement.getPosition().filename);
            code.enterScope();
            root = true;
        }
        statement.statements.forEach(this::visitStatement);

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
        insertGoto(peekInt(breakChains));
        code.deathScope();
        loops.getLast().setInfinity(false);
    }
    
    private static int peekInt(IntList integers) {
        return integers.getInt(integers.size() - 1);
    }

    @Override
    public void visitCase(CaseStatement statement) {
        ExpressionToOperand e2ot = new ExpressionToOperand(code, true);
        switchPartsStack.peek().add(new Part(code.statesCount(), statement.expressions.stream()
                .map(e2ot::apply)
                .toArray(Operand[]::new)));
        insertCaseBody(statement.body);
    }

    @Override
    public void visitClone(CloneExpression expression) {
        checkCloneable(expression.hs);
        visitUnary(expression);
        code.addState(Clone.INSTANCE);
        insertNecessaryPop();
    }

    @Override
    public void visitConstantDeclare(ConstantDeclareStatement statement) {
        if (statementDepth > 1) {
            cError(statement.getPosition(), "constants declaration is not allowed here.");
        }
        ExpressionToOperand e2of = new ExpressionToOperand(code, false);
        for (int i = 0; i < statement.names.size(); i++) {
            String name = statement.names.get(i);
            if (builtIn.testConstant(name)) {
                cError(statement.getPosition(), "constant '" + name + "' already declared");
            }
            Expression expr = statement.expressions.get(i);
            builtIn.setConstant(name, new Constant(e2of.apply(expr), false));
        }
    }

    @Override
    public void visitContinue(ContinueStatement statement) {
        if (continueChains.isEmpty()) {
            cError(statement.getPosition(), "'continue' is not allowed outside of loop.");
            return;
        }
        insertGoto(peekInt(continueChains));
        code.deathScope();
    }

    @Override
    public void visitDivide(DivideExpression expression) {
        visitBinary(expression);
        insertDiv(line(expression));
        insertNecessaryPop();
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
        endCondition();
    }

    @Override
    public void visitFallthrough(FallthroughStatement statement) {
        if (fallthroughChains.isEmpty()) {
            cError(statement.getPosition(), "'fallthrough' is not allowed outside of switch.");
            return;
        }
        insertGoto(peekInt(fallthroughChains));
        code.deathScope();
        loops.getLast().setInfinity(false); // for cases
    }

    @Override
    public void visitFalse(FalseExpression expression) {
        insertFalse();
    }

    @Override
    public void visitFloat(FloatExpression expression) {
        insertPush(expression.value, FloatOperand::valueOf);
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
        insertNecessaryPop();
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
        ExpressionToOperand e2of = new ExpressionToOperand(code, false);
        builtIn.setFunction(statement.name, new ScriptFunction(
                statement.names.toArray(new String[0]),
                locals, // function arguments must be first at local list
                statement.optionals.stream().map(e2of::apply).toArray(Operand[]::new),
                code.getBuilder()));
        code.exitScope(); code.exitContext();
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
        endCondition();
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
        endCondition();
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
            insertGoto(ex);
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
        insertPush(expression.value, IntOperand::valueOf);
    }

    @Override
    public void visitLeftShift(ShiftLeftExpression expression) {
        visitBinary(expression);
        insertLhs(line(expression));
        insertNecessaryPop();
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
        endCondition();
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
        endCondition();
    }

    @Override
    public void visitMultiply(MultiplyExpression expression) {
        visitBinary(expression);
        insertMul(line(expression));
        insertNecessaryPop();
    }

    @Override
    public void visitNegative(NegativeExpression expression) {
        visitUnary(expression);
        code.addState(line(expression), Neg.INSTANCE);
        insertNecessaryPop();
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
        endCondition();
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
        endCondition();
    }

    @Override
    public void visitNullCoalesce(NullCoalesceExpression expression) {
        // todo: Это очевидно неполноценная реализация.
        visitExpression(expression.lhs);
        insertDup(line(expression));
        int el = code.createFlow();
        code.addFlow(el, new Ifnonnull());
        code.decStack();
        code.addState(Pop.POP);
        code.decStack();
        visitExpression(expression.rhs);
        code.resolveFlow(el);
        insertNecessaryPop();
    }

    @Override
    public void visitNull(NullExpression expression) {
        code.incStack();
        code.addState(PushNull.INSTANCE);
        insertNecessaryPop();
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
        endCondition();
    }

    @Override
    public void visitParens(ParensExpression expression) {
        visitStatement(expression.expr);
    }

    @Override
    public void visitPositive(PositiveExpression expression) {
        visitUnary(expression);
        code.addState(line(expression), Pos.INSTANCE);
        insertNecessaryPop();
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
        insertNecessaryPop();
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
    }

    private static boolean isNull(Expression expression) {
        Expression expr = TreeInfo.removeParens(expression);
        return expr == null || expr instanceof NullExpression;
    }

    @Override
    public void visitRightShift(ShiftRightExpression expression) {
        visitBinary(expression);
        insertRhs(line(expression));
        insertNecessaryPop();
    }

    @Override
    public void visitString(StringExpression expression) {
        insertPush(expression.value, StringOperand::valueOf);
    }

    @Override
    public void visitSubtract(SubtractExpression expression) {
        visitBinary(expression);
        insertSub(line(expression));
        insertNecessaryPop();
    }

    @Override
    public void visitSwitch(SwitchStatement statement) {
        int count = 0;
        for (CaseStatement _case: statement.cases) {
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
        for (CaseStatement _case: statement.cases) {
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
        beginCondition();
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
        insertGoto(ex);
        code.resolveFlow(el);
        visitExpression(expression.rhs);
        code.resolveFlow(ex);
        insertNecessaryPop();
    }

    @Override
    public void visitTrue(TrueExpression expression) {
        insertTrue();
    }

    @Override
    public void visitVariable(VariableExpression expression) {
        code.incStack();
        String name = expression.name;
        if (builtIn.testConstant(name)) {
            code.addState(new Getconst(name));
        } else {
            code.addState(line(expression), new Vload(name, code.getLocal(name)));
        }
        insertNecessaryPop();
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
            insertGoto(cond);
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
            insertGoto(begin);
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
        expressionDepth++;
        visitStatement(expression);
        expressionDepth--;
    }

    private void visitBinary(BinaryExpression expression) {
        expressionDepth++;
        visitStatement(expression.lhs);
        visitStatement(expression.rhs);
        expressionDepth--;
    }

    private void visitUnary(UnaryExpression expression) {
        expressionDepth++;
        visitStatement(expression.hs);
        expressionDepth--;
    }

    private void visitList(List<? extends Expression> expressions) {
        expressionDepth++;
        expressions.forEach(this::visitStatement);
        expressionDepth--;
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

    private void visitAssignment(AssignmentExpression expression, AssignmentState state) {
        Expression var = expression.var.child();
        checkAssignable(var);
        int line = line(expression);
        if (var instanceof ArrayAccessExpression) {
            ArrayAccessExpression var0 = (ArrayAccessExpression) var;
            visitUnary(var0);
            Iterator<Expression> keys = var0.keys.iterator();
            Expression key;
            for (;;) {
                key = keys.next();
                visitExpression(key);
                if (keys.hasNext()) {
                    insertALoad(line(key));
                } else {
                    break;
                }
            }
            if (state != null) {
                insertDup2(0);
                insertALoad(line(key));
                state.insert(line);
            } else {
                visitExpression(expression.expr);
            }
            if (expressionDepth>0)
                insertDup_x2(0);
            insertAStore(line);
        } else if (var instanceof VariableExpression) {
            if (state != null) {
                visitExpression(var);
                state.insert(line);
            } else {
                visitExpression(expression.expr);
                if (expressionDepth > 0) {
                    insertDup(0);
                }
            }
            insertVStore(line, ((VariableExpression) var).name);
        }
    }

    private void visitIncrease(IncreaseExpression expression, boolean isIncrement, boolean isPost) {
        Expression hs = expression.hs.child();
        checkAssignable(hs);
        int line = line(expression);
        if (hs instanceof ArrayAccessExpression) {
            ArrayAccessExpression hs0 = (ArrayAccessExpression) hs;
            visitUnary(hs0);
            Iterator<Expression> keys = hs0.keys.iterator();
            Expression key;
            // todo: Убрать этот затрудняющий чтение цикл.
            for (;;) {
                key = keys.next();
                visitExpression(key);
                if (keys.hasNext()) {
                    insertALoad(line(key));
                } else {
                    break;
                }
            }
            insertDup2(0);
            insertALoad(line(key));
            if (isPost && (expressionDepth > 0)) {
                insertDup_x2(0);
            }
            code.addState(line, isIncrement
                    ? Inc.INSTANCE
                    : Dec.INSTANCE);
            if (!isPost && (expressionDepth > 0)) {
                insertDup_x2(0);
            }
            insertAStore(line);
        } else if (hs instanceof VariableExpression) {
            String name = ((VariableExpression) hs).name;
            if (isPost && (expressionDepth > 0)) {
                insertVLoad(line, name);
            }
            code.addState(line, isIncrement
                    ? new Vinc(name, code.getLocal(name))
                    : new Vinc(name, code.getLocal(name)));
            if (!isPost && (expressionDepth > 0)) {
                insertVLoad(line, name);
            }
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

    private void endCondition() {
        if (conditionDepth != 0) {
            return;
        }
        int ex = code.createFlow();
        insertTrue();
        insertGoto(ex);
        code.resolveFlow(popFlow());
        insertFalse();
        code.resolveFlow(ex);
        insertNecessaryPop();
    }

    private <T> void insertPush(T value, OperandFunction<T> supplier) {
        code.incStack();
        code.addState(new Push(code.intern(value, supplier)));
        insertNecessaryPop();
    }

    private void insertTrue() {
        code.incStack();
        code.addState(PushTrue.INSTANCE);
        insertNecessaryPop();
    }

    private void insertFalse() {
        code.incStack();
        code.addState(PushFalse.INSTANCE);
        insertNecessaryPop();
    }

    private void insertGoto(int flow) {
        code.addFlow(flow, new Goto());
    }

    private void insertDup(int line) {
        code.addState(line, Dup.INSTANCE);
        code.incStack();
    }

    private void insertDup_x1(int line) {
        code.addState(line, Dup_x1.INSTANCE);
        code.incStack();
    }

    private void insertDup_x2(int line) {
        code.addState(line, Dup_x2.INSTANCE);
        code.incStack();
    }

    private void insertDup2(int line) {
        code.addState(line, Dup2.INSTANCE);
        code.incStack(2);
    }

    private void insertDup2_x1(int line) {
        code.addState(line, Dup2_x1.INSTANCE);
        code.incStack(2);
    }

    private void insertDup2_x2(int line) {
        code.addState(line, Dup2_x2.INSTANCE);
        code.incStack(2);
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
        code.addState(line, Or.OR);
        code.decStack();
    }

    private void insertXor(int line) {
        code.addState(line, Xor.XOR);
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
        code.addState(line, Mul.MUL);
        code.decStack();
    }

    private void insertRem(int line) {
        code.addState(line, Rem.REM);
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
        code.addState(line, new Vload(name, code.getLocal(name)));
    }

    private void insertAStore(int line) {
        code.addState(line, Astore.INSTANCE);
        code.decStack(3);
    }

    private void insertVStore(int line, String name) {
        code.addState(line, new Vstore(name, code.getLocal(name)));
        code.decStack();
    }

    private void insertCaseBody(Statement body) {
        code.resolveFlow(popInt(fallthroughChains));
        fallthroughChains.add(code.createFlow());
        addLoop(false);
        visitStatement(body);
        if (loops.pop().isInfinity()) insertGoto(peekInt(breakChains));
    }

    @Deprecated
    private void insertReturn(boolean isVoid) {
        code.addState(Return.INSTANCE);
        code.deathScope();
    }

    private void insertReturn() {
        code.addState(Return.INSTANCE);
        code.decStack();
        code.deathScope();
    }

    /**
     * Вставляет операцию POP если это нужно.
     */
    private void insertNecessaryPop() {
        if (expressionDepth == 0) {
            code.addState(Pop.POP);
            code.decStack();
        }
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
        expr = expr.child();
        if (!expr.isCloneable()) {
            cError(expr.getPosition(), "cloneable expected.");
        }
    }

    private void checkAccessible(Expression expr) {
        expr = expr.child();
        if (!expr.isAccessible()) {
            cError(expr.getPosition(), "accessible expected.");
        } else if (expr instanceof ArrayAccessExpression) {
            checkAccessible(((ArrayAccessExpression) expr).hs);
        }
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