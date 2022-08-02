package jua.interpreter;

public interface Instructions {

    // todo: Finish functions

    static long storeOpcode(long instruction, int opcode) {
        return 0;
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

    static int fetchOPCode(long instruction) { return (int) (instruction & 0xff); }
    static int fetchPA(long instruction) { return (int) ((instruction >> 8) & 0xffff); }
    static int fetchPB(long instruction) { return (int) ((instruction >> 24) & 0xffff); }
    static int fetchPC(long instruction) { return (int) ((instruction >> 40) & 0xffff); }

    interface OPCodes {

        byte NOP         = 0;
        byte CONST_NULL  = 1;
        byte CONST_TRUE  = 2;
        byte CONST_FALSE = 3;
        byte PUSH        = 4;
        byte POP         = 5;
        byte POP2        = 6;
        byte DUP         = 7;
        byte DUP_X1      = 8;
        byte DUP_X2      = 9;
        byte DUP2        = 10;
        byte DUP2_X1     = 11;
        byte DUP2_X2     = 12;
        byte ADD         = 13;
        byte SUB         = 14;
        byte MUL         = 15;
        byte DIV         = 16;
        byte REM         = 17;
        byte SHL         = 18;
        byte SHR         = 19;
        byte AND         = 20;
        byte OR          = 21;
        byte XOR         = 22;
        byte NEG         = 23;
        byte POS         = 24;
        byte NOT         = 25;
        byte LOAD_0      = 26;
        byte LOAD_1      = 27;
        byte LOAD_2      = 28;
        byte LOAD_3      = 29;
        byte LOAD        = 30;
        byte STORE_0     = 31;
        byte STORE_1     = 32;
        byte STORE_2     = 33;
        byte STORE_3     = 34;
        byte STORE       = 35;
        byte INC         = 36;
        byte DEC         = 37;
        byte ALOAD       = 38;
        byte ASTORE      = 39;
        byte AINC        = 40;
        byte ADEC        = 41;
        byte LENGTH      = 42;
        byte CLONE       = 43;
        byte NEWARRAY    = 44;
        byte GETCONST    = 45;
        byte GOTO        = 46;
        byte IFEQ        = 47;
        byte IFNE        = 48;
        byte IFGT        = 49;
        byte IFLE        = 50;
        byte IFLT        = 51;
        byte IFGE        = 52;
        byte IFCMPEQ     = 53;
        byte IFCMPNE     = 54;
        byte IFCMPGT     = 55;
        byte IFCMPLE     = 56;
        byte IFCMPLT     = 57;
        byte IFCMPGE     = 58;
        byte IFZ         = 59;
        byte IFNZ        = 60;
        byte IFNULL      = 61;
        byte IFNONNULL   = 62;
        byte SWITCH      = 63;
        byte CALL_0      = 64;
        byte CALL_1      = 65;
        byte CALL_2      = 66;
        byte CALL_3      = 67;
        byte CALL        = 68;
        byte RETURN      = 69;
        byte HALT        = 70;
    }
}
