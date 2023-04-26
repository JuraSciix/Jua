package jua.compiler;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2IntRBTreeMap;
import jua.interpreter.Address;
import jua.interpreter.AddressUtils;
import jua.interpreter.instruction.Binaryswitch;
import jua.interpreter.instruction.Instruction;
import jua.runtime.code.CodeData;
import jua.runtime.code.ConstantPool;
import jua.runtime.code.LineNumberTable;

import java.util.ArrayDeque;
import java.util.ArrayList;

public final class Code {

    static class Chain {

        final int pc;

        final Chain next;

        final int savedStackTop;

        Chain(int pc, Chain next, int savedStackTop) {
            this.pc = pc;
            this.next = next;
            this.savedStackTop = savedStackTop;
        }
    }

    static Chain mergeChains(Chain lhs, Chain rhs) {
        if (lhs == null) return rhs;
        if (rhs == null) return lhs;
        // Рекурсивная сортировка
        if (lhs.pc <= rhs.pc)
            return new Chain(lhs.pc, mergeChains(lhs.next, rhs), lhs.savedStackTop);
        else  // lhs.pc > rhs.pc
            return new Chain(rhs.pc, mergeChains(rhs.next, lhs), rhs.savedStackTop);
    }

    private final ArrayList<Instruction> instructions = new ArrayList<>();

    private final Short2IntRBTreeMap lineTable = new Short2IntRBTreeMap();

    private final Object2IntLinkedOpenHashMap<String> localNames = new Object2IntLinkedOpenHashMap<>();

    private final ConstantPoolWriter constantPoolWriter = new ConstantPoolWriter();

    private int nstack = 0;

    private int nlocals = 0;

    private int current_nstack = 0;

    private int current_lineNumber = 0;

    private boolean alive = true;

    private final ArrayDeque<String> freeSyntheticNames = new ArrayDeque<>();
    private int syntheticLimit = 0;

    public final ProgramScope programScope;
    public final Gen gen;

    private LineMap lineMap;

    public Code(ProgramScope programScope, Source source) {
        this.programScope = programScope;
        this.lineMap = source.getLineMap();
        gen = new Gen();
        gen.code = this;
        gen.source = source;
    }

    public Chain branch(Instruction instr) {
        return new Chain(addInstruction(instr), null, curStackTop());
    }

    public int lastLineNum() {
        return current_lineNumber;
    }

    public String acquireSyntheticName() {
        if (freeSyntheticNames.isEmpty()) {
            String s = "s$" + syntheticLimit++;
            while (localNames.containsKey(s)) {
                s += "_";
            }
            return s;
        }
        return freeSyntheticNames.pollFirst();
    }

    public void releaseSyntheticName(String name) {
        freeSyntheticNames.addFirst(name);
    }

    public Instruction get(int pc) {
        return instructions.get(pc);
    }

    public int currentIP() {
        return this.instructions.size();
    }

    public void setInstruction(int cp, Instruction instruction) {
        instructions.set(cp, instruction);
    }

    public int addInstruction(Instruction instr) {
        return addInstruction0(instr);
    }

    private int addInstruction0(Instruction instruction) {
        int pc = instructions.size();
        this.instructions.add(instruction);
        adjustStack(instruction.stackAdjustment());
        return pc;
    }

    public void position(Tree tree) {
        if (tree != null) {
            putPos(tree.pos);
        }
    }

    public void putPos(int pos) {
        int line = lineMap.getLineNumber(pos);

        // todo: line always have int type
        if (line > 0 && line < (1<<16) && line != this.current_lineNumber) {
            this.lineTable.put((short) currentIP(), (short) line);
            this.current_lineNumber = line;
        }
    }

    private void adjustStack(int stackAdjustment) {
        if ((this.current_nstack += stackAdjustment) > this.nstack)
            this.nstack = this.current_nstack;
        assert this.current_nstack >= 0 : "context.current_nstack < 0, " +
                "currentIP: " +  currentIP() + ", " +
                "lineNumber: " + this.current_lineNumber;
    }

    public int curStackTop() {
        return this.current_nstack;
    }

    public void curStackTop(int nstack) {
        // todo: Я не уверен, что замена nstack на current_nstack ничего плохого
        //  за собой не повлечет
        this.current_nstack = nstack;
        if (this.current_nstack > this.nstack)
            this.nstack = this.current_nstack;
    }

    public void dead() {
        alive = false;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive() {
        alive = true;
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
            return this.localNames.getInt(name);
        }
    }

    public ConstantPoolWriter constantPoolWriter() {
        return constantPoolWriter;
    }

    public CodeData buildCodeSegment() {
        ConstantPool cp = buildConstantPool();
        return new CodeData(
                this.nstack,
                this.nlocals,
                buildCode(cp),
                cp,
                buildLineNumberTable());
    }

    private static final Instruction[] EMPTY_INSTRUCTIONS = new Instruction[0];
    private Instruction[] buildCode(ConstantPool cp) {
        Instruction[] instructions = this.instructions.toArray(EMPTY_INSTRUCTIONS);
        for (Instruction instruction : instructions) {
            if (instruction.getClass() == Binaryswitch.class) {
                Binaryswitch switch_ = (Binaryswitch) instruction;
                switch_.sort(cp);
            }
        }
        return instructions;
    }

    private LineNumberTable buildLineNumberTable() {
        return new LineNumberTable(
                lineTable.keySet().toShortArray(),
                lineTable.values().toIntArray()
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
        resolve(chain, currentIP());
    }

    public void resolve(Chain chain, int destPC) {
        while (chain != null) {
            curStackTop(chain.savedStackTop);
            get(chain.pc).setOffset(destPC - chain.pc);
            chain = chain.next;
        }
    }
}