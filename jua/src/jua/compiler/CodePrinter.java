package jua.compiler;

import jua.runtime.code.CodeSegment;
import jua.interpreter.instruction.Instruction;
import jua.runtime.JuaFunction;
import jua.runtime.heap.Operand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CodePrinter {

    private static class Case {

        private final int[] operands;

        private final int index;

        private final CodeSegment program;

        private Case(int[] operands, int index, CodeSegment program) {
            this.operands = operands;
            this.index = index;
            this.program = program;
        }

        @Override
        public String toString() {
            String operands0 = (operands == null) ? "default" : Arrays.stream(operands)
                    .mapToObj(a -> program.constantPool().at(a).toString())
                    .collect(Collectors.joining(", "));
            return String.format("%s: ->%d", operands0, index);
        }
    }

    private static class PrintState {

        private int index;

        private String name;

        private List<String> operands;

        private List<Case> cases;

        int line;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (line != 0) {
                sb.append(String.format("L%-6s ", line + ":"));
            } else {
                sb.append("        ");
            }
            if (!cases.isEmpty()) { // operands empty
                String format = String.format("%5d: %-5s ", index, name);
                sb.append(format);
                sb.append('{').append(System.lineSeparator());
                String align = String.format("%" + (format.length() + 1) + "s", " ");
                cases.forEach(c -> {
                    sb.append(align).append("\t\t").append(c).append(System.lineSeparator());
                });
                sb.append(align).append("}");
            } else {
                sb.append(String.format("%5d: %-15s %s", index, name, String.join(", ", operands)));
            }
            return sb.toString();
        }
    }

    public static void printConstants(Map<String, Operand> constants) {
        // Do not print constants
//        boolean[] test = new boolean[1];
//        constants.forEach((name, constant) -> {
//            test[0] = true;
//            System.out.printf("const %s = %s%n", name, constant);
//        });
//        if (test[0]) {
//            System.out.println();
//        }
    }

    public static void printFunctions(JuaFunction[] functions) {
        Arrays.stream(functions).forEach(function -> {
            String name = function.name();
            System.out.print("fn " + name + "(");
            CodeSegment p = function.codeSegment();
            for (int i = 0; i < function.maxNumArgs(); i++) {
                if (i > 0) {
                    System.out.print(", ");
                }
                System.out.print(p.localNameTable().nameOf(i));
                if (i >= function.minNumArgs()) {
                    System.out.print(" = ");
                    System.out.print(p.constantPool().defaultLocalAt(i).toString());
                }
            }
            System.out.println(") {");
            print(p, 1);
            System.out.println("}");
        });
    }

    public static void print(CodeSegment program, int align) {
        CodePrinter printer = new CodePrinter(program);
        printer.setAlign(align);
        printer.printHead(program);
        int lastLineNumber = 0;
        Instruction[] code = program.code();
        int length = code.length;
        for (int i = 0; i < length; i++) {
            code[i].print(printer);
            int line = program.lineNumberTable().lineNumberOf(i);
            if (line != lastLineNumber) {
                printer.printLine(line);
                lastLineNumber = line;
            }
            printer.flushAndNext();
        }
//        for (State state: program.states) {
//            state.print(printer);
//            printer.flushAndNext();
//        }
//        printer.printLines(program);
        System.out.println();
    }

    private void printLine(int line) {
        current.line = line;
    }

    private final CodeSegment program;

    private int index;

    private PrintState current;

    private int align;

    private CodePrinter(CodeSegment program) {
        super();
        this.program = program;
    }

    public void setAlign(int align) {
        this.align = align;
    }

    private void printHead(CodeSegment program) {
        System.out.printf("Code:   stack: %d, locals: %d%n", program.maxStack(), program.maxLocals());
    }

    @Deprecated
    private void printLines(CodeSegment program) {
//        System.out.println("Lines:");
//        Map<Integer, List<Integer>> lines = new TreeMap<>(Comparator.comparingInt(a -> a));
//
//        for (int i = 0; i < program.lineTable.length; i++) {
//            int line = program.lineTable[i];
//            if (line == 0) continue;
//            if (!lines.containsKey(line)) lines.put(line, new ArrayList<>());
//            lines.get(line).add(i);
//        }
//        lines.forEach((line, ops) -> {
//            StringJoiner sj = new StringJoiner(", ");
//            for (int op: ops) {
//                sj.add("#" + op);
//            }
//            System.out.printf("%4d: %s%n", line, sj);
//        });
    }

    public void printName(String name) {
        preparePrint().name = name;
    }

    public void printLocal(int id) {
        print(String.format("%d (%s)", id, program.localNameTable().nameOf(id)));
    }

    public void printIp(int ip) {
        print("->" + (this.index + ip - 1));
    }

    public void print(Object operand) {
        preparePrint().operands.add(String.valueOf(operand));
    }

    public void printCase(int[] operands, int index) {
        preparePrint().cases.add(new Case(operands, this.index + index - 1, program));
    }

    public void printLiteral(int index) {
        Operand constant = program.constantPool().at(index);
        preparePrint().operands.add(constant.type().name + " " + constant.toString());
    }

    private PrintState preparePrint() {
        if ((current) == null) {
            current = new PrintState();
            current.index = (index++);
            current.operands = new ArrayList<>();
            current.cases = new ArrayList<>();
        }
        return current;
    }

    private void flushAndNext() {
        for (int i = 0; i < align; i++) {
            System.out.print(" ");
        }
        System.out.println(current);
        current = null;
    }
}
