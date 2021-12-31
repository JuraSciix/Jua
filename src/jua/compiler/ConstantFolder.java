package jua.compiler;

import jua.parser.Tree;
import jua.parser.Tree.*;

import java.util.*;
import java.util.stream.Collectors;

public class ConstantFolder implements Visitor {

    /**
     * Ассоциативный список констант по их названиям.
     */
    private final Map<String, Expression> constantFolding;

    private Tree lower;

    public ConstantFolder(CodeData codeData) {
        constantFolding = new HashMap<>();
        putFoldingNames(codeData.constants.keySet());
    }

    private void putFoldingNames(Set<String> names) {
        names.forEach(name -> constantFolding.put(name, null));
    }

    @Override
    public void visitAdd(AddExpression expression) {
        Expression lhs = getLowerExpression(expression.lhs).child();
        Expression rhs = getLowerExpression(expression.rhs).child();
        if ((lhs instanceof FloatExpression) && (rhs instanceof FloatExpression)) {
            ((FloatExpression) lhs).value += ((FloatExpression) rhs).value;
            lower = lhs;
            return;
        }
        if ((lhs instanceof FloatExpression) && (rhs instanceof IntExpression)) {
            ((FloatExpression) lhs).value += ((IntExpression) rhs).value;
            lower = lhs;
            return;
        }
        if ((lhs instanceof IntExpression) && (rhs instanceof FloatExpression)) {
            long l = ((IntExpression) lhs).value;
            double r = ((FloatExpression) rhs).value;
            lower = new FloatExpression(expression.getPosition(), l + r);
            return;
        }
        if ((lhs instanceof IntExpression) && (rhs instanceof IntExpression)) {
            ((IntExpression) lhs).value += ((IntExpression) rhs).value;
            lower = lhs;
            return;
        }
        if ((lhs instanceof StringExpression) && (rhs instanceof StringExpression)) {
            // .concat is not appropriate here
            ((StringExpression) lhs).value += (((StringExpression) rhs).value);
            lower = lhs;
            return;
        }
        lowerBinary(expression, lhs, rhs);
    }

    @Override
    public void visitAnd(AndExpression expression) {
        Expression lhs = getLowerExpression(expression.lhs).child();
        Expression rhs = getLowerExpression(expression.rhs).child();
        if (isFalse(lhs) || isFalse(rhs)) {
            setFalse(expression);
            return;
        }
        if (isTrue(lhs)) {
            lhs = null; // compiler will detect this
        }
        if (isTrue(rhs)) {
            rhs = null; // compiler will detect this
        }
        if (lhs == null && rhs == null) {
            setTrue(expression);
            return;
        }
        lowerBinary(expression, lhs, rhs);
    }

    @Override
    public void visitArrayAccess(ArrayAccessExpression expression) {
        expression.hs.accept(this);
        expression.key = getLowerExpression(expression.key);
        lower = expression;
    }

    @Override
    public void visitArray(ArrayExpression expression) {
        Map<Expression, Expression> map = new LinkedHashMap<>();
        expression.map.forEach((key, value) -> {
            if (key.isEmpty()) {
                map.put(key, getLowerExpression(value));
            } else {
                map.put(getLowerExpression(key), getLowerExpression(value));
            }
        });
        expression.map = map;
        lower = expression;
    }

    @Override
    public void visitAssignAdd(AssignAddExpression expression) {
        visitAssignment(expression);
    }

    @Override
    public void visitAssignBitAnd(AssignBitAndExpression expression) {
        visitAssignment(expression);
    }

    @Override
    public void visitAssignBitOr(AssignBitOrExpression expression) {
        visitAssignment(expression);
    }

    @Override
    public void visitAssignBitXor(AssignBitXorExpression expression) {
        visitAssignment(expression);
    }

    @Override
    public void visitAssignDivide(AssignDivideExpression expression) {
        visitAssignment(expression);
    }

    @Override
    public void visitAssignLeftShift(AssignShiftLeftExpression expression) {
        visitAssignment(expression);
    }

    @Override
    public void visitAssign(AssignExpression expression) {
        visitAssignment(expression);
    }

    @Override
    public void visitAssignMultiply(AssignMultiplyExpression expression) {
        visitAssignment(expression);
    }

