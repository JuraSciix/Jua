package jua.runtime.interpreter;

import jua.runtime.Types;
import jua.runtime.interpreter.memory.Address;
import jua.runtime.interpreter.memory.AddressUtils;

public final class ThreadStack {

    private Address[] data;

    private int tos = 0;

    public ThreadStack() {
        // todo: настройка изначального размера стека опцией.
        // С текущим подходом к заполнению памяти стека, стек заполняется ОЧЕНЬ медленно,
        // поэтому 16 для начала, думаю, хватит за глаза.
        data = AddressUtils.allocateMemory(16, 0);
    }

    public int tos() {
        return tos;
    }

    /**
     * @deprecated Use {@link #peek(int)}
     */
    public Address getStackAddress(int offset) {
        checkAndGrow(offset);
        return peek(offset);
    }

    /**
     * @deprecated Use {@link #push(Address)} and {@link #pop()}
     */
    public void addTos(int tos) {
        this.tos += tos;
        if (this.tos < 0) {
            throw new RuntimeException("Tos < 0");
        }
    }

    /**
     * Очищает все адреса по адресам >= заданного значения.
     */
    public void clear(int base) {
        for (int i = base; i < data.length; i++) {
            data[i].reset();
        }
    }

    public Address peek(int offset) {
        return data[tos + offset];
    }

    public Address pop() {
        tos--;
        checkAndShrink();
        return data[tos];
    }

    public void pushInt(long value) {
        push().set(value);
    }

    public void pushFloat(float value) {
        push().set(value);
    }

    public Address push() {
        checkAndGrow(0);
        Address a = data[tos];
        tos++;
        return a;
    }

    public void push(Address address) {
        push().set(address);
    }

    private void checkAndShrink() {
        // todo: чтобы добавлять оптимизацию с сокращением стека,
        //  нужно убедиться, что это не приведет к гарантированной деградации в некоторых случаях.
        //  Для этого нужны метки из исходного кода, наподобие: тут цикл, после сокращения может опять
        //  потребоваться расширение...
    }

    private void checkAndGrow(int cap) {
        // Если вызывать этот метод заранее, то получится ленивое расширение. +100 к оптимизации
        if (data.length - tos <= cap) {
            data = AddressUtils.reallocateWithNewLength(data, (tos + cap) * 2);
        }
    }

    private int prevTos = 0;
    public void debugUpdate(String insnName) {
        int t = tos - prevTos;
        prevTos = tos;
        if (insnName==null)
            return;
        // \033[38;5;11m
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            String c = "";
            if (data[i].getType() == Types.T_UNDEFINED) {
                c = "\033[38;5;197m"; // pink
            }
            if (i < tos) {
                c = "\033[38;5;11m"; // yellow
            }
            int dup = -1;
            for (int j = 0; j < i; j++) {
                if (data[i].isEqualRefs(data[j])) {
//                    c = "\033[38;5;40m"; // green
                    dup = j;
                    break;
                }
            }
            if (i > 0) {
                buf.append(" | ");
            }
            String s = dup >= 0 ? "@" + dup : data[i].id();
            buf.append(c).append(String.format("%-5s", s)).append("\033[0m");
        }
        System.out.printf("%-13s tos=%02d%2s { %s } %n", insnName + ": ", this.tos-t, t >= 0 ? "+" + t : "-" + (-t), buf);
    }
}
