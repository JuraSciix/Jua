package jua;

import jua.compiler.JuaCompiler;

import java.io.File;
import java.net.MalformedURLException;

public class Main {

    public static final String NAME = "Jua";
    // todo: Разделить версию на мажорную и минорную
    public static final String VERSION = "1.95";

    // todo: Мне лень сейчас обработкой исключений заниматься..
    public static void main(String[] args) throws MalformedURLException {
        try {
            Options.bind(args);
        } catch (IllegalArgumentException e) {
            error("unrecognized option: " + e.getMessage());
        } catch (Throwable t) {
            error("can't parse console arguments: " + t);
        }
        JuaCompiler.load(testFilename().toURI().toURL());
    }

    private static File testFilename() {
        String filename = Options.filename();

        if (filename == null) {
            error("main file not specified.");
            return null;
        }
        File file = new File(filename);

        if (!file.isFile()) {
            error("main file not found.");
            return null;
        }
        return file;
    }

    private static void error(String message) {
        System.err.printf("Error: %s%n", message);
        System.exit(1);
    }
}
