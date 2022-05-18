package jua.compiler;

import jua.compiler.Tokens.Token;
import jua.util.Pair;
import jua.util.Source;

import java.util.List;

public abstract class Tree {

    public final int pos;

    protected Tree(int pos) {
        this.pos = pos;
    }

    public final boolean hasTag(Tag tag) {
        return getTag() == tag;
    }

    public abstract Tag getTag();

    public abstract void accept(Visitor visitor);

    public static class TreeTop extends Tree {

        public final Source source;

        public final List<? extends Tree> trees;

        public TreeTop(int pos, Source source, List<? extends Tree> trees) {
            super(pos);
            this.source = source;
            this.trees = trees;
        }

        @Override
        public Tag getTag() { return Tag.TOP; }

        @Override
        public void accept(Visitor visitor) { visitor.visitTop(this); }
    }

    public abstract static class Statement extends Tree {

        protected Statement(int pos) { 
            super(pos); 
        }
    }

    public abstract static class Expression extends Statement {

        protected Expression(int pos) {
            super(pos);
        }
    }

    public static class Block extends Statement {

        public final List<Statement> stats;

        public Block(int pos, List<Statement> stats) {
            super(pos);
            this.stats = stats;
        }

        @Override
        public Tag getTag() { return Tag.BLOCK; }

        @Override
        public void accept(Visitor visitor) { visitor.visitBlock(this); }
    }

    public static class FunctionDecl extends Statement {

        public final int mods;

        public final Token head;

        public final List<Pair<Token, Expression>> params;

        public Statement body;

        public FunctionDecl(int pos, int mods, Token head, List<Pair<Token, Expression>> params, Statement body) {
            super(pos);
            this.mods = mods;
            this.head = head;
            this.params = params;
            this.body = body;
        }

        @Override
        public Tag getTag() { return Tag.FUNCTION_DECL; }

        @Override
        public void accept(Visitor visitor) { visitor.visitFunctionDecl(this); }
    }

    public static class ConstantDecl extends Statement {

        public final List<Pair<Token, Expression>> decls;

        public ConstantDecl(int pos, List<Pair<Token, Expression>> decls) {
            super(pos);
            this.decls = decls;
        }

        @Override
        public Tag getTag() { return Tag.CONSTANT_DECL; }

        @Override
        public void accept(Visitor visitor) { visitor.visitConstantDecl(this); }
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
        public Tag getTag() {
            return tag;
        }

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
        public Tag getTag() {
            return tag;
        }

        @Override
        public void accept(Visitor visitor) { visitor.visitUnaryOp(this); }
    }

    public static class TernaryOp extends Expression {

        public Expression cond, a, b;

        public TernaryOp(int pos, Expression cond, Expression a, Expression b) {
            super(pos);
            this.cond = cond;
            this.a = a;
            this.b = b;
        }

        @Override
        public Tag getTag() { return Tag.TERNARY; }

        @Override
        public void accept(Visitor visitor) { visitor.visitTernaryOp(this); }
    }

    public static class Return extends Statement {

        public Expression result;

        public Return(int pos, Expression result) {
            super(pos);
            this.result = result;
        }

        @Override
        public Tag getTag() { return Tag.RETURN; }

        @Override
        public void accept(Visitor visitor) { visitor.visitReturn(this); }
    }

    public static class Switch extends Statement {

        public Expression sel;

        public final List<Case> cases;

        public Switch(int pos, Expression sel, List<Case> cases) {
            super(pos);
            this.sel = sel;
            this.cases = cases;
        }

        @Override
        public Tag getTag() { return Tag.CASE; }

        @Override
        public void accept(Visitor visitor) { visitor.visitSwitch(this); }
    }

    public static class Case extends Statement {

        public final List<Expression> labels;

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

    public static class ForLoop extends Statement {

        public final List<Expression> init;

        public Expression cond;

        public final List<Expression> step;

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
        public void accept(Visitor visitor) { visitor.visitForLoop(this); }
    }

    public static class ForeachLoop extends Statement {

        public Expression key, value, expr;

        public Statement body;

        public ForeachLoop(int pos, Expression key, Expression value, Expression expr, Statement body) {
            super(pos);
            this.key = key;
            this.value = value;
            this.expr = expr;
            this.body = body;
        }

        @Override
        public Tag getTag() { return Tag.FOREACHLOOP; }

        @Override
        public void accept(Visitor visitor) { visitor.visitForeachLoop(this); }
    }

    public static class DoLoop extends Statement {

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

    public static class WhileLoop extends Statement {

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

    public static class Var extends Expression {

        public final String name;

        public Var(int pos, String name) {
            super(pos);
            this.name = name;
        }

        @Override
        public Tag getTag() { return Tag.VARIABLE; }

