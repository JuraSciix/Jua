package jua.compiler;

import jua.compiler.Tree.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static jua.compiler.TreeInfo.*;
import static jua.compiler.Types.*;

public final class Lower extends Translator {
    
    private final ProgramLayout programLayout;

    public Lower(ProgramLayout programLayout) {
        this.programLayout = Objects.requireNonNull(programLayout);
    }

    @Override
    public void visitWhileLoop(WhileLoop tree) {
        tree.cond = translate(tree.cond);
        tree.body = translate(tree.body);
        if (isLiteralTrue(tree.cond)) {
            // while true { ... } => do { ... } while true;
            result = new DoLoop(tree.pos, tree.body, tree.cond);
        } else {
            result = tree;
        }
    }

    @Override
    public void visitVariable(Var tree) {
        String nameString = tree.name.value;
        if (programLayout.constantLiterals.containsKey(nameString)) {
            result = new Literal(tree.pos, programLayout.constantLiterals.get(nameString));
        } else {
            result = tree;
        }
    }

    @Override
    public void visitBinaryOp(BinaryOp tree) {
        tree.lhs = translate(tree.lhs);
        tree.rhs = translate(tree.rhs);

        switch (tree.tag) {

            // Лень объяснять. Лучше оставлю пример:
            // x = 'no no no';
            // println(false && x); // Lower удалит лишний false, оставив: println(x);
            // >>> no no no
            // Без оператора значение не интерпретируется генератором кода как логическое.
            // todo: Типизировать выражения. Необходимые сейчас типы: BOOLEAN, ANY.

//            case FLOW_AND:
//                result = isLiteralTrue(tree.lhs) ? tree.rhs : tree;
//                return;
//
//            case FLOW_OR:
//                result = isLiteralFalse(tree.lhs) ? tree.rhs : tree;
//                return;

            case ADD: case SUB: case MUL:
            case DIV: case REM: case SL:
            case SR: case AND: case OR:
            case XOR: case EQ: case NE:
            case GT: case GE: case LT:
            case LE:
                Expression innerLhs = stripParens(tree.lhs);

                if (innerLhs.hasTag(Tag.LITERAL)) {
                    Expression innerRhs = stripParens(tree.rhs);
                    if (innerRhs.hasTag(Tag.LITERAL)) {
                        Literal literalLhs = (Literal) innerLhs;
                        Literal literalRhs = (Literal) innerRhs;
                        Type resultType = foldBinaryOp(tree, literalLhs.type, literalRhs.type);

                        if (resultType != null) {
                            result = new Literal(tree.lhs.pos, resultType);
                            return;
                        }
                    }
                }
                break;

            case NULLCOALESCE:
                result = isLiteralNull(tree.lhs) ? tree.rhs : tree;
                return;
        }
        
        result = tree;
    }

