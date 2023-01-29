package jua.compiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Log {
    private static final int maxErrors = 3;


    private final Source source;

    private int nerrs = 0;

    private final List<String> messages = new ArrayList<>();

    public Log(Source source) {
        this.source = source;
    }

    public boolean hasMessages() {
        return !messages.isEmpty();
    }

    public boolean hasErrors() {
        return nerrs > 0;
    }

    public void error(String msg) {
        nerrs++;

        //Compile error: %message%
        messages.add("Compile error: " + msg + "\n");

        if (nerrs >= maxErrors)
            throw new CompileInterrupter();
    }

    public void error(int pos, String msg) {
        nerrs++;

        //Compile error: %message%
        //File: %file%, line: %line%
        //%source%
        //%pointer%
        LineMap lineMap = source.getLineMap();
        StringBuilder buffer = new StringBuilder();
        buffer.append("Compile error: ").append(msg).append("\n");
        printPosition(buffer,
                lineMap.getLineNumber(pos),
                lineMap.getColumnNumber(pos)
        );
        messages.add(buffer.toString());

        if (nerrs >= maxErrors)
            throw new CompileInterrupter();
    }

    public void waring(String msg) {
        messages.add("Warning: " + msg + "\n");
    }

    public void error(int pos, String fmt, Object... args) {
        error(pos, String.format(fmt, args));
    }

    private void printPosition(StringBuilder buffer, int line, int offset) {
        buffer.append(String.format("File: %s, line: %d.%n", source.name, line));

        if (offset >= 0) {
            printLine(buffer, line, offset);
        }
    }

    private void printLine(StringBuilder buffer, int line, int offset) {
        String s;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(source.name))))) {
            while (--line > 0) {
                br.readLine();
            }
            s = br.readLine();
        } catch (IOException e) {
            return;
        }
        printOffset(buffer, (s == null) ? "" : s, offset);
    }

    private void printOffset(StringBuilder buffer, String s, int offset) {
        StringBuilder sb = new StringBuilder(offset);

        for (int i = 0; i < (offset - 1); i++) {
            sb.append(i >= s.length() || s.charAt(i) != '\t' ? ' ' : '\t');
        }
        buffer.append(s);
        buffer.append("\n");
        buffer.append(sb.append('^'));
        buffer.append("\n");
    }

    public void flush(PrintStream output) {
        messages.forEach(output::println);
        messages.clear();
    }
}
