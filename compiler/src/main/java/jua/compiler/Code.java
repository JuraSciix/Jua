package jua.compiler;

import jua.compiler.InstructionUtils.IndexedInstrNode;
import jua.compiler.InstructionUtils.InstrNode;
import jua.compiler.InstructionUtils.JumpInstrNode;
import jua.compiler.InstructionUtils.SingleInstrNode;
import jua.runtime.interpreter.memory.Address;
import jua.runtime.interpreter.memory.AddressUtils;
import jua.runtime.code.ConstantPool;
import jua.runtime.code.LineNumberTable;
import jua.runtime.utils.Assert;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

public final class Code {

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
    private final TreeMap<Short, Integer> lineTable = new TreeMap<>();

    private final LinkedHashMap<String, Integer> localNames = new LinkedHashMap<>();

    private final ConstantPoolWriter constantPoolWriter = new ConstantPoolWriter();

    private int nlocals = 0;

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
        if (isAlive()) {
            instructions.add(node);
            adjustStack(node.stackAdjustment());
        }
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
            this.lineTable.put((short) pc(), line);
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

    public boolean localExists(String name) {
        return this.localNames.containsKey(name);
    }

    public int resolveLocal(Name name) {
        return resolveLocal(name.toString());
    }

    public int resolveLocal(String name) {
//        if (!isAlive()) return -1;
        if (!localExists(name)) {
            int newIndex = this.nlocals++;
            this.localNames.put(name, newIndex);
            return newIndex;
        } else {
            return this.localNames.get(name);
        }
    }

    public ConstantPoolWriter constantPoolWriter() {
        return constantPoolWriter;
    }

    public int reqargs, totargs;
    public Object[] defs;


    public Executable toExecutable() {
        return new Executable(sym.name, gen.source.fileName,
                instructions.toArray(new InstrNode[0]),
                nlocals,
                limTos,
                buildConstantPool(),
                buildLineNumberTable(), reqargs, totargs, defs,
                localNames.keySet().toArray(new String[0]));
    }

    private LineNumberTable buildLineNumberTable() {
        int[] intCodePoints = lineTable.keySet().stream().mapToInt(a -> a).toArray();
        short[] shortCodePoints = new short[intCodePoints.length];
        for (int i = 0; i < intCodePoints.length; i++) {
            shortCodePoints[i] = (short) intCodePoints[i];
        }
        return new LineNumberTable(
                shortCodePoints,
                lineTable.values().stream().mapToInt(a->a).toArray()
        );
    }

    private ConstantPool buildConstantPool() {
        Object[] values = constantPoolWriter().toArray();
        Address[] addresses = AddressUtils.allocateMemory(values.length, 0);
        for (int i = 0; i < values.length; i++) {
            AddressUtils.assignObject(addresses[i], values[i]);
        }
        return new ConstantPool(addresses);
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