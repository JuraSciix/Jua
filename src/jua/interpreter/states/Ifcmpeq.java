package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

// todo: Переделать все Ifcmp<cond> классы в перечисления для реализации паттерна singleton
public class Ifcmpeq extends JumpState {

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifcmpeq");
        super.print(printer);
    }

    @Override
    public int run(Environment env) {
        if (env.popStack().equals(env.popStack())) {
            return destination;
        } else {
            return NEXT;
        }
    }
}