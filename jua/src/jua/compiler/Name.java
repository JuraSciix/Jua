package jua.compiler;

public class Name {

    private final String value;

    public final int pos;

    public int id = -1;

    public Name(String value, int pos) {
        this.value = value;
        this.pos = pos;
    }

    @Override
    public int hashCode() { return value.hashCode(); }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Name a = (Name) obj;
        return value.equals(a.value);
    }

    @Override
    public String toString() { return value; }
}
