package jua.compiler;

import jua.util.LineMap;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class Tree {

    public enum Tag {
        ROOT,
        FUNCDECL,
        CONSTDECL,
        BLOCK,
        IFELSE,
        WHILELOOP,
        DOLOOP,
        FORLOOP,
        FOREACHLOOP,
        SWITCH,
        CASE,
        BREAK,
        CONTINUE,
        FALLTHROUGH,
        RETURN,
        EMPTY,
        DISCARDED,
        ADD,
        SUB,
        MUL,
        DIV,
        REM,
        SL,
        SR,
        BITAND,
        BITOR,
        BITXOR,
        EQ,
        NEQ,
        GT,
        GE,
        LT,
        LE,
        LOGOR,
        LOGAND,
        NULLCOALESCE,
        ASG,
        ASG_ADD,
        ASG_SUB,
        ASG_MUL,
        ASG_DIV,
        ASG_REM,
        ASG_SL,
        ASG_SR,
        ASG_BITAND,
        ASG_BITOR,
        ASG_BITXOR,
        ASG_NULLCOALESCE,
        POS,
        NEG,
        LOGCMPL,
        BITCMPL,
        PRE_INC,
        POST_INC,
        PRE_DEC,
        POST_DEC,
        ARRAY_ACCESS,
        CLONE,
        TERNARY,
        LITERAL,
        ARRAY_LITERAL,
        VARIABLE,
        FUNC_CALL,
        PARENS,
        ERROR,

        // временные теги
        @Deprecated
        PRINT,
        @Deprecated
        PRINTLN
    }

    public int pos;

    public abstract Tag getTag();

    protected Tree(int pos) {
        this.pos = pos;
    }

    public final boolean hasTag(Tag t) { return getTag() == t; }

    public abstract void accept(Visitor visitor);

    public static class CompilationUnit extends Tree {

        public URL location;

        public LineMap lineMap;

        public List<Tree> trees;

        public CompilationUnit(int pos, URL location, LineMap lineMap, List<Tree> trees) {
            super(pos); // todo: У CompilationUnit не должно быть позиции.
            this.location = location;
            this.lineMap = lineMap;
            this.trees = trees;
        }

        @Override
        public Tag getTag() {
            return Tag.ROOT;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitCompilationUnit(this);
        }
    }

    public static class ArrayAccess extends Expression {

        public Expression array;
        public Expression key;

        public ArrayAccess(int pos, Expression hs, Expression key) {
            super(pos);
            this.array = hs;
            this.key = key;
        }

        @Override
        public Tag getTag() {
            return Tag.ARRAY_ACCESS;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitArrayAccess(this);
        }
    }

    public static class ArrayLiteral extends Expression {

        public List<ArrayEntry> entries;

        public ArrayLiteral(int pos, List<ArrayEntry> entries) {
            super(pos);
            this.entries = entries;
        }

        @Override
        public boolean isAccessible() {
            return true;
        }

        @Override
        public boolean isCloneable() {
            return true;
        }

        @Override
        public Tag getTag() {
            return Tag.ARRAY_LITERAL;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitArray(this);
        }
    }

    public static class AssignOp extends Expression {

        public Tag tag;

        public Expression var;

        public Expression expr;

        public AssignOp(int pos, Tag tag, Expression var, Expression expr) {
            super(pos);
            this.tag = tag;
            this.var = var;
            this.expr = expr;
        }

        @Override
        public Tag getTag() {
            return tag;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitAssignOp(this);
        }

        @Override
        public boolean isAccessible() {
            return expr.isAccessible();
        }

        @Override
        public boolean isCloneable() {
            return expr.isCloneable();
        }

        @Override
        public boolean isNullable() {
            return expr.isNullable();
        }
    }

    public static class BinaryOp extends Expression {

        public Tag tag;

        public Expression lhs;

        public Expression rhs;

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
        public void accept(Visitor visitor) {
            visitor.visitBinaryOp(this);
        }
    }

    public static class Block extends Statement {

        public List<Statement> statements;

        public Block(int pos, List<Statement> statements) {
            super(pos);
            this.statements = statements;
        }

        @Override
        public Tag getTag() {
            return Tag.BLOCK;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitBlock(this);
        }
    }

    public static class Break extends Statement {

        @Override
        public Tag getTag() {
            return Tag.BREAK;
        }

        public Break(int pos) {
            super(pos);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitBreak(this);
        }
    }

    public static class Case extends Statement {

        public List<Expression> expressions;

        public Statement body;

        public Case(int pos, List<Expression> expressions, Statement body) {
            super(pos);
            this.expressions = expressions;
            this.body = body;
        }

        @Override
        public Tag getTag() {
            return Tag.CASE;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitCase(this);
        }
    }

    public static class ConstantDecl extends Statement {

        public List<Definition> definitions;

        public ConstantDecl(int pos, List<Definition> definitions) {
            super(pos);
            this.definitions = definitions;
        }

        @Override
        public Tag getTag() {
            return Tag.CONSTDECL;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitConstantDeclare(this);
        }
    }

    public static class Continue extends Statement {

        @Override
        public Tag getTag() {
            return Tag.CONTINUE;
        }

        public Continue(int pos) {
            super(pos);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitContinue(this);
        }
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
        public Tag getTag() {
            return Tag.DOLOOP;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitDoLoop(this);
        }
    }

    public abstract static class Expression extends Statement {

        // todo: remove
        public static Expression empty() {
            return new Expression(0) {
                @Override
                public Tag getTag() {
                    return null;
                }

                @Override
                public boolean isEmpty() {
                    return true;
                }

                @Override
                public void accept(Visitor visitor) {}
            };
        }

        protected Expression(int pos) {
            super(pos);
        }

        // todo: Почти все эти методы лишние, часть из них нужно переместить в jua.compiler.TreeInfo

        public boolean isAccessible() {
            switch (getTag()) {
                case PARENS:
                case VARIABLE:
                case ARRAY_LITERAL:
                case ARRAY_ACCESS:
                case FUNC_CALL:
                    return true;
                default:
                    return false;
            }
        }

        public boolean isAssignable() {
            switch (getTag()) {
                case PARENS:
                    return ((Parens) this).expr.isAssignable();
                case VARIABLE:
                case ARRAY_ACCESS:
                    return true;
                default:
                    return false;
            }
        }

        public boolean isCloneable() {
            return true;
        }

        public boolean isLiteral() {
            return false;
        }

        public boolean isEmpty() {
            return false;
        }

        public boolean isNullable() {
            return false;
        }

        public Expression child() {
            return this;
        }
    }

    public static class Fallthrough extends Statement {

        @Override
        public Tag getTag() {
            return Tag.FALLTHROUGH;
        }

        public Fallthrough(int pos) {
            super(pos);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitFallthrough(this);
        }
    }

    public static class ForLoop extends Statement {

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
        public Tag getTag() {
            return Tag.FORLOOP;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitFor(this);
        }
    }

    public static class Invocation extends Expression {

        public final Name name;

        public List<Argument> args;

        public Invocation(int pos, Name name, List<Argument> args) {
            super(pos);
            this.name = name;
            this.args = args;
        }

        @Override
        public boolean isAccessible() {
            return true;
        }

        @Override
        public boolean isCloneable() {
            return true;
        }

        @Override
        public boolean isNullable() {
            return true;
        }

        @Override
        public Tag getTag() {
            return Tag.FUNC_CALL;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitInvocation(this);
        }
    }

    public static class FunctionDecl extends Statement {

        // todo: Заменить строки на свои структуры (механизм уже готов, его нужно только внедрить)

        public final String name;

        public final List<String> params;

        public List<Expression> defaults;

        public Statement body;

        public FunctionDecl(int pos,
                            String name,
                            List<String> params,
                            List<Expression> defaults,
                            Statement body) {
            super(pos);
            this.name = name;
            this.params = params;
            this.defaults = defaults;
            this.body = body;
        }

        @Override
        public Tag getTag() {
            return Tag.FUNCDECL;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitFunctionDecl(this);
        }
    }

    public static class If extends Statement {

        public Expression cond;

        public Statement body;

        public Statement elseBody;

        public If(int pos, Expression cond, Statement body) {
            this(pos, cond, body, null);
        }

        public If(int pos, Expression cond, Statement body, Statement elseBody) {
            super(pos);
            this.cond = cond;
            this.body = body;
            this.elseBody = elseBody;
        }

        @Override
        public Tag getTag() {
            return Tag.IFELSE;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitIf(this);
        }
    }

    public static class Literal extends Expression {

        public Object value;

        public Literal(int pos, Object value) {
            super(pos);
            this.value = value;
        }

        public boolean isInteger() { return value instanceof Long || value instanceof Integer; }
        public boolean isFloatingPoint() { return value instanceof Double || value instanceof Float; }
        public boolean isNumber() { return value instanceof Number; }
        public boolean isBoolean() { return value instanceof Boolean; }
        public boolean isString() { return value instanceof String; }
        public boolean isNull() { return value == null; }
        public long longValue() { return ((Number) value).longValue(); }
        public double doubleValue() { return ((Number) value).doubleValue(); }
        public boolean booleanValue() { return (Boolean) value; }
        public String stringValue() { return String.valueOf(value); }

        @Override
        public boolean isLiteral() {
            return true;
        }

        @Override
        public Tag getTag() {
            return Tag.LITERAL;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitLiteral(this);
        }
    }

    public static class Parens extends Expression {

        public Expression expr;

        public Parens(int pos, Expression expr) {
            super(pos);
            this.expr = expr;
        }

        @Override
        public Expression child() {
            return expr.child();
        }

        @Override
        public Tag getTag() {
            return Tag.PARENS;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitParens(this);
        }
    }

    @Deprecated
    public static class Position {

        public final String filename;

        public final int line;

        public final int offset;

        public Position(String filename, int line, int offset) {
            this.filename = filename;
            this.line = line;
            this.offset = offset;
        }
    }

    @Deprecated
    public static class PrintlnStatement extends Statement {

        public List<Expression> expressions;

        public PrintlnStatement(int pos, List<Expression> expressions) {
            super(pos);
            this.expressions = expressions;
        }

        @Override
        public Tag getTag() {
            return Tag.PRINTLN;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitPrintln(this);
        }
    }

    @Deprecated
    // todo: Заменить это на вызов функций с соответствующими именами
    public static class PrintStatement extends Statement {

        public List<Expression> expressions;

        public PrintStatement(int pos, List<Expression> expressions) {
            // print и println с какой-то там версии больше не являются языковыми конструкциями.
            super(pos);
            this.expressions = expressions;
        }

        @Override
        public Tag getTag() {
            return Tag.PRINT;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitPrint(this);
        }
    }

    public static class Return extends Statement {

        public Expression expr;

        @Override
        public Tag getTag() {
            return Tag.RETURN;
        }

        public Return(int pos) {
            this(pos, null);
        }

        public Return(int pos, Expression expr) {
            super(pos);
            this.expr = expr;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitReturn(this);
        }
    }

    public abstract static class Statement extends Tree implements Cloneable {

        public static final Statement EMPTY = null; // todo

        protected Statement(int pos) {
            super(pos);
        }

        public Statement copy(int pos) {//todo: remove
            try {
                Statement clone = (Statement) super.clone();
                clone.pos = pos;
                return clone;
            } catch (CloneNotSupportedException e) {
                throw new InternalError(e);
            }
        }
    }

    public static class Switch extends Statement {

        public Expression selector;

        public List<Case> cases;

        public Switch(int pos, Expression selector, List<Case> cases) {
            super(pos);
            this.selector = selector;
            this.cases = cases;
        }

        @Override
        public Tag getTag() {
            return Tag.SWITCH;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitSwitch(this);
        }
    }

    public static class TernaryOp extends Expression {

        public Expression cond;

        public Expression lhs;

        public Expression rhs;

        public TernaryOp(int pos, Expression cond, Expression lhs, Expression rhs) {
            super(pos);
            this.cond = cond;
            this.lhs = lhs;
            this.rhs = rhs;
        }

        @Override
        public boolean isAccessible() {
            return lhs.isAccessible() && rhs.isAccessible();
        }

        @Override
        public boolean isCloneable() {
            return lhs.isCloneable() && rhs.isCloneable();
        }

        @Override
        public boolean isNullable() {
            return lhs.isNullable() || rhs.isNullable();
        }

        @Override
        public Tag getTag() {
            return Tag.TERNARY;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitTernaryOp(this);
        }
    }

    public static class UnaryOp extends Expression {

        public Tag tag;
        public Expression hs;

        public UnaryOp(int pos, Tag tag, Expression hs) {
            super(pos);
            this.tag = tag;
            this.hs = hs;
        }

        @Override
        public Tag getTag() {
            return tag;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitUnaryOp(this);
        }
    }

    public static class Discarded extends Expression {

        public Expression expression;

        public Discarded(int pos, Expression expression) {
            super(pos);
            this.expression = expression;
        }

        @Override
        public Tag getTag() {
            return Tag.DISCARDED;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitDiscarded(this);
        }
    }

    public static class Var extends Expression {

        public final Name name;

        public Var(int pos, Name name) {
            super(pos);
            this.name = name;
        }

        @Override
        public boolean isAccessible() {
            return true;
        }

        @Override
        public boolean isAssignable() {
            return true;
        }

        @Override
        public boolean isCloneable() {
            return true;
        }

        @Override
        public boolean isNullable() {
            return true;
        }

        @Override
        public Tag getTag() {
            return Tag.VARIABLE;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitVariable(this);
        }
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
        public Tag getTag() {
            return Tag.WHILELOOP;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitWhileLoop(this);
        }
    }

    public static class Name {

        public final String value;

        public final int pos;

        public Name(String value, int pos) {
            this.value = value;
            this.pos = pos;
        }
    }

    public static class Parameter extends Tree {

        public final Name name;

        public Expression expr;

        public Parameter(Name name, Expression expr) {
            super(name.pos);
            this.name = name;
            this.expr = expr;
        }

        @Override
        public Tag getTag() {
            return expr.getTag();
        }

        @Override
        public void accept(Visitor visitor) {
            expr.accept(visitor);
        }
    }

    public static class Argument extends Tree {

        public final Name name;

        public Expression expr;

        public Argument(Name name, Expression expr) {
            super(name.pos);
            this.name = name;
            this.expr = expr;
        }

        @Override
        public Tag getTag() {
            return expr.getTag();
        }

        @Override
        public void accept(Visitor visitor) {
            expr.accept(visitor);
        }
    }

    public static class ArrayEntry extends Tree{

        public Expression key, value;

        public ArrayEntry(Expression key, Expression value) {
            super(key.pos);
            this.key = key;
            this.value = value;
        }

        @Override
        public Tag getTag() {
            return value.getTag();
        }

        @Override
        public void accept(Visitor visitor) {
            key.accept(visitor);
            value.accept(visitor);
        }
    }

    public static class Definition extends Tree {

        public final Name name;

        public Expression expr;

        public Definition(Name name, Expression expr) {
            super(name.pos);
            this.name = name;
            this.expr = expr;
        }

        @Override
        public Tag getTag() {
            return null;
        }

        @Override
        public void accept(Visitor visitor) {
            expr.accept(visitor);
        }
    }

    public static interface Visitor {
        void visitCompilationUnit(CompilationUnit tree);

        void visitArrayAccess(ArrayAccess expression);

        void visitArray(ArrayLiteral expression);

        void visitBlock(Block statement);

        void visitBreak(Break statement);

        void visitCase(Case statement);

        void visitConstantDeclare(ConstantDecl statement);

        void visitContinue(Continue statement);

        void visitDoLoop(DoLoop statement);

        void visitFallthrough(Fallthrough statement);

        void visitFor(ForLoop statement);

        void visitInvocation(Invocation expression);

        void visitFunctionDecl(FunctionDecl statement);

        void visitIf(If statement);

        void visitParens(Parens expression);

        @Deprecated
        void visitPrintln(PrintlnStatement statement);

        @Deprecated
        void visitPrint(PrintStatement statement);

        void visitReturn(Return statement);

        void visitSwitch(Switch statement);

        void visitTernaryOp(TernaryOp expression);

        void visitVariable(Var expression);

        void visitWhileLoop(WhileLoop statement);

        void visitDiscarded(Discarded expression);

        void visitBinaryOp(BinaryOp tree);

        void visitUnaryOp(UnaryOp tree);

        void visitAssignOp(AssignOp tree);

        void visitLiteral(Literal tree);
    }

    abstract static class AbstractVisitor implements Visitor {
        public void visitTree(Tree tree) { throw new AssertionError(); }

        @Override
        public void visitCompilationUnit(CompilationUnit tree) { visitTree(tree); }

        @Override
        public void visitFunctionDecl(FunctionDecl tree) { visitTree(tree); }

        @Override
        public void visitConstantDeclare(ConstantDecl tree) { visitTree(tree); }

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
        public void visitDiscarded(Discarded tree) { visitTree(tree); }

        @Override
        public void visitAssignOp(AssignOp tree) { visitTree(tree); }

        @Override
        public void visitTernaryOp(TernaryOp tree) { visitTree(tree); }

        @Override
        public void visitBinaryOp(BinaryOp tree) { visitTree(tree); }

        @Override
        public void visitUnaryOp(UnaryOp tree) { visitTree(tree); }

        @Override
        public void visitArrayAccess(ArrayAccess tree) { visitTree(tree); }

        @Override
        public void visitVariable(Var tree) { visitTree(tree); }

        @Override
        public void visitLiteral(Literal tree) { visitTree(tree); }

        @Override
        public void visitArray(ArrayLiteral tree) { visitTree(tree); }

        @Override
        public void visitInvocation(Invocation tree) { visitTree(tree); }

        @Override
        public void visitParens(Parens tree) { visitTree(tree); }
    }

    abstract static class Analyzer extends AbstractVisitor {

        public void analyze(Tree tree) {
            if (tree != null) tree.accept(this);
        }

        public void analyze(List<? extends Tree> trees) {
            for (Tree tree : trees) {
                if (tree != null) tree.accept(this);
            }
        }

        public void analyze(Map<? extends Tree, ? extends Tree> relationalTrees) {
            for (Map.Entry<? extends Tree, ? extends Tree> relationalTree : relationalTrees.entrySet()) {
                if (relationalTree.getKey() != null) relationalTree.getKey().accept(this);
                if (relationalTree.getValue() != null) relationalTree.getValue().accept(this);
            }
        }

        public void analyzeValues(Map<?, ? extends Tree> relationalTrees) {
            for (Map.Entry<?, ? extends Tree> relationalTree : relationalTrees.entrySet()) {
                if (relationalTree.getValue() != null) relationalTree.getValue().accept(this);
            }
        }

        @Override
        public void visitCompilationUnit(CompilationUnit tree) {
            analyze(tree.trees);
        }

        @Override
        public void visitFunctionDecl(FunctionDecl tree) {
            analyze(tree.body);
        }

        @Override
        public void visitConstantDeclare(ConstantDecl tree) {
            analyze(tree.definitions);
        }

        @Override
        public void visitBlock(Block tree) {
            analyze(tree.statements);
        }

        @Override
        public void visitIf(If tree) {
            analyze(tree.cond);
            analyze(tree.body);
            analyze(tree.elseBody);
        }

        @Override
        public void visitWhileLoop(WhileLoop tree) {
            analyze(tree.cond);
            analyze(tree.body);
        }

        @Override
        public void visitFor(ForLoop tree) {
            analyze(tree.init);
            analyze(tree.cond);
            analyze(tree.step);
            analyze(tree.body);
        }

        @Override
        public void visitDoLoop(DoLoop tree) {
            analyze(tree.body);
            analyze(tree.cond);
        }

        @Override
        public void visitSwitch(Switch tree) {
            analyze(tree.selector);
            analyze(tree.cases);
        }

        @Override
        public void visitCase(Case tree) {
            analyze(tree.expressions);
            analyze(tree.body);
        }

        @Override
        public void visitBreak(Break tree) {}

        @Override
        public void visitContinue(Continue tree) {}

        @Override
        public void visitFallthrough(Fallthrough tree) {}

        @Override
        public void visitReturn(Return tree) {
            analyze(tree.expr);
        }

        @Override
        public void visitDiscarded(Discarded tree) {
            analyze(tree.expression);
        }

        @Override
        public void visitAssignOp(AssignOp tree) {
            analyze(tree.expr);
            analyze(tree.var);
        }

        @Override
        public void visitTernaryOp(TernaryOp tree) {
            analyze(tree.cond);
            analyze(tree.lhs);
            analyze(tree.rhs);
        }

        @Override
        public void visitBinaryOp(BinaryOp tree) {
            analyze(tree.lhs);
            analyze(tree.rhs);
        }

        @Override
        public void visitUnaryOp(UnaryOp tree) {
            analyze(tree.hs);
        }

        @Override
        public void visitArrayAccess(ArrayAccess tree) {
            analyze(tree.array);
            analyze(tree.key);
        }

        @Override
        public void visitVariable(Var tree) {}

        @Override
        public void visitLiteral(Literal tree) {}

        @Override
        public void visitArray(ArrayLiteral tree) {
            analyze(tree.entries);
        }

        @Override
        public void visitInvocation(Invocation tree) {
            analyze(tree.args);
        }

        @Override
        public void visitParens(Parens tree) {
            analyze(tree.expr);
        }
    }

    abstract static class Reducer extends AbstractVisitor {

        public Tree result;

        @SuppressWarnings("unchecked")
        public <T extends Tree> T reduce(Tree tree) {
            if (tree != null) {
                tree.accept(this);
                Tree r = result;
                result = null;
                return (T) r;
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        public <T extends Tree> List<T> reduce(List<T> trees) {
            for (T tree : trees) {
                if (tree != null) {
                    tree.accept(this);
                    trees.set(trees.indexOf(tree), (T) result); // оптимизация пошла нахуй
                    result = null;
                }
            }
            return trees;
        }

        @SuppressWarnings("unchecked")
        public <K extends Tree, V extends Tree>
        Map<K, V> reduce(Map<K, V> relationalTrees) {
            for (Map.Entry<K, V> relationalTree : relationalTrees.entrySet()) {
                if (relationalTree.getKey() != null) {
                    relationalTree.getKey().accept(this);
                    V tempValue = relationalTrees.get(relationalTree.getKey());
                    relationalTrees.remove(relationalTree.getKey());
                    relationalTrees.put((K) result, tempValue);
                    result = null;
                }

                if (relationalTree.getValue() != null) {
                    relationalTree.getValue().accept(this);
                    relationalTrees.put(relationalTree.getKey(), (V) result);
                    result = null;
                }
            }
            return relationalTrees;
        }

        @SuppressWarnings("unchecked")
        public <K, V extends Tree> Map<K, V> reduceValues(Map<K, V> relationalTrees) {
            for (Map.Entry<?, V> relationalTree : relationalTrees.entrySet()) {
                if (relationalTree.getValue() != null) {
                    relationalTree.getValue().accept(this);
                    relationalTrees.put((K) relationalTree.getKey(), (V) result);
                    result = null;
                }
            }
            return relationalTrees;
        }

        @Override
        public void visitCompilationUnit(CompilationUnit tree) {
            tree.trees = reduce(tree.trees);
            result = tree;
        }

        @Override
        public void visitFunctionDecl(FunctionDecl tree) {
            tree.body = reduce(tree.body);
            result = tree;
        }

        @Override
        public void visitConstantDeclare(ConstantDecl tree) {
            tree.definitions = reduce(tree.definitions);
            result = tree;
        }

        @Override
        public void visitBlock(Block tree) {
            tree.statements = reduce(tree.statements);
            result = tree;
        }

        @Override
        public void visitIf(If tree) {
            tree.cond = reduce(tree.cond);
            tree.body = reduce(tree.body);
            tree.elseBody = reduce(tree.elseBody);
            result = tree;
        }

        @Override
        public void visitWhileLoop(WhileLoop tree) {
            tree.cond = reduce(tree.cond);
            tree.body = reduce(tree.body);
            result = tree;
        }

        @Override
        public void visitFor(ForLoop tree) {
            tree.init = reduce(tree.init);
            tree.cond = reduce(tree.cond);
            tree.body = reduce(tree.body);
            tree.step = reduce(tree.step);
            result = tree;
        }

        @Override
        public void visitDoLoop(DoLoop tree) {
            tree.body = reduce(tree.body);
            tree.cond = reduce(tree.cond);
            result = tree;
        }

        @Override
        public void visitSwitch(Switch tree) {
            tree.selector = reduce(tree.selector);
            tree.cases = reduce(tree.cases);
            result = tree;
        }

        @Override
        public void visitCase(Case tree) {
            tree.expressions = reduce(tree.expressions);
            tree.body = reduce(tree.body);
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
            tree.expr = reduce(tree.expr);
            result = tree;
        }

        @Override
        public void visitDiscarded(Discarded tree) {
            tree.expression = reduce(tree.expression);
            result = tree;
        }

        @Override
        public void visitAssignOp(AssignOp tree) {
            tree.expr = reduce(tree.expr);
            tree.var = reduce(tree.var);
            result = tree;
        }

        @Override
        public void visitTernaryOp(TernaryOp tree) {
            tree.cond = reduce(tree.cond);
            tree.lhs = reduce(tree.lhs);
            tree.rhs = reduce(tree.rhs);
            result = tree;
        }

        @Override
        public void visitBinaryOp(BinaryOp tree) {
            tree.lhs = reduce(tree.lhs);
            tree.rhs = reduce(tree.rhs);
            result = tree;
        }

        @Override
        public void visitUnaryOp(UnaryOp tree) {
            tree.hs = reduce(tree.hs);
            result = tree;
        }

        @Override
        public void visitArrayAccess(ArrayAccess tree) {
            tree.array = reduce(tree.array);
            tree.key = reduce(tree.key);
            result = tree;
        }

        @Override
        public void visitVariable(Var tree) { result = tree; }

        @Override
        public void visitLiteral(Literal tree) { result = tree; }

        @Override
        public void visitArray(ArrayLiteral tree) {
            tree.entries = reduce(tree.entries);
            result = tree;
        }

        @Override
        public void visitInvocation(Invocation tree) {
            tree.args = reduce(tree.args);
            result = tree;
        }

        @Override
        public void visitParens(Parens tree) {
            tree.expr = reduce(tree.expr);
            result = tree;
        }
    }
}
