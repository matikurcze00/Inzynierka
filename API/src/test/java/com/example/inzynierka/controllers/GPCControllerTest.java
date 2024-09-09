package com.example.inzynierka.controllers;

import com.example.inzynierka.models.ParObiektRownaniaMIMO;
import com.example.inzynierka.objects.MIMODiscrete;
import com.example.inzynierka.objects.SISODiscrete;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;

class GPCControllerTest {

    @Test
    void GPCSISOTest() {
        SISODiscrete SISO = new SISODiscrete(-0.5, 0.0, 0.0, 0.0, 0.0,
            0.4, 0.3, 0.0, 0.0, 0.0, 100, -30, "srednio");
        AbstractController abstractController = new GPCController(SISO, 0.1, SISO.getYMax() / 2, 3.0);
        abstractController.setSetpoint(new double[] {30.0});
        assert (abstractController.getSetpoint()[0] == 30.0);
        double tempY = SISO.simulateStep(abstractController.countControls(SISO.getOutput()));
        double expectedY = 1.2000000000000002;
        assert (tempY == expectedY);
        SISO.resetObject();
        abstractController.resetController();
        tempY = SISO.simulateStep(abstractController.countControls(SISO.getOutput()));
        assert (tempY == expectedY);
    }

    @Test
    void MIMOTest() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ParObiektRownaniaMIMO[] Obiekty = objectMapper.treeToValue(
            objectMapper.readTree(new FileInputStream("src/main/java/com/example/inzynierka/ObiektMIMORownania.json")).path("ParObiektRownaniaMIMO"),
            ParObiektRownaniaMIMO[].class);
        MIMODiscrete obiekt = new MIMODiscrete(Obiekty, "srednio");
        double[] tempLambda = {0.5, 0.5};
        Double[] tempStrojenie = new Double[] {1.0, 1.0};
        AbstractController abstractController = new GPCController(obiekt, 5, obiekt.getYMax(), 3.0, tempStrojenie, tempLambda);
        double[] tempGoal = {30.0, 30.0};
        abstractController.setSetpoint(tempGoal);
        assert (abstractController.getSetpoint()[0] == 30.0);
        double[] tempY = obiekt.simulateStep(abstractController.countControls(obiekt.getOutput()));
        double[] tempExpectedY = {2.6999, 1.500};
        assertArrayEquals(tempY, tempExpectedY, 0.1);
        obiekt.resetObject();
        abstractController.resetController();
        tempY = obiekt.simulateStep(abstractController.countControls(obiekt.getOutput()));
        assertArrayEquals(tempY, tempExpectedY, 0.1);
    }
}
