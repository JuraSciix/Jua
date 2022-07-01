package jua.compiler;

import jua.compiler.Tree.*;
import jua.runtime.heap.Operand;

import java.util.*;
import java.util.stream.Collectors;

public class Lower implements Visitor {

    private final Map<String, Expression> literalConstants;

    private Tree result;

    /**
     * Остаточный результат от логических выражений. Пример: <pre>{@code
     * somecall() || true; // Это выражение заведомо true,
     *                     // но выкидывать вызов функции компилятор не имеет права.
     *                     // Вызов функции здесь - как раз остаточный результат,
     *                     // который будет помещен в список операторов родительского дерева (BlockStatement).}</pre>
     */
    // По идее это должно быть списком, но на практике сюда записывается только одно значение.
    private Expression formalExpression;

    @SuppressWarnings("unchecked")
    private <T extends Tree> T foldBody(Statement body) {
        if (body == null) return null;
        body.accept(this);
        if (formalExpression != null && body.getClass() != Block.class) {
            result = new Block(formalExpression.pos, new ArrayList<Statement>() {
                {
                    add(new Discarded(formalExpression.pos, formalExpression));
                    formalExpression = null;
                    add(body);
                }
            });
        }
        try {
            return (T) result;
        } finally {
            result = null;
        }
    }

    public Lower(CodeData codeData) {
        literalConstants = new HashMap<>();
        putFoldingNames(codeData.constantNames);
    }

    private void putFoldingNames(Set<String> names) {
        names.forEach(name -> literalConstants.put(name, null));
    }

    @Override
    public void visitCompilationUnit(CompilationUnit tree) {
        // todo: task for JavaKira
    }

    @Override
    public void visitAdd(AddExpression expression) {
        visitBinaryOp(expression);
    }

    @Override
    public void visitAnd(AndExpression expression) {
        visitBinaryOp(expression);
    }

    @Override
    public void visitArrayAccess(ArrayAccess expression) {
        expression.array = getLowerExpression(expression.array);
        expression.key = getLowerExpression(expression.key);
        result = expression;
    }

