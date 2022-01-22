package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.interpreter.runtime.Operand;
import jua.compiler.CodePrinter;

public final class Switch extends ChainInstruction {

    // todo: Переделать этот ужас.

    public static class Part {

        private final int index;

        private final int[] operands;

        public Part(int index, int[] operands) {
            this.index = index;
            this.operands = operands;
        }
    }

    private final Part[] parts;

    private Part _default;

    public Switch(int destIp, Part[] parts, Part _default) {
        super(destIp);
        this.parts = parts;
        this._default = _default;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("switch");

        for (Part part : parts) printer.printCase(part.operands, part.index);
        if (_default != null) printer.printCase(_default.operands, _default.index);
    }

    @Override
    public int run(InterpreterRuntime env) {
        Operand selector = env.popStack();

        for (Part part : parts) {
            for (int operand : part.operands) {
                if (env.getFrame().getConstant(operand).equals(selector)) {
                    return part.index;
                }
            }
        }
        if (_default == null) {
            return destIp;
        } else {
            return _default.index;
        }
    }
}