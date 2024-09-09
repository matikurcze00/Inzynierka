package com.example.inzynierka.models;

import lombok.Data;

@Data
public class ParWizualizacja {
    private double[] yZad;
    private double[] yPP;
    private double[] uPP;
    private int[] skok;
    private int dlugosc;
    private Double[] strojenie;
    private String err;
    public ParWizualizacja(double[] yZad, double[] yPP, double[] uPP, int[] skok, int dlugosc,
                           Double[] strojenie, String err) {
        this.yZad = yZad;
        this.yPP = yPP;
        this.uPP = uPP;
        this.skok = skok;
        this.dlugosc = dlugosc;
        this.strojenie = strojenie;
        this.err = err;
    }
}
