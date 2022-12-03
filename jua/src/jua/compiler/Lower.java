package jua.compiler;

import jua.compiler.Tree.*;
import jua.compiler.Types.Type;
import jua.util.Assert;

import java.util.HashMap;
import java.util.Map;

import static jua.compiler.TreeInfo.*;

public final class Lower extends Translator {
    
    private final Map<String, Type> constantLiterals = new HashMap<>();
    
    Types types;
    
    @Override
    public void visitCompilationUnit(CompilationUnit tree) {
        Assert.check(types == null);
        types = tree.code.getTypes();
        super.visitCompilationUnit(tree);
        types = null;
    }

    @Override
    public void visitConstDef(ConstDef tree) {
        for (ConstDef.Definition def : tree.defs) {
            def.expr = translate(def.expr);
            
            if (stripParens(def.expr).hasTag(Tag.LITERAL)) {
                Literal literalTree = (Literal) def.expr;
                constantLiterals.put(def.name.value, literalTree.type);
            }
        }
        
        result = tree;
    }

    @Override
    public void visitFuncDef(FuncDef tree) {
        Types prevTypes = types;
        try {
            types = tree.code.getTypes();
            super.visitFuncDef(tree);
        } finally {
            types = prevTypes;
        }
    }

    @Override
    public void visitVariable(Var tree) {
        String nameString = tree.name.value;
        if (constantLiterals.containsKey(nameString)) {
            result = new Literal(tree.pos, constantLiterals.get(nameString).copy(types));
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
                    return types.asLong(lhs.longValue() + rhs.longValue());
                }
                if (lhs.isNumber() && rhs.isNumber()) {
                    return types.asDouble(lhs.doubleValue() + rhs.doubleValue());
                }
                if (lhs.isString() || rhs.isString()) {
                    return types.asString(lhs.stringValue() + rhs.stringValue());
                }
                break;
                
            case SUB:
                if (lhs.isLong() && rhs.isLong()) {
                    return types.asLong(lhs.longValue() - rhs.longValue());
                }
                if (lhs.isNumber() && rhs.isNumber()) {
                    return types.asDouble(lhs.doubleValue() - rhs.doubleValue());
                }
                break;
                
            case MUL:
                if (lhs.isLong() && rhs.isLong()) {
                    return types.asLong(lhs.longValue() * rhs.longValue());
                }
                if (lhs.isNumber() && rhs.isNumber()) {
                    return types.asDouble(lhs.doubleValue() * rhs.doubleValue());
                }
                break;
                
            case DIV:
                if (lhs.isLong() && rhs.isLong()) {
                    long divisor = rhs.longValue();
                    if (divisor == 0L) {
                        break; // Мы не можем поделить целое число на ноль
                    }
                    return types.asLong(lhs.longValue() / divisor);
                }
                if (lhs.isNumber() && rhs.isNumber()) {
                    return types.asDouble(lhs.doubleValue() / rhs.doubleValue());
                }
                break;

            case REM:
                if (lhs.isLong() && rhs.isLong()) {
                    long divisor = rhs.longValue();
                    if (divisor == 0L) {
                        break; // Мы не можем взять остаток от нуля
                    }
                    return types.asLong(lhs.longValue() % divisor);
                }
                if (lhs.isNumber() && rhs.isNumber()) {
                    double divisor = rhs.doubleValue();
                    if (divisor == 0.0) {
                        break; // Мы не можем взять остаток от нуля
                    }
                    return types.asDouble(lhs.doubleValue() % divisor);
                }
                break;
                
            case SL:
                if (lhs.isLong() && rhs.isLong()) {
                    return types.asLong(lhs.longValue() >> rhs.longValue());
                }
                break;

            case SR:
                if (lhs.isLong() && rhs.isLong()) {
                    return types.asLong(lhs.longValue() << rhs.longValue());
                }
                break;

            case AND:
                if (lhs.isLong() && rhs.isLong()) {
                    return types.asLong(lhs.longValue() & rhs.longValue());
                }
                if (lhs.isBoolean() && rhs.isBoolean()) {
                    return types.asBoolean(lhs.booleanValue() & rhs.booleanValue());
                }
                break;

            case OR:
                if (lhs.isLong() && rhs.isLong()) {
                    return types.asLong(lhs.longValue() | rhs.longValue());
                }
                if (lhs.isBoolean() && rhs.isBoolean()) {
                    return types.asBoolean(lhs.booleanValue() | rhs.booleanValue());
                }
                break;

            case XOR:
                if (lhs.isLong() && rhs.isLong()) {
                    return types.asLong(lhs.longValue() ^ rhs.longValue());
                }
                if (lhs.isBoolean() && rhs.isBoolean()) {
                    return types.asBoolean(lhs.booleanValue() ^ rhs.booleanValue());
                }
                break;

            case EQ: return types.asBoolean(lhs.quickCompare(rhs, 2) == 0);
            case NE: return types.asBoolean(lhs.quickCompare(rhs, 2) != 0);
            case GT: return types.asBoolean(lhs.quickCompare(rhs, 0) > 0);
            case GE: return types.asBoolean(lhs.quickCompare(rhs, -1) >= 0);
            case LT: return types.asBoolean(lhs.quickCompare(rhs, 0) < 0);
            case LE: return types.asBoolean(lhs.quickCompare(rhs, 1) <= 0);
        }

        return null; // Не успех
    }

    @Override
    public void visitUnaryOp(UnaryOp tree) {
        tree.expr = translate(tree.expr);

        if (stripParens(tree.expr).hasTag(Tag.LITERAL)) {
            Literal literalTree = (Literal) tree.expr;
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
                    return types.asLong(-type.longValue());
                }
                if (type.isDouble()) {
                    return types.asDouble(-type.doubleValue());
                }
                break;

            case NOT:
                return types.asBoolean(!type.booleanValue());

            case INVERSE:
                if (type.isLong()) {
                    return types.asLong(~type.longValue());
                }
                break;
        }

        return null; // Не успех
    }
}
