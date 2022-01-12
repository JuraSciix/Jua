package jua.interpreter;

import jua.interpreter.runtime.Operand;

// will be caught by Jua Exception Handler
// and thrown with location in RuntimeError
public class InterpreterError extends RuntimeException {

    public static InterpreterError inconvertibleTypes(Operand.Type from, Operand.Type to) {
        return new InterpreterError(from + " cannot be converted to " + to + '.');
    }

    public static InterpreterError binaryApplication(String op, Operand.Type lhs, Operand.Type rhs) {
        return new InterpreterError("binary '" + op + "' cannot be applied with " + lhs + " and " + rhs + '.');
    }

    public static InterpreterError unaryApplication(String op, Operand.Type hs) {
        return new InterpreterError("unary '" + op + "' cannot be applied with " + hs + '.');
    }

    public static InterpreterError divisionByZero() {
        return new InterpreterError("/ by 0.");
    }

    public static InterpreterError illegalKeyType(Operand.Type type) {
        return new InterpreterError(type + " cannot be as key in array.");
    }

    public static InterpreterError variableNotExists(String name) {
        return new InterpreterError("variable '" + name + "' not exists in current scope.");
    }

    public static InterpreterError functionNotExists(String name) {
        return new InterpreterError("function '" + name + "' not exists.");
    }

    public static InterpreterError stackOverflow() {
        return new InterpreterError("stack overflow.");
    }

    public InterpreterError(String message) {
        super(message);
    }
}
