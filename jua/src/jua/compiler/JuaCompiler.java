package jua.compiler;

import jua.compiler.Tokens.Token;
import jua.compiler.Tokens.TokenType;
import jua.interpreter.Address;
import jua.runtime.JuaFunction;
import jua.utils.IOUtils;
import jua.utils.Options;

import java.io.File;
import java.io.IOException;

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
        Log log = source.getLog();
        try {
            Parser parser = new JuaParser(source);
            Tree.CompilationUnit compilationUnit = parser.parseCompilationUnit();
            ProgramScope programScope = new ProgramScope();

            compilationUnit.code = new Code(programScope, source);
            compilationUnit.funcDefs.forEach(funcDef -> funcDef.code = new Code(programScope, source));

            compilationUnit.accept(new Enter(programScope));
            compilationUnit.accept(new Lower(programScope));
            compilationUnit.accept(new Check(programScope, log));
            compilationUnit.accept(new Flow());

            if (log.hasErrors()) {
                log.flush(System.err);
                return null;
            }

            compilationUnit.accept(compilationUnit.code.gen);
            compilationUnit.funcDefs
                    .forEach(funcDef -> {
                        funcDef.accept(funcDef.code.gen);
                        programScope.lookupFunction(funcDef.name).runtimefunc = funcDef.code.gen.resultFunc;
                    });

            JuaFunction mainFunction = compilationUnit.code.gen.resultFunc;
            JuaFunction[] functions = programScope.collectFunctions();
            Address[] constantAddresses = programScope.collectConstantAddresses();

            return new Program(source, mainFunction, functions, constantAddresses);
        } catch (CompileException e) {
            System.err.println("Compiler error occurred");
            e.printStackTrace();
        } catch (CompileInterrupter ignore) {
        } finally {
            if (log.hasMessages()) {
                log.flush(System.err);
            }
        }

        log.flush(System.err);
        return null;
    }
}
