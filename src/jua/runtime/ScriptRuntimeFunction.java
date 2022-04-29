package jua.runtime;

import jua.interpreter.*;

@Deprecated
public class ScriptRuntimeFunction implements RuntimeFunction {

    public final int[] locals;

    public final int[] optionals;

    public final Program program;

    public ScriptRuntimeFunction(int[] locals, int[] optionals, Program program) {
        this.locals = locals;
        this.optionals = optionals;
        this.program = program;
    }

    @Override
    public void call(InterpreterRuntime env, String name, int argc) {
        int tot = locals.length;
        int req = (tot - optionals.length);

        if (((tot - argc) | (argc - req)) < 0) {
            error(name, (argc > tot) ?
                    "arguments too many. (total " + tot + ", got " + argc + ')' :
                    "arguments too few. (required " + req + ", got " + argc + ')');
            return;
        }
        InterpreterFrame frame = null;
        Operand[] cp = program.getConstantPool();

        for (int i = tot; i > argc; i--) frame.getState().store(req+(tot-i), cp[i-req-1]);
        for (int i = argc-1; i >= 0; i--) frame.getState().store(i, env.popStack());

        env.enterCall(frame);
        Trap.bti();
    }

    private void error(String name, String message) {
        throw new InterpreterError(String.format("%s: %s", name, message));
    }
}
