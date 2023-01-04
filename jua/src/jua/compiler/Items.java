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

        void store() {
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

        CondItem toCond() {
            load();
            return new CondItem(code.addInstruction(new Ifz()));
        }

        CondItem nonNull() {
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
                    if ((lv >>> 16) == 0L) {
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

        final int pos;
        final Tree.Name name;
        final boolean definitelyExists;

        LocalItem(int pos, Tree.Name name, boolean definitelyExists) {
            this.pos = pos;
            this.name = name;
            this.definitelyExists = definitelyExists;
        }

        @Override
        Item load() {
            code.putPos(pos);
            int id = code.resolveLocal(name.value);
            code.addInstruction(definitelyExists ? new Vloadq(id) : new Vload(id));
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
            code.addInstruction(new Vstore(code.resolveLocal(name.value)));
        }

        @Override
        void stash() {
            code.addInstruction(dup);
        }

        void inc() {
            int id = code.resolveLocal(name);
            code.addInstruction(definitelyExists ? new Vincq(id) : new Vinc(id));
        }

        void dec() {
            int id = code.resolveLocal(name);
            code.addInstruction(definitelyExists ? new Vdecq(id) : new Vdec(id));
        }
    }

    /**
     * Обращение к элементу массива.
     */
    class AccessItem extends Item {

        final int pos;

        AccessItem(int pos) {
            this.pos = pos;
        }

        @Override
        Item load() {
            code.putPos(pos);
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

        final int pos;
        final Item var;

        AssignItem(int pos, Item var) {
            this.pos = pos;
            this.var = var;
        }

        @Override
        Item load() {
            var.stash();
            code.putPos(pos);
            var.store();
            return stackItem;
        }

        @Override
        void drop() {
            code.putPos(pos);
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
        CondItem toCond() {
            return this;
        }

        CondItem negate() {
            if (Code.USE_KOSTYL && opcodePC == -1) return this; // todo: исправить костыль
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

    /**
     * Регистр для временного хранения некоторых данных
     */
    class TempItem extends Item {

        final String name = code.acquireSyntheticName();

        final int index = code.resolveLocal(name);

        @Override
        Item load() {
            code.addInstruction(new Vloadq(index));
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

    LocalItem makeLocal(int pos, Tree.Name name, boolean definitelyExists) { // todo: Передавать номер переменной вместо Tree.Name
        return new LocalItem(pos, name, definitelyExists);
    }

    AccessItem makeAccess(int pos) {
        return new AccessItem(pos);
    }

    AssignItem makeAssign(int pos, Item var) {
        return new AssignItem(pos, var);
    }

    CondItem makeCond(int opcodePC) {
        return new CondItem(opcodePC);
    }

    CondItem makeCond(int opcodePC, IntArrayList truejumps, IntArrayList falsejumps) {
        return new CondItem(opcodePC, truejumps, falsejumps);
    }

    TempItem makeTemp() {
        return new TempItem();
    }
}
