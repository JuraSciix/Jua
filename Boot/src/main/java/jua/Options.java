package jua;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
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
    public static int logMaxErrors() { return bound.logMaxErrors; }
    public static Charset charset() { return bound.charset; }
    public static boolean genJvmLoops() { return bound.genJvmLoops; }

    private final ArrayList<String> files = new ArrayList<>();
    private boolean printCode;
    private boolean enableLint;
    private boolean prettyTree;
    private final ArrayList<String> argv = new ArrayList<>();
    private int logMaxErrors = 1;
    private Charset charset = Charset.defaultCharset();
    private boolean genJvmLoops;

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
            if (option.startsWith("-m")) {
                String value = option.substring("-m".length());
                if (!value.startsWith("=") || value.length() == 1) {
                    System.err.println("Error: option '-m' must have a value specified after '='");
                    System.exit(1);
                }
                try {
                    logMaxErrors = Integer.parseUnsignedInt(value.substring(1));
                } catch (NumberFormatException e) {
                    System.err.println("Error: option '-m' have an invalid value, expected positive integer.");
                    System.exit(1);
                }
                continue;
            }
            if (option.startsWith("-c")) {
                String value = option.substring("-c".length());
                if (!value.startsWith("=") || value.length() == 1) {
                    System.err.println("Error: option '-c' must have a value specified after '='");
                    System.exit(1);
                }
                try {
                    charset = Charset.forName(value.substring(1));
                } catch (UnsupportedCharsetException e) {
                    System.err.println("Error: option '-c' have an invalid value.");
                    System.exit(1);
                }
                continue;
            }
            if (option.equals("-gj")) {
                genJvmLoops = true;
                continue;
            }

            System.err.println("Unrecognized option: " + option);
            System.exit(1);
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
        System.out.println("\t-x                              Print compiled code");
        System.out.println("\t-l, --lint                      Print tokens");
        System.out.println("\t-h, --help                      Print this help");
        System.out.println("\t-v, --version                   Print installed version");
        System.out.println("\t-v, --c=<value>                 Specify charset");
        System.out.println("\t-m=<value>, --m<value>          Specify max printable compiler errors");
        System.out.println("\t--gj                            Enable JVM loops model generation");
        System.out.println("\t-f=<values;>, --files=<values;> Specify files to be executed (didn't work)");
        System.exit(0);
    }

    private void print_version() {
        // todo
        System.out.printf("%s, v%s %n", "Jua", "1.96");
        System.out.printf("Java version: %s %n", System.getProperty("java.version"));
        System.exit(0);
    }
}