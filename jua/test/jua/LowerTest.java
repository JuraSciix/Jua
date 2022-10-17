package jua;


import jua.compiler.Code;
import jua.compiler.Lower;
import jua.compiler.Tree;
import jua.compiler.Types;
import jua.util.Source;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class LowerTest {
    private static Lower lower;
    private static Types types;

    @BeforeAll
    public static void doInit() {
        try {
            types = new Types(new Code(new Source("none", new char[]{})));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        lower = new Lower(types);
    }

    @Test
    public void doBinary() {
        //todo: перепутал excepted и actual, поменять аргументы местами
        //Арифметика
        lower.visitBinaryOp(binaryOp(Tree.Tag.ADD,
                literal(types.asLong(5)),
                binaryOp(Tree.Tag.MUL,
                        literal(types.asLong(4)),
                        literal(types.asLong(3))
                ))
        );
        Assertions.assertEquals(lower.result, literal(types.asLong(17)));

        lower.visitBinaryOp(binaryOp(Tree.Tag.ADD, literal(types.asLong(3)), literal(types.asLong(5))));
        Assertions.assertEquals(lower.result, literal(types.asLong(8)));

        lower.visitBinaryOp(binaryOp(Tree.Tag.SUB, literal(types.asLong(5)), literal(types.asLong(3))));
        Assertions.assertEquals(lower.result, literal(types.asLong(2)));

        lower.visitBinaryOp(binaryOp(Tree.Tag.DIV, literal(types.asLong(5)), literal(types.asLong(2))));
        Assertions.assertEquals(lower.result, literal(types.asLong(2)));

        lower.visitBinaryOp(binaryOp(Tree.Tag.DIV, literal(types.asLong(5)), literal(types.asDouble(2))));
        Assertions.assertEquals(lower.result, literal(types.asDouble(2.5)));

        //Деление на 0 не должно фолдится
        Tree.BinaryOp op = binaryOp(Tree.Tag.DIV, literal(types.asLong(5)), literal(types.asLong(0)));
        lower.visitBinaryOp(op);
        Assertions.assertEquals(lower.result, op);

        lower.visitBinaryOp(binaryOp(Tree.Tag.MUL, literal(types.asLong(5)), literal(types.asLong(2))));
        Assertions.assertEquals(lower.result, literal(types.asLong(10)));

        lower.visitBinaryOp(binaryOp(Tree.Tag.REM, literal(types.asLong(5)), literal(types.asLong(2))));
        Assertions.assertEquals(lower.result, literal(types.asLong(1)));

        //Сдвиг
        lower.visitBinaryOp(binaryOp(Tree.Tag.SL, literal(types.asLong(1)), literal(types.asLong(1))));
        Assertions.assertEquals(lower.result, literal(types.asLong(2)));

        lower.visitBinaryOp(binaryOp(Tree.Tag.SR, literal(types.asLong(1)), literal(types.asLong(1))));
        Assertions.assertEquals(lower.result, literal(types.asLong(0)));

        //Побитовые операции
        lower.visitBinaryOp(binaryOp(Tree.Tag.AND, literal(types.asLong(1)), literal(types.asLong(1))));
        Assertions.assertEquals(lower.result, literal(types.asLong(1)));

        lower.visitBinaryOp(binaryOp(Tree.Tag.OR, literal(types.asLong(1)), literal(types.asLong(1))));
        Assertions.assertEquals(lower.result, literal(types.asLong(1)));

        lower.visitBinaryOp(binaryOp(Tree.Tag.XOR, literal(types.asLong(1)), literal(types.asLong(1))));
        Assertions.assertEquals(lower.result, literal(types.asLong(0)));

        //Сравнение
        lower.visitBinaryOp(binaryOp(Tree.Tag.LE, literal(types.asLong(1)), literal(types.asLong(1))));
        Assertions.assertEquals(lower.result, literal(types.asBoolean(true)));

        lower.visitBinaryOp(binaryOp(Tree.Tag.LT, literal(types.asLong(1)), literal(types.asLong(1))));
        Assertions.assertEquals(lower.result, literal(types.asBoolean(false)));

        lower.visitBinaryOp(binaryOp(Tree.Tag.GT, literal(types.asLong(1)), literal(types.asLong(1))));
        Assertions.assertEquals(lower.result, literal(types.asBoolean(false)));

        lower.visitBinaryOp(binaryOp(Tree.Tag.GE, literal(types.asLong(1)), literal(types.asLong(1))));
        Assertions.assertEquals(lower.result, literal(types.asBoolean(true)));

        lower.visitBinaryOp(binaryOp(Tree.Tag.NE, literal(types.asLong(1)), literal(types.asLong(1))));
        Assertions.assertEquals(lower.result, literal(types.asBoolean(false)));

        lower.visitBinaryOp(binaryOp(Tree.Tag.EQ, literal(types.asLong(1)), literal(types.asLong(1))));
        Assertions.assertEquals(lower.result, literal(types.asBoolean(true)));

        //Мелочевка
        lower.visitBinaryOp(binaryOp(Tree.Tag.FLOW_OR, literal(types.asBoolean(false)), literal(types.asBoolean(true))));
        Assertions.assertEquals(lower.result, literal(types.asBoolean(true)));

        lower.visitBinaryOp(binaryOp(Tree.Tag.FLOW_AND, literal(types.asBoolean(true)), literal(types.asBoolean(false))));
        Assertions.assertEquals(lower.result, literal(types.asBoolean(false)));
    }

    public Tree.Literal literal(Types.Type type) {
        return new Tree.Literal(0, type);
    }

    public Tree.BinaryOp binaryOp(Tree.Tag tag, Tree.Expression lhs, Tree.Expression rhs) {
        return new Tree.BinaryOp(0, tag, lhs, rhs);
    }
}
