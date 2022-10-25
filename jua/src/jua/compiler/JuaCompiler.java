package jua.compiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Objects;

public final class JuaCompiler {

    private final Iterator<Source> sources;

    public JuaCompiler(Iterable<Source> sources) {
        Objects.requireNonNull(sources, "Sources is null");
        this.sources = sources.iterator();
    }

    public CompileResult next() throws IOException {
        Source source = sources.next();

        // todo: CodeLayout должен уметь работать с несколькими сурсами одновременно.
        CodeLayout layout = new CodeLayout(source);

        Code code = layout.getCode();
        Types types = code.getTypes();

        try (Tokenizer tokenizer = new Tokenizer(source)) { // todo: Log
            JuaParser parser = new JuaParser(tokenizer, types, source.getLog());
            Tree tree = parser.parse();

            tree.accept(new Enter(layout));
            tree.accept(new Lower(types));
            Gen gen = new Gen(layout);
            tree.accept(gen);

            return gen.getResult();
        } catch (ParseException e) {
            error("Compile error", layout, e.getMessage(), e.position, true);
        } catch (CompileError e) {
            error("Compile error", layout, e.getMessage(), e.position, true);
        }
        throw new ThreadDeath();
    }

    public void error(String prefix, CodeLayout layout, String msg, int pos, boolean calculateLineNum) {
        System.err.println(prefix + ": " + msg);
        try {
            Source source = layout.source;
            LineMap lineMap = source.getLineMap();
            printPosition(source.filename(),
                    calculateLineNum ? lineMap.getLineNumber(pos) : pos,
                    calculateLineNum ? lineMap.getColumnNumber(pos) : -1);
        } catch (IOException ex) {
            //nope
        }
        System.exit(1);
    }

    private void printPosition(String filename, int line, int offset) {
        System.err.printf("File: %s, line: %d.%n", filename, line);

        if (offset >= 0) {
            printLine(filename, line, offset);
        }
    }

    private void printLine(String filename, int line, int offset) {
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

    private void printOffset(String s, int offset) {
        StringBuilder sb = new StringBuilder(offset);

        for (int i = 0; i < (offset - 1); i++) {
            sb.append(i >= s.length() || s.charAt(i) != '\t' ? ' ' : '\t');
        }
        System.err.println(s);
        System.err.println(sb.append('^'));
    }
}