        @Override
        public void accept(Visitor visitor) { visitor.visitVariable(this); }
    }

    public static class Literal extends Expression {

        public final Object value;

        public Literal(int pos, Object value) {
            super(pos);
            this.value = value;
        }

        @Override
        public Tag getTag() { return Tag.LITERAL; }

        @Override
        public void accept(Visitor visitor) { visitor.visitLiteral(this); }
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

    public static class ArrayAccess extends Expression {

        public Expression array, index;

        public final boolean nullSafe;

        public ArrayAccess(int pos, Expression array, Expression index, boolean nullSafe) {
            super(pos);
            this.array = array;
            this.index = index;
            this.nullSafe = nullSafe;
        }

        @Override
        public Tag getTag() { return Tag.ARRAYACCESS; }

        @Override
        public void accept(Visitor visitor) { visitor.visitArrayAccess(this); }
    }

    public static class ArrayLiteral extends Expression {

        public final List<Pair<Expression, Expression>> content;

        public ArrayLiteral(int pos, List<Pair<Expression, Expression>> content) {
            super(pos);
            this.content = content;
        }

        @Override
        public Tag getTag() { return Tag.ARRAYLITERAL; }

        @Override
        public void accept(Visitor visitor) { visitor.visitArrayLiteral(this); }
    }

    public static class Invocation extends Expression {

        public final Var target;

        public final List<Expression> args;

        public Invocation(int pos, Var target, List<Expression> args) {
            super(pos);
            this.target = target;
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

        public final Tag tag;

        public Expression a, b;

        public Assign(int pos, Tag tag, Expression a, Expression b) {
            super(pos);
            this.tag = tag;
            this.a = a;
            this.b = b;
        }

        @Override
        public Tag getTag() {
            return tag;
        }

        @Override
        public void accept(Visitor visitor) { visitor.visitAssign(this); }
    }

    public static class If extends Statement {

        public Expression cond;

        public Statement body, elseBody;

        public If(int pos, Expression cond, Statement body, Statement elseBody) {
            super(pos);
            this.cond = cond;
            this.body = body;
            this.elseBody = elseBody;
        }

        @Override
        public Tag getTag() { return Tag.IF; }

        @Override
        public void accept(Visitor visitor) { visitor.visitIf(this); }
    }

    public static class Decl extends Statement {

        public String name;

        public Expression init;

        public Decl(int pos, String name, Expression init) {
            super(pos);
            this.name = name;
            this.init = init;
        }

        @Override
        public Tag getTag() { return Tag.DECL; }

        @Override
        public void accept(Visitor visitor) { visitor.visitDeclaration(this); }
    }

    public enum Tag {
        TOP,
        BLOCK,
        FUNCTION_DECL,
        CONSTANT_DECL,
        ADD,
        SUB,
        MUL,
        DIV,
        REM,
        SL,
        SR,
        AND,
        OR,
        BITAND,
        BITOR,
        BITXOR,
        PREINC,
        PREDEC,
        POSTINC,
        POSTDEC,
        NEG,
        POS,
        BITNOT,
        TERNARY,
        RETURN,
        SWITCH,
        CASE,
        FORLOOP,
        FOREACHLOOP,
        DOLOOP,
        WHILELOOP,
        LITERAL,
        VARIABLE,
        BREAK,
        CONTINUE,
        FALLTHROUGH,
        ARRAYACCESS,
        ARRAYLITERAL,
        INVOCATION,
        PARENS,
        ASSIGNMENT,
        IF,
        DECL
    }

    public interface Visitor {
        void visitTop(TreeTop tree);
        void visitBlock(Block tree);
        void visitFunctionDecl(FunctionDecl tree);
        void visitConstantDecl(ConstantDecl tree);
        void visitIf(If tree);
        void visitWhileLoop(WhileLoop tree);
        void visitForLoop(ForLoop tree);
        void visitDoLoop(DoLoop tree);
        void visitForeachLoop(ForeachLoop tree);
        void visitSwitch(Switch tree);
        void visitCase(Case tree);
        void visitBreak(Break tree);
        void visitContinue(Continue tree);
        void visitFallthrough(Fallthrough tree);
        void visitReturn(Return tree);
        void visitVariable(Var tree);
        void visitLiteral(Literal tree);
        void visitAssign(Assign tree);
        void visitBinaryOp(BinaryOp tree);
        void visitUnaryOp(UnaryOp tree);
        void visitTernaryOp(TernaryOp tree);
        void visitArrayAccess(ArrayAccess tree);
        void visitArrayLiteral(ArrayLiteral tree);
        void visitInvocation(Invocation tree);
        void visitParens(Parens tree);
        void visitDeclaration(Decl tree);
        void visitTree(Tree tree);
    }
}
