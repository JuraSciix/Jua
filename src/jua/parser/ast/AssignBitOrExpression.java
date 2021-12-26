package jua.parser.ast;

public class AssignBitOrExpression extends AssignmentExpression {

    public AssignBitOrExpression(Position position, Expression var, Expression expr) {
        super(position, var, expr);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAssignBitOr(this);
    }
}
