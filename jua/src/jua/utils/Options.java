package jua.utils;

import jua.Main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class Options {

    private static Options bound;

    public static void bind(String[] args) {
        if (bound != null) {
            throw new IllegalStateException();
        }
        bound = new Options(args);
    }

    public static String firstFile() { return bound.files.stream().findFirst().orElse(null); }
    public static boolean isShouldPrintCode() { return bound.printCode; }
    public static boolean isLintEnabled() { return bound.enableLint; }
    public static boolean isShouldPrettyTree() { return bound.prettyTree; }
    public static ArrayList<String> argv() { return bound.argv; }

    private final ArrayList<String> files = new ArrayList<>();
    private boolean printCode;
    private boolean enableLint;
    private boolean prettyTree;
    private final ArrayList<String> argv = new ArrayList<>();

    private static class OptionIterator {

        private final String[] args;
        private int pos = 0;

        OptionIterator(String[] args) {
            this.args = Objects.requireNonNull(args);
        }

        boolean hasNext() { return pos < args.length; }
        boolean isNextOption() { return hasNext() && args[pos].startsWith("-"); }
        String next() { return args[pos++]; }
    }

    private Options(String[] args) {
        if (args.length == 0) {
            print_help();
            return;
        }
        OptionIterator itr = new OptionIterator(args);
        while (itr.isNextOption()) {
            String option = itr.next();
            if (option.equals("-h") || option.equals("--help")) {
                print_help();
                // UNREACHABLE
            }
            if (option.equals("-v") || option.equals("--version")) {
                print_version();
                // UNREACHABLE
            }
            if (option.equals("-l") || option.equals("--lint")) {
                enableLint = true;
                continue;
            }
            if (option.equals("-x")) {
                printCode = true;
                continue;
            }
            if (option.equals("-p") || option.equals("--pretty")) {
                prettyTree = true;
                continue;
            }
            if (option.startsWith("-f")) {
                String path;
                if (option.startsWith("-f=")) {
                    path = option.substring("-f=".length());
                } else {
                    if (itr.isNextOption()) {
                        System.err.println("Error: invalid syntax for the '-f' option");
                        System.exit(1);
                    }
                    path = itr.next();
                }
                files.addAll(Arrays.asList(path.split(";")));
                continue;
            }
            if (option.startsWith("--files")) {
                String path;
                if (option.startsWith("--files=")) {
                    path = option.substring("--files=".length());
                } else {
                    if (itr.isNextOption()) {
                        System.err.println("Error: invalid syntax for the '--files' option");
                        System.exit(1);
                    }
                    path = itr.next();
                }
                files.addAll(Arrays.asList(path.split(";")));
                continue;
            }
        }
        if (itr.hasNext() & files.isEmpty()) {
            files.add(itr.next());
        }
        while (itr.hasNext()) {
            argv.add(itr.next());
        }
    }

    private void print_help() {
        System.out.println("Usage: jua [options...] [file]");
        System.out.println("\t-x             Print compiled code");
        System.out.println("\t-l, --lint     Print tokens");
        System.out.println("\t-h, --help     Print this help");
        System.out.println("\t-v, --version  Print installed version");
        System.exit(0);
    }

    private void print_version() {
        System.out.printf("%s, v%s %n", Main.binary(), Main.version());
        System.out.printf("Java version: %s %n", System.getProperty("java.version"));
        System.exit(0);
    }
}