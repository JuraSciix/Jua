package jua.parser.tree;

public class AssignMultiplyExpression extends AssignmentExpression {

    public AssignMultiplyExpression(Position position, Expression var, Expression expr) {
        super(Tag.ASG_MUL, position, var, expr);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAssignMultiply(this);
    }
}
