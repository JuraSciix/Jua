package jua.compiler;

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.shorts.*;
import jua.runtime.code.CodeSegment;
import jua.interpreter.instruction.JumpInstruction;
import jua.interpreter.instruction.Instruction;
import jua.runtime.code.ConstantPool;
import jua.runtime.code.LocalNameTable;
import jua.runtime.code.LineNumberTable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class Code {

    interface JumpInstructionConstructor {

        JumpInstruction create(int dest_ip);
    }

    private static class Chain {

        // Map<IP, ChainInstructionFactory>
        final Int2ObjectMap<JumpInstructionConstructor> constructors
                = new Int2ObjectOpenHashMap<>();

        int resultIP = -1;
    }

    final List<Instruction> instructions = new ArrayList<>();

    final Short2IntSortedMap lineTable = new Short2IntRBTreeMap();

    final Int2ObjectMap<Chain> chains = new Int2ObjectOpenHashMap<>();

    final Object2IntMap<String> localNames = new Object2IntLinkedOpenHashMap<>();

    final BooleanStack scopes = new BooleanArrayList();

    final ConstantPool.Builder constant_pool_b = new ConstantPool.Builder();

    int nstack = 0;

    int nlocals = 0;

    int current_nstack = 0;

    int current_lineNumber = 0;

    @Deprecated
    private Code context = this;

    public Code(Source source) {
        this.lineMap = source.getLineMap();
    }

    @Deprecated
    public void pushContext(int startPos) {
        putPos(startPos);
    }

    @Deprecated
    public void popContext() {

    }

    public int makeChain() {
        int nextChainId = context.chains.size();
        context.chains.put(nextChainId, new Chain());
        return nextChainId;
    }

    private void resolveChain0(int chainId, int resultIP) {
        context.chains.get(chainId).resultIP = resultIP;
    }

    public void resolveChain(int chainId) {
        resolveChain0(chainId, currentIP());
    }

    public void resolveChain(int chainId, int resultBci) {
        resolveChain0(chainId, resultBci);
    }

    public int getChainDest(int chainId) {
        return context.chains.get(chainId).resultIP;
    }

    public int currentIP() {
        return context.instructions.size();
    }

    public void addInstruction(Instruction instr) {
        addInstruction0(instr);
    }

    public void addChainedInstruction(JumpInstructionConstructor factory, int chainId) {
        addChainedInstruction0(factory, chainId);
    }

    private void addChainedInstruction0(JumpInstructionConstructor factory, int chainId) {
        if (!isAlive()) return;
        context.chains.get(chainId).constructors.put(currentIP(), factory);
        context.instructions.add(null); // will be installed later
        adjustStack(factory.create(0).stackAdjustment());
    }

    private void addInstruction0(Instruction instruction) {
        if (!isAlive()) return;
        context.instructions.add(instruction);
        adjustStack(instruction.stackAdjustment());
    }

    private LineMap lineMap;

    public void putPos(int pos) {
        int line = lineMap.getLineNumber(pos);

        // todo: line always have int type
        if (line > 0 && line < (1<<16) && line != context.current_lineNumber) {
            context.lineTable.put((short) currentIP(), (short) line);
            context.current_lineNumber = line;
        }
    }

    private void adjustStack(int stackAdjustment) {
        if ((context.current_nstack += stackAdjustment) > context.nstack)
            context.nstack = context.current_nstack;
        assert context.current_nstack >= 0 : "context.current_nstack < 0, " +
                "currentIP: " +  currentIP() + ", " +
                "lineNumber: " + context.current_lineNumber;
    }

    public int getSp() {
        return context.current_nstack;
    }

    public void setSp(int nstack) {
        // todo: Я не уверен, что замена nstack на current_nstack ничего плохого
        //  за собой не повлечет
        context.current_nstack = nstack;
        if (context.current_nstack > context.nstack)
            context.nstack = context.current_nstack;
    }

    private static final boolean SCOPE_ALIVE = true;
    private static final boolean SCOPE_DEAD = false;

    public void pushScope() {
        context.scopes.push(SCOPE_ALIVE);
    }

    public void popScope() {
        context.scopes.popBoolean();
    }

    public void dead() {
        if (isAlive()) {
            context.scopes.popBoolean();
            context.scopes.push(SCOPE_DEAD);
        }
    }

    public boolean isAlive() {
        return context.scopes.topBoolean() == SCOPE_ALIVE;
    }

    public boolean localExists(String name) {
        return context.localNames.containsKey(name);
    }

    public int resolveLocal(String name) {
//        if (!isAlive()) return -1;
        if (!localExists(name)) {
            int newIndex = context.nlocals++;
            context.localNames.put(name, newIndex);
            return newIndex;
        } else {
            return context.localNames.getInt(name);
        }
    }

    public int resolveLong(long value) { return context.constant_pool_b.putLongEntry(value); }
    public int resolveDouble(double value) { return context.constant_pool_b.putDoubleEntry(value); }
    public int resolveString(String value) { return context.constant_pool_b.putStringEntry(value); }

    public CodeSegment buildCodeSegment() {
        return new CodeSegment(buildCode(),
                context.nstack,
                context.nlocals,
                buildConstantPool(),
                buildLineTable(),
                new LocalNameTable(context.localNames));
    }

    private static final Instruction[] EMPTY_INSTRUCTIONS = new Instruction[0];
    private Instruction[] buildCode() {
        Instruction[] instructions = context.instructions.toArray(EMPTY_INSTRUCTIONS);
        for (Chain chain : context.chains.values()) {
            for (Int2ObjectMap.Entry<JumpInstructionConstructor> entry : chain.constructors.int2ObjectEntrySet()) {
                int ip = entry.getIntKey();
                instructions[ip] = entry.getValue().create(chain.resultIP - ip);
            }
        }
        return instructions;
    }

    private LineNumberTable buildLineTable() {
        return new LineNumberTable(
                context.lineTable.keySet().toShortArray(),
                context.lineTable.values().toIntArray()
        );
    }

    private ConstantPool buildConstantPool() {
        return context.constant_pool_b.build();
    }

    ConstantPool.Builder get_cpb() {
        return context.constant_pool_b;
    }

    private Types types;

    Types getTypes() {
        if (types == null) types = new Types(this);
        return types;
    }
}