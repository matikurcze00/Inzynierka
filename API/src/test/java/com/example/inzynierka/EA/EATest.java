package com.example.inzynierka.EA;

import com.example.inzynierka.modele.ParObiektDPAMIMO;
import com.example.inzynierka.obiekty.MIMODPA;
import com.example.inzynierka.obiekty.SISODPA;
import com.example.inzynierka.regulatory.DMCAnalityczny;
import com.example.inzynierka.regulatory.PID;
import com.example.inzynierka.regulatory.Regulator;
import com.example.inzynierka.regulatory.ZbiorPID;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;

public class EATest {

    @Test
    public void PIDSisoTest()
    {
        SISODPA SISODPA = new SISODPA(10.0, 1.0, 1, 1.0, 0, 1.0, 3.0, 5.0, 0, 1, 100.0, -100.0, "srednio");
        Regulator regulator = new PID(1.0, 1.0, 1.0, 1.0, new double[]{100.0} , 3.0, 100.0, new Double[]{null, null, null});
        int populacja = 100;
        int iteracja = 20;
        int elita = 3;
        AlgorytmEwolucyjny GA = new AlgorytmEwolucyjny(populacja, iteracja, elita, 0.3, 0.2);
        assert(GA.getRozmiarPopulacji()==100);
        assert(GA.getLiczbaIteracji()==20);
        assert(GA.getRozmiarElity()==3);
        assert(GA.getIloscKrzyzowania()==Math.floor((populacja-elita)*0.2));
        assert(GA.getIloscMutacji()==GA.getRozmiarPopulacji()-GA.getIloscKrzyzowania()-GA.getRozmiarElity());
        assert(GA.getPrawdopodobienstwoMutacji()==0.3);
        double[] tempWartosciGA = GA.dobierzWartosci(regulator.liczbaZmiennych(), regulator, SISODPA);
        assert(tempWartosciGA[0]!=0.0);

    }

    @Test
    public void DMCSisoTest()
    {
        SISODPA SISODPA = new SISODPA(10.0, 1.0, 1, 1.0, 0, 1.0, 1.0, 0.0, 0, 1, 100.0, -100.0, "srednio");
        Regulator regulator = new DMCAnalityczny(4, 0.1, SISODPA, SISODPA.getYMax() / 2, 3.0, 11);;
        int populacja = 100;
        int iteracja = 20;
        int elita = 3;
        AlgorytmEwolucyjny GA = new AlgorytmEwolucyjny(populacja, iteracja, elita, 0.3, 0.2);
        assert(GA.getRozmiarPopulacji()==100);
        assert(GA.getLiczbaIteracji()==20);
        assert(GA.getRozmiarElity()==3);
        assert(GA.getIloscKrzyzowania()==(populacja-elita)*0.8);
        assert(GA.getIloscMutacji()==GA.getRozmiarPopulacji()-GA.getIloscKrzyzowania()-GA.getRozmiarElity());
        assert(GA.getPrawdopodobienstwoMutacji()==0.3);
        double[] tempWartosciGA = GA.dobierzWartosci(regulator.liczbaZmiennych(), regulator, SISODPA);
        assert(tempWartosciGA[0]!=0.0);
    }

    @Test
    public void PIDMimoTest() throws IOException
    {
        ObjectMapper objectMapper = new ObjectMapper();
        Integer[] PV = objectMapper.treeToValue(objectMapper.readTree(new FileInputStream("src/main/java/com/example/inzynierka/ObiektMIMO.json")).path("PV"), Integer[].class);
        ParObiektDPAMIMO[] Obiekty = objectMapper.treeToValue(objectMapper.readTree(new FileInputStream("src/main/java/com/example/inzynierka/ObiektMIMO.json")).path("ParObiektMIMO"), ParObiektDPAMIMO[].class);
        MIMODPA obiekt  = new MIMODPA(Obiekty, "srednio");
        Double[] tempStrojenie = new Double[]{1.0, 1.0, 1.0, null, null, null};
        Regulator regulator = new ZbiorPID(obiekt,PV, 3.0, tempStrojenie);
        int populacja = 100;
        int iteracja = 20;
        int elita = 3;
        AlgorytmEwolucyjny GA = new AlgorytmEwolucyjny(populacja, iteracja, elita, 0.3, 0.4);
        assert(GA.getRozmiarPopulacji()==100);
        assert(GA.getLiczbaIteracji()==20);
        assert(GA.getRozmiarElity()==3);
        assert(GA.getIloscKrzyzowania()==Math.ceil((populacja-elita)*0.6));
        assert(GA.getIloscMutacji()==GA.getRozmiarPopulacji()-GA.getIloscKrzyzowania()-GA.getRozmiarElity());
        assert(GA.getPrawdopodobienstwoMutacji()==0.3);
        double[] tempWartosciGA = GA.dobierzWartosci(regulator.liczbaZmiennych(), regulator, obiekt);
        assert(tempWartosciGA[0]!=0.0);
        assert(tempWartosciGA.length==3);
    }

    @Test
    public void DMCMimoTest() throws IOException
    {
        ObjectMapper objectMapper = new ObjectMapper();
        Integer[] PV = objectMapper.treeToValue(objectMapper.readTree(new FileInputStream("src/main/java/com/example/inzynierka/ObiektMIMO.json")).path("PV"), Integer[].class);
        ParObiektDPAMIMO[] Obiekty = objectMapper.treeToValue(objectMapper.readTree(new FileInputStream("src/main/java/com/example/inzynierka/ObiektMIMO.json")).path("ParObiektDPAMIMO"), ParObiektDPAMIMO[].class);
        MIMODPA obiekt  = new MIMODPA(Obiekty, "srednio");
        double[] tempLambda = {0.5,0.5};
        Double[] tempStrojenie = new Double[]{1.0, null};
        Regulator regulator = new DMCAnalityczny(5, tempLambda, obiekt, obiekt.getYMax(), 3.0, 11, tempStrojenie);
        int populacja = 100;
        int iteracja = 20;
        int elita = 3;
        AlgorytmEwolucyjny GA = new AlgorytmEwolucyjny(populacja, iteracja, elita, 0.3, 0.4);
        assert(GA.getRozmiarPopulacji()==100);
        assert(GA.getLiczbaIteracji()==20);
        assert(GA.getRozmiarElity()==3);
        assert(GA.getIloscKrzyzowania()==Math.ceil((populacja-elita)*0.6));
        assert(GA.getIloscMutacji()==GA.getRozmiarPopulacji()-GA.getIloscKrzyzowania()-GA.getRozmiarElity());
        assert(GA.getPrawdopodobienstwoMutacji()==0.3);
        double[] tempWartosciGA = GA.dobierzWartosci(regulator.liczbaZmiennych(), regulator, obiekt);
        assert(tempWartosciGA[0]!=0.0);
        assert(tempWartosciGA.length==1);
    }
}
