package jua.compiler;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.shorts.Short2IntRBTreeMap;
import it.unimi.dsi.fastutil.shorts.Short2IntSortedMap;
import jua.interpreter.instruction.Instruction;
import jua.interpreter.instruction.JumpInstruction;
import jua.runtime.code.CodeSegment;
import jua.runtime.code.ConstantPool;
import jua.runtime.code.LineNumberTable;
import jua.runtime.code.LocalNameTable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class Code {

    @Deprecated
    interface JumpInstructionConstructor {

        JumpInstruction create(int dest_ip);
    }

    @Deprecated
    private static class Chain {

        // Map<IP, ChainInstructionFactory>
        final Int2ObjectMap<JumpInstructionConstructor> constructors
                = new Int2ObjectOpenHashMap<>();

        int resultIP = -1;
    }

    final List<Instruction> instructions = new ArrayList<>();

    final Short2IntSortedMap lineTable = new Short2IntRBTreeMap();

    @Deprecated
    final Int2ObjectMap<Chain> chains = new Int2ObjectOpenHashMap<>();

    final Object2IntMap<String> localNames = new Object2IntLinkedOpenHashMap<>();

    final ConstantPool.Builder constant_pool_b = new ConstantPool.Builder();

    int nstack = 0;

    int nlocals = 0;

    int current_nstack = 0;

    int current_lineNumber = 0;

    boolean alive = true;

    private final Deque<String> freeSyntheticNames = new ArrayDeque<>();
    private int syntheticLimit = 0;

    Log log;

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

    @Deprecated
    public void pushContext(int startPos) {
        putPos(startPos);
    }

    @Deprecated
    public void popContext() {

    }

    @Deprecated
    public int makeChain() {
        int nextChainId = this.chains.size();
        this.chains.put(nextChainId, new Chain());
        return nextChainId;
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

    private void resolveChain0(int chainId, int resultIP) {
        this.chains.get(chainId).resultIP = resultIP;
    }

    public void resolveChain(int chainId) {
        resolveChain0(chainId, currentIP());
    }

    public void resolveChain(int chainId, int resultBci) {
        resolveChain0(chainId, resultBci);
    }

    public int getChainDest(int chainId) {
        return this.chains.get(chainId).resultIP;
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

    @Deprecated
    public void addChainedInstruction(JumpInstructionConstructor factory, int chainId) {
        addChainedInstruction0(factory, chainId);
    }

    private void addChainedInstruction0(JumpInstructionConstructor factory, int chainId) {
        if (!isAlive()) return;
        this.chains.get(chainId).constructors.put(currentIP(), factory);
        this.instructions.add(null); // will be installed later
        adjustStack(factory.create(0).stackAdjustment());
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

    @Deprecated
    public void pushState() {

    }

    @Deprecated
    public void popState() {

    }

    public void dead() {
        alive = false;
    }

    public boolean isAlive() {
        return alive;
    }

    public boolean localExists(String name) {
        return this.localNames.containsKey(name);
    }

    public int resolveLocal(Tree.Name name) {
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

    public int resolveLong(long value) { return this.constant_pool_b.putLongEntry(value); }
    public int resolveDouble(double value) { return this.constant_pool_b.putDoubleEntry(value); }
    public int resolveString(String value) { return this.constant_pool_b.putStringEntry(value); }

    public CodeSegment buildCodeSegment() {
        return new CodeSegment(buildCode(),
                this.nstack,
                this.nlocals,
                buildConstantPool(),
                buildLineTable(),
                new LocalNameTable(this.localNames));
    }

    private static final Instruction[] EMPTY_INSTRUCTIONS = new Instruction[0];
    private Instruction[] buildCode() {
        Instruction[] instructions = this.instructions.toArray(EMPTY_INSTRUCTIONS);
        for (Chain chain : this.chains.values()) {
            for (Int2ObjectMap.Entry<JumpInstructionConstructor> entry : chain.constructors.int2ObjectEntrySet()) {
                int ip = entry.getIntKey();
                instructions[ip] = entry.getValue().create(chain.resultIP - ip);
            }
        }
        return instructions;
    }

    private LineNumberTable buildLineTable() {
        return new LineNumberTable(
                this.lineTable.keySet().toShortArray(),
                this.lineTable.values().toIntArray()
        );
    }

    private ConstantPool buildConstantPool() {
        return this.constant_pool_b.build();
    }

    ConstantPool.Builder get_cpb() {
        return this.constant_pool_b;
    }

    private Types types;

    Types getTypes() {
        if (types == null) types = new Types(this);
        return types;
    }
}