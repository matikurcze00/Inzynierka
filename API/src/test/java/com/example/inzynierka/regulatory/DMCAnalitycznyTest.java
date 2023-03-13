package com.example.inzynierka.regulatory;

import com.example.inzynierka.modele.ParObiektMIMO;
import com.example.inzynierka.obiekty.MIMO;
import com.example.inzynierka.obiekty.SISO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;

public class DMCAnalitycznyTest {

    @Test
    public void SISOTest() {
        SISO siso = new SISO(10.0, 1.0, 1, 1.0, 0, 1.0, 1.0, 0.0, 0, 1, 100.0, -100.0, "srednio");
        Regulator regulator = new DMCAnalityczny(4, 0.1, siso, siso.getYMax() / 2, 3.0, 11);;
        regulator.setCel(new double[]{30.0});
        assert(regulator.getCel()[0]==30.0);
        double tempY = siso.obliczKrok(regulator.policzOutput(siso.getAktualna()));
        double expectedY = 20.0;
        assert(tempY==expectedY);
        siso.resetObiektu();
        regulator.resetujRegulator();
        tempY = siso.obliczKrok(regulator.policzOutput(siso.getAktualna()));
        assert(tempY==expectedY);

    }

    @Test
    public void MIMOTest() throws IOException
    {
        ObjectMapper objectMapper = new ObjectMapper();
        Integer[] PV = objectMapper.treeToValue(objectMapper.readTree(new FileInputStream("src/main/java/com/example/inzynierka/ObiektMIMO.json")).path("PV"), Integer[].class);
        ParObiektMIMO[] Obiekty = objectMapper.treeToValue(objectMapper.readTree(new FileInputStream("src/main/java/com/example/inzynierka/ObiektMIMO.json")).path("ParObiektMIMO"), ParObiektMIMO[].class);
        MIMO obiekt  = new MIMO(Obiekty, "srednio");
        double[] tempLambda = {0.5,0.5};
        Double[] tempStrojenie = new Double[]{1.0, 1.0};
        Regulator regulator = new DMCAnalityczny(5, tempLambda, obiekt, obiekt.getYMax(), 3.0, 11, tempStrojenie);
        double[] tempCel = {30.0, 30.0};
        regulator.setCel(tempCel);
        assert(regulator.getCel()[0]==30.0);
        double[] tempY = obiekt.obliczKrok(regulator.policzOutput(obiekt.getAktualne()));
        double[] tempExpectedY = {26.23965651834505, 26.23965651834505};
        assertArrayEquals(tempY,tempExpectedY, 0.1);
        obiekt.resetObiektu();
        regulator.resetujRegulator();
        tempY = obiekt.obliczKrok(regulator.policzOutput(obiekt.getAktualne()));
        assertArrayEquals(tempY,tempExpectedY, 0.1);
    }
}
