package jua;

import jua.compiler.Code;
import jua.compiler.InstructionUtils;
import jua.compiler.LineNumberTable;
import jua.compiler.Module;
import jua.runtime.Function;
import jua.runtime.code.CodeData;
import jua.runtime.code.ConstantPool;
import jua.runtime.code.ResolvableCallee;
import jua.runtime.interpreter.instruction.Instruction;
import jua.runtime.interpreter.Address;
import jua.runtime.interpreter.AddressSupport;

import java.util.Arrays;

public class Executable2FunctionTranslator {

    public static Function translate(Module.Executable executable) {
        return new Function(
                executable.name,
                executable.fileName,
                executable.reqargs,
                executable.totargs,
                // Первые переменные это всегда параметры
                Arrays.copyOfRange(executable.varnames, 0, executable.totargs),
                Arrays.stream(executable.defs).map(o -> {
                    Address a = new Address();
                    AddressSupport.assignObject(a, o);
                    return a;
                }).toArray(Address[]::new),
                executable.flags,
                new CodeData(
                        executable.stackSize,
                        executable.regSize,
                        executable.varnames,
                        translateCode(executable.code), // todo
                        getConstantPool(executable.constantPool),
                        toLineNumTable(executable.lineNumberTable)
                ),
                null
        );
    }

    private static ConstantPool getConstantPool(Object[] values) {
        Object[] runtimeElements = new Object[values.length];
        for (int i = 0; i < values.length; i++) {
            if (runtimeElements[i] != null)
                continue;
            if (values[i] instanceof Code.Callee) {
                runtimeElements[i] = new ResolvableCallee(((Code.Callee) values[i]).utf8);
            } else {
                runtimeElements[i] = values[i];
            }
        }
        return new ConstantPool(runtimeElements);
    }

    private static jua.runtime.code.LineNumberTable toLineNumTable(LineNumberTable lnt) {
        return new jua.runtime.code.LineNumberTable(lnt.codePoints, lnt.lineNumbers);
    }

    private static Instruction[] translateCode(InstructionUtils.InstrNode[] a) {
        OPCodeTranslator t = new OPCodeTranslator();
        Arrays.stream(a).forEach(i -> i.accept(t));
        return t.getInstructions().toArray(new Instruction[0]);
    }
}
