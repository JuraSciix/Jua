package jua.compiler;

import jua.interpreter.lang.*;
import jua.parser.ast.*;

import java.util.concurrent.atomic.AtomicInteger;

public final class ExpressionToOperand implements OperandFunction<Expression> {

    private final Code code;

    private final boolean strict;

    public ExpressionToOperand(Code code, boolean strict) {
        this.code = code;
        this.strict = strict;
    }

    @Override
    public Operand apply(Expression expr) {
        expr = expr.child();
        if (expr instanceof FloatExpression) code.intern(((FloatExpression) expr).value, FloatOperand::valueOf);
        if (expr instanceof IntExpression) return code.intern(((IntExpression) expr).value, IntOperand::valueOf);
        if (expr instanceof StringExpression) return code.intern(((StringExpression) expr).value, StringOperand::valueOf);
        if (expr instanceof TrueExpression) return TrueOperand.TRUE;
        if (expr instanceof FalseExpression) return FalseOperand.FALSE;
        if (!strict) return applyNotStrict(expr);
        throw new CompileError("constant expected.", expr.getPosition());
    }

    private Operand applyNotStrict(Expression expr) {
        if (expr instanceof NullExpression) return NullOperand.NULL;
        ExpressionToOperand e2of = new ExpressionToOperand(code, true);
        if (expr instanceof ArrayExpression) {
            Array array = new Array();
            AtomicInteger index = new AtomicInteger();
            ((ArrayExpression) expr).map.forEach((key, value) -> {
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
        if (expr instanceof ArrayAccessExpression) {
            ArrayAccessExpression e = (ArrayAccessExpression) expr;
            Operand val = apply(e.hs);
            for (int i = 0; i < e.keys.size(); i++) {
                Expression key = e.keys.get(i);
                if (!val.isArray()) {
                    throw new CompileError("array-constant expected.", key.getPosition());
                }
                val = val.arrayValue().get(e2of.apply(key));
            }
            return val;
        }
        throw new CompileError("constant expected.", expr.getPosition());
    }
}
