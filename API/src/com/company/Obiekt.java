package com.company;

import com.company.regulatory.Regulator;
import lombok.Data;

import java.util.*;

@Data
public class Obiekt {

    private double[] z;
    private double[] b;
    private double K;
    private double uMax;
    private double uMin;
    private List<Double> U ;
    private List<Double> Y ;
    private double Ypp;
    private double Upp;
    private double YMax;
    private double Ts;
    public Obiekt() {
    }

    public Obiekt(double[] z,double[] b, double K, double uMax, double uMin, double Ts)
    {
        this.Ts = Ts;
        this.setK(K);
        if(z.length==2&&b.length==3)
            obiekt2b3z(z,b);

        this.setUMax(uMax);
        this.setUMin(uMin);
        this.Ypp = 0;
        this.Upp = 0;
        this.YMax = obliczYMax();
    }

    public double obliczKrok(double du)
    {
        double Uakt = U.get(0) + du;
        if(Uakt>uMax)
            Uakt=uMax;
        else if (Uakt<uMin)
            Uakt=uMin;
        for(int i = U.size()-1; i>0 ;i--)
            U.set(i,U.get(i-1));
        U.set(0,Uakt);

        double Yakt ;
        Yakt = 0.0 ;
        for(int i = 0; i<z.length; i++)
            Yakt+=U.get(i)*z[i];
        for(int i = 0; i<b.length; i++)
            Yakt+=Y.get(i)*b[i];

        for(int i = Y.size()-1; i>0 ;i--)
            Y.set(i,Y.get(i-1));
        Y.set(0,Yakt);
        return Yakt;
    }
    public Double getAktualna(){
        try{

            return Y.get(0);
        } catch (Exception ex)
        {
            return 0.0;
        }
    }

    public void resetObiektu()
    {
        setU(new ArrayList<Double>(Collections.nCopies(U.size(), Upp)));
        setY(new ArrayList<Double>(Collections.nCopies(Y.size(), Ypp)));
    }
    public double obliczPraceObiektu(Regulator regulator, double cel)
    {
        List<Double> Y = new ArrayList<>();
        int dlugoscBadania = 50;
        resetObiektu();
        double blad = 0.0;
        regulator.setCel(cel);
        for (int i = 0; i<dlugoscBadania; i++)
        {
            Y.add(obliczKrok(regulator.policzOutput(getAktualna())));
        }
        for(int i = 0; i<dlugoscBadania; i++)
        {
            blad+=Math.pow((Y.get(i)-cel),2);
        }
        blad=blad/Y.size();
        Y.clear();

        blad=blad/Y.size();
        resetObiektu();
        return blad;
    }
    private double obliczYMax()
    {
        double Ytemp;
        for (int i = 0; i<100; i++)
        {
            obliczKrok(uMax);
        }
        Ytemp = getAktualna();
        resetObiektu();
        return Ytemp;
    }
    private void obiekt2b3z(double[] z, double[] b)
    {
        double z1 = z[0];
        double z2 = z[1];
        double b1 = b[0];
        double b2 = b[1];
        double b3 = b[2];
        this.U = Arrays.asList(0.0,0.0,0.0);
        this.Y = Arrays.asList(0.0,0.0,0.0);
        this.z = new double[3];
        this.b = new double[3];

        double k = (b1*b1+(-(z1+z2)*b1)+z1*z2)/((b2-b1)*b3- b1*b2+b1*b1)/b1;
        double l = -(b2*b2+(-(z1+z2)*b2 + z1*z2))/((b2-b1)*b3-b2*b2+b1*b2)/b2;
        double m = (b3*b3+(-(z1+z2)*b3+z1*z2))/(b3*b3-(b1+b2)*b3+b1*b2)/b3;
        double ap = ePotega(b1);
        double bp = ePotega(b2);
        double cp = ePotega(b3);
        this.z[0] = this.K*(-k*ap + k - l*bp + l - m*cp + m);
        this.z[1] = this.K*(k*ap*cp + k*ap*bp - k*cp - k*bp + l*bp*cp + l*bp*ap -l*cp - l*ap + m*bp*cp + m*ap*cp - m*bp - m*ap);
        this.z[2] = this.K*(-k*ap*bp*cp + k*bp*cp - l*ap*bp*cp + l*ap*cp - m*ap*bp*cp +m*ap*bp);

        this.b[0] = ap+bp+cp;
        this.b[1] = ap*bp + ap*cp + bp*cp;
        this.b[2] = ap*bp*cp;
    }
    private double ePotega(double x)
    {
        return Math.exp(-x*this.Ts);
    }
}
