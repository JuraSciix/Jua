package jua.compiler;

import java.util.List;
import java.util.Map;

public abstract class Tree {

    public enum Tag {
        COMPOUND,
        FUNCDECL,
        CONSTDECL,
        IFELSE,
        WHILELOOP,
        DOLOOP,
        FORLOOP,
        FOREACHLOOP,
        SWITCH,
        CASE,
        BREAK,
        CONTINUE,
        FALLTHROUGH,
        RETURN,
        EMPTY,
        DISCARDED,
        ADD,
        SUB,
        MUL,
        DIV,
        REM,
        SL,
        SR,
        BITAND,
        BITOR,
        BITXOR,
        EQ,
        NEQ,
        GT,
        GE,
        LT,
        LE,
        LOGOR,
        LOGAND,
        NULLCOALESCE,
        ASG,
        ASG_ADD,
        ASG_SUB,
        ASG_MUL,
        ASG_DIV,
        ASG_REM,
        ASG_SL,
        ASG_SR,
        ASG_BITAND,
        ASG_BITOR,
        ASG_BITXOR,
        ASG_NULLCOALESCE,
        POS,
        NEG,
        LOGCMPL,
        BITCMPL,
        PRE_INC,
        POST_INC,
        PRE_DEC,
        POST_DEC,
        ARRAY_ACCESS,
        CLONE,
        TERNARY,
        LITERAL,
        ARRAY_LITERAL,
        VARIABLE,
        FUNC_CALL,
        PARENS,
        ERROR,

        // временные теги
        @Deprecated
        PRINT,
        @Deprecated
        PRINTLN
    }

    public int pos;

    public abstract Tag getTag();

    protected Tree(int pos) {
        this.pos = pos;
    }

    public final boolean isTag(Tag t) { return getTag() == t; }

    public abstract void accept(Visitor visitor);

    public static class AddExpression extends BinaryExpression {

        public AddExpression(int pos, Expression lhs, Expression rhs) {
            super(pos, lhs, rhs);
        }

        @Override
        public Tag getTag() {
            return Tag.ADD;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitAdd(this);
        }
    }

    public static class AndExpression extends BinaryExpression {

        public AndExpression(int pos, Expression lhs, Expression rhs) {
            super(pos, lhs, rhs);
        }

        @Override
        public boolean isCondition() {
            return true;
        }

        @Override
        public Tag getTag() {
            return Tag.LOGAND;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitAnd(this);
        }
    }

    public static class ArrayAccessExpression extends UnaryExpression {

        public Expression key;

        public ArrayAccessExpression(int pos, Expression hs, Expression key) {
            super(pos, hs);
            this.key = key;
        }

        @Override
        public boolean isAccessible() {
            return true;
        }

        @Override
        public boolean isCloneable() {
            return true;
        }

        @Override
        public boolean isAssignable() {
            return true;
        }

        @Override
        public boolean isNullable() {
            return true;
        }

        @Override
        public Tag getTag() {
            return Tag.ARRAY_ACCESS;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitArrayAccess(this);
        }
    }

    public static class ArrayExpression extends Expression {

        // todo: Заменить это на List со своей структурой
        public Map<Expression, Expression> map;

        public ArrayExpression(int pos, Map<Expression, Expression> map) {
            super(pos);
            this.map = map;
        }

        @Override
        public boolean isAccessible() {
            return true;
        }

        @Override
        public boolean isCloneable() {
            return true;
        }

        @Override
        public Tag getTag() {
            return Tag.ARRAY_LITERAL;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitArray(this);
        }
    }

    public static class AssignAddExpression extends AssignmentExpression {

        public AssignAddExpression(int pos, Expression var, Expression expr) {
            super(pos, var, expr);
        }

        @Override
        public Tag getTag() {
            return Tag.ASG_ADD;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitAssignAdd(this);
        }
    }

    public static class AssignBitAndExpression extends AssignmentExpression {

        public AssignBitAndExpression(int pos, Expression var, Expression expr) {
            super(pos, var, expr);
        }

