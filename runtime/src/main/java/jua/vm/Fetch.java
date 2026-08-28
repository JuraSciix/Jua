package jua.vm;

public class Fetch {

    public static byte decodeCallee(int payload) {
        return (byte) payload;
    }

    public static int decodeArgc(int payload) {
        return payload >>> 8;
    }
}
