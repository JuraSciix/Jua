package jua.compiler;

import jua.runtime.JuaFunction;
import jua.runtime.heap.Operand;

import java.net.URL;
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

    final URL location;

    final Map<String, JuaFunction> functions = new HashMap<>();

    final Map<String, Operand> constants = new HashMap<>();

    public CodeData(URL filename) {
        // This class has been removed
//        BuiltInDefinitions.init(this);
        this.location = filename;
    }

    public boolean testFunction(String name) {
        return functions.containsKey(name);
    }

    public boolean testConstant(String name) {
        return constants.containsKey(name);
    }

    public void setFunction(String name, JuaFunction function) {
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
