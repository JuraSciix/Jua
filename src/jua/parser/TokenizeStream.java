package jua.parser;

import jua.compiler.LineMap;

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

    // todo: почему здесь используется строка???
    private String content;

    private int pos = 0;

    @Deprecated
    private int line = 1;

    @Deprecated
    private int offset = 0;

//    private Tree.Position savedPosition;

    public TokenizeStream(String filename, String content) {
        this.filename = filename;
        this.content = content;
        debugContent();
    }

    private void debugContent() {
//        System.out.println("content is null?: " + (content == null)); // DEBUG
    }

    public String filename() {
        return filename;
    }

    private LineMap lmt;

    public LineMap getLmt() {
        debugContent();
        if (lmt == null) lmt = new LineMap(content);
        return lmt;
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
//        savedPosition = null;
        return next;
    }

    @Deprecated
    public void nextLine() {
        line++;
        offset = 0;
    }

    public int getPosition() {
        return pos;
    }

    @Override
    public void close() {
//        content = null;
    }
}
