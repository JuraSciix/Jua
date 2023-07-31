package jua.interpreter;

import jua.interpreter.address.Address;
import jua.runtime.Function;
import jua.runtime.StackTraceElement;

public final class InterpreterFrame {

    public final InterpreterFrame prev;

    public final Function owner;

    public final InterpreterState state;

    public final Address returnAddress;

    InterpreterFrame(InterpreterFrame prev, Function owner, InterpreterState state, Address returnAddress) {
        // Trusting constructor
        this.prev = prev;
        this.owner = owner;
        this.state = state;
        this.returnAddress = returnAddress;
    }

    public InterpreterFrame prev() { return prev; }
    public Function owner() { return owner; }
    public InterpreterState state() { return state; }
    public Address returnAddress() { return returnAddress; }

    /** Возвращает номер строки, которая сейчас выполняется. */
    int executingLineNumber() {
        if (state == null) return -1; // native function
        int cp = state.getCp();
        return owner.userCode().lineNumTable.getLineNumber(cp);
    }

    StackTraceElement toStackTraceElement() {
        return new StackTraceElement(owner.module, owner.name, executingLineNumber());
    }
}
