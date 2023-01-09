package jua.compiler;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.shorts.Short2IntRBTreeMap;
import it.unimi.dsi.fastutil.shorts.Short2IntSortedMap;
import jua.compiler.Tree.Name;
import jua.interpreter.Address;
import jua.interpreter.AddressUtils;
import jua.interpreter.instruction.Binaryswitch;
import jua.interpreter.instruction.Instruction;
import jua.interpreter.instruction.JumpInstruction;
import jua.runtime.code.LocalTable;
import jua.runtime.code.CodeSegment;
import jua.runtime.code.ConstantPool;
import jua.runtime.code.LineNumberTable;

import java.util.*;

public final class Code {

    final List<Instruction> instructions = new ArrayList<>();

    final Short2IntSortedMap lineTable = new Short2IntRBTreeMap();

    final Object2IntMap<String> localNames = new Object2IntLinkedOpenHashMap<>();

    final ConstantPoolWriter constantPoolWriter = new ConstantPoolWriter();

    int nstack = 0;

    int nlocals = 0;

    int current_nstack = 0;

    int current_lineNumber = 0;

    boolean alive = true;

    private final Deque<String> freeSyntheticNames = new ArrayDeque<>();
    private int syntheticLimit = 0;

    Log log;

    int lastLineNum() {
        return current_lineNumber;
    }

    String acquireSyntheticName() {
        if (freeSyntheticNames.isEmpty()) {
            String s = "s$" + syntheticLimit++;
            if (localNames.containsKey(s)) {
                log.error("synthetic member conflict: " + s);
            }
            return s;
        }
        return freeSyntheticNames.pollFirst();
    }

    void releaseSyntheticName(String name) {
        freeSyntheticNames.addFirst(name);
    }

    public final ProgramLayout programLayout;
    public final Lower lower;
    public final Flow flow;
    public final Check check;
    public final Gen gen;


    public Code(ProgramLayout programLayout, Source source) {
        this.programLayout = programLayout;
        this.lineMap = source.getLineMap();
        log = source.getLog();
        lower = new Lower(programLayout);
        flow = new Flow();
        check = new Check(programLayout, log);
        gen = new Gen(programLayout);
        gen.code = this;
        gen.log = log;
        gen.source = source;
    }

    Instruction get(int pc) {
        return instructions.get(pc);
    }

    JumpInstruction getJump(int pc) {
        try {
            return (JumpInstruction) instructions.get(pc);
        } catch (ClassCastException e) {
            throw new AssertionError(String.valueOf(pc), e);
        }
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

    private LineMap lineMap;

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
        return resolveLocal(name.value);
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

    public CodeSegment buildCodeSegment() {
        ConstantPool cp = buildConstantPool();
        return new CodeSegment(buildCode(cp),
                this.nstack,
                this.nlocals,
                cp,
                buildLineNumberTable(),
                buildLocalTable());
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

    private LocalTable buildLocalTable() {
        LocalTable.Local[] locals = new LocalTable.Local[localNames.size()];

        for (String name : localNames.keySet()) {
            int index = localNames.getInt(name);
            locals[index] = new LocalTable.Local(name, defaultPCIs.getOrDefault(name, -1));
        }

        return new LocalTable(locals);
    }

    private ConstantPool buildConstantPool() {
        Object[] values = constantPoolWriter().toArray();
        Address[] addresses = AddressUtils.allocateMemory(values.length, 0);
        for (int i = 0; i < values.length; i++) {
            AddressUtils.assignObject(addresses[i], values[i]);
        }
        return new ConstantPool(addresses);
    }

    private final Map<String, Integer> defaultPCIs = new HashMap<>();

    void setLocalDefaultPCI(Name name, int defaultPCI) {
        defaultPCIs.put(name.value, defaultPCI);
    }

    public void resolveJump(int opcodePC) {
        resolveJump(opcodePC, currentIP());
    }

    public void resolveJump(int opcodePC, int destPC) {
        getJump(opcodePC).offset = destPC - opcodePC;
    }

    public void resolveChain(IntArrayList opcodePCs) {
        resolveChain(opcodePCs, currentIP());
    }

    public void resolveChain(IntArrayList opcodePCs, int destPC) {
        opcodePCs.forEach((int opcodePC) -> resolveJump(opcodePC, destPC));
    }
}