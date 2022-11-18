package jua.interpreter;

public interface Instructions {

    long MASK_OPCODE = 0xFFL;

    // todo: Finish functions

    static long storeOpcode(long instruction, int opcode) {
        // Обнуляем старый опкод
        instruction &= ~MASK_OPCODE;
        // Нормализуем новый опкод
        opcode &= MASK_OPCODE;
        // Сохраняем новый опкод
        instruction |= opcode;
        return instruction;
    }

    static long storePA(long instruction, int pa) {

        return 0;
    }

    static long storePB(long instruction, int pb) {
        return 0;
    }

    static long storePC(long instruction, int pc) {
        return 0;
    }

    static int fetchOPCode(long instruction) { return (int) (instruction & MASK_OPCODE); }
    static int fetchPA(long instruction) { return (int) ((instruction >> 8) & 0xffff); }
    static int fetchPB(long instruction) { return (int) ((instruction >> 24) & 0xffff); }
    static int fetchPC(long instruction) { return (int) ((instruction >> 40) & 0xffff); }

    interface OPCodes {

        byte nop = 0;
        byte goto_ = 0;
        byte return_ = 0;
        byte leave = 0;
        byte call_0 = 0;
        byte call_1 = 0;
        byte call_2 = 0;
        byte call_3 = 0;
        byte call = 0;
        byte add = 0;
        byte sub = 0;
        byte mul = 0;
        byte div = 0;
        byte rem = 0;
        byte shl = 0;
        byte shr = 0;
        byte and = 0;
        byte or = 0;
        byte xor = 0;
        byte not = 0;
        byte inc = 0;
        byte dec = 0;
        byte mov = 0;
        byte const_true = 0;
        byte const_false = 0;
        byte const_null = 0;
        byte load_const = 0;
        byte pop = 0;
        byte push = 0;
        byte jmpz = 0;
        byte jmpnz = 0;
        byte jmpeq = 0;
        byte jmpne = 0;
        byte jmpcmpeq = 0;
        byte jmpcmpne = 0;
        byte jmpnull = 0;
        byte jmpnonnull = 0;
        byte aload = 0;
        byte astore = 0;
        byte neg = 0;
        byte pos = 0;
        byte concat = 0;
        byte adrop = 0;
        byte sizeof = 0;
    }
}
