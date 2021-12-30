package jua.compiler;

import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import jua.interpreter.Program;
import jua.interpreter.lang.*;
import jua.interpreter.states.JumpState;
import jua.interpreter.states.State;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import jua.interpreter.Program.LineTableEntry;

public final class Code {

    private static class Chain {

        static final int UNSATISFIED_DESTINATION = Integer.MIN_VALUE;

        final Int2ObjectMap<JumpState> states = new Int2ObjectOpenHashMap<>();

        int destination = UNSATISFIED_DESTINATION;
    }

    private static class Scope {

        boolean alive = true;
    }

    private static class Context {

        final String filename;

        final List<State> states = new ArrayList<>();

        final List<LineTableEntry> lineTable = new ArrayList<>();

        final List<String> locals = new ArrayList<>();

        final Int2ObjectMap<Chain> chainMap = new Int2ObjectOpenHashMap<>();

        final Deque<Scope> scopes = new ArrayDeque<>();

        final List<Operand> constantPool = new ArrayList<>();

        int stackTop = 0;

        int stackSize = 0;

        int lastLineNumber = 1;

        private Context(String filename) {
            this.filename = filename;
            lineTable.add(new LineTableEntry(1, 0));
        }
    }

    private final Deque<Context> contexts = new ArrayDeque<>();

    private final Long2ObjectMap<Operand> longConstants = new Long2ObjectOpenHashMap<>();

    private final Double2ObjectMap<Operand> doubleConstants = new Double2ObjectOpenHashMap<>();

    private final Map<String, Operand> stringConstants = new HashMap<>();

    public boolean empty() {
        return contexts.isEmpty();
    }

    public void enterContext(String filename) {
        contexts.addLast(new Context(filename));
    }

    public void exitContext() {
        Context context = contexts.removeLast();
        context.states.clear();
        context.lineTable.clear();
        context.locals.clear();
        context.chainMap.clear();
        context.scopes.clear();
    }

    public void enterScope() {
        currentContext().scopes.addLast(new Scope());
    }

    public void exitScope() {
        currentContext().scopes.removeLast();
    }

    public void deathScope() {
        currentScope().alive = false;
    }

    public boolean scopeAlive() {
        return currentScope().alive;
    }

    public void addState(State state) {
        addState(0, state);
    }

    public void addState(int line, State state) {
        if (isAlive()) {
            putLine(line);
            currentContext().states.add(state);
        }
    }

    private void putLine(int lineNumber) {
        Context context = currentContext();
        if (lineNumber > 0 &&
                lineNumber != context.lastLineNumber) {
            int startIp = context.states.size();
            context.lineTable.add(new Program.LineTableEntry(lineNumber, startIp));
            context.lastLineNumber = lineNumber;
        }
    }

    public int statesCount() {
        return currentContext().states.size();
    }

    public int createFlow() {
        if (!isAlive()) {
            return -1;
        }
        Map<Integer, Chain> map = currentChainMap();
        int id = map.size();
        map.put(id, new Chain());
        return id;
    }

    public void addFlow(int id, JumpState state) {
        addFlow(id, 0, state);
    }

    public void addFlow(int id, int line, JumpState state) {
        if (isAlive()) {
            Chain chain = currentChainMap().get(id);
            int bci = currentContext().states.size();
            if (chain.destination != Chain.UNSATISFIED_DESTINATION) {
                state.setDestination(chain.destination - bci);
            }
            chain.states.put(bci, state);
            addState(line, state);
        }
    }

    public void resolveFlow(int id) {
        if (isAlive()) {
            Chain chain = currentChainMap().get(id);
            int result = currentContext().states.size();
            chain.states.forEach((sBci, s) -> s.setDestination(result - sBci));
            chain.destination = result;
        }
    }

    public int getLocal(String name) {
        if (!isAlive()) {
            return -1;
        }
        List<String> locals = currentLocals();
        if (!locals.contains(name)) {
            locals.add(name);
        }
        return locals.indexOf(name);
    }

    public void incStack() {
        incStack(1);
    }

    public void incStack(int n) {
        if (isAlive()) {
            currentContext().stackSize += n;
            updateCurrentStack();
        }
    }

    public void decStack() {
        decStack(1);
    }

    public void decStack(int n) {
        if (isAlive()) {
            currentContext().stackSize -= n;
        }
    }

    public Program getBuilder() {
        Context context = currentContext();
        return new Program(context.filename,
                context.states.toArray(new State[0]),
                context.lineTable.toArray(new Program.LineTableEntry[0]),
                context.stackTop,
                context.locals.size(),
                context.locals.toArray(new String[0]), context.constantPool.toArray(new Operand[0]));
    }

    public int resolveLong(long value) {
        return resolve(longConstants.containsKey(value)
                        ? c -> c.isInt() && c.intValue() == value
                        : null,
                () -> IntOperand.valueOf(value),
                c -> longConstants.put(value, c));
    }

    public int resolveDouble(double value) {
        return resolve(doubleConstants.containsKey(value)
                        ? c -> c.isFloat() && Double.compare(value, c.floatValue()) == 0
                        : null,
                () -> FloatOperand.valueOf(value),
                c -> doubleConstants.put(value, c));
    }

    public int resolveString(String value) {
        return resolve(stringConstants.containsKey(value)
                        ? c -> c.isFloat() && value.equals(c.stringValue())
                        : null,
                () -> StringOperand.valueOf(value),
                c -> stringConstants.put(value, c));
    }

    private int resolve(Predicate<Operand> filter, Supplier<Operand> supplier, Consumer<Operand> cache) {
        List<Operand> constantPool = currentContext().constantPool;

        for (int i = 0; filter != null && i < constantPool.size(); i++) {
            Operand constant = constantPool.get(i);
            if (filter.test(constant)) {
                return i;
            }
        }
        Operand result = supplier.get();
        if (filter == null) {
            cache.accept(result);
        }
        constantPool.add(result);
        return constantPool.size() - 1;
    }

    private boolean isAlive() {
        return currentScope().alive;
    }

    private List<String> currentLocals() {
        return currentContext().locals;
    }

    private Map<Integer, Chain> currentChainMap() {
        return currentContext().chainMap;
    }

    private Scope currentScope() {
        return currentContext().scopes.getLast();
    }

    private void updateCurrentStack() {
        Context context = currentContext();
        if (context.stackSize > context.stackTop) {
            context.stackTop = context.stackSize;
        }
    }

    private Context currentContext() {
        return contexts.getLast();
    }
}