package com.example.inzynierka.serwisy;

import com.example.inzynierka.EA.AlgorytmEwolucyjny;
import com.example.inzynierka.modele.*;
import com.example.inzynierka.obiekty.*;
import com.example.inzynierka.regulatory.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

@Service
public class StrojenieService {

    private static void ustawSkokZaklocenia(WizualizacjaZaklocen wizualizacjaZaklocen, double[][] dUZTemp, double[] uSkok, int i, int j) {
        if (j > 0) {
            if (uSkok[i] != 0.0) {
                if (uSkok[i] - wizualizacjaZaklocen.getDeltaU()[i] < 0) {
                    dUZTemp[j][i] = uSkok[i];
                    uSkok[i] = 0.0;
                } else {
                    dUZTemp[j][i] = wizualizacjaZaklocen.getDeltaU()[i];
                    uSkok[i] -= wizualizacjaZaklocen.getDeltaU()[i];
                }
            } else {
                dUZTemp[j][i] = 0.0;
            }
        } else {
            dUZTemp[j][i] = Math.min(wizualizacjaZaklocen.getUSkok()[i], wizualizacjaZaklocen.getDeltaU()[i]);
            uSkok[i] -= Math.min(wizualizacjaZaklocen.getUSkok()[i], wizualizacjaZaklocen.getDeltaU()[i]);
        }
    }

