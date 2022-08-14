package jua.compiler;

import jua.compiler.Types.*;
import jua.util.Source;

import java.util.List;
import java.util.ListIterator;

public interface Tree {

    enum Tag {
        TOP,
        FUNCDEF,
        CONSTDEF,
        BLOCK,
        IF,
        WHILELOOP,
        DOLOOP,
        FORLOOP,
        SWITCH,
        CASE,
        BREAK,
        CONTINUE,
        FALLTHROUGH,
        RETURN,
        DISCARDED,
        LITERAL,
        ARRAYLITERAL,
        VARIABLE,
        ARRAYACCESS,
        INVOCATION,
        PARENS,
        ASSIGN,
        ASG_ADD,
        ASG_SUB,
        ASG_MUL,
        ASG_DIV,
        ASG_REM,
        ASG_SL,
        ASG_SR,
        ASG_AND,
        ASG_OR,
        ASG_XOR,
        ASG_NULLCOALESCE,
        TERNARY,
        NULLCOALESCE,
        FLOW_OR,
        FLOW_AND,
        EQ,
        NE,
        GT,
        GE,
        LT,
        LE,
        ADD,
        SUB,
        MUL,
        DIV,
        REM,
        SL,
        SR,
        AND,
        OR,
        XOR,
        NEG,
        POS,
        NOT,
        INVERSE,
        CLONE,
        PREINC,
        PREDEC,
        POSTINC,
        POSTDEC
    }

    interface Visitor {
        void visitCompilationUnit(CompilationUnit tree);
        void visitConstDef(ConstDef tree);
        void visitFuncDef(FuncDef tree);
        void visitBlock(Block tree);
        void visitIf(If tree);
        void visitWhileLoop(WhileLoop tree);
        void visitFor(ForLoop tree);
        void visitDoLoop(DoLoop tree);
        void visitSwitch(Switch tree);
        void visitCase(Case tree);
        void visitBreak(Break tree);
        void visitContinue(Continue tree);
        void visitFallthrough(Fallthrough tree);
        void visitReturn(Return tree);
        void visitDiscarded(Discarded tree);
        void visitLiteral(Literal tree);
        void visitArrayLiteral(ArrayLiteral tree);
        void visitVariable(Var tree);
        void visitArrayAccess(ArrayAccess tree);
        void visitInvocation(Invocation tree);
        void visitParens(Parens tree);
        void visitAssignOp(AssignOp tree);
        void visitTernaryOp(TernaryOp tree);
        void visitBinaryOp(BinaryOp tree);
        void visitUnaryOp(UnaryOp tree);
    }

    abstract class AbstractVisitor implements Visitor {
        @Override
        public void visitCompilationUnit(CompilationUnit tree) { visitTree(tree);
        }

        @Override
        public void visitConstDef(ConstDef tree) { visitTree(tree); }

        @Override
        public void visitFuncDef(FuncDef tree) { visitTree(tree); }

        @Override
        public void visitBlock(Block tree) { visitTree(tree); }

        @Override
        public void visitIf(If tree) { visitTree(tree); }

        @Override
        public void visitWhileLoop(WhileLoop tree) { visitTree(tree); }

        @Override
        public void visitFor(ForLoop tree) { visitTree(tree); }

        @Override
        public void visitDoLoop(DoLoop tree) { visitTree(tree); }

        @Override
        public void visitSwitch(Switch tree) { visitTree(tree); }

        @Override
        public void visitCase(Case tree) { visitTree(tree); }

        @Override
        public void visitBreak(Break tree) { visitTree(tree); }

        @Override
        public void visitContinue(Continue tree) { visitTree(tree); }

        @Override
        public void visitFallthrough(Fallthrough tree) { visitTree(tree); }

        @Override
        public void visitReturn(Return tree) { visitTree(tree); }

        @Override
        public void visitLiteral(Literal tree) { visitTree(tree); }

        @Override
        public void visitArrayLiteral(ArrayLiteral tree) { visitTree(tree); }

        @Override
        public void visitVariable(Var tree) { visitTree(tree); }

        @Override
        public void visitArrayAccess(ArrayAccess tree) { visitTree(tree); }

        @Override
        public void visitInvocation(Invocation tree) { visitTree(tree); }

        @Override
        public void visitParens(Parens tree) { visitTree(tree); }

        @Override
        public void visitAssignOp(AssignOp tree) { visitTree(tree); }

        @Override
        public void visitTernaryOp(TernaryOp tree) { visitTree(tree); }

        @Override
        public void visitBinaryOp(BinaryOp tree) { visitTree(tree); }

        @Override
        public void visitUnaryOp(UnaryOp tree) { visitTree(tree); }

