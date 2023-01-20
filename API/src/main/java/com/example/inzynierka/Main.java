package com.example.inzynierka;


import com.example.inzynierka.EA.AlgorytmEwolucyjny;
import com.example.inzynierka.modele.ParObiektMIMO;
import com.example.inzynierka.obiekty.MIMO;
import com.example.inzynierka.obiekty.SISO;
import com.example.inzynierka.regulatory.DMC;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        ////MIMO
        ObjectMapper objectMapper = new ObjectMapper();
        ParObiektMIMO[] Obiekty = objectMapper.readValue(new File("src/main/java/com/example/inzynierka/ObiektMIMO.json"), ParObiektMIMO[].class);
        System.out.println(Obiekty[0]);
        System.out.println(Obiekty[1]);
        MIMO obiekt  = new MIMO(Obiekty);
        double[] du = {2.0,8.0};
        double[] zero = {0.0, 0.0};
        List<double[]> Y = new ArrayList<>();
        Y.add(obiekt.obliczKrok(du));
        Y.add(obiekt.obliczKrok(du));
        Y.add(obiekt.obliczKrok(du));
        double[] tempLambda = {0.1,0.1};
        DMC regulator = new DMC(4, tempLambda, obiekt, obiekt.getYMax(), 3, 3);
        for(int k = 0; k<40; k++)
        {
                Y.add(obiekt.obliczKrok(regulator.policzOutput(obiekt.getAktualne())));

        }
        System.out.println(Y);
        //300;1500
        ////SISO
//        Double[] b = {1.0, 4.0, 5.0};
//        Double[] z = {3.0, 3.5};
//        SISO siso = new SISO(z,b,5, 100, 1.0, 0, 0);
//        List<Double> Y = new ArrayList<>();
//        double[] resetD = {0};
//        AlgorytmEwolucyjny GA = new AlgorytmEwolucyjny(100, 400, 10, 0.3,0.2);
//        DMC dmc = new DMC(4,0.1, siso, siso.getYMax()/2, 3, 10);
//
//        Double srednia = 0.0;
//        for(int k = 0; k<3; k++) {
//            dmc.zmienWartosci(resetD);
//            double[] tempD = GA.dobierzWartosci(dmc.liczbaZmiennych(), dmc, siso);
//            dmc.zmienWartosci(tempD);
//            Y.clear();
//            siso.resetObiektu();
//            for (int i = 0; i < 100; i++) {
//                Y.add(siso.obliczKrok(dmc.policzOutput(siso.getAktualna())));
//            }
//            double blad = 0.0;
//            for (int i = 0; i < 50; i++) {
//                blad += Math.pow(Y.get(i) - dmc.getCel(), 2);
//            }
//            blad = blad / Y.size();
//            System.out.println("BLAD:" + blad);
//            System.out.println("Y : " + Y);
//            System.out.println("DMC : " + dmc.getLambda());
//            System.out.println("Zadane : " + dmc.getCel());
//            srednia +=blad;
//        }
//        srednia=srednia/3;
//        System.out.println("Sredni blad dla Nu = 18: " + srednia );


    }

}
