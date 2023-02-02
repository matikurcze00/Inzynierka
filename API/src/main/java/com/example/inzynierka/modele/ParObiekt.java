package com.example.inzynierka.modele;

import lombok.Data;

@Data
public class ParObiekt {
    private double gain;
    private double R1;
    private double R2;
    private int Q1;
    private int Q2;
    private double T1;
    private double T2;
    private double T3;
    private int delay;
    private double Tp;
    private double uMax;
}
