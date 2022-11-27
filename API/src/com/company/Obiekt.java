package com.company;

import com.company.regulatory.Regulator;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Data
public class Obiekt {

    private double z1;
    private double z2;
    private double b1;
    private double b2;
    private double K;
    private double uMax;
    private double uMin;
    private List<Double> U = Arrays.asList(0.0,0.0);
    private List<Double> Y = Arrays.asList(0.0,0.0);
    private double Ypp;
    private double Upp;
    private double YMax;
    public Obiekt() {
    }

    public Obiekt(double z1, double z2, double b1, double b2, double K, double uMax, double uMin)
    {
        this.setZ1(z1);
        this.setZ2(z2);
        this.setB1(b1);
        this.setB2(b2);
        this.setK(K);
        this.setUMax(uMax);
        this.setUMin(uMin);
        this.Ypp = 0;
        this.Upp = 0;
        this.YMax = obliczYMax();
    }

    public double obliczKrok(double du)
    {
        double Uakt = U.get(0) + du;
        if(Uakt>uMax)
            Uakt=uMax;
        else if (Uakt<uMin)
            Uakt=uMin;

        double Yakt ;
        Yakt = K * Uakt - K * (z1+z2) * U.get(0) + K * z1 * z2 * U.get(1)  + (b1 + b2) * Y.get(0) - b1 * b2 * Y.get(1);
        U.set(1, U.get(0));
        U.set(0,Uakt);
        Y.set(1, Y.get(0));
        Y.set(0,Yakt);
        return Yakt;
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
        setU(Arrays.asList(Upp,Upp));
        setY(Arrays.asList(Ypp,Ypp));
    }
    public double obliczPraceObiektu(Regulator regulator, double cel)
    {
        List<Double> Y = new ArrayList<>();
        int dlugoscBadania = 50;
        resetObiektu();
        double blad = 0.0;
        regulator.setCel(cel);
        for (int i = 0; i<dlugoscBadania; i++)
        {
            Y.add(obliczKrok(regulator.policzOutput(getAktualna())));
        }
        for(int i = 0; i<dlugoscBadania; i++)
        {
            blad+=Math.pow((Y.get(i)-cel),2);
        }
        blad=blad/Y.size();
        Y.clear();

        blad=blad/Y.size();
        resetObiektu();
        return blad;
    }
    private double obliczYMax()
    {
        double Ytemp;
        for (int i = 0; i<100; i++)
        {
            obliczKrok(uMax);
        }
        Ytemp = getAktualna();
        resetObiektu();
        return Ytemp;
    }
}
