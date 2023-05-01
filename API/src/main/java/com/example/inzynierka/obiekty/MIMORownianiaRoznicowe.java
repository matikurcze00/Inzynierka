package com.example.inzynierka.obiekty;

import com.example.inzynierka.modele.ParObiektRownaniaMIMO;
import com.example.inzynierka.modele.ZakloceniaRownania;
import com.example.inzynierka.regulatory.Regulator;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class MIMORownianiaRoznicowe extends MIMO {
    List<List<Double[]>> A;
    List<List<Double[]>> B;
    List<List<Double>> Y;
    List<List<Double>> U;
    List<List<Double[]>> Bz;
    List<List<Double>> Uz;

    private double[] Ypp;
    private double[] Upp;
    private double[] uMin;
    private double[] uMax;
    private double[] YMax;
    private String blad;
    private int liczbaOUT;
    private int liczbaIN;
    private int liczbaZaklocen;
    private int[] delay;
    private int delayMax = 0;
    private int dlugosc;

    public MIMORownianiaRoznicowe(ParObiektRownaniaMIMO[] parObiektRownaniaMIMOS, String blad, ZakloceniaRownania[] zakloceniaRownania) {
        this(parObiektRownaniaMIMOS);
        this.blad = blad;
        this.Bz = new ArrayList<>();
        for (ZakloceniaRownania zaklocenie : zakloceniaRownania) {
            List<Double[]> BzTemp = new ArrayList<>();
            BzTemp.add(zaklocenie.getB1());
            BzTemp.add(zaklocenie.getB2());
            BzTemp.add(zaklocenie.getB3());
            BzTemp.add(zaklocenie.getB4());
            BzTemp.add(zaklocenie.getB5());
            Bz.add(BzTemp);
        }
        this.liczbaZaklocen = Bz.get(0).get(0).length;
        this.Uz = new ArrayList<>();
        for (int i = 0; i < liczbaZaklocen; i++) {
            Uz.add(new ArrayList(Collections.nCopies(5, 0.0)));
        }
    }

    public MIMORownianiaRoznicowe(ParObiektRownaniaMIMO[] parObiektRownaniaMIMOS, String blad) {
        this(parObiektRownaniaMIMOS);
        this.blad = blad;
    }

    public MIMORownianiaRoznicowe(ParObiektRownaniaMIMO[] parObiektRownaniaMIMOS) {
        this.A = new ArrayList<>();
        this.B = new ArrayList<>();
        this.U = new ArrayList<>();
        this.Y = new ArrayList<>();
        this.uMin = new double[parObiektRownaniaMIMOS.length];
        this.uMax = new double[parObiektRownaniaMIMOS.length];
        List<Double> uMinTemp = new ArrayList<>();
        List<Double> uMaxTemp = new ArrayList<>();
        this.dlugosc = 50;
        przydzielABMIMO(parObiektRownaniaMIMOS, uMinTemp, uMaxTemp);
        ograniczeniaMIMO(uMinTemp, uMaxTemp);
        liczbaOUT = parObiektRownaniaMIMOS.length;
        liczbaIN = parObiektRownaniaMIMOS[0].getB1().length;
        inicjalizacjaUYMIMO();
        obliczYMax();
    }

    private void inicjalizacjaUYMIMO() {
        this.Y = new ArrayList();
        for (int i = 0; i < this.liczbaOUT; i++) {
            Y.add(new ArrayList(Collections.nCopies(5, 0.0)));
        }
        this.U = new ArrayList();
        for (int i = 0; i < this.liczbaIN; i++) {
            U.add(new ArrayList(Collections.nCopies(5, 0.0)));
        }
    }

    private void ograniczeniaMIMO(List uMinTemp, List uMaxTemp) {
        for (int i = 0; i < uMinTemp.size(); i++) {
            uMin[i] = (double) uMinTemp.get(i);
            uMax[i] = (double) uMaxTemp.get(i);
        }
    }

    private void przydzielABMIMO(ParObiektRownaniaMIMO[] parObiektRownaniaMIMOS, List uMinTemp, List uMaxTemp) {
        for (ParObiektRownaniaMIMO parObiekt : parObiektRownaniaMIMOS) {
            List Atemp = new ArrayList<>();
            Atemp.add(parObiekt.getA1());
            Atemp.add(parObiekt.getA2());
            Atemp.add(parObiekt.getA3());
            Atemp.add(parObiekt.getA4());
            Atemp.add(parObiekt.getA5());
            this.A.add(Atemp);
            List Btemp = new ArrayList<>();
            Btemp.add(parObiekt.getB1());
            Btemp.add(parObiekt.getB2());
            Btemp.add(parObiekt.getB3());
            Btemp.add(parObiekt.getB4());
            Btemp.add(parObiekt.getB5());
            this.B.add(Btemp);
            uMinTemp.add(parObiekt.getUMin());
            uMaxTemp.add(parObiekt.getUMax());
        }
    }

    public double obliczPraceObiektu(Regulator regulator, double[] cel) {

        resetObiektu();
        double blad = 0.0;

        blad = obliczPraceBezZaklocen(regulator, cel, blad);

        blad = blad / this.dlugosc * liczbaOUT * liczbaOUT;
        resetObiektu();
        return blad;
    }

    private double obliczPraceBezZaklocen(Regulator regulator, double[] cel, double blad) {
        double[] tempCel = new double[liczbaOUT];
        for (int k = 0; k < liczbaOUT; k++) {
            for (int i = 0; i < liczbaOUT; i++) {
                tempCel[i] = 0;
            }
            tempCel[k] = cel[k];
            regulator.setCel(tempCel);
            resetObiektu();
            regulator.resetujRegulator();
            for (int i = 0; i < this.dlugosc; i++) {
                double[] Ytepm = obliczKrok(regulator.policzSterowanie(getAktualne()));
                for (int j = 0; j < Ytepm.length; j++) {
                    if (this.blad.equals("srednio")) {
                        blad += Math.pow(Ytepm[j] - tempCel[j], 2);
                    } else if (this.blad.equals("absolutny")) {
                        blad += Math.abs(Ytepm[j] - tempCel[j]);
                    }
                }
            }
        }
        return blad;
    }

    public double[] obliczKrok(double[] du) {
        obliczU(du);
        double[] Yakt = new double[liczbaOUT];

        for (int i = 0; i < liczbaOUT; i++) {
            Yakt[i] = obliczWyjscie(i);
        }
        dodajY(Yakt);
        return Yakt;
    }

    public double[] obliczKrok(double[] du, double[] duZ) {
        obliczUz(duZ);
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
        double[] YAkt = new double[liczbaOUT];
        for (int i = 0; i < liczbaOUT; i++) {
            YAkt[i] = Y.get(i).get(0);
        }
        return YAkt;
    }

    private double obliczWyjscie(int out) {
        Double Yakt = 0.0;
        for (int j = 0; j < B.get(out).size(); j++) {
            for (int k = 0; k < liczbaIN; k++) {
                Yakt += B.get(out).get(j)[k] * U.get(k).get(j);
            }
        }

        for (int i = 0; i < liczbaZaklocen; i++) {
            for (int j = 0; j < Bz.size(); j++) {
                Yakt += Bz.get(out).get(j)[i] * Uz.get(i).get(j);
            }
        }

        for (int j = 0; j < A.get(out).size(); j++) {
            for (int k = 0; k < liczbaIN; k++) {
                Yakt -= A.get(out).get(j)[k] * Y.get(out).get(j);
            }
        }
        return Yakt;
    }

    public void resetObiektu() {
        for (int i = 0; i < this.liczbaOUT; i++) {
            Y.set(i, new ArrayList(Collections.nCopies(5, 0.0)));
        }
        for (int i = 0; i < this.liczbaIN; i++) {
            U.set(i, new ArrayList(Collections.nCopies(5, 0.0)));
        }
        for (int i = 0; i < this.liczbaZaklocen; i++) {
            Uz.set(i, new ArrayList(Collections.nCopies(5, 0.0)));
        }
    }

    public void obliczU(double[] du) {
        for (int j = 0; j < liczbaIN; j++) {
            double Uakt = U.get(j).get(0) + du[j];
            if (Uakt > uMax[j]) {
                Uakt = uMax[j];
            } else if (Uakt < uMin[j]) {
                Uakt = uMin[j];
            }

            for (int i = U.get(j).size() - 1; i > 0; i--) {
                U.get(j).set(i, U.get(j).get(i - 1));
            }
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
        for (int i = U.get(IN).size() - 1; i > 0; i--) {
            U.get(IN).set(i, U.get(IN).get(i - 1));
        }
        U.get(IN).set(0, Uakt);
    }

    public void obliczUz(double[] du) {
        for (int j = 0; j < liczbaZaklocen; j++) {
            double Uakt = Uz.get(j).get(0) + du[j];
            for (int i = Uz.get(j).size() - 1; i > 0; i--) {
                Uz.get(j).set(i, Uz.get(j).get(i - 1));
            }
            Uz.get(j).set(0, Uakt);
        }
    }

    private void dodajY(double[] Yakt) {
        for (int i = 0; i < liczbaOUT; i++) {
            List<Double> Ytemp = Y.get(i);
            for (int j = Y.get(i).size() - 1; j > 0; j--) {
                Ytemp.set(j, Ytemp.get(j - 1));
            }
            Ytemp.set(0, Yakt[i]);
            Y.set(i, Ytemp);
        }
    }

    private void dodajY(int OUT, double YaktIN) {
        List<Double> Ytemp = Y.get(OUT);
        for (int j = Y.get(OUT).size() - 1; j > 0; j--) {
            Ytemp.set(j, Ytemp.get(j - 1));
        }
        Ytemp.set(0, YaktIN);
        Y.set(OUT, Ytemp);
    }

    private void obliczYMax() {
        double[] Ytemp;

        this.YMax = new double[liczbaOUT];
        for (int i = 0; i < liczbaOUT; i++) {
            this.YMax[i] = 0.0;
        }
        double[] uMax = new double[liczbaIN];
        System.arraycopy(this.uMax, 0, uMax, 0, liczbaIN);
        for (int i = 0; i < dlugosc * 2; i++) {
            obliczKrok(uMax);
        }
        Ytemp = getAktualne();
        resetObiektu();
        for (int i = 0; i < this.YMax.length; i++) {
            if (this.YMax[i] < Ytemp[i]) {
                this.YMax[i] = Ytemp[i];
            }
        }
    }
}
