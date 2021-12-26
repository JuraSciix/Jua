package jua.tools;

import jua.interpreter.Program;
import jua.interpreter.lang.Constant;
import jua.interpreter.lang.Function;
import jua.interpreter.lang.Operand;
import jua.interpreter.lang.ScriptFunction;
import jua.interpreter.states.State;

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

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (!cases.isEmpty()) { // operands empty
                sb.append(String.format("%4d: %-5s ", index, name));
                sb.append('{').append(System.lineSeparator());
                cases.forEach(c -> sb.append("\t\t").append(c).append(System.lineSeparator()));
                sb.append("      }");
            } else {
                sb.append(String.format("%4d: %-15s %s", index, name, String.join(", ", operands)));
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
            print(sf.builder);
        });
    }

    public static void print(Program.Builder builder) {
        CodePrinter printer = new CodePrinter();
        printer.printHead(builder);

        for (State state: builder.states) {
            state.print(printer);
            printer.printAndNext();
        }
        printer.printLines(builder);
        System.out.println();
    }

    private int index;

    private PrintState current;

    private CodePrinter() {
        super();
    }

    private void printHead(Program.Builder builder) {
        System.out.printf("stack: %d, locals: %d%n", builder.stackSize, builder.localsSize);
        System.out.println("Code:");
    }

    private void printLines(Program.Builder builder) {
        System.out.println("Lines:");
        Map<Integer, List<Integer>> lines = new TreeMap<>(Comparator.comparingInt(a -> a));

        for (int i = 0; i < builder.lines.length; i++) {
            int line = builder.lines[i];
            if (line == 0) continue;
            if (!lines.containsKey(line)) lines.put(line, new ArrayList<>());
            lines.get(line).add(i);
        }
        lines.forEach((line, ops) -> {
            StringJoiner sj = new StringJoiner(", ");
            for (int op: ops) {
                sj.add("#" + op);
            }
            System.out.printf("%4d: %s%n", line, sj);
        });
    }

    public void printName(String name) {
        preparePrint().name = name;
    }

    public void printIdentifier(String name, int id) {
        printOperand(String.format("%s ($%d)", name, id));
    }

    public void printIndex(int index) {
        printOperand("#" + index);
    }

    public void printOperand(Object operand) {
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

    private void printAndNext() {
        System.out.println(current);
        current = null;
    }
}
