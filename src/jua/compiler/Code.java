package jua.compiler;

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import jua.interpreter.Program;
import jua.interpreter.Program.LineTableEntry;
import jua.interpreter.lang.FloatOperand;
import jua.interpreter.lang.IntOperand;
import jua.interpreter.lang.Operand;
import jua.interpreter.lang.StringOperand;
import jua.interpreter.states.JumpState;
import jua.interpreter.states.State;
import jua.tools.ListDequeUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class Code {

    private static class Context {

        final String sourceName;

        final Context prev;

        final List<State> states = new ArrayList<>();

        final Int2IntMap lineTable = new Int2IntLinkedOpenHashMap();

        final Int2ObjectMap<Chain> chains = new Int2ObjectOpenHashMap<>();

        final Object2IntMap<String> localNames = new Object2IntLinkedOpenHashMap<>();

        final BooleanList scopes = new BooleanArrayList();

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
        final Int2ObjectMap<JumpState> states = new Int2ObjectOpenHashMap<>();

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
        ListDequeUtils.addLastBoolean(context.scopes, true);
    }

    public void popScope() {
        ListDequeUtils.removeLastBoolean(context.scopes);
    }

    public int makeChain() {
        int newChainId = context.chains.size();
//        System.err.printf("makeChain() (currentBci: %d, newChainId: %d)%n", currentBci(), newChainId);
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
        Chain chain = context.chains.get(chainId);
//        System.err.printf("resolveChain0(%d, %d) (currentBci: %d, resultBci: %d, alive: %b)%n",
//                chainId, resultBci, currentBci(), chain.resultBci, isAlive());

        if (!isAlive()) return;
        for (Int2ObjectMap.Entry<JumpState> entry : chain.states.int2ObjectEntrySet()) {
            entry.getValue().setDestination(resultBci - entry.getIntKey());
        }
        chain.resultBci = resultBci;
    }

    public void addState(State state) {
        addState0(state, 0, 0);
    }

    public void addState(int line, State state) {
        addState0(state, 0, line);
    }

    public void addState(State state, int stackAdjustment) {
        addState0(state, stackAdjustment, 0);
    }

    public void addState(int line, State state, int stackAdjustment) {
        addState0(state, stackAdjustment, line);
    }

    public void addChainedState(JumpState state, int chainId) {
        addChainedState0(state, chainId, 0, 0);
    }

    public void addChainedState(int line, JumpState state, int chainId) {
        addChainedState0(state, chainId, 0, line);
    }

    public void addChainedState(int line, JumpState state, int chainId, int stackAdjustment) {
        addChainedState0(state, chainId, stackAdjustment, line);
    }

    public void addChainedState(JumpState state, int chainId, int stackAdjustment) {
        addChainedState0(state, chainId, stackAdjustment, 0);
    }

    private void addChainedState0(JumpState state, int chainId, int stackAdjustment, int line) {
        Chain chain = context.chains.get(chainId);
//        System.err.printf("addChainedState(%s, %d, %d, %d) (currentBci: %d, resultBci: %d, alive: %b)%n",
//                state.getClass().getName(), chainId, stackAdjustment, line, currentBci(), chain.resultBci, isAlive());
        if (!isAlive()) return;
        if (chain.resultBci != -1) {
            state.setDestination(chain.resultBci - currentBci());
        }
        chain.states.put(currentBci(), state);
        addState0(state, stackAdjustment, line);
    }

    private void addState0(State state, int stackAdjustment, int line) {
//        System.err.printf("addState0(%s, %d, %d) (nstack: %d, maxNstack: %d, scopes: %s)%n",
//                state.getClass().getName(), stackAdjustment, line,
//                context.nstack, context.maxNstack, context.scopes);

        if (!isAlive()) return;
        context.states.add(state);
        context.nstack += stackAdjustment;
        if (context.nstack > context.maxNstack)
            context.maxNstack = context.nstack;
        if ((line > 0) && (context.lastLineNumber != line)) {
            putLine(currentBci(), line);
        }
    }

    public void putLine(int bci, int line) {
        context.lineTable.put(bci - 1, line);
        context.lastLineNumber = line;
    }

    public int currentBci() {
        return context.states.size();
    }

    public boolean isAlive() {
        return ListDequeUtils.peekLastBoolean(context.scopes);
    }

    public void dead() {
        ListDequeUtils.setLastBoolean(context.scopes, false);
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
                constant -> constant.isInt() && constant.intValue() == value,
                () -> {
                    if (!longConstants.containsKey(value)) {
                        longConstants.put(value, IntOperand.valueOf(value));
                    }
                    return longConstants.get(value);
                });
    }

    public int resolveConstant(double value) {
        return resolveConstant0(
                constant -> constant.isFloat() && Double.compare(constant.floatValue(), value) == 0,
                () -> {
                    if (!doubleConstants.containsKey(value)) {
                        doubleConstants.put(value, FloatOperand.valueOf(value));
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
        if (!isAlive()) return -1;
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
                context.states.toArray(new State[0]),
                buildLineTable(),
                context.maxNstack,
                context.nlocals,
                context.localNames.keySet().toArray(new String[0]),
                context.constantPool.toArray(new Operand[0]));
    }

    private LineTableEntry[] buildLineTable() {
        return context.lineTable.int2IntEntrySet().stream()
                .map(entry -> new LineTableEntry(entry.getIntKey(), entry.getIntValue()))
                // Вместо этого используется LinkedHashMap
//                .sorted(Comparator.comparingInt(entry -> entry.startBci))
                .toArray(LineTableEntry[]::new);
    }
}