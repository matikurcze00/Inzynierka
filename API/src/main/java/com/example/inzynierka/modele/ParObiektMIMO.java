package com.example.inzynierka.modele;

import lombok.Data;

@Data
public class ParObiektMIMO {

    private Double[] z1;
    private Double[] z2;
    private Double[] b1;
    private Double[] b2;
    private Double[] b3;
    private double[] K;
    private double Ts;
    private int opoznienie;
    private double szum;
    private double UMax;
}
