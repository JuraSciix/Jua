package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.InterpreterError;
import jua.interpreter.lang.Operand;
import jua.tools.CodePrinter;

public class Vload implements State {

    private final String name;

    private final int id;

    public Vload(String name, int id) {
        this.name = name;
        this.id = id;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("vload");
        printer.printIdentifier(name, id);
    }

    @Override
    public void run(Environment env) {
        Operand operand = env.getLocal(id);

        if (operand == null) {
            throw InterpreterError.variableNotExists(name);
        }
        env.pushStack(operand);
        env.nextPC();
    }
}
