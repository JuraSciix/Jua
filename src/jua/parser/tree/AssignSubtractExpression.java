package jua.parser.tree;

public class AssignSubtractExpression extends AssignmentExpression {

    public AssignSubtractExpression(Position position, Expression var, Expression expr) {
        super(Tag.ASG_SUB, position, var, expr);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAssignSubtract(this);
    }
}
