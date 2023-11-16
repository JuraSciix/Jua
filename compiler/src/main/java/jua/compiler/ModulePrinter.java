package jua.compiler;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

import static jua.compiler.InstructionUtils.*;

public class ModulePrinter {

    private static final boolean PRINT_STACK_ADJUSTMENT = true;

    private class Case {
        final int[] operands;
        final int caseCp;

        Case(int[] operands, int caseCp) {
            this.operands = operands;
            this.caseCp = caseCp;
        }

        void print() {
            String agentLiterals = (operands == null)
                    ? "else"
                    : Arrays.stream(operands)
                            .mapToObj(index -> executable.constantPool.getAddressEntry(index).toBeautifulString())
                            .collect(Collectors.joining(", "));
            printLine(String.format("%s -> %d", agentLiterals, caseCp));
        }
    }

    private class InstructionData {
        String name;
        final Collection<String> operands = new LinkedList<>();
        final Collection<Case> cases = new LinkedList<>();

        void doPrint() {
            int line = executable.lineNumberTable.getLineNumber(pc);
            if (curLineNum != line) {
                printAtStart(String.format("L%-5d ", line));
                curLineNum = line;
            } else {
                printAtStart(String.format(" %-5s ", " "));
            }

            if (PRINT_STACK_ADJUSTMENT) {
                print(String.format("%2s %-5d", (tosAdjustment > 0) ? "+" + tosAdjustment : tosAdjustment, tos));
            }

            print(String.format("%5s: %-12s %s ", pc, name, String.join(", ", operands)));

            if (!cases.isEmpty()) {
                printLine();
                printLine("{");
                adjustIndent(3);
                cases.forEach(Case::print);
                adjustIndent(-3);
                print("}");
            }

            printLine();
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
        PrintWriter w = new PrintWriter(System.out);
        ModulePrinter printer = new ModulePrinter(module, w);
        printer.printLine(String.format("Source \"%s\":", module.source.fileName));
        printer.adjustIndent(2);
        for (Module.Executable executable : module.executables) {
            if (executable == null) {
                // Native.
                continue;
            }
            printer.printFunction(executable);
        }
        printer.adjustIndent(-2);
        w.flush();
    }

    private class InstrNodePrinter implements InstrVisitor {

        @Override
        public void visitJump(JumpInstrNode node) {
            printOPCode(node.opcode);
            printCp(node.offset);
            restoreTosIn(node.offset);
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

        @Override
        public void visitSwitch(SwitchInstrNode node) {
            printOPCode(node.opcode);
            int[] labels = node.literals;
            int[] cps = node.dstIps;
            int[][] groupedLabels = new int[labels.length][];
            int[] groupedCps = new int[cps.length];
            int i = 0;

            assert labels.length == cps.length;
            // Фактически, мы проходим по массиву один раз.
            // То есть фактическая сложность O(n)
            for (int j = 0; j < cps.length; ) {
                for (int k = j + 1; k <= cps.length; k++) {
                    if (k >= cps.length || cps[j] != cps[k]) {
                        groupedLabels[i] = Arrays.copyOfRange(labels, j, k);
                        groupedCps[i] = cps[j]; // Можно взять произвольную от j до k, они все равны.
                        i++;
                        j = k;
                        break;
                    }
                }
            }

            for (int j = 0; j < i; j++) {
                printCase(groupedLabels[j], groupedCps[j]);
            }
            printCase(null, node.defCp);
        }
    }

    private void printFunction(Module.Executable executable) {
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
    private final PrintWriter writer;

    private InstructionData instrData;
    private int indent = 0;
    private char[] indentString = new char[0];
    private boolean newLine = true;
    private TosRestoring tosRestoring;

    Module.Executable executable;
    int pc = 0, tos = 0, tosAdjustment;
    int curLineNum = 0;

    private ModulePrinter(Module module, PrintWriter writer) {
        this.module = module;
        this.writer = writer;
    }

    private void adjustIndent(int i) {
        if (i != 0) {
            indent += i;
            if (indent > indentString.length) {
                indentString = new char[indent];
                Arrays.fill(indentString, ' ');
            }
        }
    }

    private void beginFunction() {
        String sig = String.join(", ", Arrays.copyOf(executable.varnames, executable.totargs));
        printLine(String.format("fn %s(%s):", executable.name, sig));
        adjustIndent(2);
        printLine("Code:");
        adjustIndent(2);
        printLine(String.format("stack=%d, locals=%d", executable.stackSize, executable.regSize));
        adjustIndent(2);
    }

    private void endFunction() {
        adjustIndent(-2 * 3);
    }

    public void printAtStart(String str) {
        writer.print(str);
        newLine = false;
    }

    public void print(String str) {
        printIndent();
        writer.print(str);
    }

    public void printLine() {
        writer.println();
        newLine = true;
    }

    public void printLine(String str) {
        printIndent();
        writer.println(str);
        newLine = true;
    }

    private void printIndent() {
        if (newLine && indent > 0) {
            writer.write(indentString, 0, indent);
            newLine = false;
        }
    }

    public void printOPCode(int opcode) {
        instrData.name = getOpcodeName(opcode);
    }

    public void printLocal(int index) {
        // todo: new LocalNameTable
//        instrData.operands.add(executable.varnames[index]);
        instrData.operands.add(Integer.toString(index));
    }

    public void printCp(int offsetJump) {
        instrData.operands.add(String.valueOf(offsetJump));
    }

    public void print(Object operand) {
        instrData.operands.add(String.valueOf(operand));
    }

    public void restoreTosIn(int atCp) {
        if (atCp > 0) {
            tosRestoring = mergeTos(tosRestoring, new TosRestoring(atCp, tos, null));
        }
    }

    public void printCase(int[] operands, int caseCp) {
        instrData.cases.add(new Case(operands, caseCp));
        restoreTosIn(caseCp);
    }

    public void printLiteral(int index) {
        instrData.operands.add(executable.constantPool.getAddressEntry(index).toBeautifulString());
    }

    public void printFuncRef(int index) {
        instrData.operands.add('"' + module.functionNames[index] + '"');
    }

    public void printConstRef(int index) {
        instrData.operands.add('"' + module.constants[index].name + '"');
    }
}
