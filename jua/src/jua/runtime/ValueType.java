package jua.runtime;

import jua.interpreter.Interpreter;

public interface ValueType {

    byte UNDEFINED = 0;
    byte LONG = 1;
    byte DOUBLE = 2;
    byte BOOLEAN = 3;
    byte STRING = 4;
    byte ARRAY = 5; // todo: Реализовать этот тип.
    byte MAP = 6;
    byte ITERATOR = 7;
    byte NULL = 8;

    int P_LL = pairOf(LONG, LONG);
    int P_LD = pairOf(LONG, DOUBLE);
    int P_DL = pairOf(DOUBLE, LONG);
    int P_DD = pairOf(DOUBLE, DOUBLE);
    int P_BB = pairOf(BOOLEAN, BOOLEAN);
    int P_LS = pairOf(LONG, STRING);
    int P_DS = pairOf(DOUBLE, STRING);
    int P_BS = pairOf(BOOLEAN, STRING);
    int P_SS = pairOf(STRING, STRING);
    int P_AS = pairOf(ARRAY, STRING);
    int P_MS = pairOf(MAP, STRING);
    int P_IS = pairOf(ITERATOR, STRING);
    int P_NS = pairOf(NULL, STRING);
    int P_SL = pairOf(STRING, LONG);
    int P_SD = pairOf(STRING, DOUBLE);
    int P_SB = pairOf(STRING, BOOLEAN);
    int P_SA = pairOf(STRING, ARRAY);
    int P_SM = pairOf(STRING, MAP);
    int P_SI = pairOf(STRING, ITERATOR);
    int P_SN = pairOf(STRING, NULL);
    int P_AA = pairOf(ARRAY, ARRAY);
    int P_MM = pairOf(MAP, MAP);
    int P_II = pairOf(ITERATOR, ITERATOR);
    int P_NN = pairOf(NULL, NULL);

    static String nameOf(byte typeCode) {
        switch (typeCode) {
            case UNDEFINED: return "<undefined>";
            case LONG: return "int";
            case BOOLEAN: return "boolean";
            case DOUBLE: return "float";
            case STRING: return "string";
             case ARRAY: return "array";
             case MAP: return "map";
            case ITERATOR: return "iterator";
            case NULL: return "<null>";
        }
        Interpreter.fallWithFatalError("Unknown type code passed: " + typeCode);
        return null;
    }

    static int pairOf(byte leftTypeCode, byte rightTypeCode) {
        return leftTypeCode | (rightTypeCode << 4);
    }

    static boolean isTypeScalar(byte typeCode) {
        return typeCode > UNDEFINED && typeCode < ARRAY;
    }
}
