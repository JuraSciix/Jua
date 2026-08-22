package jua.runtime;

import jua.vm.Address;

/**
 * @deprecated Скоро будет заменено на {@link JuaCallable}.
 */
@FunctionalInterface
public interface NativeExecutor {

    boolean execute(Address[] args, int argc, Address returnAddress);
}
