package jua.vm;

public final class ThreadData {

    private int state;
    private int cp;
    public int state() { return state; }

    public void state(int state) {
        this.state = state;
    }

    public void codePointer(int cp) {
        this.cp = cp;
    }

    public void callee(int callee) {

    }

    public void argc(int argc) {

    }
}
