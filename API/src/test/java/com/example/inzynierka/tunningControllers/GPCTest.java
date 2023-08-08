package com.example.inzynierka.tunningControllers;

import com.example.inzynierka.models.ParObiektRownaniaMIMO;
import com.example.inzynierka.objects.MIMODiscrete;
import com.example.inzynierka.objects.SISODiscrete;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;

class GPCTest {

    @Test
    void GPCSISOTest() {
        SISODiscrete SISO = new SISODiscrete(-0.5, 0.0, 0.0, 0.0, 0.0,
            0.4, 0.3, 0.0, 0.0, 0.0, 100, -30, "srednio");
        ControllerTunning controllerTunning = new GPC(SISO, 0.1, SISO.getYMax() / 2, 3.0);
        controllerTunning.setSetpoint(new double[] {30.0});
        assert (controllerTunning.getSetpoint()[0] == 30.0);
        double tempY = SISO.simulateStep(controllerTunning.countControls(SISO.getOutput()));
        double expectedY = 1.2000000000000002;
        assert (tempY == expectedY);
        SISO.resetObject();
        controllerTunning.resetController();
        tempY = SISO.simulateStep(controllerTunning.countControls(SISO.getOutput()));
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
        ControllerTunning controllerTunning = new GPC(obiekt, 5, obiekt.getYMax(), 3.0, tempStrojenie, tempLambda);
        double[] tempCel = {30.0, 30.0};
        controllerTunning.setSetpoint(tempCel);
        assert (controllerTunning.getSetpoint()[0] == 30.0);
        double[] tempY = obiekt.simulateStep(controllerTunning.countControls(obiekt.getOutput()));
        double[] tempExpectedY = {2.6999, 1.500};
        assertArrayEquals(tempY, tempExpectedY, 0.1);
        obiekt.resetObject();
        controllerTunning.resetController();
        tempY = obiekt.simulateStep(controllerTunning.countControls(obiekt.getOutput()));
        assertArrayEquals(tempY, tempExpectedY, 0.1);
    }
}
