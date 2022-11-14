package com.company.regulatory;

abstract public class Regulator {
    double cel ;
    double duMax;
    double uMax;
    double uMin;
    double Error;
    abstract double policzOutput(double aktualna);
}
