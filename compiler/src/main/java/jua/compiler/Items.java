package jua.compiler;

import jua.compiler.Code.Chain;
import jua.compiler.InstructionUtils.OPCodes;
import jua.compiler.Tree.Tag;

import java.util.Objects;

import static jua.compiler.Code.mergeChains;
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

        Item t(Tree tree) {
            this.tree = tree;
            return this;
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
            return new CondItem(OPCodes.IfNz);
        }

        CondItem asNonNullCond() {
            load();
            return new CondItem(OPCodes.IfNonNull);
        }

        CondItem asPresentCond() {
            return asNonNullCond();
        }

        SafeItem wrapEvacuate() {
            return new SafeItem(this);
        }

        Item increase(Tag increaseTag) {
            throw new UnsupportedOperationException(getClass().getName());
        }

        Item coalesceAsg(Chain skipCoalesceChain) {
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
            code.emitSingle(OPCodes.Pop);
        }

        @Override
        void duplicate() {
            code.emitSingle(OPCodes.Dup);
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
                code.emitSingle(OPCodes.ConstNull);
            } else if (value == Boolean.TRUE) {
                code.emitSingle(OPCodes.ConstTrue);
            } else if (value == Boolean.FALSE) {
                code.emitSingle(OPCodes.ConstFalse);
            } else if (value.getClass() == Long.class && -1 <= ((long) value) && ((long) value) <= 2) {
                code.emitSingle(OPCodes.ConstIntM1 + ((Long) value).intValue() + 1);
            } else {
                code.emitConst(constantIndex());
            }
            return mkStackItem();
        }

        @Override
        void drop() {
        }

        @Override
        int constantIndex() {
            return code.resolveConstant(value);
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
            if (index <= 2) {
                code.emitSingle(OPCodes.Load0 + index);
            } else {
                code.emitIndexed(OPCodes.Load, index);
            }
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
            code.emitSingle(OPCodes.Dup);
        }

        @Override
        void store() {
            code.markTreePos(tree);
            if (index <= 2) {
                code.emitSingle(OPCodes.Store0 + index);
            } else {
                code.emitIndexed(OPCodes.Store, index);
            }
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
            code.emitIndexed(increaseFromTag(increaseTag), item.index);
        }
    }

    class AccessItem extends Item {

        @Override
        Item load() {
            code.markTreePos(tree);
            code.emitSingle(OPCodes.ArrayLoad);
            return mkStackItem();
        }

        @Override
        void drop() {
            code.emitSingle(OPCodes.Pop2);
        }

        @Override
        void store() {
            code.markTreePos(tree);
            code.emitSingle(OPCodes.ArrayStore);
        }

        @Override
        CondItem asPresentCond() {
            return new CondItem(OPCodes.IfPresent);
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
            code.emitSingle(OPCodes.Dup2);
        }

        @Override
        void stash() {
            code.emitSingle(OPCodes.DupX2);
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
                code.emitSingle(OPCodes.Dup2);
                drop();
                code.emitSingle(OPCodes.ArrayLoad);
            } else {
                code.emitSingle(arrayIncreaseFromTag(increaseTag));
            }
            return mkStackItem();
        }

        @Override
        void drop() {
            code.emitSingle(arrayIncreaseFromTag(increaseTag));
            code.emitSingle(OPCodes.Pop);
        }
    }

    class AccessCoalesceItem extends Item {
        final Chain skipCoalesceChain;

        AccessCoalesceItem(Chain skipCoalesceChain) {
            this.skipCoalesceChain = skipCoalesceChain;
        }

        @Override
        Item load() {
            code.emitSingle(OPCodes.DupX2);
            code.emitSingle(OPCodes.ArrayStore);
            Chain exitChain = code.branch(OPCodes.Goto);
            code.resolve(skipCoalesceChain);
            code.emitSingle(OPCodes.ArrayLoad);
            code.resolve(exitChain);
            return mkStackItem();
        }

        @Override
        void drop() {
            code.emitSingle(OPCodes.ArrayStore);
            Chain exitChain = code.branch(OPCodes.Goto);
            code.resolve(skipCoalesceChain);
            code.emitSingle(OPCodes.Pop2);
            code.resolve(exitChain);
        }
    }

    class SafeItem extends Item {
        final Item target;

        /**
         * Цепь условных переходов из конструкции в точку её обнуления.
         * Когда справа налево встречается первый нулевой элемент цепи обращений,
         * из любого места будет выполнен переход к обнулению всей конструкции.
         */
        Chain evacuation = null;

        SafeItem(Item target) {
            this.target = target;
        }

        @Override
        Item load() {
            return target.load();
        }

        @Override
        CondItem asCond() {
            return target.asCond();
        }

        @Override
        SafeItem wrapEvacuate() {
            return this;
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
        final int opcode;
        Chain trueChain;
        Chain falseChain;

        CondItem(int opcode) {
            this(opcode, null, null);
        }

        CondItem(int opcode, Chain trueChain, Chain elseChain) {
            this.opcode = opcode;
            this.trueChain = trueChain;
            this.falseChain = elseChain;
        }

        @Override
        CondItem t(Tree tree) {
            super.t(tree);
            return this;
        }

        @Override
        public Item load() {
            Chain falseJumps = falseJumps();
            code.resolve(trueChain);
            code.emitSingle(OPCodes.ConstTrue);
            Chain skipElsePartChain = code.branch(OPCodes.Goto);
            code.resolve(falseJumps);
            code.emitSingle(OPCodes.ConstFalse);
            code.resolve(skipElsePartChain);
            return mkStackItem();
        }

        @Override
        public void drop() {
            code.emitSingle(OPCodes.Pop);
        }

        @Override
        public CondItem asCond() {
            return this;
        }

        public CondItem negated() {
            return new CondItem(InstructionUtils.negate(opcode), falseChain, trueChain).t(tree);
        }

        public Chain trueJumps() {
            code.markTreePos(tree);
            return mergeChains(trueChain, code.branch(opcode));
        }

        public Chain falseJumps() {
            code.markTreePos(tree);
            return mergeChains(falseChain, code.branch(InstructionUtils.negate(opcode)));
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

    CondItem makeCondItem(int opcode) {
        return new CondItem(opcode);
    }

    CondItem makeCondItem(int opcode, Chain trueJumps, Chain falseJumps) {
        return new CondItem(opcode, trueJumps, falseJumps);
    }

    SafeItem mkSafe(Item target) {
        return new SafeItem(target);
    }
}
