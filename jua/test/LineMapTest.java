import jua.util.LineMap;

public class LineMapTest {

    public static void main(String[] args) {
        LineMap lineMap = new LineMap(" \n \n");

        System.out.println(lineMap.getLineNumber(0)); // 1
        System.out.println(lineMap.getLineNumber(1)); // 1
        System.out.println(lineMap.getLineNumber(2)); // 2
        System.out.println(lineMap.getLineNumber(3)); // 2
        System.out.println(lineMap.getLineNumber(4)); // 3

        System.out.println("====================================");
        System.out.println(lineMap.getOffsetNumber(1)); // 1
        System.out.println(lineMap.getOffsetNumber(2)); // 0
        System.out.println(lineMap.getOffsetNumber(3)); // 1
    }
}
