package jua.runtime.memory;

import jua.runtime.interpreter.ThreadMemory;

import static jua.runtime.memory.Memories.setDoubleType;
import static jua.runtime.memory.MemoryArithms.compare;
import static jua.runtime.memory.MemoryArithms.inc;

public class MemoryTest {

    public static void main(String[] args) {
        int _i = 1;
        int _j = 2;

        Runnable r1 = new Runnable() {
            final BufferMemory m = new BufferMemory(128, 32);

            @Override
            public void run() {
                loop1(m, _i, _j, () -> {}, () -> {});
            }
        };

        Runnable r2 = new Runnable() {
            final ThreadMemory m = new ThreadMemory();

            @Override
            public void run() {
                m.acquire(5);
                loop2(m, _i, _j, () -> {}, () -> {});
                m.release(5);
            }
        };

        run(r1);
    }

    private static void run(Runnable runnable) {
        long sum = 0;
        for (int i = 0; i < 100; i++) {
            long a = System.currentTimeMillis();
            runnable.run();
            long b = System.currentTimeMillis();
            sum += (b - a);
            System.out.println((b - a) + "ms");
        }
        System.out.println("Avg: " + sum / 100 + "ms");
    }

    private static void loop1(Memory m, int i, int j, Runnable init, Runnable body) {
        setDoubleType(m, i, 0);
        setDoubleType(m, j, 10_000_000);
        init.run();

        while (compare(m, i, j, 1) < 0) {
            body.run();
            inc(m, i);
        }
    }

    private static void loop2(ThreadMemory m, int i, int j, Runnable init, Runnable body) {
        m.get(i).set(0.0);
        m.get(j).set(10_000_000.0);
        init.run();

        while (m.get(i).fastCompareWith(m.get(j), 1) < 0) {
            body.run();
            m.get(i).inc();
        }
    }
}
