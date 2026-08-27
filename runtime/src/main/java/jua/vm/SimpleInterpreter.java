package jua.vm;

import jua.vm.arena.DataArena;

import static jua.vm.OPCodes.*;
import static jua.vm.SimpleArena.*;
import static jua.vm.SimpleArithm.*;

public class SimpleInterpreter {
    public static final int MASK_CP = 0xffff;
    public static final int MASK_STACK = 0xffff;
    public static final int MASK_REG = 0xffff;

    // cs - место, откуда начать выполнение
    // sb - базовый указатель на вершину стека
    // rb - указатель на начало регистровой области
    public static void run(int cs, int sb, int rb, int[] code, DataArena arena, FrameData data) {
        int cp = cs & MASK_CP; // Code Pointer = Code Start
        int sp = sb & MASK_STACK; // Stack Pointer = Stack Base

        int state = STATE_DONE;
        while (state == STATE_DONE) {
            int inst = code[cp * 2];
            int payload = code[cp * 2 + 1];
            cp++;

            switch (inst) {
                case Nop:
                    break;
                case ConstNull:
                    putNull(arena, sp);
                    sp++;
                    break;
                case ConstTrue:
                case ConstFalse:
                    putBool(arena, sp, inst == ConstTrue);
                    sp++;
                    break;
                case ConstIntM1:
                case ConstInt0:
                case ConstInt1:
                case ConstInt2:
                    putInt64(arena, sp, inst - ConstInt0);
                    sp++;
                    break;

                case Dup:
                case DupX1:
                case DupX2:
                case Swap:
                case Dup2:
                case Dup2X1:
                case Dup2X2:
                    // ...
                    break;

                case Push:
                    putInt64(arena, sp, payload);
                    sp++;
                    break;

                case NewList:
                    // ...
                    break;

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
                    state = add(arena, sp);
                    sp--;
                    break;
                case Sub:
                    state = sub(arena, sp);
                    sp--;
                    break;
                case Mul:
                    state = mul(arena, sp);
                    sp--;
                    break;
                case Div:
                    state = div(arena, sp);
                    sp--;
                    break;
                case Rem:
                case And:
                case Or:
                case Xor:
                case Shl:
                case Shr:
                case Length:
                case Pos:
                case Neg:
                case Not:
                    // ...
                    break;

                case Load:
                    clearAndMove(arena, rb + (payload & MASK_REG), sp);
                    sp++;
                    break;
                case Load0:
                case Load1:
                case Load2:
                    clearAndMove(arena, rb + inst - Load0, sp);
                    sp++;
                    break;
                case Store:
                    clearAndMove(arena, sp - 1, rb + (payload & MASK_REG));
                    sp--;
                    break;
                case Store0:
                case Store1:
                case Store2:
                    clearAndMove(arena, sp - 1, rb + inst - Store0);
                    sp--;
                    break;
                case Inc:
                case Dec:
                    state = inc(arena, rb + (payload & MASK_REG), inst == Inc ? 1L : -1L);
                    break;

                case ArrayLoad:
                case ArrayStore:
                case ArrayInc:
                case ArrayDec:
                    // ...
                    break;

                case Goto:
                    cp = payload & MASK_CP;
                    break;

                case IfEq: case IfNe:
                case IfGe: case IfLt:
                case IfLe: case IfGt:
                    if ((inst == IfNe) ^ compare(arena, sp,
                            inst == IfNe || inst == IfEq || inst == IfGe || inst == IfLe,
                            inst == IfGe || inst == IfGt,
                            inst == IfLe || inst == IfLt)) {
                        cp = payload & MASK_CP;
                    }
                    sp -= 2;
                    break;

                case IfZ:
                case IfNz:
                    if (compareInt64Zero(arena, sp) == (inst == IfZ)) {
                        cp = payload & MASK_CP;
                    }
                    sp -= 1;
                    break;

                case IfNull:
                case IfNonNull:
                    if (compareRefNull(arena, sp) == (inst == IfNull)) {
                        cp = payload & MASK_CP;
                    }
                    sp -= 1;
                    break;

                case Call:
                    // ...
                    break;

                case Leave:
                    putNull(arena, sp);
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
