package com.example.inzynierka.serwisy;

import com.example.inzynierka.EA.AlgorytmEwolucyjny;
import com.example.inzynierka.modele.*;
import com.example.inzynierka.obiekty.MIMODPA;
import com.example.inzynierka.obiekty.SISODPA;
import com.example.inzynierka.regulatory.DMCAnalityczny;
import com.example.inzynierka.regulatory.PID;
import com.example.inzynierka.regulatory.Regulator;
import com.example.inzynierka.regulatory.ZbiorPID;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

@Service
public class StrojenieService {

    public OdpowiedzStrojenie SISOStrojenie(ParStrojenie parStrojenie) {
        ParObiekt parObiekt = parStrojenie.getParObiekt();
        ParRegulator parRegulator = parStrojenie.getParRegulator();
        ParWizualizacja parWizualizacja = parStrojenie.getParWizualizacja();
        Zaklocenia zaklocenie = new Zaklocenia();
        if(parStrojenie.getZaklocenia().getGain() != null && parStrojenie.getZaklocenia().getGain().length!=0) {
            zaklocenie = parStrojenie.getZaklocenia();
        }
        Integer[] PIE = new Integer[3];
        OdpowiedzStrojenie odpowiedzStrojenie = new OdpowiedzStrojenie();

        SISODPA obiekt = dobierzObiektSISO(parObiekt, parRegulator, parWizualizacja, zaklocenie);
        if (obiekt == null) return null;
        Regulator regulator;
        regulator = dobierzRegulatorSISO(parObiekt, parRegulator, parWizualizacja, obiekt, PIE);
        if (regulator == null) return null;
        AlgorytmEwolucyjny GA = new AlgorytmEwolucyjny(PIE[0], PIE[1], PIE[2], 0.3, 0.2);
        dobierzStrojenieSISO(parWizualizacja, obiekt, regulator, GA, odpowiedzStrojenie);
        if(parStrojenie.getParObiektSymulacji()!=null) {
            if(zaklocenie!=null) {
                obiekt = new SISODPA(parStrojenie.getParObiektSymulacji(), parRegulator.getUMax(), parRegulator.getUMin(), parWizualizacja.getBlad(), zaklocenie);
            } else {
                obiekt = new SISODPA(parStrojenie.getParObiektSymulacji(), parRegulator.getUMax(), parRegulator.getUMin(), parWizualizacja.getBlad());
            }
        }
        double[] Y = symulacjaRegulacjiSISO(parWizualizacja, obiekt, regulator, odpowiedzStrojenie, parStrojenie.getWizualizacjaZaklocen());
        System.out.println("strojenie::OK");
        obliczBladSISO(parWizualizacja, regulator, Y);
        return odpowiedzStrojenie;
    }

    private void obliczBladSISO(ParWizualizacja parWizualizacja, Regulator regulator, double[] Y) {
        double blad = 0.0;
        for(int i = 0; i < parWizualizacja.getSkok()[0]; i ++)
            blad += Math.pow(Y[i] - parWizualizacja.getYPP()[0], 2);
        for (int i = parWizualizacja.getSkok()[0]; i < parWizualizacja.getDlugosc(); i++) {
            blad += Math.pow(Y[i] - regulator.getCel()[0], 2);
        }
        blad = blad / parWizualizacja.getDlugosc() ;
        System.out.println("BLAD:" + blad);
    }

    private double[] symulacjaRegulacjiSISO(ParWizualizacja parWizualizacja, SISODPA obiekt, Regulator regulator, OdpowiedzStrojenie odpowiedzStrojenie, WizualizacjaZaklocen wizualizacjaZaklocen) {
        ustawCelSISO(parWizualizacja, regulator, odpowiedzStrojenie);
        if(wizualizacjaZaklocen.getUSkok()!=null && wizualizacjaZaklocen.getUSkok().length!=0) {
            return dodajWartosciWykresowSISO(parWizualizacja, obiekt, regulator, odpowiedzStrojenie,
                ustawSterowanieZaklocen(parWizualizacja, wizualizacjaZaklocen));
        } else {
            return dodajWartosciWykresowSISO(parWizualizacja, obiekt, regulator, odpowiedzStrojenie);
        }

    }

