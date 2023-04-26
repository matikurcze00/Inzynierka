package com.example.inzynierka.EA;

import com.example.inzynierka.obiekty.MIMODPA;
import com.example.inzynierka.obiekty.SISODPA;
import com.example.inzynierka.regulatory.Regulator;
import lombok.Data;

import java.util.*;

@Data
public class AlgorytmEwolucyjny {
    private int rozmiarPopulacji;
    private int liczbaIteracji;
    private int rozmiarElity;
    private int iloscKrzyzowania;
    private int iloscMutacji;
    private double prawdopodobienstwoMutacji;
    private List<Osobnik> populacja;

    public AlgorytmEwolucyjny(int rozmiarPopulacji, int liczbaIteracji, int rozmiarElity, double prawdopodobienstwoMutacji, double czestotliwoscKrzyzowania) {
        this.rozmiarPopulacji = rozmiarPopulacji;
        this.liczbaIteracji = liczbaIteracji;
        this.rozmiarElity = rozmiarElity;
        this.prawdopodobienstwoMutacji = prawdopodobienstwoMutacji;
        this.iloscKrzyzowania = (int) Math.floor(((rozmiarPopulacji - rozmiarElity)) * czestotliwoscKrzyzowania);
        this.iloscMutacji = rozmiarPopulacji - rozmiarElity - iloscKrzyzowania;
    }

    public AlgorytmEwolucyjny() {
    }

    public double[] dobierzWartosci(int liczbaArgumentow, Regulator regulator, SISODPA SISODPA) {
        populacja = new ArrayList<Osobnik>();
        Random r = new Random();
        double[] cel = new double[]{SISODPA.getYMax() / 2};
        regulator.setCel(cel);
        for (int i = 0; i < rozmiarPopulacji; i++) {
            Osobnik osobnikTemp = new Osobnik(liczbaArgumentow);
            for (int j = 0; j < liczbaArgumentow; j++) {
                osobnikTemp.setParametryIndex(j, r.nextDouble(5.0));
            }
            regulator.zmienNastawy(osobnikTemp.getParametry());
            SISODPA.resetObiektu();
            regulator.resetujRegulator();
            osobnikTemp.setWartosc(SISODPA.obliczPraceObiektu(regulator, cel));
            populacja.add(osobnikTemp);
        }
        Collections.sort(populacja);
        for (int k = 0; k < liczbaIteracji; k++) {
            ewolucje(liczbaArgumentow, regulator, SISODPA, cel);
        }
        Collections.sort(populacja);
        return populacja.get(0).getParametry();
    }

    private void ewolucje(int liczbaArgumentow, Regulator regulator, SISODPA SISODPA, double[] cel) {
        Random r = new Random();
        List<Osobnik> reprodukcja = new ArrayList<Osobnik>();
        Collections.sort(populacja);
        for (int i = 0; i < rozmiarElity; i++) {
            reprodukcja.add(populacja.get(i));
        }
        krzyzowania(liczbaArgumentow, regulator, SISODPA, cel, r, reprodukcja);
        mutacje(liczbaArgumentow, regulator, SISODPA, cel, r, reprodukcja);
        populacja = reprodukcja;
    }

    private void mutacje(int liczbaArgumentow, Regulator regulator, SISODPA SISODPA, double[] cel, Random r, List<Osobnik> reprodukcja) {
        for (int i = 0; i < iloscMutacji; i++) {
            int rodzic = r.nextInt(rozmiarElity);
            Osobnik osobnikTemp = new Osobnik(liczbaArgumentow);
            for (int j = 0; j < liczbaArgumentow; j++) {
                osobnikTemp.getParametry()[j] = (r.nextDouble() < getPrawdopodobienstwoMutacji()) ? Math.abs(r.nextGaussian(populacja.get(rodzic).getParametry()[j], 0.4)) : populacja.get(rodzic).getParametry()[j];
            }
            regulator.zmienNastawy(osobnikTemp.getParametry());
            SISODPA.resetObiektu();
            regulator.resetujRegulator();
            osobnikTemp.setWartosc(SISODPA.obliczPraceObiektu(regulator, cel));
            reprodukcja.add(osobnikTemp);
        }
    }

