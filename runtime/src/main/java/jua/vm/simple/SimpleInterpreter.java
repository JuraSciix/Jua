package jua.vm.simple;

import jua.vm.arena.DataArena;

import static jua.vm.simple.Fetch.decodeArgc;
import static jua.vm.simple.Fetch.decodeCallee;
import static jua.vm.OPCodes.*;
import static jua.vm.simple.SimpleArena.*;
import static jua.vm.simple.SimpleArithm.*;

public class SimpleInterpreter {
    private static final int MASK_CP = 0xffff;
    private static final int MASK_STACK = 0xffff;
    private static final int MASK_INST = 0xff;

    // cs - место, откуда начать выполнение
    // sb - базовый указатель на вершину стека
    // rb - указатель на начало регистровой области
    public static void run(int cs, int sb, int rb, int[] code, DataArena arena, ThreadData data) {
        int cp = cs & MASK_CP; // Code Pointer = Code Start
        int sp = sb & MASK_STACK; // Stack Pointer = Stack Base

        int state = STATE_DONE;
        while (state == STATE_DONE) {
            int inst = code[cp] & MASK_INST;
            int payload = code[cp] >>> 8;
            cp++;

            switch (inst) {
                case _nop:
                    continue;
                case _const_null:
                    putNull(arena, sp);
                    sp++;
                    continue;
                case _const_true:
                case _const_false:
                    putBool(arena, sp, inst == _const_true);
                    sp++;
                    continue;
                case _const_i_m1:
                case _const_i_0:
                case _const_i_1:
                case _const_i_2:
                    putInt64(arena, sp, inst - _const_i_0);
                    sp++;
                    continue;

                case _dup:
                case _dup_x1:
                case _dup_x2:
                case _swap:
                case _dup2:
                case _dup2_x1:
                case _dup2_x2:
                    // ...
                    continue;

                case _push:
                    putInt64(arena, sp, payload);
                    sp++;
                    continue;

                case _new_list:
                    // ...
                    continue;

                case _pop:
                    sp--;
                    continue;
                case _pop2:
                    sp -= 2;
                    continue;
                case _add:
                    state = add(arena, sp);
                    sp--;
                    continue;
                case _sub:
                    state = sub(arena, sp);
                    sp--;
                    continue;
                case _mul:
                    state = mul(arena, sp);
                    sp--;
                    continue;
                case _div:
                    state = div(arena, sp);
                    sp--;
                    continue;
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
                    continue;

                case _load:
                    arena.move(rb + payload, sp);
                    sp++;
                    continue;
                case _load0:
                case _load1:
                case _load2:
                    arena.move(rb + inst - _load0, sp);
                    sp++;
                    continue;
                case _store:
                    arena.move(sp - 1, rb + payload);
                    sp--;
                    continue;
                case _store0:
                case _store1:
                case _store2:
                    arena.move(sp - 1, rb + inst - _store0);
                    sp--;
                    continue;
                case _inc:
                    state = inc(arena, rb + payload, 1L);
                    continue;
                case _dec:
                    state = inc(arena, rb + payload,-1L);
                    continue;

                case _a_load:
                case _a_store:
                case _a_inc:
                case _a_dec:
                    // ...
                    continue;

                case _goto:
                    cp = payload;
                    continue;

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
                    continue;

                case _if_bz:
                case _if_nbz:
                    sp -= 1;
                    if (compareBool(arena, sp + 1) == (inst == _if_bz)) {
                        cp = payload;
                    }
                    continue;

                case _if_null:
                case _if_nnull:
                    sp -= 1;
                    if (compareNullPtr(arena, sp + 1) == (inst == _if_null)) {
                        cp = payload;
                    }
                    continue;

                case _call:
                    data.callee(decodeCallee(payload));
                    data.argc(decodeArgc(payload));
                    state = STATE_CALL;
                    continue;

                case _leave:
                    putNull(arena, sp);
                    sp++;
                    // fallthrough
                case _return:
                    state = STATE_LEAVE;
                    continue;

                default:
                    // Перед выполнением код проходит валидацию.
                    // Поэтому этот сценарий считается невозможным.
                    throw new AssertionError();
            }
        }

        data.state(state);
        data.codePointer(cp);
    }
}
