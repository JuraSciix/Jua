package jua.parser;

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
        UNUSED,
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
        PRINT,
        PRINTLN
    }
    public final Tag tag;

    public Position position;

    protected Tree(Tag tag, Position position) {
        this.tag = tag;
        this.position = position;
    }

    public final boolean isTag(Tag t) { return tag == t; }

    public final Position getPosition() { return position; }

    public abstract void accept(Visitor visitor);

    public static class AddExpression extends BinaryExpression {

        public AddExpression(Position position, Expression lhs, Expression rhs) {
            super(Tag.ADD, position, lhs, rhs);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitAdd(this);
        }
    }

    public static class AndExpression extends BinaryExpression {

        public AndExpression(Position position, Expression lhs, Expression rhs) {
            super(Tag.LOGAND, position, lhs, rhs);
        }

        @Override
        public boolean isCondition() {
            return true;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitAnd(this);
        }
    }

    public static class ArrayAccessExpression extends UnaryExpression {

        public Expression key;

        public ArrayAccessExpression(Position position, Expression hs, Expression key) {
            super(Tag.ARRAY_ACCESS, position, hs);
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
        public void accept(Visitor visitor) {
            visitor.visitArrayAccess(this);
        }
    }

    public static class ArrayExpression extends Expression {

        // todo: Заменить это на List со своей структурой
        public Map<Expression, Expression> map;

        public ArrayExpression(Position position, Map<Expression, Expression> map) {
            super(Tag.ARRAY_LITERAL, position);
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
        public void accept(Visitor visitor) {
            visitor.visitArray(this);
        }
    }

    public static class AssignAddExpression extends AssignmentExpression {

        public AssignAddExpression(Position position, Expression var, Expression expr) {
            super(Tag.ASG_ADD, position, var, expr);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitAssignAdd(this);
        }
    }

    public static class AssignBitAndExpression extends AssignmentExpression {

        public AssignBitAndExpression(Position position, Expression var, Expression expr) {
            super(Tag.ASG_BITAND, position, var, expr);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitAssignBitAnd(this);
        }
    }

    public static class AssignBitOrExpression extends AssignmentExpression {

        public AssignBitOrExpression(Position position, Expression var, Expression expr) {
            super(Tag.ASG_BITOR, position, var, expr);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitAssignBitOr(this);
        }
    }

    public static class AssignBitXorExpression extends AssignmentExpression {

        public AssignBitXorExpression(Position position, Expression var, Expression expr) {
            super(Tag.ASG_BITXOR, position, var, expr);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitAssignBitXor(this);
        }
    }

    public static class AssignDivideExpression extends AssignmentExpression {

        public AssignDivideExpression(Position position, Expression var, Expression expr) {
            super(Tag.ASG_DIV, position, var, expr);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitAssignDivide(this);
        }
    }

    public static class AssignExpression extends AssignmentExpression {

        public AssignExpression(Position position, Expression var, Expression expr) {
            super(Tag.ASG, position, var, expr);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitAssign(this);
        }
    }

    public abstract static class AssignmentExpression extends Expression {

        public Expression var;

        public Expression expr;

        protected AssignmentExpression(Tag tag, Position position, Expression var, Expression expr) {
            super(tag, position);
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

        public AssignMultiplyExpression(Position position, Expression var, Expression expr) {
            super(Tag.ASG_MUL, position, var, expr);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitAssignMultiply(this);
        }
    }

    public static class AssignNullCoalesceExpression extends AssignmentExpression {

        public AssignNullCoalesceExpression(Position position, Expression var, Expression expr) {
            super(Tag.ASG_NULLCOALESCE, position, var, expr);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitAssignNullCoalesce(this);
        }
    }

    public static class AssignRemainderExpression extends AssignmentExpression {

        public AssignRemainderExpression(Position position, Expression var, Expression expr) {
            super(Tag.ASG_REM, position, var, expr);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitAssignRemainder(this);
        }
    }

    public static class AssignShiftLeftExpression extends AssignmentExpression {

        public AssignShiftLeftExpression(Position position, Expression var, Expression expr) {
            super(Tag.ASG_SL, position, var, expr);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitAssignLeftShift(this);
        }
    }

    public static class AssignShiftRightExpression extends AssignmentExpression {

        public AssignShiftRightExpression(Position position, Expression var, Expression expr) {
            super(Tag.ASG_SR, position, var, expr);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitAssignRightShift(this);
        }
    }

    public static class AssignSubtractExpression extends AssignmentExpression {

        public AssignSubtractExpression(Position position, Expression var, Expression expr) {
            super(Tag.ASG_SUB, position, var, expr);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitAssignSubtract(this);
        }
    }

    public abstract static class BinaryExpression extends Expression {

        public Expression lhs;

        public Expression rhs;

        protected BinaryExpression(Tag tag, Position position, Expression lhs, Expression rhs) {
            super(tag, position);
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

        public BitAndExpression(Position position, Expression lhs, Expression rhs) {
            super(Tag.BITAND, position, lhs, rhs);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitBitAnd(this);
        }
    }

    public static class BitNotExpression extends UnaryExpression {

        public BitNotExpression(Position position, Expression hs) {
            super(Tag.BITCMPL, position, hs);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitBitNot(this);
        }
    }

    public static class BitOrExpression extends BinaryExpression {

        public BitOrExpression(Position position, Expression lhs, Expression rhs) {
            super(Tag.BITOR, position, lhs, rhs);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitBitOr(this);
        }
    }

    public static class BitXorExpression extends BinaryExpression {

        public BitXorExpression(Position position, Expression lhs, Expression rhs) {
            super(Tag.BITXOR, position, lhs, rhs);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitBitXor(this);
        }
    }

    public static class BlockStatement extends Statement {

        public List<Statement> statements;

        public BlockStatement(Position position, List<Statement> statements) {
            super(Tag.COMPOUND, position);
            this.statements = statements;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitBlock(this);
        }
    }

    public abstract static class BooleanExpression extends Expression {

        protected BooleanExpression(Position position) {
            super(Tag.LITERAL, position);
        }
    }

    public static class BreakStatement extends Statement {

        public BreakStatement(Position position) {
            super(Tag.BREAK, position);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitBreak(this);
        }
    }

    public static class CaseStatement extends Statement {

        public List<Expression> expressions;

        public Statement body;

        public CaseStatement(Position position, List<Expression> expressions, Statement body) {
            super(Tag.CASE, position);
            this.expressions = expressions;
            this.body = body;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitCase(this);
        }
    }

    public static class CloneExpression extends UnaryExpression {

        public CloneExpression(Position position, Expression hs) {
            super(Tag.CLONE, position, hs);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitClone(this);
        }
    }

    public abstract static class ConditionalExpression extends BinaryExpression {

        protected ConditionalExpression(Tag tag, Position position, Expression lhs, Expression rhs) {
            super(tag, position, lhs, rhs);
        }
    }

    public static class ConstantDeclareStatement extends Statement {

        public List<String> names;

        public List<Expression> expressions;

        public ConstantDeclareStatement(Position position, List<String> names, List<Expression> expressions) {
            super(Tag.CONSTDECL, position);
            this.names = names;
            this.expressions = expressions;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitConstantDeclare(this);
        }
    }

    public static class ContinueStatement extends Statement {

        public ContinueStatement(Position position) {
            super(Tag.CONTINUE, position);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitContinue(this);
        }
    }

    public static class DivideExpression extends BinaryExpression {

        public DivideExpression(Position position, Expression lhs, Expression rhs) {
            super(Tag.DIV, position, lhs, rhs);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitDivide(this);
        }
    }

    public static class DoStatement extends Statement {

        public Statement body;

        public Expression cond;

        public DoStatement(Position position, Statement body, Expression cond) {
            super(Tag.DOLOOP, position);
            this.body = body;
            this.cond = cond;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitDo(this);
        }
    }

    public static class EqualExpression extends ConditionalExpression {

        public EqualExpression(Position position, Expression lhs, Expression rhs) {
            super(Tag.EQ, position, lhs, rhs);
        }

        @Override
        public boolean isCondition() {
            return true;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitEqual(this);
        }
    }

    public abstract static class Expression extends Statement {

        public static Expression empty() {
            return new Expression(Tag.EMPTY, null) {

                @Override
                public boolean isEmpty() {
                    return true;
                }

                @Override
                public void accept(Visitor visitor) { }
            };
        }

        protected Expression(Tag tag, Position position) {
            super(tag, position);
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

        public FallthroughStatement(Position position) {
            super(Tag.FALLTHROUGH, position);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitFallthrough(this);
        }
    }

    public static class FalseExpression extends BooleanExpression {

        public FalseExpression(Position position) {
            super(position);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitFalse(this);
        }
    }

    public static class FloatExpression extends Expression {

        public double value;

        public FloatExpression(Position position, double value) {
            super(Tag.LITERAL, position);
            this.value = value;
        }

        @Override
        public boolean isLiteral() {
            return true;
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

        public ForStatement(Position position, List<Expression> init, Expression cond, List<Expression> step, Statement body) {
            super(Tag.FORLOOP, position);
            this.init = init;
            this.cond = cond;
            this.step = step;
            this.body = body;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitFor(this);
        }
    }

    public static class FunctionCallExpression extends Expression {

        public final String name;

        public List<Expression> args;

        public FunctionCallExpression(Position position, String name, List<Expression> args) {
            super(Tag.FUNC_CALL, position);
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

        public FunctionDefineStatement(Position position,
                                       String name,
                                       List<String> names,
                                       List<Expression> optionals,
                                       Statement body) {
            super(Tag.FUNCDECL, position);
            this.name = name;
            this.names = names;
            this.optionals = optionals;
            this.body = body;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitFunctionDefine(this);
        }
    }

    public static class GreaterEqualExpression extends ConditionalExpression {

        public GreaterEqualExpression(Position position, Expression lhs, Expression rhs) {
            super(Tag.GE, position, lhs, rhs);
        }

        @Override
        public boolean isCondition() {
            return true;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitGreaterEqual(this);
        }
    }

    public static class GreaterExpression extends ConditionalExpression {

        public GreaterExpression(Position position, Expression lhs, Expression rhs) {
            super(Tag.GT, position, lhs, rhs);
        }

        @Override
        public boolean isCondition() {
            return true;
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

        public IfStatement(Position position, Expression cond, Statement body) {
            this(position, cond, body, null);
        }

        public IfStatement(Position position, Expression cond, Statement body, Statement elseBody) {
            super(Tag.IFELSE, position);
            this.cond = cond;
            this.body = body;
            this.elseBody = elseBody;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitIf(this);
        }
    }

    public abstract static class IncreaseExpression extends UnaryExpression {

        protected IncreaseExpression(Tag tag, Position position, Expression hs) {
            super(tag, position, hs);
        }
    }

    public static class IntExpression extends Expression {

        public long value;

        public IntExpression(Position position, long value) {
            super(Tag.LITERAL, position);
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

        public LessEqualExpression(Position position, Expression lhs, Expression rhs) {
            super(Tag.LE, position, lhs, rhs);
        }

        @Override
        public boolean isCondition() {
            return true;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitLessEqual(this);
        }
    }

    public static class LessExpression extends ConditionalExpression {

        public LessExpression(Position position, Expression lhs, Expression rhs) {
            super(Tag.LT, position, lhs, rhs);
        }

        @Override
        public boolean isCondition() {
            return true;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitLess(this);
        }
    }

    public static class MultiplyExpression extends BinaryExpression {

        public MultiplyExpression(Position position, Expression lhs, Expression rhs) {
            super(Tag.MUL, position, lhs, rhs);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitMultiply(this);
        }
    }

    public static class NegativeExpression extends UnaryExpression {

        public NegativeExpression(Position position, Expression hs) {
            super(Tag.NEG, position, hs);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitNegative(this);
        }
    }

    public static class NotEqualExpression extends ConditionalExpression {

        public NotEqualExpression(Position position, Expression lhs, Expression rhs) {
            super(Tag.NEQ, position, lhs, rhs);
        }

        @Override
        public boolean isCondition() {
            return true;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitNotEqual(this);
        }
    }

    public static class NotExpression extends UnaryExpression {

        public NotExpression(Position position, Expression hs) {
            super(Tag.LOGCMPL, position, hs);
        }

        @Override
        public boolean isCondition() {
            return true;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitNot(this);
        }
    }

    public static class NullCoalesceExpression extends BinaryExpression {

        public NullCoalesceExpression(Position position, Expression lhs, Expression rhs) {
            super(Tag.NULLCOALESCE, position, lhs, rhs);
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
        public void accept(Visitor visitor) {
            visitor.visitNullCoalesce(this);
        }
    }

    public static class NullExpression extends Expression {

        public NullExpression(Position position) {
            super(Tag.LITERAL, position);
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

        public OrExpression(Position position, Expression lhs, Expression rhs) {
            super(Tag.LOGOR, position, lhs, rhs);
        }

        @Override
        public boolean isCondition() {
            return true;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitOr(this);
        }
    }

    public static class ParensExpression extends Expression {

        public Expression expr;

        public ParensExpression(Position position, Expression expr) {
            super(Tag.PARENS, position);
            this.expr = expr;
        }

        @Override
        public Expression child() {
            return expr.child();
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitParens(this);
        }
    }

    // todo: У меня уже имеется механизм для координации по файлу по линиям и столбцам.
    // todo: внедрить этот механизм в эту версию
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

        public PositiveExpression(Position position, Expression hs) {
            super(Tag.POS, position, hs);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitPositive(this);
        }
    }

    public static class PostDecrementExpression extends IncreaseExpression {

        public PostDecrementExpression(Position position, Expression hs) {
            super(Tag.POST_DEC, position, hs);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitPostDecrement(this);
        }
    }

    public static class PostIncrementExpression extends IncreaseExpression {

        public PostIncrementExpression(Position position, Expression hs) {
            super(Tag.POST_INC, position, hs);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitPostIncrement(this);
        }
    }

    public static class PreDecrementExpression extends IncreaseExpression {

        public PreDecrementExpression(Position position, Expression hs) {
            super(Tag.PRE_DEC, position, hs);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitPreDecrement(this);
        }
    }

    public static class PreIncrementExpression extends IncreaseExpression {

        public PreIncrementExpression(Position position, Expression hs) {
            super(Tag.PRE_INC, position, hs);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitPreIncrement(this);
        }
    }

    public static class PrintlnStatement extends Statement {

        public List<Expression> expressions;

        public PrintlnStatement(Position position, List<Expression> expressions) {
            super(Tag.PRINTLN, position);
            this.expressions = expressions;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitPrintln(this);
        }
    }

    // todo: Заменить это на вызов функций с соответствующими именами
    public static class PrintStatement extends Statement {

        public List<Expression> expressions;

        public PrintStatement(Position position, List<Expression> expressions) {
            // print и println с какой-то там версии больше не являются языковыми конструкциями.
            super(Tag.PRINT, position);
            this.expressions = expressions;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitPrint(this);
        }
    }

    public static class RemainderExpression extends BinaryExpression {

        public RemainderExpression(Position position, Expression lhs, Expression rhs) {
            super(Tag.REM, position, lhs, rhs);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitRemainder(this);
        }
    }

    public static class ReturnStatement extends Statement {

        public Expression expr;

        public ReturnStatement(Position position) {
            this(position, null);
        }

        public ReturnStatement(Position position, Expression expr) {
            super(Tag.RETURN, position);
            this.expr = expr;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitReturn(this);
        }
    }

    public static class ShiftLeftExpression extends BinaryExpression {

        public ShiftLeftExpression(Position position, Expression lhs, Expression rhs) {
            super(Tag.SL, position, lhs, rhs);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitLeftShift(this);
        }
    }

    public static class ShiftRightExpression extends BinaryExpression {

        public ShiftRightExpression(Position position, Expression lhs, Expression rhs) {
            super(Tag.SR, position, lhs, rhs);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitRightShift(this);
        }
    }

    public abstract static class Statement extends Tree implements Cloneable {

        public static final Statement EMPTY = new Statement(Tag.EMPTY, null) {
            @Override
            public void accept(Visitor visitor) {}
        };

        protected Statement(Tag tag, Position position) {
            super(tag, position);
        }

        public Statement copy(Position position) {
            try {
                Statement clone = (Statement) super.clone();
                clone.position = position;
                return clone;
            } catch (CloneNotSupportedException e) {
                throw new InternalError(e);
            }
        }
    }

    public static class StringExpression extends Expression {

        public String value;

        public StringExpression(Position position, String value) {
            super(Tag.LITERAL, position);
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

        public SubtractExpression(Position position, Expression lhs, Expression rhs) {
            super(Tag.SUB, position, lhs, rhs);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitSubtract(this);
        }
    }

    public static class SwitchStatement extends Statement {

        public Expression selector;

        public List<CaseStatement> cases;

        public SwitchStatement(Position position, Expression selector, List<CaseStatement> cases) {
            super(Tag.SWITCH, position);
            this.selector = selector;
            this.cases = cases;
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

        public TernaryExpression(Position position, Expression cond, Expression lhs, Expression rhs) {
            super(Tag.TERNARY, position);
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
        public void accept(Visitor visitor) {
            visitor.visitTernary(this);
        }
    }

    public static class TrueExpression extends BooleanExpression {

        public TrueExpression(Position position) {
            super(position);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitTrue(this);
        }
    }

    public abstract static class UnaryExpression extends Expression {

        public Expression hs;

        protected UnaryExpression(Tag tag, Position position, Expression hs) {
            super(tag, position);
            this.hs = hs;
        }

        @Override
        public boolean isAccessible() {
            return hs.isAccessible();
        }
    }

    public static class UnusedExpression extends Expression {

        public Expression expression;

        public UnusedExpression(Position position, Expression expression) {
            super(Tag.UNUSED, position);
            this.expression = expression;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitUnused(this);
        }
    }

    public static class VariableExpression extends Expression {

        // todo: Заменить это на свою структуру (механизм уже готов, его нужно только внедрить)
        public final String name;

        public VariableExpression(Position position, String name) {
            super(Tag.VARIABLE, position);
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
        void visitPrintln(PrintlnStatement statement);
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
        void visitUnused(UnusedExpression expression);
    }

    public static class WhileStatement extends Statement {

        public Expression cond;

        public Statement body;

        public WhileStatement(Position position, Expression cond, Statement body) {
            super(Tag.WHILELOOP, position);
            this.cond = cond;
            this.body = body;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitWhile(this);
        }
    }
}
