package com.example.inzynierka.EA;

import com.example.inzynierka.Obiekt;
import com.example.inzynierka.regulatory.Regulator;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
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
    private List<Osobnik> populacja;

    public AlgorytmEwolucyjny(int rozmiarPopulacji,  int liczbaIteracji, int rozmiarElity, double prawdopodobienstwoMutacji, double czestotliwoscMutacji)
    {
        this.rozmiarPopulacji = rozmiarPopulacji;
        this.liczbaIteracji = liczbaIteracji;
        this.rozmiarElity = rozmiarElity;
        this.prawdopodobienstwoMutacji = prawdopodobienstwoMutacji;
        this.iloscMutacji = (int) Math.floor(((rozmiarPopulacji-rozmiarElity))*czestotliwoscMutacji);
        this.iloscKrzyzowania = rozmiarPopulacji-rozmiarElity- iloscMutacji;
    }

    public AlgorytmEwolucyjny() {
    }

    public double[] dobierzWartosci(int liczbaArgumentow, Regulator regulator, Obiekt obiekt)
    {
        populacja = new ArrayList<Osobnik>();
        Random r = new Random();
        double YMax = obiekt.getYMax();
        regulator.setCel(YMax/2);
        for (int i = 0; i<rozmiarPopulacji; i++)
        {
            Osobnik osobnikTemp = new Osobnik(liczbaArgumentow);
            for(int j = 0; j<liczbaArgumentow; j++)
            {
                osobnikTemp.setParametryIndex(j,r.nextDouble(1.0));
            }
            regulator.zmienWartosci(osobnikTemp.getParametry());
            obiekt.resetObiektu();
            osobnikTemp.setWartosc(obiekt.obliczPraceObiektu(regulator, YMax/2));
            populacja.add(osobnikTemp);
        }
        Collections.sort(populacja);
        for(int k = 0; k<liczbaIteracji; k++)
        {
         mutacje(liczbaArgumentow,regulator, obiekt, YMax);
        }
        Collections.sort(populacja);
        return populacja.get(0).getParametry();
    }

    private void mutacje (int liczbaArgumentow, Regulator regulator, Obiekt obiekt, double YMax)
    {
        Random r = new Random();
        List<Osobnik> reprodukcja = new ArrayList<Osobnik>();
        Collections.sort(populacja);
        for(int i = 0; i<rozmiarElity; i++)
        {
            reprodukcja.add(populacja.get(i));
        }
        for(int i = 0; i<iloscKrzyzowania; i++)
        {
            int osobnik1 = r.nextInt(rozmiarElity);
            int osobnik2 = r.nextInt(rozmiarElity);
            Osobnik osobnikTemp = new Osobnik(liczbaArgumentow);
            for(int j = 0; j<liczbaArgumentow; j++)
            {
                osobnikTemp.getParametry()[j]= (r.nextBoolean())? populacja.get(osobnik1).getParametry()[j] : populacja.get(osobnik2).getParametry()[j];
            }
            regulator.zmienWartosci(osobnikTemp.getParametry());
            obiekt.resetObiektu();
            osobnikTemp.setWartosc(obiekt.obliczPraceObiektu(regulator, YMax/2));
            reprodukcja.add(osobnikTemp);
        }
        for(int i = 0; i<iloscMutacji; i++)
        {
            int rodzic = r.nextInt(rozmiarElity);
            Osobnik osobnikTemp = new Osobnik(liczbaArgumentow);
            for(int j = 0; j<liczbaArgumentow; j++)
            {
                osobnikTemp.getParametry()[j]= (r.nextDouble()<getPrawdopodobienstwoMutacji())? r.nextGaussian(populacja.get(rodzic).getParametry()[j],0.4) : populacja.get(rodzic).getParametry()[j];
            }
            regulator.zmienWartosci(osobnikTemp.getParametry());
            obiekt.resetObiektu();
            osobnikTemp.setWartosc(obiekt.obliczPraceObiektu(regulator, YMax/2));
            reprodukcja.add(osobnikTemp);
        }
        populacja = reprodukcja;
    }
}