        @Override
        public Tag getTag() {
            return Tag.ASG_BITAND;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitAssignBitAnd(this);
        }
    }

    public static class AssignBitOrExpression extends AssignmentExpression {

        public AssignBitOrExpression(int pos, Expression var, Expression expr) {
            super(pos, var, expr);
        }

        @Override
        public Tag getTag() {
            return Tag.ASG_BITOR;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitAssignBitOr(this);
        }
    }

    public static class AssignBitXorExpression extends AssignmentExpression {

        public AssignBitXorExpression(int pos, Expression var, Expression expr) {
            super(pos, var, expr);
        }

        @Override
        public Tag getTag() {
            return Tag.ASG_BITXOR;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitAssignBitXor(this);
        }
    }

    public static class AssignDivideExpression extends AssignmentExpression {

        public AssignDivideExpression(int pos, Expression var, Expression expr) {
            super(pos, var, expr);
        }

        @Override
        public Tag getTag() {
            return Tag.ASG_DIV;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitAssignDivide(this);
        }
    }

    public static class AssignExpression extends AssignmentExpression {

        public AssignExpression(int pos, Expression var, Expression expr) {
            super(pos, var, expr);
        }

        @Override
        public Tag getTag() {
            return Tag.ASG;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitAssign(this);
        }
    }

    public abstract static class AssignmentExpression extends Expression {

        public Expression var;

        public Expression expr;

        protected AssignmentExpression(int pos, Expression var, Expression expr) {
            super(pos);
            this.var = var;
            this.expr = expr;
        }

        @Override
        public boolean isAccessible() {
            return expr.isAccessible();
        }

        @Override
        public boolean isCloneable() {
            return expr.isCloneable();
        }

        @Override
        public boolean isNullable() {
            return expr.isNullable();
        }
    }

    public static class AssignMultiplyExpression extends AssignmentExpression {

        public AssignMultiplyExpression(int pos, Expression var, Expression expr) {
            super(pos, var, expr);
        }

        @Override
        public Tag getTag() {
            return Tag.ASG_MUL;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitAssignMultiply(this);
        }
    }

    public static class AssignNullCoalesceExpression extends AssignmentExpression {

        public AssignNullCoalesceExpression(int pos, Expression var, Expression expr) {
            super(pos, var, expr);
        }

        @Override
        public Tag getTag() {
            return Tag.ASG_NULLCOALESCE;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitAssignNullCoalesce(this);
        }
    }

    public static class AssignRemainderExpression extends AssignmentExpression {

        public AssignRemainderExpression(int pos, Expression var, Expression expr) {
            super(pos, var, expr);
        }

        @Override
        public Tag getTag() {
            return Tag.ASG_REM;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitAssignRemainder(this);
        }
    }

    public static class AssignShiftLeftExpression extends AssignmentExpression {

        public AssignShiftLeftExpression(int pos, Expression var, Expression expr) {
            super(pos, var, expr);
        }

        @Override
        public Tag getTag() {
            return Tag.ASG_SL;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitAssignLeftShift(this);
        }
    }

    public static class AssignShiftRightExpression extends AssignmentExpression {

        public AssignShiftRightExpression(int pos, Expression var, Expression expr) {
            super(pos, var, expr);
        }

        @Override
        public Tag getTag() {
            return Tag.ASG_SR;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitAssignRightShift(this);
        }
    }

    public static class AssignSubtractExpression extends AssignmentExpression {

        public AssignSubtractExpression(int pos, Expression var, Expression expr) {
            super(pos, var, expr);
        }

        @Override
        public Tag getTag() {
            return Tag.ASG_SUB;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitAssignSubtract(this);
        }
    }

    public abstract static class BinaryExpression extends Expression {

        public Expression lhs;

        public Expression rhs;

        protected BinaryExpression(int pos, Expression lhs, Expression rhs) {
            super(pos);
            this.lhs = lhs;
            this.rhs = rhs;
        }

