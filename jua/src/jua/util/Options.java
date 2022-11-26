package jua.util;

import jua.Main;

public final class Options {

    private static final Options OPTIONS = new Options();

    public static void bind(String[] args) {
        int i;

        for (i = 0; i < args.length && args[i].startsWith("-"); i++) {
            switch (args[i]) {
                case "-h":
                    OPTIONS.stop = false;
                    // fallthrough
                case "-H":
                    help();
                    continue;

                case "-V":
                    OPTIONS.stop = false;
                    // fallthrough
                case "-v":
                    version();
                    continue;

                case "-X":
                    OPTIONS.stop = false;
                    // fallthrough
                case "-x":
                    OPTIONS.disassembler = true;
                    continue;

                case "-O": {
                    OPTIONS.optimize = false;
                    continue;
                }

                case "-L": {
                    OPTIONS.stop = false;
                }

                case "-l": {
                    OPTIONS.lint = true;
                    continue;
                }
            }
            if (args[i].matches("-r\\d+\\.?\\d*(?:[Ee][+-]?\\d+)?")) { // wtf?
                OPTIONS.callStackSize = (int) Double.parseDouble(args[i].substring(2));
                continue;
            }
            throw new IllegalArgumentException(args[i]);
        }
        if (i < args.length) OPTIONS.filename = args[i];
        OPTIONS.argv = new String[args.length - i];
        System.arraycopy(args, i, OPTIONS.argv, 0, args.length - i);
    }

    private static void help() {
        System.out.println("Usage: jua [options] <filename> ...");
        System.out.println("\t...        : arguments, that will be available in the script.");
        System.out.println("\t-v         : prints version of " + Main.NAME + " and exit.");
        System.out.println("\t-h         : prints this help and exit.");
        System.out.println("\t-x         : switches the program to disassembler.");
        System.out.println("\t-O         : disables all optimizations. (not recommended)");
        System.out.println("\t-r<number> : sets custom limit to call stack. (min 2^10, max 2^30)");
        System.out.println();
        System.out.println("Disassembler special symbols:");
        System.out.println("\t#<number> - reference to some Index.");
        System.out.println("\t$<number> - identifier of variable.");
    }

    private static void version() {
        System.out.printf("%s, version %s.%n", Main.NAME, Main.VERSION);
        System.out.printf("Running in Java %s.%n", System.getProperty("java.version"));
    }

    public static String filename() {
        return OPTIONS.filename;
    }

    public static boolean disassembler() {
        return OPTIONS.disassembler;
    }

    public static boolean optimize() {
        return OPTIONS.optimize;
    }

    public static int callStackSize() {
        return OPTIONS.callStackSize;
    }

    public static String[] argv() {
        return OPTIONS.argv.clone();
    }

    public static boolean lint() {
        return OPTIONS.lint;
    }

    private String filename;
    private boolean disassembler;
    private boolean optimize = true;
    private int callStackSize = (1 << 14);
    private String[] argv;
    private boolean lint;
    /**
     * Завершение работы
     */
    private boolean stop = true;

    public static boolean stop() {
        return OPTIONS.stop;
    }

    private Options() {
        super();
    }
}