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

    public void validate() {
        if (tos < 0) {
            throw new InterpreterException("tos < 0");
        }
        // tos может быть равен data.length какое-то время.
    }

    public int tos() {
        return tos;
    }

    public void tos(int tos) {
        this.tos = tos;
    }

    /**
     * @deprecated Use {@link #peek(int)}
     */
    public Address getStackAddress(int offset) {
        ensureCapacity(offset + 1);
        return peek(offset);
    }

    /**
     * @deprecated Use {@link #push(Address)} and {@link #popGet()}
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
    public void cleanup() {
        for (int i = tos; i < data.length; i++) {
            data[i].reset();
        }
    }

    /**
     * Этот метод НЕ будет провоцировать расширение памяти.
     * Чтобы расширить память, надо использовать {@link #push(Address)}.
     *
     * @throws ArrayIndexOutOfBoundsException Если offset выходит за пределы выделенной памяти
     */
    public Address peek(int offset) {
        return data[tos + offset];
    }

    public Address popGet() {
        tos--;
        checkAndShrink();
        return data[tos];
    }

    public void pop() {
        // Ячейка стека будет очищена при попытке выделить много памяти
        // или возврате из метода.
        tos--;
    }

    public void pop2() {
        tos -= 2;
    }

    public void pushInt(long value) {
        pushGet().set(value);
    }

    public void pushFloat(float value) {
        pushGet().set(value);
    }

    public Address pushGet() {
        ensureCapacity(0);
        Address a = data[tos];
        tos++;
        return a;
    }

    public void push(Address address) {
        pushGet().set(address);
    }

    private void checkAndShrink() {
        // todo: чтобы добавлять оптимизацию с сокращением стека,
        //  нужно убедиться, что это не приведет к гарантированной деградации в некоторых случаях.
        //  Для этого нужны метки из исходного кода, наподобие: тут цикл, после сокращения может опять
        //  потребоваться расширение...
    }

    private void ensureCapacity(int cap) {
        // Если вызывать этот метод заранее, то получится ленивое расширение. +100 к оптимизации
        if (data.length - tos < cap) {
            data = AddressUtils.reallocateWithNewLength(data, (tos + cap) * 2);
        }
    }

    public void dup() {
        ensureCapacity(1);
        // -1  0
        //  A
        //  A  A
        peek(0).set(peek(-1));
        tos += 1;
    }

    public void dupX1() {
        ensureCapacity(1);
        // Нужно переместить 2 элемента на 1 позицию вправо
        // Затем последний элемент скопировать в элемент на 2 позиции левее.

        // -2 -1  0
        //  A  B
        //  B  A  B
        peek(0).set(peek(-1));
        peek(-1).set(peek(-2));
        peek(-2).set(peek(0));
        tos += 1;
    }

    public void dupX2() {
        ensureCapacity(1);
        // Нужно переместить 3 элемента на 2 позиции вправо
        // Затем последний элемент скопировать в элемент на 3 позиции левее.

        // -3 -2 -1  0
        //  C  B  A
        //  A  C  B  A
        peek(0).set(peek(-1));
        peek(-1).set(peek(-2));
        peek(-2).set(peek(-3));
        peek(-3).set(peek(0));
        tos += 1;
    }

    public void dup2() {
        ensureCapacity(2);
        peek(0).set(peek(-2));
        peek(1).set(peek(-1));
        tos += 2;
    }

    public void dup2X1() {
        ensureCapacity(2);
        // Нужно переместить 3 элемента на 2 позиции вправо
        // Затем 2 последних элементах скопировать в элементы на 3 позиции левее.

        // -3 -2 -1  0  1
        //  H  A  B  _  _
        //  _  _  H  A  B
        //  A  B  H  A  B
        peek(1).set(peek(-1));
        peek(0).set(peek(-2));
        peek(-1).set(peek(-3));
        peek(-2).set(peek(1));
        peek(-3).set(peek(0));
        tos += 2;
    }

    public void dup2X2() {
        ensureCapacity(2);
        // Нужно переместить 4 элемента на 2 позиции вправо
        // Затем 2 последних элементах скопировать в элементы на 4 позиции левее.

        // -4 -3 -2 -1  0  1
        //  G  H  A  B  _  _
        //  G  H  G  H  A  B
        //  A  B  G  H  A  B

        peek(1).set(peek(-1));
        peek(0).set(peek(-2));
        peek(-1).set(peek(-3));
        peek(-3).set(peek(1));
        peek(-4).set(peek(10));
        tos += 2;
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
        System.out.printf("%-16s tos=%02d%2s { %s } %n", insnName + ": ", this.tos-t, t >= 0 ? "+" + t : "-" + (-t), buf);
    }
}
