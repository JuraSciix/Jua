package jua.compiler;

import jua.compiler.ModuleScope.FunctionSymbol;
import jua.compiler.ModuleScope.VarSymbol;
import jua.compiler.utils.Flow;

public abstract class Tree {

    public enum Tag {
        EVACUATE,
        DOC,
        FUNCDEF,
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
        VARDEF,
        RETURN,
        DISCARDED,
        LITERAL,
        LISTLIT,
        VAR,
        MEMACCESS,
        ARRACC,
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
        ASG_BIT_AND,
        ASG_BIT_OR,
        ASG_BIT_XOR,
        ASG_COALESCE,
        TERNARY,
        COALESCE,
        OR,
        AND,
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
        BIT_AND,
        BIT_OR,
        BIT_XOR,
        NEG,
        POS,
        NOT,
        BIT_INV,
        PREINC,
        PREDEC,
        POSTINC,
        POSTDEC,
        NULLCHK
    }

    public interface Visitor {
        void visitDocument(Document tree);
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
        void visitVarDef(VarDef tree);
        void visitReturn(Return tree);
        void visitDiscarded(Discarded tree);
        void visitLiteral(Literal tree);
        void visitListLiteral(ListLiteral tree);
        void visitVariable(Var tree);
        void visitMemberAccess(MemberAccess tree);
        void visitArrayAccess(Access tree);
        void visitInvocation(Invocation tree);
        void visitParens(Parens tree);
        void visitAssign(Assign tree);
        void visitEnhancedAssign(EnhancedAssign tree);
        void visitConditional(Conditional tree);
        void visitBinaryOp(BinaryOp tree);
        void visitUnaryOp(UnaryOp tree);
    }

    public static abstract class AbstractVisitor implements Visitor {
        @Override
        public void visitDocument(Document tree) { visitTree(tree); }

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
        public void visitVarDef(VarDef tree) { visitTree(tree); }

        @Override
        public void visitReturn(Return tree) { visitTree(tree); }

        @Override
        public void visitDiscarded(Discarded tree) { visitTree(tree); }

        @Override
        public void visitLiteral(Literal tree) { visitTree(tree); }

        @Override
        public void visitListLiteral(ListLiteral tree) { visitTree(tree); }

        @Override
        public void visitVariable(Var tree) { visitTree(tree); }

        @Override
        public void visitMemberAccess(MemberAccess tree) { visitTree(tree); }

        @Override
        public void visitArrayAccess(Access tree) { visitTree(tree); }

        @Override
        public void visitInvocation(Invocation tree) { visitTree(tree); }

        @Override
        public void visitParens(Parens tree) { visitTree(tree); }

        @Override
        public void visitAssign(Assign tree) { visitTree(tree); }

        @Override
        public void visitEnhancedAssign(EnhancedAssign tree) { visitTree(tree); }

        @Override
        public void visitConditional(Conditional tree) { visitTree(tree); }

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

        public void scan(Flow<? extends Tree> flow) {
            Flow.forEach(flow, this::scan);
        }

        @Override
        public void visitDocument(Document tree) {
            scan(tree.functions);
            scan(tree.stats);
        }