        @Override
        public boolean isAccessible() {
            return lhs.isAccessible() && rhs.isAccessible();
        }

        @Override
        public boolean isCloneable() {
            return lhs.isCloneable() && rhs.isCloneable();
        }
    }

    public static class BitAndExpression extends BinaryExpression {

        public BitAndExpression(int pos, Expression lhs, Expression rhs) {
            super(pos, lhs, rhs);
        }

        @Override
        public Tag getTag() {
            return Tag.BITAND;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitBitAnd(this);
        }
    }

    public static class BitNotExpression extends UnaryExpression {

        public BitNotExpression(int pos, Expression hs) {
            super(pos, hs);
        }

        @Override
        public Tag getTag() {
            return Tag.BITCMPL;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitBitNot(this);
        }
    }

    public static class BitOrExpression extends BinaryExpression {

        public BitOrExpression(int pos, Expression lhs, Expression rhs) {
            super(pos, lhs, rhs);
        }

        @Override
        public Tag getTag() {
            return Tag.BITOR;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitBitOr(this);
        }
    }

    public static class BitXorExpression extends BinaryExpression {

        public BitXorExpression(int pos, Expression lhs, Expression rhs) {
            super(pos, lhs, rhs);
        }

        @Override
        public Tag getTag() {
            return Tag.BITXOR;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitBitXor(this);
        }
    }

    public static class BlockStatement extends Statement {

        public List<Statement> statements;

        public BlockStatement(int pos, List<Statement> statements) {
            super(pos);
            this.statements = statements;
        }

        @Override
        public Tag getTag() {
            return Tag.COMPOUND;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitBlock(this);
        }
    }

    public abstract static class BooleanExpression extends LiteralExpression {

        protected BooleanExpression(int pos) {
            super(pos);
        }
    }

    public static class BreakStatement extends Statement {

        @Override
        public Tag getTag() {
            return Tag.BREAK;
        }

        public BreakStatement(int pos) {
            super(pos);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitBreak(this);
        }
    }

    public static class CaseStatement extends Statement {

        public List<Expression> expressions;

        public Statement body;

        public CaseStatement(int pos, List<Expression> expressions, Statement body) {
            super(pos);
            this.expressions = expressions;
            this.body = body;
        }

        @Override
        public Tag getTag() {
            return Tag.CASE;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitCase(this);
        }
    }

    public static class CloneExpression extends UnaryExpression {

        public CloneExpression(int pos, Expression hs) {
            super(pos, hs);
        }

        @Override
        public Tag getTag() {
            return Tag.CLONE;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitClone(this);
        }
    }

    public abstract static class ConditionalExpression extends BinaryExpression {

        protected ConditionalExpression(int pos, Expression lhs, Expression rhs) {
            super(pos, lhs, rhs);
        }
    }

    public static class ConstantDeclareStatement extends Statement {

        public List<String> names;

        public List<Expression> expressions;

        public ConstantDeclareStatement(int pos, List<String> names, List<Expression> expressions) {
            super(pos);
            this.names = names;
            this.expressions = expressions;
        }

        @Override
        public Tag getTag() {
            return Tag.CONSTDECL;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitConstantDeclare(this);
        }
    }

    public static class ContinueStatement extends Statement {

        @Override
        public Tag getTag() {
            return Tag.CONTINUE;
        }

        public ContinueStatement(int pos) {
            super(pos);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitContinue(this);
        }
    }

    public static class DivideExpression extends BinaryExpression {

        public DivideExpression(int pos, Expression lhs, Expression rhs) {
            super(pos, lhs, rhs);
        }

        @Override
        public Tag getTag() {
            return Tag.DIV;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitDivide(this);
        }
    }

    public static class DoStatement extends Statement {

        public Statement body;

        public Expression cond;

        public DoStatement(int pos, Statement body, Expression cond) {
            super(pos);
            this.body = body;
            this.cond = cond;
        }

        @Override
        public Tag getTag() {
            return Tag.DOLOOP;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitDo(this);
        }
    }

