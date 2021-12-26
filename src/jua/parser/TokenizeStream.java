package jua.parser;

import jua.parser.ast.Position;

import java.io.*;

public class TokenizeStream implements Closeable {

    public static TokenizeStream fromFile(String filename) throws IOException {
        try (Reader reader = new FileReader(filename)) {
            StringWriter beta = new StringWriter();
            for (int c; (c = reader.read()) >= 0; ) {
                beta.write(c);
            }
            return new TokenizeStream(filename, beta.toString());
        }
    }

    private final String filename;

    private String content;

    private int pos = 0;

    private int line = 1;

    private int offset = 0;

    private Position savedPosition;

    public TokenizeStream(String filename, String content) {
        this.filename = filename;
        this.content = content;
    }

    public String filename() {
        return filename;
    }

    public boolean availableMore() {
        return pos < content.length();
    }

    public int peek() {
        return availableMore() ? content.charAt(pos) : -1;
    }

    public int next() {
        int next;
        // for correctly printing positions of errors
        if ((next = availableMore() ? content.charAt(pos++) : -1) == '\n') {
            nextLine();
        } else {
            offset++;
        }
        savedPosition = null;
        return next;
    }

    public void nextLine() {
        line++;
        offset = 0;
    }

    public Position getPosition() {
        if (savedPosition == null) {
            savedPosition = new Position(filename, line, offset);
        }
        return savedPosition;
    }

    @Override
    public void close() {
        content = null;
    }
}