        @Override
        public void visitFuncDef(FuncDef tree) {
            Flow.forEach(tree.params, param -> {
                if (param.expr != null) {
                    scan(param.expr);
                }
            });
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
        public void visitVarDef(VarDef tree) {
            Flow.forEach(tree.defs, def -> {
                if (def.init != null) {
                    scan(def.init);
                }
            });
        }

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
        public void visitListLiteral(ListLiteral tree) {
            scan(tree.entries);
        }

        @Override
        public void visitVariable(Var tree) {  }

        @Override
        public void visitMemberAccess(MemberAccess tree) {
            scan(tree.expr);
        }

        @Override
        public void visitArrayAccess(Access tree) {
            scan(tree.expr);
            scan(tree.index);
        }

        @Override
        public void visitInvocation(Invocation tree) {
            Flow.forEach(tree.args, a -> scan(a.expr));
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
        public void visitEnhancedAssign(EnhancedAssign tree) {
            scan(tree.expr);
            scan(tree.var);
        }

        @Override
        public void visitConditional(Conditional tree) {
            scan(tree.cond);
            scan(tree.ths);
            scan(tree.fhs);
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

        public <T extends Tree> Flow<T> translate(Flow<T> flow) {
            if (flow != null) {
                Flow.translate(flow, this::translate);
            }
            return flow;
        }

        @Override
        public void visitDocument(Document tree) {
            tree.functions = translate(tree.functions);
            tree.stats = translate(tree.stats);
            result = tree;
        }

        @Override
        public void visitFuncDef(FuncDef tree) {
            Flow.forEach(tree.params, param -> {
                if (param.expr != null) {
                    param.expr = translate(param.expr);
                }
            });
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
        public void visitVarDef(VarDef tree) {
            Flow.forEach(tree.defs, def -> {
                if (def.init != null) {
                    def.init = translate(def.init);
                }
            });
            result = tree;
        }

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
        public void visitListLiteral(ListLiteral tree) {
            tree.entries = translate(tree.entries);
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
        public void visitArrayAccess(Access tree) {
            tree.expr = translate(tree.expr);
            tree.index = translate(tree.index);
            result = tree;
        }

        @Override
        public void visitInvocation(Invocation tree) {
            Flow.forEach(tree.args, a -> a.expr = translate(a.expr));
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
        public void visitEnhancedAssign(EnhancedAssign tree) {
            tree.var = translate(tree.var);
            tree.expr = translate(tree.expr);
            result = tree;
        }

        @Override
        public void visitConditional(Conditional tree) {
            tree.cond = translate(tree.cond);
            tree.ths = translate(tree.ths);
            tree.fhs = translate(tree.fhs);
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

    public boolean hasTag(Tag tag) { return getTag() == tag; }

    public static class Document extends Tree {

        public final Source source;

        @Deprecated
        public Flow<Stmt> stats;

        public Flow<FuncDef> functions;

        public Document(int pos, Source source,
                        Flow<FuncDef> functions,
                        Flow<Stmt> stats) {
            super(pos);
            this.source = source;
            this.functions = functions;
            this.stats = stats;
        }

        @Override
        public Tag getTag() { return Tag.DOC; }

        @Override
        public void accept(Visitor visitor) { visitor.visitDocument(this); }
    }

    public static abstract class Stmt extends Tree {
        
        protected Stmt(int pos) {
            super(pos);
        }
    }

    public static class FuncDef extends Tree {

        public static class Parameter {

            public final int pos;
            public final String name;

            public Expr expr;

            public VarSymbol sym;

            public Parameter(int pos, String name, Expr expr) {
                this.pos = pos;
                this.name = name;
                this.expr = expr;
            }
        }

        public final int namePos;

        public final String name;

        public Flow<Parameter> params;

        public Stmt body;

        public FunctionSymbol sym;

        public FuncDef(int pos, int namePos, String name, Flow<Parameter> params, Stmt body) {
            super(pos);
            this.namePos = namePos;
            this.name = name;
            this.params = params;
            this.body = body;
        }

        @Override
        public Tag getTag() { return Tag.FUNCDEF; }

        @Override
        public void accept(Visitor visitor) { visitor.visitFuncDef(this); }
    }

    public static class Block extends Stmt {

        public Flow<Stmt> stats;

        public Block(int pos, Flow<Stmt> stats) {
            super(pos);
            this.stats = stats;
        }

        @Override
        public Tag getTag() { return Tag.BLOCK; }

        @Override
        public void accept(Visitor visitor) { visitor.visitBlock(this); }
    }

    public static class If extends Stmt {

        public Expr cond;

        public Stmt thenbody;

        public Stmt elsebody;

        public If(int pos, Expr cond, Stmt thenbody, Stmt elsebody) {
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

    public static class WhileLoop extends Stmt {

        public Expr cond;

        public Stmt body;

        public WhileLoop(int pos, Expr cond, Stmt body) {
            super(pos);
            this.cond = cond;
            this.body = body;
        }

        @Override
        public Tag getTag() { return Tag.WHILELOOP; }

        @Override
        public void accept(Visitor visitor) { visitor.visitWhileLoop(this); }
    }

    public static class DoLoop extends Stmt {

        public Stmt body;

        public Expr cond;

        public DoLoop(int pos, Stmt body, Expr cond) {
            super(pos);
            this.body = body;
            this.cond = cond;
        }

        @Override
        public Tag getTag() { return Tag.DOLOOP; }

        @Override
        public void accept(Visitor visitor) { visitor.visitDoLoop(this); }
    }

    public static class ForLoop extends Stmt {

        public Flow<Stmt> init;

        public Expr cond;

        public Flow<Expr> step;

        public Stmt body;

        public ForLoop(int pos, Flow<Stmt> init, Expr cond, Flow<Expr> step, Stmt body) {
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

    public static class Switch extends Stmt {

        public Expr expr;

        public Flow<Case> cases;

        public Switch(int pos, Expr expr, Flow<Case> cases) {
            super(pos);
            this.expr = expr;
            this.cases = cases;
        }

        @Override
        public Tag getTag() { return Tag.SWITCH; }

        @Override
        public void accept(Visitor visitor) { visitor.visitSwitch(this); }
    }

    public static class Case extends Stmt {

        public Flow<Expr> labels;

        public Stmt body;

        public Case(int pos, Flow<Expr> labels, Stmt body) {
            super(pos);
            this.labels = labels;
            this.body = body;
        }

        @Override
        public Tag getTag() { return Tag.CASE; }

        @Override
        public void accept(Visitor visitor) { visitor.visitCase(this); }
    }

    public static class Break extends Stmt {

        public Break(int pos) {
            super(pos);
        }

        @Override
        public Tag getTag() { return Tag.BREAK; }

        @Override
        public void accept(Visitor visitor) { visitor.visitBreak(this); }
    }

    public static class Continue extends Stmt {

        public Continue(int pos) {
            super(pos);
        }

        @Override
        public Tag getTag() { return Tag.CONTINUE; }

        @Override
        public void accept(Visitor visitor) { visitor.visitContinue(this); }
    }

    public static class Fallthrough extends Stmt {

        public Fallthrough(int pos) {
            super(pos);
        }

        @Override
        public Tag getTag() { return Tag.FALLTHROUGH; }

        @Override
        public void accept(Visitor visitor) { visitor.visitFallthrough(this); }
    }

    public static class VarDef extends Stmt {

        public static class Definition {

            public final int pos;

            public final String name;

            public Expr init;

            public VarSymbol sym;

            public Definition(int pos, String name, Expr init) {
                this.pos = pos;
                this.name = name;
                this.init = init;
            }
        }

        public Flow<Definition> defs;

        public VarDef(int pos, Flow<Definition> defs) {
            super(pos);
            this.defs = defs;
        }

        @Override
        public Tag getTag() { return Tag.VARDEF; }

        @Override
        public void accept(Visitor visitor) { visitor.visitVarDef(this); }
    }

    public static class Return extends Stmt {

        public Expr expr;

        public Return(int pos, Expr expr) {
            super(pos);
            this.expr = expr;
        }

        @Override
        public Tag getTag() { return Tag.RETURN; }

        @Override
        public void accept(Visitor visitor) { visitor.visitReturn(this); }
    }

    public static class Discarded extends Stmt {

        public Expr expr;

        public Discarded(int pos, Expr expr) {
            super(pos);
            this.expr = expr;
        }

        @Override
        public Tag getTag() { return Tag.DISCARDED; }

        @Override
        public void accept(Visitor visitor) { visitor.visitDiscarded(this); }
    }

    public static abstract class Expr extends Tree {

        protected Expr(int pos) {
            super(pos);
        }
    }

    public static class Literal extends Expr {

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

    public static class ListLiteral extends Expr {

        public Flow<Expr> entries;

        public ListLiteral(int pos, Flow<Expr> entries) {
            super(pos);
            this.entries = entries;
        }

        @Override
        public Tag getTag() { return Tag.LISTLIT; }

        @Override
        public void accept(Visitor visitor) { visitor.visitListLiteral(this); }
    }

    public static class Var extends Expr {

        public final String name;

        public VarSymbol sym;

        public Var(int pos, String name) {
            super(pos);
            this.name = name;
        }

        @Override
        public Tag getTag() { return Tag.VAR; }

        @Override
        public void accept(Visitor visitor) { visitor.visitVariable(this); }
    }

    @Deprecated
    public static class MemberAccess extends Expr {

        public final Tag tag;

        public Expr expr;

        public int memberPos;

        public String member;

        public MemberAccess(int pos, Tag tag, Expr expr, int memberPos, String member) {
            super(pos);
            this.tag = tag;
            this.expr = expr;
            this.memberPos = memberPos;
            this.member = member;
        }

        @Override
        public Tag getTag() { return tag; }

        @Override
        public void accept(Visitor visitor) { visitor.visitMemberAccess(this); }
    }

    public static class Access extends Expr {

        public Expr expr, index;

        public Access(int pos, Expr expr, Expr index) {
            super(pos);
            this.expr = expr;
            this.index = index;
        }

        @Override
        public Tag getTag() { return Tag.ARRACC; }

        @Override
        public void accept(Visitor visitor) { visitor.visitArrayAccess(this); }
    }

    public static class Invocation extends Expr {

        public static class Argument {

            public final int pos;

            public final String name;

            public Expr expr;

            public Argument(int pos, String name, Expr expr) {
                this.pos = pos;
                this.name = name;
                this.expr = expr;
            }
        }
        
        public final Expr target;

        public Flow<Argument> args;

        public FunctionSymbol sym;

        public Invocation(int pos, Expr target, Flow<Argument> args) {
            super(pos);
            this.target = target;
            this.args = args;
        }

        @Override
        public Tag getTag() { return Tag.INVOCATION; }

        @Override
        public void accept(Visitor visitor) { visitor.visitInvocation(this); }
    }

    public static class Parens extends Expr {

        public Expr expr;

        public Parens(int pos, Expr expr) {
            super(pos);
            this.expr = expr;
        }

        @Override
        public Tag getTag() { return Tag.PARENS; }

        @Override
        public void accept(Visitor visitor) { visitor.visitParens(this); }
    }

    public static class Assign extends Expr {

        public Expr var, expr;

        public Assign(int pos, Expr var, Expr expr) {
            super(pos);
            this.var = var;
            this.expr = expr;
        }

        @Override
        public Tag getTag() { return Tag.ASSIGN; }

        @Override
        public void accept(Visitor visitor) { visitor.visitAssign(this); }
    }

    public static class EnhancedAssign extends Expr {

        public final Tag tag;

        public Expr var, expr;

        public EnhancedAssign(int pos, Tag tag, Expr var, Expr expr) {
            super(pos);
            this.tag = tag;
            this.var = var;
            this.expr = expr;
        }

        @Override
        public Tag getTag() { return tag; }

        @Override
        public void accept(Visitor visitor) { visitor.visitEnhancedAssign(this); }
    }

    public static class Conditional extends Expr {

        public Expr cond, ths, fhs;

        public Conditional(int pos, Expr cond, Expr ths, Expr fhs) {
            super(pos);
            this.cond = cond;
            this.ths = ths;
            this.fhs = fhs;
        }

        @Override
        public Tag getTag() { return Tag.TERNARY; }

        @Override
        public void accept(Visitor visitor) { visitor.visitConditional(this); }
    }

    public static class BinaryOp extends Expr {

        public final Tag tag;

        public Expr lhs, rhs;

        public BinaryOp(int pos, Tag tag, Expr lhs, Expr rhs) {
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

    public static class UnaryOp extends Expr {

        public final Tag tag;

        public Expr expr;

        public UnaryOp(int pos, Tag tag, Expr expr) {
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