    public static class EqualExpression extends ConditionalExpression {

        public EqualExpression(int pos, Expression lhs, Expression rhs) {
            super(pos, lhs, rhs);
        }

        @Override
        public boolean isCondition() {
            return true;
        }

        @Override
        public Tag getTag() {
            return Tag.EQ;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitEqual(this);
        }
    }

    public abstract static class Expression extends Statement {

        public static Expression empty() {
            return new Expression(0) {

                @Override
                public boolean isEmpty() {
                    return true;
                }

                @Override
                public Tag getTag() {
                    return Tag.EMPTY;
                }

                @Override
                public void accept(Visitor visitor) { }
            };
        }

        protected Expression(int pos) {
            super(pos);
        }

        // todo: Почти все эти методы лишние, часть из них нужно переместить в jua.compiler.TreeInfo

        public boolean isAccessible() {
            return false;
        }

        public boolean isAssignable() {
            return false;
        }

        public boolean isCloneable() {
            return false;
        }

        public boolean isCondition() {
            return false;
        }

        public boolean isLiteral() {
            return false;
        }

        public boolean isEmpty() {
            return false;
        }

        public boolean isNullable() {
            return false;
        }

        public Expression child() {
            return this;
        }
    }

    public static class FallthroughStatement extends Statement {

        @Override
        public Tag getTag() {
            return Tag.FALLTHROUGH;
        }

        public FallthroughStatement(int pos) {
            super(pos);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitFallthrough(this);
        }
    }

    public static class FalseExpression extends BooleanExpression {

        public FalseExpression(int pos) {
            super(pos);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitFalse(this);
        }
    }

    public static class FloatExpression extends LiteralExpression {

        public double value;

        public FloatExpression(int pos, double value) {
            super(pos);
            this.value = value;
        }

        @Override
        public boolean isLiteral() {
            return true;
        }

        @Override
        public Tag getTag() {
            return null;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitFloat(this);
        }
    }

    public static class ForStatement extends Statement {

        public List<Expression> init;

        public Expression cond;

        public List<Expression> step;

        public Statement body;

        public ForStatement(int pos, List<Expression> init, Expression cond, List<Expression> step, Statement body) {
            super(pos);
            this.init = init;
            this.cond = cond;
            this.step = step;
            this.body = body;
        }

        @Override
        public Tag getTag() {
            return Tag.FORLOOP;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitFor(this);
        }
    }

    public static class FunctionCallExpression extends Expression {

        public final String name;

        public List<Expression> args;

        public FunctionCallExpression(int pos, String name, List<Expression> args) {
            super(pos);
            this.name = name;
            this.args = args;
        }

        @Override
        public boolean isAccessible() {
            return true;
        }

        @Override
        public boolean isCloneable() {
            return true;
        }

        @Override
        public boolean isNullable() {
            return true;
        }

        @Override
        public Tag getTag() {
            return Tag.FUNC_CALL;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitFunctionCall(this);
        }
    }

    public static class FunctionDefineStatement extends Statement {

        // todo: Заменить строки на свои структуры (механизм уже готов, его нужно только внедрить)

        public final String name;

        public final List<String> names;

        public List<Expression> optionals;

        public Statement body;

        public FunctionDefineStatement(int pos,
                                       String name,
                                       List<String> names,
                                       List<Expression> optionals,
                                       Statement body) {
            super(pos);
            this.name = name;
            this.names = names;
            this.optionals = optionals;
            this.body = body;
        }

        @Override
        public Tag getTag() {
            return Tag.FUNCDECL;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitFunctionDefine(this);
        }
    }

    public static class GreaterEqualExpression extends ConditionalExpression {

        public GreaterEqualExpression(int pos, Expression lhs, Expression rhs) {
            super(pos, lhs, rhs);
        }

        @Override
        public boolean isCondition() {
            return true;
        }

        @Override
        public Tag getTag() {
            return Tag.GE;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitGreaterEqual(this);
        }
    }

