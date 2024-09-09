package com.example.inzynierka.models;

import lombok.Data;

@Data
public class DisturbanceDPA {
    private Double[] gain;
    private Double[] R1;
    private Double[] R2;
    private Integer[] Q1;
    private Integer[] Q2;
    private Double[] T1;
    private Double[] T2;
    private Double[] T3;
    private Double[] Tp;
    private Integer[] delay;
}
