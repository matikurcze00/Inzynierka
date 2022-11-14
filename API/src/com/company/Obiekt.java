package com.company;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class Obiekt {

    private double z1;
    private double z2;
    private double b1;
    private double b2;
    private double uMax;
    private double uMin;
    private List<Double> U = Arrays.asList(0.0,0.0);
    private List<Double> Y = Arrays.asList(0.0,0.0);

    public Obiekt() {
    }

    public Obiekt(Double z1, double z2, double b1, double b2)
    {
        Obiekt obiekt = new Obiekt();
        obiekt.setZ1(z1);
        obiekt.setZ2(z2);
        obiekt.setB1(b1);
        obiekt.setB2(b2);
        obiekt.setY(Arrays.asList((obiekt.uMax+obiekt.uMin)/2,(obiekt.uMax+obiekt.uMin)/2));
        obiekt.setU(Arrays.asList(0.0,0.0));
    }

    public double obliczKrok(double Uakt)
    {
        double Yakt ;
        Yakt = Uakt - (z1 + z2) * U.get(0) + z1 * z2 * U.get(1) + (b1 + b2) * Y.get(0) - b1 * b2 * Y.get(1);
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
}
