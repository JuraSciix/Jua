package jua.runtime;

import jua.compiler.Types;
import jua.interpreter.Address;
import jua.runtime.NativeSupport.NativeFunctionPresent;
import jua.runtime.NativeSupport.ParamsData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NativeStdlib {

    private static final ArrayList<NativeFunctionPresent> nativeFunctionPresents = new ArrayList<>();

    static {
        nativeFunctionPresents.add(new PrintFunction());
        nativeFunctionPresents.add(new PrintlnFunction());
    }

    static class PrintFunction extends NativeFunctionPresent {

        PrintFunction() {
            super("print", ParamsData.create().add("value"));
        }

        @Override
        public boolean execute(Address[] args, int argc, Address returnAddress) {
            Address buffer = new Address();
            boolean error = !args[0].stringVal(buffer);
            if (error) return false;
            System.out.print(buffer.getStringHeap().toString());
            returnAddress.setNull();
            return true;
        }
    }

    static class PrintlnFunction extends NativeFunctionPresent {

        PrintlnFunction() {
            super("println", ParamsData.create().add("value"));
        }

        @Override
        public boolean execute(Address[] args, int argc, Address returnAddress) {
            Address buffer = new Address();
            boolean error = !args[0].stringVal(buffer);
            if (error) return false;
            System.out.println(buffer.getStringHeap().toString());
            returnAddress.setNull();
            return true;
        }
    }

    public static Map<String, Types.Type> getNativeConstants() {
        return Collections.emptyMap();
    }

    public static List<Function> getNativeFunctions() {
        return nativeFunctionPresents.stream().map(NativeFunctionPresent::build).collect(Collectors.toList());
    }
}
