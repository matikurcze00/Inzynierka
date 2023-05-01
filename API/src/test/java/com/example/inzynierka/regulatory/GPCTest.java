package com.example.inzynierka.regulatory;

import com.example.inzynierka.modele.ParObiektDPAMIMO;
import com.example.inzynierka.modele.ParObiektRownaniaMIMO;
import com.example.inzynierka.obiekty.MIMODPA;
import com.example.inzynierka.obiekty.MIMORownianiaRoznicowe;
import com.example.inzynierka.obiekty.SISODPA;
import com.example.inzynierka.obiekty.SISORownianiaRoznicowe;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;

public class GPCTest {

    @Test
    public void GPCSISOTest() {
        SISORownianiaRoznicowe SISO = new SISORownianiaRoznicowe(-0.5, 0.0, 0.0, 0.0, 0.0,
            0.4, 0.3, 0.0, 0.0, 0.0, 100, -30, "srednio");
        Regulator regulator = new GPC( SISO, 0.1, SISO.getYMax() / 2, 3.0);
        regulator.setCel(new double[]{30.0});
        assert(regulator.getCel()[0]==30.0);
        double tempY = SISO.obliczKrok(regulator.policzSterowanie(SISO.getAktualna()));
        double expectedY = 1.2000000000000002;
        assert(tempY==expectedY);
        SISO.resetObiektu();
        regulator.resetujRegulator();
        tempY = SISO.obliczKrok(regulator.policzSterowanie(SISO.getAktualna()));
        assert(tempY==expectedY);
    }

    @Test
    public void MIMOTest() throws IOException
    {
        ObjectMapper objectMapper = new ObjectMapper();
        ParObiektRownaniaMIMO[] Obiekty = objectMapper.treeToValue(objectMapper.readTree(new FileInputStream("src/main/java/com/example/inzynierka/ObiektMIMORownania.json")).path("ParObiektRownaniaMIMO"), ParObiektRownaniaMIMO[].class);
        MIMORownianiaRoznicowe obiekt  = new MIMORownianiaRoznicowe(Obiekty, "srednio");
        double[] tempLambda = {0.5,0.5};
        Double[] tempStrojenie = new Double[]{1.0, 1.0};
        Regulator regulator = new GPC( obiekt,  5, obiekt.getYMax(), 3.0, tempStrojenie, tempLambda);
        double[] tempCel = {30.0, 30.0};
        regulator.setCel(tempCel);
        assert(regulator.getCel()[0]==30.0);
        double[] tempY = obiekt.obliczKrok(regulator.policzSterowanie(obiekt.getAktualne()));
        double[] tempExpectedY = {2.6999, 1.500};
        assertArrayEquals(tempY,tempExpectedY, 0.1);
        obiekt.resetObiektu();
        regulator.resetujRegulator();
        tempY = obiekt.obliczKrok(regulator.policzSterowanie(obiekt.getAktualne()));
        assertArrayEquals(tempY,tempExpectedY, 0.1);
    }
}
