package com.example.inzynierka.objects;

import org.junit.jupiter.api.Test;


class SISODPATest {

    @Test
    void test1() {
        SISODPA SISODPA = new SISODPA(10.0, 1.0, 1, 1.0, 0, 1.0, 1.0, 0.0, 0, 1, 100.0, -100.0, "srednio");

        assert (SISODPA.getUMax() == 100.0);
        assert (SISODPA.getUMin() == -100.0);
        assert (SISODPA.getU().get(0) == 0.0);
        assert (SISODPA.getY().get(0) == 0.0);
        assert (SISODPA.getTypeOfError() == "srednio");
        assert (SISODPA.getYpp() == 0.0);
        SISODPA.simulateStep(1.0);
        assert (SISODPA.getU().get(0) == 1.0);
        assert (SISODPA.getY().get(0) == 6.666666666666668);
    }
}