        @Override
        public void visitDiscarded(Discarded tree) { visitTree(tree); }

        public void visitTree(Tree tree) { throw new AssertionError(); }
    }

    abstract class Scanner extends AbstractVisitor {

        public void scan(Tree tree) {
            if (tree != null) {
                tree.accept(this);
            }
        }

        public void scan(List<? extends Tree> trees) {
            if (trees != null && !trees.isEmpty()) {
                for (Tree tree : trees) {
                    // В списке не должно быть null-значений.
                    tree.accept(this);
                }
            }
        }

        @Override
        public void visitCompilationUnit(CompilationUnit tree) {
            scan(tree.trees);
        }

        @Override
        public void visitConstDef(ConstDef tree) {
            for (ConstDef.Definition def : tree.defs) {
                scan(def.expr);
            }
        }

        @Override
        public void visitFuncDef(FuncDef tree) {
            for (FuncDef.Parameter param : tree.params) {
                scan(param.expr);
            }
            scan(tree.body);
        }

        @Override
        public void visitBlock(Block tree) {
            scan(tree.stats);
        }

        @Override
        public void visitIf(If tree) {
            scan(tree.cond);
            scan(tree.thenbody);
            scan(tree.elsebody);
        }

        @Override
        public void visitWhileLoop(WhileLoop tree) {
            scan(tree.cond);
            scan(tree.body);
        }

        @Override
        public void visitFor(ForLoop tree) {
            scan(tree.init);
            scan(tree.cond);
            scan(tree.step);
            scan(tree.body);
        }

        @Override
        public void visitDoLoop(DoLoop tree) {
            scan(tree.body);
            scan(tree.cond);
        }

        @Override
        public void visitSwitch(Switch tree) {
            scan(tree.expr);
            scan(tree.cases);
        }

        @Override
        public void visitCase(Case tree) {
            scan(tree.labels);
            scan(tree.body);
        }

        @Override
        public void visitBreak(Break tree) {}

        @Override
        public void visitContinue(Continue tree) { }

        @Override
        public void visitFallthrough(Fallthrough tree) {}

        @Override
        public void visitReturn(Return tree) {
            scan(tree.expr);
        }

        @Override
        public void visitLiteral(Literal tree) {  }

        @Override
        public void visitArrayLiteral(ArrayLiteral tree) {
            for (ArrayLiteral.Entry entry : tree.entries) {
                scan(entry.key);
                scan(entry.value);
            }
        }

        @Override
        public void visitVariable(Var tree) {  }

        @Override
        public void visitArrayAccess(ArrayAccess tree) {
            scan(tree.expr);
            scan(tree.index);
        }

        @Override
        public void visitInvocation(Invocation tree) {
            for (Invocation.Argument arg : tree.args) {
                scan(arg.expr);
            }
        }

        @Override
        public void visitParens(Parens tree) {
            scan(tree.expr);
        }

        @Override
        public void visitAssignOp(AssignOp tree) {
            scan(tree.src);
            scan(tree.dst);
        }

        @Override
        public void visitTernaryOp(TernaryOp tree) {
            scan(tree.cond);
            scan(tree.thenexpr);
            scan(tree.elseexpr);
        }

        @Override
        public void visitBinaryOp(BinaryOp tree) {
            scan(tree.lhs);
            scan(tree.rhs);
        }

        @Override
        public void visitUnaryOp(UnaryOp tree) {
            scan(tree.expr);
        }

        @Override
        public void visitDiscarded(Discarded tree) { }
    }

    abstract class Translator extends AbstractVisitor {

        public Tree result;

