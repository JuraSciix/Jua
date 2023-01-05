package jua.compiler;

import jua.compiler.Types.*;
import jua.util.List;

import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class Tree {

    public enum Tag {
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
        MEMACCESS,
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
        PREINC,
        PREDEC,
        POSTINC,
        POSTDEC
    }

    public interface Visitor {
        void visitCompilationUnit(CompilationUnit tree);
        void visitConstDef(ConstDef tree);
        void visitFuncDef(FuncDef tree);
        void visitBlock(Block tree);
        void visitIf(If tree);
        void visitWhileLoop(WhileLoop tree);
        void visitDoLoop(DoLoop tree);
        void visitForLoop(ForLoop tree);
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
        void visitMemberAccess(MemberAccess tree);
        void visitArrayAccess(ArrayAccess tree);
        void visitInvocation(Invocation tree);
        void visitParens(Parens tree);
        void visitAssign(Assign tree);
        void visitCompoundAssign(CompoundAssign tree);
        void visitTernaryOp(TernaryOp tree);
        void visitBinaryOp(BinaryOp tree);
        void visitUnaryOp(UnaryOp tree);
    }

    public static abstract class AbstractVisitor implements Visitor {
        @Override
        public void visitCompilationUnit(CompilationUnit tree) { visitTree(tree); }

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
        public void visitDoLoop(DoLoop tree) { visitTree(tree); }

        @Override
        public void visitForLoop(ForLoop tree) { visitTree(tree); }

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
        public void visitDiscarded(Discarded tree) { visitTree(tree); }

        @Override
        public void visitLiteral(Literal tree) { visitTree(tree); }

        @Override
        public void visitArrayLiteral(ArrayLiteral tree) { visitTree(tree); }

        @Override
        public void visitVariable(Var tree) { visitTree(tree); }

        @Override
        public void visitMemberAccess(MemberAccess tree) { visitTree(tree); }

        @Override
        public void visitArrayAccess(ArrayAccess tree) { visitTree(tree); }

        @Override
        public void visitInvocation(Invocation tree) { visitTree(tree); }

        @Override
        public void visitParens(Parens tree) { visitTree(tree); }

        @Override
        public void visitAssign(Assign tree) { visitTree(tree); }

        @Override
        public void visitCompoundAssign(CompoundAssign tree) { visitTree(tree); }

        @Override
        public void visitTernaryOp(TernaryOp tree) { visitTree(tree); }

        @Override
        public void visitBinaryOp(BinaryOp tree) { visitTree(tree); }

        @Override
        public void visitUnaryOp(UnaryOp tree) { visitTree(tree); }

        public void visitTree(Tree tree) { throw new AssertionError(); }
    }

    public static abstract class Scanner extends AbstractVisitor {

        public void scan(Tree tree) {
            if (tree != null) {
                tree.accept(this);
            }
        }

        public void scan(List<? extends Tree> trees) {
            if (trees != null && trees.nonEmpty()) {
                for (Tree tree : trees) {
                    // В списке не должно быть null-значений.
                    tree.accept(this);
                }
            }
        }

        @Override
        public void visitCompilationUnit(CompilationUnit tree) {
            scan(tree.constDefs);
            scan(tree.funcDefs);
            scan(tree.stats);
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
        public void visitDoLoop(DoLoop tree) {
            scan(tree.body);
            scan(tree.cond);
        }

        @Override
        public void visitForLoop(ForLoop tree) {
            scan(tree.init);
            scan(tree.cond);
            scan(tree.body);
            scan(tree.step);
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
        public void visitDiscarded(Discarded tree) {
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
        public void visitMemberAccess(MemberAccess tree) {
            scan(tree.expr);
        }

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
        public void visitAssign(Assign tree) {
            scan(tree.var);
            scan(tree.expr);
        }

        @Override
        public void visitCompoundAssign(CompoundAssign tree) {
            scan(tree.expr);
            scan(tree.var);
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
    }

    public static abstract class Translator extends AbstractVisitor {

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
            if (trees != null && trees.nonEmpty()) {
                for (List.Node<T> node = trees.head(); node != null; node = node.next()) {
                    node.value.accept(this);
                    node.value = (T) result;
                    result = null;
                }
            }
            return trees;
        }

        @Override
        public void visitCompilationUnit(CompilationUnit tree) {
            tree.constDefs = translate(tree.constDefs);
            tree.funcDefs = translate(tree.funcDefs);
            tree.stats = translate(tree.stats);
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
        public void visitDoLoop(DoLoop tree) {
            tree.body = translate(tree.body);
            tree.cond = translate(tree.cond);
            result = tree;
        }

        @Override
        public void visitForLoop(ForLoop tree) {
            tree.init = translate(tree.init);
            tree.cond = translate(tree.cond);
            tree.body = translate(tree.body);
            tree.step = translate(tree.step);
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
        public void visitDiscarded(Discarded tree) {
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
        public void visitMemberAccess(MemberAccess tree) {
            tree.expr = translate(tree.expr);
            result = tree;
        }

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
        public void visitAssign(Assign tree) {
            tree.var = translate(tree.var);
            tree.expr = translate(tree.expr);
            result = tree;
        }

        @Override
        public void visitCompoundAssign(CompoundAssign tree) {
            tree.var = translate(tree.var);
            tree.expr = translate(tree.expr);
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
    }

    public final int pos;
    
    protected Tree(int pos) {
        this.pos = pos;
    }
    
    public abstract Tag getTag();

    public abstract void accept(Visitor visitor);

    public boolean hasTag(Tag tag) {
        return getTag() == tag;
    }

    @Override
    public String toString() {
        StringWriter writer = new StringWriter();
        accept(new Pretty(new PrintWriter(writer)));
        return writer.toString();
    }

    public static class Name {

        public final String value;

        public final int pos;

        public Name(String value, int pos) {
            this.value = value;
            this.pos = pos;
        }
    }

    public static class CompilationUnit extends Tree {

        public final Source source;

        public List<Statement> stats;

        public List<FuncDef> funcDefs;

        public List<ConstDef> constDefs;

        public Code code;

        public CompilationUnit(int pos, Source source, List<Statement> stats, List<FuncDef> funcDefs, List<ConstDef> constDefs) {
            super(pos);
            this.source = source;
            this.stats = stats;
            this.funcDefs = funcDefs;
            this.constDefs = constDefs;
        }

        @Override
        public Tag getTag() { return Tag.TOP; }

        @Override
        public void accept(Visitor visitor) { visitor.visitCompilationUnit(this); }
    }

    public static abstract class Statement extends Tree {
        
        protected Statement(int pos) {
            super(pos);
        }
    }

    public static class ConstDef extends Tree {

        public static class Definition {

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

    public static class FuncDef extends Tree {

        public static class Parameter {

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

        public Code code;

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

    public static class Block extends Statement {

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

    public static class If extends Statement {

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

    public static class WhileLoop extends Statement {

        public Expression cond;

        public Statement body;

        /** Возможно ли выполнение кода после данного цикла */
        public boolean _infinite; // helpful compiler flag

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

    public static class DoLoop extends Statement {

        public Statement body;

        public Expression cond;

        /** Возможно ли выполнение кода после данного цикла */
        public boolean _infinite; // helpful compiler flag

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

    public static class ForLoop extends Statement {

        public List<Expression> init;

        public Expression cond;

        public List<Expression> step;

        public Statement body;

        /** Возможно ли выполнение кода после данного цикла */
        public boolean _infinite; // helpful compiler flag

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
        public void accept(Visitor visitor) { visitor.visitForLoop(this); }
    }

    public static class Switch extends Statement {

        public Expression expr;

        public List<Case> cases;
        
        /** Возможно ли выполнение кода после данного {@code switch}. */
        public boolean _final; // helpful compiler flag

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

    public static class Case extends Statement {

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

    public static class Break extends Statement {

        public Break(int pos) {
            super(pos);
        }

        @Override
        public Tag getTag() { return Tag.BREAK; }

        @Override
        public void accept(Visitor visitor) { visitor.visitBreak(this); }
    }

    public static class Continue extends Statement {

        public Continue(int pos) {
            super(pos);
        }

        @Override
        public Tag getTag() { return Tag.CONTINUE; }

        @Override
        public void accept(Visitor visitor) { visitor.visitContinue(this); }
    }

    public static class Fallthrough extends Statement {

        public Fallthrough(int pos) {
            super(pos);
        }

        @Override
        public Tag getTag() { return Tag.FALLTHROUGH; }

        @Override
        public void accept(Visitor visitor) { visitor.visitFallthrough(this); }
    }

    public static class Return extends Statement {

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

    public static class Discarded extends Statement {

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

    public static abstract class Expression extends Tree {

        protected Expression(int pos) {
            super(pos);
        }
    }

    public static class Literal extends Expression {

        public final Type type;

        public Literal(int pos, Type type) {
            super(pos);
            this.type = type;
        }

        @Override
        public Tag getTag() { return Tag.LITERAL; }

        @Override
        public void accept(Visitor visitor) { visitor.visitLiteral(this); }
    }

    public static class ArrayLiteral extends Expression {

        public static class Entry {

            public final int pos;

            public Expression key, value;

            public Entry(int pos, Expression key, Expression value) {
                this.pos = pos;
                this.key = key;
                this.value = value;
            }
        }
        
        public List<Entry> entries;

        /** Является ли массив списком */
        public boolean _isList = false; // helpful compiler analysis flag

        public ArrayLiteral(int pos, List<Entry> entries) {
            super(pos);
            this.entries = entries;
        }

        @Override
        public Tag getTag() { return Tag.ARRAYLITERAL; }

        @Override
        public void accept(Visitor visitor) { visitor.visitArrayLiteral(this); }
    }

    public static class Var extends Expression {

        public final Name name;

        /** Уверены ли мы в том, что переменная была явно определена. */
        public boolean _defined; // helpful compiler flag

        public Var(int pos, Name name) {
            super(pos);
            this.name = name;
        }

        @Override
        public Tag getTag() { return Tag.VARIABLE; }

        @Override
        public void accept(Visitor visitor) { visitor.visitVariable(this); }
    }

    public static class MemberAccess extends Expression {

        public Expression expr;

        public Name member;

        public MemberAccess(int pos, Expression expr, Name member) {
            super(pos);
            this.expr = expr;
            this.member = member;
        }

        @Override
        public Tag getTag() { return Tag.MEMACCESS; }

        @Override
        public void accept(Visitor visitor) { visitor.visitMemberAccess(this); }
    }

    public static class ArrayAccess extends Expression {

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

    public static class Invocation extends Expression {

        public static class Argument {

            public final Name name; // todo: Значение этого поля всегда null, потому что не реализовано.

            public Expression expr;

            public Argument(Name name, Expression expr) {
                this.name = name;
                this.expr = expr;
            }
        }
        
        public final Expression callee;

        public List<Argument> args;

        public Invocation(int pos, Expression callee, List<Argument> args) {
            super(pos);
            this.callee = callee;
            this.args = args;
        }

        @Override
        public Tag getTag() { return Tag.INVOCATION; }

        @Override
        public void accept(Visitor visitor) { visitor.visitInvocation(this); }
    }

    public static class Parens extends Expression {

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

    public static class Assign extends Expression {

        public Expression var, expr;

        public Assign(int pos, Expression var, Expression expr) {
            super(pos);
            this.var = var;
            this.expr = expr;
        }

        @Override
        public Tag getTag() { return Tag.ASSIGN; }

        @Override
        public void accept(Visitor visitor) { visitor.visitAssign(this); }
    }

    public static class CompoundAssign extends Expression {

        public final Tag tag;

        public Expression var, expr; // todo: Переименовать в var, expr соответственно.

        public CompoundAssign(int pos, Tag tag, Expression var, Expression expr) {
            super(pos);
            this.tag = tag;
            this.var = var;
            this.expr = expr;
        }

        @Override
        public Tag getTag() { return tag; }

        @Override
        public void accept(Visitor visitor) { visitor.visitCompoundAssign(this); }
    }

    public static class TernaryOp extends Expression {

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

    public static class BinaryOp extends Expression {

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

    public static class UnaryOp extends Expression {

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
