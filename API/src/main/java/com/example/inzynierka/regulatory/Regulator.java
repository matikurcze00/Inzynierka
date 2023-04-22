package com.example.inzynierka.regulatory;

import lombok.Data;

@Data
abstract public class Regulator {
    double[] cel;

    abstract public double policzOutput(double aktualna);
    abstract public double policzOutput(double aktualna, double[] sterowanieZaklocenia);

    abstract public double[] policzOutput(double[] aktualna);
    abstract public double[] policzOutput(double[] aktualna, double[] sterowanieZaklocenia);

    abstract public void zmienWartosci(double[] wartosci);

    abstract public void resetujRegulator();

    abstract public int liczbaZmiennych();
}
