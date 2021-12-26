package jua.parser.ast;

public class AssignExpression extends AssignmentExpression {

    public AssignExpression(Position position, Expression var, Expression expr) {
        super(position, var, expr);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAssign(this);
    }
}
