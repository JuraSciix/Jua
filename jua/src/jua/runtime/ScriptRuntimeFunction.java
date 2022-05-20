package jua.runtime;

import jua.interpreter.*;
import jua.runtime.code.CodeSegment;
import jua.runtime.code.ConstantPool;

@Deprecated
public class ScriptRuntimeFunction implements RuntimeFunction {

    public final int[] locals;

    public final int[] optionals;

    public final CodeSegment program;

    public ScriptRuntimeFunction(int[] locals, int[] optionals, CodeSegment program) {
        this.locals = locals;
        this.optionals = optionals;
        this.program = program;
    }

    @Override
    public void call(InterpreterThread env, String name, int argc) {
        int tot = locals.length;
        int req = (tot - optionals.length);

        if (((tot - argc) | (argc - req)) < 0) {
            error(name, (argc > tot) ?
                    "arguments too many. (total " + tot + ", got " + argc + ')' :
                    "arguments too few. (required " + req + ", got " + argc + ')');
            return;
        }
        InterpreterFrame frame = null;
        ConstantPool cp = program.constantPool();

        for (int i = tot; i > argc; i--) frame.state().store(req+(tot-i), cp.at(i-req-1));
        for (int i = argc-1; i >= 0; i--) frame.state().store(i, env.popStack());

        env.enterCall(frame);
        Trap.bti();
    }

    private void error(String name, String message) {
        throw new InterpreterError(String.format("%s: %s", name, message));
    }
}
