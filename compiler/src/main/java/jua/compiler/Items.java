package jua.compiler;

import jua.compiler.Code.Chain;
import jua.compiler.Tree.Tag;
import jua.interpreter.instruction.*;

import java.util.Objects;

import static jua.compiler.Code.mergeChains;
import static jua.compiler.InstructionFactory.*;
import static jua.compiler.InstructionUtils.arrayIncreaseFromTag;
import static jua.compiler.InstructionUtils.increaseFromTag;

public final class Items {
    // Поля не должны быть видны вне класса Items
    private final Code code;
    private final StackItem stackItem = new StackItem();

    public Items(Code code) {
        this.code = Objects.requireNonNull(code);
    }

    abstract class Item {
        Tree tree;

        @SuppressWarnings("unchecked")
        <T extends Item> T t(Tree tree) {
            this.tree = tree;
            return (T) this;
        }

        Item load() {
            throw new UnsupportedOperationException(getClass().getName());
        }

        void drop() {
            load().drop();
        }

        void duplicate() {
            throw new UnsupportedOperationException(getClass().getName());
        }

        void stash() {
            throw new UnsupportedOperationException(getClass().getName());
        }

        void store() {
            throw new UnsupportedOperationException(getClass().getName());
        }

        CondItem asCond() {
            load();
            return new CondItem(new Ifnz());
        }

        CondItem asNonNullCond() {
            load();
            return new CondItem(new Ifnonnull());
        }

        CondItem asPresentCond() {
            return asNonNullCond();
        }

        SafeAccessItem asSafe(Chain chain) {
            return new SafeAccessItem(this, chain);
        }

        Item increase(Tag increaseTag) {
            throw new UnsupportedOperationException(getClass().getName());
        }

        Item coalesceAsg(Chain skipCoalesceChain) {
            throw new UnsupportedOperationException(getClass().getName());
        }

        Item coalesce() {
            throw new UnsupportedOperationException(getClass().getName());
        }

        int constantIndex() {
            throw new UnsupportedOperationException(getClass().getName());
        }
    }

    class StackItem extends Item {

        @Override
        Item load() {
            return this;
        }

        @Override
        void drop() {
            code.addInstruction(pop);
        }

        @Override
        void duplicate() {
            code.addInstruction(dup);
        }
    }

    class LiteralItem extends Item {
        final Object value;

        LiteralItem(Object value) {
            this.value = value;
        }

        @Override
        Item load() {
            code.markTreePos(tree);
            if (value == null) {
                code.addInstruction(const_null);
            } else if (value == Boolean.TRUE) {
                code.addInstruction(const_true);
            } else if (value == Boolean.FALSE) {
                code.addInstruction(const_false);
            } else if (value.getClass() == Long.class && -1 <= ((long) value) && ((long) value) <= 1) {
                code.addInstruction(const_ix[((Long) value).intValue() + 1]);
            } else {
                code.addInstruction(new Push(constantIndex()));
            }
            return makeStackItem();
        }

        @Override
        void drop() {
        }

        @Override
        int constantIndex() {
            return code.constantPoolWriter().write(value);
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
            code.markTreePos(tree);
            code.addInstruction((index <= 2) ? load_x[index] : new Load(index));
            return makeStackItem();
        }

        @Override
        void drop() {
        }

        @Override
        void duplicate() {
        }

        @Override
        void stash() {
            code.addInstruction(dup);
        }

        @Override
        void store() {
            code.markTreePos(tree);
            code.addInstruction((index <= 2) ? store_x[index] : new Store(index));
        }

        @Override
        CondItem asPresentCond() {
            return asNonNullCond();
        }

        @Override
        Item increase(Tag increaseTag) {
            return new LocalIncreaseItem(this, increaseTag);
        }

        @Override
        Item coalesceAsg(Chain skipCoalesceChain) {
            store();
            code.resolve(skipCoalesceChain);
            return this;
        }
    }

    class LocalIncreaseItem extends Item {
        final LocalItem item;
        final Tag increaseTag;

        LocalIncreaseItem(LocalItem item, Tag increaseTag) {
            this.item = item;
            this.increaseTag = increaseTag;
        }

        @Override
        Item load() {
            if (increaseTag == Tag.PREINC || increaseTag == Tag.PREDEC) {
                drop();
                item.load();
            } else {
                item.load();
                drop();
            }
            return makeStackItem();
        }

        @Override
        void drop() {
            code.addInstruction(increaseFromTag(increaseTag, item.index));
        }
    }

    class AccessItem extends Item {

        @Override
        Item load() {
            code.markTreePos(tree);
            code.addInstruction(aload);
            return makeStackItem();
        }

        @Override
        void drop() {
            code.addInstruction(pop2);
        }

        @Override
        void store() {
            code.markTreePos(tree);
            code.addInstruction(astore);
        }

