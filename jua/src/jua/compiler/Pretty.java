package jua.compiler;

import jua.compiler.Tree.*;
import jua.util.List;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;

public class Pretty extends Scanner {

    private final PrintWriter writer;

    private final int tabIndent;

    private boolean newLine = false;

    private int nTabs = 0;

    public Pretty(PrintWriter writer) {
        this(writer, 2);
    }

    public Pretty(PrintWriter writer, int tabIndent) {
        this.writer = Objects.requireNonNull(writer, "Writer is null");
        this.tabIndent = tabIndent;
    }

    @Override
    public void visitCompilationUnit(CompilationUnit tree) {
        printLine("/* the begin of the code */");
        printLine();

        super.visitCompilationUnit(tree);

        printLine();
        printLine("/* the end of the code */");
    }

    @Override
    public void visitConstDef(ConstDef tree) {
        print("const ");

        printSequence(tree.defs, def -> {
            print(def.name);
            print(" = ");
            scan(def.expr);
        });

        printLine(";");
    }

    @Override
    public void visitFuncDef(FuncDef tree) {
        print("fn ");
        print(tree.name);
        print("(");
        printSequence(tree.params, param -> {
            print(param.name);
            if (param.expr != null) {
                print(" = ");
                scan(param.expr);
            }
        });
        print(") ");

        switch (tree.body.getTag()) {
            case BLOCK:
                scan(tree.body);
                break;
            case DISCARDED:
                print("= ");
                Discarded bodyTree = (Discarded) tree.body;
                scan(bodyTree.expr);
                printLine(";");
                break;
            default:
                throw illegalTagException(tree.body.getTag());
        }
    }

    @Override
    public void visitBlock(Block tree) {
        printLine("{");
        addTab();
        scan(tree.stats);
        subTab();
        printLine("}");
    }

    @Override
    public void visitIf(If tree) {
        print("if ");
        scan(tree.cond);
        printBody(tree.thenbody);
        if (tree.elsebody != null) {
            print(" else");
            printBody(tree.elsebody);
        }
    }

    @Override
    public void visitWhileLoop(WhileLoop tree) {
        print("while ");
        scan(tree.cond);
        printBody(tree.body);
    }

    @Override
    public void visitDoLoop(DoLoop tree) {
        print("do");
        printBody(tree.body);
        if (newLine) {
            print("while ");
        } else {
            print(" while ");
        }
        scan(tree.cond);
        printLine(";");
    }

    @Override
    public void visitForLoop(ForLoop tree) {
        print("for ");
        printSequence(tree.init, this::scan);
        print("; ");
        scan(tree.cond);
        print("; ");
        printSequence(tree.step, this::scan);
        printBody(tree.body);
    }

    @Override
    public void visitSwitch(Switch tree) {
        print("switch ");
        scan(tree.expr);
        printLine(" {");
        addTab();
        scan(tree.cases);
        subTab();
        printLine("}");
    }

    @Override
    public void visitCase(Case tree) {
        if (tree.labels == null) {
            print("default: ");
        } else {
            print("case ");
            printSequence(tree.labels, this::scan);
            print(": ");
        }
        scan(tree.body);
    }

    @Override
    public void visitBreak(Break tree) {
        printLine("break;");
    }

    @Override
    public void visitContinue(Continue tree) {
        printLine("continue;");
    }

    @Override
    public void visitFallthrough(Fallthrough tree) {
        printLine("fallthrough;");
    }

    @Override
    public void visitReturn(Return tree) {
        if (tree.expr == null) {
            printLine("return;");
        } else {
            print("return ");
            scan(tree.expr);
            printLine(";");
        }
    }

    @Override
    public void visitDiscarded(Discarded tree) {
        scan(tree.expr);
        printLine(";");
    }

    @Override
    public void visitLiteral(Literal tree) {
        print(tree.type.toString());
    }

    @Override
    public void visitArrayLiteral(ArrayLiteral tree) {
        if (tree._isList) {
            print("[");
            printSequence(tree.entries, entry -> scan(entry.value));
            print("]");
        } else {
            print("{");
            printSequence(tree.entries, entry -> {
                if (entry.key != null) {
                    scan(entry.key);
                    print(": ");
                }
                scan(entry.value);
            });
            print("}");
        }
    }

    @Override
    public void visitVariable(Var tree) {
        print(tree.name);
    }

    @Override
    public void visitMemberAccess(MemberAccess tree) {
        if (tree.expr != null) {
            scan(tree.expr);
            print(".");
        }
        print(tree.member);
    }

    @Override
    public void visitArrayAccess(ArrayAccess tree) {
        scan(tree.expr);
        print("[");
        scan(tree.index);
        print("]");
    }

