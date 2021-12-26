package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.lang.Operand;
import jua.tools.CodePrinter;

public class Switch extends JumpState {

    public static class Part {

        private final int index;

        private final Operand[] operands;

        public Part(int index, Operand[] operands) {
            this.index = index;
            this.operands = operands;
        }
    }

    private final Part[] parts;

    private Part _default;

    public Switch(Part[] parts) {
        this.parts = parts;
    }

    public void setDefault(Part part) {
        _default = part;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("switch");

        for (Part part: parts) printer.printCase(part.operands, part.index);
        if (_default != null) printer.printCase(_default.operands, _default.index);
    }

    @Override
    public void run(Environment env) {
        Operand selector = env.popStack();

        for (Part part: parts) {
            for (Operand operand: part.operands) {
                if (operand.equals(selector)) {
                    env.setPC(part.index);
                    return;
                }
            }
        }
        if (_default == null) {
            env.setPC(destination);
        } else {
            env.setPC(_default.index);
        }
    }
}
