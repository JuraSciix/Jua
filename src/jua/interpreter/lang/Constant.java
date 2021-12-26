package jua.interpreter.lang;

public class Constant {

    public final Operand value;

    public final boolean isExtern;

    public Constant(Operand value, boolean isExtern) {
        this.value = value;
        this.isExtern = isExtern;
    }
}
