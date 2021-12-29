package jua.tools;

import jua.interpreter.Program;
import jua.interpreter.lang.Constant;
import jua.interpreter.lang.Function;
import jua.interpreter.lang.Operand;
import jua.interpreter.lang.ScriptFunction;

import java.util.*;
import java.util.stream.Collectors;

public class CodePrinter {

    private static class Case {

        private final Operand[] operands;

        private final int index;

        private Case(Operand[] operands, int index) {
            this.operands = operands;
            this.index = index;
        }

        @Override
        public String toString() {
            String operands0 = (operands == null) ? "default" : Arrays.stream(operands)
                    .map(Operand::toString)
                    .collect(Collectors.joining(", "));
            return String.format("%s: #%d", operands0, index);
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
                sb.append(String.format("%5d: %-5s ", index, name));
                sb.append('{').append(System.lineSeparator());
                cases.forEach(c -> sb.append("\t\t").append(c).append(System.lineSeparator()));
                sb.append("      }");
            } else {
                sb.append(String.format("%5d: %-15s %s", index, name, String.join(", ", operands)));
            }
            return sb.toString();
        }
    }

    public static void printConstants(Map<String, Constant> constants) {
        boolean[] test = new boolean[1];
        constants.forEach((name, constant) -> {
            if (constant.isExtern)
                return;
            test[0] = true;
            System.out.printf("const %s = %s%n", name, constant.value);
        });
        if (test[0]) {
            System.out.println();
        }
    }

    public static void printFunctions(Map<String, Function> functions) {
        functions.forEach((name, function) -> {
            if (!(function instanceof ScriptFunction))
                return;
            ScriptFunction sf = (ScriptFunction) function;
            StringJoiner args = new StringJoiner(", ");

            for (int i = 0, o = (sf.args.length - sf.optionals.length); i < sf.args.length; i++) {
                if (i >= o) {
                    args.add(sf.args[i] + " = " + sf.optionals[i - o]);
                } else {
                    args.add(sf.args[i]);
                }
            }
            System.out.printf("fn %s(%s)%n", name, args);
            print(sf.program);
        });
    }

    public static void print(Program program) {
        CodePrinter printer = new CodePrinter(program);
        printer.printHead(program);
        int lastLineNumber = 0;
        for (int i = 0; i < program.states.length; i++) {
            program.states[i].print(printer);
            int line = program.getInstructionLine(i);
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

    private final Program program;

    private int index;

    private PrintState current;

    private CodePrinter(Program program) {
        super();
        this.program = program;
    }

    private void printHead(Program program) {
        System.out.printf("Code:   stack: %d, locals: %d%n", program.stackSize, program.localsSize);
    }

    @Deprecated
    private void printLines(Program program) {
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
        print(String.format("%d (%s)", id, program.localsNames[id]));
    }

    public void printIp(int ip) {
        print("->" + (this.index + ip - 1));
    }

    public void print(Object operand) {
        preparePrint().operands.add(String.valueOf(operand));
    }

    public void printCase(Operand[] operands, int index) {
        preparePrint().cases.add(new Case(operands, index));
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
        System.out.println(current);
        current = null;
    }
}
