package jua.compiler;

import jua.compiler.Tree.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class Lower extends Translator {

    private final Types types;

    private final Map<String, Literal> constantLiterals = new HashMap<>();

    public Lower(Types types) {
        this.types = Objects.requireNonNull(types, "Types is null");
    }

    @Override
    public void visitConstDef(ConstDef tree) {
        for (ConstDef.Definition def : tree.defs) {
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
            result = new Literal(tree.pos, constantLiteral.type);
        } else {
            result = tree;
        }
    }

    @Override
    public void visitMemberAccess(MemberAccess tree) {
        // todo: Gen пока плохо умеет работать с FieldAccess --
        //  1. Исправить это
        //  2. Удалить это преобразование
        result = new ArrayAccess(tree.pos, translate(tree.expr),
                new Literal(tree.member.pos, types.asString(tree.member.value)));
    }

    @Override
    public void visitParens(Parens tree) {
        // Удаляем скобки.
        result = translate(tree.expr);
    }

    @Override
    public void visitCompoundAssign(CompoundAssign tree) {
        Expression dst = translate(tree.dst);
        Expression src = translate(tree.src);
        // По-хорошему мы должны избегать дублирования кода.
        // Например, попытка преобразования следующего кода:
        //  a[sqrt(x*x+y*y)] ??= z;
        // В следующий вид:
        //  a[sqrt(x*x+y*y)] = a[sqrt(x*x+y*y)] ?? z;
        // Сулит, как видно, сложными дублированиями.
        if (tree.hasTag(Tag.ASG_NULLCOALESCE) || !dst.hasTag(Tag.VARIABLE)) {
            tree.dst = dst;
            tree.src = translate(src);
            result = tree;
            return;
        }
        // Преобразуем выражение типов {a+=b} в {a=a+b}
        result = new Assign(tree.pos,
                dst,
                new BinaryOp(src.pos,
                        TreeInfo.tagWithoutAsg(tree.tag),
                        dst,
                        src));
    }

    @Override
    public void visitBinaryOp(BinaryOp tree) {
        tree.lhs = translate(tree.lhs);
        tree.rhs = translate(tree.rhs);

        if (tree.lhs.getTag() != Tag.LITERAL || tree.rhs.getTag() != Tag.LITERAL) {
            result = tree;
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
//            default: throw new IllegalArgumentException("Could fold binary " + tree.tag);
            default:result = tree;
        }
    }

    @Override
    public void visitUnaryOp(UnaryOp tree) {
        tree.expr = translate(tree.expr);

        if (tree.expr.getTag() == Tag.LITERAL) {
            result = foldUnary(tree);
        } else {
            result = tree;
        }
    }

    private Expression foldUnary(UnaryOp tree) {
        Literal literal = (Literal) tree.expr;

        switch (tree.tag) {
            case NOT:
                if (literal.type.isBoolean())
                    return new Literal(tree.pos, types.asBoolean(!literal.type.booleanValue()));
                else return tree;
            case NEG:
                if (literal.type.isLong())
                    return new Literal(tree.pos, types.asLong(-literal.type.longValue()));
                else if (literal.type.isNumber())
                    return new Literal(tree.pos, types.asDouble(-literal.type.doubleValue()));
                else return tree;
            case POS:
                if (literal.type.isLong())
                    return new Literal(tree.pos, types.asLong(Math.abs(literal.type.longValue())));
                else if (literal.type.isNumber())
                    return new Literal(tree.pos, types.asDouble(Math.abs(literal.type.doubleValue())));
                else return tree;
//            default: throw new IllegalArgumentException(tree.getTag() + " is not unary operation");
            default: return tree;
        }
    }

    private Expression foldShift(BinaryOp tree) {
        Literal left = (Literal) tree.lhs;
        Literal right = (Literal) tree.rhs;
        if (!left.type.isLong() || !right.type.isLong())
            return tree;

        switch (tree.getTag()) {
            case SL:
                return new Literal(left.pos, types.asLong(left.type.longValue() << right.type.longValue()));
            case SR:
                return new Literal(left.pos, types.asLong(left.type.longValue() >> right.type.longValue()));
//            default: throw new IllegalArgumentException(tree.getTag() + " is not shift operation");
            default:return tree;
        }
    }

    private Expression foldBitwise(BinaryOp tree) {
        Literal left = (Literal) tree.lhs;
        Literal right = (Literal) tree.rhs;
        switch (tree.getTag()) {
            case AND:
                if (left.type.isLong() && right.type.isLong())
                    return new Literal(left.pos, types.asLong(left.type.longValue() & right.type.longValue()));
                else if (left.type.isBoolean() && right.type.isBoolean())
                    return new Literal(left.pos, types.asBoolean(left.type.booleanValue() & right.type.booleanValue()));
                else
                    return tree;
            case OR:
                if (left.type.isLong() && right.type.isLong())
                    return new Literal(left.pos, types.asLong(left.type.longValue() | right.type.longValue()));
                else if (left.type.isBoolean() && right.type.isBoolean())
                    return new Literal(left.pos, types.asBoolean(left.type.booleanValue() | right.type.booleanValue()));
                else
                    return tree;
            case XOR:
                if (left.type.isLong() && right.type.isLong())
                    return new Literal(left.pos, types.asLong(left.type.longValue() ^ right.type.longValue()));
                else if (left.type.isBoolean() && right.type.isBoolean())
                    return new Literal(left.pos, types.asBoolean(left.type.booleanValue() ^ right.type.booleanValue()));
                else
                    return tree;
            default: throw new IllegalArgumentException(tree.getTag() + " is not bitwise operation");
        }
    }

    private Expression foldRelational(BinaryOp tree) {
        Literal left = (Literal) tree.lhs;
        Literal right = (Literal) tree.rhs;

        switch (tree.getTag()) {
            case LE:
                return new Literal(left.pos, types.asBoolean(left.type.doubleValue() <= right.type.doubleValue()));
            case LT:
                return new Literal(left.pos, types.asBoolean(left.type.doubleValue() < right.type.doubleValue()));
            case GT:
                return new Literal(left.pos, types.asBoolean(left.type.doubleValue() > right.type.doubleValue()));
            case GE:
                return new Literal(left.pos, types.asBoolean(left.type.doubleValue() >= right.type.doubleValue()));
            case NE:
                return new Literal(left.pos, types.asBoolean(left.type.doubleValue() != right.type.doubleValue()));
            case EQ:
                return new Literal(left.pos, types.asBoolean(left.type.doubleValue() == right.type.doubleValue()));
            default: throw new IllegalArgumentException(tree.getTag() + " is not relational operation");
        }
    }

    private Expression foldArithmetic(BinaryOp tree) {
        Literal left = (Literal) tree.lhs;
        Literal right = (Literal) tree.rhs;

        switch (tree.getTag()) {
            case ADD:
                if (left.type.isLong() && right.type.isLong())
                    return new Literal(left.pos, types.asLong(left.type.longValue() + right.type.longValue()));
                else if (left.type.isNumber() && right.type.isNumber())
                    return new Literal(left.pos, types.asDouble(left.type.doubleValue() + right.type.doubleValue()));
                return tree;
            case MUL:
                if (left.type.isLong() && right.type.isLong())
                    return new Literal(left.pos, types.asLong(left.type.longValue() * right.type.longValue()));
                else if (left.type.isNumber() && right.type.isNumber())
                    return new Literal(left.pos, types.asDouble(left.type.doubleValue() * right.type.doubleValue()));
                return tree;
            case SUB:
                if (left.type.isLong() && right.type.isLong())
                    return new Literal(left.pos, types.asLong(left.type.longValue() - right.type.longValue()));
                else if (left.type.isNumber() && right.type.isNumber())
                    return new Literal(left.pos, types.asDouble(left.type.doubleValue() - right.type.doubleValue()));
                return tree;
            case REM:
                if (left.type.isLong() && right.type.isLong())
                    return new Literal(left.pos, types.asLong(left.type.longValue() % right.type.longValue()));
                else if (left.type.isNumber() && right.type.isNumber())
                    return new Literal(left.pos, types.asDouble(left.type.doubleValue() % right.type.doubleValue()));
                return tree;
            case DIV:
                if (left.type.isLong() && right.type.isLong()) {
                    if (right.type.longValue() == 0)
                        return tree;
                    return new Literal(left.pos, types.asLong(left.type.longValue() / right.type.longValue()));
                } else if (left.type.isNumber() && right.type.isNumber())
                    return new Literal(left.pos, types.asDouble(left.type.doubleValue() / right.type.doubleValue()));
                return tree;
            default: throw new IllegalArgumentException(tree.getTag() + " is not arithmetic operation");
        }
    }

    private Expression foldAnd(BinaryOp tree) {
        Literal left = (Literal) tree.lhs;
        Literal right = (Literal) tree.rhs;
        return new Literal(left.pos, types.asBoolean(left.type.booleanValue() && right.type.booleanValue()));
    }

    private Expression foldOr(BinaryOp tree) {
        Literal left = (Literal) tree.lhs;
        Literal right = (Literal) tree.rhs;
        return new Literal(left.pos, types.asBoolean(left.type.booleanValue() || right.type.booleanValue()));
    }
}
