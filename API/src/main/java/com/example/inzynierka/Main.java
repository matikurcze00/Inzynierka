package com.example.inzynierka;


import com.example.inzynierka.EA.AlgorytmEwolucyjny;
import com.example.inzynierka.obiekty.SISO;
import com.example.inzynierka.regulatory.DMC;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        Double[] b = {10.0, 40.0, 5.0};
        Double[] z = {3.0, -300.0};
        SISO siso = new SISO(z,b,5, 100, 1.0, 0, 0);
        List<Double> Y = new ArrayList<>();
        double[] resetD = {0};
        AlgorytmEwolucyjny GA = new AlgorytmEwolucyjny(100, 400, 10, 0.3,0.2);
        DMC dmc = new DMC(4,0.1, siso, siso.getYMax()/2, 3, 20);
        //System.out.println(dmc.getD());
        Double srednia = 0.0;
        for(int k = 0; k<3; k++) {
            dmc.zmienWartosci(resetD);
            double[] tempD = GA.dobierzWartosci(dmc.liczbaZmiennych(), dmc, siso);
            dmc.zmienWartosci(tempD);
            Y.clear();
            siso.resetObiektu();
            for (int i = 0; i < 100; i++) {
                Y.add(siso.obliczKrok(dmc.policzOutput(siso.getAktualna())));
            }
            double blad = 0.0;
            for (int i = 0; i < 50; i++) {
                blad += Math.pow(Y.get(i) - dmc.getCel(), 2);
            }
            blad = blad / Y.size();
            System.out.println("BLAD:" + blad);
//            System.out.println("Y : " + Y);
            System.out.println("DMC : " + dmc.getLambda());
//            System.out.println("Zadane : " + dmc.getCel());
            srednia +=blad;
        }
        srednia=srednia/3;
        System.out.println("Sredni blad dla Nu = 18: " + srednia );

        DMC dmc2 = new DMC(4,0.1, siso, siso.getYMax()/2, 3, 20);
        //System.out.println(dmc.getD());
        srednia = 0.0;
        for(int k = 0; k<3; k++) {
            dmc2.zmienWartosci(resetD);
            double[] tempD = GA.dobierzWartosci(dmc2.liczbaZmiennych(), dmc2, siso);
            dmc2.zmienWartosci(tempD);
            Y.clear();
            siso.resetObiektu();
            for (int i = 0; i < 100; i++) {
                Y.add(siso.obliczKrok(dmc2.policzOutput(siso.getAktualna())));
            }
            double blad = 0.0;
            for (int i = 0; i < 50; i++) {
                blad += Math.pow(Y.get(i) - dmc2.getCel(), 2);
            }
            blad = blad / Y.size();
            System.out.println("BLAD:" + blad);
//            System.out.println("Y : " + Y);
            System.out.println("DMC : " + dmc2.getLambda());
//            System.out.println("Zadane : " + dmc.getCel());
            srednia +=blad;
        }
        srednia=srednia/3;
        System.out.println("Sredni blad dla Nu = 20: " + srednia );

        DMC dmc4 = new DMC(4,0.1, siso, siso.getYMax()/2, 3, 4);
        //System.out.println(dmc.getD());
        srednia = 0.0;
        for(int k = 0; k<3; k++) {
            dmc4.zmienWartosci(resetD);
            double[] tempD = GA.dobierzWartosci(dmc4.liczbaZmiennych(), dmc4, siso);
            dmc4.zmienWartosci(tempD);
            Y.clear();
            siso.resetObiektu();
            for (int i = 0; i < 100; i++) {
                Y.add(siso.obliczKrok(dmc4.policzOutput(siso.getAktualna())));
            }
            double blad = 0.0;
            for (int i = 0; i < 50; i++) {
                blad += Math.pow(Y.get(i) - dmc4.getCel(), 2);
            }
            blad = blad / Y.size();
            System.out.println("BLAD:" + blad);
//            System.out.println("Y : " + Y);
            System.out.println("DMC : " + dmc4.getLambda());
//            System.out.println("Zadane : " + dmc.getCel());
            srednia +=blad;
        }
        srednia=srednia/3;
        System.out.println("Sredni blad dla Nu = 4: " + srednia );

        DMC dmc8 = new DMC(4,0.1, siso, siso.getYMax()/2, 3, 16);
        //System.out.println(dmc.getD());
        srednia = 0.0;
        for(int k = 0; k<3; k++) {
            dmc8.zmienWartosci(resetD);
            double[] tempD = GA.dobierzWartosci(dmc8.liczbaZmiennych(), dmc8, siso);
            dmc8.zmienWartosci(tempD);
            Y.clear();
            siso.resetObiektu();
            for (int i = 0; i < 100; i++) {
                Y.add(siso.obliczKrok(dmc8.policzOutput(siso.getAktualna())));
            }
            double blad = 0.0;
            for (int i = 0; i < 50; i++) {
                blad += Math.pow(Y.get(i) - dmc8.getCel(), 2);
            }
            blad = blad / Y.size();
            System.out.println("BLAD:" + blad);
//            System.out.println("Y : " + Y);
            System.out.println("DMC : " + dmc8.getLambda());
//            System.out.println("Zadane : " + dmc.getCel());
            srednia +=blad;
        }
        srednia=srednia/3;
        System.out.println("Sredni blad dla Nu = 16: " + srednia );

        DMC dmc10 = new DMC(4,0.1, siso, siso.getYMax()/2, 3, 10);
        //System.out.println(dmc.getD());
        srednia = 0.0;
        for(int k = 0; k<3; k++) {
            dmc10.zmienWartosci(resetD);
            double[] tempD = GA.dobierzWartosci(dmc10.liczbaZmiennych(), dmc10, siso);
            dmc10.zmienWartosci(tempD);
            Y.clear();
            siso.resetObiektu();
            for (int i = 0; i < 100; i++) {
                Y.add(siso.obliczKrok(dmc10.policzOutput(siso.getAktualna())));
            }
            double blad = 0.0;
            for (int i = 0; i < 50; i++) {
                blad += Math.pow(Y.get(i) - dmc10.getCel(), 2);
            }
            blad = blad / Y.size();
            System.out.println("BLAD:" + blad);
//            System.out.println("Y : " + Y);
            System.out.println("DMC : " + dmc10.getLambda());
//            System.out.println("Zadane : " + dmc.getCel());
            srednia +=blad;
        }
        srednia=srednia/3;
        System.out.println("Sredni blad dla Nu = 10: " + srednia );

        DMC dmc13 = new DMC(4,0.1, siso, siso.getYMax()/2, 3, 13);
        //System.out.println(dmc.getD());
        srednia = 0.0;
        for(int k = 0; k<3; k++) {
            dmc13.zmienWartosci(resetD);
            double[] tempD = GA.dobierzWartosci(dmc13.liczbaZmiennych(), dmc13, siso);
            dmc13.zmienWartosci(tempD);
            Y.clear();
            siso.resetObiektu();
            for (int i = 0; i < 100; i++) {
                Y.add(siso.obliczKrok(dmc13.policzOutput(siso.getAktualna())));
            }
            double blad = 0.0;
            for (int i = 0; i < 50; i++) {
                blad += Math.pow(Y.get(i) - dmc13.getCel(), 2);
            }
            blad = blad / Y.size();
            System.out.println("BLAD:" + blad);
//            System.out.println("Y : " + Y);
            System.out.println("DMC : " + dmc13.getLambda());
//            System.out.println("Zadane : " + dmc.getCel());
            srednia +=blad;
        }
        srednia=srednia/3;
        System.out.println("Sredni blad dla Nu = 13: " + srednia );
    }

}
