package com.company.regulatory;

import lombok.Data;

@Data
abstract public class Regulator {
    double cel ;
    double duMax;
    double uMax;
    double uMin;
    double Error;
    abstract public double policzOutput(double aktualna);
    abstract public void zmienWartosci(double[] wartosci);
}
