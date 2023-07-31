package jua.compiler;

import jua.interpreter.instruction.Instruction;
import jua.interpreter.instruction.InstructionPrinter;
import jua.runtime.Function;
import jua.runtime.code.CodeData;
import jua.utils.Assert;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class InstructionPrinterImpl implements InstructionPrinter {

    private static final boolean PRINT_STACK_ADJUSTMENT = true;

    private class Case {
        final int[] operands;
        final int casePC;

        Case(int[] operands, int casePC) {
            this.operands = operands;
            this.casePC = casePC;
        }

        @Override
        public String toString() {
            if (operands == null)
                return "default -> " + casePC;
            String agentLiterals = Arrays.stream(operands)
                    .mapToObj(index -> function.userCode().constantPool.getAddressEntry(index).toBeautifulString())
                    .collect(Collectors.joining(", "));
            return String.format("%s -> %d", agentLiterals, casePC);
        }
    }

    private class InstructionData {
        String name;
        final Collection<String> operands = new LinkedList<>();
        final Collection<Case> cases = new LinkedList<>();

        void doPrint() {
            int line = function.userCode().lineNumTable.getLineNumber(pc);
            if (curLineNum != line) {
                k("L%-5d ", line);
                curLineNum = line;
            } else {
                k(" %-5s ", " ");
            }

            if (PRINT_STACK_ADJUSTMENT && cases.isEmpty()) {
                k("%2s %-5d", (tosAdjustment > 0) ? "+" + tosAdjustment : tosAdjustment, tos);
            }

            k("%5s: %-12s %s ", pc, name, String.join(", ", operands));

            if (!cases.isEmpty()) {
                k("{ %n");
                adjustIndent(3);
                cases.forEach(c -> f("%s %n", c));
                adjustIndent(-3);
                f("} ");
            }

            f("%n");
        }
    }

    private static class TosRestoring {
        final int pc, tos;
        final TosRestoring next;

        TosRestoring(int pc, int tos, TosRestoring next) {
            this.pc = pc;
            this.tos = tos;
            this.next = next;
        }
    }

    private static TosRestoring mergeTos(TosRestoring a, TosRestoring b) {
        if (a == null) return b;
        if (b == null) return a;
        return (a.pc < b.pc) ?
                new TosRestoring(a.pc, a.tos, mergeTos(a.next, b)) :
                new TosRestoring(b.pc, b.tos, mergeTos(b.next, a));
    }

    public static void printModule(Module module) {
        InstructionPrinterImpl printer = new InstructionPrinterImpl(module, System.out);
        printer.f("Source \"%s\": %n", module.source.fileName);
        printer.adjustIndent(2);
        printer.printFunction(module.main);
        for (Function function : module.functions) {
            Assert.check(function != null, "module contains null-reference function");
            if ((function.flags & Function.FLAG_NATIVE) != 0) {
                continue;
            }
            printer.printFunction(function);
        }
        printer.adjustIndent(-2);
    }

    private void printFunction(Function function) {
        this.function = function;
        beginFunction();
        // Сбрасываем поля.
        tos = 0;
        tosAdjustment = 0;
        pc = 0;
        curLineNum = 0;
        CodeData code = function.userCode();
        for (int i = 0; i < code.code.length; i++) {
            initPrinter();
            Instruction instr = code.code[i];
            pc = i;
            while (tosRestoring != null && tosRestoring.pc <= pc) {
//                System.out.printf("Restoring TOS at PC=%d, TOS=%d %n", pc, tosRestoring.tos);
                tos = tosRestoring.tos;
                tosRestoring = tosRestoring.next;
            }
            tosAdjustment = instr.stackAdjustment();
            tos += tosAdjustment;
            instr.print(this);
            instrData.doPrint();
        }
        endFunction();
    }

    private void initPrinter() {
        instrData = new InstructionData();
    }

    private final Module module;
    private final PrintStream stream;

    private InstructionData instrData;
    private int indent = 0;
    private char[] indentString = new char[0];
    private TosRestoring tosRestoring;

    Function function;
    int pc = 0, tos = 0, tosAdjustment;
    int curLineNum = 0;

    private InstructionPrinterImpl(Module module, PrintStream stream) {
        this.module = module;
        this.stream = stream;
    }

    private void adjustIndent(int i) {
        if (i != 0) {
            indent += i;
            indentString = new char[indent];
            Arrays.fill(indentString, ' ');
        }
    }

    private void beginFunction() {
        String sig = String.join(", ", Arrays.copyOf(function.userCode().localNames, function.maxArgc));
        CodeData code = function.userCode();
        f("fn %s(%s): %n", function.name, sig);                      // fn sum(a, b):
        adjustIndent(2);                                              //   _
        f("Code: %n");                                               //   Code:
        adjustIndent(2);                                              //     _
        f("stack=%d, locals=%d %n", code.stack, code.locals); //     stack=2, locals=0
        adjustIndent(2);                                              //       _
    }

    private void endFunction() {
        adjustIndent(-2 * 3);
    }

    void t(String str) {
        stream.print(str);
    }

    void k(String str, Object... args) {
        stream.printf(str, args);
    }

    void p(String str) {
        stream.print(indentString);
        stream.print(str);
    }

    void f(String str, Object... args) {
        stream.print(indentString);
        stream.printf(str, args);
    }

    @Override
    public void printName(String name) {
        instrData.name = name;
    }

    @Override
    public void printLocal(int index) {
        instrData.operands.add(function.userCode().localNames[index]);
    }

    @Override
    public void printCp(int offsetJump) {
        print(String.valueOf(offsetJump));
    }

    @Override
    public void print(Object operand) {
        instrData.operands.add(String.valueOf(operand));
    }

    @Override
    public void restoreTosIn(int offset) {
        if (offset > 0) {
            tosRestoring = mergeTos(tosRestoring, new TosRestoring(pc + offset, tos, null));
        }
    }

    @Override
    public void printCase(int[] operands, int offsetJump) {
        instrData.cases.add(new Case(operands, pc + offsetJump));
    }

    @Override
    public void printLiteral(int index) {
        instrData.operands.add(function.userCode().constantPool.getAddressEntry(index).toBeautifulString());
    }

    @Override
    public void beginSwitch() {

    }

    @Override
    public void endSwitch() {

    }

    @Override
    public void printFuncRef(int index) {
        instrData.operands.add('"' + module.functions[index].name + '"');
    }

    @Override
    public void printConstRef(int index) {
        instrData.operands.add('"' + module.constants[index].name + '"');
    }
}