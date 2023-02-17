package jua.compiler;

import jua.compiler.Code.Chain;
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
            return new CondItem(new Ifnz());
        }

        CondItem nonNull() {
            load();
            return new CondItem(new Ifnonnull());
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
                    if (-1L <= lv && lv <= 1L) {
                        code.addInstruction(const_ix[(int) lv + 1]);
                        return stackItem;
                    }
                }
                code.addInstruction(new Push(type.resolvePoolConstant(code)));
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
        LocalItem(int index) {
            this.index = index;
        }

        @Override
        Item load() {
            code.addInstruction((index <= 2) ? load_x[index] : new Load(index));
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
            code.addInstruction((index <= 2) ? store_x[index] : new Store(index));
        }

        @Override
        void stash() {
            code.addInstruction(dup);
        }

        void inc() {
            code.addInstruction(new Inc(index));
        }

        void dec() {
            code.addInstruction(new Dec(index));
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

        final Instruction opcode;
        Chain trueChain;
        Chain falseChain;

        CondItem(Instruction opcode) {
            this(opcode, null, null);
        }

        CondItem(Instruction opcode, Chain trueChain, Chain falseChain) {
            this.opcode = opcode;
            this.trueChain = trueChain;
            this.falseChain = falseChain;
        }

        @Override
        Item load() {
            Chain falseJumps = falseJumps();
            code.resolve(trueChain);
            int st = code.curStackTop();
            code.addInstruction(const_true);
            Chain trueChain = code.branch(new Goto());
            code.curStackTop(st);
            code.resolve(falseJumps);
            code.addInstruction(const_false);
            code.resolve(trueChain);
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
            return new CondItem(opcode.negate(), falseChain, trueChain);
        }

        Chain trueJumps() {
            return Code.mergeChains(trueChain, code.branch(opcode));
        }

        Chain falseJumps() {
            return Code.mergeChains(falseChain, code.branch(opcode.negate()));
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
            code.addInstruction(new Load(index));
            drop();
            return stackItem;
        }

        @Override
        void store() {
            code.addInstruction(new Store(index));
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

    LocalItem makeLocal(int index) {
        return new LocalItem(index);
    }

    AccessItem makeAccess() {
        return new AccessItem();
    }

    AssignItem makeAssign(Item var) {
        return new AssignItem(var);
    }

    CondItem makeCond(Instruction opcode) {
        return new CondItem(opcode);
    }

    CondItem makeCond(Instruction opcode, Chain truejumps, Chain falsejumps) {
        return new CondItem(opcode, truejumps, falsejumps);
    }

    TempItem makeTemp() {
        return new TempItem();
    }
}
