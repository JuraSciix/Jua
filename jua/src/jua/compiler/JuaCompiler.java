package jua.compiler;

import jua.util.IOUtils;

import java.io.File;
import java.io.IOException;

public final class JuaCompiler {

    public static CompilerResult compileFile(File file) {
        String filecontents;
        try {
            filecontents = new String(IOUtils.readCharsFromFile(file));
        } catch (IOException e) {
            System.err.println("Unable access to file.");
            System.exit(1);
            return null;
        }
        Source source = new Source(file.getName(), filecontents);

        CodeLayout codeLayout = new CodeLayout(source);
        Code code = codeLayout.getCode();
        MinorGen codegen = new MinorGen(codeLayout, source.getLog());
        JuaParser parser = new JuaParser(source, code.getTypes());
        Tree tree = parser.parse();
        tree.accept(new Enter(codeLayout, source.getLog()));
        tree.accept(new Lower(code.getTypes()));
        tree.accept(codegen);
        if (source.getLog().hasMessages()) {
            source.getLog().flush(System.err);
        }
        if (source.getLog().hasErrors()) {
            return null;
        }
        return codegen.getResult();
    }
}
