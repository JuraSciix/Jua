package jua.interpreter.instruction;

public abstract class JumpInstruction implements Instruction {

    protected int _elsePoint;

    @Override
    public abstract JumpInstruction negated();

    @Override
    public JumpInstruction elsePoint(int pc) {
        _elsePoint = pc;
        return this;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printIp(_elsePoint);
    }
}