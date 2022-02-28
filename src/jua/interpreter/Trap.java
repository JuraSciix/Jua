package jua.interpreter;

// Скопировано из другой версии

/**
 * Mechanism for delegating execution between the interpreter and the frame, based on Java exceptions.
 */
// todo: заменить на InterpreterState.Message
public final class Trap extends Error {

    /**
     * Back To Interpreter state.
     */
    // Использовалось для подмена фреймов при вызове/выходе из функций
    public static final int STATE_BTI = 1;

    /**
     * Halt state.
     */
    public static final int STATE_HALT = 2;

    /**
     * Triggers a {@link #STATE_BTI} trap.
     */
    public static void bti() {
        throw new Trap(STATE_BTI);
    }

    /**
     * Triggers a {@link #STATE_HALT} trap.
     */
    public static void halt() {
        throw new Trap(STATE_HALT);
    }

    private final int state;

    private Trap(int state) {
        super(null, null, false, false);
        this.state = state;
    }

    /**
     * @return state of this trap instance.
     */
    public int state() {
        return state;
    }

    public boolean isState(int s) {
        return s == state;
    }
}
