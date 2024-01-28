package jua;

import jua.compiler.JuaCompiler;
import jua.compiler.Module;
import jua.compiler.ModulePrinter;
import jua.compiler.ModuleScope;
import jua.compiler.ModuleScope.FunctionSymbol;
import jua.runtime.Function;
import jua.runtime.JuaEnvironment;
import jua.runtime.interpreter.InterpreterThread;
import jua.runtime.interpreter.memory.Address;
import jua.stdlib.NativeStdlib;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static Module module;
    private static Function[] nativeFunctions;


    public static void main(String[] args) {
        parseOptions(args);
        targetFile();
        compile();
        interpret();
    }

    private static void parseOptions(String[] args) {
        try {
            Options.bind(args);
        } catch (RuntimeException e) {
            System.err.println("Unable to parse options: " + e);
            System.exit(1);
        }
    }

    private static void targetFile() {
        File file = new File(Options.firstFile());
        if (!file.isFile()) {
            System.err.println("Unable to find file " + Options.firstFile());
            System.exit(1);
        }
    }

    private static void compile() {
        JuaCompiler c = new JuaCompiler();
        c.setCharset(Options.charset());
        c.setFile(Options.firstFile());
        c.setGenJvmLoops(Options.genJvmLoops());
        c.setStderr(System.err);
        c.setStdout(System.out);
        c.setLintMode(Options.isLintEnabled());
//        c.setPrettyTreeMode(Options.isShouldPrettyTree());
        c.setLogLimit(Options.logMaxErrors());

        // Регистрируем нативные члены.
        ModuleScope ms = c.getModuleScope();

        nativeFunctions = NativeStdlib.getNativeFunctions().toArray(new Function[0]);
        for (int i = 0; i < nativeFunctions.length; i++) {
            Function f = nativeFunctions[i];
            ms.defineNativeFunction(f.name, f.minArgc, f.maxArgc,
                    Arrays.stream(f.defaults).map(Address::toObject).toArray(), f.params, i);
        }

        module = c.compile();
        if (module == null) {
            // todo: Сделать нормальную проверку на ошибку компиляции.
            System.exit(1);
        }

        if (Options.isShouldPrintCode()) {
            ModulePrinter.printModule(module);
            System.exit(1);
        }
    }

    private static void interpret() {
        List<Function> functions = Arrays.stream(module.executables)
                .map(Executable2FunctionTranslator::translate)
                .collect(Collectors.toList());

        Collections.addAll(functions, nativeFunctions);

        Function mainFn = functions.stream()
                .filter(f -> f.name.equals("<main>"))
                .findAny().orElseThrow(AssertionError::new);

        InterpreterThread thread = new InterpreterThread(Thread.currentThread(),
                new JuaEnvironment(functions.toArray(new Function[0])));
        Address resultReceiver = new Address();
        thread.callAndWait(mainFn, new Address[0], resultReceiver);
        // Если будет интересно, что вернул код, то можно напечатать resultReceiver.
    }
}
