package jua.compiler;

import jua.compiler.SemanticInfo.BooleanEquivalent;
import jua.compiler.Tree.*;
import jua.compiler.utils.Flow;

import java.util.Objects;

import static jua.compiler.SemanticInfo.ofBoolean;
import static jua.compiler.CompHelper.*;

public final class Lower extends Translator {
    private final ModuleScope scope;
    private final Operators operators = new Operators();

    public Lower(ModuleScope scope) {
        this.scope = Objects.requireNonNull(scope);
    }


    @Override
    public void visitDocument(Document tree) {
        // Jua, начиная с версии 3.1 от 10/3/2023 не поддерживает выполняемые инструкции вне функций.
        // Для временной обратной совместимости, компилятор преобразовывает код вне функций в код функции <main>.
        // Возможно, в будущем времени компилятор полностью перестанет работать с кодом вне функций.

        // Заметка: в tree.stats не могут находиться операторы Tag.FUNCDEF и Tag.CONSTDEF.
        int pos = tree.pos;
        tree.functions = Flow.builder(tree.functions)
                .append(
                        new FuncDef(pos,
                                pos,
                                "<main>",
                                Flow.empty(),
                                new Block(pos, tree.stats), 0))
                .toFlow();
        tree.stats = Flow.empty();

        super.visitDocument(tree);
    }

    @Override
    public void visitWhileLoop(WhileLoop tree) {
        tree.cond = translate(tree.cond);
        tree.body = translate(tree.body);
        if (isLiteralTrue(tree.cond)) {
            // while true { ... } => do { ... } while true;
            result = new DoLoop(tree.pos, tree.body, tree.cond);
        } else {
            result = tree;
        }
    }

    @Override
    public void visitConditional(Conditional tree) {
        tree.cond = translate(tree.cond);
        // Не будем забегать вперед с редукцией потенциально лишних участков кода.

        Expr condTree = stripParens(tree.cond);
        if (condTree.hasTag(Tag.LITERAL)) {
            Literal condLit = (Literal) condTree;
            BooleanEquivalent booleanEquivalent = ofBoolean(condLit.value);
            if (booleanEquivalent.isTrue()) {
                result = translate(tree.ths);
                return;
            }
            if (booleanEquivalent.isFalse()) {
                result = translate(tree.fhs);
                return;
            }
        }

        tree.ths = translate(tree.ths);
        tree.fhs = translate(tree.fhs);
        result = tree;
    }

    @Override
    public void visitBinaryOp(BinaryOp tree) {
        tree.lhs = translate(tree.lhs);
        tree.rhs = translate(tree.rhs);

        switch (tree.tag) {
            case COALESCE:
                if (isNull(tree.lhs)) {
                    // null ?? (...) => (...)
                    result = tree.rhs;
                } else {
                    result = tree;
                }
                break;
            case EQ:
            case NE:
                if (isNull(tree.lhs)) {
                    // (x == null) => nullChk(x)
                    // (x != null) => !nullChk(x)
                    UnaryOp nullCheckTree = new UnaryOp(tree.pos, Tag.NULLCHK, tree.rhs);
                    if (tree.hasTag(Tag.NE)) {
                        nullCheckTree = new UnaryOp(tree.pos, Tag.NOT, nullCheckTree); // negate null-check
                    }
                    result = translate(nullCheckTree); // reduce "null == null"
                    break;
                }
                if (isNull(tree.rhs)) {
                    // (null == y) => nullChk(y)
                    // (null != y) => !nullChk(y)
                    UnaryOp nullCheckTree = new UnaryOp(tree.lhs.pos, Tag.NULLCHK, tree.lhs);
                    if (tree.hasTag(Tag.NE)) {
                        nullCheckTree = new UnaryOp(tree.lhs.pos, Tag.NOT, nullCheckTree); // negate null-check
                    }
                    result = nullCheckTree; // "null == null" is never be here
                    break;
                }
            default:
                Expr lhsTree = stripParens(tree.lhs);
                Expr rhsTree = stripParens(tree.rhs);
                if (lhsTree.hasTag(Tag.LITERAL) && rhsTree.hasTag(Tag.LITERAL)) {
                    Literal lhsLit = (Literal) lhsTree;
                    Literal rhsLit = (Literal) rhsTree;
                    result = operators.applyBinaryOperator(tree.tag, tree.pos, lhsLit.value, rhsLit.value, tree);
                    break;
                }
                result = tree;
        }
    }

    @Override
    public void visitUnaryOp(UnaryOp tree) {
        tree.expr = translate(tree.expr);

        Expr exprTree = stripParens(tree.expr);
        if (exprTree.hasTag(Tag.LITERAL)) {
            // Пост-унарными операциями считаются только POSTINC и POSTDEC,
            // которые:
            // 1. Не сворачиваются.
            // 2. Не применяются к литералам.
            // А значит, что никогда не встречаются в этом куске кода,
            // и что проверку на пост-унарную инструкцию для вычисления
            // минимальной позиции делать не нужно - она всегда у tree.
            Literal exprLit = (Literal) exprTree;
            result = operators.applyUnaryOperator(tree.tag, tree.pos, exprLit.value, tree);
            return;
        }

        result = tree;
    }
}
