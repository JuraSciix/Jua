package jua.compiler;

import java.util.ArrayList;

public final class LineMap {

    private final int[] lineStartPoints;

    private final int contentEndPoint;

    LineMap(char[] content) {
        int len = content.length;
        while (len > 0 && content[len - 1] <= ' ') {
            len--;
        }

        lineStartPoints = buildLineMap(new SourceReader(content, 0, len));
        contentEndPoint = len;
    }

    private int[] buildLineMap(SourceReader reader) {
        // todo: Не полагаться на обертки.
        ArrayList<Integer> lineStartPoints = new ArrayList<>();

        // Первая линия
        lineStartPoints.add(0);

        while (true) {
            int c = reader.peek();
            if (c == -1) { // EOF reached
                break;
            }
            if (c == '\n') {
                lineStartPoints.add(reader.pos() + 1);
            }
            reader.next();
        }

        return lineStartPoints.stream().mapToInt(a->a).toArray();
    }

    public int getLineNumber(int pos) {
        int index = findLineIndex(pos);
        return index + 1;
    }

    public int getColumnNumber(int pos) {
        int index = findLineIndex(pos);
        return pos - lineStartPoints[index];
    }

    public int getLineStart(int lineNum) {
        return lineStartPoints[lineNum - 1];
    }

    public int getLineEnd(int lineNum) {
        if (lineNum >= lineStartPoints.length)
            return contentEndPoint;
        return lineStartPoints[lineNum] - 1;
    }

    private int findLineIndex(int pos) {
        int bottom = 0;
        int top = lineStartPoints.length;
        int current = top >> 1;

        while ((top - bottom) > 1) {
           if (lineStartPoints[current] <= pos) {
               bottom = current;
           } else {
               top = current;
           }
           current = (bottom + top) >> 1;
        }

        return current;
    }
}