    @Override
    public void visitAssignNullCoalesce(AssignNullCoalesceExpression expression) {
        visitAssignment(expression);
    }

    @Override
    public void visitAssignRemainder(AssignRemainderExpression expression) {
        visitAssignment(expression);
    }

    @Override
    public void visitAssignRightShift(AssignShiftRightExpression expression) {
        visitAssignment(expression);
    }

    @Override
    public void visitAssignSubtract(AssignSubtractExpression expression) {
        visitAssignment(expression);
    }

    @Override
    public void visitBitAnd(BitAndExpression expression) {
        Expression lhs = getLowerExpression(expression.lhs).child();
        Expression rhs = getLowerExpression(expression.rhs).child();
        if ((lhs instanceof IntExpression) && (rhs instanceof IntExpression)) {
            ((IntExpression) lhs).value &= ((IntExpression) rhs).value;
            lower = lhs;
            return;
        }
        if ((lhs instanceof BooleanExpression) && (rhs instanceof BooleanExpression)) {
            if ((lhs instanceof TrueExpression) && (rhs instanceof TrueExpression)) {
                lower = lhs;
            } else {
                setFalse(expression);
            }
            return;
        }
        lowerBinary(expression, lhs, rhs);
    }

    @Override
    public void visitBitNot(BitNotExpression expression) {
        Expression hs = getLowerExpression(expression.hs).child();
        if (hs instanceof IntExpression) {
            ((IntExpression) hs).value = ~((IntExpression) hs).value;
            lower = hs;
            return;
        }
        lowerUnary(expression, hs);
    }

    @Override
    public void visitBitOr(BitOrExpression expression) { expression.lhs.accept(this);
        Expression lhs = getLowerExpression(expression.lhs).child();
        Expression rhs = getLowerExpression(expression.rhs).child();
        if ((lhs instanceof IntExpression) && (rhs instanceof IntExpression)) {
            ((IntExpression) lhs).value |= ((IntExpression) rhs).value;
            lower = lhs;
            return;
        }
        if ((lhs instanceof BooleanExpression) && (rhs instanceof BooleanExpression)) {
            if ((lhs instanceof TrueExpression) || (rhs instanceof TrueExpression)) {
                lower = (lhs instanceof TrueExpression) ? lhs : rhs;
            } else {
                setFalse(expression);
            }
            return;
        }
        lowerBinary(expression, lhs, rhs);
    }

    @Override
    public void visitBitXor(BitXorExpression expression) {
        Expression lhs = getLowerExpression(expression.lhs).child();
        Expression rhs = getLowerExpression(expression.rhs).child();
        if ((lhs instanceof IntExpression) && (rhs instanceof IntExpression)) {
            ((IntExpression) lhs).value ^= ((IntExpression) rhs).value;
            lower = lhs;
            return;
        }
        if ((lhs instanceof BooleanExpression) && (rhs instanceof BooleanExpression)) {
            if (lhs.getClass() != rhs.getClass()) {
                setTrue(expression);
            } else {
                setFalse(expression);
            }
            return;
        }
        lowerBinary(expression, lhs, rhs);
    }

    @Override
    public void visitBlock(BlockStatement statement) {
        statement.statements = lowerList(statement.statements);
        lower = statement;
    }

    @Override
    public void visitBreak(BreakStatement statement) {
        nothing(statement);
    }

    @Override
    public void visitCase(CaseStatement statement) {
        statement.expressions = lowerList(statement.expressions);
        statement.body = getLowerStatement(statement.body);
        lower = statement;
    }

    @Override
    public void visitClone(CloneExpression expression) {
        lowerUnary(expression, getLowerExpression(expression.hs).child());
    }

    @Override
    public void visitConstantDeclare(ConstantDeclareStatement statement) {
        int size = statement.names.size() & statement.expressions.size();
        for (int i = 0; i < size; i++) {
            String name = statement.names.get(i);
            if (statement.names.indexOf(name) != statement.names.lastIndexOf(name)) {
                throw new CompileError("duplicate name '" + name + "'.", statement.getPosition());
            }
            Expression expr = getLowerExpression(statement.expressions.get(i)).child();
            if (expr.isLiteral() ||
                    (expr instanceof NullExpression) ||
                    (expr instanceof BooleanExpression)) {
                constantFolding.put(name, expr);
            }
            statement.expressions.set(i, expr);
        }
        lower = statement;
    }

