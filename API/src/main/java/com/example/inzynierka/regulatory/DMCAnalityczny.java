package com.example.inzynierka.regulatory;

import Jama.Matrix;
import com.example.inzynierka.obiekty.MIMODPA;
import com.example.inzynierka.obiekty.SISODPA;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class DMCAnalityczny extends RegulatorMPC {

    protected Matrix Mpz;
    protected Matrix Mz;
    private Matrix K;
    protected List<List<Double>> S;
    protected List<List<Double>> Sz;
    protected Matrix dU;

    protected Matrix dUz;
    protected Matrix M;
    protected Matrix Mp;

    protected Double[] strojenieZadane;
    protected int liczbaStrojeniaZadanego;

    public DMCAnalityczny() {
    }

    public DMCAnalityczny(int Nu, double lambda, SISODPA SISODPA, double cel, double duMax, int N, Double[] strojenieZadane) {
        this(Nu, lambda, SISODPA, cel, duMax, N);
        if (strojenieZadane[0] != null) {
            liczbaStrojeniaZadanego = 1;
            this.strojenieZadane = strojenieZadane;
            this.getLambda().set(0, strojenieZadane[0]);
            this.policzWartosci(SISODPA);
        }
    }

    public DMCAnalityczny(int Nu, double lambda, SISODPA SISODPA, double cel, double duMax, int N) {
        this.Lambda = Arrays.asList(lambda);
        this.Nu = Nu;
        this.N = N;
        this.cel = new double[]{cel};
        this.duMax = duMax;
        policzWartosci(SISODPA);

    }

    public DMCAnalityczny(int Nu, double[] lambda, MIMODPA obiekt, double[] cel, double duMax, int N, Double[] strojenieZadane) {
        this(Nu, lambda, obiekt, cel, duMax, N);
        this.liczbaStrojeniaZadanego = 0;
        this.strojenieZadane = strojenieZadane;
        for (int i = 0; i < strojenieZadane.length; i++) {
            if (strojenieZadane[i] != null) {
                liczbaStrojeniaZadanego += 1;
                this.getLambda().set(i, strojenieZadane[i]);
            }
        }
        this.policzWartosci(obiekt);
    }

    public DMCAnalityczny(int Nu, double[] lambda, MIMODPA obiekt, double[] cel, double duMax, int N) {
        List<Double> tempLambda = new ArrayList<>();
        for (double wartosc : lambda) {
            tempLambda.add(wartosc);
        }
        this.Lambda = new ArrayList<>(tempLambda);
        this.Nu = Nu;
        this.N = N;
        this.cel = cel;
        this.duMax = duMax;
        policzWartosci(obiekt);
    }

    public double policzSterowanie(double aktualna) {
        Matrix yZad = ustawMatrixYZad();
        double[] yTemp = new double[N];
        Arrays.fill(yTemp, aktualna);
        Matrix y = new Matrix(yTemp, 1);
        Matrix Utemp = K.times(yZad.transpose().minus(y.transpose()).minus(Mp.times(dU.transpose())));
        poprawaUTemp(Utemp, 0);
        dodajdU(Utemp.get(0, 0));
        return Utemp.get(0, 0);
    }
    public double policzSterowanie(double aktualna, double[] sterowanieZaklocenia) {
        Matrix yZad = ustawMatrixYZad();
        dodajdUz(sterowanieZaklocenia);
        double[] yTemp = new double[N];
        Arrays.fill(yTemp, aktualna);
        Matrix y = new Matrix(yTemp, 1);
        Matrix Utemp = K.times(yZad.transpose().minus(y.transpose()).minus(Mp.times(dU.transpose()))
            .minus(Mpz.times(dUz.transpose())));
        poprawaUTemp(Utemp, 0);
        dodajdU(Utemp.get(0, 0));
        return Utemp.get(0, 0);
    }

    public double[] policzSterowanie(double[] aktualna) {
        int OUT = cel.length;
        double[] celTemp = new double[N * OUT];
        for (int i = 0; i < N * OUT; i++) {
            celTemp[i] = cel[i % OUT];
        }
        Matrix yZad = new Matrix(celTemp, 1);
        double[] yTemp = new double[N * OUT];
        for (int i = 0; i < N * OUT; i++) {
            yTemp[i] = aktualna[i % OUT];
        }
        Matrix y = new Matrix(yTemp, 1);
        Matrix Utemp = K.times(yZad.transpose().minus(y.transpose()).minus(Mp.times(dU.transpose())));
        double[] tempdU = new double[getLambda().size()];
        for (int i = 0; i < getLambda().size(); i++) {
            poprawaUTemp(Utemp, i);
            tempdU[i] = Utemp.get(i, 0);
        }
        dodajdU(tempdU);
        return tempdU;
    }
    public double[] policzSterowanie(double[] aktualna, double[] sterowanieZaklocenia) {
        int OUT = cel.length;
        dodajdUz(sterowanieZaklocenia);
        double[] celTemp = new double[N * OUT];
        for (int i = 0; i < N * OUT; i++) {
            celTemp[i] = cel[i % OUT];
        }
        Matrix yZad = new Matrix(celTemp, 1);
        double[] yTemp = new double[N * OUT];
        for (int i = 0; i < N * OUT; i++) {
            yTemp[i] = aktualna[i % OUT];
        }
        Matrix y = new Matrix(yTemp, 1);
        Matrix Utemp = K.times(yZad.transpose().minus(y.transpose()).minus(Mp.times(dU.transpose()))
            .minus(Mpz.times(dUz.transpose())));
        double[] tempdU = new double[getLambda().size()];
        for (int i = 0; i < getLambda().size(); i++) {
            poprawaUTemp(Utemp, i);
            tempdU[i] = Utemp.get(i, 0);
        }
        dodajdU(tempdU);
        return tempdU;
    }

    protected void poprawaUTemp(Matrix Utemp, int i) {
        if (Utemp.get(i, 0) > duMax) {
            Utemp.set(i, 0, duMax);
        } else if (Utemp.get(i, 0) < -duMax) {
            Utemp.set(i, 0, -duMax);
        }
    }

    protected Matrix ustawMatrixYZad() {
        Matrix yZad = new Matrix(cel.length, N);
        for (int i = 0; i < cel.length; i++)
            for (int j = 0; j < N; j++)
                yZad.set(i, j, cel[i]);
        return yZad;
    }



    protected void policzWartosci(SISODPA SISODPA) {
        this.S = new ArrayList<>();
        policzS(SISODPA);
        policzMp();
        policzM();
        policzK();
        if(SISODPA.getZakloceniaMierzalne()!=null && !SISODPA.getZakloceniaMierzalne().isEmpty()) {
            resetujZaklocenia(SISODPA.getZakloceniaMierzalne().size());
            policzSz(SISODPA);
            policzMz(SISODPA.getZakloceniaMierzalne().size(), 1);
            policzMpz(SISODPA.getZakloceniaMierzalne().size(), 1);
        }
        resetujRegulator(1);
        SISODPA.resetObiektu();
    }

    protected void policzWartosci(MIMODPA MIMODPA) {
        this.S = new ArrayList<>();
        policzS(MIMODPA);
        policzMp(MIMODPA.getLiczbaIN(), MIMODPA.getLiczbaOUT());
        policzM(MIMODPA.getLiczbaIN(), MIMODPA.getLiczbaOUT());
        policzK(MIMODPA.getLiczbaIN());
        if(MIMODPA.getZakloceniaMierzalne()!=null ) {
            resetujZaklocenia(MIMODPA.getZakloceniaMierzalne().getLiczbaIN());
            policzSz(MIMODPA);
            policzMz(MIMODPA.getZakloceniaMierzalne().getLiczbaIN(), MIMODPA.getZakloceniaMierzalne().getLiczbaOUT());
            policzMpz(MIMODPA.getZakloceniaMierzalne().getLiczbaIN(), MIMODPA.getZakloceniaMierzalne().getLiczbaOUT());
        }
        resetujRegulator(MIMODPA.getLiczbaIN());
        MIMODPA.resetObiektu();
    }

    public void zmienNastawy(double[] wartosci) {
        List<Double> tempLambda = new ArrayList<>();
        if (this.liczbaStrojeniaZadanego == 0) {
            for (double wartosc : wartosci) {
                tempLambda.add(wartosc);
            }
        } else {
            int iTemp = 0;
            for (int i = 0; i < getLambda().size(); i++) {
                if (strojenieZadane[i] != null) {
                    tempLambda.add(strojenieZadane[i]);
                } else {
                    tempLambda.add(wartosci[iTemp]);
                    iTemp += 1;
                }
            }
        }
        setLambda(tempLambda);
        policzK(tempLambda.size());
        resetujRegulator(tempLambda.size());
    }

    public void resetujRegulator() {
        for (int i = 0; i < dU.getColumnDimension(); i++)
            for (int j = 0; j < dU.getRowDimension(); j++)
                dU.set(j, i, 0.0);
        if(dUz!=null) {
            for (int i = 0; i < dUz.getColumnDimension(); i++)
                for (int j = 0; j < dUz.getRowDimension(); j++)
                    dUz.set(j, i, 0.0);
        }
    }

    public void resetujRegulator(int IN) {
        dU = new Matrix(1, (D - 1) * IN, 0.0);
    }
    public void resetujZaklocenia(int IN) {
        dUz = new Matrix(1, (D) * IN, 0.0);
    }
    protected void policzS(SISODPA SISODPA) {
        double U = 1;
        int i = 2;
        List<Double> Stemp = new ArrayList<Double>();
        double Utemp = 0;
        SISODPA.resetObiektu();
        Stemp.add((SISODPA.obliczKrok(U) - SISODPA.getYpp()) / U);
        Stemp.add((SISODPA.obliczKrok(Utemp) - SISODPA.getYpp()) / U);
        while (!(Math.abs(Stemp.get(i - 1) - Stemp.get(i - 2)) < 0.002) || Stemp.get(i - 2) == 0.0) {
            Stemp.add((SISODPA.obliczKrok(Utemp) - SISODPA.getYpp()) / U);
            i++;
        }
        this.S.add(Stemp);
        this.D = S.get(0).size();
        this.N = D;
    }

    protected void policzSz(SISODPA obiekt) {
        this.Sz = new ArrayList<>();
        for(int i = 0; i < obiekt.getZakloceniaMierzalne().size(); i++) {
            List<Double> Stemp = new ArrayList<Double>();
            double Utemp = 0;
            obiekt.resetObiektu();
            Stemp.add(obiekt.obliczKrokZaklocenia(1,i) / 1);
            for(int j = 1; j<D+1; j++) {
                Stemp.add(obiekt.obliczKrokZaklocenia(Utemp, i) / 1);
            }
            this.Sz.add(Stemp);
        }
    }
    protected void policzSz(MIMODPA obiekt) {
        this.Sz = new ArrayList<>();
        for (int i = 0; i < obiekt.getZakloceniaMierzalne().getLiczbaOUT(); i++) {
            for (int j = 0; j < obiekt.getZakloceniaMierzalne().getLiczbaIN(); j++) {
                obiekt.resetObiektu();
                double U = 1;
                double Utemp = 0;
                List<Double> Stemp = new ArrayList<>();
                Stemp.add(obiekt.obliczKrokZaklocenia(U, j, i) / U);
                for (int k = 1; k < D + 1; k++) {
                    Stemp.add(obiekt.obliczKrokZaklocenia(Utemp,j, i) / U);
                }
                this.Sz.add(Stemp);
            }
        }
    }
    protected void policzS(MIMODPA obiekt) {
        for (int i = 0; i < obiekt.getLiczbaOUT(); i++) {
            for (int j = 0; j < obiekt.getLiczbaIN(); j++) {
                obiekt.resetObiektu();
                double U = 1;
                double Utemp = 0;
                int k = 2;
                List<Double> Stemp = new ArrayList<>();
                Stemp.add((obiekt.obliczKrok(U, j, i) - obiekt.getYpp(i)) / U);
                Stemp.add((obiekt.obliczKrok(Utemp, j, i) - obiekt.getYpp(i)) / U);
                while (!(Math.abs(Stemp.get(k - 1) - Stemp.get(k - 2)) < 0.005) || Stemp.get(k - 2) == 0.0) {
                    Stemp.add((obiekt.obliczKrok(Utemp, j, i) - obiekt.getYpp(i)) / U);
                    k++;
                }
                this.S.add(Stemp);
            }
        }
        this.D = S.get(0).size();
        for (int i = 0; i < S.size(); i++) {
            if (D < S.get(i).size()) {
                D = S.get(i).size();
            }
        }
        for (int i = 0; i < S.size(); i++) {
            while (D != S.get(i).size()) {
                S.get(i).add(S.get(i).get(S.get(i).size()-1));
            }
        }
        this.N = D;
    }

    protected void policzM() {
        Matrix M = new Matrix(N, Nu);
        for (int i = 0; i < Nu; i++) {
            for (int j = 0; j < N; j++) {
                if (j >= i)
                    M.set(j, i, S.get(0).get(j - i));
            }
        }
        this.M = M;
    }

    protected void policzM(int IN, int OUT) {
        Matrix M = new Matrix(N * OUT, Nu * IN);
        for (int i = 0; i < Nu; i++) {
            for (int j = 0; j < N; j++) {
                if (j >= i)
                    for (int k = 0; k < OUT; k++)
                        for (int m = 0; m < IN; m++)
                            M.set(j * OUT + k, i * IN + m, S.get(k * IN + m).get(j - i));
            }
        }
        this.M = M;
    }

    protected void policzMp() {
        Mp = new Matrix(N, D - 1);
        for (int i = 0; i < D - 1; i++) {
            for (int j = 0; j < N; j++) {
                if ((j + i + 1) < D)
                    Mp.set(j, i, S.get(0).get(j + i + 1) - S.get(0).get(i));
                else
                    Mp.set(j, i, S.get(0).get(D - 1) - S.get(0).get(i));
            }
        }
    }

    protected void policzMp(int IN, int OUT) {
        Mp = new Matrix(N * OUT, (D - 1) * IN);
        for (int i = 0; i < D - 1; i++) { //bok ,wszerz
            for (int j = 0; j < N; j++) { //dół
                for (int k = 0; k < OUT; k++) {
                    for (int m = 0; m < IN; m++) {
                        if ((j + i + 1) < D) {
                            Mp.set(j * OUT + k, i * IN + m, S.get(k * IN + m).get(j + i + 1) - S.get(k * IN + m).get(i));
                        } else
                            Mp.set(j * OUT + k, i * IN + m, S.get(k * IN + m).get(D - 1) - S.get(k * IN + m).get(i));
                    }
                }
            }
        }
    }
    protected void policzMz(int IN, int OUT) {
        Matrix Mz = new Matrix(N * OUT, (D) * IN);
        for (int i = 0; i < D ; i++) { //wszerz
            for (int j = -1; j < N-1; j++) { //wzdłuż
                if(i>1+Nu) {
                    for (int k = 0; k < OUT; k++)
                        for (int m = 0; m < IN; m++)
                            if(j<Nu) {
                                Mz.set((j+1) * OUT + k, i * IN + m, 0.0);
                            } else {
                                Mz.set((j+1) * OUT + k, i * IN + m,  Mz.get((j+1) * OUT + k, (i - 1) * IN + m) * 0.6 );
                            }
                } else if (j >= i)
                    for (int k = 0; k < OUT; k++)
                        for (int m = 0; m < IN; m++)
                            if(j - i  >= D) {
                                Mz.set((j+1) * OUT + k, i * IN + m, Sz.get(k * IN + m).get(D));
                            } else {
                                Mz.set((j+1) * OUT + k, i * IN + m, Sz.get(k * IN + m).get(j - i + 1));
                            }
            }
        }
        this.Mz = Mz;
    }
    protected void policzMpz(int IN, int OUT) {
        Mpz = new Matrix(N * OUT, (D) * IN);
        //pierwsza kolumna
        for (int j = 0; j < N; j++) {
            for (int k = 0; k < OUT; k++) {
                for (int m = 0; m < IN; m++) {
                    if (j  <=  D ) {
                        Mpz.set(j * OUT + k, m, Sz.get(k * IN + m).get(j));
                    } else
                        Mpz.set(j * OUT + k, m, Sz.get(k * IN + m).get(D));
                }
            }
        }
        //kolejne
        for (int i = 0; i < D - 1; i++) {
            for (int j = 0; j < N; j++) {
                for (int k = 0; k < OUT; k++) {
                    for (int m = 0; m < IN; m++) {
                        if ((j + i + 1) < D+1) {
                            Mpz.set(j * OUT + k, (i + 1) * IN + m, (Sz.get(k * IN + m).get(j + i + 1) - Sz.get(k * IN + m).get(i)));
                        } else
                            Mpz.set(j * OUT + k, (i + 1) * IN + m, (Sz.get(k * IN + m).get(D) - Sz.get(k * IN + m).get(i)));
                    }
                }
            }
        }
    }


    private void policzK() {
        Matrix I = new Matrix(Nu, Nu);
        for (int i = 0; i < Nu; i++) {
            for (int j = 0; j < Nu; j++) {
                if (i == j) {
                    I.set(i, j, getLambda().get(0));
                } else
                    I.set(i, j, 0);
            }
        }
        this.K = ((M.transpose().times(M).plus(I)).inverse()).times(M.transpose());
    }

    private void policzK(int IN) {
        Matrix I = new Matrix(Nu * IN, Nu * IN);
        for (int i = 0; i < Nu; i++) {
            for (int j = 0; j < Nu; j++) {
                for (int m = 0; m < IN; m++) {
                    if (i == j) {
                        I.set(i * IN + m, j * IN + m, getLambda().get(m));
                    } else
                        I.set(i * IN + m, j * IN + m, 0);
                }
            }
        }
        this.K = ((M.transpose().times(M).plus(I)).inverse()).times(M.transpose());
    }

    protected void dodajdU(double dUAktualne) {
        for(int i = D-1; i > D - 11; i-- )
            dU.set(0, i-1, (dU.get(0,  i-1) + 3*dU.get(0, i-2))/4 );
        for (int i = D - 11; i > 1; i--) {
            dU.set(0, i - 1, dU.get(0, i - 2));
        }
        dU.set(0, 0, dUAktualne);
    }

    protected void dodajdUz(double[] sterowanieZaklocenia) {
        if(sterowanieZaklocenia.length>1) {
            for (int i = D - 1; i > 1; i--) {
                for (int j = sterowanieZaklocenia.length - 1; j >= 0; j--)
                    dUz.set(0, sterowanieZaklocenia.length * i - 1 - j, dUz.get(0, sterowanieZaklocenia.length * i - sterowanieZaklocenia.length - j - 1));
            }

            for (int j = sterowanieZaklocenia.length - 1; j >= 0; j--)
                dUz.set(0, j, sterowanieZaklocenia[sterowanieZaklocenia.length-j-1]);

        } else {
            for (int i = D; i > 1; i--) {
                dUz.set(0, i - 1, dUz.get(0, i - 2));
            }
            dUz.set(0, 0, sterowanieZaklocenia[0]);
        }
    }

    protected void dodajdU(double[] dUAktualne) {

        for (int i = D - 1; i > 1; i--) {
            for (int j = dUAktualne.length - 1; j >= 0; j--)
                dU.set(0, dUAktualne.length * i  - j - 1, dU.get(0, dUAktualne.length * i - dUAktualne.length - j - 1));
        }

        for (int j = 0; j < dUAktualne.length ; j++)
            dU.set(0, j, dUAktualne[j]);

    }

    public int liczbaZmiennych() {

        return getLambda().size() - liczbaStrojeniaZadanego;
    }
}
