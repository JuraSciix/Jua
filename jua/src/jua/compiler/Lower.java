package jua.compiler;

import jua.compiler.Tree.*;
import jua.runtime.heap.Operand;

import java.util.HashMap;
import java.util.Map;

public final class Lower extends Translator {

    private final Map<String, Literal> constantLiterals = new HashMap<>();

    @Override
    public void visitConstDef(ConstDef tree) {
        for (Definition def : tree.defs) {
            Expression expr = translate(def.expr);
            if (expr != null && expr.getTag() == Tag.LITERAL) {
                // Сохраняем объект литерала для встраивания.
                Literal literal = (Literal) expr;
                constantLiterals.put(def.name.value, literal);
            }
            def.expr = expr;
        }
        result = tree;
    }

    @Override
    public void visitVariable(Var tree) {
        String var = tree.name.value;
        if (constantLiterals.containsKey(var)) {
            // Подставляем значение константы вместо переменной.
            Literal constantLiteral = constantLiterals.get(var);
            result = new Literal(tree.pos, constantLiteral.value);
        } else {
            result = tree;
        }
    }

    @Override
    public void visitParens(Parens tree) {
        // Удаляем скобки.
        result = translate(tree.expr);
    }

    @Override
    public void visitAssignOp(AssignOp tree) {
        // todo: Удаление выражений {a=a}
        // todo: Не преобразовывать ASG_NULLCOALESCE
        if (tree.getTag() == Tag.ASSIGN) {
            result = tree;
            return;
        }

        // Преобразуем выражение типов {a+=b} в {a=a+b}
        result = new AssignOp(tree.pos, Tag.ASSIGN,
                tree.dst,
                new BinaryOp(tree.src.pos, TreeInfo.tagWithoutAsg(tree.tag),
                        tree.dst,
                        tree.src));
    }

    @Override
    public void visitBinaryOp(BinaryOp tree) {
        if (tree.lhs.getTag() != Tag.LITERAL || tree.rhs.getTag() != Tag.LITERAL) {
            super.visitBinaryOp(tree);
            return;
        }

        switch (tree.tag) {
            case SL:  case SR:
                result = foldShift(tree);                        break;
            case ADD: case SUB: case MUL: case DIV: case REM:
                result = foldArithmetic(tree);                   break;
            case AND: case OR: case XOR:
                result = foldBitwise(tree);                      break;
            case LE: case EQ: case GE: case NE: case GT: case LT:
                result = foldRelational(tree);                   break;
            case FLOW_AND:
                result = foldAnd(tree);                          break;
            case FLOW_OR:
                result = foldOr(tree);                          break;
            default: throw new IllegalArgumentException("Could fold binary " + tree.tag);
        }
    }

    @Override
    public void visitUnaryOp(UnaryOp tree) {
        Expression foldResult = foldUnary(tree);
        if (foldResult != tree) {
            result = foldResult;
            return;
        }

        super.visitUnaryOp(tree);
    }

    private static Expression foldUnary(UnaryOp tree) {
        Expression lower = tree;
        Expression oldLower;
        Expression result = tree;
        while (lower.getTag() == Tag.NOT) {
            oldLower = lower;
            lower = ((UnaryOp) lower).expr;
            if (oldLower.getTag() == Tag.NOT && lower.getTag() == Tag.NOT) {
                result = ((UnaryOp) lower).expr;
                lower = ((UnaryOp) lower).expr;
            }
        }

        lower = tree; // начинаем заново
        while (lower.getTag() == Tag.NEG) {
            oldLower = lower;
            lower = ((UnaryOp) lower).expr;
            if (oldLower.getTag() == Tag.NEG && lower.getTag() == Tag.NEG) {
                result = ((UnaryOp) lower).expr;
                lower = ((UnaryOp) lower).expr;
            }
        }

        return result;
    }

    private static Expression foldShift(BinaryOp tree) {
        Literal left = (Literal) tree.lhs;
        Literal right = (Literal) tree.rhs;
        if (!left.isInteger() || !right.isInteger())
            return tree;

        switch (tree.getTag()) {
            case SL: return new Literal(left.pos, left.longValue() << right.longValue());
            case SR: return new Literal(left.pos, left.longValue() >> right.longValue());
            default: throw new IllegalArgumentException(tree.getTag() + " is not shift operation");
        }
    }

