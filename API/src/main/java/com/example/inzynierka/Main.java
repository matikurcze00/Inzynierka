package com.example.inzynierka;


import com.example.inzynierka.EA.AlgorytmEwolucyjny;
import com.example.inzynierka.regulatory.DMC;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        Double[] b = {1.0,2.0};
        Double[] z = {3.0,4.0,5.0};
        Obiekt obiekt = new Obiekt(b,z,10, 100, 1.0, 0, 0);
        List<Double> Y = new ArrayList<>();

//        PID pid = new PID(0.0,0.0,0.0,0.5,29.0, 3, 100, 0);
//        AlgorytmEwolucyjny GA = new AlgorytmEwolucyjny(10, 40, 10, 0.3,0.2);
        DMC dmc = new DMC(3+ obiekt.getOpoznienie(),0.1,obiekt, obiekt.getYMax()/2, 3);
//        double[] tempD = GA.dobierzWartosci(dmc.liczbaZmiennych(),dmc,obiekt);
//        dmc.zmienWartosci(tempD);
        double[] tempL = {0.001};
        dmc.zmienWartosci(tempL);

        Y.clear();
        obiekt.resetObiektu();
//        pid.zmienWartosci(tempD);
//        pid.setCel(obiekt.getYMax()/3);
        for (int i = 0; i<100; i++)
        {
            Y.add(obiekt.obliczKrok(dmc.policzOutput(obiekt.getAktualna())));
//            Y.add(obiekt.obliczKrok(pid.policzOutput(obiekt.getAktualna())));
        }
        double blad = 0.0;
        for(int i = 0; i<50; i++)
        {
            blad+=Math.pow(Y.get(i)-dmc.getCel(),2);
        }
        blad=blad/Y.size();
        System.out.println("BLAD:" + blad);
//        System.out.println("Wartosci pid" + pid.getK() +"| " + pid.getTi() + " | "+pid.getTi());
        System.out.println("Y : " + Y);
        System.out.println("DMC : " + dmc.getLambda());
        System.out.println("Zadane : " + dmc.getCel());
    }
}
