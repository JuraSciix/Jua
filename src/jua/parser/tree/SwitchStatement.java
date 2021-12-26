package jua.parser.tree;

import java.util.List;

public class SwitchStatement extends Statement {

    public Expression selector;

    public List<CaseStatement> cases;

    public SwitchStatement(Position position, Expression selector, List<CaseStatement> cases) {
        super(Tag.SWITCH, position);
        this.selector = selector;
        this.cases = cases;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitSwitch(this);
    }
}