        @Override
        CondItem asPresentCond() {
            return new CondItem(new IfPresent());
        }

        @Override
        Item increase(Tag increaseTag) {
            return new AccessIncreaseItem(increaseTag);
        }

        @Override
        Item coalesceAsg(Chain skipCoalesceChain) {
            return new AccessCoalesceItem(skipCoalesceChain);
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

    class AccessIncreaseItem extends Item {
        final Tag increaseTag;

        AccessIncreaseItem(Tag increaseTag) {
            this.increaseTag = increaseTag;
        }

        @Override
        Item load() {
            // adec/ainc оставляют после себя старое значение.
            if (increaseTag == Tag.PREINC || increaseTag == Tag.PREDEC) {
                code.addInstruction(dup2);
                drop();
                code.addInstruction(aload);
            } else {
                code.addInstruction(arrayIncreaseFromTag(increaseTag));
            }
            return makeStackItem();
        }

        @Override
        void drop() {
            code.addInstruction(arrayIncreaseFromTag(increaseTag));
            code.addInstruction(pop);
        }
    }

    class AccessCoalesceItem extends Item {
        final Chain skipCoalesceChain;

        AccessCoalesceItem(Chain skipCoalesceChain) {
            this.skipCoalesceChain = skipCoalesceChain;
        }

        @Override
        Item load() {
            code.addInstruction(dup_x2);
            code.addInstruction(astore);
            Chain exitChain = code.branch(new Goto());
            code.resolve(skipCoalesceChain);
            code.addInstruction(aload);
            code.resolve(exitChain);
            return makeStackItem();
        }

        @Override
        void drop() {
            code.addInstruction(astore);
            Chain exitChain = code.branch(new Goto());
            code.resolve(skipCoalesceChain);
            code.addInstruction(pop2);
            code.resolve(exitChain);
        }
    }

    class SafeAccessItem extends Item {
        final Item item;
        /**
         * Цепь условных переходов из конструкции в точку её обнуления.
         * Когда справа налево встречается первый нулевой элемент цепи обращений,
         * из любого места будет выполнен переход к обнулению всей конструкции.
         */
        Chain constNullChain;

        SafeAccessItem(Item item, Chain constNullChain) {
            this.item = item;
            this.constNullChain = constNullChain;
        }

        @Override
        Item load() {
            item.load();
            Chain skipNullingChain = code.branch(new Goto());
            code.resolve(constNullChain);
            code.addInstruction(const_null);
            code.resolve(skipNullingChain);
            return makeStackItem();
        }

        @Override
        SafeAccessItem asSafe(Chain chain) {
            return new SafeAccessItem(item, mergeChains(constNullChain, chain));
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
            return makeStackItem();
        }

        @Override
        void drop() {
            var.store();
        }

        @Override
        void duplicate() {
            load().duplicate();
        }
    }

    /**
     * Условное разветвление.
     */
    class CondItem extends Item {
        final JumpInstruction opcode;
        Chain trueChain;
        Chain falseChain;

        CondItem(JumpInstruction opcode) {
            this(opcode, null, null);
        }

        CondItem(JumpInstruction opcode, Chain trueChain, Chain elseChain) {
            this.opcode = opcode;
            this.trueChain = trueChain;
            this.falseChain = elseChain;
        }

        @Override
        public Item load() {
            Chain falseJumps = falseJumps();
            code.resolve(trueChain);
            code.addInstruction(const_true);
            Chain skipElsePartChain = code.branch(new Goto());
            code.resolve(falseJumps);
            code.addInstruction(const_false);
            code.resolve(skipElsePartChain);
            return makeStackItem();
        }

        @Override
        public void drop() {
            code.addInstruction(pop);
        }

        @Override
        public CondItem asCond() {
            return this;
        }

        public CondItem negated() {
            return new CondItem(opcode.negated(), falseChain, trueChain).t(tree);
        }

        public Chain trueJumps() {
            code.markTreePos(tree);
            return mergeChains(trueChain, code.branch(opcode));
        }

        public Chain falseJumps() {
            code.markTreePos(tree);
            return mergeChains(falseChain, code.branch(opcode.negated()));
        }
    }

    StackItem makeStackItem() {
        return stackItem;
    }

    LiteralItem makeLiteralItem(Object value) {
        return new LiteralItem(value);
    }

    LocalItem makeLocal(int index) {
        return new LocalItem(index);
    }

    AccessItem makeAccessitem() {
        return new AccessItem();
    }

    AssignItem makeAssignItem(Item var) {
        return new AssignItem(var);
    }

    CondItem makeCondItem(JumpInstruction opcode) {
        return new CondItem(opcode);
    }

    CondItem makeCondItem(JumpInstruction opcode, Chain trueJumps, Chain falseJumps) {
        return new CondItem(opcode, trueJumps, falseJumps);
    }
}
