package jua.stdlib;

import jua.runtime.Function;
import jua.runtime.Types;
import jua.runtime.heap.ListHeap;
import jua.runtime.heap.StringHeap;
import jua.runtime.interpreter.InterpreterThread;
import jua.runtime.interpreter.memory.Address;
import jua.stdlib.NativeSupport.NativeFunctionPresent;
import jua.stdlib.NativeSupport.ParamsData;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NativeStdlib {

    /** nativeFunctionPresents */
    private static final ArrayList<NativeFunctionPresent> nfp = new ArrayList<>();

    static {
        nfp.add(new PrintFunction());
        nfp.add(new PrintfFunction());
        nfp.add(new PrintlnFunction());
        nfp.add(new TypeofFunction());
        nfp.add(new TimeFunction());
        nfp.add(new PanicFunction());
        nfp.add(new StrCodePointsFunction());
        nfp.add(new _SizeOfFunction());
        nfp.add(new MSqrtFunction());
        nfp.add(new MCosFunction());
        nfp.add(new MSinFunction());
        nfp.add(new MAtan2Function());
        nfp.add(new MLogFunction());
        nfp.add(new MExpFunction());
        nfp.add(new MAbsFunction());
        nfp.add(new StrCharArrayFunction());
        nfp.add(new StrCmpFunction());
        nfp.add(new HashFunction());
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
            String str = "";
            if (argc > 0) {
                Address buffer = new Address();
                boolean error = !args[0].stringVal(buffer);
                if (error) return false;
                str = buffer.getStringHeap().toString();
            }
            System.out.println(str);
            returnAddress.setNull();
            return true;
        }
    }

    static class PrintfFunction extends NativeFunctionPresent {

        PrintfFunction() {
            super("printf", ParamsData.create().add("fmt").optional("args", ""));
        }

        @Override
        public boolean execute(Address[] args, int argc, Address returnAddress) {
            // todo: %<separator>l conversion for lists
            Object[] javaArgs;
            if (argc == 2) {
                if (args[1].hasType(Types.T_LIST)) {
                    javaArgs = new Object[args[1].getListHeap().length()];
                    for (int i = 0; i < javaArgs.length; i++) {
                        javaArgs[i] = args[1].getListHeap().get(i).toObject();
                    }
                } else {
                    javaArgs = new Object[]{args[1].toObject()};
                }
            } else {
                // argc == 1
                javaArgs = new Object[0];
            }
            System.out.printf(args[0].stringVal().toString(), javaArgs);
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
            returnAddress.setNull();
            return false;
        }
    }

    static class StrCodePointsFunction extends NativeFunctionPresent {

        StrCodePointsFunction() {
            super("str_code_points", ParamsData.create().add("str"));
        }

        @Override
        public boolean execute(Address[] args, int argc, Address returnAddress) {
            if (!args[0].testType(Types.T_STRING)) return false;
            StringHeap str = args[0].getStringHeap();
            ListHeap chars = new ListHeap(str.length());
            for (int i = 0; i < str.length(); ) {
                int cp = str.codePointAt(i);
                chars.get(i).set(cp);
                i += Character.charCount(cp);
            }
            returnAddress.set(chars);
            return true;
        }
    }

    static class _SizeOfFunction extends NativeFunctionPresent {

        _SizeOfFunction() {
            super("_sizeof", ParamsData.create().add("val"));
        }

        @Override
        public boolean execute(Address[] args, int argc, Address returnAddress) {
            returnAddress.set(ObjectSizeAnalyzing.analyzeSize(args[0]));
            return true;
        }
    }

    static class MSqrtFunction extends NativeFunctionPresent {

        MSqrtFunction() {
            super("m_sqrt", ParamsData.create().add("x"));
        }

        @Override
        public boolean execute(Address[] args, int argc, Address returnAddress) {
            Address doubleVal = new Address();
            if (args[0].doubleVal(doubleVal)) {
                returnAddress.set(Math.sqrt(doubleVal.getDouble()));
                return true;
            }
            return false;
        }
    }

    static class MCosFunction extends NativeFunctionPresent {

        MCosFunction() {
            super("m_cos", ParamsData.create().add("x"));
        }

        @Override
        public boolean execute(Address[] args, int argc, Address returnAddress) {
            Address doubleVal = new Address();
            if (args[0].doubleVal(doubleVal)) {
                returnAddress.set(Math.cos(doubleVal.getDouble()));
                return true;
            }
            return false;
        }
    }

    static class MSinFunction extends NativeFunctionPresent {

        MSinFunction() {
            super("m_sin", ParamsData.create().add("x"));
        }

        @Override
        public boolean execute(Address[] args, int argc, Address returnAddress) {
            Address doubleVal = new Address();
            if (args[0].doubleVal(doubleVal)) {
                returnAddress.set(Math.sin(doubleVal.getDouble()));
                return true;
            }
            return false;
        }
    }

    static class MAtan2Function extends NativeFunctionPresent {

        MAtan2Function() {
            super("m_atan2", ParamsData.create().add("x").add("y"));
        }

        @Override
        public boolean execute(Address[] args, int argc, Address returnAddress) {
            Address doubleValX = new Address();
            Address doubleValY = new Address();
            if (args[0].doubleVal(doubleValX) && args[1].doubleVal(doubleValY)) {
                returnAddress.set(Math.atan2(doubleValX.getDouble(), doubleValY.getDouble()));
                return true;
            }
            return false;
        }
    }

    static class MLogFunction extends NativeFunctionPresent {

        MLogFunction() {
            super("m_log", ParamsData.create().add("x"));
        }

        @Override
        public boolean execute(Address[] args, int argc, Address returnAddress) {
            Address doubleVal = new Address();
            if (args[0].doubleVal(doubleVal)) {
                returnAddress.set(Math.log(doubleVal.getDouble()));
                return true;
            }
            return false;
        }
    }

    static class MExpFunction extends NativeFunctionPresent {

        MExpFunction() {
            super("m_exp", ParamsData.create().add("x"));
        }

        @Override
        public boolean execute(Address[] args, int argc, Address returnAddress) {
            Address doubleVal = new Address();
            if (args[0].doubleVal(doubleVal)) {
                returnAddress.set(Math.exp(doubleVal.getDouble()));
                return true;
            }
            return false;
        }
    }

    static class MAbsFunction extends NativeFunctionPresent {

        MAbsFunction() {
            super("m_abs", ParamsData.create().add("x"));
        }

        @Override
        public boolean execute(Address[] args, int argc, Address returnAddress) {
            Address doubleVal = new Address();
            if (args[0].doubleVal(doubleVal)) {
                returnAddress.set(Math.abs(doubleVal.getDouble()));
                return true;
            }
            return false;
        }
    }

    static class StrCharArrayFunction extends NativeFunctionPresent {

        StrCharArrayFunction() {
            super("str_char_array", ParamsData.create().add("str"));
        }

        @Override
        public boolean execute(Address[] args, int argc, Address returnAddress) {
            Address str = args[0];
            if (!str.hasType(Types.T_STRING)) {
                InterpreterThread.threadError("str_char_array: str must be string");
                return false;
            }
            StringHeap strHeap = str.getStringHeap();
            ListHeap charArray = new ListHeap(strHeap.length());
            for (int i = 0; i < strHeap.length(); i++) {
                charArray.get(i).set(strHeap.subSequence(i, i + 1));
            }
            returnAddress.set(charArray);
            return true;
        }
    }

    static class StrCmpFunction extends NativeFunctionPresent {

        StrCmpFunction() {
            super("str_cmp", ParamsData.create().add("lhs").add("rhs"));
        }

        @Override
        public boolean execute(Address[] args, int argc, Address returnAddress) {
            Address lhs = args[0];
            Address rhs = args[1];
            if (!lhs.hasType(Types.T_STRING)) {
                InterpreterThread.threadError("str_cmp: lhs must be string");
                return false;
            }
            if (!rhs.hasType(Types.T_STRING)) {
                InterpreterThread.threadError("str_cmp: lhs must be string");
                return false;
            }
            returnAddress.set(lhs.getStringHeap().compareTo(rhs.getStringHeap()));
            return true;
        }
    }

    static class HashFunction extends NativeFunctionPresent {

        HashFunction() {
            super("hash", ParamsData.create().add("obj"));
        }

        @Override
        public boolean execute(Address[] args, int argc, Address returnAddress) {
            returnAddress.set(args[0].hashCode());
            return true;
        }
    }

    public static List<Function> getNativeFunctions() {
        return nfp.stream().map(NativeFunctionPresent::build).collect(Collectors.toList());
    }
}
