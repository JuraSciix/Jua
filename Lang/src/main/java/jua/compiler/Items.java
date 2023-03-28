package jua.compiler;

import jua.compiler.Code.Chain;
import jua.compiler.Tree.Tag;
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

        Tree tree;

        Item load() {
            throw new AssertionError(this);
        }

        void drop() {
            load();
            code.addInstruction(pop);
        }

        void duplicate() {

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

        CondItem contains() {
            throw new AssertionError(this);
        }

        int constantIndex() {
            throw new AssertionError(this);
        }

        void incOrDec(Tag tag) {
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
            code.position(tree);
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
            code.position(tree);
            code.addInstruction((index <= 2) ? load_x[index] : new Load(index));
            return stackItem;
        }

        @Override
        void drop() {
            // Ранее переменная загружалась для того,
            // чтобы убедиться в её существовании во
            // времени выполнения. Сейчас переменная
            // декларируется в коде явно, поэтому
            // эта механика неактуальна.
//            load();
//            code.addInstruction(pop);
        }

        @Override
        void store() {
            code.position(tree);
            code.addInstruction((index <= 2) ? store_x[index] : new Store(index));
        }

        @Override
        CondItem contains() {
            return nonNull();
        }

        @Override
        void incOrDec(Tag tag) {
            code.position(tree);
            code.addInstruction((tag == Tag.POSTINC || tag == Tag.PREINC) ?
                    new Inc(index) : new Dec(index));
        }

        @Override
        void stash() {
            code.addInstruction(dup);
        }
    }

    /**
     * Обращение к элементу массива.
     */
    class AccessItem extends Item {

        @Override
        Item load() {
            code.position(tree);
            code.addInstruction(aload);
            return stackItem;
        }

        @Override
        void store() {
            code.position(tree);
            code.addInstruction(astore);
        }

        @Override
        CondItem contains() {
            return new CondItem(new Ifpresent());
        }

        @Override
        void incOrDec(Tag tag) {
            code.addInstruction((tag == Tag.POSTINC || tag == Tag.PREINC) ? ainc : adec);
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

    class NullSafeItem extends Item {

        final Item child;

        Chain nullChain;

        NullSafeItem(Item child) {
            this.child = child;
        }

        @Override
        Item load() {
            child.load();
            Chain nonNullChain = code.branch(new Goto());
            code.resolve(nullChain);
            code.addInstruction(pop);
            code.addInstruction(const_null);
            code.resolve(nonNullChain);
            return stackItem;
        }

        @Override
        void stash() {
            child.stash();
        }

        @Override
        CondItem contains() {
            return new CondItem(new Ifpresent());
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
            code.position(tree);
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
            return treeify(new CondItem(opcode.negate(), falseChain, trueChain), tree);
        }

        Chain trueJumps() {
            code.position(tree);
            return Code.mergeChains(trueChain, code.branch(opcode));
        }

        Chain falseJumps() {
            code.position(tree);
            return Code.mergeChains(falseChain, code.branch(opcode.negate()));
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

    NullSafeItem makeNullSafe(Item child) {
        return new NullSafeItem(child);
    }

    CondItem makeCond(Instruction opcode) {
        return new CondItem(opcode);
    }

    CondItem makeCond(Instruction opcode, Chain truejumps, Chain falsejumps) {
        return new CondItem(opcode, truejumps, falsejumps);
    }

    static <T extends Item> T treeify(T t, Tree tree) {
        t.tree = tree;
        return t;
    }
}