    public static class GreaterExpression extends ConditionalExpression {

        public GreaterExpression(int pos, Expression lhs, Expression rhs) {
            super(pos, lhs, rhs);
        }

        @Override
        public boolean isCondition() {
            return true;
        }

        @Override
        public Tag getTag() {
            return Tag.GT;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitGreater(this);
        }
    }

    public static class IfStatement extends Statement {

        public Expression cond;

        public Statement body;

        public Statement elseBody;

        public IfStatement(int pos, Expression cond, Statement body) {
            this(pos, cond, body, null);
        }

        public IfStatement(int pos, Expression cond, Statement body, Statement elseBody) {
            super(pos);
            this.cond = cond;
            this.body = body;
            this.elseBody = elseBody;
        }

        @Override
        public Tag getTag() {
            return Tag.IFELSE;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitIf(this);
        }
    }

    public abstract static class IncreaseExpression extends UnaryExpression {

        protected IncreaseExpression(int pos, Expression hs) {
            super(pos, hs);
        }
    }

    public static abstract class LiteralExpression extends Expression {

        protected LiteralExpression(int pos) {
            super(pos);
        }

        @Override
        public Tag getTag() {
            return Tag.LITERAL;
        }
    }

    public static class IntExpression extends LiteralExpression {

        public long value;

        public IntExpression(int pos, long value) {
            super(pos);
            this.value = value;
        }

        @Override
        public boolean isLiteral() {
            return true;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitInt(this);
        }
    }

    public static class LessEqualExpression extends ConditionalExpression {

        public LessEqualExpression(int pos, Expression lhs, Expression rhs) {
            super(pos, lhs, rhs);
        }

        @Override
        public boolean isCondition() {
            return true;
        }

        @Override
        public Tag getTag() {
            return Tag.LE;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitLessEqual(this);
        }
    }

    public static class LessExpression extends ConditionalExpression {

        public LessExpression(int pos, Expression lhs, Expression rhs) {
            super(pos, lhs, rhs);
        }

        @Override
        public boolean isCondition() {
            return true;
        }

        @Override
        public Tag getTag() {
            return Tag.LT;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitLess(this);
        }
    }

    public static class MultiplyExpression extends BinaryExpression {

        public MultiplyExpression(int pos, Expression lhs, Expression rhs) {
            super(pos, lhs, rhs);
        }

        @Override
        public Tag getTag() {
            return Tag.MUL;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitMultiply(this);
        }
    }

    public static class NegativeExpression extends UnaryExpression {

        public NegativeExpression(int pos, Expression hs) {
            super(pos, hs);
        }

        @Override
        public Tag getTag() {
            return Tag.NEG;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitNegative(this);
        }
    }

    public static class NotEqualExpression extends ConditionalExpression {

        public NotEqualExpression(int pos, Expression lhs, Expression rhs) {
            super(pos, lhs, rhs);
        }

        @Override
        public boolean isCondition() {
            return true;
        }

        @Override
        public Tag getTag() {
            return Tag.NEQ;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitNotEqual(this);
        }
    }

    public static class NotExpression extends UnaryExpression {

        public NotExpression(int pos, Expression hs) {
            super(pos, hs);
        }

        @Override
        public boolean isCondition() {
            return true;
        }

        @Override
        public Tag getTag() {
            return Tag.LOGCMPL;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitNot(this);
        }
    }

    public static class NullCoalesceExpression extends BinaryExpression {

        public NullCoalesceExpression(int pos, Expression lhs, Expression rhs) {
            super(pos, lhs, rhs);
        }

        @Override
        public boolean isAccessible() {
            return rhs.isAccessible();
        }

        @Override
        public boolean isCloneable() {
            return rhs.isCloneable();
        }

        @Override
        public boolean isNullable() {
            return lhs.isNullable();
        }

        @Override
        public Tag getTag() {
            return Tag.NULLCOALESCE;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitNullCoalesce(this);
        }
    }

    public static class NullExpression extends LiteralExpression {

