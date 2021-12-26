package jua.parser.tree;

public class AssignBitOrExpression extends AssignmentExpression {

    public AssignBitOrExpression(Position position, Expression var, Expression expr) {
        super(Tag.ASG_BITOR, position, var, expr);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAssignBitOr(this);
    }
}
