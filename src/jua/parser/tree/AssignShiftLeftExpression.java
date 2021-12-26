package jua.parser.tree;

public class AssignShiftLeftExpression extends AssignmentExpression {

    public AssignShiftLeftExpression(Position position, Expression var, Expression expr) {
        super(Tag.ASG_SL, position, var, expr);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAssignLeftShift(this);
    }
}
