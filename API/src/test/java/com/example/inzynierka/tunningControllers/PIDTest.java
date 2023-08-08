package com.example.inzynierka.tunningControllers;

import com.example.inzynierka.models.ParObiektDPAMIMO;
import com.example.inzynierka.objects.MIMODPA;
import com.example.inzynierka.objects.SISODPA;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;

class PIDTest {

    @Test
    void SISOTest() {
        SISODPA SISODPA = new SISODPA(10.0, 1.0, 1, 1.0, 0, 1.0, 1.0, 0.0, 0, 1, 100.0, -100.0, "srednio");
        ControllerTunning controllerTunning = new PID(1.0, 1.0, 1.0, 1.0, new double[] {100.0}, 3.0, 100.0, new Double[] {null, null, null});
        controllerTunning.setSetpoint(new double[] {30.0});
        assert (controllerTunning.getSetpoint()[0] == 30.0);
        double tempY = SISODPA.simulateStep(controllerTunning.countControls(SISODPA.getOutput()));
        double expectedY = 20.0;
        assert (tempY == expectedY);
        SISODPA.resetObject();
        controllerTunning.resetController();
        tempY = SISODPA.simulateStep(controllerTunning.countControls(SISODPA.getOutput()));
        assert (tempY == expectedY);
    }

    @Test
    void MIMOTest() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Integer[] PV =
            objectMapper.treeToValue(objectMapper.readTree(new FileInputStream("src/main/java/com/example/inzynierka/ObiektMIMODPA.json")).path("PV"),
                Integer[].class);
        ParObiektDPAMIMO[] Obiekty = objectMapper.treeToValue(
            objectMapper.readTree(new FileInputStream("src/main/java/com/example/inzynierka/ObiektMIMODPA.json")).path("ParObiektDPAMIMO"),
            ParObiektDPAMIMO[].class);
        MIMODPA obiekt = new MIMODPA(Obiekty, "srednio");
        Double[] tempStrojenie = new Double[] {1.0, 1.0, 1.0, 1.0, 1.0, 1.0};
        ControllerTunning controllerTunning = new PIDCollection(obiekt, PV, 3.0, tempStrojenie);
        double[] tempCel = {30.0, 30.0};
        controllerTunning.setSetpoint(tempCel);
        assert (controllerTunning.getSetpoint()[0] == 30.0);
        double[] tempY = obiekt.simulateStep(controllerTunning.countControls(obiekt.getOutput()));
        double[] tempExpectedY = {26.23965651834505, 26.23965651834505};
        assertArrayEquals(tempY, tempExpectedY, 0.1);
        obiekt.resetObject();
        controllerTunning.resetController();
        tempY = obiekt.simulateStep(controllerTunning.countControls(obiekt.getOutput()));
        assertArrayEquals(tempY, tempExpectedY, 0.1);
    }
}
