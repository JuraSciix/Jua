package jua;

import jua.vm.simple.ThreadData;
import jua.vm.simple.SimpleInterpreter;
import jua.vm.arena.DataArena;

import static jua.vm.OPCodes.*;

public class Main {

    public static void main(String[] args) {
        Object testHandle = new Object();
        Object startHandle = new Object();

        int[] code = new CodeBuilder()
                .emit(_const_i_0)
                .emit(_store0)
                .emit(_const_i_1)
                .emit(_store1)
                .emitJump(_goto, testHandle)
                .resolveJump(startHandle)
                .emit(_load0)
                .emit(_load1)
                .emit(_add)
                .emit(_store0)
                .emit(_inc, 1)
                .resolveJump(testHandle)
                .emit(_load1)
                .emit(_push, 1_000_000)
                .emitJump(_if_le, startHandle)
                .emit(_load0)
                .emit(_return)
                .toArray();
        DataArena arena = new DataArena();
        ThreadData data = new ThreadData();

        arena.allocate(4);

        long sum = 0;
        for (int i = 0; i < 1000; i++) {
            long time1 = System.nanoTime();
            SimpleInterpreter.run(0, 2, 0, code, arena, data);
            long time2 = System.nanoTime();
            if (i >= 10) {
                sum += (time2 - time1) / 1000;
            }
            System.out.println((time2 - time1) / 1000);
        }
        System.out.println("Average: " + sum / 1000);
        arena.deallocate(3);
    }

}
