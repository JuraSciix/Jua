package jua.compiler;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import jua.interpreter.instruction.*;

import java.util.Objects;

import static jua.compiler.InstructionFactory.*;

public class Items {

    private final Code code;

    private final StackItem stackItem = new StackItem();

    public Items(Code code) {
        this.code = Objects.requireNonNull(code);
    }

    abstract class Item {

        Item load() {
            throw new AssertionError(this);
        }

        void drop() {
            throw new AssertionError(this);
        }

        void duplicate() {
            throw new AssertionError(this);
        }

        void stash() {
            throw new AssertionError(this);
        }

        void store() {
            throw new AssertionError(this);
        }

        CondItem isTrue() {
            load();
            return new CondItem(code.addInstruction(new Ifz()));
        }

        CondItem isNull() {
            load();
            return new CondItem(code.addInstruction(new Ifnonnull()));
        }

        int constantIndex() {
            throw new AssertionError(this);
        }
    }

    /**
     * Динамическое значение.
     */
    class StackItem extends Item {

        @Override
        Item load() {
            return this;
        }

        @Override
        void drop() {
            load();
            code.addInstruction(pop);
        }

        @Override
        void duplicate() {
            code.addInstruction(dup);
        }

        @Override
        void stash() {
            code.addInstruction(dup);
        }
    }

    /**
     * Литерал.
     */
    class LiteralItem extends Item {

        final Types.Type type;

        LiteralItem(Types.Type type) {
            this.type = type;
        }

        @Override
        Item load() {
            if (type.isBoolean()) {
                if (type.booleanValue()) {
                    code.addInstruction(const_true);
                } else {
                    code.addInstruction(const_false);
                }
            } else if (type.isNull()) {
                code.addInstruction(const_null);
            } else {
                if (type.isLong()) {
                    long lv = type.longValue();
                    if (-32768L <= lv && lv <= 32767L) {
                        code.addInstruction(new Push((short) lv));
                        return stackItem;
                    }
                }
                code.addInstruction(new Ldc(type.resolvePoolConstant(code)));
            }
            return stackItem;
        }

        @Override
        void drop() { /* no-op */ }

        @Override
        int constantIndex() {
            return type.resolvePoolConstant(code);
        }
    }

    /**
     * Обращение к локальной переменной.
     */
    class LocalItem extends Item {

        final int index;
        @Deprecated
        final boolean definitelyExists;

        LocalItem(int index, boolean definitelyExists) {
            this.index = index;
            this.definitelyExists = definitelyExists;
        }

        @Override
        Item load() {
            code.addInstruction(new Vload(index));
            return stackItem;
        }

        @Override
        void drop() {
            load();
            code.addInstruction(pop);
        }

        @Override
        void duplicate() { /* no-op */ }

        @Override
        void store() {
            code.addInstruction(new Vstore(index));
        }

        @Override
        void stash() {
            code.addInstruction(dup);
        }

        void inc() {
            code.addInstruction(new Vinc(index));
        }

        void dec() {
            code.addInstruction(new Vdec(index));
        }
    }

    /**
     * Обращение к элементу массива.
     */
    class AccessItem extends Item {

        @Override
        Item load() {
            code.addInstruction(aload);
            return stackItem;
        }

        @Override
        void store() {
            code.addInstruction(astore);
        }

        @Override
        void drop() {
            code.addInstruction(pop2);
        }

        @Override
        void duplicate() {
            code.addInstruction(dup2);
        }

        @Override
        void stash() {
            code.addInstruction(dup_x2);
        }
    }

    /**
     * Присвоение.
     */
    class AssignItem extends Item {

        final Item var;

        AssignItem(Item var) {
            this.var = var;
        }

        @Override
        Item load() {
            var.stash();
            var.store();
            return stackItem;
        }

        @Override
        void drop() {
            var.store();
        }

        @Override
        void stash() {
            var.stash();
        }
    }

    /**
     * Условное разветвление.
     */
    class CondItem extends Item {

        final int opcodePC;
        final IntArrayList truejumps;
        final IntArrayList falsejumps;

        CondItem(int opcodePC) {
            this(opcodePC, new IntArrayList(), new IntArrayList());
        }

        CondItem(int opcodePC, IntArrayList truejumps, IntArrayList falsejumps) {
            this.opcodePC = opcodePC;
            this.truejumps = truejumps;
            this.falsejumps = falsejumps;

            falsejumps.add(0, opcodePC);
        }

        @Override
        Item load() {
            resolveTrueJumps();
            code.addInstruction(const_true);
            int skipPC = code.addInstruction(new Goto());
            resolveFalseJumps();
            code.addInstruction(const_false);
            code.resolveJump(skipPC);
            return stackItem;
        }

        @Override
        void drop() {
            load();
            code.addInstruction(pop);
        }

        @Override
        CondItem isTrue() {
            return this;
        }

        CondItem negate() {
            code.setInstruction(opcodePC, code.getJump(opcodePC).negate());
            CondItem condItem = new CondItem(opcodePC, falsejumps, truejumps);
            condItem.truejumps.removeInt(0);
            return condItem;
        }

        void resolveTrueJumps() {
            resolveTrueJumps(code.currentIP());
        }

        void resolveTrueJumps(int pc) {
            code.resolveChain(truejumps, pc);
        }

        void resolveFalseJumps() {
            resolveFalseJumps(code.currentIP());
        }

        void resolveFalseJumps(int pc) {
            code.resolveChain(falsejumps, pc);
        }
    }

    class CallItem extends Item {

        final int index, nargs;
        @Deprecated
        final boolean safe;

        CallItem(int index, int nargs, boolean safe) {
            this.index = index;
            this.nargs = nargs;
            this.safe = safe;
        }

        @Override
        Item load() {
            code.addInstruction(new Call((short) index, (byte) nargs));
            return stackItem;
        }

        @Override
        void drop() {
//            code.addInstruction(safe ? new CallPopq((short) index, (byte) nargs) :
//                    new CallPop((short) index, (byte) nargs));
            load().drop();
        }
    }

    /**
     * Регистр для временного хранения некоторых данных
     */
    class TempItem extends Item {

        final String name = code.acquireSyntheticName();

        final int index = code.resolveLocal(name);

        @Override
        Item load() {
            code.addInstruction(new Vload(index));
            drop();
            return stackItem;
        }

        @Override
        void store() {
            code.addInstruction(new Vstore(index));
        }

        @Override
        void drop() {
            // Очищаем регистр чтобы не было утечек памяти
            code.addInstruction(const_null);
            store();
            code.releaseSyntheticName(name);
        }
    }

    StackItem makeStack() {
        return stackItem;
    }

    LiteralItem makeLiteral(Types.Type type) {
        return new LiteralItem(type);
    }

    LocalItem makeLocal(int index, boolean definitelyExists) {
        return new LocalItem(index, definitelyExists);
    }

    AccessItem makeAccess() {
        return new AccessItem();
    }

    AssignItem makeAssign(Item var) {
        return new AssignItem(var);
    }

    CondItem makeCond(int opcodePC) {
        return new CondItem(opcodePC);
    }

    CondItem makeCond(int opcodePC, IntArrayList truejumps, IntArrayList falsejumps) {
        return new CondItem(opcodePC, truejumps, falsejumps);
    }

    CallItem makeCall(int index, int nargs, boolean safe) {
        return new CallItem(index, nargs, safe);
    }

    TempItem makeTemp() {
        return new TempItem();
    }
}
