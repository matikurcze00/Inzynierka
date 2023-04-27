package com.example.inzynierka.serwisy;

import com.example.inzynierka.modele.*;
import com.example.inzynierka.obiekty.MIMODPA;
import com.example.inzynierka.obiekty.SISODPA;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OdpowiedzService {

    public OdpowiedzSkokowa SISOOdpowiedz(ParStrojenie parStrojenie) {
        ParObiektDPA parObiektDPA = parStrojenie.getParObiektDPA();
        ParRegulator parRegulator = parStrojenie.getParRegulator();
        ParWizualizacja parWizualizacja = parStrojenie.getParWizualizacja();
        SISODPA obiekt;
        try {
            obiekt = new SISODPA(parObiektDPA, parRegulator.getUMax(), parRegulator.getUMin(), parWizualizacja.getBlad());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        double U = 1;
        double[][] odpSkokowa = new double[1][parWizualizacja.getDlugosc()];
        symulacjaOdpowiedziSISO(parWizualizacja, obiekt, U, odpSkokowa);
        return new OdpowiedzSkokowa(odpSkokowa);
    }

    private void symulacjaOdpowiedziSISO(ParWizualizacja parWizualizacja, SISODPA obiekt, double U, double[][] odpSkokowa) {
        double Utemp = 0;
        odpSkokowa[0][0] = obiekt.obliczKrok(Utemp);
        odpSkokowa[0][1] = obiekt.obliczKrok(U);
        for (int i = 2; i < parWizualizacja.getDlugosc(); i++)
            odpSkokowa[0][i] = obiekt.obliczKrok(Utemp);
    }

    public OdpowiedzSkokowa MIMOOdpowiedz(MultipartFile file, ParRegulator parRegulator, ParWizualizacja parWizualizacja) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root;
        ParObiektDPAMIMO[] obiekty;
        MIMODPA obiekt;
        try {
            root = objectMapper.readTree(file.getInputStream());
            obiekty = objectMapper.treeToValue(root.path("ParObiektMIMO"), ParObiektDPAMIMO[].class);
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

    private void symulacjaOdpowiedziMIMO(ParWizualizacja parWizualizacja, MIMODPA obiekt, double U, double Utemp, double[][] odpSkokowa) {
        for (int k = 0; k < obiekt.getLiczbaIN(); k++) {
            for (int j = 0; j < obiekt.getLiczbaOUT(); j++) {
                obiekt.resetObiektu();
                odpSkokowa[k * obiekt.getLiczbaIN() + j][0] = obiekt.obliczKrok(Utemp, k, j);
                odpSkokowa[k * obiekt.getLiczbaIN() + j][1] = obiekt.obliczKrok(U, k, j);
                for (int i = 2; i < parWizualizacja.getDlugosc(); i++)
                    odpSkokowa[k * obiekt.getLiczbaIN() + j][i] = obiekt.obliczKrok(Utemp, k, j);
            }
        }
    }
}
