package jua;

import jua.tools.FileLoader;

import java.io.File;

public class Main {

    public static final String NAME = "Jua";
    public static final String VERSION = "1.0_94";

    public static void main(String[] args) {
        try {
            Options.bind(args);
        } catch (IllegalArgumentException e) {
            error("unrecognized option: " + e.getMessage());
        } catch (Throwable t) {
            error("can't parse console arguments: " + t);
        }
        FileLoader.load(testFilename());
    }

    private static String testFilename() {
        String filename = Options.filename();

        if (filename == null) {
            error("main file not specified.");
        } else if (!new File(filename).isFile()) {
            error("main file not found.");
        }
        return filename;
    }

    private static void error(String message) {
        System.err.printf("Error: %s%n", message);
        System.exit(1);
    }
}
