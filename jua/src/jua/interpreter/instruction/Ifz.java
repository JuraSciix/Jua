package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;


/* todo
0: const_true
1: if<cond>     #3
3: ...

Should be optimized to:

0: const_true
1: pop

 */
// todo: Переименовать в ifz
public final class Ifz extends JumpInstruction {

    public Ifz() {
        super();
    }

    public Ifz(int offset) {
        super(offset);
    }

    @Override
    public int stackAdjustment() { return -1; }

    @Override
    public JumpInstruction negate() { return new ifnz(offset); }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifz");
        super.print(printer);
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.ifz(offset);
    }
}
