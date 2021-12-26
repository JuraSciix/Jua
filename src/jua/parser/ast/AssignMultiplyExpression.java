package jua.parser.ast;

public class AssignMultiplyExpression extends AssignmentExpression {

    public AssignMultiplyExpression(Position position, Expression var, Expression expr) {
        super(position, var, expr);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAssignMultiply(this);
    }
}
