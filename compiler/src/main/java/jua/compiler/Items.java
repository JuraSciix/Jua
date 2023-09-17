package jua.compiler;

import jua.compiler.Code.Chain;
import jua.compiler.Tree.Tag;
import jua.interpreter.instruction.*;
import jua.interpreter.instruction.InstructionImpls.*;

import java.util.Objects;

import static jua.compiler.Code.mergeChains;
import static jua.compiler.InstructionFactory.*;
import static jua.compiler.InstructionUtils.arrayIncreaseFromTag;
import static jua.compiler.InstructionUtils.increaseFromTag;

/**
 * Item - это такая хуйня, которая получается после компиляции (генерации кода)
 * узла абстрактного дерева исходного кода, которой можно манипулировать.
 */
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
            load().duplicate();
        }

        void stash() {
            throw new UnsupportedOperationException(getClass().getName());
        }

        void store() {
            throw new UnsupportedOperationException(getClass().getName());
        }

        CondItem asCond() {
            load();
            return new CondItem(new IfNz(0));
        }

        CondItem asNonNullCond() {
            load();
            return new CondItem(new IfNonNull(0));
        }

        CondItem asPresentCond() {
            return asNonNullCond();
        }

        SafeItem asSafe(Item coalesce, Chain chain) {
            return new SafeItem(this, coalesce, chain);
        }

        Item increase(Tag increaseTag) {
            throw new UnsupportedOperationException(getClass().getName());
        }

        Item coalesceAsg(Chain skipCoalesceChain) {
            throw new UnsupportedOperationException(getClass().getName());
        }

        Item coalesce(Item item) {
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
            return mkStackItem();
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
            return mkStackItem();
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
            return mkStackItem();
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
            return mkStackItem();
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
            return new CondItem(new IfPresent(0));
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
            return mkStackItem();
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
            Chain exitChain = code.branch(new InstructionImpls.Goto(0));
            code.resolve(skipCoalesceChain);
            code.addInstruction(aload);
            code.resolve(exitChain);
            return mkStackItem();
        }

        @Override
        void drop() {
            code.addInstruction(astore);
            Chain exitChain = code.branch(new Goto(0));
            code.resolve(skipCoalesceChain);
            code.addInstruction(pop2);
            code.resolve(exitChain);
        }
    }

    class SafeItem extends Item {
        final Item child;
        final Item coalesce;

        /**
         * Цепь условных переходов из конструкции в точку её обнуления.
         * Когда справа налево встречается первый нулевой элемент цепи обращений,
         * из любого места будет выполнен переход к обнулению всей конструкции.
         */
        Chain coalesceChain;

        SafeItem(Item child, Item coalesce, Chain coalesceChain) {
            this.child = child;
            this.coalesce = coalesce;
            this.coalesceChain = coalesceChain;
        }

        @Override
        Item load() {
            Item load = child.load();
            Chain skipCoalesceLoadChain = code.branch(new Goto(0));
            code.resolve(coalesceChain);
            load.drop();
            coalesce.load();
            code.resolve(skipCoalesceLoadChain);
            return mkStackItem();
        }

        @Override
        SafeItem asSafe(Item coalesce, Chain chain) {
            return new SafeItem(child, coalesce, mergeChains(coalesceChain, chain));
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
            return mkStackItem();
        }

        @Override
        void drop() {
            var.store();
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
            Chain skipElsePartChain = code.branch(new Goto(0));
            code.resolve(falseJumps);
            code.addInstruction(const_false);
            code.resolve(skipElsePartChain);
            return mkStackItem();
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

    StackItem mkStackItem() {
        return stackItem;
    }

    LiteralItem mkLiteral(Object value) {
        return new LiteralItem(value);
    }

    LocalItem makeLocal(int index) {
        return new LocalItem(index);
    }

    AccessItem mkAccessItem() {
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
