package jua.compiler;

import jua.compiler.SemanticInfo.BoolCode;
import jua.compiler.Tree.*;

import static jua.compiler.CompHelper.stripParens;
import static jua.compiler.SemanticInfo.ofBoolean;
import static jua.compiler.Tree.Tag.*;

/**
 * Вычисляет операции с произвольными типами.
 */
public class Evaluator {

    private Object result;

    public Tree tryEvaluate(BinaryOp tree) {
        Expr lhs = stripParens(tree.lhs);
        if (lhs.hasTag(LITERAL)) {
            Expr rhs = stripParens(tree.rhs);
            if (rhs.hasTag(LITERAL)) {
                Literal lhsLit = (Literal) lhs;
                Literal rhsLit = (Literal) rhs;

                if (tryEvaluate2(tree.tag, align(lhsLit.value), align(rhsLit.value))) {
                    Object r = result;
                    result = null;
                    return new Literal(tree.pos, r);
                }
            }
        }

        // Вычислить невозможно
        return tree;
    }

    public Tree tryEvaluate(UnaryOp tree) {
        Expr expr = stripParens(tree.expr);
        if (expr.hasTag(LITERAL)) {
            Literal lit = (Literal) expr;

            if (tryEvaluate2(tree.tag, align(lit.value))) {
                Object r = result;
                result = null;
                return new Literal(tree.pos, r);
            }
        }

        // Вычислить невозможно
        return tree;
    }

    private Object align(Object o) {
        if (o == null) {
            return null;
        }

        // Все интегральные типы приводим к int64.
        // Все вещественные типы приводим к float64.
        // Остальные не конвертируем.

        Class<?> c = o.getClass();
        if (c == Byte.class) {
            return ((Byte) o).longValue();
        }
        if (c == Short.class) {
            return ((Short) o).longValue();
        }
        if (c == Integer.class) {
            return ((Integer) o).longValue();
        }
        if (c == Float.class) {
            return ((Float) o).doubleValue();
        }
        return o;
    }

