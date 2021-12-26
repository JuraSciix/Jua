package jua.parser.tree;

public class AssignDivideExpression extends AssignmentExpression {

    public AssignDivideExpression(Position position, Expression var, Expression expr) {
        super(Tag.ASG_DIV, position, var, expr);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAssignDivide(this);
    }
}
