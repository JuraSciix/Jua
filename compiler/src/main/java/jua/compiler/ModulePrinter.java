package jua.compiler;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

import static jua.compiler.InstructionUtils.*;

public class ModulePrinter {

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
                    .mapToObj(index -> executable.constantPool.getAddressEntry(index).toBeautifulString())
                    .collect(Collectors.joining(", "));
            return String.format("%s -> %d", agentLiterals, casePC);
        }
    }

    private class InstructionData {
        String name;
        final Collection<String> operands = new LinkedList<>();
        final Collection<Case> cases = new LinkedList<>();

        void doPrint() {
            int line = executable.lineNumberTable.getLineNumber(pc);
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
        ModulePrinter printer = new ModulePrinter(module, System.out);
        printer.f("Source \"%s\": %n", module.source.fileName);
        printer.adjustIndent(2);
        for (Executable executable : module.executables) {
            if (executable == null) {
                // Native.
                continue;
            }
            printer.printFunction(executable);
        }
        printer.adjustIndent(-2);
    }

    private class InstrNodePrinter implements InstrVisitor {

        @Override
        public void visitJump(JumpInstrNode node) {
            printOPCode(node.opcode);
            printCp(node.offset);
        }

        @Override
        public void visitSingle(SingleInstrNode node) {
            printOPCode(node.opcode);
        }

        @Override
        public void visitCall(CallInstrNode node) {
            printOPCode(node.opcode); // node.opcode always must be OPCodes.Call
            printFuncRef(node.callee);
            print(node.argc);
        }

        @Override
        public void visitIndexed(IndexedInstrNode node) {
            printOPCode(node.opcode);
            if (node.opcode == OPCodes.GetConst) {
                // todo: remove getconst
                printConstRef(node.index);
            } else {
                printLocal(node.index);
            }
        }

        @Override
        public void visitConst(ConstantInstrNode node) {
            printOPCode(node.opcode);
            printLiteral(node.index);
        }
    }

    private void printFunction(Executable executable) {
        this.executable = executable;
        beginFunction();
        // Сбрасываем поля.
        tos = 0;
        tosAdjustment = 0;
        pc = 0;
        curLineNum = 0;
        InstrNodePrinter printer = new InstrNodePrinter();
        for (int i = 0; i < executable.code.length; i++) {
            initPrinter();
            InstrNode node = executable.code[i];
            pc = i;
            while (tosRestoring != null && tosRestoring.pc <= pc) {
//                System.out.printf("Restoring TOS at PC=%d, TOS=%d %n", pc, tosRestoring.tos);
                tos = tosRestoring.tos;
                tosRestoring = tosRestoring.next;
            }
            // tos должен обновляться после того, как напечатается инструкция,
            // чтобы принтер отображал реальное значение tos перед выполнением инструкции.
            tosAdjustment = node.stackAdjustment();
            node.accept(printer);
            instrData.doPrint();
            tos += tosAdjustment;
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

    Executable executable;
    int pc = 0, tos = 0, tosAdjustment;
    int curLineNum = 0;

    private ModulePrinter(Module module, PrintStream stream) {
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
        String sig = String.join(", ", Arrays.copyOf(executable.varnames, executable.totargs));
        f("fn %s(%s): %n", executable.name, sig);                      // fn sum(a, b):
        adjustIndent(2);                                              //   _
        f("Code: %n");                                               //   Code:
        adjustIndent(2);                                              //     _
        f("stack=%d, locals=%d %n", executable.stackSize, executable.regSize); //     stack=2, locals=0
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

    public void printOPCode(int opcode) {
        instrData.name = getOpcodeName(opcode);
    }

    public void printLocal(int index) {
        instrData.operands.add(executable.varnames[index]);
    }

    public void printCp(int offsetJump) {
        print(String.valueOf(offsetJump));
    }

    public void print(Object operand) {
        instrData.operands.add(String.valueOf(operand));
    }

    public void restoreTosIn(int offset) {
        if (offset > 0) {
            tosRestoring = mergeTos(tosRestoring, new TosRestoring(pc + offset, tos, null));
        }
    }

    public void printCase(int[] operands, int offsetJump) {
        instrData.cases.add(new Case(operands, pc + offsetJump));
    }

    public void printLiteral(int index) {
        instrData.operands.add(executable.constantPool.getAddressEntry(index).toBeautifulString());
    }

    public void beginSwitch() {

    }

    public void endSwitch() {

    }

    public void printFuncRef(int index) {
        instrData.operands.add('"' + module.functionNames[index] + '"');
    }

    public void printConstRef(int index) {
        instrData.operands.add('"' + module.constants[index].name + '"');
    }
}
