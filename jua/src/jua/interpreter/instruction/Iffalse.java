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
public final class Iffalse extends JumpInstruction {

    public Iffalse(int destIp) {
        super(destIp);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("iffalse");
        super.print(printer);
    }

    @Override
    public int run(InterpreterState state) {
        return state.popStack().booleanValue() ? NEXT : destIp;
    }
}
