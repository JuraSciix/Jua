package jua.interpreter;

import jua.util.ReflectionUtils;

import java.lang.reflect.Method;

public class Interpreter {

    public interface State {
        int RUNNING = 0;
        int FATAL = 1;
        int EXITED = 2;
    }

    private static final Interpreter interpreter = new Interpreter();

    public static void fallWithFatalError(String message) {
        Method caller = ReflectionUtils.getCallerMethod();


    }

    public static Interpreter getInterpreter() {
        return interpreter;
    }

    byte state;

    private Interpreter() {

    }


}
