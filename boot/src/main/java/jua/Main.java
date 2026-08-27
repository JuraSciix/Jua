package jua;

import jua.vm.FrameData;
import jua.vm.SimpleInterpreter;
import jua.vm.arena.DataArena;

import static jua.vm.OPCodes.*;

public class Main {

    public static void main(String[] args) {
        Object contHandle = new Object();
        Object exitHandle = new Object();
        int[] code = new CodeBuilder()
                .emit(ConstInt0)
                .emit(Store0)
                .resolveJump(contHandle)
                .emit(Load0)
                .emit(Push, 0)
                .emitJump(IfGe, exitHandle)
                .emit(Inc, 0)
                .emitJump(Goto, contHandle)
                .resolveJump(exitHandle)
                .toArray();
        DataArena arena = new DataArena();
        FrameData data = new FrameData();

        for (int i = 0; i < 100; i++) {
            long time0 = System.nanoTime();
            arena.allocate(20);
            SimpleInterpreter.run(0, 10, 0, code, arena, data);
            arena.deallocate(20);
            long time1 = System.nanoTime();
            System.out.println((time1 - time0) / 1E6);
        }
    }
}
