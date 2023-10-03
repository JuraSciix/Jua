package jua;

import jua.compiler.ModulePrinter;
import jua.compiler.JuaCompiler;
import jua.compiler.Module;
import jua.compiler.ModuleScope;
import jua.compiler.ModuleScope.FunctionSymbol;
import jua.interpreter.InterpreterThread;
import jua.interpreter.memory.Address;
import jua.runtime.ConstantMemory;
import jua.runtime.Function;
import jua.runtime.JuaEnvironment;
import jua.runtime.NativeStdlib;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Main {

    private static Module module;
    private static final Map<Integer, Function> nativeFunctions = new HashMap<>();


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
        for (Map.Entry<String, Address> cm : NativeStdlib.getNativeConstants().entrySet()) {
            // Все константы будут встроены в код.
            ms.defineNativeConstant(cm.getKey(), cm.getValue().toObject());
        }
        for (Function function : NativeStdlib.getNativeFunctions()) {
            FunctionSymbol sym = ms.defineNativeFunction(function);
            nativeFunctions.put(sym.id, function);
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
        ConstantMemory[] constants = module.constants;
        Function[] functions = Arrays.stream(module.executables)
                .map(Executable2FunctionTranslator::translate)
                .toArray(Function[]::new);

        // Привязываем нативные функции к их дескрипторам.
        for (Map.Entry<Integer, Function> nativeFn : nativeFunctions.entrySet()) {
            assert functions[nativeFn.getKey()] == null;
            functions[nativeFn.getKey()] = nativeFn.getValue();
        }

        Function mainFn = Arrays.stream(functions)
                .filter(f -> f.name.equals("<main>"))
                .findAny().orElseThrow(AssertionError::new);

        JuaEnvironment env = new JuaEnvironment(functions, constants);
        InterpreterThread thread = new InterpreterThread(Thread.currentThread(), env);
        Address response = new Address();
        thread.callAndWait(mainFn, new Address[0], response);
        // Если будет интересно, что вернул код, то можно напечатать response.
    }
}
