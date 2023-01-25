package com.example.inzynierka.modele;

import lombok.Data;

@Data
public class ParWizualizacja {
    public ParWizualizacja(double[] yZad, double[] yPP,double[] uPP, int[] skok, int dlugosc) {
        this.yZad = yZad;
        this.yPP = yPP;
        this.uPP = uPP;
        this.skok = skok;
        this.dlugosc = dlugosc;
    }
    private double[] yZad;
    private double[] yPP;
    private double[] uPP;
    private int[] skok;
    private int dlugosc;

}
