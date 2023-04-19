package com.example.inzynierka.obiekty;

import com.example.inzynierka.modele.ParObiekt;
import com.example.inzynierka.modele.Zaklocenia;
import com.example.inzynierka.regulatory.Regulator;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class SISOTransmitancjaCiagle {

    private TransmitancjaCiagla transmitancja;
    private List<Double> U;
    private List<List<Double>> Uz;
    private List<Double> Y;
    private double uMin;
    private double uMax;
    private double YMax;
    private int dlugosc;
    private String blad;

    private List<TransmitancjaCiagla> zakloceniaMierzalne;

    public SISOTransmitancjaCiagle() {
    }

    public SISOTransmitancjaCiagle(ParObiekt parObiekt, double uMax, double uMin, String blad, Zaklocenia zakloceniaMierzalne) {
        this(parObiekt.getGain(), parObiekt.getR1(), parObiekt.getQ1(), parObiekt.getR2(),
            parObiekt.getQ2(), parObiekt.getT1(), parObiekt.getT2(), parObiekt.getT3()
            , parObiekt.getDelay(), parObiekt.getTp(), uMax, uMin, blad);
        this.zakloceniaMierzalne = new ArrayList<>();
        if(zakloceniaMierzalne.getGain()!=null){
            for(int i = 0; i < zakloceniaMierzalne.getGain().length; i++) {
                this.zakloceniaMierzalne.add(new TransmitancjaCiagla(zakloceniaMierzalne.getGain()[i], zakloceniaMierzalne.getR1()[i], zakloceniaMierzalne.getQ1()[i],
                    zakloceniaMierzalne.getR2()[i], zakloceniaMierzalne.getQ2()[i], zakloceniaMierzalne.getT1()[i], zakloceniaMierzalne.getT2()[i],
                    zakloceniaMierzalne.getT3()[i], zakloceniaMierzalne.getDelay()[i], zakloceniaMierzalne.getTp()[i]));
        }}
        resetObiektu();
    }
    public SISOTransmitancjaCiagle(ParObiekt parObiekt, double uMax, double uMin, String blad) {
        this(parObiekt.getGain(), parObiekt.getR1(), parObiekt.getQ1(), parObiekt.getR2(),
                parObiekt.getQ2(), parObiekt.getT1(), parObiekt.getT2(), parObiekt.getT3()
                , parObiekt.getDelay(), parObiekt.getTp(), uMax, uMin, blad);
    }

    public SISOTransmitancjaCiagle(double gain, double R1, int Q1, double R2, int Q2, double T1,
                                   double T2, double T3, int delay, double Tp, double uMax, double uMin, String blad) {
        this.transmitancja = new TransmitancjaCiagla(gain, R1, Q1, R2, Q2, T1, T2, T3, delay, Tp);
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
        setU(new ArrayList<Double>(Collections.nCopies(U.size(), transmitancja.getUpp())));
        setY(new ArrayList<Double>(Collections.nCopies(Y.size(), transmitancja.getYpp())));
        transmitancja.reset();
        if(zakloceniaMierzalne !=null && !zakloceniaMierzalne.isEmpty()) {
            this.Uz = new ArrayList();
            for(TransmitancjaCiagla zaklocenie: zakloceniaMierzalne) {
                Uz.add(new ArrayList<>(Collections.nCopies(3 + zaklocenie.getDelay(), 0.0)));
                zaklocenie.reset();
            }
        }
    }

    public double obliczPraceObiektu(Regulator regulator, double[] cel) {
        resetObiektu();
        regulator.setCel(cel);
        double[] Y ;
        if(zakloceniaMierzalne !=null && !zakloceniaMierzalne.isEmpty()) {
            Y = obliczPraceZZakloceniem(regulator);
        } else {
            Y = obliczPraceBezZaklocen(regulator);
        }
        resetObiektu();
        return obliczBlad(Y, cel[0]);
    }

    private double[] obliczPraceBezZaklocen(Regulator regulator) {
        double[] Y = new double[dlugosc];
        for (int i = 0; i < this.dlugosc; i++) {
            Y[i] = obliczKrok(regulator.policzOutput(getAktualna()));
        }
        return Y;
    }

    public double[] obliczPraceZZakloceniem(Regulator regulator) {
        double[] Y = new double[dlugosc];
        for (int i = 0; i < Math.floorDiv(this.dlugosc,2); i++)
            Y[i] = obliczKrok(regulator.policzOutput(getAktualna()));
        double[] zakloceniaU = new double[zakloceniaMierzalne.size()];
        for(int i = 0; i< zakloceniaU.length; i++)
            zakloceniaU[i] = 3 * transmitancja.getGain() / zakloceniaMierzalne.get(i).getGain();
        for (int i = Math.floorDiv(this.dlugosc,2); i < Math.floorDiv(this.dlugosc*3,4); i++)
            Y[i] = obliczKrok(regulator.policzOutput(getAktualna(), zakloceniaU), zakloceniaU);
        for(int i = 0; i< zakloceniaU.length; i++)
            zakloceniaU[i] = 0.0;
        for (int i = Math.floorDiv(this.dlugosc*3,4); i < dlugosc; i++)
            Y[i] = obliczKrok(regulator.policzOutput(getAktualna(),zakloceniaU),zakloceniaU);

        return Y;
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
    public void obliczUZ(double du, int zaklocenie) {
        double Uakt = Uz.get(zaklocenie).get(0) + du;
        if (Uakt > uMax)
            Uakt = uMax;
        else if (Uakt < uMin)
            Uakt = uMin;
        for (int i = Uz.get(zaklocenie).size() - 1; i > 0; i--)
            Uz.get(zaklocenie).set(i, Uz.get(zaklocenie).get(i - 1));
        Uz.get(zaklocenie).set(0, Uakt);
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

    public void obliczDlugosc() {
        resetObiektu();
        double U = getUMax() / 2;
        int i = 2;
        List<Double> Stemp = new ArrayList<Double>();
        double Utemp = 0;
        Stemp.add((obliczKrok(U) - getYpp()) / U);
        Stemp.add((obliczKrok(Utemp) - getYpp()) / U);
        while (!(Math.abs(Stemp.get(i - 1) - Stemp.get(i - 2)) < 0.001) || Stemp.get(i - 2) == 0.0) {
            Stemp.add((obliczKrok(Utemp) - getYpp()) / U);
            i++;
        }
        this.dlugosc = Stemp.size();
        if (this.dlugosc < 40)
            this.dlugosc = 40;
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