        public NullExpression(int pos) {
            super(pos);
        }

        @Override
        public boolean isNullable() {
            return true;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitNull(this);
        }
    }

    public static class OrExpression extends BinaryExpression {

        public OrExpression(int pos, Expression lhs, Expression rhs) {
            super(pos, lhs, rhs);
        }

        @Override
        public boolean isCondition() {
            return true;
        }

        @Override
        public Tag getTag() {
            return Tag.LOGOR;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitOr(this);
        }
    }

    public static class ParensExpression extends Expression {

        public Expression expr;

        public ParensExpression(int pos, Expression expr) {
            super(pos);
            this.expr = expr;
        }

        @Override
        public Expression child() {
            return expr.child();
        }

        @Override
        public Tag getTag() {
            return Tag.PARENS;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitParens(this);
        }
    }

    @Deprecated
    public static class Position {

        public final String filename;

        public final int line;

        public final int offset;

        public Position(String filename, int line, int offset) {
            this.filename = filename;
            this.line = line;
            this.offset = offset;
        }
    }

    public static class PositiveExpression extends UnaryExpression {

        public PositiveExpression(int pos, Expression hs) {
            super(pos, hs);
        }

        @Override
        public Tag getTag() {
            return Tag.POS;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitPositive(this);
        }
    }

    public static class PostDecrementExpression extends IncreaseExpression {

        public PostDecrementExpression(int pos, Expression hs) {
            super(pos, hs);
        }

        @Override
        public Tag getTag() {
            return Tag.POST_DEC;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitPostDecrement(this);
        }
    }

    public static class PostIncrementExpression extends IncreaseExpression {

        public PostIncrementExpression(int pos, Expression hs) {
            super(pos, hs);
        }

        @Override
        public Tag getTag() {
            return Tag.POST_INC;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitPostIncrement(this);
        }
    }

    public static class PreDecrementExpression extends IncreaseExpression {

        public PreDecrementExpression(int pos, Expression hs) {
            super(pos, hs);
        }

        @Override
        public Tag getTag() {
            return Tag.PRE_DEC;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitPreDecrement(this);
        }
    }

    public static class PreIncrementExpression extends IncreaseExpression {

        public PreIncrementExpression(int pos, Expression hs) {
            super(pos, hs);
        }

        @Override
        public Tag getTag() {
            return Tag.PRE_INC;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitPreIncrement(this);
        }
    }

    @Deprecated
    public static class PrintlnStatement extends Statement {

        public List<Expression> expressions;

        public PrintlnStatement(int pos, List<Expression> expressions) {
            super(pos);
            this.expressions = expressions;
        }

        @Override
        public Tag getTag() {
            return Tag.PRINTLN;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitPrintln(this);
        }
    }

    @Deprecated
    // todo: Заменить это на вызов функций с соответствующими именами
    public static class PrintStatement extends Statement {

        public List<Expression> expressions;

        public PrintStatement(int pos, List<Expression> expressions) {
            // print и println с какой-то там версии больше не являются языковыми конструкциями.
            super(pos);
            this.expressions = expressions;
        }

        @Override
        public Tag getTag() {
            return Tag.PRINT;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitPrint(this);
        }
    }

    public static class RemainderExpression extends BinaryExpression {

        public RemainderExpression(int pos, Expression lhs, Expression rhs) {
            super(pos, lhs, rhs);
        }

        @Override
        public Tag getTag() {
            return Tag.REM;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitRemainder(this);
        }
    }

    public static class ReturnStatement extends Statement {

        public Expression expr;

        @Override
        public Tag getTag() {
            return Tag.RETURN;
        }

        public ReturnStatement(int pos) {
            this(pos, null);
        }

        public ReturnStatement(int pos, Expression expr) {
            super(pos);
            this.expr = expr;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitReturn(this);
        }
    }

    public static class ShiftLeftExpression extends BinaryExpression {

        public ShiftLeftExpression(int pos, Expression lhs, Expression rhs) {
            super(pos, lhs, rhs);
        }

