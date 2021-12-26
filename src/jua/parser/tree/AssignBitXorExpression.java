package jua.parser.tree;

public class AssignBitXorExpression extends AssignmentExpression {

    public AssignBitXorExpression(Position position, Expression var, Expression expr) {
        super(Tag.ASG_BITXOR, position, var, expr);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAssignBitXor(this);
    }
}
