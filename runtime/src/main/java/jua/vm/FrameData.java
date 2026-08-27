package jua.vm;

public final class FrameData {

    private int state;
    private int cp;
    private int sp;

    public int state() { return state; }

    public void state(int state) {
        this.state = state;
    }

    public void codePointer(int cp) {
        this.cp = cp;
    }

    public void stackPointer(int sp) {
        this.sp = sp;
    }
}
