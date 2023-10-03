package jua.runtime.interpreter;

import jua.runtime.interpreter.memory.Address;
import jua.runtime.interpreter.memory.MemoryStack;
import jua.runtime.Function;
import jua.runtime.code.CodeData;

public class ACallStack implements CallStack {

    private final SingleInterpreterFrame[] frames;

    private final RewriteableInterpreterState[] states;

    private final MemoryStack frameStackMemMgr;
    private final MemoryStack frameSlotsMemMgr;

    private int top = 1; // frames[0] всегда ссылается на нуль, так как является системным вызовом (entry point).

    public ACallStack(int capacity,
                      MemoryStack frameStackMemMgr,
                      MemoryStack frameSlotsMemMgr) {
        if (capacity < 1) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }
        frames = new SingleInterpreterFrame[capacity];
        states = new RewriteableInterpreterState[capacity];
        for (int i = 1; i < capacity; i++) {
            frames[i] = new SingleInterpreterFrame();
            states[i] = new RewriteableInterpreterState();
        }
        this.frameStackMemMgr = frameStackMemMgr;
        this.frameSlotsMemMgr = frameSlotsMemMgr;
    }

    @Override
    public void push(Function function, Address returnAddress) {
        InterpreterFrame caller = current();
        SingleInterpreterFrame frame = frames[top];
        frame.setFunction(function);
        RewriteableInterpreterState state = null;
        if (function.isUserDefined()) {
            state = states[top];
            state.setCp(0);
            state.setTos(0);
            CodeData cd = function.userCode();
            state.setStack(frameStackMemMgr.allocate(cd.stack));
            state.setSlots(frameSlotsMemMgr.allocate(cd.locals));
            frame.setState(state);
        }
        frame.setReturnAddress(returnAddress);
        frame.setCaller(caller);
        top++;
    }

    @Override
    public void pop() {
        InterpreterFrame lastFrame = current();
        if (lastFrame == null)
            return;
        --top;
        if (lastFrame.getFunction().isUserDefined()) {
            CodeData cd = lastFrame.getFunction().userCode();
            frameStackMemMgr.free(cd.stack);
            frameSlotsMemMgr.free(cd.locals);
        }
    }

    @Override
    public InterpreterFrame current() {
        return top < 1 ? null : frames[top - 1];
    }
}