        @Override
        public Tag getTag() {
            return Tag.SL;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitLeftShift(this);
        }
    }

    public static class ShiftRightExpression extends BinaryExpression {

        public ShiftRightExpression(int pos, Expression lhs, Expression rhs) {
            super(pos, lhs, rhs);
        }

        @Override
        public Tag getTag() {
            return Tag.SR;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitRightShift(this);
        }
    }

    public abstract static class Statement extends Tree implements Cloneable {

        public static final Statement EMPTY = new Statement(0) {
            @Override
            public Tag getTag() {
                return Tag.EMPTY;
            }

            @Override
            public void accept(Visitor visitor) {}
        };

        protected Statement(int pos) {
            super(pos);
        }

        public Statement copy(int pos) {
            try {
                Statement clone = (Statement) super.clone();
                clone.pos = pos;
                return clone;
            } catch (CloneNotSupportedException e) {
                throw new InternalError(e);
            }
        }
    }

    public static class StringExpression extends LiteralExpression {

        public String value;

        public StringExpression(int pos, String value) {
            super(pos);
            this.value = value;
        }

        @Override
        public boolean isLiteral() {
            return true;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitString(this);
        }
    }

    public static class SubtractExpression extends BinaryExpression {

        public SubtractExpression(int pos, Expression lhs, Expression rhs) {
            super(pos, lhs, rhs);
        }

        @Override
        public Tag getTag() {
            return Tag.SUB;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitSubtract(this);
        }
    }

    public static class SwitchStatement extends Statement {

        public Expression selector;

        public List<CaseStatement> cases;

        public SwitchStatement(int pos, Expression selector, List<CaseStatement> cases) {
            super(pos);
            this.selector = selector;
            this.cases = cases;
        }

        @Override
        public Tag getTag() {
            return Tag.SWITCH;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitSwitch(this);
        }
    }

    public static class TernaryExpression extends Expression {

        public Expression cond;

        public Expression lhs;

        public Expression rhs;

        public TernaryExpression(int pos, Expression cond, Expression lhs, Expression rhs) {
            super(pos);
            this.cond = cond;
            this.lhs = lhs;
            this.rhs = rhs;
        }

        @Override
        public boolean isAccessible() {
            return lhs.isAccessible() && rhs.isAccessible();
        }

        @Override
        public boolean isCloneable() {
            return lhs.isCloneable() && rhs.isCloneable();
        }

        @Override
        public boolean isNullable() {
            return lhs.isNullable() || rhs.isNullable();
        }

        @Override
        public Tag getTag() {
            return Tag.TERNARY;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitTernary(this);
        }
    }

    public static class TrueExpression extends BooleanExpression {

        public TrueExpression(int pos) {
            super(pos);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitTrue(this);
        }
    }

    public abstract static class UnaryExpression extends Expression {

        public Expression hs;

        protected UnaryExpression(int pos, Expression hs) {
            super(pos);
            this.hs = hs;
        }

        @Override
        public boolean isAccessible() {
            return hs.isAccessible();
        }
    }

    public static class DiscardedExpression extends Expression {

        public Expression expression;

        public DiscardedExpression(int pos, Expression expression) {
            super(pos);
            this.expression = expression;
        }

        @Override
        public Tag getTag() {
            return Tag.DISCARDED;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitDiscarded(this);
        }
    }

    public static class VariableExpression extends Expression {

        // todo: Заменить это на свою структуру (механизм уже готов, его нужно только внедрить)
        public final String name;

        public VariableExpression(int pos, String name) {
            super(pos);
            this.name = name;
        }

        @Override
        public boolean isAccessible() {
            return true;
        }

        @Override
        public boolean isAssignable() {
            return true;
        }

        @Override
        public boolean isCloneable() {
            return true;
        }

        @Override
        public boolean isNullable() {
            return true;
        }

        @Override
        public Tag getTag() {
            return Tag.VARIABLE;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitVariable(this);
        }
    }

