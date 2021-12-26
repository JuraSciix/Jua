package jua.parser.tree;

public class AssignAddExpression extends AssignmentExpression {

    public AssignAddExpression(Position position, Expression var, Expression expr) {
        super(Tag.ASG_ADD, position, var, expr);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAssignAdd(this);
    }
}
