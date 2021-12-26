package jua.parser.ast;

public class AssignNullCoalesceExpression extends AssignmentExpression {

    public AssignNullCoalesceExpression(Position position, Expression var, Expression expr) {
        super(position, var, expr);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAssignNullCoalesce(this);
    }
}
