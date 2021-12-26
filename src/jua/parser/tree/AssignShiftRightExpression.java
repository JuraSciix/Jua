package jua.parser.tree;

public class AssignShiftRightExpression extends AssignmentExpression {

    public AssignShiftRightExpression(Position position, Expression var, Expression expr) {
        super(Tag.ASG_SR, position, var, expr);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAssignRightShift(this);
    }
}
