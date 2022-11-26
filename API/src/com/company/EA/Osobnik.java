package com.company.EA;

import lombok.Data;

@Data
public class Osobnik implements Comparable<Osobnik> {
    private double[] parametry;
    private double wartosc;

    public Osobnik(int rozmiarPopulacji)
    {
        parametry = new double[rozmiarPopulacji];
        wartosc = 0.0;
    }
    public void setParametryIndex(int index, double wartoscParametru)
    {
     parametry[index] = wartoscParametru;
    }
    @Override
    public int compareTo(Osobnik o)
    {
        if(this.getWartosc()>o.getWartosc()){
            return 1;
        }
        else if (this.getWartosc()==o.getWartosc())
        {
            return 0;
        }
        else
        {
            return -1;
        }
    }
}
