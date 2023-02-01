package jua.runtime;

import jua.compiler.Types;
import jua.interpreter.Address;
import jua.interpreter.InterpreterThread;
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
        nfp.add(new PanicFunction());
        nfp.add(new IntFunction());
        //nfp.add(new FloatFunction());
        //nfp.add(new BooleanFunction());
        //nfp.add(new StringFunction());
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

    static class PanicFunction extends NativeFunctionPresent {

        PanicFunction() {
            super("panic", ParamsData.create().add("msg"));
        }

        @Override
        public boolean execute(Address[] args, int argc, Address returnAddress) {
            Address msg = new Address();
            if (!args[0].stringVal(msg)) return false;
            InterpreterThread.threadError(msg.getStringHeap().toString());
            return false;
        }
    }

    static class IntFunction extends NativeFunctionPresent {

        IntFunction() {
            super("int", ParamsData.create().add("value"));
        }

        @Override
        public boolean execute(Address[] args, int argc, Address returnAddress) {
            switch (args[0].getType()) {
                case ValueType.LONG:
                case ValueType.DOUBLE:
                case ValueType.BOOLEAN:
                    args[0].longVal(returnAddress);
                    break;
                case ValueType.STRING:
                    try {
                        returnAddress.set(Long.parseLong(args[0].getStringHeap().toString()));
                    } catch (NumberFormatException e) {
                        InterpreterThread.threadError("invalid number format");
                        return false;
                    }
                    break;
                default:
                    InterpreterThread.threadError("%s cannot be converted to int",
                            args[0].getTypeName(),
                            ValueType.getTypeName(ValueType.DOUBLE));
                    break;
            }
            return true;
        }
    }

    static class FloatFunction extends NativeFunctionPresent {

        FloatFunction() {
            super("float", ParamsData.create().add("value"));
        }

        @Override
        public boolean execute(Address[] args, int argc, Address returnAddress) {
            switch (args[0].getType()) {
                case ValueType.LONG:
                case ValueType.DOUBLE:
                case ValueType.BOOLEAN:
                    args[0].doubleVal(returnAddress);
                    break;
                case ValueType.STRING:
                    try {
                        returnAddress.set(Double.parseDouble(args[0].getStringHeap().toString()));
                    } catch (NumberFormatException e) {
                        InterpreterThread.threadError("invalid number format");
                        return false;
                    }
                    break;
                default:
                    InterpreterThread.threadError("%s cannot be converted to %s",
                            args[0].getTypeName(),
                            ValueType.getTypeName(ValueType.DOUBLE));
                    break;
            }
            return true;
        }
    }

    static class BooleanFunction extends NativeFunctionPresent {

        BooleanFunction() {
            super("boolean", ParamsData.create().add("value"));
        }

        @Override
        public boolean execute(Address[] args, int argc, Address returnAddress) {
            return args[0].booleanVal(returnAddress);
        }
    }

    static class StringFunction extends NativeFunctionPresent {

        StringFunction() {
            super("string", ParamsData.create().add("value"));
        }

        @Override
        public boolean execute(Address[] args, int argc, Address returnAddress) {
            return args[0].stringVal(returnAddress);
        }
    }

    public static Map<String, Types.Type> getNativeConstants() {
        return Collections.emptyMap();
    }

    public static List<Function> getNativeFunctions() {
        return nfp.stream().map(NativeFunctionPresent::build).collect(Collectors.toList());
    }
}
