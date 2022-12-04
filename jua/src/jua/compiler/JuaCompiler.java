package jua.compiler;

import jua.util.IOUtils;

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
        try {
            ProgramLayout programLayout = new ProgramLayout();

            Parser parser = new JuaParser(source);
            programLayout.topTree = parser.parseCompilationUnit();
            programLayout.lower = new Lower();
            Program program = programLayout.buildProgram();
            if (!source.getLog().hasErrors()) {
                return program;
            }
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
