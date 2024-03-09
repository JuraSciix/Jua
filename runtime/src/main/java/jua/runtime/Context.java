package jua.runtime;

import jua.runtime.interpreter.Address;
import jua.runtime.interpreter.AddressSupport;
import jua.runtime.interpreter.AddressUtils;
import jua.runtime.interpreter.InterpreterThread;

public final class Context {

    public void error(String msg) {
        throw new RuntimeErrorException(msg);
    }

    public void error(String fmt, Object... args) {
        throw new RuntimeErrorException(String.format(fmt, args));
    }

    public Object call(String name, Object... args) {
        Address returnAddress = new Address();
        Address[] addrArgs = AddressUtils.allocateMemory(args.length, 0);
        for (int i = 0; i < args.length; i++) {
            AddressSupport.assignObject(addrArgs[i], args[i]);
        }
        directCall(name, addrArgs, returnAddress);
        return AddressSupport.toJavaObject(returnAddress);
    }

    public void directCall(String name, Address[] args, Address returnAddress) {
        InterpreterThread thread = InterpreterThread.currentThread();
        Function function = thread.getEnvironment().lookupFunction(name);
        // todo: Выбрасывать исключение, если произошла ошибка выполнения
        thread.callAndWait(function, args, returnAddress);
    }
}
