package com.company;

import com.company.EA.AlgorytmEwolucyjny;
import com.company.regulatory.DMC;
import com.company.regulatory.PID;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Main {

    public static void main(String[] args) {
        double[] b = {1.0,2.0};
        double[] z = {3.0,4.0,5.0};
        Obiekt obiekt = new Obiekt(b,z,10, 100,0,1);
        List<Double> Y = new ArrayList<>();

//        PID pid = new PID(0.0,0.0,0.0,0.5,29.0, 3, 100, 0);
//        AlgorytmEwolucyjny GA = new AlgorytmEwolucyjny(1000, 400, 100, 0.3,0.2);
        DMC dmc = new DMC(2,0.1,obiekt, 29.0, 3);
//        double[] tempD = GA.dobierzWartosci(3,pid,obiekt);
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
//        System.out.println("CEL: " + pid.getCel());
    }
}
