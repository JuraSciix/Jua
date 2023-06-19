package jua.compiler;

import jua.interpreter.Address;
import jua.interpreter.instruction.Instruction;
import jua.runtime.Function;
import jua.runtime.code.CodeData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CodePrinter implements jua.interpreter.instruction.CodePrinter {

    private static final boolean PRINT_STACK_ADJUSTMENT = false;

    private static class Case {

        private final int[] operands;

        private final int index;

        private final CodeData program;

        private Case(int[] operands, int index, CodeData program) {
            this.operands = operands;
            this.index = index;
            this.program = program;
        }

        @Override
        public String toString() {
            String operands0 = (operands == null) ? "default" : Arrays.stream(operands)
                    .mapToObj(index -> {
                        address.set(program.constantPool.getAddress(index));
                        return address.toBeautifulString();
                    })
                    .collect(Collectors.joining(", "));
            return String.format("%s: ->%d", operands0, index);
        }
    }

    private static class PrintState {

        private int index;

        int stacktop, stackAdjustment;

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
                sb.append(PRINT_STACK_ADJUSTMENT ?
                        String.format("%5d: %-12s %-40s %3d(%2d)", index, name, String.join(", ", operands), stacktop, stackAdjustment) :
                        String.format("%5d: %-12s %-40s", index, name, String.join(", ", operands)));
            }
            return sb.toString();
        }
    }

    public static void printFunctions(Program script, ArrayList<Function> functions) {
        functions.forEach(function -> {
            if (function == null || (function.flags & Function.FLAG_NATIVE) != 0) return;
            CodeData p = function.userCode();
            System.out.print("fn " + function);
            System.out.println(" { /* id=" + functions.indexOf(function) + " */");
            print(script, p, 1);
            System.out.println("}");
        });
    }

    public static void print(Program script, CodeData program, int align) {
        CodePrinter printer = new CodePrinter(script, program);
        printer.setAlign(align);
        printer.printHead(program);
        int lastLineNumber = 0;
        Instruction[] code = program.code;
        int length = code.length;
        for (int i = 0; i < length; i++) {
            printer.stacktop += (printer.stackAdjustment = code[i].stackAdjustment());
            code[i].print(printer);
            int line = program.lineNumTable.getLineNumber(i);
            if (line != lastLineNumber) {
                // todo: Sometimes an instruction that is not on the first line outputs L1
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

    private final Program script;

    private final CodeData program;

    private int index = 0;

    private int stacktop = 0, stackAdjustment;

    private PrintState current;

    private int align;

    private CodePrinter(Program script, CodeData program) {
        super();
        this.script = script;
        this.program = program;
    }

    public void setAlign(int align) {
        this.align = align;
    }

    private void printHead(CodeData program) {
        System.out.printf("Code:   stack: %d, locals: %d%n", program.stackSize, program.registers);
    }

    @Override
    public void printName(String name) {
        preparePrint().name = name;
    }

    @Override
    public void printLocal(int id) {
        print("$" + id);
    }

    @Override
    public void printIp(int ip) {
        print("->" + (this.index + ip - 1));
    }

    @Override
    public void print(Object operand) {
        preparePrint().operands.add(String.valueOf(operand));
    }

    @Override
    public void printCase(int[] operands, int index) {
        preparePrint().cases.add(new Case(operands, this.index + index - 1, program));
    }

    private static final Address address = new Address();

    @Override
    public void printLiteral(int index) {
        address.set(program.constantPool.getAddress(index));
        preparePrint().operands.add(String.format("#%d %s", index, address.toBeautifulString()));
    }

    public void printFunctionRef(int index) {
        Function function = script.functions[index];
        // test.jua:hello@8
        // stub_module:sum@10
        preparePrint().operands.add(String.format("%s:%s@%d", function.module, function.name, index));
    }

    public void printConstRef(int index) {
        // todo: Выводить название константы.
        print(String.format("@%d", index));
    }

    private PrintState preparePrint() {
        if ((current) == null) {
            current = new PrintState();
            current.index = (index++);
            current.stacktop = stacktop;
            current.stackAdjustment = stackAdjustment;
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
