package com.example.inzynierka.regulatory;

import lombok.Data;

@Data
abstract public class Regulator {
    double[] cel;

    abstract public double policzSterowanie(double aktualna);

    abstract public double policzSterowanie(double aktualna, double[] sterowanieZaklocenia);

    abstract public double[] policzSterowanie(double[] aktualna);

    abstract public double[] policzSterowanie(double[] aktualna, double[] sterowanieZaklocenia);

    abstract public void zmienNastawy(double[] wartosci);

    abstract public void resetujRegulator();

    abstract public int liczbaZmiennych();
}
