package com.example.inzynierka.EA;

import com.example.inzynierka.models.ParObiektDPAMIMO;
import com.example.inzynierka.objects.MIMODPA;
import com.example.inzynierka.objects.SISODPA;
import com.example.inzynierka.tunningControllers.DMCController;
import com.example.inzynierka.tunningControllers.PIDController;
import com.example.inzynierka.tunningControllers.AbstractController;
import com.example.inzynierka.tunningControllers.PIDCollection;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;

class EATest {

    @Test
    void PIDSisoTest() {
        SISODPA SISODPA = new SISODPA(10.0, 1.0, 1, 1.0, 0, 1.0, 3.0, 5.0, 0, 1, 100.0, -100.0, "srednio");
        AbstractController abstractController = new PIDController(1.0, 1.0, 1.0, 1.0, new double[] {100.0}, 3.0, 100.0, new Double[] {null, null, null});
        int populacja = 50;
        int iteracja = 3;
        int mu = 100;
        EvolutionaryAlgorithm GA = new EvolutionaryAlgorithm(populacja, iteracja, mu, 0.3, 0.2);
        assert (GA.getPopulationNumber() == populacja);
        assert (GA.getIterationNumber() == iteracja);
        assert (GA.getMu() == mu);
        assert (GA.getRecombinationNumber() == Math.floor(mu * 0.2));
        assert (GA.getMutationNumber() == GA.getMu() - GA.getRecombinationNumber());
        assert (GA.getMutationProbability() == 0.3);
        double[] tempWartosciGA = GA.getTuningParameters(abstractController.getNumberOfTuningParameters(), abstractController, SISODPA);
        assert (tempWartosciGA[0] != 0.0);
        assert (tempWartosciGA.length == 3);
    }

    @Test
    void DMCSisoTest() {
        SISODPA SISODPA = new SISODPA(10.0, 1.0, 1, 1.0, 0, 1.0, 3.0, 5.0, 0, 1, 100.0, -100.0, "srednio");
        AbstractController abstractController = new DMCController(4, 0.1, SISODPA, SISODPA.getYMax() / 2, 3.0, 11);
        int populacja = 50;
        int iteracja = 3;
        int mu = 100;
        EvolutionaryAlgorithm GA = new EvolutionaryAlgorithm(populacja, iteracja, mu, 0.3, 0.2);
        assert (GA.getPopulationNumber() == populacja);
        assert (GA.getIterationNumber() == iteracja);
        assert (GA.getMu() == mu);
        assert (GA.getRecombinationNumber() == mu * 0.2);
        assert (GA.getMutationNumber() == GA.getMu() - GA.getRecombinationNumber());
        assert (GA.getMutationProbability() == 0.3);
        double[] tempWartosciGA = GA.getTuningParameters(abstractController.getNumberOfTuningParameters(), abstractController, SISODPA);
        assert (tempWartosciGA[0] != 0.0);
        assert (tempWartosciGA.length == 1);
    }

    @Test
    void PIDMimoTest() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Integer[] PV =
            objectMapper.treeToValue(objectMapper.readTree(new FileInputStream("src/main/java/com/example/inzynierka/ObiektMIMODPA.json")).path("PV"),
                Integer[].class);
        ParObiektDPAMIMO[] Obiekty = objectMapper.treeToValue(
            objectMapper.readTree(new FileInputStream("src/main/java/com/example/inzynierka/ObiektMIMODPA.json")).path("ParObiektDPAMIMO"),
            ParObiektDPAMIMO[].class);
        MIMODPA obiekt = new MIMODPA(Obiekty, "srednio");
        Double[] tempStrojenie = new Double[] {1.0, 1.0, 1.0, null, null, null};
        AbstractController abstractController = new PIDCollection(obiekt, PV, 3.0, tempStrojenie);
        int populacja = 50;
        int iteracja = 3;
        int mu = 100;
        EvolutionaryAlgorithm GA = new EvolutionaryAlgorithm(populacja, iteracja, mu, 0.3, 0.4);
        assert (GA.getPopulationNumber() == 50);
        assert (GA.getIterationNumber() == 3);
        assert (GA.getMu() == 100);
        assert (GA.getRecombinationNumber() == Math.floor(mu * 0.4));
        assert (GA.getMutationNumber() == GA.getMu() - GA.getRecombinationNumber());
        assert (GA.getMutationProbability() == 0.3);
        double[] tempWartosciGA = GA.getTuningParameters(abstractController.getNumberOfTuningParameters(), abstractController, obiekt);
        assert (tempWartosciGA[0] != 0.0);
        assert (tempWartosciGA.length == 3);
    }

    @Test
    void DMCMimoTest() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Integer[] PV =
            objectMapper.treeToValue(objectMapper.readTree(new FileInputStream("src/main/java/com/example/inzynierka/ObiektMIMODPA.json")).path("PV"),
                Integer[].class);
        ParObiektDPAMIMO[] Obiekty = objectMapper.treeToValue(
            objectMapper.readTree(new FileInputStream("src/main/java/com/example/inzynierka/ObiektMIMODPA.json")).path("ParObiektDPAMIMO"),
            ParObiektDPAMIMO[].class);
        MIMODPA obiekt = new MIMODPA(Obiekty, "srednio");
        double[] tempLambda = {0.5, 0.5};
        Double[] tempStrojenie = new Double[] {1.0, null};
        AbstractController abstractController = new DMCController(5, tempLambda, obiekt, obiekt.getYMax(), 3.0, 11, tempStrojenie);
        int populacja = 50;
        int iteracja = 3;
        int mu = 100;
        EvolutionaryAlgorithm GA = new EvolutionaryAlgorithm(populacja, iteracja, mu, 0.3, 0.4);
        assert (GA.getPopulationNumber() == 50);
        assert (GA.getIterationNumber() == 3);
        assert (GA.getMu() == 100);
        assert (GA.getRecombinationNumber() == Math.floor(mu * 0.4));
        assert (GA.getMutationNumber() == mu - GA.getRecombinationNumber());
        assert (GA.getMutationProbability() == 0.3);
        double[] tempWartosciGA = GA.getTuningParameters(abstractController.getNumberOfTuningParameters(), abstractController, obiekt);
        assert (tempWartosciGA[0] != 0.0);
        assert (tempWartosciGA.length == 1);
    }
}
