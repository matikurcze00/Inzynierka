package com.example.inzynierka.obiekty;


import com.example.inzynierka.modele.ParObiektRownania;
import com.example.inzynierka.modele.ZakloceniaRownania;
import com.example.inzynierka.regulatory.Regulator;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class SISORownianiaRoznicowe extends SISO{
    private List<Double> A;
    private List<Double> B;
    private List<Double> Y;
    private List<Double> U;
    private List<Double[]> Bz;
    private List<List<Double>> Uz;
    private int liczbaZaklocen;
    private double Upp;
    private double Ypp;
    private double uMin;
    private double uMax;
    private double YMax;
    private int dlugosc;
    private String blad;

    public SISORownianiaRoznicowe(ParObiektRownania parObiektRownania, double uMax, double uMin, String blad, ZakloceniaRownania zakloceniaRownania) {
        this(parObiektRownania, uMax, uMin, blad);
        List<Double[]> BzTemp = new ArrayList<>();
        BzTemp.add(zakloceniaRownania.getB1());
        BzTemp.add(zakloceniaRownania.getB2());
        BzTemp.add(zakloceniaRownania.getB3());
        BzTemp.add(zakloceniaRownania.getB4());
        BzTemp.add(zakloceniaRownania.getB5());
        this.Bz = BzTemp;
        liczbaZaklocen = zakloceniaRownania.getB1().length;
        this.Uz = new ArrayList<>();
        for(int i = 0; i < liczbaZaklocen; i++) {
            Uz.add(new ArrayList(Collections.nCopies(5, 0.0)));
        }
    }
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
        this.Y = new ArrayList<>(Collections.nCopies(5, 0.0));
        this.U = new ArrayList<>(Collections.nCopies(5, 0.0));
        this.dlugosc = 50;
        obliczYMax();

    }
    public Double getAktualna() {
        try {
            return Y.get(0);
        } catch (Exception ex) {
            return 0.0;
        }
    }

    public void resetObiektu() {
        setU(new ArrayList<>(Collections.nCopies(U.size(), Upp)));
        setY(new ArrayList<>(Collections.nCopies(Y.size(), Ypp)));
        for (int i = 0; i < this.liczbaZaklocen; i++) {
            Uz.set(i, new ArrayList(Collections.nCopies(5, 0.0)));
        }
    }
    public double obliczPraceObiektu(Regulator regulator, double cel) {
        resetObiektu();
        regulator.setCel(new double[] {cel});

        double[] YSymulacji = obliczPraceBezZaklocen(regulator);

        resetObiektu();
        return obliczBlad(YSymulacji, cel);
    }
    public double obliczPraceObiektu(Regulator regulator, double[] cel) {
        resetObiektu();
        regulator.setCel(cel);

        double[] YSymulacji = obliczPraceBezZaklocen(regulator);

        resetObiektu();
        return obliczBlad(YSymulacji, cel[0]);
    }
    private double[] obliczPraceBezZaklocen(Regulator regulator) {
        double[] YSymulacji = new double[dlugosc];
        for (int i = 0; i < this.dlugosc; i++) {
            YSymulacji[i] = obliczKrok(regulator.policzSterowanie(getAktualna()));
        }
        return YSymulacji;
    }

    public double obliczKrok(double du) {
        obliczU(du);
        double Yakt = obliczWyjscie();

        dodajY(Yakt);
        return Yakt;
    }
    public double obliczKrok(double du, double[] duZ) {
        obliczUz(duZ);
        return obliczKrok(du);
    }

    public double obliczKrokZaklocenia(double du, int zaklocenie) {
        obliczUz(du, zaklocenie);
        double Yakt = obliczWyjscieZaklocenia(zaklocenie);
        dodajY(Yakt);
        return Yakt;
    }
    public double obliczWyjscieZaklocenia(int zaklocenie) {
        double Yakt = 0.0;
        for(int j = 0; j < Bz.size(); j++)
            Yakt +=  Bz.get(j)[zaklocenie] *  Uz.get(zaklocenie).get(j);
        for(int i = 0; i < A.size(); i++)
            Yakt -= A.get(i) * Y.get(i);
        return Yakt;
    }
    public void obliczUz(double[] du) {
        for (int j = 0; j < liczbaZaklocen; j++) {
            double Uakt = Uz.get(j).get(0) + du[j];
            for (int i = Uz.get(j).size() - 1; i > 0; i--)
                Uz.get(j).set(i, Uz.get(j).get(i - 1));
            Uz.get(j).set(0, Uakt);
        }
    }
    public void obliczUz(double du, int zaklocenie) {
        double Uakt = Uz.get(zaklocenie).get(0) + du;
        if (Uakt > uMax)
            Uakt = uMax;
        else if (Uakt < uMin)
            Uakt = uMin;

        for (int i = Uz.get(zaklocenie).size() - 1; i > 0; i--)
            Uz.get(zaklocenie).set(i, Uz.get(zaklocenie).get(i - 1));
        Uz.get(zaklocenie).set(0, Uakt);
    }
    private double obliczWyjscie() {
        double Yakt = 0.0;
        for(int i = 0; i < B.size(); i++)
            Yakt += B.get(i) * U.get(i);
        for(int i = 0; i < liczbaZaklocen; i++)
            for(int j = 0; j < Bz.size(); j++)
                Yakt +=  Bz.get(j)[i] *  Uz.get(i).get(j);
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

    public double obliczBlad(double[] YSymulacji, double yZad) {
        double bladTemp = 0.0;
        if (this.blad.equals("srednio"))
            for (int i = 0; i < this.dlugosc; i++)
                bladTemp += Math.pow(YSymulacji[i] - yZad, 2);
        else if (this.blad.equals("absolutny"))
            for (int i = 0; i < this.dlugosc; i++)
                bladTemp += Math.abs(YSymulacji[i] - yZad);
        return bladTemp / this.dlugosc;
    }
    private void obliczYMax() {
        double yMax = 0;
        double yTemp = 0;
        for(int i = 0; i < B.size(); i++)
            yTemp += B.get(i) * this.uMax;
        yMax += yTemp;
        for(int i = 0; i < A.size(); i++)
            yMax -= A.get(i) * yTemp;
        this.YMax = yMax;
    }
}
