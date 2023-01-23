package com.example.inzynierka.modele;

import lombok.Data;

@Data
public class ParRegulator {
    public ParRegulator(String typ, double duMax, double uMax, String blad) {
        this.typ = typ;
        this.duMax = duMax;
        this.uMax = uMax;
        this.blad = blad;
    }

    private String typ;
    private double duMax;
    private double uMax;
    private String blad;
}
