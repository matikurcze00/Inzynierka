package com.example.inzynierka.models;

import lombok.Data;

@Data
public class WizualizacjaZaklocen {
    private Double[] uSkok = null;
    private Integer[] skokZaklocenia = null;
    private Integer[] skokPowrotnyZaklocenia = null;
    private Double[] deltaU = null;
    public WizualizacjaZaklocen(Double[] uSkok, Integer[] skokZaklocenia, Integer[] skokPowrotnyZaklocenia, Double[] deltaU) {
        this.uSkok = uSkok;
        this.skokZaklocenia = skokZaklocenia;
        this.skokPowrotnyZaklocenia = skokPowrotnyZaklocenia;
        this.deltaU = deltaU;
    }
}
