package com.example.inzynierka.regulatory;

import com.example.inzynierka.obiekty.MIMO;

import java.util.ArrayList;
import java.util.List;

public class ZbiorPID extends Regulator{

    private List<PID> PIDy;
    private Integer[] PV;
    private double[] uMax;
    public ZbiorPID(MIMO obiekt, Integer[] PV, double duMax)
    {
        PIDy = new ArrayList<>(PV.length);
        uMax= obiekt.getUMax();
        for(int i = 0; i<PV.length; i++) {
            double[] yMaxTemp = new double[]{obiekt.getYMax()[i] / 2};
            PIDy.add(new PID(1.0, 1.0, 1.0, obiekt.getTp(PV[i]), yMaxTemp, duMax, uMax[PV[i]]));
        }
        this.PV = PV;

    }

    @Override
    public double policzOutput(double aktualna) {
        return 0;
    }

    public double[] policzOutput(double[] aktualne)
    {
        double[] output = new double[PV.length];
        for(int i = 0; i<PV.length; i++)
        {
            output[PV[i]] = PIDy.get(i).policzOutput(aktualne[i]);
        }
        return output;
    }
    @Override
    public void setCel(double[] cel)
    {
        this.cel = cel;
        for(int i = 0; i < cel.length; i++)
        {
            PIDy.get(i).setCel(new double[]{cel[i]});
        }
    }
    @Override
    public void resetujRegulator()
    {
        for(PID pid: PIDy)
        {
            pid.resetujRegulator();
        }
    }

    @Override
    public void zmienWartosci(double[] wartosci) {
        for(int i = 0; i < wartosci.length/3; i ++)
        {
            double[] wartosciTemp = new double[]{wartosci[i*3], wartosci[i*3+1], wartosci[i*3+2]};
            PIDy.get(i).zmienWartosci(wartosciTemp);
        }
    }
    @Override
    public int liczbaZmiennych()
    {
        return 3 * PV.length;
    }
}
