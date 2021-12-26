package jua.parser.tree;

public class AssignNullCoalesceExpression extends AssignmentExpression {

    public AssignNullCoalesceExpression(Position position, Expression var, Expression expr) {
        super(Tag.ASG_NULLCOALESCE, position, var, expr);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAssignNullCoalesce(this);
    }
}
