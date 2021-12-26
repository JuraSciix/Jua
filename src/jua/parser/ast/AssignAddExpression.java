package jua.parser.ast;

public class AssignAddExpression extends AssignmentExpression {

    public AssignAddExpression(Position position, Expression var, Expression expr) {
        super(position, var, expr);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAssignAdd(this);
    }
}