    @Override
    public void visitContinue(ContinueStatement statement) {
        nothing(statement);
    }

    @Override
    public void visitDivide(DivideExpression expression) {
        Expression lhs = getLowerExpression(expression.lhs).child();
        Expression rhs = getLowerExpression(expression.rhs).child();
        if ((lhs instanceof FloatExpression) && (rhs instanceof FloatExpression)) {
            ((FloatExpression) lhs).value /= ((FloatExpression) rhs).value;
            lower = lhs;
            return;
        }
        if ((lhs instanceof FloatExpression) && (rhs instanceof IntExpression)) {
            ((FloatExpression) lhs).value /= ((IntExpression) rhs).value;
            lower = lhs;
            return;
        }
        if ((lhs instanceof IntExpression) && (rhs instanceof FloatExpression)) {
            long l = ((IntExpression) lhs).value;
            double r = ((FloatExpression) rhs).value;
            lower = new FloatExpression(expression.getPosition(), l / r);
            return;
        }
        if ((lhs instanceof IntExpression) && (rhs instanceof IntExpression)) {
            long l = ((IntExpression) lhs).value;
            long r = ((IntExpression) rhs).value;
            if (r != 0L) {
                if (l % r == 0) {
                    ((IntExpression) lhs).value = (l / r);
                    lower = lhs;
                } else {
                    lower = new FloatExpression(expression.getPosition(), (double) l / r);
                }
                return;
            }
        }
        lowerBinary(expression, lhs, rhs);
    }

    @Override
    public void visitDo(DoStatement statement) {
        Statement body = getLowerStatement(statement.body);
        Expression cond = getLowerExpression(statement.cond).child();
        if (isFalse(cond)) {
            lower = body;
            return;
        }
        if (isTrue(cond)) {
            cond = null; // compiler will detect this
        }
        statement.body = body;
        statement.cond = cond;
        lower = statement;
    }

    @Override
    public void visitEqual(EqualExpression expression) {
        Expression lhs = getLowerExpression(expression.lhs).child();
        Expression rhs = getLowerExpression(expression.rhs).child();
        switch (compareLiterals(lhs, rhs)) {
            case 0:
                setTrue(expression);
                break;
            case 1:
            case -1:
            case 2:
                setFalse(expression);
                break;
            default:
                lowerBinary(expression, lhs, rhs);
        }
    }

    @Override
    public void visitFallthrough(FallthroughStatement statement) {
        nothing(statement);
    }

    @Override
    public void visitFalse(FalseExpression expression) {
        nothing(expression);
    }

    @Override
    public void visitFloat(FloatExpression expression) {
        nothing(expression);
    }

    @Override
    public void visitFor(ForStatement statement) {
        if (statement.init != null) {
            statement.init = lowerList(statement.init);
        }
        if (statement.cond != null) {
            Expression cond = getLowerExpression(statement.cond);

            if (isFalse(cond)) {
                lower = Statement.EMPTY;
                return;
            }
            if (isTrue(cond)) {
                cond = null; // compiler will detect this
            }
            statement.cond = cond;
        }
        if (statement.step != null) {
            statement.step = lowerList(statement.step);
        }
        statement.body = getLowerStatement(statement.body);
        lower = statement;
    }

    @Override
    public void visitFunctionCall(FunctionCallExpression expression) {
        expression.args = lowerList(expression.args);
        lower = expression;
    }

    @Override
    public void visitFunctionDefine(FunctionDefineStatement statement) {
        statement.optionals = lowerList(statement.optionals);
        statement.body = getLowerStatement(statement.body);
        lower = statement;
    }

    @Override
    public void visitGreaterEqual(GreaterEqualExpression expression) {
        Expression lhs = getLowerExpression(expression.lhs).child();
        Expression rhs = getLowerExpression(expression.rhs).child();
        switch (compareLiterals(lhs, rhs)) {
            case 1:
            case 0:
                setTrue(expression);
                break;
            case -1:
            case 2:
                setFalse(expression);
                break;
            default:
                lowerBinary(expression, lhs, rhs);
        }
    }