    private void ustawCelSISO(ParWizualizacja parWizualizacja, Regulator regulator, OdpowiedzStrojenie odpowiedzStrojenie) {
        regulator.setCel(new double[]{parWizualizacja.getYZad()[0]});
        double[] celTemp = new double[parWizualizacja.getDlugosc()];
        for (int i = 0; i < parWizualizacja.getDlugosc(); i++) {
            if (i < parWizualizacja.getSkok()[0])
                celTemp[i] = parWizualizacja.getYPP()[0];
            else
                celTemp[i] = parWizualizacja.getYZad()[0];

        }
        odpowiedzStrojenie.setCel(celTemp);
    }
    private double[][] ustawSterowanieZaklocen(ParWizualizacja parWizualizacja, WizualizacjaZaklocen wizualizacjaZaklocen) {
        double[][] dUZTemp = new double[parWizualizacja.getDlugosc()][wizualizacjaZaklocen.getUSkok().length];
        double[] uSkok = Arrays.stream(wizualizacjaZaklocen.getUSkok().clone()).mapToDouble(Double::doubleValue).toArray();
        double[] uPowrot = Arrays.stream(wizualizacjaZaklocen.getUSkok().clone()).mapToDouble(Double::doubleValue).toArray();
        for (int i = 0; i < wizualizacjaZaklocen.getUSkok().length; i++) {
            for (int j = 0; j < parWizualizacja.getDlugosc(); j++) {
                if (j < wizualizacjaZaklocen.getSkokZaklocenia()[i])
                    dUZTemp[j][i] = 0.0;
                else {
                    if(j < wizualizacjaZaklocen.getSkokPowrotnyZaklocenia()[i]) {
                        if(j>0) {
                            if(uSkok[i] != 0.0) {
                                if(uSkok[i] - wizualizacjaZaklocen.getDeltaU()[i] <0) {
                                    dUZTemp[j][i] = uSkok[i];
                                    uSkok[i] = 0.0;
                                } else {
                                    dUZTemp[j][i] = wizualizacjaZaklocen.getDeltaU()[i];
                                    uSkok[i] -= wizualizacjaZaklocen.getDeltaU()[i];
                                }
                            } else {
                                dUZTemp[j][i] = 0.0;
                            }
                        }  else {
                            dUZTemp[j][i] = wizualizacjaZaklocen.getDeltaU()[i];
                        }
                    } else {
                        if (j > 0) {
                            if (uPowrot[i] != 0.0) {
                                if (uPowrot[i] - wizualizacjaZaklocen.getDeltaU()[i] < 0) {
                                    dUZTemp[j][i] = -uPowrot[i];
                                    uPowrot[i] = 0.0;
                                } else {
                                    dUZTemp[j][i] = -wizualizacjaZaklocen.getDeltaU()[i];
                                    uPowrot[i] -= wizualizacjaZaklocen.getDeltaU()[i];
                                }
                            } else {
                                dUZTemp[j][i] = 0.0;
                            }
                        } else {
                            dUZTemp[j][i] = wizualizacjaZaklocen.getDeltaU()[i];
                        }
                    }
                }
            }
        }
        return dUZTemp;
    }

    private void dobierzStrojenieSISO(ParWizualizacja parWizualizacja, SISODPA obiekt, Regulator regulator, AlgorytmEwolucyjny GA, OdpowiedzStrojenie odpowiedzStrojenie) {
        double[] tempWartosciGA = GA.dobierzWartosci(regulator.liczbaZmiennych(), regulator, obiekt);
        double[] tempStrojenie = dodajStrojenie(parWizualizacja, tempWartosciGA);
        odpowiedzStrojenie.setWspolczynniki(tempStrojenie);
        regulator.zmienNastawy(tempStrojenie);
    }

