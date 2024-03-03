package jua.runtime.interpreter;

public final class FrameFactory {

    private final InterpreterFrame[] frames;

    private int top = 0;

    public FrameFactory() {
        int p = 128;
        frames = new InterpreterFrame[p];
        for (int i = 0; i < p; i++) {
            frames[i] = new InterpreterFrame();
        }
    }

    public InterpreterFrame allocate() {
        InterpreterFrame frame;
        if (top >= frames.length) {
            frame = new InterpreterFrame();
        } else {
            frame = frames[top];
        }
        top++;
        return frame;
    }

    public void release() {
        top--;
    }
}