    @Override
    public void visitGreater(GreaterExpression expression) {
        Expression lhs = getLowerExpression(expression.lhs).child();
        Expression rhs = getLowerExpression(expression.rhs).child();
        switch (compareLiterals(lhs, rhs)) {
            case 1:
                setTrue(expression);
                break;
            case 0:
            case -1:
            case 2:
                setFalse(expression);
                break;
            default:
                lowerBinary(expression, lhs, rhs);
        }
    }

    @Override
    public void visitIf(IfStatement statement) {
        Expression cond = getLowerExpression(statement.cond).child();
        Statement body = getLowerStatement(statement.body);
        Statement elseBody = null;
        if (statement.elseBody != null) {
            elseBody = getLowerStatement(statement.elseBody);
        }
        if (isTrue(cond)) {
            lower = body;
            return;
        }
        if (isFalse(cond)) {
            lower = Statement.EMPTY;
            return;
        }
        statement.cond = cond;
        statement.body = body;
        statement.elseBody = elseBody;
        lower = statement;
    }

    @Override
    public void visitInt(IntExpression expression) {
        nothing(expression);
    }

    @Override
    public void visitLeftShift(ShiftLeftExpression expression) {
        Expression lhs = getLowerExpression(expression.lhs).child();
        Expression rhs = getLowerExpression(expression.rhs).child();
        if ((lhs instanceof IntExpression) && (rhs instanceof IntExpression)) {
            ((IntExpression) lhs).value <<= ((IntExpression) rhs).value;
            lower = lhs;
            return;
        }
        lowerBinary(expression, lhs, rhs);
    }

    @Override
    public void visitLessEqual(LessEqualExpression expression) {
        Expression lhs = getLowerExpression(expression.lhs).child();
        Expression rhs = getLowerExpression(expression.rhs).child();
        switch (compareLiterals(lhs, rhs)) {
            case -1:
            case 0:
                setTrue(expression);
                break;
            case 1:
            case 2:
                setFalse(expression);
                break;
            default:
                lowerBinary(expression, lhs, rhs);
        }
    }

    @Override
    public void visitLess(LessExpression expression) {
        Expression lhs = getLowerExpression(expression.lhs).child();
        Expression rhs = getLowerExpression(expression.rhs).child();
        switch (compareLiterals(lhs, rhs)) {
            case -1:
                setTrue(expression);
                break;
            case 1:
            case 0:
            case 2:
                setFalse(expression);
                break;
            default:
                lowerBinary(expression, lhs, rhs);
        }
    }

    @Override
    public void visitMultiply(MultiplyExpression expression) {
        Expression lhs = getLowerExpression(expression.lhs).child();
        Expression rhs = getLowerExpression(expression.rhs).child();
        if ((lhs instanceof FloatExpression) && (rhs instanceof FloatExpression)) {
            ((FloatExpression) lhs).value *= ((FloatExpression) rhs).value;
            lower = lhs;
            return;
        }
        if ((lhs instanceof FloatExpression) && (rhs instanceof IntExpression)) {
            ((FloatExpression) lhs).value *= ((IntExpression) rhs).value;
            lower = lhs;
            return;
        }
        if ((lhs instanceof IntExpression) && (rhs instanceof FloatExpression)) {
            ((FloatExpression) rhs).value *= ((IntExpression) lhs).value;
            lower = rhs;
            return;
        }
        if ((lhs instanceof IntExpression) && (rhs instanceof IntExpression)) {
            ((IntExpression) lhs).value *= ((IntExpression) rhs).value;
            lower = lhs;
            return;
        }
        lowerBinary(expression, lhs, rhs);
    }

    @Override
    public void visitNegative(NegativeExpression expression) {
        Expression hs = getLowerExpression(expression.hs).child();

        if (hs instanceof FloatExpression) {
            ((FloatExpression) hs).value = -((FloatExpression) hs).value;
            lower = hs;
            return;
        }
        if (hs instanceof IntExpression) {
            ((IntExpression) hs).value = -((IntExpression) hs).value;
            lower = hs;
            return;
        }
        lowerUnary(expression, hs);
    }

