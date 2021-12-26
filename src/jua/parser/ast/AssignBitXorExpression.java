package jua.parser.ast;

public class AssignBitXorExpression extends AssignmentExpression {

    public AssignBitXorExpression(Position position, Expression var, Expression expr) {
        super(position, var, expr);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAssignBitXor(this);
    }
}
