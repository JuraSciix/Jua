package jua.compiler;

import jua.compiler.Tokens.Token;
import jua.compiler.Tokens.TokenType;
import jua.compiler.utils.Flow;
import jua.compiler.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;

public final class JuaCompiler {

    private PrintStream stdout = System.out;

    private PrintStream stderr = System.err;

    private int logLimit;

    private String file;

    private boolean lintMode;

    private boolean prettyTreeMode;

    private Charset charset = Charset.defaultCharset();

    private final ModuleScope moduleScope = new ModuleScope();

    public ModuleScope getModuleScope() {
        return moduleScope;
    }

    public boolean isGenJvmLoops() {
        return genJvmLoops;
    }

    public void setGenJvmLoops(boolean genJvmLoops) {
        this.genJvmLoops = genJvmLoops;
    }

    private boolean genJvmLoops = false;

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public void setPrettyTreeMode(boolean prettyTreeMode) {
        this.prettyTreeMode = prettyTreeMode;
    }

    public void setLintMode(boolean lintMode) {
        this.lintMode = lintMode;
    }

    public void setStdout(PrintStream stdout) {
        this.stdout = stdout;
    }

    public void setStderr(PrintStream stderr) {
        this.stderr = stderr;
    }

    public void setLogLimit(int logLimit) {
        this.logLimit = logLimit;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public Module compile() {
        char[] filecontents;
        try {
            filecontents = IOUtils.readFileCharBuffer(new File(file), charset);
        } catch (IOException e) {
            stderr.println("Unable access to file.");
            System.exit(1);
            return null;
        }
        Log log = new SimpleLog(stderr, logLimit);
        Source source = new Source(file, filecontents);

        if (lintMode) {
            lint(log, source);
            return null;
        }
        try {
            JuaParser parser = new JuaParser(source, log);
            Tree.Document compilationUnit = parser.parseDocument();
            if (prettyTreeMode) {
                System.err.println("Prettier isn't available.");
//                compilationUnit.accept(new Pretty(System.err));
                return null;
            }
            ModuleScope programScope = getModuleScope();
            // todo: Проверить, что тут надо было пофиксить.
            compilationUnit.accept(new Lower(programScope));
            compilationUnit.accept(new Enter(programScope, log));
            compilationUnit.accept(new Check(programScope, log));

            if (log.hasErrors()) {
                return null;
            }

            Flow.forEach(compilationUnit.functions, funcDef -> funcDef.sym.code = new Code(programScope, source));

            Flow.forEach(compilationUnit.functions, funcDef -> {
                funcDef.accept(funcDef.sym.code.gen);
                programScope.lookupFunction(funcDef.name).executable = funcDef.sym.executable;
            });

            Module.Executable[] functions = programScope.getUserFunctions().stream()
                    .map(s -> s.code.toExecutable())
                    .toArray(Module.Executable[]::new);

            return new Module(source, functions);
        } catch (RuntimeException e) {
            System.err.println("Compiler error occurred");
            e.printStackTrace();
            return null;
        }
    }

    private void lint(Log log, Source source) {
        Lexer tokenizer = new Lexer(source, log);
        for (Token token = tokenizer.nextToken();
             token.type != TokenType.EOF;
             token = tokenizer.nextToken()) {
            stdout.printf("[%d:%d] ",
                    source.getLineMap().getLineNumber(token.pos),
                    source.getLineMap().getColumnNumber(token.pos));
            switch (token.type.kind()) {
                case DEFAULT:
                    stdout.println(token.type.name());
                    break;
                case NAMED:
                    stdout.printf("%s: %s%n", token.type.name(), token.name());
                    break;
                case STRING:
                    stdout.printf("%s: \"%s\"%n", token.type.name(), token.value());
                    break;
                case NUMERIC:
                    stdout.printf("%s: %s * %d%n", token.type.name(), token.value(), token.radix());
                    break;
                default:
                    throw new AssertionError(token.type);
            }
        }
    }

    public static class CompileException extends RuntimeException {

        public CompileException(String message) {
            super(message);
        }
    }
}