    private static void ustawSkokPowrotnyZaklocenia(WizualizacjaZaklocen wizualizacjaZaklocen, double[][] dUZTemp, double[] uPowrot, int i, int j) {
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

    private static MIMO ustawObiekt(MultipartFile[] file, ParRegulator parRegulator, ParWizualizacja parWizualizacja, ObjectMapper objectMapper) {
        if (parRegulator.getTyp().equals("pid") || parRegulator.getTyp().equals("dmc")) {
            try {
                if (file.length == 3) {
                    JsonNode root = objectMapper.readTree(file[2].getInputStream());
                    ParObiektDPAMIMO[] obiekty = objectMapper.treeToValue(root.path("ParObiektDPAMIMO"), ParObiektDPAMIMO[].class);
                    MIMODPA zaklocenie = new MIMODPA(obiekty, parWizualizacja.getBlad());
                    root = objectMapper.readTree(file[0].getInputStream());
                    obiekty = objectMapper.treeToValue(root.path("ParObiektDPAMIMO"), ParObiektDPAMIMO[].class);
                    return new MIMODPA(obiekty, parWizualizacja.getBlad(), zaklocenie);
                } else {
                    JsonNode root = objectMapper.readTree(file[0].getInputStream());
                    ParObiektDPAMIMO[] obiekty = objectMapper.treeToValue(root.path("ParObiektDPAMIMO"), ParObiektDPAMIMO[].class);
                    return new MIMODPA(obiekty, parWizualizacja.getBlad());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        } else {
            try {
                if (file.length == 3) {
                    JsonNode root = objectMapper.readTree(file[2].getInputStream());
                    ZakloceniaRownania[] zakloceniaRownania = objectMapper.treeToValue(root.path("ZakloceniaRownania"), ZakloceniaRownania[].class);
                    root = objectMapper.readTree(file[0].getInputStream());
                    ParObiektRownaniaMIMO[] obiekty = objectMapper.treeToValue(root.path("ParObiektRownaniaMIMO"), ParObiektRownaniaMIMO[].class);
                    return new MIMORownianiaRoznicowe(obiekty, parWizualizacja.getBlad(), zakloceniaRownania);
                } else {
                    JsonNode root = objectMapper.readTree(file[0].getInputStream());
                    ParObiektRownaniaMIMO[] obiekty = objectMapper.treeToValue(root.path("ParObiektRownaniaMIMO"), ParObiektRownaniaMIMO[].class);
                    return new MIMORownianiaRoznicowe(obiekty, parWizualizacja.getBlad());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }

    private static MIMO ustawObiektSymulacji(MultipartFile[] file, ParRegulator parRegulator, ParWizualizacja parWizualizacja, ObjectMapper objectMapper,
                                             MIMO obiekt) {
        if (file.length > 1) {
            if (parRegulator.getTyp().equals("pid") || parRegulator.getTyp().equals("dmc")) {
                obiekt = ustawObiektSymulacjiDPA(file, parWizualizacja, objectMapper);
            } else {
                obiekt = ustawObiektSymulacjiRownania(file, parWizualizacja, objectMapper);
            }
        }
        return obiekt;
    }

    private static MIMO ustawObiektSymulacjiDPA(MultipartFile[] file, ParWizualizacja parWizualizacja, ObjectMapper objectMapper) {
        MIMO obiekt;
        JsonNode root;
        try {

            if (file.length == 3) {
                root = objectMapper.readTree(file[2].getInputStream());
                ParObiektDPAMIMO[] obiekty = objectMapper.treeToValue(root.path("ParObiektDPAMIMO"), ParObiektDPAMIMO[].class);
                MIMODPA zaklocenie = new MIMODPA(obiekty, parWizualizacja.getBlad());
                root = objectMapper.readTree(file[1].getInputStream());
                obiekty = objectMapper.treeToValue(root.path("ParObiektDPAMIMO"), ParObiektDPAMIMO[].class);
                obiekt = new MIMODPA(obiekty, parWizualizacja.getBlad(), zaklocenie);
            } else {
                root = objectMapper.readTree(file[1].getInputStream());
                ParObiektDPAMIMO[] obiekty = objectMapper.treeToValue(root.path("ParObiektDPAMIMO"), ParObiektDPAMIMO[].class);
                obiekt = new MIMODPA(obiekty, parWizualizacja.getBlad());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        return obiekt;
    }

    private static MIMO ustawObiektSymulacjiRownania(MultipartFile[] file, ParWizualizacja parWizualizacja, ObjectMapper objectMapper) {
        JsonNode root;
        MIMO obiekt;
        try {
            if (file.length == 3) {
                root = objectMapper.readTree(file[2].getInputStream());
                ZakloceniaRownania[] zakloceniaRownania = objectMapper.treeToValue(root.path("ZakloceniaRownania"), ZakloceniaRownania[].class);
                root = objectMapper.readTree(file[1].getInputStream());
                ParObiektRownaniaMIMO[] obiekty = objectMapper.treeToValue(root.path("ParObiektRownaniaMIMO"), ParObiektRownaniaMIMO[].class);
                obiekt = new MIMORownianiaRoznicowe(obiekty, parWizualizacja.getBlad(), zakloceniaRownania);
            } else {
                root = objectMapper.readTree(file[1].getInputStream());
                ParObiektRownaniaMIMO[] obiekty = objectMapper.treeToValue(root.path("ParObiektRownaniaMIMO"), ParObiektRownaniaMIMO[].class);
                obiekt = new MIMORownianiaRoznicowe(obiekty, parWizualizacja.getBlad());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        return obiekt;
    }

    public OdpowiedzStrojenie SISOStrojenie(ParStrojenie parStrojenie) {
        ParObiektDPA parObiektDPA = parStrojenie.getParObiektDPA();
        ParObiektRownania parObiektRownania = parStrojenie.getParObiektRownania();
        ParRegulator parRegulator = parStrojenie.getParRegulator();
        ParWizualizacja parWizualizacja = parStrojenie.getParWizualizacja();
        ZakloceniaDPA zakloceniaDPA = new ZakloceniaDPA();
        ZakloceniaRownania zakloceniaRownania = new ZakloceniaRownania();
        if (parStrojenie.getZakloceniaDPA().getGain() != null && parStrojenie.getZakloceniaDPA().getGain().length != 0) {
            zakloceniaDPA = parStrojenie.getZakloceniaDPA();
        } else {
            zakloceniaDPA = null;
        }
        if (parStrojenie.getZakloceniaRownania().getB1() != null && parStrojenie.getZakloceniaRownania().getB1().length != 0) {
            zakloceniaRownania = parStrojenie.getZakloceniaRownania();
        } else {
            zakloceniaRownania = null;
        }

        Integer[] wartosciEA = new Integer[3];
        OdpowiedzStrojenie odpowiedzStrojenie = new OdpowiedzStrojenie();

        SISO obiekt = dobierzObiektSISO(parObiektDPA, parObiektRownania, parRegulator, parWizualizacja, zakloceniaDPA, zakloceniaRownania);
        if (obiekt == null) {
            return null;
        }
        Regulator regulator;
        regulator = dobierzRegulatorSISO(parObiektDPA, parRegulator, parWizualizacja, obiekt, wartosciEA);
        if (regulator == null) {
            return null;
        }
        if (regulator.liczbaZmiennych() != 0) {
            AlgorytmEwolucyjny GA = new AlgorytmEwolucyjny(wartosciEA[0], wartosciEA[1], wartosciEA[2], 0.3, 0.2);
            dobierzStrojenieSISO(parWizualizacja, obiekt, regulator, GA, odpowiedzStrojenie);
        } else {
            double[] primitiveArray = new double[parWizualizacja.getStrojenie().length];
            for (int i = 0; i < parWizualizacja.getStrojenie().length; i++) {
                primitiveArray[i] = parWizualizacja.getStrojenie()[i];
            }
            odpowiedzStrojenie.setWspolczynniki(primitiveArray);
        }
        if (parStrojenie.getParObiektSymulacjiDPA() != null) {
            if (zakloceniaDPA != null) {
                obiekt = new SISODPA(parStrojenie.getParObiektSymulacjiDPA(), parRegulator.getUMax(), parRegulator.getUMin(), parWizualizacja.getBlad(),
                    zakloceniaDPA);
            } else {
                obiekt = new SISODPA(parStrojenie.getParObiektSymulacjiDPA(), parRegulator.getUMax(), parRegulator.getUMin(), parWizualizacja.getBlad());
            }
        }
        double[] Y = symulacjaRegulacjiSISO(parWizualizacja, obiekt, regulator, odpowiedzStrojenie, parStrojenie.getWizualizacjaZaklocen());
        System.out.println("strojenie::OK");
        obliczBladSISO(parWizualizacja, regulator, Y);
        return odpowiedzStrojenie;
    }

    private void obliczBladSISO(ParWizualizacja parWizualizacja, Regulator regulator, double[] Y) {
        double blad = 0.0;
        for (int i = 0; i < parWizualizacja.getSkok()[0]; i++) {
            blad += Math.pow(Y[i] - parWizualizacja.getYPP()[0], 2);
        }
        for (int i = parWizualizacja.getSkok()[0]; i < parWizualizacja.getDlugosc(); i++) {
            blad += Math.pow(Y[i] - regulator.getCel()[0], 2);
        }
        blad = blad / parWizualizacja.getDlugosc();
        System.out.println("BLAD:" + blad);
    }

    private double[] symulacjaRegulacjiSISO(ParWizualizacja parWizualizacja, SISO obiekt, Regulator regulator,
                                            OdpowiedzStrojenie odpowiedzStrojenie, WizualizacjaZaklocen wizualizacjaZaklocen) {
        ustawCelSISO(parWizualizacja, regulator, odpowiedzStrojenie);
        if (wizualizacjaZaklocen.getUSkok() != null && wizualizacjaZaklocen.getUSkok().length != 0) {
            return dodajWartosciWykresowSISO(parWizualizacja, obiekt, regulator, odpowiedzStrojenie,
                ustawSterowanieZaklocen(parWizualizacja, wizualizacjaZaklocen));
        } else {
            return dodajWartosciWykresowSISO(parWizualizacja, obiekt, regulator, odpowiedzStrojenie);
        }

    }

    private void ustawCelSISO(ParWizualizacja parWizualizacja, Regulator regulator, OdpowiedzStrojenie odpowiedzStrojenie) {
        regulator.setCel(new double[] {parWizualizacja.getYZad()[0]});
        double[] celTemp = new double[parWizualizacja.getDlugosc()];
        for (int i = 0; i < parWizualizacja.getDlugosc(); i++) {
            if (i < parWizualizacja.getSkok()[0]) {
                celTemp[i] = parWizualizacja.getYPP()[0];
            } else {
                celTemp[i] = parWizualizacja.getYZad()[0];
            }

        }
        odpowiedzStrojenie.setCel(celTemp);
    }

    private double[][] ustawSterowanieZaklocen(ParWizualizacja parWizualizacja, WizualizacjaZaklocen wizualizacjaZaklocen) {
        double[][] dUZTemp = new double[parWizualizacja.getDlugosc()][wizualizacjaZaklocen.getUSkok().length];
        double[] uSkok = Arrays.stream(wizualizacjaZaklocen.getUSkok().clone()).mapToDouble(Double::doubleValue).toArray();
        double[] uPowrot = Arrays.stream(wizualizacjaZaklocen.getUSkok().clone()).mapToDouble(Double::doubleValue).toArray();
        for (int i = 0; i < wizualizacjaZaklocen.getUSkok().length; i++) {
            for (int j = 0; j < parWizualizacja.getDlugosc(); j++) {
                if (j < wizualizacjaZaklocen.getSkokZaklocenia()[i]) {
                    dUZTemp[j][i] = 0.0;
                } else {
                    if (j < wizualizacjaZaklocen.getSkokPowrotnyZaklocenia()[i]) {
                        ustawSkokZaklocenia(wizualizacjaZaklocen, dUZTemp, uSkok, i, j);
                    } else {
                        ustawSkokPowrotnyZaklocenia(wizualizacjaZaklocen, dUZTemp, uPowrot, i, j);
                    }
                }
            }
        }
        return dUZTemp;
    }

    private void dobierzStrojenieSISO(ParWizualizacja parWizualizacja, SISO obiekt, Regulator regulator, AlgorytmEwolucyjny GA,
                                      OdpowiedzStrojenie odpowiedzStrojenie) {
        double[] tempWartosciGA = GA.dobierzWartosci(regulator.liczbaZmiennych(), regulator, obiekt);
        double[] tempStrojenie = dodajStrojenie(parWizualizacja, tempWartosciGA);
        odpowiedzStrojenie.setWspolczynniki(tempStrojenie);
        regulator.zmienNastawy(tempStrojenie);
    }

    private SISO dobierzObiektSISO(ParObiektDPA parObiektDPA, ParObiektRownania parObiektRownania, ParRegulator parRegulator,
                                   ParWizualizacja parWizualizacja, ZakloceniaDPA zakloceniaDPA, ZakloceniaRownania zakloceniaRownania) {
        try {
            if (!parRegulator.getTyp().equals("gpc")) {

                if (zakloceniaDPA != null) {
                    return new SISODPA(parObiektDPA, parRegulator.getUMax(), parRegulator.getUMin(), parWizualizacja.getBlad(), zakloceniaDPA);
                } else {
                    return new SISODPA(parObiektDPA, parRegulator.getUMax(), parRegulator.getUMin(), parWizualizacja.getBlad());
                }
            } else {
                if (zakloceniaRownania != null) {
                    return new SISORownianiaRoznicowe(parObiektRownania, parRegulator.getUMax(), parRegulator.getUMin(), parWizualizacja.getBlad(),
                        zakloceniaRownania);
                } else {
                    return new SISORownianiaRoznicowe(parObiektRownania, parRegulator.getUMax(), parRegulator.getUMin(), parWizualizacja.getBlad());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private Regulator dobierzRegulatorSISO(ParObiektDPA parObiektDPA, ParRegulator parRegulator, ParWizualizacja parWizualizacja, SISO obiekt,
                                           Integer[] wartosciEA) {
        Regulator regulator;
        if (parRegulator.getTyp().equals("pid")) {
            regulator = new PID(0.0, 0.0, 0.0, parObiektDPA.getTp(), new double[] {obiekt.getYMax()}, parRegulator.getDuMax(), parRegulator.getUMax(),
                parWizualizacja.getStrojenie());
            wartosciEA[0] = 200;
            wartosciEA[1] = 200;
            wartosciEA[2] = 500;
        } else if (parRegulator.getTyp().equals("dmc")) {
            regulator = new DMCAnalityczny(4, 0.1, (SISODPA) obiekt, obiekt.getYMax() / 2, parRegulator.getDuMax(), 11, parWizualizacja.getStrojenie());
            wartosciEA[0] = 50;
            wartosciEA[1] = 20;
            wartosciEA[2] = 250;
        } else if (parRegulator.getTyp().equals("gpc")) {
            regulator = new GPC((SISORownianiaRoznicowe) obiekt, 0.1, obiekt.getYMax() / 2, parRegulator.getDuMax(), parWizualizacja.getStrojenie());
            wartosciEA[0] = 50;
            wartosciEA[1] = 20;
            wartosciEA[2] = 250;
        } else {
            return null;
        }
        return regulator;
    }

    private double[] dodajWartosciWykresowSISO(ParWizualizacja parWizualizacja, SISO obiekt, Regulator regulator,
                                               OdpowiedzStrojenie odpowiedzStrojenie) {
        double[] Y = new double[parWizualizacja.getDlugosc()];
        double[] U = new double[parWizualizacja.getDlugosc()];
        obiekt.resetObiektu();
        regulator.resetujRegulator();
        Y[0] = obiekt.getYpp();
        U[0] = parWizualizacja.getUPP()[0];
        for (int i = 1; i < parWizualizacja.getSkok()[0]; i++) {
            U[i] = parWizualizacja.getUPP()[0];
            Y[i] = obiekt.obliczKrok(parWizualizacja.getUPP()[0]);
        }
        for (int i = Math.max(1, parWizualizacja.getSkok()[0]); i < parWizualizacja.getDlugosc(); i++) {
            {
                Y[i] = obiekt.obliczKrok(regulator.policzSterowanie(obiekt.getAktualna()));
                U[i] = obiekt.getU().get(0);
            }
        }
        odpowiedzStrojenie.setWykres(Y);
        odpowiedzStrojenie.setSterowanie(U);
        return Y;
    }

    private double[] dodajWartosciWykresowSISO(ParWizualizacja parWizualizacja, SISO obiekt, Regulator regulator,
                                               OdpowiedzStrojenie odpowiedzStrojenie, double[][] dUZ) {
        double[] Y = new double[parWizualizacja.getDlugosc()];
        double[] U = new double[parWizualizacja.getDlugosc()];
        double[][] Uz = new double[dUZ[0].length][parWizualizacja.getDlugosc()];
        obiekt.resetObiektu();
        regulator.resetujRegulator();
        Y[0] = obiekt.getYpp();
        U[0] = parWizualizacja.getUPP()[0];

        regulator.setCel(parWizualizacja.getYPP());
        for (int i = 0; i < parWizualizacja.getSkok()[0]; i++) {
            Y[i] = obiekt.obliczKrok(regulator.policzSterowanie(obiekt.getAktualna(), dUZ[i]), dUZ[i]);
            U[i] = obiekt.getU().get(0);
        }
        regulator.setCel(new double[] {parWizualizacja.getYZad()[0]});
        for (int i = parWizualizacja.getSkok()[0]; i < parWizualizacja.getDlugosc(); i++) {
            {
                Y[i] = obiekt.obliczKrok(regulator.policzSterowanie(obiekt.getAktualna(), dUZ[i]), dUZ[i]);
                U[i] = obiekt.getU().get(0);
            }
        }
        for (int j = 0; j < dUZ[0].length; j++) {
            Uz[j][0] = dUZ[0][j];
        }

        for (int i = 1; i < parWizualizacja.getDlugosc(); i++) {
            for (int j = 0; j < dUZ[0].length; j++) {
                Uz[j][i] = Uz[j][i - 1] + dUZ[i][j];
            }
        }
        odpowiedzStrojenie.setWykres(Y);
        odpowiedzStrojenie.setSterowanie(U);
        odpowiedzStrojenie.setSterowanieZaklocenia(Uz);
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

    public OdpowiedzStrojenieMIMO MIMOStrojenie(MultipartFile[] file, ParRegulator parRegulator, ParWizualizacja parWizualizacja,
                                                WizualizacjaZaklocen wizualizacjaZaklocen) {
        ObjectMapper objectMapper = new ObjectMapper();
        MIMO obiekt;

        Integer[] wartosciEA = new Integer[3];
        OdpowiedzStrojenieMIMO odpowiedz = new OdpowiedzStrojenieMIMO();
        obiekt = ustawObiekt(file, parRegulator, parWizualizacja, objectMapper);
        if (obiekt == null) {
            return null;
        }
        Regulator regulator = dobierzRegulatorMIMO(parRegulator, parWizualizacja, objectMapper, obiekt, wartosciEA, file);
        if (regulator == null) {
            return null;
        }
        if (regulator.liczbaZmiennych() != 0) {
            AlgorytmEwolucyjny GA = new AlgorytmEwolucyjny(wartosciEA[0], wartosciEA[1], wartosciEA[2], 0.5, 0.5);
            dobierzStrojenieMIMO(parWizualizacja, obiekt, regulator, GA, odpowiedz);
        } else {
            double[] tempWartosci = new double[parWizualizacja.getStrojenie().length];
            for (int i = 0; i < parWizualizacja.getStrojenie().length; i++) {
                tempWartosci[i] = parWizualizacja.getStrojenie()[i];
            }
            odpowiedz.setWspolczynniki(tempWartosci);
        }
        obiekt = ustawObiektSymulacji(file, parRegulator, parWizualizacja, objectMapper, obiekt);
        if (obiekt == null) {
            return null;
        }
        double[][] Y = symulacjaRegulacjiMIMO(parWizualizacja, obiekt, regulator, odpowiedz, wizualizacjaZaklocen);
        System.out.println("strojenie::OK");
        obliczBladMIMO(regulator, parWizualizacja.getDlugosc(), Y);
        return odpowiedz;
    }

    private double[][] symulacjaRegulacjiMIMO(ParWizualizacja parWizualizacja, MIMO obiekt,
                                              Regulator regulator, OdpowiedzStrojenieMIMO odpowiedz, WizualizacjaZaklocen wizualizacjaZaklocen) {
        double[][] celTemp = ustawCelMIMO(parWizualizacja, obiekt, parWizualizacja.getDlugosc());
        odpowiedz.setCel(celTemp);
        if (wizualizacjaZaklocen.getUSkok() != null && wizualizacjaZaklocen.getUSkok().length != 0) {
            return dodajWartosciWykresowMIMO(obiekt, regulator, odpowiedz, parWizualizacja.getDlugosc(), celTemp,
                ustawSterowanieZaklocen(parWizualizacja, wizualizacjaZaklocen));
        } else {
            return dodajWartosciWykresowMIMO(obiekt, regulator, odpowiedz, parWizualizacja.getDlugosc(), celTemp);
        }
    }

    private void dobierzStrojenieMIMO(ParWizualizacja parWizualizacja, MIMO obiekt, Regulator regulator, AlgorytmEwolucyjny GA,
                                      OdpowiedzStrojenieMIMO odpowiedz) {
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

    private Regulator dobierzRegulatorMIMO(ParRegulator parRegulator, ParWizualizacja parWizualizacja,
                                           ObjectMapper objectMapper, MIMO obiekt, Integer[] wartosciEA, MultipartFile[] file) {
        Regulator regulator;
        if (parRegulator.getTyp().equals("pid")) {
            Integer[] PV;
            try {
                JsonNode root = objectMapper.readTree(file[0].getInputStream());
                PV = objectMapper.treeToValue(root.path("PV"), Integer[].class);
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
            regulator = new ZbiorPID((MIMODPA) obiekt, PV, parRegulator.getDuMax(), parWizualizacja.getStrojenie());
            wartosciEA[0] = 100;
            wartosciEA[1] = 50;
            wartosciEA[2] = 500;
        } else if (parRegulator.getTyp().equals("dmc")) {
            double[] tempLambda = new double[obiekt.getLiczbaIN()];
            for (int i = 0; i < obiekt.getLiczbaIN(); i++) {
                tempLambda[i] = 0.5;
            }
            regulator = new DMCAnalityczny(4, tempLambda, (MIMODPA) obiekt, obiekt.getYMax(), parRegulator.getDuMax(), 11, parWizualizacja.getStrojenie());
            wartosciEA[0] = 15;
            wartosciEA[1] = 25;
            wartosciEA[2] = 75;
        } else if (parRegulator.getTyp().equals("gpc")) {
            double[] tempLambda = new double[obiekt.getLiczbaIN()];
            for (int i = 0; i < obiekt.getLiczbaIN(); i++) {
                tempLambda[i] = 0.5;
            }
            regulator = new GPC((MIMORownianiaRoznicowe) obiekt, 5, obiekt.getYMax(), parRegulator.getDuMax(), parWizualizacja.getStrojenie(), tempLambda);
            wartosciEA[0] = 15;
            wartosciEA[1] = 25;
            wartosciEA[2] = 75;
        } else {
            throw new RuntimeException();
        }
        return regulator;
    }

    private double[][] dodajWartosciWykresowMIMO(MIMO obiekt, Regulator regulator, OdpowiedzStrojenieMIMO odpowiedz, int dlugoscSymulacji, double[][] celTemp) {
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
            for (int j = 0; j < obiekt.getLiczbaOUT(); j++) {
                Y[j][i] = tempY[j];
            }
            for (int j = 0; j < obiekt.getLiczbaIN(); j++) {
                U[j][i] = obiekt.getU().get(j).get(0);
            }
        }
        odpowiedz.setSterowanie(U);
        odpowiedz.setWykres(Y);
        return Y;
    }

    private double[][] dodajWartosciWykresowMIMO(MIMO obiekt, Regulator regulator,
                                                 OdpowiedzStrojenieMIMO odpowiedz, int dlugoscSymulacji,
                                                 double[][] celTemp, double[][] dUZ) {
        obiekt.resetObiektu();
        regulator.resetujRegulator();
        double[][] Y = new double[obiekt.getLiczbaOUT()][dlugoscSymulacji];
        double[][] U = new double[obiekt.getLiczbaIN()][dlugoscSymulacji];
        double[][] Uz = new double[dUZ[0].length][dlugoscSymulacji];

        for (int i = 0; i < dlugoscSymulacji; i++) {
            double[] temp = new double[obiekt.getLiczbaOUT()];
            //przy każdej iteracji jest pobierane yzad
            for (int m = 0; m < obiekt.getLiczbaOUT(); m++) {
                temp[m] = celTemp[m][i];
            }
            //ustawiane
            regulator.setCel(temp);
            double[] tempY = obiekt.obliczKrok(regulator.policzSterowanie(obiekt.getAktualne(), dUZ[i]), dUZ[i]);
            for (int j = 0; j < obiekt.getLiczbaOUT(); j++) {
                Y[j][i] = tempY[j];
            }
            for (int j = 0; j < obiekt.getLiczbaIN(); j++) {
                U[j][i] = obiekt.getU().get(j).get(0);
            }
        }
        for (int j = 0; j < dUZ[0].length; j++) {
            Uz[j][0] = dUZ[0][j];
        }

        for (int i = 1; i < dlugoscSymulacji; i++) {
            for (int j = 0; j < dUZ[0].length; j++) {
                Uz[j][i] = Uz[j][i - 1] + dUZ[i][j];
            }
        }
        odpowiedz.setSterowanie(U);
        odpowiedz.setWykres(Y);
        odpowiedz.setSterowanieZaklocenia(Uz);
        return Y;
    }

    private double[][] ustawCelMIMO(ParWizualizacja parWizualizacja, MIMO obiekt, int dlugoscSymulacji) {
        double[][] celTemp = new double[obiekt.getLiczbaOUT()][dlugoscSymulacji];
        for (int i = 0; i < obiekt.getLiczbaOUT(); i++) {
            for (int j = 0; j < dlugoscSymulacji; j++) {
                if (j < parWizualizacja.getSkok()[i]) {
                    celTemp[i][j] = parWizualizacja.getYPP()[i];
                } else {
                    celTemp[i][j] = parWizualizacja.getYZad()[i];
                }
            }
        }
        return celTemp;
    }
}
