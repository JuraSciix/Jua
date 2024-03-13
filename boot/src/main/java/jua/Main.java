package jua;

import jua.compiler.JuaCompiler;
import jua.compiler.Module;
import jua.compiler.ModulePrinter;
import jua.compiler.ModuleScope;
import jua.runtime.Function;
import jua.runtime.JuaEnvironment;
import jua.runtime.interpreter.AddressSupport;
import jua.runtime.interpreter.InterpreterThread;
import jua.runtime.interpreter.Address;
import jua.stdlib.Lib;

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

        nativeFunctions = Lib.getFunctions().toArray(new Function[0]);
        for (int i = 0; i < nativeFunctions.length; i++) {
            Function f = nativeFunctions[i];
            ms.defineNativeFunction(f.getName(), f.getMinArgc(), f.getMaxArgc(),
                    Arrays.stream(f.getDefaults()).map(AddressSupport::toJavaObject).toArray(), f.getParams(), i);
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
                .filter(f -> f.getName().equals("<main>"))
                .findAny().orElseThrow(AssertionError::new);

        JuaEnvironment env = JuaEnvironment.getEnvironment();
        functions.forEach(env::addFunction);
        InterpreterThread thread = new InterpreterThread(Thread.currentThread(), env);
        Address resultReceiver = new Address();
        thread.callAndWait(mainFn, new Address[0], resultReceiver);
        // Если будет интересно, что вернул код, то можно напечатать resultReceiver.
    }
}
