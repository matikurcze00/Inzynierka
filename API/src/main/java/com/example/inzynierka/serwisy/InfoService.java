package com.example.inzynierka.serwisy;

import com.example.inzynierka.modele.OdpowiedzInfoMIMO;
import com.example.inzynierka.modele.ParObiektDPAMIMO;
import com.example.inzynierka.obiekty.MIMODPA;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class InfoService {
    public OdpowiedzInfoMIMO InfoWejsciaWyjscia(MultipartFile file)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root;
        ParObiektDPAMIMO[] obiekty;
        MIMODPA obiekt;
        try {
            root = objectMapper.readTree(file.getInputStream());
            obiekty = objectMapper.treeToValue(root.path("ParObiektMIMO"), ParObiektDPAMIMO[].class);
            obiekt = new MIMODPA(obiekty);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        OdpowiedzInfoMIMO odpowiedz = new OdpowiedzInfoMIMO();
        odpowiedz.setWejscia(obiekt.getLiczbaIN());
        odpowiedz.setWyjscia(obiekt.getLiczbaOUT());
        return odpowiedz;
    }
}
