package com.company;

import com.company.regulatory.PID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        Obiekt obiekt = new Obiekt(0.6,2,0.7,0.2);
        List<Double> Y = new ArrayList<>();

        PID pid = new PID(-0.1212,-0.196,-0.3265,0.5,29.0, 3, 100, -100);
        AlgorytmEwolucyjny GA = new AlgorytmEwolucyjny();
        for (int i = 0; i<40; i++)
        {
            Y.add(obiekt.obliczKrok(pid.policzOutput(obiekt.getAktualna())));
        }
        double[] tempD = {1.0,10000.0,0.0};
        System.out.println(Y);
        System.out.println((GA.dobierzWartosci(3,pid))[0]);
    }
}