    private SISODPA dobierzObiektSISO(ParObiekt parObiekt, ParRegulator parRegulator, ParWizualizacja parWizualizacja, Zaklocenia zaklocenie) {
        SISODPA obiekt;
        try{
            if(zaklocenie!=null) {
                return new SISODPA(parObiekt, parRegulator.getUMax(), parRegulator.getUMin(), parWizualizacja.getBlad(), zaklocenie);
            } else {
                return new SISODPA(parObiekt, parRegulator.getUMax(), parRegulator.getUMin(), parWizualizacja.getBlad());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private Regulator dobierzRegulatorSISO(ParObiekt parObiekt, ParRegulator parRegulator, ParWizualizacja parWizualizacja, SISODPA obiekt, Integer[] PIE) {
        Regulator regulator;
        if (parRegulator.getTyp().equals("pid")) {
            regulator = new PID(0.0, 0.0, 0.0, parObiekt.getTp(), new double[]{obiekt.getYMax()}, parRegulator.getDuMax(), parRegulator.getUMax(), parWizualizacja.getStrojenie());
            PIE[0] = 300;
            PIE[1] = 40;
            PIE[2] = 10;
        } else if (parRegulator.getTyp().equals("dmc")) {
            regulator = new DMCAnalityczny(4, 0.1, obiekt, obiekt.getYMax() / 2, parRegulator.getDuMax(), 11, parWizualizacja.getStrojenie());
            PIE[0] = 100;
            PIE[1] = 20;
            PIE[2] = 3;
        } else
        {
            return null;
        }
        return regulator;
    }

    private double[] dodajWartosciWykresowSISO(ParWizualizacja parWizualizacja, SISODPA obiekt, Regulator regulator, OdpowiedzStrojenie odpowiedzStrojenie) {
        double[] Y = new double[parWizualizacja.getDlugosc()];
        double[] U = new double[parWizualizacja.getDlugosc()];
        obiekt.resetObiektu();
        regulator.resetujRegulator();
        Y[0] = obiekt.getYpp();
        U[0] = parWizualizacja.getUPP()[0];
        for (int i = 0; i < parWizualizacja.getSkok()[0]; i++) {
            U[i] = parWizualizacja.getUPP()[0];
            Y[i] = obiekt.obliczKrok(parWizualizacja.getUPP()[0]);
        }
        for (int i = parWizualizacja.getSkok()[0]; i < parWizualizacja.getDlugosc(); i++) {
            {
                Y[i] = obiekt.obliczKrok(regulator.policzSterowanie(obiekt.getAktualna()));
                U[i] = obiekt.getU().get(0);
            }
        }
        odpowiedzStrojenie.setWykres(Y);
        odpowiedzStrojenie.setSterowanie(U);
        return Y;
    }

    private double[] dodajWartosciWykresowSISO(ParWizualizacja parWizualizacja, SISODPA obiekt, Regulator regulator,
                                               OdpowiedzStrojenie odpowiedzStrojenie, double[][] dUZ) {
        double[] Y = new double[parWizualizacja.getDlugosc()];
        double[] U = new double[parWizualizacja.getDlugosc()];
        obiekt.resetObiektu();
        regulator.resetujRegulator();
        Y[0] = obiekt.getYpp();
        U[0] = parWizualizacja.getUPP()[0];
        regulator.setCel(parWizualizacja.getYPP());
        for (int i = 0; i < parWizualizacja.getSkok()[0]; i++) {
            Y[i] = obiekt.obliczKrok(regulator.policzSterowanie(obiekt.getAktualna(), dUZ[i]), dUZ[i]);
            U[i] = obiekt.getU().get(0);
        }
        regulator.setCel(new double[]{parWizualizacja.getYZad()[0]});
        for (int i = parWizualizacja.getSkok()[0]; i < parWizualizacja.getDlugosc(); i++) {
            {
                Y[i] = obiekt.obliczKrok(regulator.policzSterowanie(obiekt.getAktualna(), dUZ[i]), dUZ[i]);
                U[i] = obiekt.getU().get(0);
            }
        }
        odpowiedzStrojenie.setWykres(Y);
        odpowiedzStrojenie.setSterowanie(U);
        return Y;
    }

    private double[] dodajStrojenie(ParWizualizacja parWizualizacja, double[] tempWartosciGA) {
        int iTemp = 0;
        double[] tempStrojenie = new double[parWizualizacja.getStrojenie().length];
        for (int i = 0; i < parWizualizacja.getStrojenie().length; i++) {
            if (parWizualizacja.getStrojenie()[i] == null) {
                tempStrojenie[i] = tempWartosciGA[iTemp];
                iTemp += 1;
            } else {
                tempStrojenie[i] = parWizualizacja.getStrojenie()[i];
            }
        }
        return tempStrojenie;
    }

    public OdpowiedzStrojenieMIMO MIMOStrojenie(MultipartFile[] file, ParRegulator parRegulator, ParWizualizacja parWizualizacja, WizualizacjaZaklocen wizualizacjaZaklocen) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root;
        MIMODPA obiekt;
        ParObiektMIMO[] obiekty;
        Integer[] PIE = new Integer[3];
        OdpowiedzStrojenieMIMO odpowiedz = new OdpowiedzStrojenieMIMO();
        try {
            root = objectMapper.readTree(file[0].getInputStream());
            obiekty = objectMapper.treeToValue(root.path("ParObiektMIMO"), ParObiektMIMO[].class);
            obiekt = new MIMODPA(obiekty, parWizualizacja.getBlad());
            if(file.length==3) {
                root = objectMapper.readTree(file[2].getInputStream());
                obiekty = objectMapper.treeToValue(root.path("ParObiektMIMO"), ParObiektMIMO[].class);
                MIMODPA zaklocenie = new MIMODPA(obiekty, parWizualizacja.getBlad());
                obiekt.setZakloceniaMierzalne(zaklocenie);
            }
            root = objectMapper.readTree(file[0].getInputStream());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        Regulator regulator = dobierzRegulatorMIMO(parRegulator, parWizualizacja, objectMapper, obiekt, root, PIE);
        if (regulator == null) return null;
        AlgorytmEwolucyjny GA = new AlgorytmEwolucyjny(PIE[0], PIE[1], PIE[2], 0.5, 0.5);
        dobierzStrojenieMIMO(parWizualizacja, obiekt, regulator, GA, odpowiedz);
        if(file.length>1)
        {
            try {
                root = objectMapper.readTree(file[1].getInputStream());
                obiekty = objectMapper.treeToValue(root.path("ParObiektMIMO"), ParObiektMIMO[].class);
                obiekt = new MIMODPA(obiekty, parWizualizacja.getBlad());
                if(file.length==3) {
                    root = objectMapper.readTree(file[2].getInputStream());
                    obiekty = objectMapper.treeToValue(root.path("ParObiektMIMO"), ParObiektMIMO[].class);
                    MIMODPA zaklocenie = new MIMODPA(obiekty, parWizualizacja.getBlad());
                    obiekt.setZakloceniaMierzalne(zaklocenie);
                }
                root = objectMapper.readTree(file[1].getInputStream());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        double[][] Y = symulacjaRegulacjiMIMO(parWizualizacja, obiekt, regulator, odpowiedz, wizualizacjaZaklocen);
        System.out.println("strojenie::OK");
        obliczBladMIMO(regulator, parWizualizacja.getDlugosc(), Y);
        return odpowiedz;
    }

    private double[][] symulacjaRegulacjiMIMO(ParWizualizacja parWizualizacja, MIMODPA obiekt,
              Regulator regulator, OdpowiedzStrojenieMIMO odpowiedz, WizualizacjaZaklocen wizualizacjaZaklocen) {
        double[][] celTemp = ustawCelMIMO(parWizualizacja, obiekt, parWizualizacja.getDlugosc());
        odpowiedz.setCel(celTemp);
        if(wizualizacjaZaklocen.getUSkok()!= null && wizualizacjaZaklocen.getUSkok().length!=0) {
            return dodajWartosciWykresowMIMO(obiekt, regulator, odpowiedz, parWizualizacja.getDlugosc(), celTemp,
                ustawSterowanieZaklocen(parWizualizacja, wizualizacjaZaklocen));
        } else {
            return  dodajWartosciWykresowMIMO(obiekt, regulator, odpowiedz, parWizualizacja.getDlugosc(), celTemp);
        }
    }

    private void dobierzStrojenieMIMO(ParWizualizacja parWizualizacja, MIMODPA obiekt, Regulator regulator, AlgorytmEwolucyjny GA, OdpowiedzStrojenieMIMO odpowiedz) {
        double[] tempWartosciGA = GA.dobierzWartosci(regulator.liczbaZmiennych(), regulator, obiekt);
        regulator.zmienNastawy(tempWartosciGA);
        double[] tempStrojenie = dodajStrojenie(parWizualizacja, tempWartosciGA);
        odpowiedz.setWspolczynniki(tempStrojenie);
    }

    private void obliczBladMIMO(Regulator regulator, int dlugoscSymulacji, double[][] Y) {
        double blad = 0.0;
        for (int i = 0; i < dlugoscSymulacji; i++) {
            blad += Math.pow(Y[0][i] - regulator.getCel()[0], 2);
        }
        blad = blad / Y[0].length;
        System.out.println(blad);
    }

    private Regulator dobierzRegulatorMIMO(ParRegulator parRegulator, ParWizualizacja parWizualizacja, ObjectMapper objectMapper, MIMODPA obiekt, JsonNode root, Integer[] PIE) {
        Regulator regulator;
        if (parRegulator.getTyp().equals("pid")) {
            Integer[] PV;
            try {

                PV = objectMapper.treeToValue(root.path("PV"), Integer[].class);
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
            regulator = new ZbiorPID(obiekt, PV, parRegulator.getDuMax(), parWizualizacja.getStrojenie());
            PIE[0] = 600;
            PIE[1] = 300;
            PIE[2] = 10;
        } else if (parRegulator.getTyp().equals("dmc")) {
            double[] tempLambda = new double[obiekt.getLiczbaIN()];
            for (int i = 0; i < obiekt.getLiczbaIN(); i++)
                tempLambda[i] = 0.5;
            regulator = new DMCAnalityczny(4, tempLambda, obiekt, obiekt.getYMax(), parRegulator.getDuMax(), 11, parWizualizacja.getStrojenie());
            PIE[0] = 50;
            PIE[1] = 20;
            PIE[2] = 1;
        } else {
            return null;
        }
        return regulator;
    }

    private double[][] dodajWartosciWykresowMIMO(MIMODPA obiekt, Regulator regulator, OdpowiedzStrojenieMIMO odpowiedz, int dlugoscSymulacji, double[][] celTemp) {
        obiekt.resetObiektu();
        regulator.resetujRegulator();
        double[][] Y = new double[obiekt.getLiczbaOUT()][dlugoscSymulacji];
        double[][] U = new double[obiekt.getLiczbaIN()][dlugoscSymulacji];
        for (int i = 0; i < dlugoscSymulacji; i++) {
            double[] temp = new double[obiekt.getLiczbaOUT()];
            //przy każdej iteracji jest pobierane yzad
            for (int m = 0; m < obiekt.getLiczbaOUT(); m++) {
                temp[m] = celTemp[m][i];
            }
            //ustawiane
            regulator.setCel(temp);
            double[] tempY = obiekt.obliczKrok(regulator.policzSterowanie(obiekt.getAktualne()));
            for (int j = 0; j < obiekt.getLiczbaOUT(); j++)
                Y[j][i] = tempY[j];
            for (int j = 0; j < obiekt.getLiczbaIN(); j++)
                U[j][i] = obiekt.getU().get(j).get(0);
        }
        odpowiedz.setSterowanie(U);
        odpowiedz.setWykres(Y);
        return Y;
    }

    private double[][] dodajWartosciWykresowMIMO(MIMODPA obiekt, Regulator regulator,
                                                 OdpowiedzStrojenieMIMO odpowiedz, int dlugoscSymulacji,
                                                 double[][] celTemp, double[][] dUZ) {
        obiekt.resetObiektu();
        regulator.resetujRegulator();
        double[][] Y = new double[obiekt.getLiczbaOUT()][dlugoscSymulacji];
        double[][] U = new double[obiekt.getLiczbaIN()][dlugoscSymulacji];
        for (int i = 0; i < dlugoscSymulacji; i++) {
            double[] temp = new double[obiekt.getLiczbaOUT()];
            //przy każdej iteracji jest pobierane yzad
            for (int m = 0; m < obiekt.getLiczbaOUT(); m++) {
                temp[m] = celTemp[m][i];
            }
            //ustawiane
            regulator.setCel(temp);
            double[] tempY = obiekt.obliczKrok(regulator.policzSterowanie(obiekt.getAktualne(), dUZ[i]), dUZ[i]);
            for (int j = 0; j < obiekt.getLiczbaOUT(); j++)
                Y[j][i] = tempY[j];
            for (int j = 0; j < obiekt.getLiczbaIN(); j++)
                U[j][i] = obiekt.getU().get(j).get(0);
        }
        odpowiedz.setSterowanie(U);
        odpowiedz.setWykres(Y);
        return Y;
    }
    private double[][] ustawCelMIMO(ParWizualizacja parWizualizacja, MIMODPA obiekt, int dlugoscSymulacji) {
        double[][] celTemp = new double[obiekt.getLiczbaOUT()][dlugoscSymulacji];
        for (int i = 0; i < obiekt.getLiczbaOUT(); i++) {
            for (int j = 0; j < dlugoscSymulacji; j++) {
                if (j < parWizualizacja.getSkok()[i])
                    celTemp[i][j] = parWizualizacja.getYPP()[i];
                else
                    celTemp[i][j] = parWizualizacja.getYZad()[i];
            }
        }
        return celTemp;
    }
}
