package com.example.inzynierka.regulatory;

import com.example.inzynierka.modele.ParObiektDPAMIMO;
import com.example.inzynierka.obiekty.MIMODPA;
import com.example.inzynierka.obiekty.SISODPA;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;

public class PIDTest {

    @Test
    public void SISOTest() {
        SISODPA SISODPA = new SISODPA(10.0, 1.0, 1, 1.0, 0, 1.0, 1.0, 0.0, 0, 1, 100.0, -100.0, "srednio");
        Regulator regulator = new PID(1.0, 1.0, 1.0, 1.0, new double[]{100.0} , 3.0, 100.0, new Double[]{null, null, null});
        regulator.setCel(new double[]{30.0});
        assert(regulator.getCel()[0]==30.0);
        double tempY = SISODPA.obliczKrok(regulator.policzSterowanie(SISODPA.getAktualna()));
        double expectedY = 20.0;
        assert(tempY==expectedY);
        SISODPA.resetObiektu();
        regulator.resetujRegulator();
        tempY = SISODPA.obliczKrok(regulator.policzSterowanie(SISODPA.getAktualna()));
        assert(tempY==expectedY);
    }

    @Test
    public void MIMOTest() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Integer[] PV = objectMapper.treeToValue(objectMapper.readTree(new FileInputStream("src/main/java/com/example/inzynierka/ObiektMIMO.json")).path("PV"), Integer[].class);
        ParObiektDPAMIMO[] Obiekty = objectMapper.treeToValue(objectMapper.readTree(new FileInputStream("src/main/java/com/example/inzynierka/ObiektMIMO.json")).path("ParObiektMIMO"), ParObiektDPAMIMO[].class);
        MIMODPA obiekt  = new MIMODPA(Obiekty, "srednio");
        Double[] tempStrojenie = new Double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0};
        Regulator regulator = new ZbiorPID(obiekt,PV, 3.0, tempStrojenie);
        double[] tempCel = {30.0, 30.0};
        regulator.setCel(tempCel);
        assert(regulator.getCel()[0]==30.0);
        double[] tempY = obiekt.obliczKrok(regulator.policzSterowanie(obiekt.getAktualne()));
        double[] tempExpectedY = {26.23965651834505, 26.23965651834505};
        assertArrayEquals(tempY,tempExpectedY, 0.1);
        obiekt.resetObiektu();
        regulator.resetujRegulator();
        tempY = obiekt.obliczKrok(regulator.policzSterowanie(obiekt.getAktualne()));
        assertArrayEquals(tempY,tempExpectedY, 0.1);
    }
}