        @SuppressWarnings("unchecked")
        public <T extends Tree> T translate(Tree tree) {
            if (tree != null) {
                tree.accept(this);
                Tree r = result;
                result = null;
                return (T) r;
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        public <T extends Tree> List<T> translate(List<T> trees) {
            if (trees != null && !trees.isEmpty()) {
                ListIterator<T> treeIterator = trees.listIterator();
                do {
                    treeIterator.next().accept(this);
                    treeIterator.set((T) result);
                    result = null;
                } while (treeIterator.hasNext());
            }
            return trees;
        }

        @Override
        public void visitCompilationUnit(CompilationUnit tree) {
            tree.trees = translate(tree.trees);
            result = tree;
        }

        @Override
        public void visitConstDef(ConstDef tree) {
            for (ConstDef.Definition def : tree.defs) {
                def.expr = translate(def.expr);
            }
            result = tree;
        }

        @Override
        public void visitFuncDef(FuncDef tree) {
            for (FuncDef.Parameter param : tree.params) {
                param.expr = translate(param.expr);
            }
            tree.body = translate(tree.body);
            result = tree;
        }

        @Override
        public void visitBlock(Block tree) {
            tree.stats = translate(tree.stats);
            result = tree;
        }

        @Override
        public void visitIf(If tree) {
            tree.cond = translate(tree.cond);
            tree.thenbody = translate(tree.thenbody);
            tree.elsebody = translate(tree.elsebody);
            result = tree;
        }

        @Override
        public void visitWhileLoop(WhileLoop tree) {
            tree.cond = translate(tree.cond);
            tree.body = translate(tree.body);
            result = tree;
        }

        @Override
        public void visitFor(ForLoop tree) {
            tree.init = translate(tree.init);
            tree.cond = translate(tree.cond);
            tree.body = translate(tree.body);
            tree.step = translate(tree.step);
            result = tree;
        }

        @Override
        public void visitDoLoop(DoLoop tree) {
            tree.body = translate(tree.body);
            tree.cond = translate(tree.cond);
            result = tree;
        }

        @Override
        public void visitSwitch(Switch tree) {
            tree.expr = translate(tree.expr);
            tree.cases = translate(tree.cases);
            result = tree;
        }

        @Override
        public void visitCase(Case tree) {
            tree.labels = translate(tree.labels);
            tree.body = translate(tree.body);
            result = tree;
        }

        @Override
        public void visitBreak(Break tree) { result = tree; }

        @Override
        public void visitContinue(Continue tree) { result = tree; }

        @Override
        public void visitFallthrough(Fallthrough tree) { result = tree; }

        @Override
        public void visitReturn(Return tree) {
            tree.expr = translate(tree.expr);
            result = tree;
        }

        @Override
        public void visitLiteral(Literal tree) { result = tree; }

        @Override
        public void visitArrayLiteral(ArrayLiteral tree) {
            for (ArrayLiteral.Entry entry : tree.entries) {
                entry.key = translate(entry.key);
                entry.value = translate(entry.value);
            }
            result = tree;
        }

        @Override
        public void visitVariable(Var tree) { result = tree; }

        @Override
        public void visitArrayAccess(ArrayAccess tree) {
            tree.expr = translate(tree.expr);
            tree.index = translate(tree.index);
            result = tree;
        }

        @Override
        public void visitInvocation(Invocation tree) {
            for (Invocation.Argument arg : tree.args) {
                arg.expr = translate(arg.expr);
            }
            result = tree;
        }

        @Override
        public void visitParens(Parens tree) {
            tree.expr = translate(tree.expr);
            result = tree;
        }

        @Override
        public void visitAssignOp(AssignOp tree) {
            tree.dst = translate(tree.dst);
            tree.src = translate(tree.src);
            result = tree;
        }

        @Override
        public void visitTernaryOp(TernaryOp tree) {
            tree.cond = translate(tree.cond);
            tree.thenexpr = translate(tree.thenexpr);
            tree.elseexpr = translate(tree.elseexpr);
            result = tree;
        }

        @Override
        public void visitBinaryOp(BinaryOp tree) {
            tree.lhs = translate(tree.lhs);
            tree.rhs = translate(tree.rhs);
            result = tree;
        }

        @Override
        public void visitUnaryOp(UnaryOp tree) {
            tree.expr = translate(tree.expr);
            result = tree;
        }

        @Override
        public void visitDiscarded(Discarded tree) {
            tree.expr = translate(tree.expr);
            result = tree;
        }
    }

    Tag getTag();

    void accept(Visitor visitor);

    final class Name {

        public final String value;

        public final int pos;

        public Name(String value, int pos) {
            this.value = value;
            this.pos = pos;
        }
    }

    final class CompilationUnit implements Tree {

        public final Source source;

        public List<Tree> trees;

        public CompilationUnit(Source source, List<Tree> trees) {
            this.source = source;
            this.trees = trees;
        }

        @Override
        public Tag getTag() { return Tag.TOP; }

        @Override
        public void accept(Visitor visitor) { visitor.visitCompilationUnit(this); }
    }

    abstract class Statement implements Tree {

        public final int pos;

        protected Statement(int pos) {
            this.pos = pos;
        }
    }

    final class ConstDef extends Statement {

        public static final class Definition {

            public final Name name;

            public Expression expr;

            public Definition(Name name, Expression expr) {
                this.name = name;
                this.expr = expr;
            }
        }

        public List<Definition> defs;

        public ConstDef(int pos, List<Definition> defs) {
            super(pos);
            this.defs = defs;
        }

        @Override
        public Tag getTag() { return Tag.CONSTDEF; }

        @Override
        public void accept(Visitor visitor) { visitor.visitConstDef(this); }
    }

    final class FuncDef extends Statement {

        public static final class Parameter {

            public final Name name;

            public Expression expr;

            public Parameter(Name name, Expression expr) {
                this.name = name;
                this.expr = expr;
            }
        }
        
        public final Name name;

        public List<Parameter> params;

        public Statement body;

        public FuncDef(int pos, Name name, List<Parameter> params, Statement body) {
            super(pos);
            this.name = name;
            this.params = params;
            this.body = body;
        }

        @Override
        public Tag getTag() { return Tag.FUNCDEF; }

        @Override
        public void accept(Visitor visitor) { visitor.visitFuncDef(this); }
    }

    final class Block extends Statement {

        public List<Statement> stats;

        public Block(int pos, List<Statement> stats) {
            super(pos);
            this.stats = stats;
        }

        @Override
        public Tag getTag() { return Tag.BLOCK; }

        @Override
        public void accept(Visitor visitor) { visitor.visitBlock(this); }
    }

    final class If extends Statement {

        public Expression cond;

        public Statement thenbody;

        public Statement elsebody;

        public If(int pos, Expression cond, Statement thenbody, Statement elsebody) {
            super(pos);
            this.cond = cond;
            this.thenbody = thenbody;
            this.elsebody = elsebody;
        }

        @Override
        public Tag getTag() { return Tag.IF; }

        @Override
        public void accept(Visitor visitor) { visitor.visitIf(this); }
    }

    final class WhileLoop extends Statement {

        public Expression cond;

        public Statement body;

        public WhileLoop(int pos, Expression cond, Statement body) {
            super(pos);
            this.cond = cond;
            this.body = body;
        }

        @Override
        public Tag getTag() { return Tag.WHILELOOP; }

        @Override
        public void accept(Visitor visitor) { visitor.visitWhileLoop(this); }
    }

    final class ForLoop extends Statement {

        public List<Expression> init;

        public Expression cond;

        public List<Expression> step;

        public Statement body;

        public ForLoop(int pos, List<Expression> init, Expression cond, List<Expression> step, Statement body) {
            super(pos);
            this.init = init;
            this.cond = cond;
            this.step = step;
            this.body = body;
        }

        @Override
        public Tag getTag() { return Tag.FORLOOP; }

        @Override
        public void accept(Visitor visitor) { visitor.visitFor(this); }
    }

    final class DoLoop extends Statement {

        public Statement body;

        public Expression cond;

        public DoLoop(int pos, Statement body, Expression cond) {
            super(pos);
            this.body = body;
            this.cond = cond;
        }

        @Override
        public Tag getTag() { return Tag.DOLOOP; }

        @Override
        public void accept(Visitor visitor) { visitor.visitDoLoop(this); }
    }

    final class Switch extends Statement {

        public Expression expr;

        public List<Case> cases;

        public Switch(int pos, Expression expr, List<Case> cases) {
            super(pos);
            this.expr = expr;
            this.cases = cases;
        }

        @Override
        public Tag getTag() { return Tag.SWITCH; }

        @Override
        public void accept(Visitor visitor) { visitor.visitSwitch(this); }
    }

    final class Case extends Statement {

        public List<Expression> labels;

        public Statement body;

        public Case(int pos, List<Expression> labels, Statement body) {
            super(pos);
            this.labels = labels;
            this.body = body;
        }

        @Override
        public Tag getTag() { return Tag.CASE; }

        @Override
        public void accept(Visitor visitor) { visitor.visitCase(this); }
    }

    final class Break extends Statement {

        public Break(int pos) {
            super(pos);
        }

        @Override
        public Tag getTag() { return Tag.BREAK; }

        @Override
        public void accept(Visitor visitor) { visitor.visitBreak(this); }
    }

    final class Continue extends Statement {

        public Continue(int pos) {
            super(pos);
        }

        @Override
        public Tag getTag() { return Tag.CONTINUE; }

        @Override
        public void accept(Visitor visitor) { visitor.visitContinue(this); }
    }

    final class Fallthrough extends Statement {

        public Fallthrough(int pos) {
            super(pos);
        }

        @Override
        public Tag getTag() { return Tag.FALLTHROUGH; }

        @Override
        public void accept(Visitor visitor) { visitor.visitFallthrough(this); }
    }

    final class Return extends Statement {

        public Expression expr;

        public Return(int pos, Expression expr) {
            super(pos);
            this.expr = expr;
        }

        @Override
        public Tag getTag() { return Tag.RETURN; }

        @Override
        public void accept(Visitor visitor) { visitor.visitReturn(this); }
    }

    final class Discarded extends Statement {

        public Expression expr;

        public Discarded(int pos, Expression expr) {
            super(pos);
            this.expr = expr;
        }

        @Override
        public Tag getTag() { return Tag.DISCARDED; }

        @Override
        public void accept(Visitor visitor) { visitor.visitDiscarded(this); }
    }

    abstract class Expression extends Statement {

        protected Expression(int pos) {
            super(pos);
        }
    }

    final class Literal extends Expression {

        public final Type value;

        public Literal(int pos, Type value) {
            super(pos);
            this.value = value;
        }

        @Override
        public Tag getTag() { return Tag.LITERAL; }

        @Override
        public void accept(Visitor visitor) { visitor.visitLiteral(this); }
    }

    final class ArrayLiteral extends Expression {

        public static final class Entry {

            public Expression key, value;

            public Entry(Expression key, Expression value) {
                this.key = key;
                this.value = value;
            }
        }
        
        public List<Entry> entries;

        public ArrayLiteral(int pos, List<Entry> entries) {
            super(pos);
            this.entries = entries;
        }

        @Override
        public Tag getTag() { return Tag.ARRAYLITERAL; }

        @Override
        public void accept(Visitor visitor) { visitor.visitArrayLiteral(this); }
    }

    final class Var extends Expression {

        public final Name name;

        public Var(int pos, Name name) {
            super(pos);
            this.name = name;
        }

        @Override
        public Tag getTag() { return Tag.VARIABLE; }

        @Override
        public void accept(Visitor visitor) { visitor.visitVariable(this); }
    }

    final class ArrayAccess extends Expression {

        public Expression expr, index;

        public ArrayAccess(int pos, Expression expr, Expression index) {
            super(pos);
            this.expr = expr;
            this.index = index;
        }

        @Override
        public Tag getTag() { return Tag.ARRAYACCESS; }

        @Override
        public void accept(Visitor visitor) { visitor.visitArrayAccess(this); }
    }

    final class Invocation extends Expression {

        public static final class Argument {

            public final Name name; // todo: Значение этого поля всегда null, потому что не реализовано.

            public Expression expr;

            public Argument(Name name, Expression expr) {
                this.name = name;
                this.expr = expr;
            }
        }
        
        public final Name name;

        public List<Argument> args;

        public Invocation(int pos, Name name, List<Argument> args) {
            super(pos);
            this.name = name;
            this.args = args;
        }

        @Override
        public Tag getTag() { return Tag.INVOCATION; }

        @Override
        public void accept(Visitor visitor) { visitor.visitInvocation(this); }
    }

    final class Parens extends Expression {

        public Expression expr;

        public Parens(int pos, Expression expr) {
            super(pos);
            this.expr = expr;
        }

        @Override
        public Tag getTag() { return Tag.PARENS; }

        @Override
        public void accept(Visitor visitor) { visitor.visitParens(this); }
    }

    final class AssignOp extends Expression {

        public final Tag tag;

        public Expression dst, src;

        public AssignOp(int pos, Tag tag, Expression dst, Expression src) {
            super(pos);
            this.tag = tag;
            this.dst = dst;
            this.src = src;
        }

        @Override
        public Tag getTag() { return tag; }

        @Override
        public void accept(Visitor visitor) { visitor.visitAssignOp(this); }
    }

    final class TernaryOp extends Expression {

        public Expression cond, thenexpr, elseexpr;

        public TernaryOp(int pos, Expression cond, Expression thenexpr, Expression elseexpr) {
            super(pos);
            this.cond = cond;
            this.thenexpr = thenexpr;
            this.elseexpr = elseexpr;
        }

        @Override
        public Tag getTag() { return Tag.TERNARY; }

        @Override
        public void accept(Visitor visitor) { visitor.visitTernaryOp(this); }
    }

    final class BinaryOp extends Expression {

        public final Tag tag;

        public Expression lhs, rhs;

        public BinaryOp(int pos, Tag tag, Expression lhs, Expression rhs) {
            super(pos);
            this.tag = tag;
            this.lhs = lhs;
            this.rhs = rhs;
        }

        @Override
        public Tag getTag() { return tag; }

        @Override
        public void accept(Visitor visitor) { visitor.visitBinaryOp(this); }
    }

    final class UnaryOp extends Expression {

        public final Tag tag;

        public Expression expr;

        public UnaryOp(int pos, Tag tag, Expression expr) {
            super(pos);
            this.tag = tag;
            this.expr = expr;
        }

        @Override
        public Tag getTag() { return tag; }

        @Override
        public void accept(Visitor visitor) { visitor.visitUnaryOp(this); }
    }
}
