package com.example.inzynierka.obiekty;

import com.example.inzynierka.modele.ParObiektDPA;
import com.example.inzynierka.modele.ZakloceniaDPA;
import com.example.inzynierka.regulatory.Regulator;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class SISODPA extends SISO{

    private DPA transmitancja;
    private List<Double> U;
    private List<List<Double>> Uz;
    private List<Double> Y;
    private double uMin;
    private double uMax;
    private double YMax;
    private int dlugosc;
    private String blad;

    private List<DPA> zakloceniaMierzalne;

    public SISODPA() {
    }

    public SISODPA(ParObiektDPA parObiektDPA, double uMax, double uMin, String blad, ZakloceniaDPA zakloceniaDPAMierzalne) {
        this(parObiektDPA.getGain(), parObiektDPA.getR1(), parObiektDPA.getQ1(), parObiektDPA.getR2(),
            parObiektDPA.getQ2(), parObiektDPA.getT1(), parObiektDPA.getT2(), parObiektDPA.getT3()
            , parObiektDPA.getDelay(), parObiektDPA.getTp(), uMax, uMin, blad);
        this.zakloceniaMierzalne = new ArrayList<>();
        if(zakloceniaDPAMierzalne.getGain()!=null){
            for(int i = 0; i < zakloceniaDPAMierzalne.getGain().length; i++) {
                this.zakloceniaMierzalne.add(new DPA(zakloceniaDPAMierzalne.getGain()[i], zakloceniaDPAMierzalne.getR1()[i], zakloceniaDPAMierzalne.getQ1()[i],
                    zakloceniaDPAMierzalne.getR2()[i], zakloceniaDPAMierzalne.getQ2()[i], zakloceniaDPAMierzalne.getT1()[i], zakloceniaDPAMierzalne.getT2()[i],
                    zakloceniaDPAMierzalne.getT3()[i], zakloceniaDPAMierzalne.getDelay()[i], zakloceniaDPAMierzalne.getTp()[i]));
        }}
        resetObiektu();
    }
    public SISODPA(ParObiektDPA parObiektDPA, double uMax, double uMin, String blad) {
        this(parObiektDPA.getGain(), parObiektDPA.getR1(), parObiektDPA.getQ1(), parObiektDPA.getR2(),
                parObiektDPA.getQ2(), parObiektDPA.getT1(), parObiektDPA.getT2(), parObiektDPA.getT3()
                , parObiektDPA.getDelay(), parObiektDPA.getTp(), uMax, uMin, blad);
    }

    public SISODPA(double gain, double R1, int Q1, double R2, int Q2, double T1,
                   double T2, double T3, int delay, double Tp, double uMax, double uMin, String blad) {
        this.transmitancja = new DPA(gain, R1, Q1, R2, Q2, T1, T2, T3, delay, Tp);
        U = new ArrayList(Collections.nCopies(3 + delay, transmitancja.getUpp()));
        Y = new ArrayList(Collections.nCopies(3, transmitancja.getYpp()));
        this.uMax = uMax;
        this.uMin = uMin;
        this.blad = blad;
        obliczDlugosc();
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
        setU(new ArrayList(Collections.nCopies(U.size(), transmitancja.getUpp())));
        setY(new ArrayList(Collections.nCopies(Y.size(), transmitancja.getYpp())));
        transmitancja.reset();
        if(zakloceniaMierzalne !=null && !zakloceniaMierzalne.isEmpty()) {
            this.Uz = new ArrayList<>();
            for(DPA zaklocenie: zakloceniaMierzalne) {
                Uz.add(new ArrayList(Collections.nCopies(3 + zaklocenie.getDelay(), 0.0)));
                zaklocenie.reset();
            }
        }
    }

    public double obliczPraceObiektu(Regulator regulator, double[] cel) {
        resetObiektu();
        regulator.setCel(cel);
        double[] YSymulacji ;
        if(zakloceniaMierzalne !=null && !zakloceniaMierzalne.isEmpty()) {
            YSymulacji = obliczPraceZZakloceniem(regulator);
        } else {
            YSymulacji = obliczPraceBezZaklocen(regulator);
        }
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

    public double[] obliczPraceZZakloceniem(Regulator regulator) {
        double[] YSymulacji = new double[dlugosc];
        for (int i = 0; i < Math.floorDiv(this.dlugosc,2); i++)
            YSymulacji[i] = obliczKrok(regulator.policzSterowanie(getAktualna()));
        double[] zakloceniaU = new double[zakloceniaMierzalne.size()];
        for(int i = 0; i< zakloceniaU.length; i++)
            zakloceniaU[i] = 3 * transmitancja.getGain() / zakloceniaMierzalne.get(i).getGain();
        for (int i = Math.floorDiv(this.dlugosc,2); i < Math.floorDiv(this.dlugosc*3,4); i++)
            YSymulacji[i] = obliczKrok(regulator.policzSterowanie(getAktualna(), zakloceniaU), zakloceniaU);
        for(int i = 0; i< zakloceniaU.length; i++)
            zakloceniaU[i] = 0.0;
        for (int i = Math.floorDiv(this.dlugosc*3,4); i < dlugosc; i++)
            YSymulacji[i] = obliczKrok(regulator.policzSterowanie(getAktualna(),zakloceniaU),zakloceniaU);

        return YSymulacji;
    }

    public double obliczKrok(double du) {
        obliczU(du);
        double Yakt;
        Yakt = transmitancja.obliczKrok(U);

        dodajY(Yakt);
        return Yakt;
    }

    private void dodajY(double Yakt) {
        for (int i = Y.size() - 1; i > 0; i--)
            Y.set(i, Y.get(i - 1));
        Y.set(0, Yakt);
    }

    public double obliczKrok(double du, double[] duZ) {
        obliczU(du);
        double Yakt;
        Yakt = transmitancja.obliczKrok(U);
        for(int i = 0; i < duZ.length; i++) {
            Yakt+=obliczKrokZaklocenia(duZ[i], i);
        }
        dodajY(Yakt);
        return Yakt;
    }
    public double obliczKrokZaklocenia(double du, int zaklocenie) {
        double Yakt;
        obliczUZ(du,zaklocenie);
        Yakt = this.zakloceniaMierzalne.get(zaklocenie).obliczKrok(Uz.get(zaklocenie));

        dodajY(Yakt);
        return Yakt;
    }
    private void obliczUZ(double du, int zaklocenie) {
        double Uakt = Uz.get(zaklocenie).get(0) + du;
        if (Uakt > uMax)
            Uakt = uMax;
        else if (Uakt < uMin)
            Uakt = uMin;
        for (int i = Uz.get(zaklocenie).size() - 1; i > 0; i--)
            Uz.get(zaklocenie).set(i, Uz.get(zaklocenie).get(i - 1));
        Uz.get(zaklocenie).set(0, Uakt);
    }
    private void obliczU(double du) {
        double Uakt = U.get(0) + du;
        if (Uakt > uMax)
            Uakt = uMax;
        else if (Uakt < uMin)
            Uakt = uMin;
        for (int i = U.size() - 1; i > 0; i--)
            U.set(i, U.get(i - 1));
        U.set(0, Uakt);
    }

    @Override
    public double getYpp() {
        return transmitancja.getYpp();
    }


    private void obliczYMax() {
        double Ytemp;
        for (int i = 0; i < this.dlugosc * 2; i++) {
            obliczKrok(getUMax());
        }
        Ytemp = getAktualna();
        resetObiektu();
        this.YMax = Ytemp;
    }

    private void obliczDlugosc() {
        resetObiektu();
        double USkok = getUMax() / 2;
        int i = 2;
        List<Double> Stemp = new ArrayList<Double>();
        double Utemp = 0;
        Stemp.add((obliczKrok(USkok) - getYpp()) / USkok);
        Stemp.add((obliczKrok(Utemp) - getYpp()) / USkok);
        while (!(Math.abs(Stemp.get(i - 1) - Stemp.get(i - 2)) < 0.001) || Stemp.get(i - 2) == 0.0) {
            Stemp.add((obliczKrok(Utemp) - getYpp()) / USkok);
            i++;
        }
        this.dlugosc = Stemp.size();
        if (this.dlugosc < 40)
            this.dlugosc = 40;
    }

    private double obliczBlad(double[] Y, double yZad) {
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
