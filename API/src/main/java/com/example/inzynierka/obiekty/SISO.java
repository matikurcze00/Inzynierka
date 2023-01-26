package com.example.inzynierka.obiekty;

import com.example.inzynierka.modele.ParObiekt;
import com.example.inzynierka.regulatory.Regulator;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class SISO {

    private TransmitancjaCiagla transmitancja;
    private List<Double> U ;
    private List<Double> Y ;
    private double uMin;
    private double uMax;
    private double YMax;
    private int dlugosc;
    public SISO() {}

    public SISO(ParObiekt parObiekt, double uMax, double uMin)
    {
        this(parObiekt.getGain(), parObiekt.getR1(), parObiekt.getQ1(), parObiekt.getR2(),
                parObiekt.getQ2(), parObiekt.getT1(), parObiekt.getT2(), parObiekt.getT3()
        ,parObiekt.getDelay(), parObiekt.getTp(), uMax, uMin);
    }
    public SISO(double gain, double R1, int Q1, double R2, int Q2, double T1,
                double T2, double T3, int delay, double Tp, double uMax, double uMin)
    {
        this.transmitancja = new TransmitancjaCiagla(gain, R1, Q1, R2, Q2, T1, T2, T3, delay, Tp);
        U = new ArrayList(Collections.nCopies(3+delay, transmitancja.getUpp()));
        Y = new ArrayList(Collections.nCopies(3, transmitancja.getYpp()));
        this.uMax = uMax;
        this.uMin = uMin;
        obliczDlugosc();
        obliczYMax();
    }

    public Double getAktualna(){
        try{

            return Y.get(0);
        } catch (Exception ex)
        {
            return 0.0;
        }
    }

    public void resetObiektu()
    {
        setU(new ArrayList<Double>(Collections.nCopies(U.size(), transmitancja.getUpp())));
        setY(new ArrayList<Double>(Collections.nCopies(Y.size(), transmitancja.getYpp())));
        transmitancja.reset();
    }
    public double obliczPraceObiektu(Regulator regulator, double[] cel)
    {
        resetObiektu();
        double blad = 0.0;
        regulator.setCel(cel);
        for (int i = 0; i<this.dlugosc; i++)
        {
//            if(i==25)
//                blad = Math.abs(obliczKrok(regulator.policzOutput(getAktualna()))-cel[0]);
            blad+=Math.pow(obliczKrok(regulator.policzOutput(getAktualna()))-cel[0],2);
        }
        blad=blad/this.dlugosc;
        resetObiektu();
        return blad;
    }
    public double obliczKrok(double du)
    {
        obliczU(du);
        double Yakt ;
        Yakt = transmitancja.obliczKrok(U);

        for(int i = Y.size()-1; i>0 ;i--)
            Y.set(i,Y.get(i-1));
        Y.set(0,Yakt);
        return Yakt;
    }
    public void obliczU(double du)
    {
        double Uakt = U.get(0) + du;
        if(Uakt>uMax)
            Uakt=uMax;
        else if (Uakt<uMin)
            Uakt=uMin;
        for(int i = U.size()-1; i>0 ;i--)
            U.set(i,U.get(i-1));
        U.set(0,Uakt);
    }

    public double getYpp()
    {
        return transmitancja.getYpp();
    }


    private void obliczYMax()
    {
        double Ytemp;
        for (int i = 0; i<this.dlugosc*2; i++)
        {
            obliczKrok(getUMax());
        }
        Ytemp = getAktualna();
        resetObiektu();
        this.YMax = Ytemp;
    }

    public void obliczDlugosc()
    {
        resetObiektu();
        double U = getUMax()/2;
        int i = 2;
        List<Double> Stemp = new ArrayList<Double>();
        double Utemp = 0;
        Stemp.add((obliczKrok(U)- getYpp())/U);
        Stemp.add((obliczKrok(Utemp)- getYpp())/U);
        while((!((Stemp.get(i-1)-Stemp.get(i-2))<=0.005) || Stemp.get(i-2)==0.0))
        {
            Stemp.add((obliczKrok(Utemp)- getYpp())/U);
            i++;
        }
        this.dlugosc = Stemp.size();
    }
}
