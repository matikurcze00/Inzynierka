package com.example.inzynierka.obiekty;

import com.example.inzynierka.regulatory.Regulator;
import lombok.Data;

import java.util.List;

@Data
abstract public class SISO {
    private List<Double> Y;
    private List<Double> U;
    private double uMin;
    private double uMax;
    private double YMax;
    private int dlugosc;
    private String blad;
    private double Ypp;

    public abstract Double getAktualna();

    public abstract void resetObiektu();

    public abstract double obliczPraceObiektu(Regulator regulator, double[] cel);

    public abstract double obliczKrok(double du);

    public abstract double obliczKrok(double du, double[] duZ);

    public abstract double obliczKrokZaklocenia(double du, int zaklocenie);
}
