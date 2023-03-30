package com.example.inzynierka.modele;

import lombok.Data;

@Data
public class WizualizacjaZaklocen {
    public WizualizacjaZaklocen(double[] uSkok, int[] skokZaklocenia, int[] skokPowrotnyZaklocenia, double[] deltaU) {
        this.uSkok = uSkok;
        this.skokZaklocenia = skokZaklocenia;
        this.skokPowrotnyZaklocenia = skokPowrotnyZaklocenia;
        this.deltaU = deltaU;
    }

    private double[] uSkok;
    private int[] skokZaklocenia;
    private int[] skokPowrotnyZaklocenia;
    private double[] deltaU;
}
