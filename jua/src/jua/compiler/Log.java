package jua.compiler;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Log {
    private static final int maxErrors = 1;
    private final PrintStream bufferStream;
    private final Source source;
    private int errors;

    public Log(PrintStream stream, Source source) {
        this.source = source;
        bufferStream = new PrintStream(new BufferedOutputStream(stream));
    }


    public boolean hasErrors() { return errors > 0;}

    public void flush() {
        bufferStream.flush();
    }

    public void error(int pos, String msg) {
        errors++;

        //Compile error: %message%
        //File: %file%, line: %line%
        //%source%
        //%pointer%
        try {
            LineMap lineMap = source.getLineMap();
            bufferStream.println("Compile error: " + msg);
            printPosition(source.filename(),
                    lineMap.getLineNumber(pos),
                    lineMap.getColumnNumber(pos)
            );
            bufferStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (errors >= maxErrors)
            throw new CompileInterrupter();
    }

    public void waring(String msg) {
        bufferStream.println("Warning: " + msg);
        bufferStream.flush();
    }

    public void error(int pos, String fmt, Object... args) {
        error(pos, String.format(fmt, args));
    }

    private void printPosition(String filename, int line, int offset) {
        bufferStream.printf("File: %s, line: %d.%n", filename, line);

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
        bufferStream.println(s);
        bufferStream.println(sb.append('^'));
    }
}
