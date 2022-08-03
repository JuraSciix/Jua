package jua.compiler;

import jua.Options;
import jua.compiler.parser.JuaParser;
import jua.compiler.parser.Tokenizer;
import jua.compiler.parser.Tokens;
import jua.interpreter.InterpreterThread;
import jua.runtime.RuntimeErrorException;
import jua.util.IOUtils;
import jua.util.LineMap;
import jua.util.Source;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JuaCompiler {

    // todo: Полностью переписать

    private static class JuaExceptionHandler implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            if (e instanceof RuntimeErrorException) {
                runtimeError(((RuntimeErrorException) e).thread,
                        (RuntimeErrorException) e);
            } else {
                fatal("Fatal error occurred at runtime: ", e);
            }
            System.exit(1);
        }
    }

    public static final Thread.UncaughtExceptionHandler RUNTIME_EXCEPTION_HANDLER = new JuaExceptionHandler();

    public static void load(File file) {
        Source source;
        try {
            source = new Source(file.getName(), IOUtils.readCharsFromFile(file));
        } catch (IOException e) {
            fatal("IO error: ", e);
            return;
        }
        Result result = compile(parse(source), source);

        if (Options.disassembler()) {
            result.print();
            if (Options.stop()) return;
        }
        interpret(result.toThread());
    }

    private static class TokenPrinter implements Tokens.TokenVisitor {

        private final LineMap lineMap;

        TokenPrinter(LineMap lineMap) {
            this.lineMap = lineMap;
        }

        @Override
        public void visitOperator(Tokens.OperatorToken token) {
            System.out.printf("[%d:%d] %s (%s)%n",
                    lineMap.getLineNumber(token.pos),
                    lineMap.getColumnNumber(token.pos),
                    token.type.name(),
                    token.type.toString());
        }

        @Override
        public void visitDummy(Tokens.DummyToken token) {
            System.out.printf("[%d:%d] %s%n",
                    lineMap.getLineNumber(token.pos),
                    lineMap.getColumnNumber(token.pos),
                    token.type.name());
        }

        @Override
        public void visitString(Tokens.StringToken token) {
            System.out.printf("[%d:%d] %s: \"%s\" %n",
                    lineMap.getLineNumber(token.pos),
                    lineMap.getColumnNumber(token.pos),
                    token.type.name(),
                    token.value);
        }

        @Override
        public void visitNumeric(Tokens.NumberToken token) {
            System.out.printf("[%d:%d] %s: %s * %s %n",
                    lineMap.getLineNumber(token.pos),
                    lineMap.getColumnNumber(token.pos),
                    token.type.name(),
                    token.value,
                    token.radix);
        }
    }

    private static Tree parse(Source source) {
        if (Options.lint()) {
            try (Tokenizer tokenizer = new Tokenizer(source)) {
                TokenPrinter printer = new TokenPrinter(source.getLineMap());
                while (tokenizer.hasMoreTokens()) {
                    try {
                        tokenizer.nextToken().accept(printer);
                    } catch (ParseException e) {
                        if (Options.stop()) {
                            parseError(e, source);
                        }
                        break;
                    }
                }
                if (Options.stop()) throw new ThreadDeath();
            } catch (IOException e) {
                fatal("IO error: ", e);
            }
        }
        try (Tokenizer tokenizer = new Tokenizer(source)) {
            return new JuaParser(tokenizer).parse();
        } catch (ParseException e) {
            parseError(e, source);
        } catch (Throwable t) {
            fatal("Fatal error occurred at parser: ", t);
        }
        return null;
    }

    private static void parseError(ParseException e, Source source) {
        System.err.println("Parse error: " + e.getMessage());
        try {
            printPosition(source.filename(),
                    source.getLineMap().getLineNumber(e.position),
                    source.getLineMap().getColumnNumber(e.position));
        } catch (IOException ex) {
            //nope
        }
        System.exit(1);
    }

    private static Result compile(Tree root, Source source) {
        CodeData codeData = new CodeData(source.filename());
        Gen gen = new Gen(codeData);

        try {
            // Свёртка констант - это обязательный этап.
            if (true || Options.optimize()) {
                root.accept(new Lower());
            } else {
                System.err.println("Warning: disabling optimization is strongly discouraged. " +
                        "This feature may be removed in a future version");
            }
            root.accept(new Enter(codeData));
            root.accept(gen);
        } catch (CompileError e) {
            compileError(e, source);
        } catch (Throwable t) {
            fatal("Fatal error occurred at compiler: ", t);
        }
        return gen.getResult();
    }

    private static void compileError(CompileError e, Source source) {
        System.err.println("Compile error: " + e.getMessage());
        try {
            printPosition(source.filename(),
                    source.getLineMap().getLineNumber(e.position),
                    source.getLineMap().getColumnNumber(e.position));
        } catch (IOException ex) {
            //nope
        }
        System.exit(1);
    }

    private static void interpret(InterpreterThread thread) {
        Thread.setDefaultUncaughtExceptionHandler(RUNTIME_EXCEPTION_HANDLER);
        thread.run();
    }

    private static void runtimeError(InterpreterThread runtime, RuntimeErrorException e) {
        System.err.println("Runtime error: " + e.getMessage());
        // todo: Че там с многопотоком)
        printPosition(runtime.current_location(), runtime.current_line_number(), -1);
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

        try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(filename))))) {
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
