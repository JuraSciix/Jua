package jua.interpreter.runtime;

public class Constant {

    public final Operand value;

    public final boolean isExtern;

    public Constant(Operand value, boolean isExtern) {
        this.value = value;
        this.isExtern = isExtern;
    }
}
