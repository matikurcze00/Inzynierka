package com.example.inzynierka.regulatory;

import com.example.inzynierka.obiekty.MIMODPA;

import java.util.ArrayList;
import java.util.List;

public class ZbiorPID extends Regulator {

    private final List<PID> PIDy;
    private final Integer[] PV;
    private final double[] uMax;
    private final Double[] strojenieZadane;
    private final int liczbaStrojeniaZadanego;

    public ZbiorPID(MIMODPA obiekt, Integer[] PV, double duMax, Double[] strojenieZadane) {
        PIDy = new ArrayList<>(PV.length);
        uMax = obiekt.getUMax();
        for (int i = 0; i < PV.length; i++) {
            double[] yMaxTemp = new double[] {obiekt.getYMax()[i] / 2};
            PIDy.add(new PID((strojenieZadane[i * 3] == null) ? 1.0 : strojenieZadane[i * 3],
                (strojenieZadane[i * 3 + 1] == null) ? 1.0 : strojenieZadane[i * 3 + 1],
                (strojenieZadane[i * 3 + 2] == null) ? 1.0 : strojenieZadane[i * 3 + 2],
                obiekt.getTp(PV[i]), yMaxTemp, duMax, uMax[PV[i]]));
        }
        this.PV = PV;
        int liczbaTemp = 0;
        this.strojenieZadane = strojenieZadane;
        for (Double wartosc : strojenieZadane) {
            if (wartosc != null) {
                liczbaTemp += 1;
            }
        }
        this.liczbaStrojeniaZadanego = liczbaTemp;
    }

    @Override
    public double policzSterowanie(double aktualna) {
        return 0;
    }

    @Override
    public double policzSterowanie(double aktualna, double[] UZ) {
        return 0;
    }

    @Override
    public double[] policzSterowanie(double[] aktualne) {
        double[] output = new double[PV.length];
        for (int i = 0; i < PV.length; i++) {
            output[PV[i]] = PIDy.get(i).policzSterowanie(aktualne[i]);
        }
        return output;
    }

    @Override
    public double[] policzSterowanie(double[] aktualna, double[] UZ) {
        return policzSterowanie(aktualna);
    }

    @Override
    public void setCel(double[] cel) {
        this.cel = cel;
        for (int i = 0; i < PIDy.size(); i++) {
            PIDy.get(i).setCel(new double[] {cel[i]});
        }
    }

    @Override
    public void resetujRegulator() {
        for (PID pid : PIDy) {
            pid.resetujRegulator();
        }
    }

    @Override
    public void zmienNastawy(double[] wartosci) {
        int iTemp = 0;
        for (int i = 0; i < PV.length; i++) {
            double[] wartosciTemp = new double[3];
            if (liczbaStrojeniaZadanego == 0) {
                wartosciTemp = new double[] {wartosci[i * 3], wartosci[i * 3 + 1], wartosci[i * 3 + 2]};
            } else {
                for (int j = 0; j < 3; j++) {
                    if (strojenieZadane[i * 3 + j] == null) {
                        wartosciTemp[j] = wartosci[iTemp];
                        iTemp += 1;
                    } else {
                        wartosciTemp[j] = strojenieZadane[i * 3 + j];
                    }
                }

            }
            PIDy.get(i).zmienNastawy(wartosciTemp);
        }
    }

    @Override
    public int liczbaZmiennych() {
        return 3 * PV.length - liczbaStrojeniaZadanego;
    }
}
