package jua.interpreter;

import jua.runtime.heap.Operand;
import jua.runtime.RuntimeErrorException;

@Deprecated
public class InterpreterError extends RuntimeException {

    public static RuntimeErrorException inconvertibleTypes(Operand.Type from, Operand.Type to) {
        throw new RuntimeErrorException(from.name + " cannot be converted to " + to.name + '.');
    }

    public static RuntimeErrorException binaryApplication(String op, Operand.Type lhs, Operand.Type rhs) {
        throw new RuntimeErrorException("binary '" + op + "' cannot be applied with " + lhs.name + " and " + rhs.name + '.');
    }

    public static RuntimeErrorException unaryApplication(String op, Operand.Type hs) {
        throw new RuntimeErrorException("unary '" + op + "' cannot be applied with " + hs.name + '.');
    }

    public static RuntimeErrorException divisionByZero() {
        throw new RuntimeErrorException("/ by 0.");
    }

    public static RuntimeErrorException illegalKeyType(Operand.Type type) {
        throw new RuntimeErrorException(type.name + " cannot be as key in array.");
    }

    public static RuntimeErrorException variableNotExists(String name) {
        throw new RuntimeErrorException("variable '" + name + "' not exists in current scope.");
    }

    public static RuntimeErrorException functionNotExists(String name) {
        throw new RuntimeErrorException("function '" + name + "' not exists.");
    }

    @Deprecated
    public static RuntimeErrorException stackOverflow() {
        throw new RuntimeErrorException("stack overflow.");
    }

    public InterpreterError(String message) {
        super(message);
    }
}