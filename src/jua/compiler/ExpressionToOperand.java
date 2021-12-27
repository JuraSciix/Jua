package jua.compiler;

import jua.interpreter.lang.*;
import jua.parser.Tree;

import java.util.concurrent.atomic.AtomicInteger;

public final class ExpressionToOperand implements OperandFunction<Tree.Expression> {

    private final Code code;

    private final boolean strict;

    public ExpressionToOperand(Code code, boolean strict) {
        this.code = code;
        this.strict = strict;
    }

    @Override
    public Operand apply(Tree.Expression expr) {
        expr = expr.child();
        if (expr instanceof Tree.FloatExpression) code.intern(((Tree.FloatExpression) expr).value, FloatOperand::valueOf);
        if (expr instanceof Tree.IntExpression) return code.intern(((Tree.IntExpression) expr).value, IntOperand::valueOf);
        if (expr instanceof Tree.StringExpression) return code.intern(((Tree.StringExpression) expr).value, StringOperand::valueOf);
        if (expr instanceof Tree.TrueExpression) return TrueOperand.TRUE;
        if (expr instanceof Tree.FalseExpression) return FalseOperand.FALSE;
        if (!strict) return applyNotStrict(expr);
        throw new CompileError("constant expected.", expr.getPosition());
    }

    private Operand applyNotStrict(Tree.Expression expr) {
        if (expr instanceof Tree.NullExpression) return NullOperand.NULL;
        ExpressionToOperand e2of = new ExpressionToOperand(code, true);
        if (expr instanceof Tree.ArrayExpression) {
            Array array = new Array();
            AtomicInteger index = new AtomicInteger();
            ((Tree.ArrayExpression) expr).map.forEach((key, value) -> {
                Operand oKey;
                if (key.isEmpty()) {
                    oKey = code.intern(index.longValue(), IntOperand::valueOf);
                } else {
                    oKey = e2of.apply(key);
                }
                array.set(oKey, apply(value));
                index.incrementAndGet();
            });
            return new ArrayOperand(array);
        }
        if (expr instanceof Tree.ArrayAccessExpression) {
            Tree.ArrayAccessExpression e = (Tree.ArrayAccessExpression) expr;
            Operand val = apply(e.hs);
            if (!val.isArray()) {
                throw new CompileError("array-constant expected.", e.hs.getPosition());
            }
            return val;
        }
        throw new CompileError("constant expected.", expr.getPosition());
    }
}