    private boolean tryEvaluate2(Tag opTag, Object lhs, Object rhs) {
        switch (opTag) {
            case ADD: {
                if (lhs instanceof Long && rhs instanceof Long) {
                    result = (long) lhs + (long) rhs;
                    return true;
                }
                if (lhs instanceof Number && rhs instanceof Number) {
                    Number l = (Number) lhs;
                    Number r = (Number) rhs;
                    result = l.doubleValue() + r.doubleValue();
                    return true;
                }
                if (lhs instanceof String || rhs instanceof String) {
                    result = String.valueOf(lhs) + rhs;
                    return true;
                }
                break;
            }
            case SUB: {
                if (lhs instanceof Long && rhs instanceof Long) {
                    result = (long) lhs - (long) rhs;
                    return true;
                }
                if (lhs instanceof Number && rhs instanceof Number) {
                    Number l = (Number) lhs;
                    Number r = (Number) rhs;
                    result = l.doubleValue() - r.doubleValue();
                    return true;
                }
                break;
            }
            case MUL: {
                if (lhs instanceof Long && rhs instanceof Long) {
                    result = (long) lhs * (long) rhs;
                    return true;
                }
                if (lhs instanceof Number && rhs instanceof Number) {
                    Number l = (Number) lhs;
                    Number r = (Number) rhs;
                    result = l.doubleValue() * r.doubleValue();
                    return true;
                }
                break;
            }
            case DIV: {
                if (lhs instanceof Long && rhs instanceof Long) {
                    if ((long) rhs == 0L) {
                        return false;
                    }
                    result = (long) lhs / (long) rhs;
                    return true;
                }
                if (lhs instanceof Number && rhs instanceof Number) {
                    Number l = (Number) lhs;
                    Number r = (Number) rhs;
                    result = l.doubleValue() / r.doubleValue();
                    return true;
                }
                break;
            }
            case REM: {
                if (lhs instanceof Long && rhs instanceof Long) {
                    if ((long) rhs == 0L) {
                        return false;
                    }
                    result = (long) lhs % (long) rhs;
                    return true;
                }
                if (lhs instanceof Number && rhs instanceof Number) {
                    Number l = (Number) lhs;
                    Number r = (Number) rhs;
                    if (r.doubleValue() == 0.0) {
                        return false;
                    }
                    result = l.doubleValue() % r.doubleValue();
                    return true;
                }
                break;
            }
            case BIT_AND: {
                if (lhs instanceof Long && rhs instanceof Long) {
                    if ((long) rhs == 0L) {
                        return false;
                    }
                    result = (long) lhs & (long) rhs;
                    return true;
                }
                if (lhs instanceof Boolean && rhs instanceof Boolean) {
                    result = (boolean) lhs & (boolean) rhs;
                    return true;
                }
                break;
            }
            case BIT_OR: {
                if (lhs instanceof Long && rhs instanceof Long) {
                    if ((long) rhs == 0L) {
                        return false;
                    }
                    result = (long) lhs | (long) rhs;
                    return true;
                }
                if (lhs instanceof Boolean && rhs instanceof Boolean) {
                    result = (boolean) lhs | (boolean) rhs;
                    return true;
                }
                break;
            }
            case BIT_XOR: {
                if (lhs instanceof Long && rhs instanceof Long) {
                    if ((long) rhs == 0L) {
                        return false;
                    }
                    result = (long) lhs ^ (long) rhs;
                    return true;
                }
                if (lhs instanceof Boolean && rhs instanceof Boolean) {
                    result = (boolean) lhs ^ (boolean) rhs;
                    return true;
                }
                break;
            }
            case SL: {
                if (lhs instanceof Long && rhs instanceof Long) {
                    if ((long) rhs == 0L) {
                        return false;
                    }
                    result = (long) lhs << (long) rhs;
                    return true;
                }
                break;
            }
            case SR: {
                if (lhs instanceof Long && rhs instanceof Long) {
                    if ((long) rhs == 0L) {
                        return false;
                    }
                    result = (long) lhs >> (long) rhs;
                    return true;
                }
                break;
            }
            case AND: {
                result = ofBoolean(lhs).isTrue() && ofBoolean(rhs).isTrue();
                return true;
            }
            case OR: {
                BoolCode l = ofBoolean(lhs);
                BoolCode r = ofBoolean(rhs);
                if (l.isUndefined() || r.isUndefined()) {
                    // Если один из операндов не определен, то вычислить результат невозможно.
                    return false;
                }
                result = l.isTrue() || r.isTrue();
                return true;
            }
            case EQ:
            case NE: {
                boolean factor = (opTag == EQ);
                if (lhs instanceof Long && rhs instanceof Long) {
                    result = ((long) lhs == (long) rhs) == factor;
                    return true;
                }
                if (lhs instanceof Boolean && rhs instanceof Boolean) {
                    result = ((boolean) lhs == (boolean) rhs) == factor;
                    return true;
                }
                if (lhs instanceof Number && rhs instanceof Number) {
                    Number l = (Number) lhs;
                    Number r = (Number) rhs;
                    result = (l.doubleValue() == r.doubleValue()) == factor;
                    return true;
                }
                if (lhs instanceof String && rhs instanceof String) {
                    String l = (String) lhs;
                    String r = (String) rhs;
                    result = l.equals(r) == factor;
                    return true;
                }

                result = (lhs == rhs) == factor;
                return true;
            }
            case LT:
            case LE: {
                int sign = opTag == LT ? 0 : 1;
                if (lhs instanceof Long && rhs instanceof Long) {
                    result = ((long) lhs - (long) rhs) < sign;
                    return true;
                }
                if (lhs instanceof Number && rhs instanceof Number) {
                    Number l = (Number) lhs;
                    Number r = (Number) rhs;
                    result = (l.doubleValue() - r.doubleValue()) < sign;
                    return true;
                }
                break;
            }
            case GT:
            case GE: {
                int sign = opTag == GT ? 0 : -1;
                if (lhs instanceof Long && rhs instanceof Long) {
                    result = ((long) lhs - (long) rhs) > sign;
                    return true;
                }
                if (lhs instanceof Number && rhs instanceof Number) {
                    Number l = (Number) lhs;
                    Number r = (Number) rhs;
                    result = (l.doubleValue() - r.doubleValue()) > sign;
                    return true;
                }
                break;
            }
        }
        // Вычислить невозможно
        return false;
    }

    private boolean tryEvaluate2(Tag opTag, Object o) {
        switch (opTag) {
            case POS: {
                if (o instanceof Number) {
                    result = o;
                    return true;
                }
                break;
            }
            case NEG: {
                if (o instanceof Long) {
                    result = -(long) o;
                    return true;
                }
                if (o instanceof Number) {
                    Number n = (Number) o;
                    result = -n.doubleValue();
                    return true;
                }
                break;
            }
            case NOT: {
                BoolCode l = ofBoolean(o);
                if (l.isTrue()) {
                    result = false;
                    return true;
                }
                if (l.isFalse()) {
                    result = true;
                    return true;
                }
                break;
            }
            case BIT_INV: {
                if (o instanceof Long) {
                    result = ~(long) o;
                    return true;
                }
                break;
            }
        }
        // Вычислить невозможно
        return false;
    }
}
