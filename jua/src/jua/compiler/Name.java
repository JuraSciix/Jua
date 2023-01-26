package jua.compiler;

public class Name {

    public final String value;

    public final int pos;

    public int id = -1;

    public Name(String value, int pos) {
        this.value = value;
        this.pos = pos;
    }
}
