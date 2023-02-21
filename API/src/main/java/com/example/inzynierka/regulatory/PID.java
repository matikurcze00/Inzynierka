package com.example.inzynierka.regulatory;


import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
public class PID extends Regulator{
    private double K;
    private double Ti;
    private double Td;
    private double Ts;
    private List<Double> E = Arrays.asList(0.0,0.0,0.0);
    private double r0;
    private double r1;
    private double r2;
    private double duMax;
    private double uMax;
    private Double[] strojenieZadane;
    private int liczbaStrojeniaZadanego;
    @Override
    public double policzOutput(double aktualna)
    {
        //Wyliczanie błędu
        E.set(2, E.get(1));
        E.set(1,E.get(0));
        E.set(0,cel[0]-aktualna);

        //
        double du = r0 * E.get(0) + r1 * E.get(1) + r2 * E.get(2);
        if(du>duMax)
            du=duMax;
        else if (du<-duMax)
            du=-duMax;
        return du;
    }

    @Override
    public double[] policzOutput(double[] aktualna) {
        return new double[0];
    }

    public PID(double P, double I, double D, double Ts, double[] cel, double duMax, double uMax, Double[] strojenieZadane)
    {

        this(P, I, D, Ts, cel, duMax, uMax);
        if(strojenieZadane[0]!=null)
            this.K=strojenieZadane[0];
        if(strojenieZadane[1]!=null)
            this.Ti=strojenieZadane[1];
        if(strojenieZadane[2]!=null)
            this.Td=strojenieZadane[2];
        policzWartosci();
        resetujRegulator();
        this.strojenieZadane=strojenieZadane;
        int liczbaTemp = 0;
        for(Double wartosc : strojenieZadane)
            if(wartosc!=null)
                liczbaTemp+=1;
        this.liczbaStrojeniaZadanego = liczbaTemp;

    }


    public PID(double P, double I, double D, double Ts, double[] cel, double duMax, double uMax)
    {
        this.K = P;
        this.Ti = I;
        this.Td = D;
        this.Ts=Ts;
        this.cel = cel;
        this.duMax = duMax;
        this.uMax = uMax;
        policzWartosci();
        resetujRegulator();
    }
    @Override
    public void zmienWartosci(double[] wartosci)
    {
        if(this.liczbaStrojeniaZadanego==0)
        {
            this.K = wartosci[0];
            this.Ti = wartosci[1];
            this.Td = wartosci[2];
        }
        else
        {
            int iTemp = 0;
            if(strojenieZadane[0]==null)
            {
                this.K = wartosci[iTemp];
                iTemp+=1;
            }
            if(strojenieZadane[1]==null)
            {
                this.Ti = wartosci[iTemp];
                iTemp+=1;
            }
            if(strojenieZadane[2]==null)
            {
                this.Td = wartosci[iTemp];
                iTemp+=1;
            }
        }
        policzWartosci();
        resetujRegulator();
    }
    @Override
    public void resetujRegulator()
    {
        E = Arrays.asList(0.0,0.0,0.0);

    }
    private void policzWartosci()
    {
        //ISA
        r2 = K * Td / Ts;
        r1 = -K * (1 + 2 * Td/ Ts - Ts /(2 * Ti));
        r0 = K * (1 + Ts / (2*Ti) + Td/ Ts);
    }
    public int liczbaZmiennych()
    {
        return 3 - this.liczbaStrojeniaZadanego;
    }
}
