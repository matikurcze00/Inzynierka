package com.example.inzynierka.objects;

import com.example.inzynierka.models.ParObiektRownaniaMIMO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;

class MIMORownaniaTest {

    @Test
    void test1() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ParObiektRownaniaMIMO[] Obiekty = objectMapper.treeToValue(
            objectMapper.readTree(new FileInputStream("src/main/java/com/example/inzynierka/ObiektMIMORownania.json")).path("ParObiektRownaniaMIMO"),
            ParObiektRownaniaMIMO[].class);
        MIMODiscrete obiekt = new MIMODiscrete(Obiekty, "srednio");
        double[] expectedUMax = {100.0, 100.0};
        assertArrayEquals(obiekt.getUMax(), expectedUMax, 0.001);
        double[] expectedUMin = {-50.0, -50.0};
        assertArrayEquals(obiekt.getUMin(), expectedUMin, 0.001);
        assert (obiekt.getU().get(0).get(0) == 0.0);
        assert (obiekt.getY().get(0).get(0) == 0.0);

        double[] tempDU = {1.0, 1.0};
        obiekt.simulateStep(tempDU);
        assert (obiekt.getU().get(0).get(0) == 1.0);
        double[] expectedY = {0.9, 0.0, 0.0, 0.0, 0.0};
        double[] arrayY = obiekt.getY().get(0).stream().mapToDouble(Double::doubleValue).toArray();
        assertArrayEquals(arrayY, expectedY, 0.01);
    }
}
