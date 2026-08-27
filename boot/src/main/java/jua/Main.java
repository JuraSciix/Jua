package jua;

import jua.vm.FrameData;
import jua.vm.SimpleArena;
import jua.vm.SimpleInterpreter;
import jua.vm.arena.DataArena;

import static jua.vm.OPCodes.*;

public class Main {

    public static void main(String[] args) {
        Object testHandle = new Object();
        Object startHandle = new Object();

        int[] code = new CodeBuilder()
                .emit(ConstInt0)
                .emit(Store0)
                .emit(ConstInt1)
                .emit(Store1)
                .emitJump(Goto, testHandle)
                .resolveJump(startHandle)
                .emit(Load0)
                .emit(Load1)
                .emit(Add)
                .emit(Store0)
                .emit(Inc, 1)
                .resolveJump(testHandle)
                .emit(Load1)
                .emit(Push, 500000)
                .emitJump(IfLe, startHandle)
                .emit(Load0)
                .emit(Return)
                .toArray();
        DataArena arena = new DataArena();
        FrameData data = new FrameData();

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