    private void krzyzowania(int liczbaArgumentow, Regulator regulator, SISODPA SISODPA, double[] cel, Random r, List<Osobnik> reprodukcja) {
        for (int i = 0; i < iloscKrzyzowania; i++) {
            int osobnik1 = r.nextInt(rozmiarPopulacji);
            int osobnik2 = r.nextInt(rozmiarPopulacji);
            Osobnik osobnikTemp = new Osobnik(liczbaArgumentow);
            for (int j = 0; j < liczbaArgumentow; j++) {
                osobnikTemp.getParametry()[j] = (r.nextBoolean()) ? populacja.get(osobnik1).getParametry()[j] : populacja.get(osobnik2).getParametry()[j];
            }
            regulator.zmienNastawy(osobnikTemp.getParametry());
            SISODPA.resetObiektu();
            regulator.resetujRegulator();
            osobnikTemp.setWartosc(SISODPA.obliczPraceObiektu(regulator, cel));
            reprodukcja.add(osobnikTemp);
        }
    }

    public double[] dobierzWartosci(int liczbaArgumentow, Regulator regulator, MIMODPA obiekt) {
        populacja = new ArrayList<Osobnik>();
        Random r = new Random();
        double[] cel = Arrays.copyOf(obiekt.getYMax(), obiekt.getYMax().length);
        for (int i = 0; i < cel.length; i++)
            cel[i] = cel[i] / 5;

        regulator.setCel(cel);
        for (int i = 0; i < rozmiarPopulacji; i++) {
            Osobnik osobnikTemp = new Osobnik(liczbaArgumentow);
            for (int j = 0; j < liczbaArgumentow; j++) {
                osobnikTemp.setParametryIndex(j, r.nextDouble(3.0));
            }
            regulator.zmienNastawy(osobnikTemp.getParametry());
            obiekt.resetObiektu();
            osobnikTemp.setWartosc(obiekt.obliczPraceObiektu(regulator, cel));
            populacja.add(osobnikTemp);
        }
        Collections.sort(populacja);
        for (int k = 0; k < liczbaIteracji; k++) {
            ewolucje(liczbaArgumentow, regulator, obiekt, cel);
        }
        Collections.sort(populacja);
        return populacja.get(0).getParametry();
    }

    private void ewolucje(int liczbaArgumentow, Regulator regulator, MIMODPA obiekt, double[] cel) {
        Random r = new Random();
        List<Osobnik> reprodukcja = new ArrayList<Osobnik>();
        Collections.sort(populacja);
        for (int i = 0; i < rozmiarElity; i++) {
            reprodukcja.add(populacja.get(i));
        }
        krzyzowania(liczbaArgumentow, regulator, obiekt, cel, r, reprodukcja);
        mutacje(liczbaArgumentow, regulator, obiekt, cel, r, reprodukcja);
        populacja = reprodukcja;
    }

    private void mutacje(int liczbaArgumentow, Regulator regulator, MIMODPA obiekt, double[] cel, Random r, List<Osobnik> reprodukcja) {
        for (int i = 0; i < iloscMutacji; i++) {
            int rodzic = r.nextInt(rozmiarPopulacji);
            Osobnik osobnikTemp = new Osobnik(liczbaArgumentow);
            for (int j = 0; j < liczbaArgumentow; j++) {
                osobnikTemp.getParametry()[j] = (r.nextDouble() < getPrawdopodobienstwoMutacji()) ? Math.abs(r.nextGaussian(populacja.get(rodzic).getParametry()[j], 0.6)) : populacja.get(rodzic).getParametry()[j];
            }
            regulator.zmienNastawy(osobnikTemp.getParametry());
            obiekt.resetObiektu();
            osobnikTemp.setWartosc(obiekt.obliczPraceObiektu(regulator, cel));
            reprodukcja.add(osobnikTemp);
        }
    }

    private void krzyzowania(int liczbaArgumentow, Regulator regulator, MIMODPA obiekt, double[] cel, Random r, List<Osobnik> reprodukcja) {
        for (int i = 0; i < iloscKrzyzowania; i++) {
            int osobnik1 = r.nextInt(rozmiarPopulacji);
            int osobnik2 = r.nextInt(rozmiarPopulacji);
            Osobnik osobnikTemp = new Osobnik(liczbaArgumentow);
            for (int j = 0; j < liczbaArgumentow; j++) {
                osobnikTemp.getParametry()[j] = (r.nextBoolean()) ? populacja.get(osobnik1).getParametry()[j] : populacja.get(osobnik2).getParametry()[j];
            }
            regulator.zmienNastawy(osobnikTemp.getParametry());
            obiekt.resetObiektu();
            osobnikTemp.setWartosc(obiekt.obliczPraceObiektu(regulator, cel));
            reprodukcja.add(osobnikTemp);
        }
    }
}
