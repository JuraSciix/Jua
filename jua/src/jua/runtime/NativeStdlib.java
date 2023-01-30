package jua.runtime;

import jua.compiler.Types;
import jua.interpreter.Address;
import jua.runtime.NativeSupport.NativeFunctionPresent;
import jua.runtime.NativeSupport.ParamsData;
import jua.runtime.heap.StringHeap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NativeStdlib {

    /** nativeFunctionPresents */
    private static final ArrayList<NativeFunctionPresent> nfp = new ArrayList<>();

    static {
        nfp.add(new PrintFunction());
        nfp.add(new PrintlnFunction());
        nfp.add(new TypeofFunction());
        nfp.add(new TimeFunction());
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
            super("println", ParamsData.create().optional("value", ""));
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

    static class TypeofFunction extends NativeFunctionPresent {

        TypeofFunction() {
            super("typeof", ParamsData.create().add("value"));
        }

        @Override
        public boolean execute(Address[] args, int argc, Address returnAddress) {
            returnAddress.set(new StringHeap(args[0].getTypeName()));
            return true;
        }
    }

    static class TimeFunction extends NativeFunctionPresent {

        TimeFunction() {
            super("time", ParamsData.create());
        }

        @Override
        public boolean execute(Address[] args, int argc, Address returnAddress) {
            returnAddress.set(System.nanoTime() / 1E9);
            return true;
        }
    }

    public static Map<String, Types.Type> getNativeConstants() {
        return Collections.emptyMap();
    }

    public static List<Function> getNativeFunctions() {
        return nfp.stream().map(NativeFunctionPresent::build).collect(Collectors.toList());
    }
}
