package jua.compiler;

@Deprecated
public final class Target {

    private final Version version;

    public Target(Version version) {
        this.version = version;
    }

    public boolean isFunctionSyntacticSugarAllowed() {
        return version.compareTo(Version.JUA_1_2) >= 0;
    }

    public boolean isNamedArgumentInvocationAllowed() {
        return version.compareTo(Version.JUA_1_4) >= 0;
    }
}
