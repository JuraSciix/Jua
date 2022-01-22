package jua.compiler;

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanStack;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.shorts.Short2ShortMap;
import it.unimi.dsi.fastutil.shorts.Short2ShortRBTreeMap;
import jua.interpreter.Program;
import jua.interpreter.Program.LineNumberTable;
import jua.interpreter.instructions.ChainInstruction;
import jua.interpreter.instructions.Instruction;
import jua.interpreter.runtime.DoubleOperand;
import jua.interpreter.runtime.LongOperand;
import jua.interpreter.runtime.Operand;
import jua.interpreter.runtime.StringOperand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class Code {

    private static class Context {

        final String sourceName;

        final Context prev;

        final List<Instruction> instructions = new ArrayList<>();

        final Short2ShortMap lineTable = new Short2ShortRBTreeMap();

        final Int2ObjectMap<Chain> chains = new Int2ObjectOpenHashMap<>();

        final Object2IntMap<String> localNames = new Object2IntLinkedOpenHashMap<>();

        /**
         * aliveState[]
         */
        final BooleanStack scopes = new BooleanArrayList();

        // todo: Оптимизировать пул констант.
        final List<Operand> constantPool = new ArrayList<>();

        int nstack = 0;

        int maxNstack = 0;

        int nlocals = 0;

        int lastLineNumber;

        Context(String sourceName, Context prev) {
            this.sourceName = sourceName;
            this.prev = prev;
        }
    }

    private static class Chain {

        // Map<BCI, State>
        final Int2ObjectMap<ChainInstruction> states = new Int2ObjectOpenHashMap<>();

        int resultBci = -1;
    }

    private final Long2ObjectMap<Operand> longConstants;

    private final Double2ObjectMap<Operand> doubleConstants;

    private final Map<String, Operand> stringConstants;

    /**
     * Current context.
     */
    private Context context;

    public Code() {
        longConstants = new Long2ObjectOpenHashMap<>();
        doubleConstants = new Double2ObjectOpenHashMap<>();
        stringConstants = new HashMap<>();
    }

    public void pushContext(String sourceName) {
        context = new Context(sourceName, context);
    }

    public void popContext() {
        context = context.prev;
    }

    public void pushScope() {
        context.scopes.push(true);
    }

    public void popScope() {
        context.scopes.popBoolean();
    }

    public int makeChain() {
        int newChainId = context.chains.size();
        context.chains.put(newChainId, new Chain());
        return newChainId;
    }

    public void resolveChain(int chainId) {
        resolveChain0(chainId, currentBci());
    }

    public void resolveChain(int chainId, int resultBci) {
        resolveChain0(chainId, resultBci);
    }

    private void resolveChain0(int chainId, int resultBci) {
        if (!isAlive()) return;
        Chain chain = context.chains.get(chainId);
        for (Int2ObjectMap.Entry<ChainInstruction> entry : chain.states.int2ObjectEntrySet()) {
            entry.getValue().setDestination(resultBci - entry.getIntKey());
        }
        chain.resultBci = resultBci;
    }

    public void addState(Instruction instruction) {
        addState0(instruction, 0, 0);
    }

    public void addState(int line, Instruction instruction) {
        addState0(instruction, 0, line);
    }

    public void addState(Instruction instruction, int stackAdjustment) {
        addState0(instruction, stackAdjustment, 0);
    }

    public void addState(int line, Instruction instruction, int stackAdjustment) {
        addState0(instruction, stackAdjustment, line);
    }

    public void addChainedState(ChainInstruction state, int chainId) {
        addChainedState0(state, chainId, 0, 0);
    }

    public void addChainedState(int line, ChainInstruction state, int chainId) {
        addChainedState0(state, chainId, 0, line);
    }

    public void addChainedState(int line, ChainInstruction state, int chainId, int stackAdjustment) {
        addChainedState0(state, chainId, stackAdjustment, line);
    }

    public void addChainedState(ChainInstruction state, int chainId, int stackAdjustment) {
        addChainedState0(state, chainId, stackAdjustment, 0);
    }

    private void addChainedState0(ChainInstruction state, int chainId, int stackAdjustment, int line) {
        if (!isAlive()) return;
        Chain chain = context.chains.get(chainId);
        if (chain.resultBci != -1) {
            state.setDestination(chain.resultBci - currentBci());
        }
        chain.states.put(currentBci(), state);
        addState0(state, stackAdjustment, line);
    }

    public int getSp() {
        return context.nstack;
    }

    public void setSp(int nstack) {
        context.nstack = nstack;
    }

    private void addState0(Instruction instruction, int stackAdjustment, int line) {
        if (!isAlive()) return;
        context.instructions.add(instruction);
        context.nstack += stackAdjustment;
        if (context.nstack > context.maxNstack)
            context.maxNstack = context.nstack;
        if ((line > 0) && (context.lastLineNumber != line)) {
            putLine(currentBci() - 1, line);
        }
    }

    public void putLine(int bci, int line) {
        context.lineTable.put((short) bci, (short) line);
        context.lastLineNumber = line;
    }

    public int currentBci() {
        return context.instructions.size();
    }

    public boolean isAlive() {
        return context.scopes.topBoolean();
    }

    public void dead() {
        if (context.scopes.topBoolean()) {
            context.scopes.popBoolean();
            context.scopes.push(false);
        }
    }

    public int resolveLocal(String name) {
        if (!isAlive()) return -1;
        if (!context.localNames.containsKey(name)) {
            int newIndex = context.nlocals++;
            context.localNames.put(name, newIndex);
            return newIndex;
        } else {
            return context.localNames.getInt(name);
        }
    }

    public int resolveConstant(long value) {
        return resolveConstant0(
                constant -> constant.isLong() && constant.longValue() == value,
                () -> {
                    if (!longConstants.containsKey(value)) {
                        longConstants.put(value, LongOperand.valueOf(value));
                    }
                    return longConstants.get(value);
                });
    }

    public int resolveConstant(double value) {
        return resolveConstant0(
                constant -> constant.isDouble() && Double.compare(constant.doubleValue(), value) == 0,
                () -> {
                    if (!doubleConstants.containsKey(value)) {
                        doubleConstants.put(value, DoubleOperand.valueOf(value));
                    }
                    return doubleConstants.get(value);
                });
    }

    public int resolveConstant(String value) {
        return resolveConstant0(
                constant -> constant.isString() && constant.stringValue().equals(value),
                () -> {
                    if (!stringConstants.containsKey(value)) {
                        stringConstants.put(value, StringOperand.valueOf(value));
                    }
                    return stringConstants.get(value);
                });
    }

    private int resolveConstant0(Predicate<Operand> filter, Supplier<Operand> producer) {
        // Во время обработки опциональных аргументов функции, isAlive() почему-то возвращает false.
        // И мне лень разбираться, почему
//        if (!isAlive()) return -1;
        for (int i = 0; i < context.constantPool.size(); i++) {
            if (filter.test(context.constantPool.get(i))) {
                return i;
            }
        }
        context.constantPool.add(producer.get());
        return context.constantPool.size() - 1;
    }

    public Program toProgram() {
        return new Program(context.sourceName,
                context.instructions.toArray(new Instruction[0]),
                buildLineTable(),
                context.maxNstack,
                context.nlocals,
                context.localNames.keySet().toArray(new String[0]),
                context.constantPool.toArray(new Operand[0]));
    }

    private LineNumberTable buildLineTable() {
        return new LineNumberTable(
                context.lineTable.keySet().toShortArray(),
                context.lineTable.values().toShortArray()
        );
    }
}