    @Override
    public void visitNotEqual(NotEqualExpression expression) {
        Expression lhs = getLowerExpression(expression.lhs).child();
        Expression rhs = getLowerExpression(expression.rhs).child();
        switch (compareLiterals(lhs, rhs)) {
            case -1:
            case 1:
            case 2:
                setTrue(expression);
                break;
            case 0:
                setFalse(expression);
                break;
            default:
                lowerBinary(expression, lhs, rhs);
        }
    }

    @Override
    public void visitNot(NotExpression expression) {
        Expression hs = getLowerExpression(expression.hs).child();

        if (isTrue(hs)) {
            setFalse(expression);
            return;
        }
        if (isFalse(hs)) {
            setTrue(expression);
            return;
        }
        lowerUnary(expression, hs);
    }

    @Override
    public void visitNullCoalesce(NullCoalesceExpression expression) {
        Expression lhs = getLowerExpression(expression.lhs).child();
        Expression rhs = getLowerExpression(expression.rhs).child();
        if (!lhs.isNullable() || (lhs instanceof NullExpression)) {
            lower = rhs;
        } else {
            lowerBinary(expression, lhs, rhs);
        }
    }

    @Override
    public void visitNull(NullExpression expression) {
        nothing(expression);
    }

    @Override
    public void visitOr(OrExpression expression) {
        Expression lhs = getLowerExpression(expression.lhs).child();
        Expression rhs = getLowerExpression(expression.rhs).child();
        if (isTrue(lhs) || isTrue(rhs)) {
            setTrue(expression);
            return;
        }
        if (isFalse(lhs)) {
            lhs = null; // compiler will detect this
        }
        if (isFalse(rhs)) {
            rhs = null; // compiler will detect this
        }
        if (lhs == null && rhs == null) {
            setFalse(expression);
            return;
        }
        lowerBinary(expression, lhs, rhs);
    }

    @Override
    public void visitParens(ParensExpression expression) {
        // Удаление скобок
        lower = getLowerExpression(expression.expr);
    }

    @Override
    public void visitPositive(PositiveExpression expression) {
        Expression hs = getLowerExpression(expression.hs).child();

        if ((hs instanceof FloatExpression) || (hs instanceof IntExpression)) {
            lower = hs;
            return;
        }
        lowerUnary(expression, hs);
    }

    @Override
    public void visitPostDecrement(PostDecrementExpression expression) {
        visitIncrease(expression);
    }

    @Override
    public void visitPostIncrement(PostIncrementExpression expression) {
        visitIncrease(expression);
    }

    @Override
    public void visitPreDecrement(PreDecrementExpression expression) {
        visitIncrease(expression);
    }

    @Override
    public void visitPreIncrement(PreIncrementExpression expression) {
        visitIncrease(expression);
    }

    @Override
    public void visitPrintln(PrintlnStatement statement) {
        statement.expressions = lowerList(statement.expressions);
        lower = statement;
    }

    @Override
    public void visitPrint(PrintStatement statement) {
        statement.expressions = lowerList(statement.expressions);
        lower = statement;
    }

    @Override
    public void visitRemainder(RemainderExpression expression) {
        Expression lhs = getLowerExpression(expression.lhs).child();
        Expression rhs = getLowerExpression(expression.rhs).child();
        if ((lhs instanceof FloatExpression) && (rhs instanceof FloatExpression)) {
            double r = ((FloatExpression) rhs).value;
            if (r != 0D) {
                ((FloatExpression) lhs).value %= r;
                lower = lhs;
                return;
            }
        }
        if ((lhs instanceof FloatExpression) && (rhs instanceof IntExpression)) {
            long r = ((IntExpression) rhs).value;
            if (r != 0L) {
                ((FloatExpression) lhs).value %= r;
                lower = lhs;
                return;
            }
        }
        if ((lhs instanceof IntExpression) && (rhs instanceof FloatExpression)) {
            long l = ((IntExpression) lhs).value;
            double r = ((FloatExpression) rhs).value;
            if (r != 0D) {
                lower = new FloatExpression(expression.getPosition(), l % r);
                return;
            }
        }
        if ((lhs instanceof IntExpression) && (rhs instanceof IntExpression)) {
            long r = ((IntExpression) rhs).value;
            if (r != 0L) {
                ((IntExpression) lhs).value %= r;
                lower = lhs;
                return;
            }
        }
        lowerBinary(expression, lhs, rhs);
    }

