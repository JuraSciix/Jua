package jua.parser.ast;

public class AssignRightShiftExpression extends AssignmentExpression {

    public AssignRightShiftExpression(Position position, Expression var, Expression expr) {
        super(position, var, expr);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAssignRightShift(this);
    }
}
