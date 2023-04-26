package jua.compiler;

import jua.compiler.Tree.*;
import jua.utils.Assert;
import jua.utils.List;
import jua.utils.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Consumer;

public class Pretty extends Scanner {

    /** @see Tree#toString() */
    static String stringifyTree(Tree tree) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(1024);
        tree.accept(new Pretty(new PrintStream(buffer)));
        return buffer.toString();
    }

    private final PrintStream output;

    private final int tabIndent;

    private boolean newLine = false;

    private int nTabs = 0;

    private String codeHeader, codeFooter;

    public Pretty(PrintStream output) {
        this(output, 2);
    }

    public Pretty(PrintStream output, int tabIndent) {
        if (output == null) {
            throw new IllegalArgumentException("the output should not be null");
        }
        if (tabIndent < 0) {
            throw new IllegalArgumentException("the tabIndent should not be negative");
        }
        this.output = output;
        this.tabIndent = tabIndent;
    }

    public void setCodeHeader(String codeHeader) {
        this.codeHeader = StringUtils.stripWhitespacesToNull(codeHeader);
    }

    public void setCodeFooter(String codeFooter) {
        this.codeFooter = StringUtils.stripWhitespacesToNull(codeFooter);
    }

    @Override
    public void visitCompilationUnit(CompilationUnit tree) {
        if (codeHeader != null) {
            printLine(codeHeader);
            printLine();
        }

        for (Import anImport : tree.imports) {
            scan(anImport);
            printLine();
        }

        if (tree.imports.nonEmpty()) printLine();

        for (ConstDef constDef : tree.constants) {
            scan(constDef);
            printLine();
        }
        
        if (tree.constants.nonEmpty()) printLine();

        for (FuncDef funcDef : tree.functions) {
            scan(funcDef);
            printLine();
            printLine();
        }
        
        for (Statement stmt : tree.stats) {
            scan(stmt);
            printLine();
        }

        if (codeFooter != null) {
            printLine();
            printLine(codeFooter);
        }
    }

    @Override
    public void visitImport(Import tree) {
        print("use ");
        print(tree.lib);
        print(".");
        print(tree.target == null ? "*" : tree.target);
        print(";");
    }

    @Override
    public void visitConstDef(ConstDef tree) {
        print("const ");

        printEnumeration(tree.defs, def -> {
            print(def.name);
            print(" = ");
            scan(def.expr);
        });

        print(";");
    }

    @Override
    public void visitFuncDef(FuncDef tree) {
        print("fn ");
        print(tree.name);
        print("(");
        printEnumeration(tree.params, param -> {
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
                print(";");
                break;
            default:
                throw illegalTagException(tree.body.getTag());
        }
    }

    @Override
    public void visitBlock(Block tree) {
        printLine("{");
        addTab();
        for (Statement stmt : tree.stats) {
            scan(stmt);
            if (!isNewLine()) {
                printLine();
            }
        }
        subTab();
        printLine("}");
    }

    @Override
    public void visitIf(If tree) {
        print("if ");
        scan(tree.cond);
        printBody(tree.thenbody);
        if (tree.elsebody != null) {
            if (!isNewLine()) {
                print(" ");
            }
            print("else");
            if (tree.elsebody.hasTag(Tag.IF)) {
                print(" ");
                scan(tree.elsebody);
            } else {
                printBody(tree.elsebody);
            }
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
        print(";");
    }

    @Override
    public void visitForLoop(ForLoop tree) {
        print("for ");
        printEnumeration(tree.init, this::scan);
        print(" ");
        scan(tree.cond);
        print("; ");
        printEnumeration(tree.step, update -> scan(update.expr));
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
            printEnumeration(tree.labels, this::scan);
            print(": ");
        }
        scan(tree.body);
    }

    @Override
    public void visitBreak(Break tree) {
        print("break;");
    }

    @Override
    public void visitContinue(Continue tree) {
        print("continue;");
    }

    @Override
    public void visitFallthrough(Fallthrough tree) {
        print("fallthrough;");
    }

    @Override
    public void visitVarDef(VarDef tree) {
        print("var ");
        printEnumeration(tree.defs, def -> {
            print(def.name);
            if (def.init != null) {
                print(" = ");
                scan(def.init);
            }
        });
        print(";");
    }

    @Override
    public void visitReturn(Return tree) {
        if (tree.expr == null) {
            print("return;");
        } else {
            print("return ");
            scan(tree.expr);
            print(";");
        }
    }

    @Override
    public void visitDiscarded(Discarded tree) {
        scan(tree.expr);
        print(";");
    }

    @Override
    public void visitLiteral(Literal tree) {
        print(tree.type.toString());
    }

    @Override
    public void visitListInit(ListInit tree) {
        print("[");
        printEnumeration(tree.entries, this::scan);
        print("]");
    }

    @Override
    public void visitMapInit(MapInit tree) {
        if (tree.entries.isEmpty()) {
            print("{}");
            return;
        }
        printLine("{");
        addTab();
        printEnumeration(tree.entries, entry -> {
            if (entry.field) {
                Literal key = (Literal) entry.key;
                print(key.type.stringValue());
            } else {
                print("[");
                scan(entry.key);
                print("]");
            }
            print(": ");
            scan(entry.value);
            printLine();
        });
        subTab();
        print("}");
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
        scan(tree.target);
        print("(");
        printEnumeration(tree.args, arg -> {
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
            case ASG_COALESCE: operator = " ??= "; break;
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
            case COALESCE: operator = " ?? "; break;
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
            print(";");
            return;
        }
        print(" ");
        if (tree.hasTag(Tag.BLOCK)) {
            scan(tree);
        } else {
            printLine();
            addTab();
            scan(tree);
            subTab();
            printLine();
        }
    }

    public void print(Object value) {
        print(String.valueOf(value));
    }

    public void print(String value) {
        if (newLine) {
            char[] indent = new char[nTabs * tabIndent];
            Arrays.fill(indent, ' ');
            output.print(indent);
            newLine = false;
        }
        output.print(value);
    }

    public <T> void printEnumeration(List<? extends T> enumeration, Consumer<? super T> printer) {
        Iterator<? extends T> itr = enumeration.iterator();

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
        Assert.ensure(nTabs > 0);
        nTabs--;
    }

    public void printLine(Object value) {
        print(value);
        printLine();
    }

    public void printLine() {
        output.print('\n');
        newLine = true;
    }

    public boolean isNewLine() {
        return newLine;
    }

    private static IllegalArgumentException illegalTagException(Tag tag) {
        return new IllegalArgumentException(tag.name());
    }
}
