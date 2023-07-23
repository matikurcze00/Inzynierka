package com.example.inzynierka.modele;

import lombok.Data;

@Data
public class OdpowiedzStrojenieMIMO {
    private double[] wspolczynniki;
    private double[][] wykres;
    private double[][] cel;
    private double[][] sterowanie;
    private double[][] sterowanieZaklocenia;
}