    @Override
    public void visitArray(ArrayLiteral expression) {
        Map<Expression, Expression> map = new LinkedHashMap<>();
        expression.map.forEach((key, value) -> {
            if (key.isEmpty()) {
                map.put(key, getLowerExpression(value));
            } else {
                map.put(getLowerExpression(key), getLowerExpression(value));
            }
        });
        expression.map = map;
        result = expression;
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
    public void visitAssignOp(AssignExpression expression) {
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
        visitBinaryOp(expression);
    }

    @Override
    public void visitBitNot(BitNotExpression expression) {
        visitUnaryOp(expression);
    }

    @Override
    public void visitBitOr(BitOrExpression expression) { expression.lhs.accept(this);
        visitBinaryOp(expression);
    }

    @Override
    public void visitBitXor(BitXorExpression expression) {
        visitBinaryOp(expression);
    }

    @Override
    public void visitBlock(Block statement) {
        // special case
        ListIterator<Statement> iterator = statement.statements.listIterator();
        Expression prevResidual = formalExpression;
        formalExpression = null;
        while (iterator.hasNext()) {
            Statement lower = getLowerStatement(iterator.next());
            if (formalExpression != null) {
                iterator.remove();
                iterator.add(
                        // Остаточный результат заведомо является unused
                        new Discarded(formalExpression.pos, formalExpression));
                formalExpression = null;
                if (lower != null && !lower.hasTag(Tag.EMPTY))
                    iterator.add(lower);
            } else {
                if (lower == null || lower.hasTag(Tag.EMPTY))
                    iterator.remove();
                else
                    iterator.set(lower);
            }
        }
        result = statement;
        formalExpression = prevResidual;
    }

    @Override
    public void visitBreak(Break statement) {
        nothing(statement);
    }

    @Override
    public void visitCase(Case statement) {
        if (statement.expressions != null) // is not default case?
            statement.expressions = lowerList(statement.expressions);
        statement.body = getLowerStatement(statement.body);
        result = statement;
    }

    @Override
    public void visitClone(CloneExpression expression) {
        lowerUnary(expression, getLowerExpression(expression.hs).child());
    }

    @Override
    public void visitConstantDeclare(ConstantDecl statement) {
        int size = statement.names.size() & statement.expressions.size();
        for (int i = 0; i < size; i++) {
            String name = statement.names.get(i);
            if (statement.names.indexOf(name) != statement.names.lastIndexOf(name)) {
                throw new CompileError("duplicate name '" + name + "'.", statement.pos);
            }
            Expression expr = getLowerExpression(statement.expressions.get(i)).child();
            if (expr.isLiteral() ||
                    (expr instanceof NullExpression) ||
                    (expr instanceof BooleanExpression)) {
                literalConstants.put(name, expr);
            }
            statement.expressions.set(i, expr);
        }
        result = statement;
    }

    @Override
    public void visitContinue(Continue statement) {
        nothing(statement);
    }

    @Override
    public void visitDivide(DivideExpression expression) {
        visitBinaryOp(expression);
    }

    @Override
    public void visitDoLoop(DoLoop tree) {
        tree.body = foldBody(tree.body);
        tree.cond = getLowerExpression(tree.cond);

        if (isTrue(tree.cond)) {
            tree.cond = null; // codegen will handle it.
        } else if (isFalse(tree.cond)) {
            result = tree.body;
            return;
        }
        result = tree;
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
    public void visitFallthrough(Fallthrough statement) {
        nothing(statement);
    }

    @Override
    public void visitFalse(FalseExpression expression) {
        visitLiteral(expression);
    }

    @Override
    public void visitFloat(FloatExpression expression) {
        visitLiteral(expression);
    }

    @Override
    public void visitFor(ForLoop tree) {
        if (tree.init != null) tree.init = lowerList(tree.init);

        tree.cond = getLowerExpression(tree.cond);

        if (isFalse(tree.cond)) {
            // todo: Возможно, здесь стоит использовать CommaExpression
            result = new Block(tree.pos, tree.init.stream()
                    .map(expr -> (Statement) expr)
                    .collect(Collectors.toList()));
            return;
        }

        if (isTrue(tree.cond)) tree.cond = null; // codegen will handle it.

        if (tree.step != null) tree.step = lowerList(tree.step);

        tree.body = foldBody(tree.body);
        result = tree;
    }

    @Override
    public void visitInvocation(Invocation expression) {
        expression.args = lowerList(expression.args);
        result = expression;
    }

    @Override
    public void visitFunctionDecl(FunctionDecl statement) {
        statement.defaults = lowerList(statement.defaults);
        statement.body = foldBody(statement.body);
        result = statement;
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
    public void visitIf(If tree) {
        tree.cond = getLowerExpression(tree.cond);

        if (isTrue(tree.cond)) {
            result = foldBody(tree.body);
            return;
        }
        if (isFalse(tree.cond)) {
            result = foldBody(tree.elseBody);
            return;
        }
        tree.body = foldBody(tree.body);
        tree.elseBody = foldBody(tree.elseBody);
        result = tree;
    }

    @Override
    public void visitInt(IntExpression expression) {
        visitLiteral(expression);
    }

    @Override
    public void visitLeftShift(ShiftLeftExpression expression) {
        visitBinaryOp(expression);
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
        visitBinaryOp(expression);
    }

    @Override
    public void visitMultiply(MultiplyExpression expression) {
        visitBinaryOp(expression);
    }

    @Override
    public void visitNegative(NegativeExpression expression) {
        visitUnaryOp(expression);
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
            result = rhs;
        } else {
            lowerBinary(expression, lhs, rhs);
        }
    }

    @Override
    public void visitNull(NullExpression expression) {
        visitLiteral(expression);
    }

    @Override
    public void visitOr(OrExpression expression) {
        Expression lhs = getLowerExpression(expression.lhs).child();
        if (isTrue(lhs)) {
            setTrue(expression);
            return;
        }
        if (isFalse(lhs)) {
            result = getLowerExpression(expression.rhs);
            return;
        }
        Expression rhs = getLowerExpression(expression.rhs).child();
        if (isFalse(rhs)) {
            result = lhs;
            return;
        }
        if (isTrue(rhs)) {
            formalExpression = lhs;
            setTrue(expression);
            return;
        }
        lowerBinary(expression, lhs, rhs);
    }

    @Override
    public void visitParens(Parens expression) {
        // Удаление скобок
        result = getLowerExpression(expression.expr);
    }

    @Override
    public void visitPositive(PositiveExpression expression) {
        Expression hs = getLowerExpression(expression.hs).child();

        if ((hs instanceof FloatExpression) || (hs instanceof IntExpression)) {
            result = hs;
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

    @Deprecated
    @Override
    public void visitPrintln(PrintlnStatement statement) {
        throw new AssertionError("deprecated");
    }

    @Deprecated
    @Override
    public void visitPrint(PrintStatement statement) {
        throw new AssertionError("deprecated");
    }

    @Override
    public void visitRemainder(RemainderExpression expression) {
        visitBinaryOp(expression);
    }

    @Override
    public void visitReturn(Return statement) {
        if ((statement.expr) != null) {
            statement.expr = getLowerExpression(statement.expr);
        }
        result = statement;
    }

    @Override
    public void visitRightShift(ShiftRightExpression expression) {
        visitBinaryOp(expression);
    }

    @Override
    public void visitString(StringExpression expression) {
        visitLiteral(expression);
    }

    @Override
    public void visitSubtract(SubtractExpression expression) {
        visitBinaryOp(expression);
    }

    @Override
    public void visitSwitch(Switch statement) {
        statement.selector = getLowerExpression(statement.selector);
        statement.cases = lowerList(statement.cases);
        result = statement;
    }

    @Override
    public void visitTernaryOp(TernaryOp expression) {
        Expression cond = getLowerExpression(expression.cond).child();
        Expression lhs = getLowerExpression(expression.lhs);
        Expression rhs = getLowerExpression(expression.rhs);
        if (isTrue(cond)) {
            result = lhs;
            return;
        }
        if (isFalse(cond)) {
            result = rhs;
            return;
        }
        expression.cond = cond;
        expression.lhs = lhs;
        expression.rhs = rhs;
        result = expression;
    }

    @Override
    public void visitTrue(TrueExpression expression) {
        visitLiteral(expression);
    }

    @Override
    public void visitVariable(Var expression) {
        if (literalConstants.containsKey(expression.name)) {
            result = literalConstants.get(expression.name).copy(expression.pos);
        } else {
            nothing(expression);
        }
    }

    @Override
    public void visitWhileLoop(WhileLoop tree) {
        tree.cond = getLowerExpression(tree.cond);

        if (isTrue(tree.cond)) {
            tree.cond = null; // codegen will handle it.
        } else if (isFalse(tree.cond)) {
            result = null;
            return;
        }
        tree.body = foldBody(tree.body);
        result = tree;
    }

    @Override
    public void visitDiscarded(Discarded expression) {
        expression.expression = getLowerExpression(expression.expression);
        result = expression;
    }

    @Override
    public void visitBinaryOp(BinaryOp tree) {
        if (!tree.lhs.hasTag(Tag.LITERAL) || !tree.rhs.hasTag(Tag.LITERAL)) {
            result = tree;
            return;
        }

        switch (tree.tag) {
            case SL:     result = foldShiftLeft(tree);          break;
            case SR:     result = foldShiftRight(tree);         break;
            case BITAND: result = foldBitwiseDisjunction(tree); break;
            case BITOR:  result = foldBitwiseConjunction(tree); break;
            case BITXOR: result = foldBitwiseXor(tree);         break;
            case ADD:    result = foldAddition(tree);           break;
            case SUB:    result = foldSubtraction(tree);        break;
            case MUL:    result = foldMultiplication(tree);     break;
            case DIV:    result = foldDivision(tree);           break;
            case REM:    result = foldRemainder(tree);          break;
            default:     result = tree;
        }
    }

    @Override
    public void visitUnaryOp(UnaryOp tree) {
        result = tree;
    }

    @Override
    public void visitAssignOp(AssignOp tree) {
        // todo: task for JavaKira
        result = tree;
    }

    @Override
    public void visitLiteral(Literal tree) {
        result = tree;
    }

    private void visitAssignment(AssignOp expression) {
        checkFolding(expression.var);
        expression.var = getLowerExpression(expression.var).child();
        expression.expr = getLowerExpression(expression.expr).child();
        result = expression;
    }

    private void visitIncrease(IncreaseExpression expression) {
        checkFolding(expression.hs);
        expression.hs = getLowerExpression(expression.hs).child();
        result = expression;
    }

    private void checkFolding(Expression expression) {
        expression = expression.child();
        if (!(expression instanceof Var)) {
            return;
        }
        if (literalConstants.containsKey(((Var) expression).name)) {
            throw new CompileError("assignment to constant is not allowed.", expression.pos);
        }
    }

    private void lowerBinary(BinaryOp expression, Expression lhs, Expression rhs) {
        expression.lhs = lhs;
        expression.rhs = rhs;
        result = expression;
    }

    private void lowerUnary(UnaryOp expression, Expression hs) {
        expression.hs = hs;
        result = expression;
    }

    @SuppressWarnings("unchecked")
    private <T extends Tree> List<T> lowerList(List<T> list) {
        return list.stream().map(n -> {
            n.accept(this);
            if (result == null) // empty statement
                return (T) Statement.EMPTY;
            try {
                return (T) result;
            } finally {
                result = null;
            }
        }).collect(Collectors.toList());
    }

    // todo: Переместить нижние методы в TreeInfo

    private boolean isTrue(Expression expr) {
        return false; // todo
    }

    private boolean isFalse(Expression expr) {
        return false; // todo
    }

    private void setTrue(Expression expression) {
        result = new TrueExpression(expression.pos);
    }

    private void setFalse(Expression expression) {
        result = new FalseExpression(expression.pos);
    }

    private void nothing(Tree tree) {
        result = tree;
    }

    private int compareLiterals(Expression a, Expression b) {
        return -2; // todo
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
            return (Expression) result;
        } finally {
            result = null;
        }
    }

    private Statement getLowerStatement(Statement statement) {
        statement.accept(this);
        if (result == null) { // empty
            return Statement.EMPTY;
        }
        try {
            return (Statement) result;
        } finally {
            result = null;
        }
    }

    private static Expression foldShiftLeft(BinaryOp tree) {
        Operand lhs = TreeInfo.resolveLiteral((Literal) tree.lhs);
        Operand rhs = TreeInfo.resolveLiteral((Literal) tree.rhs);

        if (lhs.isLong() && rhs.isLong()) {
            return new Literal(tree.lhs.pos, lhs.longValue() << rhs.longValue());
        }
        return tree;
    }

    private static Expression foldShiftRight(BinaryOp tree) {
        Operand lhs = TreeInfo.resolveLiteral((Literal) tree.lhs);
        Operand rhs = TreeInfo.resolveLiteral((Literal) tree.rhs);

        if (lhs.isLong() && rhs.isLong()) {
            return new Literal(tree.lhs.pos, lhs.longValue() >> rhs.longValue());
        }
        return tree;
    }

    private static Expression foldBitwiseDisjunction(BinaryOp tree) {
        Operand lhs = TreeInfo.resolveLiteral((Literal) tree.lhs);
        Operand rhs = TreeInfo.resolveLiteral((Literal) tree.rhs);

        if (lhs.isLong() && rhs.isLong()) {
            return new Literal(tree.lhs.pos, lhs.longValue() & rhs.longValue());
        }
        if (lhs.isBoolean() && rhs.isBoolean()) {
            return new Literal(tree.lhs.pos, lhs.booleanValue() & rhs.booleanValue());
        }
        return tree;
    }

    private static Expression foldBitwiseConjunction(BinaryOp tree) {
        Operand lhs = TreeInfo.resolveLiteral((Literal) tree.lhs);
        Operand rhs = TreeInfo.resolveLiteral((Literal) tree.rhs);

        if (lhs.isLong() && rhs.isLong()) {
            return new Literal(tree.lhs.pos, lhs.longValue() | rhs.longValue());
        }
        if (lhs.isBoolean() && rhs.isBoolean()) {
            return new Literal(tree.lhs.pos, lhs.booleanValue() | rhs.booleanValue());
        }
        return tree;
    }

    private static Expression foldBitwiseXor(BinaryOp tree) {
        Operand lhs = TreeInfo.resolveLiteral((Literal) tree.lhs);
        Operand rhs = TreeInfo.resolveLiteral((Literal) tree.rhs);

        if (lhs.isLong() && rhs.isLong()) {
            return new Literal(tree.lhs.pos, lhs.longValue() ^ rhs.longValue());
        }
        if (lhs.isBoolean() && rhs.isBoolean()) {
            return new Literal(tree.lhs.pos, lhs.booleanValue() ^ rhs.booleanValue());
        }
        return tree;
    }


    private static Expression foldAddition(BinaryOp tree) {
        Operand lhs = TreeInfo.resolveLiteral((Literal) tree.lhs);
        Operand rhs = TreeInfo.resolveLiteral((Literal) tree.rhs);

        if (lhs.isLong() && rhs.isLong()) {
            return new Literal(tree.lhs.pos, lhs.longValue() + rhs.longValue());
        }
        if (lhs.isNumber() && rhs.isNumber()) {
            return new Literal(tree.lhs.pos, lhs.doubleValue() + rhs.doubleValue());
        }
        return tree;
    }

    private static Expression foldDivision(BinaryOp tree) {
        Operand lhs = TreeInfo.resolveLiteral((Literal) tree.lhs);
        Operand rhs = TreeInfo.resolveLiteral((Literal) tree.rhs);

        if (lhs.isLong() && rhs.isLong()) {
            if (rhs.longValue() == 0L) return tree; // do not fold division by zero!
            return new Literal(tree.lhs.pos, lhs.longValue() / rhs.longValue());
        }
        if (lhs.isNumber() && rhs.isNumber()) {
            return new Literal(tree.lhs.pos, lhs.doubleValue() / rhs.doubleValue());
        }
        return tree;
    }

    private static Expression foldSubtraction(BinaryOp tree) {
        Operand lhs = TreeInfo.resolveLiteral((Literal) tree.lhs);
        Operand rhs = TreeInfo.resolveLiteral((Literal) tree.rhs);

        if (lhs.isLong() && rhs.isLong()) {
            return new Literal(tree.lhs.pos, lhs.longValue() - rhs.longValue());
        }
        if (lhs.isNumber() && rhs.isNumber()) {
            return new Literal(tree.lhs.pos, lhs.doubleValue() - rhs.doubleValue());
        }
        return tree;
    }

    private static Expression foldMultiplication(BinaryOp tree) {
        Operand lhs = TreeInfo.resolveLiteral((Literal) tree.lhs);
        Operand rhs = TreeInfo.resolveLiteral((Literal) tree.rhs);

        if (lhs.isLong() && rhs.isLong()) {
            return new Literal(tree.lhs.pos, lhs.longValue() * rhs.longValue());
        }
        if (lhs.isNumber() && rhs.isNumber()) {
            return new Literal(tree.lhs.pos, lhs.doubleValue() * rhs.doubleValue());
        }
        return tree;
    }

    private static Expression foldRemainder(BinaryOp tree) {
        Operand lhs = TreeInfo.resolveLiteral((Literal) tree.lhs);
        Operand rhs = TreeInfo.resolveLiteral((Literal) tree.rhs);

        if (lhs.isLong() && rhs.isLong()) {
            if (rhs.longValue() == 0L) return tree; // do not fold division by zero!
            return new Literal(tree.lhs.pos, lhs.longValue() % rhs.longValue());
        }
        if (lhs.isNumber() && rhs.isNumber()) {
            if (rhs.doubleValue() == 0.0D) return tree; // do not fold division by zero!
            return new Literal(tree.lhs.pos, lhs.doubleValue() % rhs.doubleValue());
        }
        return tree;
    }
}