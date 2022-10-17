package jua.runtime.heap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

public class LongOperandTest {

    private long testRatio;

    @BeforeAll
    public void doInit() {
        testRatio = Long.getLong("jua.long-operand.test-ratio", 512L);
    }

    @Test
    public void doTest() {
        for (long l = 0; l < testRatio; l++) {
            long x = l - (testRatio >> 1);
            LongOperand operand = LongOperand.valueOf(x);
            Assertions.assertEquals(x, operand.longValue());
        }
    }
}
