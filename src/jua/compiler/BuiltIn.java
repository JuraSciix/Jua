package jua.compiler;

import jua.interpreter.lang.Constant;
import jua.interpreter.lang.Function;

import java.util.HashMap;
import java.util.Map;

public class BuiltIn {

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

    final Map<String, Function> functions = new HashMap<>();

    final Map<String, Constant> constants = new HashMap<>();

    public BuiltIn() {
        BuiltInFunctions.init(this);
    }

    public boolean testFunction(String name) {
        return functions.containsKey(name);
    }

    public boolean testConstant(String name) {
        return constants.containsKey(name);
    }

    public void setFunction(String name, Function function) {
        if (functions.containsKey(name)) {
            throw BuiltInException.functionRedefinition(name);
        }
        functions.put(name, function);
    }

    public void setConstant(String name, Constant constant) {
        if (constants.containsKey(name)) {
            throw BuiltInException.constantRedefinition(name);
        }
        constants.put(name, constant);
    }
}
