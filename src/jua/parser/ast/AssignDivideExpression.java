package jua.parser.ast;

public class AssignDivideExpression extends AssignmentExpression {

    public AssignDivideExpression(Position position, Expression var, Expression expr) {
        super(position, var, expr);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAssignDivide(this);
    }
}
