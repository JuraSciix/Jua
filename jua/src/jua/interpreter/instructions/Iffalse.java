package jua.interpreter.instructions;

import jua.interpreter.InterpreterState;


/* todo
0: const_true
1: if<cond>     #3
3: ...

Should be optimized to:

0: const_true
1: pop

 */
public final class Iffalse extends ChainInstruction {

    public Iffalse(int destIp) {
        super(destIp);
    }

    @Override
    public int run(InterpreterState state) {
        return state.popStack().booleanValue() ? NEXT : destIp;
    }
}
