package jua;

import jua.compiler.Executable;
import jua.compiler.InstructionUtils;
import jua.interpreter.instruction.Instruction;
import jua.interpreter.memory.Address;
import jua.interpreter.memory.AddressUtils;
import jua.runtime.Function;
import jua.runtime.code.CodeData;

import java.util.Arrays;

public class Executable2FunctionTranslator {

    public static Function translate(Executable executable) {
        if (executable == null) {
            // Native
            return null;
        }
        return new Function(
                executable.name,
                executable.fileName,
                executable.reqargs,
                executable.totargs,
                // Первые переменные это всегда параметры
                Arrays.copyOfRange(executable.varnames, 0, executable.totargs),
                Arrays.stream(executable.defs).map(o -> {
                    Address a = new Address();
                    AddressUtils.assignObject(a, o);
                    return a;
                }).toArray(Address[]::new),
                0L,
                new CodeData(
                        executable.stackSize,
                        executable.reqargs,
                        executable.varnames,
                        translateCode(executable.code), // todo
                        executable.constantPool,
                        executable.lineNumberTable
                ),
                null
        );
    }

    private static Instruction[] translateCode(InstructionUtils.InstrNode[] a) {
        OPCodeTranslator t = new OPCodeTranslator();
        Arrays.stream(a).forEach(i -> i.accept(t));
        return t.getInstructions().toArray(new Instruction[0]);
    }
}
