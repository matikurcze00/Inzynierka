package com.example.inzynierka.obiekty;

import org.junit.jupiter.api.Test;


public class SISOTest {

    @Test
    public void test1()
    {
        SISO siso = new SISO(10.0, 1.0, 1, 1.0, 0, 1.0, 1.0, 0.0, 0, 1, 100.0, -100.0, "srednio");

        assert(siso.getUMax()== 100.0);
        assert(siso.getUMin()==-100.0);
        assert(siso.getU().get(0)==0.0);
        assert(siso.getY().get(0)==0.0);
        assert(siso.getBlad()=="srednio");
        assert(siso.getYpp()==0.0);
        siso.obliczKrok(1.0);
        assert(siso.getU().get(0)==1.0);
        assert(siso.getY().get(0)==6.666666666666668);
    }
}
