package jua;

import jua.compiler.Program;
import jua.compiler.JuaCompiler;
import jua.runtime.RuntimeErrorException;
import jua.util.Options;

import java.io.File;
import java.io.IOException;

public class Main {

    // todo: jua test.jua -p=examples

    public static final String NAME = "Jua";
    // todo: Разделить версию на мажорную и минорную
    public static final String VERSION = "1.95.209";

    public static String binary() {
        return NAME;
    }

    public static String version() {
        return VERSION;
    }

    // todo: Мне лень сейчас обработкой исключений заниматься..
    public static void main(String[] args) throws IOException {
        try {
            Options.bind(args);
        } catch (IllegalArgumentException e) {
            error("unrecognized option: " + e.getMessage());
        } catch (Throwable t) {
            error("can't parse console arguments: " + t);
        }

        // todo: Работа с несколькими файлами одновременно

        File file = testTargetFile();
        Program result = JuaCompiler.compileFile(file);

        if (result == null) return;
        if (Options.isShouldPrintCode()) {
            result.print();
            return;
        }
        try {
            result.run();
        } catch (RuntimeErrorException e) {
            // todo: Починить вывод который влад сломал
            e.printStackTrace();
        }
    }

    private static File testTargetFile() {
        String filename = Options.firstFile();

        if (filename == null) {
            error("main file not specified.");
            throw new ThreadDeath(); // avoiding warnings
        }
        File file = new File(filename);

        if (!file.isFile()) {
            error("main file not found.");
        }
        return file;
    }

    private static void error(String message) {
        System.err.printf("Error: %s%n", message);
        System.exit(1);
    }
}