    @Override
    public void visitInvocation(Invocation tree) {
        scan(tree.callee);
        print("(");
        printSequence(tree.args, arg -> {
            if (arg.name != null) {
                print(arg.name);
                print(": ");
            }
            scan(arg.expr);
        });
        print(")");
    }

    @Override
    public void visitParens(Parens tree) {
        print("(");
        scan(tree.expr);
        print(")");
    }

    @Override
    public void visitAssign(Assign tree) {
        scan(tree.var);
        print(" = ");
        scan(tree.expr);
    }

    @Override
    public void visitCompoundAssign(CompoundAssign tree) {
        String operator;
        switch (tree.tag) {
            case ASG_ADD:          operator = " += ";  break;
            case ASG_SUB:          operator = " -= ";  break;
            case ASG_MUL:          operator = " *= ";  break;
            case ASG_DIV:          operator = " /= ";  break;
            case ASG_REM:          operator = " %= ";  break;
            case ASG_SL:           operator = " <<= "; break;
            case ASG_SR:           operator = " >>= "; break;
            case ASG_BIT_AND:      operator = " &= ";  break;
            case ASG_BIT_OR:       operator = " |= ";  break;
            case ASG_BIT_XOR:      operator = " ^= ";  break;
            case ASG_NULLCOALSC: operator = " ??= "; break;
            default: throw illegalTagException(tree.tag);
        }

        scan(tree.var);
        print(operator);
        scan(tree.expr);
    }

    @Override
    public void visitTernaryOp(TernaryOp tree) {
        scan(tree.cond);
        print(" ? ");
        scan(tree.thenexpr);
        print(" : ");
        scan(tree.elseexpr);
    }

    @Override
    public void visitBinaryOp(BinaryOp tree) {
        String operator;
        switch (tree.tag) {
            case NULLCOALSC: operator = " ?? "; break;
            case OR:      operator = " || "; break;
            case AND:     operator = " && "; break;
            case EQ:      operator = " == "; break;
            case NE:      operator = " != "; break;
            case GT:      operator = " > ";  break;
            case GE:      operator = " >= "; break;
            case LT:      operator = " < ";  break;
            case LE:      operator = " <= "; break;
            case ADD:     operator = " + ";  break;
            case SUB:     operator = " - ";  break;
            case MUL:     operator = " * ";  break;
            case DIV:     operator = " / ";  break;
            case REM:     operator = " % ";  break;
            case SL:      operator = " << "; break;
            case SR:      operator = " >> "; break;
            case BIT_AND: operator = " & ";  break;
            case BIT_OR:  operator = " | ";  break;
            case BIT_XOR: operator = " ^ ";  break;
            default: throw illegalTagException(tree.tag);
        }

        scan(tree.lhs);
        print(operator);
        scan(tree.rhs);
    }

    @Override
    public void visitUnaryOp(UnaryOp tree) {
        String operator;
        switch (tree.tag) {
            case NEG:                   operator = "-";  break;
            case POS:                   operator = "+";  break;
            case NOT:                   operator = "!";  break;
            case BIT_INV:               operator = "~";  break;
            case PREINC: case POSTINC:  operator = "++"; break;
            case PREDEC: case POSTDEC:  operator = "--"; break;
            default: throw illegalTagException(tree.tag);
        }

        if (tree.hasTag(Tag.POSTDEC) || tree.hasTag(Tag.POSTINC)) {
            scan(tree.expr);
            print(operator);
        } else {
            print(operator);
            scan(tree.expr);
        }
    }

    public void printBody(Tree tree) {
        if (tree == null) {
            printLine(";");
            return;
        }
        print(" ");
        if (tree.getTag() == Tag.BLOCK) {
            scan(tree);
        } else {
            printLine();
            addTab();
            scan(tree);
            subTab();
        }
    }

    public void print(Name name) {
        print(name.value);
    }

    public void print(Object value) {
        print(String.valueOf(value));
    }

    public void print(String value) {
        if (newLine) {
            char[] indent = new char[nTabs * tabIndent];
            Arrays.fill(indent, ' ');
            writer.print(indent);
            newLine = false;
        }
        writer.print(value);
    }

    public <T> void printSequence(List<? extends T> sequence, Consumer<? super T> printer) {
        Iterator<? extends T> itr = sequence.iterator();

        if (itr.hasNext()) {
            printer.accept(itr.next());

            while (itr.hasNext()) {
                print(", ");
                printer.accept(itr.next());
            }
        }
    }

    public void addTab() {
        nTabs++;
    }

    public void subTab() {
        nTabs--;
    }

    public void printLine(Object value) {
        print(value);
        printLine();
    }

    public void printLine() {
        writer.print('\n');
        newLine = true;
    }

    private static IllegalArgumentException illegalTagException(Tag tag) {
        return new IllegalArgumentException(tag.name());
    }
}
