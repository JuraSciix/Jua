package jua.parser.ast;

public class AssignBitAndExpression extends AssignmentExpression {

    public AssignBitAndExpression(Position position, Expression var, Expression expr) {
        super(position, var, expr);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAssignBitAnd(this);
    }
}
