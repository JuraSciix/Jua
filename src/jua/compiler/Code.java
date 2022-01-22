package jua.compiler;

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ShortMap;
import it.unimi.dsi.fastutil.shorts.Short2ShortRBTreeMap;
import jua.interpreter.Program;
import jua.interpreter.instructions.ChainInstruction;
import jua.interpreter.instructions.Instruction;
import jua.interpreter.runtime.DoubleOperand;
import jua.interpreter.runtime.LongOperand;
import jua.interpreter.runtime.Operand;
import jua.interpreter.runtime.StringOperand;
import jua.parser.Tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class Code {

    interface ChainInstructionFactory {

        ChainInstruction create(int dest_ip);
    }

    private static class Chain {

        // Map<IP, ChainInstructionFactory>
        final Int2ObjectMap<ChainInstructionFactory> factories
                = new Int2ObjectOpenHashMap<>();

        int resultIP = -1;
    }

    private static class Context {

        final Context prev;

        final List<Instruction> instructions = new ArrayList<>();

        final Short2ShortMap lineTable = new Short2ShortRBTreeMap();

        final Int2ObjectMap<Chain> chains = new Int2ObjectOpenHashMap<>();

        final Object2IntMap<String> localNames = new Object2IntLinkedOpenHashMap<>();

        final BooleanStack scopes = new BooleanArrayList();

        final Object2IntMap<Object> literals = new Object2IntOpenHashMap<>();

        int nstack = 0;

        int nlocals = 0;

        int current_nstack = 0;

        int current_lineNumber = 0;

        Context(Context prev) { this.prev = prev; }
    }

    private Context context;

    private final Map<Object, Operand> literals__ = new HashMap<>();

    private final String filename;

    public Code(String filename) {
        this.filename = filename;
    }

    public void pushContext(Tree.Position startPos) {
        context = new Context(context);
        putPos(startPos);
    }

    public void popContext() {
        context = context.prev;
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
        addInstruction0(instr, 0);
    }

    public void addInstruction(Instruction instr, int stackAdjustment) {
        addInstruction0(instr, stackAdjustment);
    }

    public void addChainedInstruction(ChainInstructionFactory factory, int chainId) {
        addChainedInstruction0(factory, chainId, 0);
    }

    public void addChainedInstruction(ChainInstructionFactory factory, int chainId, int stackAdjustment) {
        addChainedInstruction0(factory, chainId, stackAdjustment);
    }

    private void addChainedInstruction0(ChainInstructionFactory factory, int chainId, int stackAdjustment) {
        if (!isAlive()) return;
        context.chains.get(chainId).factories.put(currentIP(), factory);
        context.instructions.add(null); // will be installed later
        adjustStack(stackAdjustment);
    }

    private void addInstruction0(Instruction instruction, int stackAdjustment) {
        if (!isAlive()) return;
        context.instructions.add(instruction);
        adjustStack(stackAdjustment);
    }

    public void putPos(Tree.Position pos) {
        int line = pos.line;

        if (line != context.current_lineNumber) {
            context.lineTable.put((short) currentIP(), (short) line);
            context.current_lineNumber = line;
        }
    }

    private void adjustStack(int stackAdjustment) {
        if ((context.current_nstack += stackAdjustment) > context.nstack)
            context.nstack = context.current_nstack;
    }

    public int getSp() {
        return context.nstack;
    }

    public void setSp(int nstack) {
        context.nstack = nstack;
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

    public int resolveLocal(String name) {
//        if (!isAlive()) return -1;
        if (!context.localNames.containsKey(name)) {
            int newIndex = context.nlocals++;
            context.localNames.put(name, newIndex);
            return newIndex;
        } else {
            return context.localNames.getInt(name);
        }
    }

    public int resolveLong(long value) { return resolve(value, LongOperand::valueOf); }
    public int resolveDouble(double value) { return resolve(value, DoubleOperand::valueOf); }
    public int resolveString(String value) { return resolve(value, StringOperand::valueOf); }

    private <T> int resolve(T value, Function<T, Operand> operandMaker) {
        if (!literals__.containsKey(value))
            literals__.put(value, operandMaker.apply(value));
        if (!context.literals.containsKey(value)) {
            context.literals.put(
                    value,
                    context.literals.size()
            );
        }
        return context.literals.getInt(value);
    }

    private static final String[] EMPTY_STRINGS = new String[0];
    public Program toProgram() {
        return new Program(filename,
                buildInstructions(),
                buildLineTable(),
                context.nstack,
                context.nlocals,
                context.localNames.keySet().toArray(EMPTY_STRINGS),
                buildConstantPool());
    }

    private static final Instruction[] EMPTY_INSTRUCTIONS = new Instruction[0];
    private Instruction[] buildInstructions() {
        Instruction[] instructions = context.instructions.toArray(EMPTY_INSTRUCTIONS);
        for (Chain chain : context.chains.values()) {
            for (Int2ObjectMap.Entry<ChainInstructionFactory> entry : chain.factories.int2ObjectEntrySet()) {
                int ip = entry.getIntKey();
                instructions[ip] = entry.getValue().create(chain.resultIP - ip);
            }
        }
        return instructions;
    }

    private Program.LineNumberTable buildLineTable() {
        return new Program.LineNumberTable(
                context.lineTable.keySet().toShortArray(),
                context.lineTable.values().toShortArray()
        );
    }

    private Operand[] buildConstantPool() {
        Operand[] constantPool = new Operand[context.literals.size()];
        for (Object2IntMap.Entry<Object> entry : context.literals.object2IntEntrySet()) {
            constantPool[entry.getIntValue()] = literals__.get(entry.getKey());
        }
        return constantPool;
    }
}