    private static Expression foldBitwise(BinaryOp tree) {
        Literal left = (Literal) tree.lhs;
        Literal right = (Literal) tree.rhs;
        switch (tree.getTag()) {
            case AND:
                if (left.isInteger() && right.isInteger())
                    return new Literal(left.pos, left.longValue() & right.longValue());
                else if (left.isBoolean() && right.isBoolean())
                    return new Literal(left.pos, left.booleanValue() & right.booleanValue());
                else
                    return tree;
            case OR:
                if (left.isInteger() && right.isInteger())
                    return new Literal(left.pos, left.longValue() ^ right.longValue());
                else if (left.isBoolean() && right.isBoolean())
                    return new Literal(left.pos, left.booleanValue() ^ right.booleanValue());
                else
                    return tree;
            case XOR:
                if (left.isInteger() && right.isInteger())
                    return new Literal(left.pos, left.longValue() | right.longValue());
                else if (left.isBoolean() && right.isBoolean())
                    return new Literal(left.pos, left.booleanValue() | right.booleanValue());
                else
                    return tree;
            default: throw new IllegalArgumentException(tree.getTag() + " is not bitwise operation");
        }
    }

    private static Expression foldRelational(BinaryOp tree) {
        Literal left = (Literal) tree.lhs;
        Literal right = (Literal) tree.rhs;

        switch (tree.getTag()) {
            case LE: return new Literal(left.pos, left.doubleValue() <= right.doubleValue());
            case LT: return new Literal(left.pos, left.doubleValue() < right.doubleValue());
            case GT: return new Literal(left.pos, left.doubleValue() > right.doubleValue());
            case GE: return new Literal(left.pos, left.doubleValue() >= right.doubleValue());
            case NE: return new Literal(left.pos, left.doubleValue() != right.doubleValue());
            case EQ: return new Literal(left.pos, left.doubleValue() == right.doubleValue());
            default: throw new IllegalArgumentException(tree.getTag() + " is not relational operation");
        }
    }

    private static Expression foldArithmetic(BinaryOp tree) {
        Literal left = (Literal) tree.lhs;
        Literal right = (Literal) tree.rhs;

        switch (tree.getTag()) {
            case ADD:
                if (left.isInteger() && right.isInteger())
                    return new Literal(left.pos, left.longValue() + right.longValue());
                else if (left.isNumber() && right.isNumber())
                    return new Literal(left.pos, left.doubleValue() + right.doubleValue());
                return tree;
            case MUL:
                if (left.isInteger() && right.isInteger())
                    return new Literal(left.pos, left.longValue() * right.longValue());
                else if (left.isNumber() && right.isNumber())
                    return new Literal(left.pos, left.doubleValue() * right.doubleValue());
                return tree;
            case SUB:
                if (left.isInteger() && right.isInteger())
                    return new Literal(left.pos, left.longValue() - right.longValue());
                else if (left.isNumber() && right.isNumber())
                    return new Literal(left.pos, left.doubleValue() - right.doubleValue());
                return tree;
            case REM:
                if (left.isInteger() && right.isInteger())
                    return new Literal(left.pos, left.longValue() % right.longValue());
                else if (left.isNumber() && right.isNumber())
                    return new Literal(left.pos, left.doubleValue() % right.doubleValue());
                return tree;
            case DIV:
                if (left.isInteger() && right.isInteger()) {
                    if (right.longValue() == 0)
                        return tree;
                    return new Literal(left.pos, left.longValue() / right.longValue());
                } else if (left.isNumber() && right.isNumber())
                    return new Literal(left.pos, left.doubleValue() / right.doubleValue());
                return tree;
            default: throw new IllegalArgumentException(tree.getTag() + " is not arithmetic operation");
        }
    }

    private static Expression foldAnd(BinaryOp tree) {
        Literal left = (Literal) tree.lhs;
        Literal right = (Literal) tree.rhs;
        return new Literal(left.pos, left.booleanValue() && right.booleanValue());
    }

    private static Expression foldOr(BinaryOp tree) {
        Literal left = (Literal) tree.lhs;
        Literal right = (Literal) tree.rhs;
        return new Literal(left.pos, left.booleanValue() || right.booleanValue());
    }
}
