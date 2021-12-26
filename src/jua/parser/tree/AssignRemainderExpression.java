package jua.parser.tree;

public class AssignRemainderExpression extends AssignmentExpression {

    public AssignRemainderExpression(Position position, Expression var, Expression expr) {
        super(Tag.ASG_REM, position, var, expr);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAssignRemainder(this);
    }
}