    public static interface Visitor {

        void visitAdd(AddExpression expression);
        void visitAnd(AndExpression expression);
        void visitArrayAccess(ArrayAccessExpression expression);
        void visitArray(ArrayExpression expression);
        void visitAssignAdd(AssignAddExpression expression);
        void visitAssignBitAnd(AssignBitAndExpression expression);
        void visitAssignBitOr(AssignBitOrExpression expression);
        void visitAssignBitXor(AssignBitXorExpression expression);
        void visitAssignDivide(AssignDivideExpression expression);
        void visitAssignLeftShift(AssignShiftLeftExpression expression);
        void visitAssign(AssignExpression expression);
        void visitAssignMultiply(AssignMultiplyExpression expression);
        void visitAssignNullCoalesce(AssignNullCoalesceExpression expression);
        void visitAssignRemainder(AssignRemainderExpression expression);
        void visitAssignRightShift(AssignShiftRightExpression expression);
        void visitAssignSubtract(AssignSubtractExpression expression);
        void visitBitAnd(BitAndExpression expression);
        void visitBitNot(BitNotExpression expression);
        void visitBitOr(BitOrExpression expression);
        void visitBitXor(BitXorExpression expression);
        void visitBlock(BlockStatement statement);
        void visitBreak(BreakStatement statement);
        void visitCase(CaseStatement statement);
        void visitClone(CloneExpression expression);
        void visitConstantDeclare(ConstantDeclareStatement statement);
        void visitContinue(ContinueStatement statement);
        void visitDivide(DivideExpression expression);
        void visitDo(DoStatement statement);
        void visitEqual(EqualExpression expression);
        void visitFallthrough(FallthroughStatement statement);
        void visitFalse(FalseExpression expression);
        void visitFloat(FloatExpression expression);
        void visitFor(ForStatement statement);
        void visitFunctionCall(FunctionCallExpression expression);
        void visitFunctionDefine(FunctionDefineStatement statement);
        void visitGreaterEqual(GreaterEqualExpression expression);
        void visitGreater(GreaterExpression expression);
        void visitIf(IfStatement statement);
        void visitInt(IntExpression expression);
        void visitLeftShift(ShiftLeftExpression expression);
        void visitLessEqual(LessEqualExpression expression);
        void visitLess(LessExpression expression);
        void visitMultiply(MultiplyExpression expression);
        void visitNegative(NegativeExpression expression);
        void visitNotEqual(NotEqualExpression expression);
        void visitNot(NotExpression expression);
        void visitNullCoalesce(NullCoalesceExpression expression);
        void visitNull(NullExpression expression);
        void visitOr(OrExpression expression);
        void visitParens(ParensExpression expression);
        void visitPositive(PositiveExpression expression);
        void visitPostDecrement(PostDecrementExpression expression);
        void visitPostIncrement(PostIncrementExpression expression);
        void visitPreDecrement(PreDecrementExpression expression);
        void visitPreIncrement(PreIncrementExpression expression);
        @Deprecated
        void visitPrintln(PrintlnStatement statement);
        @Deprecated
        void visitPrint(PrintStatement statement);
        void visitRemainder(RemainderExpression expression);
        void visitReturn(ReturnStatement statement);
        void visitRightShift(ShiftRightExpression expression);
        void visitString(StringExpression expression);
        void visitSubtract(SubtractExpression expression);
        void visitSwitch(SwitchStatement statement);
        void visitTernary(TernaryExpression expression);
        void visitTrue(TrueExpression expression);
        void visitVariable(VariableExpression expression);
        void visitWhile(WhileStatement statement);
        void visitDiscarded(DiscardedExpression expression);
    }

    public static class WhileStatement extends Statement {

        public Expression cond;

        public Statement body;

        public WhileStatement(int pos, Expression cond, Statement body) {
            super(pos);
            this.cond = cond;
            this.body = body;
        }

        @Override
        public Tag getTag() {
            return Tag.WHILELOOP;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitWhile(this);
        }
    }
}
