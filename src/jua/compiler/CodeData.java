package jua.compiler;

import jua.interpreter.runtime.Operand;
import jua.interpreter.runtime.RuntimeFunction;

import java.util.HashMap;
import java.util.Map;

public class CodeData {

    public static class BuiltInException extends IllegalStateException {

        static BuiltInException constantRedefinition(String name) {
            return new BuiltInException("constant '" + name + "' already defined.");
        }

        static BuiltInException functionRedefinition(String name) {
            return new BuiltInException("function '" + name + "' already defined.");
        }

        private BuiltInException(String s) {
            super(s);
        }
    }

    final String filename;

    final Map<String, RuntimeFunction> functions = new HashMap<>();

    final Map<String, Operand> constants = new HashMap<>();

    public CodeData(String filename) {
        // This class has been removed
//        BuiltInDefinitions.init(this);
        this.filename = filename;
    }

    public boolean testFunction(String name) {
        return functions.containsKey(name);
    }

    public boolean testConstant(String name) {
        return constants.containsKey(name);
    }

    public void setFunction(String name, RuntimeFunction function) {
        if (functions.containsKey(name)) {
            throw BuiltInException.functionRedefinition(name);
        }
        functions.put(name, function);
    }

    public void setConstant(String name, Operand constant) {
        if (constants.containsKey(name)) {
            throw BuiltInException.constantRedefinition(name);
        }
        constants.put(name, constant);
    }
}
