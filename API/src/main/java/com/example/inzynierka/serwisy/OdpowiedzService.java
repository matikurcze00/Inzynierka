package com.example.inzynierka.serwisy;

import com.example.inzynierka.modele.*;
import com.example.inzynierka.obiekty.*;
import com.example.inzynierka.regulatory.Regulator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OdpowiedzService {

    public OdpowiedzSkokowa SISOOdpowiedz(ParStrojenie parStrojenie) {
        ParRegulator parRegulator = parStrojenie.getParRegulator();
        ParWizualizacja parWizualizacja = parStrojenie.getParWizualizacja();
        if(parRegulator.getTyp().equals("pid") || parRegulator.getTyp().equals("dmc")) {

            return wyznaczOdpowiedzSISODPA(parStrojenie, parRegulator, parWizualizacja);
        } else {
            return wyznaczOdpowiedzSISORownania(parStrojenie, parRegulator, parWizualizacja);
        }
    }

    private OdpowiedzSkokowa wyznaczOdpowiedzSISORownania(ParStrojenie parStrojenie, ParRegulator parRegulator, ParWizualizacja parWizualizacja) {
        ParObiektRownania parObiektRownania = parStrojenie.getParObiektRownania();
        SISORownianiaRoznicowe obiekt;
        try {
            obiekt = new SISORownianiaRoznicowe(parObiektRownania, parRegulator.getUMax(), parRegulator.getUMin(), parWizualizacja.getBlad());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        double[][] odpSkokowa = new double[1][parWizualizacja.getDlugosc()];
        symulacjaOdpowiedziSISO(parWizualizacja, obiekt, odpSkokowa);
        return new OdpowiedzSkokowa(odpSkokowa);
    }

    private OdpowiedzSkokowa wyznaczOdpowiedzSISODPA(ParStrojenie parStrojenie, ParRegulator parRegulator, ParWizualizacja parWizualizacja) {
        ParObiektDPA parObiektDPA = parStrojenie.getParObiektDPA();
        SISODPA obiekt;
        try {
            obiekt = new SISODPA(parObiektDPA, parRegulator.getUMax(), parRegulator.getUMin(), parWizualizacja.getBlad());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        double[][] odpSkokowa = new double[1][parWizualizacja.getDlugosc()];
        symulacjaOdpowiedziSISO(parWizualizacja, obiekt, odpSkokowa);
        return new OdpowiedzSkokowa(odpSkokowa);
    }

    private void symulacjaOdpowiedziSISO(ParWizualizacja parWizualizacja, SISO obiekt, double[][] odpSkokowa) {
        double USkoku = 1;
        double UZero = 0;
        odpSkokowa[0][0] = obiekt.obliczKrok(UZero);
        odpSkokowa[0][1] = obiekt.obliczKrok(USkoku);
        for (int i = 2; i < parWizualizacja.getDlugosc(); i++) {
            odpSkokowa[0][i] = obiekt.obliczKrok(UZero);
        }
    }

    public OdpowiedzSkokowa MIMOOdpowiedz(MultipartFile file, ParRegulator parRegulator, ParWizualizacja parWizualizacja) {
        ObjectMapper objectMapper = new ObjectMapper();
        if(parRegulator.getTyp().equals("pid") || parRegulator.getTyp().equals("dmc")) {
            return wyznaczOdpowiedzMIMODPA(file, parWizualizacja, objectMapper);
        } else {
            return wyznaczOdpowiedzMIMORowniania(file, parWizualizacja, objectMapper);
        }
    }

    private OdpowiedzSkokowa wyznaczOdpowiedzMIMODPA(MultipartFile file, ParWizualizacja parWizualizacja, ObjectMapper objectMapper) {
        JsonNode root;
        ParObiektDPAMIMO[] obiekty;
        MIMODPA obiekt;
        try {
            root = objectMapper.readTree(file.getInputStream());
            obiekty = objectMapper.treeToValue(root.path("ParObiektDPAMIMO"), ParObiektDPAMIMO[].class);
            obiekt = new MIMODPA(obiekty, parWizualizacja.getBlad());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        double U = 1;
        double Utemp = 0;
        double[][] odpSkokowa = new double[obiekt.getLiczbaIN() * obiekt.getLiczbaOUT()][parWizualizacja.getDlugosc()];
        symulacjaOdpowiedziMIMO(parWizualizacja, obiekt, U, Utemp, odpSkokowa);
        return new OdpowiedzSkokowa(odpSkokowa);
    }
    private OdpowiedzSkokowa wyznaczOdpowiedzMIMORowniania(MultipartFile file, ParWizualizacja parWizualizacja, ObjectMapper objectMapper) {
        JsonNode root;
        ParObiektRownaniaMIMO[] obiekty;
        MIMORownianiaRoznicowe obiekt;
        try {
            root = objectMapper.readTree(file.getInputStream());
            obiekty = objectMapper.treeToValue(root.path("ParObiektRownaniaMIMO"), ParObiektRownaniaMIMO[].class);
            obiekt = new MIMORownianiaRoznicowe(obiekty, parWizualizacja.getBlad());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        double U = 1;
        double Utemp = 0;
        double[][] odpSkokowa = new double[obiekt.getLiczbaIN() * obiekt.getLiczbaOUT()][parWizualizacja.getDlugosc()];
        symulacjaOdpowiedziMIMO(parWizualizacja, obiekt, U, Utemp, odpSkokowa);
        return new OdpowiedzSkokowa(odpSkokowa);
    }

    private void symulacjaOdpowiedziMIMO(ParWizualizacja parWizualizacja, MIMO obiekt, double U, double Utemp, double[][] odpSkokowa) {
        for (int k = 0; k < obiekt.getLiczbaIN(); k++) {
            for (int j = 0; j < obiekt.getLiczbaOUT(); j++) {
                obiekt.resetObiektu();
                odpSkokowa[k * obiekt.getLiczbaIN() + j][0] = obiekt.obliczKrok(Utemp, k, j);
                odpSkokowa[k * obiekt.getLiczbaIN() + j][1] = obiekt.obliczKrok(U, k, j);
                for (int i = 2; i < parWizualizacja.getDlugosc(); i++) {
                    odpSkokowa[k * obiekt.getLiczbaIN() + j][i] = obiekt.obliczKrok(Utemp, k, j);
                }
            }
        }
    }

}
