package jua.compiler;

import jua.interpreter.lang.*;
import jua.interpreter.states.*;
import jua.parser.ast.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

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

    private final Stack<Integer> breakStack;

    private final Stack<Integer> continueStack;

    private final Stack<Integer> fallthroughStack;

    private final Stack<List<Part>> switchPartsStack;

    private final Stack<Integer> chains;

    private final Stack<Loop> loops;

    private int statementDepth = 0;
    
    private int expressionDepth = 0;

    private int conditionDepth = 0;

    private boolean conditionInvert = false;

    public Gen(BuiltIn builtIn) {
        this.builtIn = builtIn;
        code = new Code();
        breakStack = new Stack<>();
        continueStack = new Stack<>();
        fallthroughStack = new Stack<>();
        switchPartsStack = new Stack<>();
        chains = new Stack<>();
        loops = new Stack<>();
    }

    public Result getResult() {
        return new Result(builtIn, code.getBuilder());
    }

    @Override
    public void visitAdd(AddExpression expression) {
        visitBinary(expression);
        insertAdd(line(expression));
        insertPop();
    }

    @Override
    public void visitAnd(AndExpression expression) {
        insertCondition();
        if (conditionInvert) {
            if (expression.rhs == null) {
                visitCondition(expression.lhs);
            } else {
                int fa = chains.push(code.createFlow());
                conditionInvert = false;
                visitCondition(expression.lhs);
                chains.pop();
                conditionInvert = true;
                visitCondition(expression.rhs);
                code.resolveFlow(fa);
            }
        } else {
            visitCondition(expression.lhs);
            visitCondition(expression.rhs);
        }
        insertBoolean();
    }

    @Override
    public void visitArrayAccess(ArrayAccessExpression expression) {
        checkAccessible(expression.hs);
        visitUnary(expression);
        for (Expression key: expression.keys) {
            visitExpression(key);
            insertALoad(line(key));
        }
        insertPop();
    }

    @Override
    public void visitArray(ArrayExpression expression) {
        code.incStack();
        code.addState(Newarray.NEWARRAY);
        insertDup1X(expression.map.size());
        AtomicInteger index = new AtomicInteger();
        expressionDepth++;
        expression.map.forEach((key, value) -> {
            int line;
            if (key.isEmpty()) {
                line = value.getPosition().line;
                insertPush(index.longValue(), IntOperand::valueOf);
            } else {
                line = key.getPosition().line;
                visitStatement(key);
            }
            visitStatement(value);
            insertAStore(line);
            index.incrementAndGet();
        });
        expressionDepth--;
        insertPop();
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
    public void visitAssignLeftShift(AssignLeftShiftExpression expression) {
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
    public void visitAssignRightShift(AssignRightShiftExpression expression) {
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
        insertPop();
    }

    @Override
    public void visitBitNot(BitNotExpression expression) {
        visitUnary(expression);
        code.addState(line(expression), Not.NOT);
        insertPop();
    }

    @Override
    public void visitBitOr(BitOrExpression expression) {
        visitBinary(expression);
        insertOr(line(expression));
        insertPop();
    }

    @Override
    public void visitBitXor(BitXorExpression expression) {
        visitBinary(expression);
        insertXor(line(expression));
        insertPop();
    }

    @Override
    public void visitBlock(BlockStatement statement) {
        if (code.empty()) {
            code.enterContext(statement.getPosition().filename);
            code.enterScope();
        }
        statement.statements.forEach(this::visitStatement);
    }

    @Override
    public void visitBreak(BreakStatement statement) {
        if (breakStack.isEmpty()) {
            cError(statement.getPosition(), "'break' is not allowed outside of loop/switch.");
            return;
        }
        insertGoto(breakStack.peek());
        code.deathScope();
        loops.peek().setInfinity(false);
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
        code.addState(Clone.CLONE);
        insertPop();
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
        if (continueStack.isEmpty()) {
            cError(statement.getPosition(), "'continue' is not allowed outside of loop.");
            return;
        }
        insertGoto(continueStack.peek());
        code.deathScope();
    }

    @Override
    public void visitDivide(DivideExpression expression) {
        visitBinary(expression);
        insertDiv(line(expression));
        insertPop();
    }

    @Override
    public void visitDo(DoStatement statement) {
        compileLoop(null, statement.cond, null, statement.body, false);
    }

    @Override
    public void visitEqual(EqualExpression expression) {
        insertCondition();
        Expression rhs = expression.rhs.child();
        if (rhs instanceof NullExpression) {
            visitExpression(expression.lhs);
            code.addFlow(chains.peek(), conditionInvert ? new Ifnull() : new Ifnonnull());
            code.decStack();
        } else {
            visitBinary(expression);
            code.addFlow(chains.peek(), conditionInvert ? new Ifcmpeq() : new Ifcmpne());
            code.decStack(2);
        }
        insertBoolean();
    }

    @Override
    public void visitFallthrough(FallthroughStatement statement) {
        if (fallthroughStack.isEmpty()) {
            cError(statement.getPosition(), "'fallthrough' is not allowed outside of switch.");
            return;
        }
        insertGoto(fallthroughStack.peek());
        code.deathScope();
        loops.peek().setInfinity(false); // for cases
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
        insertPop();
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
        insertReturn(true);
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
        insertCondition();
        visitBinary(expression);
        code.addFlow(chains.peek(), line(expression),
                conditionInvert ? new Ifcmpge() : new Ifcmplt());
        code.decStack(2);
        insertBoolean();
    }

    @Override
    public void visitGreater(GreaterExpression expression) {
        insertCondition();
        visitBinary(expression);
        code.addFlow(chains.peek(), line(expression),
                conditionInvert ? new Ifcmpgt() : new Ifcmple());
        code.decStack(2);
        insertBoolean();
    }

    @Override
    public void visitIf(IfStatement statement) {
        if (statement.elseBody == null) {
            chains.add(code.createFlow());
            visitCondition(statement.cond);
            visitBody(statement.body);
            code.resolveFlow(chains.pop());
        } else {
            int el = chains.push(code.createFlow());
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
    public void visitLeftShift(LeftShiftExpression expression) {
        visitBinary(expression);
        insertLhs(line(expression));
        insertPop();
    }

    @Override
    public void visitLessEqual(LessEqualExpression expression) {
        insertCondition();
        visitBinary(expression);
        code.addFlow(chains.peek(), line(expression),
                conditionInvert ? new Ifcmple() : new Ifcmpgt());
        code.decStack(2);
        insertBoolean();
    }

    @Override
    public void visitLess(LessExpression expression) {
        insertCondition();
        visitBinary(expression);
        code.addFlow(chains.peek(), line(expression),
                conditionInvert ? new Ifcmplt() : new Ifcmpge());
        code.decStack(2);
        insertBoolean();
    }

    @Override
    public void visitMultiply(MultiplyExpression expression) {
        visitBinary(expression);
        insertMul(line(expression));
        insertPop();
    }

    @Override
    public void visitNegative(NegativeExpression expression) {
        visitUnary(expression);
        code.addState(line(expression), Neg.NEG);
        insertPop();
    }

    @Override
    public void visitNotEqual(NotEqualExpression expression) {
        insertCondition();
        Expression rhs = expression.rhs.child();
        if (rhs instanceof NullExpression) {
            visitExpression(expression.lhs);
            code.addFlow(chains.peek(), conditionInvert ? new Ifnonnull() : new Ifnull());
            code.decStack();
        } else {
            visitBinary(expression);
            code.addFlow(chains.peek(), conditionInvert ? new Ifcmpne() : new Ifcmpeq());
            code.decStack(2);
        }
        insertBoolean();
    }

    @Override
    public void visitNot(NotExpression expression) {
        insertCondition();
        if (conditionInvert) {
            conditionInvert = false;
            visitCondition(expression.hs);
            conditionInvert = true;
        } else {
            conditionInvert = true;
            visitCondition(expression.hs);
            conditionInvert = false;
        }
        insertBoolean();
    }

    @Override
    public void visitNullCoalesce(NullCoalesceExpression expression) {
        visitExpression(expression.lhs);
        insertDup1X();
        int el = code.createFlow();
        code.addFlow(el, new Ifnonnull());
        code.decStack();
        code.addState(Pop.POP);
        code.decStack();
        visitExpression(expression.rhs);
        code.resolveFlow(el);
        insertPop();
    }

    @Override
    public void visitNull(NullExpression expression) {
        code.incStack();
        code.addState(PushNull.INSTANCE);
        insertPop();
    }

    @Override
    public void visitOr(OrExpression expression) {
        insertCondition();
        if (conditionInvert) {
            visitCondition(expression.lhs);
            visitCondition(expression.rhs);
        } else {
            if (expression.rhs == null) {
                visitCondition(expression.lhs);
            } else {
                int tr = chains.push(code.createFlow());
                conditionInvert = true;
                visitCondition(expression.lhs);
                chains.pop();
                conditionInvert = false;
                visitCondition(expression.rhs);
                code.resolveFlow(tr);
            }
        }
        insertBoolean();
    }

    @Override
    public void visitParens(ParensExpression expression) {
        visitStatement(expression.expr);
    }

    @Override
    public void visitPositive(PositiveExpression expression) {
        visitUnary(expression);
        code.addState(line(expression), Pos.POS);
        insertPop();
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
        insertPop();
    }

    @Override
    public void visitReturn(ReturnStatement statement) {
        boolean isVoid = (statement.expr == null) || (statement.expr.child() instanceof NullExpression);
        if (isVoid) {
            insertReturn(true);
        } else {
            visitExpression(statement.expr);
            insertReturn(false);
        }
    }

    @Override
    public void visitRightShift(RightShiftExpression expression) {
        visitBinary(expression);
        insertRhs(line(expression));
        insertPop();
    }

    @Override
    public void visitString(StringExpression expression) {
        insertPush(expression.value, StringOperand::valueOf);
    }

    @Override
    public void visitSubtract(SubtractExpression expression) {
        visitBinary(expression);
        insertSub(line(expression));
        insertPop();
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
        code.addFlow(breakStack.push(code.createFlow()), _switch);
        fallthroughStack.add(code.createFlow());
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
        code.resolveFlow(fallthroughStack.pop());
        code.resolveFlow(breakStack.pop());
    }

    @Override
    public void visitTernary(TernaryExpression expression) {
        insertCondition();
        int el = chains.push(code.createFlow());
        int ex = code.createFlow();
        if (conditionInvert) {
            conditionInvert = false;
            visitCondition(expression.cond);
            conditionInvert = true;
        } else {
            visitCondition(expression.cond);
        }
        chains.pop();
        visitExpression(expression.lhs);
        insertGoto(ex);
        code.resolveFlow(el);
        visitExpression(expression.rhs);
        code.resolveFlow(ex);
        insertPop();
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
        insertPop();
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
        breakStack.add(exit);
        continueStack.add(cond);
        visitBody(body);
        if (steps != null) {
            steps.forEach(this::visitStatement);
        }
        code.resolveFlow(cond);
        if (condition == null) {
            insertGoto(begin);
        } else {
            chains.add(begin);
            conditionInvert = true;
            visitCondition(condition);
            conditionInvert = false;
            chains.pop();
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
        code.addFlow(chains.peek(), line(expression),
                conditionInvert ? new Ifne() : new Ifeq());
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
                insertDup2_1();
                insertALoad(line(key));
                state.insert(line);
            } else {
                visitExpression(expression.expr);
            }
            if ((expressionDepth > 0)) {
                insertDupMov_1_m3();
            }
            insertAStore(line);
        } else if (var instanceof VariableExpression) {
            if (state != null) {
                visitExpression(var);
                state.insert(line);
            } else {
                visitExpression(expression.expr);
            }
            if ((expressionDepth > 0)) {
                insertDup1X();
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
            for (;;) {
                key = keys.next();
                visitExpression(key);
                if (keys.hasNext()) {
                    insertALoad(line(key));
                } else {
                    break;
                }
            }
            insertDup2_1();
            insertALoad(line(key));
            if (isPost && (expressionDepth > 0)) {
                insertDupMov_1_m3();
            }
            code.addState(line, isIncrement ? Inc.INC : Dec.DEC);
            if (!isPost && (expressionDepth > 0)) {
                insertDupMov_1_m3();
            }
            insertAStore(line);
        } else if (hs instanceof VariableExpression) {
            String name = ((VariableExpression) hs).name;
            insertVLoad(line, name);
            if (isPost && (expressionDepth > 0)) {
                insertDup1X();
            }
            code.addState(line, isIncrement ? Inc.INC : Dec.DEC);
            if (!isPost && (expressionDepth > 0)) {
                insertDup1X();
            }
            insertVStore(line, name);
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

    private void insertCondition() {
        if (conditionDepth != 0) {
            return;
        }
        chains.add(code.createFlow());
    }

    private void insertBoolean() {
        if (conditionDepth != 0) {
            return;
        }
        int ex = code.createFlow();
        insertTrue();
        insertGoto(ex);
        code.resolveFlow(chains.pop());
        insertFalse();
        code.resolveFlow(ex);
        insertPop();
    }

    private <T> void insertPush(T value, OperandFunction<T> supplier) {
        code.incStack();
        code.addState(new Push(code.intern(value, supplier)));
        insertPop();
    }

    private void insertTrue() {
        code.incStack();
        code.addState(PushTrue.INSTANCE);
        insertPop();
    }

    private void insertFalse() {
        code.incStack();
        code.addState(PushFalse.INSTANCE);
        insertPop();
    }

    private void insertGoto(int flow) {
        code.addFlow(flow, new Goto());
    }

    private void insertDup1X() {
        insertDup1X(1);
    }

    private void insertDup1X(int x) {
        switch (x) {
            case 0: break;
            case 1:
                code.incStack();
                code.addState(Dup.DUP1_1);
                break;
            case 2:
                code.incStack(2);
                code.addState(Dup.DUP1_2);
                break;
            default:
                code.incStack(x);
                code.addState(new Dup(1, x));
        }
    }

    private void insertDup2_1() {
        code.incStack(2);
        code.addState(Dup.DUP2_1);
    }

    private void insertDupMov_1_m3() {
        code.incStack();
        code.addState(DupMov.DUP_MOV_1_M3);
    }

    private void insertAdd(int line) {
        code.addState(line, Add.ADD);
        code.decStack();
    }

    private void insertAnd(int line) {
        code.addState(line, And.AND);
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
        code.addState(line, Div.DIV);
        code.decStack();
    }

    private void insertLhs(int line) {
        code.addState(line, Shl.LSH);
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
        code.addState(line, Shr.RSH);
        code.decStack();
    }

    private void insertSub(int line) {
        code.addState(line, Sub.SUB);
        code.decStack();
    }

    private void insertALoad(int line) {
        code.addState(line, Aload.ALOAD);
        code.decStack();
    }

    private void insertVLoad(int line, String name) {
        code.incStack();
        code.addState(line, new Vload(name, code.getLocal(name)));
    }

    private void insertAStore(int line) {
        code.addState(line, Astore.ASTORE);
        code.decStack(3);
    }

    private void insertVStore(int line, String name) {
        code.addState(line, new Vstore(name, code.getLocal(name)));
        code.decStack();
    }

    private void insertCaseBody(Statement body) {
        code.resolveFlow(fallthroughStack.pop());
        fallthroughStack.add(code.createFlow());
        addLoop(false);
        visitStatement(body);
        if (loops.pop().isInfinity()) insertGoto(breakStack.peek());
    }

    private void insertReturn(boolean isVoid) {
        code.addState(isVoid ? Return.VOID : Return.NOT_VOID);
        code.deathScope();
    }

    private void insertPop() {
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
        return stmt.getPosition().line;
    }

    private void cError(Position position, String message) {
        throw new CompileError(message, position);
    }
}