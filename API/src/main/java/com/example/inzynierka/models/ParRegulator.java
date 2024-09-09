package com.example.inzynierka.models;

import lombok.Data;

@Data
public class ParRegulator {
    private String typ;
    private double uMin;
    private double duMax;
    private double uMax;
    private String err;
    public ParRegulator(String typ, double duMax, double uMax, double uMin, String err) {
        this.typ = typ;
        this.duMax = duMax;
        this.uMax = uMax;
        this.uMin = uMin;

        this.err = err;
    }
}
