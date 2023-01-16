package com.example.inzynierka.obiekty;

import com.example.inzynierka.regulatory.Regulator;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class SISO {

    private double[] z;
    private double[] b;
    private double K;
    private double uMax;
    private double uMin = 0;
    private List<Double> U ;
    private List<Double> Y ;
    private double Ypp;
    private double Upp;
    private double YMax;
    private double Ts;
    private int opoznienie;
    private double szum;
    public SISO() {}

    public SISO(Double[] z, Double[] b, double K, double uMax, double Ts, int opoznienie, double szum)
    {
        this.Ts = Ts;
        this.setK(K);
        this.setUMax(uMax);
        this.Ypp = 0;
        this.Upp = 0;
        this.opoznienie = opoznienie;
        if(z.length==2&&b.length==3)
            obiekt3b2z(z,b);
        else if(z.length==1&&b.length==3)
            obiekt3b1z(z,b);
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
            Yakt+=U.get(i+opoznienie)*z[i];
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
        int dlugoscBadania = 50;
        resetObiektu();
        double blad = 0.0;
        regulator.setCel(cel);
        for (int i = 0; i<dlugoscBadania; i++)
        {
            if(i==25)
                blad = Math.abs(obliczKrok(regulator.policzOutput(getAktualna()))-cel);
//            blad+=Math.pow(obliczKrok(regulator.policzOutput(getAktualna()))-cel,2);
        }
//        blad=blad/dlugoscBadania;
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
    private void obiekt3b2z(Double[] z, Double[] b)
    {
        double z1 = z[0];
        double z2 = z[1];
        double b1 = b[0];
        double b2 = b[1];
        double b3 = b[2];
        this.U = new ArrayList(Collections.nCopies(3+opoznienie, Upp));
        this.Y = new ArrayList(Collections.nCopies(3, Ypp));;
        this.z = new double[3];
        this.b = new double[3];

        double k = (b1*b1 + (-(z1 + z2)*b1)+z1*z2)/((b2 - b1)*b3 - b1*b2 + b1*b1)/b1;
        double l = -(b2*b2+(-(z1 + z2)*b2 + z1*z2))/((b2 - b1)*b3 - b2*b2 + b1*b2)/b2;
        double m = (b3*b3+(-(z1 + z2)*b3 + z1*z2))/(b3*b3 - (b1 + b2)*b3 + b1*b2)/b3;
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
    private void obiekt3b1z(Double[] z, Double[] b)
    {
        double z1 = z[0];
        double b1 = b[0];
        double b2 = b[1];
        double b3 = b[2];
        this.U = new ArrayList(Collections.nCopies(3+opoznienie, Upp));
        this.Y = new ArrayList(Collections.nCopies(3, Ypp));;
        this.z = new double[3];
        this.b = new double[3];
        double k = (z1 - b1)/(b1*b1 - b1*b2 + b3*(b2-b1))/b1;
        double l = (z1 - b2)/(b2*b2 - b1*b2 + b3*(b2-b1))/b2;
        double m = (b3 - z1)/(b3*b3 + b1*b2 - b3*(b1+b2))/b3;
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
