package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.lang.Operand;
import jua.tools.CodePrinter;

public class Switch extends JumpState {

    // todo: Переделать этот ужас.

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

        for (Part part : parts) printer.printCase(part.operands, part.index);
        if (_default != null) printer.printCase(_default.operands, _default.index);
    }

    @Override
    public int run(Environment env) {
        Operand selector = env.popStack();

        for (Part part : parts) {
            for (Operand operand : part.operands) {
                if (operand.equals(selector)) {
                    return part.index;
                }
            }
        }
        if (_default == null) {
            return destination;
        } else {
            return _default.index;
        }
    }
}