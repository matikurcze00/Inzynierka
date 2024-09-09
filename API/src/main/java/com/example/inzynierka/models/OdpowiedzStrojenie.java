package com.example.inzynierka.models;

import lombok.Data;

@Data
public class OdpowiedzStrojenie {
    private double[] wspolczynniki;
    private double[] wykres;
    private double[] goal;
    private double[] sterowanie;
    private double[][] sterowanieZaklocenia;
}