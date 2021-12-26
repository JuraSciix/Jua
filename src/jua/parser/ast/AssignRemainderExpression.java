package jua.parser.ast;

public class AssignRemainderExpression extends AssignmentExpression {

    public AssignRemainderExpression(Position position, Expression var, Expression expr) {
        super(position, var, expr);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAssignRemainder(this);
    }
}
