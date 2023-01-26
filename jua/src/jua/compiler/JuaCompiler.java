package jua.compiler;

import jua.compiler.Tokens.Token;
import jua.compiler.Tokens.TokenType;
import jua.utils.IOUtils;
import jua.utils.Options;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public final class JuaCompiler {

    public static Program compileFile(File file) {
        String filecontents;
        try {
            filecontents = new String(IOUtils.readCharsFromFile(file));
        } catch (IOException e) {
            System.err.println("Unable access to file.");
            System.exit(1);
            return null;
        }
        Source source = new Source(file.getName(), filecontents);

        if (Options.isLintEnabled()) {
            Lexer tokenizer = new Tokenizer(source);
            for (Token token = tokenizer.nextToken();
                 token.type != TokenType.EOF;
                 token = tokenizer.nextToken()) {
                System.out.printf("[%d:%d] ",
                        source.getLineMap().getLineNumber(token.pos),
                        source.getLineMap().getColumnNumber(token.pos));
                switch (token.type.kind) {
                    case DEFAULT:
                        System.out.println(token.type.name());
                        break;
                    case NAMED:
                        System.out.printf("%s: %s%n", token.type.name(), token.name());
                        break;
                    case STRING:
                        System.out.printf("%s: \"%s\"%n", token.type.name(), token.value());
                        break;
                    case NUMERIC:
                        System.out.printf("%s: %s * %d%n", token.type.name(), token.value(), token.radix());
                        break;
                    default:
                        throw new AssertionError(token.type);
                }
            }
            return null;
        }
        try {
            ProgramLayout programLayout = new ProgramLayout();

            Parser parser = new JuaParser(source);
            programLayout.topTree = parser.parseCompilationUnit();
            if (Options.isShouldPrettyTree()) {
                PrintWriter writer = new PrintWriter(System.err);
                programLayout.topTree.accept(new Pretty(System.err));
                writer.flush();
                return null;
            }
            Program program = programLayout.buildProgram();
            if (!source.getLog().hasErrors()) {
                return program;
            }
        } catch (CompileException e) {
            System.err.println("Compiler error occurred");
            e.printStackTrace();
        } catch (CompileInterrupter ignore) {
        } finally {
            if (source.getLog().hasMessages()) {
                source.getLog().flush(System.err);
            }
        }

        source.getLog().flush(System.err);
        return null;
    }
}