    /**
     * Сворачивает двучлен и возвращает результат в случае успеха, или {@code null}. 
     */
    private Type foldBinaryOp(BinaryOp tree, Type lhs, Type rhs) {
        switch (tree.tag) {
            case ADD:
                if (lhs.isLong() && rhs.isLong()) {
                    return new LongType(lhs.longValue() + rhs.longValue());
                }
                if (lhs.isNumber() && rhs.isNumber()) {
                    return new DoubleType(lhs.doubleValue() + rhs.doubleValue());
                }
                if (lhs.isString() || rhs.isString()) {
                    return new StringType(lhs.stringValue() + rhs.stringValue());
                }
                break;
                
            case SUB:
                if (lhs.isLong() && rhs.isLong()) {
                    return new LongType(lhs.longValue() - rhs.longValue());
                }
                if (lhs.isNumber() && rhs.isNumber()) {
                    return new DoubleType(lhs.doubleValue() - rhs.doubleValue());
                }
                break;
                
            case MUL:
                if (lhs.isLong() && rhs.isLong()) {
                    return new LongType(lhs.longValue() * rhs.longValue());
                }
                if (lhs.isNumber() && rhs.isNumber()) {
                    return new DoubleType(lhs.doubleValue() * rhs.doubleValue());
                }
                break;
                
            case DIV:
                if (lhs.isLong() && rhs.isLong()) {
                    long divisor = rhs.longValue();
                    if (divisor == 0L) {
                        break; // Мы не можем поделить целое число на ноль
                    }
                    return new LongType(lhs.longValue() / divisor);
                }
                if (lhs.isNumber() && rhs.isNumber()) {
                    return new DoubleType(lhs.doubleValue() / rhs.doubleValue());
                }
                break;

            case REM:
                if (lhs.isLong() && rhs.isLong()) {
                    long divisor = rhs.longValue();
                    if (divisor == 0L) {
                        break; // Мы не можем взять остаток от нуля
                    }
                    return new LongType(lhs.longValue() % divisor);
                }
                if (lhs.isNumber() && rhs.isNumber()) {
                    double divisor = rhs.doubleValue();
                    if (divisor == 0.0) {
                        break; // Мы не можем взять остаток от нуля
                    }
                    return new DoubleType(lhs.doubleValue() % divisor);
                }
                break;
                
            case SL:
                if (lhs.isLong() && rhs.isLong()) {
                    return new LongType(lhs.longValue() >> rhs.longValue());
                }
                break;

            case SR:
                if (lhs.isLong() && rhs.isLong()) {
                    return new LongType(lhs.longValue() << rhs.longValue());
                }
                break;

            case AND:
                if (lhs.isLong() && rhs.isLong()) {
                    return new LongType(lhs.longValue() & rhs.longValue());
                }
                if (lhs.isBoolean() && rhs.isBoolean()) {
                    return ofBoolean(lhs.booleanValue() & rhs.booleanValue());
                }
                break;

            case OR:
                if (lhs.isLong() && rhs.isLong()) {
                    return new LongType(lhs.longValue() | rhs.longValue());
                }
                if (lhs.isBoolean() && rhs.isBoolean()) {
                    return ofBoolean(lhs.booleanValue() | rhs.booleanValue());
                }
                break;

            case XOR:
                if (lhs.isLong() && rhs.isLong()) {
                    return new LongType(lhs.longValue() ^ rhs.longValue());
                }
                if (lhs.isBoolean() && rhs.isBoolean()) {
                    return ofBoolean(lhs.booleanValue() ^ rhs.booleanValue());
                }
                break;

            case EQ: return ofBoolean(lhs.quickCompare(rhs, 2) == 0);
            case NE: return ofBoolean(lhs.quickCompare(rhs, 2) != 0);
            case GT: return ofBoolean(lhs.quickCompare(rhs, 0) > 0);
            case GE: return ofBoolean(lhs.quickCompare(rhs, -1) >= 0);
            case LT: return ofBoolean(lhs.quickCompare(rhs, 0) < 0);
            case LE: return ofBoolean(lhs.quickCompare(rhs, 1) <= 0);
        }

        return null; // Не успех
    }

    @Override
    public void visitUnaryOp(UnaryOp tree) {
        tree.expr = translate(tree.expr);

        Expression innerExpr = stripParens(tree.expr);
        if (innerExpr.hasTag(Tag.LITERAL)) {
            Literal literalTree = (Literal) innerExpr;
            Type resultType = foldUnaryOp(tree, literalTree.type);

            if (resultType != null) {
                // Пост-унарными операциями считаются только POSTINC и POSTDEC,
                // которые:
                // 1. Не сворачиваются.
                // 2. Не применяются к литералам.
                // А значит, что никогда не встречаются в этом куске кода,
                // и что проверку на пост-унарную инструкцию для вычисления
                // минимальной позиции делать не нужно - она всегда у tree.
                result = new Literal(tree.pos, resultType);
                return;
            }
        }

        result = tree;
    }

    /**
     * Сворачивает унарное выражение и возвращает: новый литерал в случае успеха, или {@code null}.
     */
    private Type foldUnaryOp(UnaryOp tree, Type type) {
        switch (tree.tag) {
            case POS:
                if (type.isNumber()) {
                    return type;
                }
                break;

            case NEG:
                if (type.isLong()) {
                    return new LongType(-type.longValue());
                }
                if (type.isDouble()) {
                    return new DoubleType(-type.doubleValue());
                }
                break;

            case NOT:
                return ofBoolean(!type.booleanValue());

            case INVERSE:
                if (type.isLong()) {
                    return new LongType(~type.longValue());
                }
                break;
        }

        return null; // Не успех
    }
}
