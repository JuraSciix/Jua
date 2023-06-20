package jua.compiler;

import jua.compiler.ProgramScope.ConstantSymbol;
import jua.compiler.Tree.*;

import java.util.Objects;

import static jua.compiler.TreeInfo.*;

public final class Lower extends Translator {

    private final ProgramScope programScope;

    private final Operators operators = new Operators();

    public Lower(ProgramScope programScope) {
        this.programScope = Objects.requireNonNull(programScope);
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
    public void visitVariable(Var tree) {
        if (programScope.isConstantDefined(tree.name)) {
            ConstantSymbol cd = programScope.lookupConstant(tree.name);
            if (cd.value != null) {
                result = new Literal(tree.pos, cd.value);
                return;
            }
        }
        result = tree;
    }

    @Override
    public void visitBinaryOp(BinaryOp tree) {
        tree.lhs = translate(tree.lhs);
        tree.rhs = translate(tree.rhs);

        switch (tree.tag) {

            // Лень объяснять. Лучше оставлю пример:
            // x = 'no no no';
            // println(false && x); // Lower удалит лишний false, оставив: println(x);
            // >>> no no no
            // Без оператора значение не интерпретируется генератором кода как логическое.
            // todo: Типизировать выражения. Необходимые сейчас типы: BOOLEAN, ANY.

//            case FLOW_AND:
//                result = isLiteralTrue(tree.lhs) ? tree.rhs : tree;
//                return;
//
//            case FLOW_OR:
//                result = isLiteralFalse(tree.lhs) ? tree.rhs : tree;
//                return;

            case ADD: case SUB: case MUL:
            case DIV: case REM: case SL:
            case SR: case BIT_AND: case BIT_OR:
            case BIT_XOR: case EQ: case NE:
            case GT: case GE: case LT:
            case LE:
                Expression innerLhs = stripParens(tree.lhs);
                if (innerLhs.hasTag(Tag.LITERAL)) {
                    Expression innerRhs = stripParens(tree.rhs);
                    if (innerRhs.hasTag(Tag.LITERAL)) {
                        Literal literalLhs = (Literal) innerLhs;
                        Literal literalRhs = (Literal) innerRhs;
                        result = operators.applyBinaryOperator(tree.tag, tree.pos, literalLhs.value, literalRhs.value, tree);
                        return;
                    }
                }
                break;

            case COALESCE:
                result = isLiteralNull(tree.lhs) ? tree.rhs : tree;
                return;
        }
        
        result = tree;
    }

    @Override
    public void visitUnaryOp(UnaryOp tree) {
        tree.expr = translate(tree.expr);

        Expression innerExpr = stripParens(tree.expr);
        if (innerExpr.hasTag(Tag.LITERAL)) {
            // Пост-унарными операциями считаются только POSTINC и POSTDEC,
            // которые:
            // 1. Не сворачиваются.
            // 2. Не применяются к литералам.
            // А значит, что никогда не встречаются в этом куске кода,
            // и что проверку на пост-унарную инструкцию для вычисления
            // минимальной позиции делать не нужно - она всегда у tree.
            Literal literalTree = (Literal) innerExpr;
            result = operators.applyUnaryOperator(tree.tag, tree.pos, literalTree.value, tree);
            return;
        }

        result = tree;
    }
}