    @Override
    public void visitReturn(ReturnStatement statement) {
        if ((statement.expr) != null) {
            statement.expr = getLowerExpression(statement.expr);
        }
        lower = statement;
    }

    @Override
    public void visitRightShift(ShiftRightExpression expression) {
        Expression lhs = getLowerExpression(expression.lhs).child();
        Expression rhs = getLowerExpression(expression.rhs).child();
        if ((lhs instanceof IntExpression) && (rhs instanceof IntExpression)) {
            ((IntExpression) lhs).value >>= ((IntExpression) rhs).value;
            lower = lhs;
            return;
        }
        lowerBinary(expression, lhs, rhs);
    }

    @Override
    public void visitString(StringExpression expression) {
        nothing(expression);
    }

    @Override
    public void visitSubtract(SubtractExpression expression) {
        Expression lhs = getLowerExpression(expression.lhs).child();
        Expression rhs = getLowerExpression(expression.rhs).child();
        if ((lhs instanceof FloatExpression) && (rhs instanceof FloatExpression)) {
            ((FloatExpression) lhs).value -= ((FloatExpression) rhs).value;
            lower = lhs;
            return;
        }
        if ((lhs instanceof FloatExpression) && (rhs instanceof IntExpression)) {
            ((FloatExpression) lhs).value -= ((IntExpression) rhs).value;
            lower = lhs;
            return;
        }
        if ((lhs instanceof IntExpression) && (rhs instanceof FloatExpression)) {
            long l = ((IntExpression) lhs).value;
            double r = ((FloatExpression) rhs).value;
            lower = new FloatExpression(expression.getPosition(), l - r);
            return;
        }
        if ((lhs instanceof IntExpression) && (rhs instanceof IntExpression)) {
            ((IntExpression) lhs).value -= ((IntExpression) rhs).value;
            lower = lhs;
            return;
        }
        lowerBinary(expression, lhs, rhs);
    }

    @Override
    public void visitSwitch(SwitchStatement statement) {
        statement.selector = getLowerExpression(statement.selector);
        statement.cases = lowerList(statement.cases);
        lower = statement;
    }

    @Override
    public void visitTernary(TernaryExpression expression) {
        Expression cond = getLowerExpression(expression.cond).child();
        Expression lhs = getLowerExpression(expression.lhs);
        Expression rhs = getLowerExpression(expression.rhs);
        if (isTrue(cond)) {
            lower = lhs;
            return;
        }
        if (isFalse(cond)) {
            lower = rhs;
            return;
        }
        expression.cond = cond;
        expression.lhs = lhs;
        expression.rhs = rhs;
        lower = expression;
    }

    @Override
    public void visitTrue(TrueExpression expression) {
        nothing(expression);
    }

    @Override
    public void visitVariable(VariableExpression expression) {
        if (constantFolding.containsKey(expression.name)) {
            lower = constantFolding.get(expression.name).copy(expression.getPosition());
        } else {
            nothing(expression);
        }
    }

    @Override
    public void visitWhile(WhileStatement statement) {
        Expression cond = getLowerExpression(statement.cond).child();
        Statement body = getLowerStatement(statement.body);

        if (isFalse(cond)) {
            lower = Statement.EMPTY;
            return;
        }
        if (isTrue(cond)) {
            cond = null; // compiler will detect this
        }
        statement.cond = cond;
        statement.body = body;
        lower = statement;
    }

    @Override
    public void visitUnused(UnusedExpression expression) {
        expression.expression = getLowerExpression(expression.expression);
        lower = expression;
    }

    private void visitAssignment(AssignmentExpression expression) {
        checkFolding(expression.var);
        expression.var = getLowerExpression(expression.var).child();
        expression.expr = getLowerExpression(expression.expr).child();
        lower = expression;
    }

    private void visitIncrease(IncreaseExpression expression) {
        checkFolding(expression.hs);
        expression.hs = getLowerExpression(expression.hs).child();
        lower = expression;
    }

