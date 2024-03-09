package jua.compiler;

import jua.compiler.InstructionUtils.IndexedInstrNode;
import jua.compiler.InstructionUtils.InstrNode;
import jua.compiler.InstructionUtils.JumpInstrNode;
import jua.compiler.InstructionUtils.SingleInstrNode;
import jua.compiler.utils.Assert;

import java.util.*;

public final class Code {

    public static final int MAX_CONSTANT_POOL_SIZE = 65535;

    public static class Chain {
        final int pc, tos;
        final Chain next;

        Chain(int pc, int tos, Chain next) {
            Assert.check(next == null || next.tos == tos);
            this.pc = pc;
            this.tos = tos;
            this.next = next;
        }
    }

    static Chain mergeChains(Chain lhs, Chain rhs) {
        if (lhs == null) return rhs;
        if (rhs == null) return lhs;
        // Рекурсивная сортировка
        if (lhs.pc <= rhs.pc)
            return new Chain(lhs.pc, lhs.tos, mergeChains(lhs.next, rhs));
        else  // lhs.pc > rhs.pc
            return new Chain(rhs.pc, rhs.tos, mergeChains(rhs.next, lhs));
    }

    private final List<InstrNode> instructions = new ArrayList<>();
    private final TreeMap<Short, Integer> cpLineMap = new TreeMap<>();

    /** Top of stack. */
    private int tos = 0;

    /** Top of stack limit. */
    private int limTos = 0;

    /** Current line number. */
    private int cLineNum = 0;

    private boolean alive = true;

    public final ModuleScope programScope;
    public final Gen gen;

    private LineMap lineMap;

    // Set from Gen.visitFuncDef or Gen.visitCompilationUnit
    public ModuleScope.FunctionSymbol sym;

    private final Map<Object, Integer> constantPool = new LinkedHashMap<>();

    public Code(ModuleScope programScope, Source source) {
        this.programScope = programScope;
        this.lineMap = source.getLineMap();
        gen = new Gen();
        gen.code = this;
        gen.source = source;
    }

    public Chain branch(int opcode) {
        return new Chain(emitJump(opcode), tos(), null);
    }

    public int lineNum() {
        return cLineNum;
    }

    public InstrNode get(int pc) {
        return instructions.get(pc);
    }

    public int pc() {
        return instructions.size();
    }

    public int emitSingle(int opcode) {
        return emitNode(new SingleInstrNode(opcode));
    }

    public int emitIndexed(int opcode, int index) {
        return emitNode(new IndexedInstrNode(opcode, index));
    }

    public int emitJump(int opcode) {
        return emitNode(new JumpInstrNode(opcode));
    }

    public int emitConst(int index) {
        return emitNode(new InstructionUtils.ConstantInstrNode(InstructionUtils.OPCodes.Push, index));
    }

    public int emitCall(int callee, int argc) {
        return emitNode(new InstructionUtils.CallInstrNode(InstructionUtils.OPCodes.Call, callee, argc));
    }

    public int emitNode(InstrNode node) {
        int pc = pc();
//        if (isAlive()) {
            instructions.add(node);
            adjustStack(node.stackAdjustment());
//        }
        return pc;
    }


    public void markTreePos(Tree tree) {
        if (tree != null) {
            putPos(tree.pos);
        }
    }

    public void putPos(int pos) {
        int line = lineMap.getLineNumber(pos);

        // todo: line always have int type
        if (line > 0 && line < (1<<16) && line != this.cLineNum) {
            this.cpLineMap.put((short) pc(), line);
            this.cLineNum = line;
        }
    }

    private void adjustStack(int stackAdjustment) {
        tos(tos + stackAdjustment);
        Assert.check(tos >= 0, () ->
                String.format(
                        "negative tos: PC=%d, LINE=%d",
                        pc(), lineNum()));
    }

    public int tos() {
        return tos;
    }

    public void tos(int _tos) {
        tos = _tos;
        limTos = Math.max(limTos, _tos);
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public static class Callee {
        public int utf8Hash;
        public int utf8;

        public Callee(String utf8, int utf8Index) {
            this.utf8Hash = utf8.hashCode();
            this.utf8 = utf8Index;
        }

        @Override
        public int hashCode() {
            return utf8Hash;
        }

        @Override
        public boolean equals(Object o) {
            // o == this почти всегда false.
            return (o instanceof Callee) && ((Callee) o).utf8 == utf8;
        }
    }

    public int resolveCallee(String name) {
        return resolveConstant(new Callee(name, resolveConstant(name)));
    }

    public int resolveConstant(Object o) {
        return constantPool.computeIfAbsent(o, o1 -> {
            int d = constantPool.size();
            if (d >= MAX_CONSTANT_POOL_SIZE) {
                throw new JuaCompiler.CompileException("Constant pool overflow");
            }
            return d;
        });
    }

    public Object[] getConstantPoolEntries() {
        return constantPool.keySet().toArray();
    }

    public Module.Executable toExecutable() {
        return new Module.Executable(sym.name, gen.source.fileName,
                instructions.toArray(new InstrNode[0]),
                sym.nlocals,
                limTos,
                getConstantPoolEntries(),
                buildLineNumberTable(),
                sym.loargc,
                sym.hiargc,
                sym.defs,
                sym.params, sym.flags);
    }

    private LineNumberTable buildLineNumberTable() {
        int size = cpLineMap.size();
        short[] shortCodePoints = new short[size];
        int[] lineNumbers = new int[size];

        int i = 0;
        for (Map.Entry<Short, Integer> entry : cpLineMap.entrySet()) {
            shortCodePoints[i] = entry.getKey();
            lineNumbers[i] = entry.getValue();
            i++;
        }

        return new LineNumberTable(shortCodePoints, lineNumbers);
    }

    public void resolve(Chain chain) {
        resolve(chain, pc());
    }

    public void resolve(Chain chain, int destPC) {
        if (chain == null) return;
        tos(chain.tos);
        do {
            get(chain.pc).setOffset(destPC);
            chain = chain.next;
        } while (chain != null);
    }

    public void checkTosConvergence(int tos) {
        Assert.check(tos() == tos, () ->
                String.format(
                        "TOS violation: EXPECTED=%d, ACTUAL=%d, PC=%d, LINE=%d",
                        tos, tos(), pc(), lineNum()));
    }

}