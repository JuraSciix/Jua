package jua.compiler;

import jua.util.LineMap;

import java.io.*;

// todo: Если не удалю, то переименовать во что-нибудь покрасивее
public class TokenizeStream implements Closeable {

    public static TokenizeStream fromFile(String filename) throws IOException {
        final int BUFFER_SIZE = 1024;
        try (Reader reader = new FileReader(filename)) {
            CharArrayWriter beta = new CharArrayWriter();
            char[] buffer = new char[BUFFER_SIZE];
            int len;
            while ((len = reader.read(buffer, 0, BUFFER_SIZE)) >= 0) {
                beta.write(buffer, 0, len);
            }
            return new TokenizeStream(filename, beta.toCharArray());
        }
    }

    private final String filename;

    private char[] content;

    private int pos = 0;

    @Deprecated
    private int line = 1;

    @Deprecated
    private int offset = 0;

//    private Tree.Position savedPosition;

    public TokenizeStream(String filename, char[] content) {
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
        return pos < content.length;
    }

    public int peek() {
        return availableMore() ? content[(pos)] : -1;
    }

    public int next() {
        int next;
        // for correctly printing positions of errors
        if ((next = availableMore() ? content[(pos++)] : -1) == '\n') {
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
