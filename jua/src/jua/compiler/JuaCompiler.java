package jua.compiler;

import jua.compiler.Tokens.Token;
import jua.compiler.Tokens.TokenType;
import jua.interpreter.Address;
import jua.runtime.Function;
import jua.utils.IOUtils;
import jua.utils.Options;

import java.io.File;
import java.io.IOException;

public final class JuaCompiler {

    public static Program compileFile(File file) {
        char[] filecontents;
        try {
            filecontents = IOUtils.readCharsFromFile(file);
        } catch (IOException e) {
            System.err.println("Unable access to file.");
            System.exit(1);
            return null;
        }
        Log log = new Log(System.err, Options.logMaxErrors());
        Source source = new Source(file.getName(), filecontents, log);

        if (Options.isLintEnabled()) {
            Lexer tokenizer = new Lexer(source);
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
            JuaParser parser = new JuaParser(source);
            Tree.CompilationUnit compilationUnit = parser.parseCompilationUnit();
            if (Options.isShouldPrettyTree()) {
                compilationUnit.accept(new Pretty(System.err));
                return null;
            }
            ProgramScope programScope = new ProgramScope();
            compilationUnit.accept(new Lower(programScope));
            compilationUnit.accept(new Enter(programScope));

            compilationUnit.sym.code = new Code(programScope, source);
            compilationUnit.functions.forEach(funcDef -> funcDef.sym.code = new Code(programScope, source));

            compilationUnit.accept(new Check(programScope));
            compilationUnit.accept(new Flow());

            if (log.hasErrors()) {
                return null;
            }

            compilationUnit.accept(compilationUnit.sym.code.gen);
            compilationUnit.functions
                    .forEach(funcDef -> {
                        funcDef.accept(funcDef.sym.code.gen);
                        programScope.lookupFunction(funcDef.name).runtimefunc = funcDef.sym.runtimefunc;
                    });

            Function mainFunction = compilationUnit.sym.runtimefunc;
            Function[] functions = programScope.collectFunctions();
            Address[] constantAddresses = programScope.collectConstantAddresses();

            return new Program(source, mainFunction, functions, constantAddresses);
        } catch (CompileException e) {
            System.err.println("Compiler error occurred");
            e.printStackTrace();
            return null;
        }
    }
}
