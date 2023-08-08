package com.example.inzynierka.objects;

import com.example.inzynierka.models.ParObiektDPAMIMO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;

class MIMODPATest {

    @Test
    void test1() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Integer[] PV =
            objectMapper.treeToValue(objectMapper.readTree(new FileInputStream("src/main/java/com/example/inzynierka/ObiektMIMODPA.json")).path("PV"),
                Integer[].class);
        ParObiektDPAMIMO[] Obiekty = objectMapper.treeToValue(
            objectMapper.readTree(new FileInputStream("src/main/java/com/example/inzynierka/ObiektMIMODPA.json")).path("ParObiektDPAMIMO"),
            ParObiektDPAMIMO[].class);
        MIMODPA obiekt = new MIMODPA(Obiekty, "srednio");
        double[] expectedUMax = {100.0, 100.0};
        assertArrayEquals(obiekt.getUMax(), expectedUMax, 0.001);
        double[] expectedUMin = {0.0, 0.0};
        assertArrayEquals(obiekt.getUMin(), expectedUMin, 0.001);
        assert (obiekt.getU().get(0).get(0) == 0.0);
        assert (obiekt.getY().get(0).get(0) == 0.0);

        double[] tempDU = {1.0, 1.0};
        obiekt.simulateStep(tempDU);
        assert (obiekt.getU().get(0).get(0) == 1.0);
        double[] expectedY = {8.779339058027583, 0.0, 0.0};
        double[] arrayY = obiekt.getY().get(0).stream().mapToDouble(Double::doubleValue).toArray();
        assertArrayEquals(arrayY, expectedY, 0.0001);
    }

    @Test
    void test2() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Integer[] PV =
            objectMapper.treeToValue(objectMapper.readTree(new FileInputStream("src/main/java/com/example/inzynierka/ObiektMIMODPA.json")).path("PV"),
                Integer[].class);
        ParObiektDPAMIMO[] Obiekty = objectMapper.treeToValue(
            objectMapper.readTree(new FileInputStream("src/main/java/com/example/inzynierka/ObiektMIMODPA.json")).path("ParObiektDPAMIMO"),
            ParObiektDPAMIMO[].class);
        MIMODPA obiekt = new MIMODPA(Obiekty, "srednio");
        obiekt.simulateStep(1.0, 1, 0);
        assert (obiekt.getU().get(0).get(0) == 0.0);
        assert (obiekt.getU().get(1).get(0) == 1.0);
        double[] expectedY = {8.730158730158731, 0.0, 0.0};
        double[] arrayY = obiekt.getY().get(0).stream().mapToDouble(Double::doubleValue).toArray();
        assertArrayEquals(arrayY, expectedY, 0.0001);
    }
}
