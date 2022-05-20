package jua.compiler;

import jua.Options;
import jua.compiler.parser.Parser;
import jua.compiler.parser.Tokenizer;
import jua.compiler.parser.Tokens;
import jua.interpreter.InterpreterThread;
import jua.runtime.RuntimeErrorException;
import jua.util.LineMap;
import jua.util.TokenizeStream;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class JuaCompiler {

    // todo: Полностью переписать

    private static class JuaExceptionHandler implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            if (e instanceof RuntimeErrorException) {
                runtimeError(((RuntimeErrorException) e).runtime,
                             (RuntimeErrorException) e);
            } else {
                fatal("Fatal error occurred at runtime: ", e);
            }
            System.exit(1);
        }
    }

    public static final Thread.UncaughtExceptionHandler RUNTIME_EXCEPTION_HANDLER = new JuaExceptionHandler();

    public static void load(String filename) {
        TokenizeStream s;
        try {
            s = TokenizeStream.fromFile(filename);
        } catch (IOException e) {
            fatal("IO error: ", e);
            return;
        }
        Result result = compile(parse(s), s.filename(), s.getLmt());

        if (Options.disassembler()) {
            result.print();
            if (Options.stop()) return;
        }
        interpret(result.env());
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
                    lineMap.getOffsetNumber(token.pos),
                    token.type.name(),
                    token.type.toString());
        }

        @Override
        public void visitDummy(Tokens.DummyToken token) {
            System.out.printf("[%d:%d] %s%n",
                    lineMap.getLineNumber(token.pos),
                    lineMap.getOffsetNumber(token.pos),
                    token.type.name());
        }

        @Override
        public void visitString(Tokens.StringToken token) {
            System.out.printf("[%d:%d] %s: \"%s\" %n",
                    lineMap.getLineNumber(token.pos),
                    lineMap.getOffsetNumber(token.pos),
                    token.type.name(),
                    token.value);
        }

        @Override
        public void visitNumeric(Tokens.NumberToken token) {
            System.out.printf("[%d:%d] %s: %s * %s %n",
                    lineMap.getLineNumber(token.pos),
                    lineMap.getOffsetNumber(token.pos),
                    token.type.name(),
                    token.value,
                    token.radix);
        }
    }

    private static Tree.Statement parse(TokenizeStream s) {
        if (Options.lint()) {
            Tokenizer tokenizer = new Tokenizer(s);
            TokenPrinter printer = new TokenPrinter(s.getLmt());
            while (tokenizer.hasMoreTokens()) {
                try {
                    tokenizer.nextToken().accept(printer);
                } catch (ParseException e) {
                    if (Options.stop()) {
                        parseError(e, s.filename(), s.getLmt());
                    }
                    break;
                }
            }
            if (Options.stop()) throw new ThreadDeath();
        }
        try (TokenizeStream stream = s) {
            return new Parser(new Tokenizer(stream)).parse();
        } catch (ParseException e) {
            parseError(e, s.filename(), s.getLmt());
        } catch (Throwable t) {
            fatal("Fatal error occurred at parser: ", t);
        }
        return null;
    }

    private static void parseError(ParseException e, String filename, LineMap lnt) {
        System.err.println("Parse error: " + e.getMessage());
        printPosition(filename, lnt.getLineNumber(e.position), lnt.getOffsetNumber(e.position));
        System.exit(1);
    }

    private static Result compile(Tree.Statement root, String filename, LineMap lineMap) {
        CodeData codeData = new CodeData(filename);
        Gen gen = new Gen(codeData, lineMap);

        try {
            if (Options.optimize()) {
                root.accept(new Fold(codeData));
            } else {
                System.err.println("Warning: disabling optimization is strongly discouraged. " +
                        "This feature may be removed in a future version");
            }
            root.accept(gen);
        } catch (CompileError e) {
            compileError(e, lineMap, filename);
        } catch (Throwable t) {
            fatal("Fatal error occurred at compiler: ", t);
        }
        return gen.getResult();
    }

    private static void compileError(CompileError e, LineMap lineMap, String filename) {
        System.err.println("Compile error: " + e.getMessage());
        printPosition(filename, lineMap.getLineNumber(e.position),
                lineMap.getOffsetNumber(e.position));
        System.exit(1);
    }

    private static void interpret(InterpreterThread env) {
        Thread.setDefaultUncaughtExceptionHandler(RUNTIME_EXCEPTION_HANDLER);
        env.run();
    }

    private static void runtimeError(InterpreterThread runtime, RuntimeErrorException e) {
        System.err.println("Runtime error: " + e.getMessage());
        // todo: Че там с многопотоком)
        printPosition(runtime.currentFile(), runtime.currentLine(), -1);
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
