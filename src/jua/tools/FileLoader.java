package jua.tools;

import jua.Options;
import jua.compiler.Gen;
import jua.compiler.*;
import jua.interpreter.Environment;
import jua.interpreter.RuntimeError;
import jua.parser.ParseException;
import jua.parser.Parser;
import jua.parser.TokenizeStream;
import jua.parser.Tokenizer;
import jua.parser.ast.Statement;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FileLoader {

    private static class JuaExceptionHandler implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            if (e instanceof RuntimeError) {
                runtimeError((RuntimeError) e);
            } else {
                fatal("Fatal error occurred at runtime: ", e);
            }
            System.exit(1);
        }
    }

    public static final Thread.UncaughtExceptionHandler RUNTIME_EXCEPTION_HANDLER = new JuaExceptionHandler();

    public static void load(String filename) {
        Result result = compile(parse(filename));

        if (Options.disassembler()) {
            result.print();
        } else {
            //for (int i = 0; i < 20_000; i++)
                interpret(result.env());
        }
    }

    private static Statement parse(String filename) {
        try (TokenizeStream stream = TokenizeStream.fromFile(filename)) {
            return new Parser(new Tokenizer(stream)).parse();
        } catch (IOException e) {
            fatal("I/O error occurred at tokenizer: ", e);
        } catch (ParseException e) {
            parseError(e);
        } catch (Throwable t) {
            fatal("Fatal error occurred at parser: ", t);
        }
        return null;
    }

    private static void parseError(ParseException e) {
        System.err.println("Parse error: " + e.getMessage());
        printPosition(e.position.filename, e.position.line, e.position.offset);
        System.exit(1);
    }

    private static Result compile(Statement root) {
        BuiltIn builtIn = new BuiltIn();
        Gen gen = new Gen(builtIn);

        try {
            if (Options.optimize()) {
                root.accept(new ConstantFolder(builtIn));
            } else {
                System.err.println("Warning: disabling optimization is strongly discouraged. " +
                        "This feature may be removed in a future version");
            }
            root.accept(gen);
        } catch (CompileError e) {
            compileError(e);
        } catch (Throwable t) {
            fatal("Fatal error occurred at compiler: ", t);
        }
        return gen.getResult();
    }

    private static void compileError(CompileError e) {
        System.err.println("Compile error: " + e.getMessage());
        printPosition(e.position.filename, e.position.line, e.position.offset);
        System.exit(1);
    }

    private static void interpret(Environment env) {
        Thread.setDefaultUncaughtExceptionHandler(RUNTIME_EXCEPTION_HANDLER);
        env.run();
    }

    private static void runtimeError(RuntimeError e) {
        System.err.println("Runtime error: " + e.getMessage());
        printPosition(e.filename, e.line, -1);
        System.exit(1);
    }

    private static void printPosition(String filename, int line, int offset) {
        System.err.printf("File: %s, line: %d.%n", filename, line);

        if (offset >= 0) {
            printLine(filename, line, offset);
        }
    }

    private static void printLine(String filename, int line, int offset) {
        String s;

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            while (--line > 0) {
                br.readLine();
            }
            s = br.readLine();
        } catch (IOException e) {
            return;
        }
        printOffset((s == null) ? "" : s, offset);
    }

    private static void printOffset(String s, int offset) {
        StringBuilder sb = new StringBuilder(offset);

        for (int i = 0; i < (offset - 1); i++) {
            sb.append(i >= s.length() || s.charAt(i) != '\t' ? ' ' : '\t');
        }
        System.err.println(s);
        System.err.println(sb.append('^'));
    }

    private static void fatal(String message, Throwable fatal) {
        System.err.print(message);
        fatal.printStackTrace();
        System.exit(1);
    }
}
