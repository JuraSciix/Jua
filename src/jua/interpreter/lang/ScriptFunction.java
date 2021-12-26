package jua.interpreter.lang;

import jua.interpreter.Environment;
import jua.interpreter.InterpreterError;
import jua.interpreter.Program;
import jua.interpreter.Trap;

public class ScriptFunction implements Function {

    public final String[] args;

    public final int[] locals;

    public final Operand[] optionals;

    public final Program.Builder builder;

    public ScriptFunction(String[] args, int[] locals, Operand[] optionals, Program.Builder builder) {
        this.args = args;
        this.locals = locals;
        this.optionals = optionals;
        this.builder = builder;
    }

    @Override
    public void call(Environment env, String name, int argc) {
        int tot = locals.length;
        int req = (tot - optionals.length);

        if (argc > tot) {
            error(name, "arguments too many. (total " + tot + ", got " + argc + ')');
        } else if (argc < req) {
            error(name, "arguments too few. (required " + req + ", got " + argc + ')');
        }
        Program program = builder.build();
        Operand[] args = new Operand[tot];

        for (int i = tot; --i >= 0; ) {
            program.store(locals[i], args[i] = (i >= argc) ? optionals[i - req] : env.popStack());
        }
        env.enterCall(name, args);
        env.setProgram(program);
        Trap.bti();
    }

    private void error(String name, String message) {
        throw new InterpreterError(String.format("%s: %s", name, message));
    }
}
