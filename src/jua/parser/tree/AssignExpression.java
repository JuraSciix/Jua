package jua.parser.tree;

public class AssignExpression extends AssignmentExpression {

    public AssignExpression(Position position, Expression var, Expression expr) {
        super(Tag.ASG, position, var, expr);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAssign(this);
    }
}
