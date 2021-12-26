package jua.parser.ast;

public class AssignLeftShiftExpression extends AssignmentExpression {

    public AssignLeftShiftExpression(Position position, Expression var, Expression expr) {
        super(position, var, expr);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAssignLeftShift(this);
    }
}
