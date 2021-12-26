package jua.parser.tree;

public class AssignBitAndExpression extends AssignmentExpression {

    public AssignBitAndExpression(Position position, Expression var, Expression expr) {
        super(Tag.ASG_BITAND, position, var, expr);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAssignBitAnd(this);
    }
}
