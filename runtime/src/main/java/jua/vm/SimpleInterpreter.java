package jua.vm;

import jua.vm.arena.DataArena;

import static jua.vm.OPCodes.*;
import static jua.vm.SimpleArithm.STATE_DONE;
import static jua.vm.SimpleArithm.STATE_LEAVE;
import static jua.vm.SimpleType.*;

public class SimpleInterpreter {
    public static final int MASK_CP = 0xffff;
    public static final int MASK_REG = 0xff;

    // cs - место, откуда начать выполнение
    // sb - базовый указатель на вершину стека
    // rb - указатель на начало регистровой области
    public static void run(int cs, int sb, int rb, int[] code, DataArena arena, FrameData data) {
        int cp = cs & MASK_CP; // Code Pointer = Code Start
        int sp = sb; // Stack Pointer = Stack Base

        int state = STATE_DONE;
        while (state == STATE_DONE) {
            // Код проходит валидацию, поэтому в конце обязательно присутствует
            // инструкция Leave/Return, за которыми следует два нуля.
            // Однако, для JIT требуется эта проверка.
            if ((cp + 1) * 2 >= code.length) {
                throw new AssertionError();
            }
            int inst = code[cp * 2];
            int payload = code[cp * 2 + 1];
            cp++;

            switch (inst) {
                case Nop:
                    break;
                case ConstNull:
                    arena.writeType(sp, TYPE_REF);
                    arena.writeReference(sp, null);
                    sp++;
                    break;
                case ConstTrue:
                case ConstFalse:
                    arena.writeType(sp, TYPE_BOOL64);
                    arena.writeReference(sp, inst == ConstTrue);
                    sp++;
                    break;
                case ConstIntM1:
                case ConstInt0:
                case ConstInt1:
                case ConstInt2:
                    arena.writeType(sp, TYPE_INT64);
                    arena.writeInt64(sp, inst - ConstInt0);
                    sp++;
                    break;

                // ...

                case Push:
                    arena.writeType(sp, TYPE_INT64);
                    arena.writeInt64(sp, payload);
                    sp++;
                    break;

                // ...

                // Заметка: инструкции Pop/Pop2 несут ответственность за ссылки, оставляемые на стеке.

                case Pop:
                    arena.clear(sp - 1);
                    sp--;
                    break;
                case Pop2:
                    arena.clear(sp - 2);
                    arena.clear(sp - 1);
                    sp -= 2;
                    break;
                case Add:
                    state = SimpleArithm.add(arena, sp);
                    sp--;
                    break;
                case Sub:
                    state = SimpleArithm.sub(arena, sp);
                    sp--;
                    break;
                case Mul:
                    state = SimpleArithm.mul(arena, sp);
                    sp--;
                    break;
                case Div:
                    state = SimpleArithm.div(arena, sp);
                    sp--;
                    break;

                // ...

                case Load:
                    arena.clearAndMove(rb + payload & MASK_REG, sp);
                    sp++;
                    break;
                case Load0:
                case Load1:
                case Load2:
                    arena.clearAndMove(rb + inst - Load0, sp);
                    sp++;
                    break;
                case Store:
                    arena.clearAndMove(sp - 1, rb + payload & MASK_REG);
                    sp--;
                    break;
                case Store0:
                case Store1:
                case Store2:
                    arena.clearAndMove(sp - 1, rb + inst - Store0);
                    sp--;
                    break;
                case Inc:
                case Dec:
                    state = SimpleArithm.inc(arena, rb + payload & MASK_REG, inst == Inc ? 1L : -1L);
                    break;

                // ...

                case Goto:
                    cp = payload & MASK_CP;
                    break;

                case IfEq: case IfNe: // 49, 50
                case IfGe: case IfLt: // 51, 52
                case IfLe: case IfGt: // 53, 54
                    if (SimpleArithm.compare(arena, sp,
                            inst == IfNe || inst == IfEq || inst == IfGe || inst == IfLe,
                            inst == IfGe || inst == IfGt,
                            inst == IfLe || inst == IfLt) == (inst != IfNe)) {
                        cp = payload & MASK_CP;
                    }
                    sp -= 2;
                    break;

                case IfZ:
                case IfNz:
                    if (SimpleArithm.compareInt64Zero(arena, sp) == (inst == IfZ)) {
                        cp = payload & MASK_CP;
                    }
                    sp -= 1;
                    break;

                case IfNull:
                case IfNonNull:
                    if (SimpleArithm.compareRefNull(arena, sp) == (inst == IfNull)) {
                        cp = payload & MASK_CP;
                    }
                    sp -= 1;
                    break;

                // ...

                case Leave:
                    arena.writeType(sp, TYPE_REF);
                    arena.writeReference(sp, null);
                    sp++;
                    // fallthrough
                case Return:
                    state = STATE_LEAVE;
                    break;

                default:
                    // Перед выполнением код проходит валидацию.
                    // Поэтому этот сценарий считается невозможным.
                    throw new AssertionError(String.format(
                            "cp=%d sb=%d sp=%d rb=%d inst=%d payload=%d",
                            cp, sb, sp, rb, inst, payload));
            }
        }

        data.state(state);
        // Мы всегда инкрементируем CP перед выполнением,
        // поэтому вычитаем 1.
        data.codePointer(cp - 1);
        data.stackPointer(sp);
    }
}
