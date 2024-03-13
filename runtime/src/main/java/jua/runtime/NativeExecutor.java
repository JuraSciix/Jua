package jua.runtime;

import jua.runtime.interpreter.Address;

/**
 * @deprecated Скоро будет заменено на {@link JuaCallable}.
 */
@FunctionalInterface
public interface NativeExecutor {

    boolean execute(Address[] args, int argc, Address returnAddress);
}