    private void checkFolding(Expression expression) {
        expression = expression.child();
        if (!(expression instanceof VariableExpression)) {
            return;
        }
        if (constantFolding.containsKey(((VariableExpression) expression).name)) {
            throw new CompileError("assignment to constant is not allowed.", expression.getPosition());
        }
    }

    private void lowerBinary(BinaryExpression expression, Expression lhs, Expression rhs) {
        expression.lhs = lhs;
        expression.rhs = rhs;
        lower = expression;
    }

    private void lowerUnary(UnaryExpression expression, Expression hs) {
        expression.hs = hs;
        lower = expression;
    }

    @SuppressWarnings("unchecked")
    private <T extends Tree> List<T> lowerList(List<T> list) {
        return list.stream().map(n -> {
            n.accept(this);
            if (lower == null) // empty statement
                return (T) Statement.EMPTY;
            try {
                return (T) lower;
            } finally {
                lower = null;
            }
        }).collect(Collectors.toList());
    }

    // todo: Переместить нижние методы в TreeInfo

    private boolean isTrue(Expression expr) {
        expr = expr.child();
        if (expr instanceof StringExpression) {
            return !((StringExpression) expr).value.isEmpty();
        }
        if (expr instanceof IntExpression) {
            return ((IntExpression) expr).value != 0L;
        }
        if (expr instanceof FloatExpression) {
            return ((FloatExpression) expr).value != 0D;
        }
        return (expr instanceof TrueExpression);
    }

    private boolean isFalse(Expression expr) {
        expr = expr.child();
        if (expr instanceof StringExpression) {
            return ((StringExpression) expr).value.isEmpty();
        }
        if (expr instanceof IntExpression) {
            return ((IntExpression) expr).value == 0L;
        }
        if (expr instanceof FloatExpression) {
            return ((FloatExpression) expr).value == 0D;
        }
        return (expr instanceof FalseExpression) || (expr instanceof NullExpression);
    }

    private void setTrue(Expression expression) {
        lower = new TrueExpression(expression.getPosition());
    }

    private void setFalse(Expression expression) {
        lower = new FalseExpression(expression.getPosition());
    }

    private void nothing(Tree tree) {
        lower = tree;
    }

    private int compareLiterals(Expression a, Expression b) {
        if ((a instanceof FloatExpression) && (b instanceof FloatExpression)) {
            double l = ((FloatExpression) a).value;
            double r = ((FloatExpression) b).value;
            return compareNumbers(l, r);
        }
        if ((a instanceof FloatExpression) && (b instanceof IntExpression)) {
            double l = ((FloatExpression) a).value;
            long r = ((IntExpression) b).value;
            return compareNumbers(l, r);
        }
        if ((a instanceof IntExpression) && (b instanceof FloatExpression)) {
            long l = ((IntExpression) a).value;
            double r = ((FloatExpression) b).value;
            return compareNumbers(l, r);
        }
        if ((a instanceof IntExpression) && (b instanceof IntExpression)) {
            long l = ((IntExpression) a).value;
            long r = ((IntExpression) b).value;
            return Long.compare(l, r);
        }
        if ((a instanceof StringExpression) && (b instanceof StringExpression)) {
            String l = ((StringExpression) a).value;
            String r = ((StringExpression) b).value;
            return l.compareTo(r);
        }
        if ((a instanceof BooleanExpression) && (b instanceof BooleanExpression)) {
            return a.getClass() == b.getClass() ? 0 : 2;
        }
        if ((a instanceof NullExpression) && (b instanceof NullExpression)) {
            return 0;
        }
        return -2; // not applicable
    }

    private int compareNumbers(double a, double b) {
        if (Double.isNaN(a) || Double.isNaN(b)) {
            return 2; // automatically false
        }
        return Double.compare(a, b);
    }

    private Expression getLowerExpression(Expression expression) {
        expression.accept(this);
        try {
            return (Expression) lower;
        } finally {
            lower = null;
        }
    }

    private Statement getLowerStatement(Statement statement) {
        statement.accept(this);
        if (lower == null) { // empty
            return Statement.EMPTY;
        }
        try {
            return (Statement) lower;
        } finally {
            lower = null;
        }
    }
}