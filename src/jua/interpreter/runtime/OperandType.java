package jua.interpreter.runtime;

public enum OperandType {

    ARRAY("array"),
    BOOLEAN("boolean"),
    FLOAT("float"),
    INT("int"),
    NULL("null"),
    STRING("string");

    private final String name;

    OperandType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
