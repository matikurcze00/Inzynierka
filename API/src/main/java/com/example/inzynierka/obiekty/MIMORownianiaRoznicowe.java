package com.example.inzynierka.obiekty;

import com.example.inzynierka.regulatory.Regulator;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class MIMORownianiaRoznicowe extends MIMO{
    List<List<Double>> A;
    List<List<Double>> B;
    List<List<Double>> Y;
    List<List<Double>> U;
    private double[] Ypp;
    private double[] Upp;
    private double[] uMin;
    private double[] uMax;
    private double[] YMax;
    private String blad;
    private int OUT;
    private int IN;
    private int[] delay;
    private int delayMax = 0;
    private int dlugosc;

    public double obliczPraceObiektu(Regulator regulator, double[] cel) {

        resetObiektu();
        double blad = 0.0;

        blad = obliczPraceBezZaklocen(regulator, cel, blad);

        blad = blad / this.dlugosc * OUT * OUT;
        resetObiektu();
        return blad;
    }

    private double obliczPraceBezZaklocen(Regulator regulator, double[] cel, double blad) {
        double[] tempCel = new double[OUT];
        for (int k = 0; k < OUT; k++) {
            for (int i = 0; i < OUT; i++)
                tempCel[i] = 0;
            tempCel[k] = cel[k];
            regulator.setCel(tempCel);
            resetObiektu();
            regulator.resetujRegulator();
            for (int i = 0; i < this.dlugosc; i++) {
                double[] Ytepm = obliczKrok(regulator.policzSterowanie(getAktualne()));
                for (int j = 0; j < Ytepm.length; j++) {
                    if (this.blad.equals("srednio"))
                        blad += Math.pow(Ytepm[j] - tempCel[j], 2);
                    else if (this.blad.equals("absolutny"))
                        blad += Math.abs(Ytepm[j] - tempCel[j]);
                }
            }
        }
        return blad;
    }

    public double[] obliczKrok(double[] du) {
        obliczU(du);
        double[] Yakt = new double[OUT];

        for(int i = 0; i < OUT; i++) {
            Yakt[i] = obliczWyjscie(i);
        }
        dodajY(Yakt);
        return Yakt;
    }
    public double[] obliczKrok(double[] du, double[]dz) {
        return obliczKrok(du);
    }
    public double obliczKrokZaklocenia(double du, int IN, int OUT) {
        return 0.0;
    }

    public double obliczKrok(double du, int IN, int OUT) {
        obliczU(du, IN);
        double YaktIN = obliczWyjscie(OUT);
        dodajY(OUT, YaktIN);
        return YaktIN;
    }

    public double[] getAktualne() {
        double[] YAkt = new double[OUT];
        for (int i = 0; i < OUT; i++)
            YAkt[i] = Y.get(i).get(0);
        return YAkt;
    }

    private double obliczWyjscie(int i ) {
        double Yakt = 0.0;
        for(int j = 0; i < B.get(i).size(); j++)
            Yakt += B.get(i).get(j) * U.get(i).get(j);

        for(int j = 0; j < A.get(i).size(); j++)
            Yakt -= A.get(i).get(j) * Y.get(i).get(j);
        return Yakt;
    }
    public void resetObiektu() {
        for (int i = 0; i < this.OUT; i++) {
            Y.set(i, new ArrayList(Collections.nCopies(3, Ypp[i])));
        }
        for (int i = 0; i < this.IN; i++) {
            U.set(i, new ArrayList(Collections.nCopies(3 + delayMax, Upp[i])));
        }
    }
    public void obliczU(double[] du) {
        for (int j = 0; j < IN; j++) {
            double Uakt = U.get(j).get(0) + du[j];
            if (Uakt > uMax[j])
                Uakt = uMax[j];
            else if (Uakt < uMin[j])
                Uakt = uMin[j];

            for (int i = U.get(j).size() - 1; i > 0; i--)
                U.get(j).set(i, U.get(j).get(i - 1));
            U.get(j).set(0, Uakt);
        }
    }
    public void obliczU(double du, int IN) {
        double Uakt = U.get(IN).get(0) + du;
        if (Uakt > uMax[IN]) {
            Uakt = uMax[IN];
        } else if (Uakt < uMin[IN]) {
            Uakt = uMin[IN];
        }
        for (int i = U.get(IN).size() - 1; i > 0; i--)
            U.get(IN).set(i, U.get(IN).get(i - 1));
        U.get(IN).set(0, Uakt);
    }
    private void dodajY(double[] Yakt) {
        for (int i = 0; i < OUT; i++) {
            List<Double> Ytemp = Y.get(i);
            for (int j = Y.get(i).size() - 1; j > 0; j--)
                Ytemp.set(j, Ytemp.get(j - 1));
            Ytemp.set(0, Yakt[i]);
            Y.set(i, Ytemp);
        }
    }
    private void dodajY(int OUT, double YaktIN) {
        List<Double> Ytemp = Y.get(OUT);
        for (int j = Y.get(OUT).size() - 1; j > 0; j--)
            Ytemp.set(j, Ytemp.get(j - 1));
        Ytemp.set(0, YaktIN);
        Y.set(OUT, Ytemp);
    }
    private void obliczYMax() {
        double[] Ytemp;

        this.YMax = new double[OUT];
        for (int i = 0; i < OUT; i++)
            this.YMax[i] = 0.0;
        double[] uMax = new double[IN];
        for (int i = 0; i < IN; i++) {
            uMax[i] = this.uMax[i];
        }
        for (int i = 0; i < dlugosc * 2; i++) {
            obliczKrok(uMax);
        }
        Ytemp = getAktualne();
        resetObiektu();
        for (int i = 0; i < this.YMax.length; i++)
            if (this.YMax[i] < Ytemp[i])
                this.YMax[i] = Ytemp[i];
    }

    public void obliczDelayMax() {
        int tempDelayMax = delay[0];
        for (int i = 1; i < IN; i++) {
            if(tempDelayMax<delay[i])
                tempDelayMax = delay[i];
        }
        delayMax = tempDelayMax;
    }
}
