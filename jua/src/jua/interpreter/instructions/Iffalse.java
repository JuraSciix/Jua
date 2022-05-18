package jua.interpreter.instructions;

import jua.interpreter.InterpreterThread;


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
    public int run(InterpreterThread env) {
        return env.popStack().booleanValue() ? NEXT : destIp;
    }
}
