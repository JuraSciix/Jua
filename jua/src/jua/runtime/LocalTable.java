package jua.runtime;

public class LocalTable {

    public static final class Local {

        public final String name;

        /** Pool Constant Index */
        public final int defaultPCI;

        public Local(String name) {
            this(name, -1);
        }

        public Local(String name, int defaultPCI) {
            this.name = name;
            this.defaultPCI = defaultPCI;
        }
    }

    private final Local[] locals;

    public LocalTable(Local[] locals) {
        this.locals = locals;
    }

    public String getLocalName(int index) {
        ensureIndexValidity(index);
        return locals[index].name;
    }

    public int getLocalDefaultPCI(int index) {
        ensureIndexValidity(index);
        return locals[index].defaultPCI;
    }

    private void ensureIndexValidity(int index) {
        if (index < 0 || index >= locals.length) {
            throw new IndexOutOfBoundsException(Integer.toString(index));
        }
    }
}
