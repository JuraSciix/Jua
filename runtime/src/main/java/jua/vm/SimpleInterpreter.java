package jua.vm;

import jua.vm.arena.DataArena;

import static jua.vm.OPCodes.*;
import static jua.vm.SimpleArena.*;
import static jua.vm.SimpleArithm.*;

public class SimpleInterpreter {
    private static final int MASK_CP = 0xffff;
    private static final int MASK_STACK = 0xffff;
    private static final int MASK_INST = 0xff;

    // cs - место, откуда начать выполнение
    // sb - базовый указатель на вершину стека
    // rb - указатель на начало регистровой области
    public static void run(int cs, int sb, int rb, int[] code, DataArena arena, FrameData data) {
        int cp = cs & MASK_CP; // Code Pointer = Code Start
        int sp = sb & MASK_STACK; // Stack Pointer = Stack Base

        int state = STATE_DONE;
        while (state == STATE_DONE) {
            int inst = code[cp] & MASK_INST;
            int payload = code[cp] >>> 8;
            cp++;

            switch (inst) {
                case _nop:
                    break;
                case _const_null:
                    putNull(arena, sp);
                    sp++;
                    break;
                case _const_true:
                case _const_false:
                    putBool(arena, sp, inst == _const_true);
                    sp++;
                    break;
                case _const_i_m1:
                case _const_i_0:
                case _const_i_1:
                case _const_i_2:
                    putInt64(arena, sp, inst - _const_i_0);
                    sp++;
                    break;

                case _dup:
                case _dup_x1:
                case _dup_x2:
                case _swap:
                case _dup2:
                case _dup2_x1:
                case _dup2_x2:
                    // ...
                    break;

                case _push:
                    putInt64(arena, sp, payload);
                    sp++;
                    break;

                case _new_list:
                    // ...
                    break;

                case _pop:
                    sp--;
                    break;
                case _pop2:
                    sp -= 2;
                    break;
                case _add:
                    state = add(arena, sp);
                    sp--;
                    break;
                case _sub:
                    state = sub(arena, sp);
                    sp--;
                    break;
                case _mul:
                    state = mul(arena, sp);
                    sp--;
                    break;
                case _div:
                    state = div(arena, sp);
                    sp--;
                    break;
                case _rem:
                case _bit_and:
                case _bit_or:
                case _bit_xor:
                case _bit_shl:
                case _bit_shr:
                case _len:
                case _pos:
                case _neg:
                case _bit_inv:
                    // ...
                    break;

                case _load:
                    arena.move(rb + payload, sp);
                    sp++;
                    break;
                case _load0:
                case _load1:
                case _load2:
                    arena.move(rb + inst - _load0, sp);
                    sp++;
                    break;
                case _store:
                    arena.move(sp - 1, rb + payload);
                    sp--;
                    break;
                case _store0:
                case _store1:
                case _store2:
                    arena.move(sp - 1, rb + inst - _store0);
                    sp--;
                    break;
                case _inc:
                    state = inc(arena, rb + payload, 1L);
                    break;
                case _dec:
                    state = inc(arena, rb + payload,-1L);
                    break;

                case _a_load:
                case _a_store:
                case _a_inc:
                case _a_dec:
                    // ...
                    break;

                case _goto:
                    cp = payload;
                    break;

                case _if_eq: case _if_ne:
                case _if_ge: case _if_lt:
                case _if_le: case _if_gt:
                    sp -= 2;
                    if ((inst == _if_ne) ^ compare(arena, sp + 2,
                            inst == _if_ne || inst == _if_eq || inst == _if_ge || inst == _if_le,
                            inst == _if_ge || inst == _if_gt,
                            inst == _if_le || inst == _if_lt)) {
                        cp = payload;
                    }
                    break;

                case _if_z:
                case _if_nz:
                    sp -= 1;
                    if (compareInt64Zero(arena, sp + 1) == (inst == _if_z)) {
                        cp = payload;
                    }
                    break;

                case _if_n:
                case _if_nn:
                    sp -= 1;
                    if (compareRefNull(arena, sp + 1) == (inst == _if_n)) {
                        cp = payload;
                    }
                    break;

                case _call:
                    // ...
                    break;

                case _leave:
                    putNull(arena, sp);
                    sp++;
                    // fallthrough
                case _return:
                    state = STATE_LEAVE;
                    break;

                default:
                    // Перед выполнением код проходит валидацию.
                    // Поэтому этот сценарий считается невозможным.
                    throw new AssertionError(String.format(
                            "cp=%d sb=%d sp=%d rb=%d inst=%d payload=%d",
                            cp, sb, sp, rb, inst, payload));
            }

            // Хак, чтобы заставить javac сворачивать транзитивные прыжки:
            // Сворачивая этот лишний прыжок, javac каскадным образом свернёт и все транзитивные.
            // Транзитивные прыжки - это break внутри switch-cases.
            //noinspection UnnecessaryContinue
            continue;
        }

        data.state(state);
        // Мы всегда инкрементируем CP перед выполнением,
        // поэтому вычитаем 1.
        data.codePointer(cp - 1);
        data.stackPointer(sp);
    }
}
