package jua.compiler;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.shorts.Short2IntRBTreeMap;
import it.unimi.dsi.fastutil.shorts.Short2IntSortedMap;
import jua.compiler.Tree.Name;
import jua.interpreter.Address;
import jua.interpreter.instruction.Binaryswitch;
import jua.interpreter.instruction.Instruction;
import jua.interpreter.instruction.JumpInstruction;
import jua.runtime.code.LocalTable;
import jua.runtime.code.CodeSegment;
import jua.runtime.code.ConstantPool;
import jua.runtime.code.LineNumberTable;
import jua.runtime.heap.StringHeap;

import java.util.*;
import java.util.function.Supplier;

public final class Code {

    final List<Instruction> instructions = new ArrayList<>();

    final Short2IntSortedMap lineTable = new Short2IntRBTreeMap();

    final Object2IntMap<String> localNames = new Object2IntLinkedOpenHashMap<>();

    final ConstantPoolBuilder constant_pool_b = new ConstantPoolBuilder();

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

    public Code(Source source) {
        this.lineMap = source.getLineMap();
        log = source.getLog();
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
        // todo: Exception in thread "main" java.lang.AssertionError: 4
        //	at jua.compiler.Code.getJump(Code.java:110)
        //	at jua.compiler.MinorGen.resolveJump(MinorGen.java:1669)
        //	at jua.compiler.MinorGen.resolveJump(MinorGen.java:1665)
        //	at jua.compiler.MinorGen.genLoop(MinorGen.java:803)
        //	at jua.compiler.MinorGen.visitFor(MinorGen.java:325)
        //	at jua.compiler.Tree$ForLoop.accept(Tree.java:749)
        //	at jua.compiler.Tree$Scanner.scan(Tree.java:204)
        //	at jua.compiler.Tree$Scanner.visitBlock(Tree.java:231)
        //	at jua.compiler.Tree$Block.accept(Tree.java:683)
        //	at jua.compiler.MinorGen.genBranch(MinorGen.java:1678)
        //	at jua.compiler.MinorGen.generateBranch(MinorGen.java:1187)
        //	at jua.compiler.MinorGen.visitFuncDef(MinorGen.java:438)
        //	at jua.compiler.Tree$FuncDef.accept(Tree.java:667)
        //	at jua.compiler.ProgramLayout.lambda$buildProgram$10(ProgramLayout.java:98)
        //	at java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:193)
        //	at java.util.Spliterators$ArraySpliterator.forEachRemaining(Spliterators.java:948)
        //	at java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:482)
        //	at java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:472)
        //	at java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:546)
        //	at java.util.stream.AbstractPipeline.evaluateToArrayNode(AbstractPipeline.java:260)
        //	at java.util.stream.ReferencePipeline.toArray(ReferencePipeline.java:505)
        //	at jua.compiler.ProgramLayout.buildProgram(ProgramLayout.java:101)
        //	at jua.compiler.JuaCompiler.compileFile(JuaCompiler.java:28)
        //	at jua.Main.main(Main.java:29)
        //Caused by: java.lang.ClassCastException: jua.interpreter.instruction.Vinc cannot be cast to jua.interpreter.instruction.JumpInstruction
        //	at jua.compiler.Code.getJump(Code.java:108)
        //	... 23 more
        //
        if (true || isAlive()) {
            this.instructions.add(instruction);
            adjustStack(instruction.stackAdjustment());
        }
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

    public int resolveNull() { return constant_pool_b.putNull(); }
    public int resolveLong(long value) { return constant_pool_b.putLong(value); }
    public int resolveDouble(double value) { return constant_pool_b.putDouble(value); }
    public int resolveBoolean(boolean value) { return constant_pool_b.putBoolean(value); }
    public int resolveString(String value) { return constant_pool_b.putString(value); }

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
        return this.constant_pool_b.build();
    }

    @Deprecated
    ConstantPoolBuilder get_cpb() {
        return this.constant_pool_b;
    }

    private final Map<String, Integer> defaultPCIs = new HashMap<>();

    void setLocalDefaultPCI(Name name, int defaultPCI) {
        defaultPCIs.put(name.value, defaultPCI);
    }

    public static final class ConstantPoolBuilder {

        final Map<Object, Integer> indexMap = new HashMap<>();

        Address[] entries = new Address[1];

        private int putLenient(Object key, Supplier<Address> addressSupplier) {
            return indexMap.computeIfAbsent(key, _key -> {
                int newIdx = indexMap.size();
                if (newIdx >= entries.length) {
                    if (newIdx > ConstantPool.MAX_SIZE) {
                        throw new OutOfMemoryError(); // todo: Выбрасывать соответствующе исключение
                    }
                    entries = Arrays.copyOf(entries, entries.length << 1);
                }
                entries[newIdx] = addressSupplier.get();
                return newIdx;
            });
        }

        public int putNull() {
            return putLenient(null, () -> {
                Address a = new Address();
                a.setNull();
                return a;
            });
        }

        public int putLong(long value) {
            return putLenient(value, () -> {
                Address a = new Address();
                a.set(value);
                return a;
            });
        }

        public int putDouble(double value) {
            return putLenient(value, () -> {
                Address a = new Address();
                a.set(value);
                return a;
            });
        }

        public int putBoolean(boolean value) {
            return putLenient(value, () -> {
                Address a = new Address();
                a.set(value);
                return a;
            });
        }

        public int putString(String value) {
            return putLenient(value, () -> {
                Address a = new Address();
                a.set(new StringHeap(value));
                return a;
            });
        }

        // todo: putMap

        public ConstantPool build() {
            return new ConstantPool(entries);
        }
    }
}