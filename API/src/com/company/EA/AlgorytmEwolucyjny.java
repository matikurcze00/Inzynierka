package com.company;

import com.company.regulatory.Regulator;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Data
public class AlgorytmEwolucyjny {
    private int rozmiarPopulacji;
    private int liczbaIteracji;
    private int rozmiarElity;
    private int iloscKrzyzowania;
    private int iloscMutacji;
    private double prawdopodobienstwoMutacji;
    private List<List<Double>> populacja;

    public AlgorytmEwolucyjny(int rozmiarPopulacji,  int liczbaIteracji, int rozmiarElity, double prawdopodobienstwoMutacji, double stosunekMutacjiBK)
    {
        this.rozmiarPopulacji = rozmiarPopulacji;
        this.liczbaIteracji = liczbaIteracji;
        this.rozmiarElity = rozmiarElity;
        this.prawdopodobienstwoMutacji = prawdopodobienstwoMutacji;
        this.iloscMutacji = (int) Math.floor(((rozmiarPopulacji-rozmiarElity))*stosunekMutacjiBK);
        this.iloscKrzyzowania = rozmiarPopulacji-rozmiarPopulacji- iloscMutacji;
    }

    public AlgorytmEwolucyjny() {
    }

    public double[] dobierzWartosci(int liczbaArgumentow, Regulator regulator, Obiekt obiekt)
    {
        populacja= new ArrayList<ArrayList<Double>(liczbaArgumentow+1)>(rozmiarPopulacji);

        for (int i = 0; i<rozmiarPopulacji; i++)
        {
            Random r = new Random();
            for(int j = 0; j<liczbaArgumentow; j++)
            {
                populacja.set(j,)= r.nextDouble(50.0);
            }
            regulator.zmienWartosci(populacja[i]);
            populacja[i][liczbaArgumentow+1] = obiekt.obliczPraceObiektu(regulator);
        }
        populacja.sort()
        for(int k = 0; k<liczbaIteracji; k++)
        {

        }
        double[] wynik = {0.0};
        return wynik;
    }
    public static class Comparators {
        @Override
        public int compare(double[] o1, double o2) {
            return Integer.compare(o1.size(), o2.size());
        }
    }
}
