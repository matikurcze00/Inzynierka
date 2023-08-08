package com.example.inzynierka.objects;

import org.junit.jupiter.api.Test;

class SISORownaniaTest {

    @Test
    void test1() {
        SISODiscrete SISO = new SISODiscrete(-0.5, 0.0, 0.0, 0.0, 0.0,
            0.4, 0.3, 0.0, 0.0, 0.0, 100, -30, "srednio");
        assert (SISO.getUMax() == 100.0);
        assert (SISO.getUMin() == -30.0);
        assert (SISO.getA().get(0) == -0.5);
        assert (SISO.getA().get(1) == 0.0);
        assert (SISO.getB().get(0) == 0.4);
        assert (SISO.getB().get(1) == 0.3);
        assert (SISO.getB().get(2) == 0.0);
        assert (SISO.getTypeOfError() == "srednio");
        SISO.simulateStep(1.0);
        assert (SISO.getU().get(0) == 1.0);
        assert (SISO.getOutput() == 0.4);
    }
}
