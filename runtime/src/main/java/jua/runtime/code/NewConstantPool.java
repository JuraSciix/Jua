package jua.runtime.code;

import jua.runtime.heap.StringHeap;
import jua.runtime.interpreter.Address;
import jua.runtime.interpreter.InterpreterException;
import jua.runtime.memory.ReadOnlyMemory;

import static jua.runtime.Types.*;

public final class NewConstantPool {

    private final ReadOnlyMemory memory;

    public NewConstantPool(ReadOnlyMemory memory) {
        this.memory = memory;
    }

    public String getString(int index) {
        return (String) memory.getRefAt(index);
    }

    public ResolvableCallee getCallee(int index) {
        return (ResolvableCallee) memory.getRefAt(index);
    }

    public int getInt(int index) {
        return (int) memory.getLongAt(index);
    }

    public void readToAddress(int index, Address address) {
        byte type = memory.getTypeAt(index);
        switch (type) {
            case T_INT:
                address.set(memory.getLongAt(index));
                break;
            case T_FLOAT:
                address.set(memory.getDoubleAt(index));
                break;
            case T_BOOLEAN:
                address.set(memory.getLongAt(index) != 0);
                break;
            case T_STRING:
                String str = getString(index);
                address.set(new StringHeap(str));
                break;
            case T_NULL:
                address.setNull();
                break;
            default:
                throw new InterpreterException("Illegal type in constant pool: " + getTypeName(type));
        }
    }
}
