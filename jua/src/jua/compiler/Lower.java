package jua.compiler;

import jua.compiler.Tree.*;

import java.util.HashMap;
import java.util.Map;

public final class Lower extends Translator {

    private final Map<String, Literal> constantLiterals = new HashMap<>();

    @Override
    public void visitConstDef(ConstDef tree) {
        for (Definition def : tree.defs) {
            Expression expr = translate(def.expr);
            if (expr != null && expr.getTag() == Tag.LITERAL) {
                // Сохраняем объект литерала для встраивания.
                Literal literal = (Literal) expr;
                constantLiterals.put(def.name.value, literal);
            }
            def.expr = expr;
        }
        result = tree;
    }

    @Override
    public void visitVariable(Var tree) {
        String var = tree.name.value;
        if (constantLiterals.containsKey(var)) {
            // Подставляем значение константы вместо переменной.
            Literal constantLiteral = constantLiterals.get(var);
            result = new Literal(tree.pos, constantLiteral.value);
        } else {
            result = tree;
        }
    }

    @Override
    public void visitParens(Parens tree) {
        // Удаляем скобки.
        result = translate(tree.expr);
    }

    @Override
    public void visitAssignOp(AssignOp tree) {
        // todo: Удаление выражений {a=a}
        // todo: Не преобразовывать ASG_NULLCOALESCE
        if (tree.getTag() == Tag.ASSIGN) {
            result = tree;
            return;
        }

        // Преобразуем выражение типов {a+=b} в {a=a+b}
        result = new AssignOp(tree.pos, Tag.ASSIGN,
                tree.dst,
                new BinaryOp(tree.src.pos, TreeInfo.tagWithoutAsg(tree.tag),
                        tree.dst,
                        tree.src));
    }

    @Override
    public void visitBinaryOp(BinaryOp tree) {
        // todo: task for JavaKira
        super.visitBinaryOp(tree);
    }

    @Override
    public void visitUnaryOp(UnaryOp tree) {
        // todo: task for JavaKira
        super.visitUnaryOp(tree);
    }
}
