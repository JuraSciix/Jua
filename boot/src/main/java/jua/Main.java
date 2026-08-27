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
                .emit(Push, 50)
                .emitJump(IfLe, startHandle)
                .emit(Load0)
                .emit(Return)
                .toArray();
        DataArena arena = new DataArena();
        FrameData data = new FrameData();

        arena.allocate(4);
        SimpleInterpreter.run(0, 2, 0, code, arena, data);
        System.out.println("State: " + data.state());
        System.out.println(SimpleArena.toString(arena, 2));
        arena.deallocate(3);
    }

}
