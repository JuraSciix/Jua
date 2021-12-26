package jua.parser.ast;

public class AssignSubtractExpression extends AssignmentExpression {

    public AssignSubtractExpression(Position position, Expression var, Expression expr) {
        super(position, var, expr);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAssignSubtract(this);
    }
}
