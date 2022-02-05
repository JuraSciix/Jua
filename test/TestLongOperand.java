import jua.interpreter.runtime.LongOperand;

public class TestLongOperand {

    public static void main(String[] args) {
        // cache pool bounds: [-128; 127]
        for (long x = (-128 - 1); x <= (127 + 1); x++) {
            LongOperand wrapper_x = LongOperand.valueOf(x);
            if (wrapper_x.longValue() != x) {
                throw new AssertionError(x);
            }
        }
        System.out.println("Done");
    }
}
