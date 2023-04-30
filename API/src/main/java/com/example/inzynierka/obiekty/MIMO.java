package com.example.inzynierka.obiekty;

import com.example.inzynierka.regulatory.Regulator;
import lombok.Data;

import java.util.List;

@Data
public abstract class MIMO {
    private int liczbaOUT;
    private int liczbaIN;
    private int delayMax;
    private int dlugosc;
    private List<List<Double>> U;
    private List<List<Double>> Y;
    private double[] YMax;
    private double[] uMin;
    private double[] uMax;

    public abstract double[] obliczKrok(double[] du);
    public abstract double[] obliczKrok(double[] du, double[] dUz);
    public abstract double obliczKrok(double du, int IN, int OUT);
    public abstract double obliczKrokZaklocenia(double du, int IN, int OUT);
    public abstract double[] getAktualne();
    public abstract double obliczPraceObiektu(Regulator regulator, double[] cel);
    public abstract void resetObiektu();

}
