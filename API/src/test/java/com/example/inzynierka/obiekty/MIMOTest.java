package com.example.inzynierka.obiekty;

import com.example.inzynierka.modele.ParObiektMIMO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import java.io.FileInputStream;
import java.io.IOException;

public class MIMOTest {

    @Test
    public void test1() throws IOException
    {
        ObjectMapper objectMapper = new ObjectMapper();
        Integer[] PV = objectMapper.treeToValue(objectMapper.readTree(new FileInputStream("src/main/java/com/example/inzynierka/ObiektMIMO.json")).path("PV"), Integer[].class);
        ParObiektMIMO[] Obiekty = objectMapper.treeToValue(objectMapper.readTree(new FileInputStream("src/main/java/com/example/inzynierka/ObiektMIMO.json")).path("ParObiektMIMO"), ParObiektMIMO[].class);
        MIMO obiekt  = new MIMO(Obiekty, "srednio");
        double[] expectedUMax = {100.0, 100.0};
        assertArrayEquals(obiekt.getUMax(),expectedUMax, 0.001);
        double[] expectedUMin = {0.0,0.0};
        assertArrayEquals(obiekt.getUMin(), expectedUMin, 0.001);
        assert(obiekt.getU().get(0).get(0)==0.0);
        assert(obiekt.getY().get(0).get(0)==0.0);

        double[] tempDU = {1.0,1.0};
        obiekt.obliczKrok(tempDU);
        assert(obiekt.getU().get(0).get(0)==1.0);
        double[] expectedY = {8.746552172781682, 0.0, 0.0};
        double[] arrayY = obiekt.getY().get(0).stream().mapToDouble(Double::doubleValue).toArray();
        assertArrayEquals(arrayY, expectedY, 0.0001);
    }

    @Test
    public void test2() throws IOException
    {
        ObjectMapper objectMapper = new ObjectMapper();
        Integer[] PV = objectMapper.treeToValue(objectMapper.readTree(new FileInputStream("src/main/java/com/example/inzynierka/ObiektMIMO.json")).path("PV"), Integer[].class);
        ParObiektMIMO[] Obiekty = objectMapper.treeToValue(objectMapper.readTree(new FileInputStream("src/main/java/com/example/inzynierka/ObiektMIMO.json")).path("ParObiektMIMO"), ParObiektMIMO[].class);
        MIMO obiekt  = new MIMO(Obiekty, "srednio");
        obiekt.obliczKrok(1.0, 1, 0);
        assert(obiekt.getU().get(0).get(0)==0.0);
        assert(obiekt.getU().get(1).get(0)==1.0);
        double[] expectedY = {8.730158730158731, 0.0, 0.0};
        double[] arrayY = obiekt.getY().get(0).stream().mapToDouble(Double::doubleValue).toArray();
        assertArrayEquals(arrayY, expectedY, 0.0001);
    }
}
