package jua.runtime.interpreter;

public class Histogram {

    private static class NoOpHistogram extends Histogram {

        @Override
        public void start(int opcode) {
        }

        @Override
        public void end(int opcode) {
        }

        @Override
        public void print() {
        }
    }

    private static final Histogram[] instances = {
            new NoOpHistogram(),
            new Histogram()
    };

    private static int key = 0;

    public static void enable() {
        key = 1;
    }

    public static void disable() {
        key = 0;
    }

    public static Histogram get() {
        return instances[key];
    }

    public static boolean isEnabled() {
        return key == 1;
    }

    private final long[] measurements = new long[OPCodes._InstrCount];
    private final int[] counters = new int[OPCodes._InstrCount];

    private final long[] starts = new long[OPCodes._InstrCount];

    public void start(int opcode) {
        starts[opcode] = System.nanoTime();
    }

    public void end(int opcode) {
        if (starts[opcode] > 0) {
            measurements[opcode] += System.nanoTime() - starts[opcode];
            counters[opcode]++;
            starts[opcode] = 0;
        }
    }

    public void print() {
        System.out.println("=== HISTOGRAM BEGIN ===");
        int limit = 100;
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < OPCodes._InstrCount; i++) {
            long avg = counters[i] == 0 ? 0 : measurements[i] / counters[i];
            if (avg > limit) {
                buf.append("\033[38;5;196m");
                avg = limit;
            }

            int x = (int) (49 * (1d * avg / limit));
            for (int j = 0; j < x; j++) {
                buf.append('=');
            }
            if (avg >= limit) {
                buf.append('>');
            } else {
                for (int j = x; j < 50; j++) {
                    buf.append(' ');
                }
            }


            buf.append("\033[0m");
            System.out.printf("%-20s: %-10s | %-51s %d ns/op%n", OPCodes.NAMES[i] + ':', counters[i] + "x", buf, avg);
            buf.setLength(0);
        }
        System.out.println("===  HISTOGRAM END  ===");
    }
}
