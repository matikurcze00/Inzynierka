package com.example.inzynierka.obiekty;

import org.junit.jupiter.api.Test;


public class SISOTransmitancjaCiagleTest {

    @Test
    public void test1()
    {
        SISOTransmitancjaCiagle sisoTransmitancjaCiagle = new SISOTransmitancjaCiagle(10.0, 1.0, 1, 1.0, 0, 1.0, 1.0, 0.0, 0, 1, 100.0, -100.0, "srednio");

        assert(sisoTransmitancjaCiagle.getUMax()== 100.0);
        assert(sisoTransmitancjaCiagle.getUMin()==-100.0);
        assert(sisoTransmitancjaCiagle.getU().get(0)==0.0);
        assert(sisoTransmitancjaCiagle.getY().get(0)==0.0);
        assert(sisoTransmitancjaCiagle.getBlad()=="srednio");
        assert(sisoTransmitancjaCiagle.getYpp()==0.0);
        sisoTransmitancjaCiagle.obliczKrok(1.0);
        assert(sisoTransmitancjaCiagle.getU().get(0)==1.0);
        assert(sisoTransmitancjaCiagle.getY().get(0)==6.666666666666668);
    }
}
