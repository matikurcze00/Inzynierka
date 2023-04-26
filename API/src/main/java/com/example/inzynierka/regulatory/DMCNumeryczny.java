package com.example.inzynierka.regulatory;

import Jama.Matrix;
import com.example.inzynierka.obiekty.SISODPA;


import java.util.ArrayList;
import java.util.Arrays;

public class DMCNumeryczny extends DMCAnalityczny{

    public DMCNumeryczny(int Nu, double lambda, SISODPA SISODPA, double cel, double duMax, int N, Double[] strojenieZadane) {
        this(Nu, lambda, SISODPA, cel, duMax, N);
        if (strojenieZadane[0] != null) {
            liczbaStrojeniaZadanego = 1;
            this.strojenieZadane = strojenieZadane;
            this.getLambda().set(0, strojenieZadane[0]);
            this.policzWartosci(SISODPA);
        }
    }
    public DMCNumeryczny(int Nu, double lambda, SISODPA SISODPA, double cel, double duMax, int N) {
        this.Lambda = Arrays.asList(lambda);
        this.Nu = Nu;
        this.N = N;
        this.cel = new double[]{cel};
        this.duMax = duMax;
        policzWartosci(SISODPA);
    }
    @Override
    protected void policzWartosci(SISODPA SISODPA)
    {
        this.S = new ArrayList();
        policzS(SISODPA);
        policzMp();
        policzM();
        resetujRegulator(1);
        SISODPA.resetObiektu();
    }

    public double policzSterowanie(double aktualna) {
        Matrix yZad = ustawMatrixYZad();
        double[] yTemp = new double[N];
        Arrays.fill(yTemp, aktualna);
        Matrix y = new Matrix(yTemp, 1);
        //TODO: zaimlementowac numeryczne obliczenia
//        Matrix Utemp = K.times(yZad.transpose().minus(y.transpose()).minus(Mp.times(dU.transpose())));
//        poprawaUTemp(Utemp, 0);
//        dodajdU(Utemp.get(0, 0));
//        return Utemp.get(0, 0);
        return 0.0;
    }
}
