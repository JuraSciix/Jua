package jua.util;

public class Pair<A, B> {

    public static <A, B> Pair<A, B> of(A a, B b) {
        return new Pair<>(a, b);
    }

    public A fss;

    public B snd;

    public Pair(A fss, B snd) {
        this.fss = fss;
        this.snd = snd;
    }
}
