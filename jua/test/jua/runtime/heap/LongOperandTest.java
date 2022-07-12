package jua.runtime.heap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LongOperandTest {

    private long testRatio;

    @Before
    public void doInit() {
        testRatio = Long.getLong("jua.long-operand.test-ratio", 512L);
    }

    @Test
    public void doTest() {
        for (long l = 0; l < testRatio; l++) {
            long x = l - (testRatio >> 1);
            LongOperand operand = LongOperand.valueOf(x);
            Assert.assertEquals(x, operand.longValue());
        }
    }
}
