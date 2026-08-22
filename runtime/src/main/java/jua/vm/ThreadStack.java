package jua.vm;

public final class ThreadStack {
    private final ThreadRegion region;

    public ThreadStack(ThreadRegion region) {
        this.region = region;
    }

    public void validate() {
        if (!region.test()) {
            throw new InterpreterException("Corrupt");
        }
    }

    public int tos() {
        return region.stackPointer();
    }

    public void increment() {
        region.stackInc();
    }

    public void increment2() {
        region.stackInc();
        region.stackInc();
    }

    public void decrement() {
        region.stackDec();
    }

    public void decrement2() {
        region.stackDec();
        region.stackDec();
    }

    public void decrement3() {
        region.stackDec();
        region.stackDec();
        region.stackDec();
    }

    /**
     * Этот метод НЕ будет провоцировать расширение памяти.
     * Чтобы расширить память, надо использовать {@link #push(Address)}.
     *
     * @throws ArrayIndexOutOfBoundsException Если offset выходит за пределы выделенной памяти
     */
    public Address peek(int offset) {
        return region.stack(offset);
    }

    public Address peek1() {
        return peek(-1);
    }

    public Address peek2() {
        return peek(-2);
    }

    public Address peek3() {
        return peek(-3);
    }

    public Address popGet() {
        region.stackDec();
        return region.stackTop();
    }

    public void pop() {
        // Ячейка стека будет очищена при попытке выделить много памяти
        // или возврате из метода.
        decrement();
    }

    public void pop2() {
        decrement2();
    }

    public Address pushGet() {
        Address top = region.stackTop();
        region.stackInc();
        return top;
    }

    public void push(Address address) {
        region.stackTop().set(address);
        region.stackInc();
    }

    public void dup() {
        Address top = region.stack(-1);
        region.stackTop().set(top);
        region.stackInc();
    }

    public void dupX1() {
        // Нужно переместить 2 элемента на 1 позицию вправо
        // Затем последний элемент скопировать в элемент на 2 позиции левее.

        // -2 -1  0
        //  A  B
        //  B  A  B
        peek(0).set(peek(-1));
        peek(-1).set(peek(-2));
        peek(-2).set(peek(0));
        increment();
    }

    public void dupX2() {
        // Нужно переместить 3 элемента на 2 позиции вправо
        // Затем последний элемент скопировать в элемент на 3 позиции левее.

        // -3 -2 -1  0
        //  C  B  A
        //  A  C  B  A
        peek(0).set(peek(-1));
        peek(-1).set(peek(-2));
        peek(-2).set(peek(-3));
        peek(-3).set(peek(0));
        increment();
    }

    public void dup2() {
        peek(0).set(peek(-2));
        peek(1).set(peek(-1));
        increment2();
    }

    public void dup2X1() {
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
        increment2();
    }

    public void dup2X2() {
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
        increment2();
    }
}
