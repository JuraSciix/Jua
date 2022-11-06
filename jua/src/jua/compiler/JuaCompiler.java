package jua.compiler;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public final class JuaCompiler {

    private Log log;
    private final Iterator<Source> sources;

    public JuaCompiler(Iterable<Source> sources) {
        Objects.requireNonNull(sources, "Sources is null");
        this.sources = sources.iterator();
    }

    public CompileResult next() throws IOException {
        Source source = sources.next();

        // todo: CodeLayout должен уметь работать с несколькими сурсами одновременно.
        CodeLayout layout = new CodeLayout(source);

        log = source.createLog();

        Code code = layout.getCode();
        Types types = code.getTypes();

        try (Tokenizer tokenizer = new Tokenizer(source)) { // todo: Log
            JuaParser parser = new JuaParser(tokenizer, types, log);
            Tree tree = parser.parse();

            tree.accept(new Enter(layout));
            tree.accept(new Lower(types));
            Gen gen = new Gen(layout);
            tree.accept(gen);

            return gen.getResult();
//        }
//        } catch (ParseException e) {
//            error("Compile error", layout, e.getMessage(), e.position, true);
        } catch (CompileError e) {
            log.error(e.position, e.getMessage());
        } catch (CompileInterrupter interrupter) {
            log.flush();
        } catch (Exception e) {
            e.printStackTrace(); // todo
        }
        throw new ThreadDeath();
    }

    public Log getLog() {
        return log;
    }
}
