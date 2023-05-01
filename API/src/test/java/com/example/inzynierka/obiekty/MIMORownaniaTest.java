package com.example.inzynierka.obiekty;

import com.example.inzynierka.modele.ParObiektDPAMIMO;
import com.example.inzynierka.modele.ParObiektRownania;
import com.example.inzynierka.modele.ParObiektRownaniaMIMO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;

public class MIMORownaniaTest {

    @Test
    public void test1() throws IOException
    {
        ObjectMapper objectMapper = new ObjectMapper();
        ParObiektRownaniaMIMO[] Obiekty = objectMapper.treeToValue(objectMapper.readTree(new FileInputStream("src/main/java/com/example/inzynierka/ObiektMIMORownania.json")).path("ParObiektRownaniaMIMO"), ParObiektRownaniaMIMO[].class);
        MIMORownianiaRoznicowe obiekt  = new MIMORownianiaRoznicowe(Obiekty, "srednio");
        double[] expectedUMax = {100.0, 100.0};
        assertArrayEquals(obiekt.getUMax(),expectedUMax, 0.001);
        double[] expectedUMin = {-50.0,-50.0};
        assertArrayEquals(obiekt.getUMin(), expectedUMin, 0.001);
        assert(obiekt.getU().get(0).get(0)==0.0);
        assert(obiekt.getY().get(0).get(0)==0.0);

        double[] tempDU = {1.0,1.0};
        obiekt.obliczKrok(tempDU);
        assert(obiekt.getU().get(0).get(0)==1.0);
        double[] expectedY = {0.9, 0.0, 0.0, 0.0, 0.0};
        double[] arrayY = obiekt.getY().get(0).stream().mapToDouble(Double::doubleValue).toArray();
        assertArrayEquals(arrayY, expectedY, 0.01);
    }
}
