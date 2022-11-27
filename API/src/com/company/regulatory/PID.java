package com.company.regulatory;


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
    private double cel;
    private double r0;
    private double r1;
    private double r2;
    private double duMax;
    private double uMax;
    private double uMin;
    private double Error = 0;

    @Override
    public double policzOutput(double aktualna)
    {
        //Wyliczanie błędu
        E.set(2, E.get(1));
        E.set(1,E.get(0));
        E.set(0,cel-aktualna);
        Error += cel-aktualna;

        //
        double du = r0 * E.get(0) + r1 * E.get(1) + r2 * E.get(2);
        if(du>duMax)
            du=duMax;
        else if (du<-duMax)
            du=-duMax;
        return du;
    }

    public PID(double P, double I, double D, double Ts, double cel, double duMax, double uMax, double uMin)
    {
        this.K = P;
        this.Ti = I;
        this.Td = D;
        this.Ts=Ts;
        this.cel = cel;
        this.duMax = duMax;
        this.uMax = uMax;
        this.uMin = uMin;
        policzWartosci();
        resetujRegulator();
    }
    @Override
    public void zmienWartosci(double[] wartosci)
    {
        this.K = wartosci[0];
        this.Ti = wartosci[1];
        this.Td = wartosci[2];
        policzWartosci();
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
}
