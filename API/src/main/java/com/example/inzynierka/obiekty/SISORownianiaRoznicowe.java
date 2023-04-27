package com.example.inzynierka.obiekty;


import com.example.inzynierka.modele.ParObiektRownania;
import com.example.inzynierka.regulatory.Regulator;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class SISORownianiaRoznicowe extends SISO{
    List<Double> A;
    List<Double> B;
    List<Double> Y;
    List<Double> U;
    private double Upp;
    private double Ypp;
    private double uMin;
    private double uMax;
    private double YMax;
    private int dlugosc;
    private String blad;

    public SISORownianiaRoznicowe(ParObiektRownania parObiektRownania, double uMax, double uMin, String blad) {
        this(parObiektRownania.getA1(), parObiektRownania.getA2(), parObiektRownania.getA3(), parObiektRownania.getA4(), parObiektRownania.getA5(),
            parObiektRownania.getB1(), parObiektRownania.getB2(), parObiektRownania.getB3(), parObiektRownania.getB4(), parObiektRownania.getB5(),
            uMax, uMin, blad);
    }
    public SISORownianiaRoznicowe(double A1, double A2, double A3, double A4, double A5,
                                  double B1, double B2, double B3, double B4, double B5,
                                  double uMax, double uMin, String blad) {
        this.A = new ArrayList<>();
        this.A.add(A1);
        this.A.add(A2);
        this.A.add(A3);
        this.A.add(A4);
        this.A.add(A5);
        this.B = new ArrayList<>();
        this.B.add(B1);
        this.B.add(B2);
        this.B.add(B3);
        this.B.add(B4);
        this.B.add(B5);
        this.uMax = uMax;
        this.uMin = uMin;
        this.blad = blad;
    }
    public Double getAktualna() {
        try {
            return Y.get(0);
        } catch (Exception ex) {
            return 0.0;
        }
    }

    public void resetObiektu() {
        setU(new ArrayList<Double>(Collections.nCopies(U.size(), Upp)));
        setY(new ArrayList<Double>(Collections.nCopies(Y.size(), Ypp)));
    }
    public double obliczPraceObiektu(Regulator regulator, double[] cel) {
        resetObiektu();
        regulator.setCel(cel);

        double[] Y = obliczPraceBezZaklocen(regulator);

        resetObiektu();
        return obliczBlad(Y, cel[0]);
    }
    private double[] obliczPraceBezZaklocen(Regulator regulator) {
        double[] Y = new double[dlugosc];
        for (int i = 0; i < this.dlugosc; i++) {
            Y[i] = obliczKrok(regulator.policzSterowanie(getAktualna()));
        }
        return Y;
    }

    public double obliczKrok(double du) {
        obliczU(du);
        double Yakt = obliczWyjscie();

        dodajY(Yakt);
        return Yakt;
    }
    public double obliczKrok(double du, double[] duZ) {
        return obliczKrok(du);
    }

    public double obliczKrokZaklocenia(double du, int zaklocenie) {
        return 0.0;
    }

    private double obliczWyjscie() {
        double Yakt = 0.0;
        for(int i = 0; i < B.size(); i++)
            Yakt += B.get(i) * U.get(i);

        for(int i = 0; i < A.size(); i++)
            Yakt -= A.get(i) * Y.get(i);
        return Yakt;
    }

    public void obliczU(double du) {
        double Uakt = U.get(0) + du;
        if (Uakt > uMax)
            Uakt = uMax;
        else if (Uakt < uMin)
            Uakt = uMin;
        for (int i = U.size() - 1; i > 0; i--)
            U.set(i, U.get(i - 1));
        U.set(0, Uakt);
    }

    private void dodajY(double Yakt) {
        for (int i = Y.size() - 1; i > 0; i--)
            Y.set(i, Y.get(i - 1));
        Y.set(0, Yakt);
    }

    public double obliczBlad(double[] Y, double yZad) {
        double bladTemp = 0.0;
        if (this.blad.equals("srednio"))
            for (int i = 0; i < this.dlugosc; i++)
                bladTemp += Math.pow(Y[i] - yZad, 2);
        else if (this.blad.equals("absolutny"))
            for (int i = 0; i < this.dlugosc; i++)
                bladTemp += Math.abs(Y[i] - yZad);
        return bladTemp / this.dlugosc;
    }
}
