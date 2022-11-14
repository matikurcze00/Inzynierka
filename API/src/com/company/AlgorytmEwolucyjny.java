package com.company;

import com.company.regulatory.Regulator;
import lombok.Data;

@Data
public class AlgorytmEwolucyjny {
    private int rozmiarPopulacji;
    private int liczbaIteracji;
    private int rozmiarElity;
    private int iloscMutacjaKrzyzowej;
    private int iloscMutacjiBinarnej;
    private double prawdopodobienstwoMutacji;
    private double[][] populacja;

    public AlgorytmEwolucyjny(int rozmiarPopulacji,  int liczbaIteracji, int rozmiarElity, double prawdopodobienstwoMutacji, double stosunekMutacjiBK)
    {
        this.rozmiarPopulacji = rozmiarPopulacji;
        this.liczbaIteracji = liczbaIteracji;
        this.rozmiarElity = rozmiarElity;
        this.prawdopodobienstwoMutacji = prawdopodobienstwoMutacji;
        this.iloscMutacjiBinarnej = (int) Math.floor(((rozmiarPopulacji-rozmiarElity))*stosunekMutacjiBK);
        this.iloscMutacjaKrzyzowej = rozmiarPopulacji-rozmiarPopulacji-iloscMutacjiBinarnej;
    }

    public AlgorytmEwolucyjny() {
    }

    public double[] dobierzWartosci(int liczbaArgumentow, Regulator regulator)
    {
        double[] temp = {0.0};
        return temp;
    }

}
