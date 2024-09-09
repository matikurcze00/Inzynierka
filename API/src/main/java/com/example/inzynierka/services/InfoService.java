package com.example.inzynierka.services;

import com.example.inzynierka.models.DisturbanceDiscrete;
import com.example.inzynierka.models.OdpowiedzInfoMIMO;
import com.example.inzynierka.models.ParObiektDPAMIMO;
import com.example.inzynierka.models.ParObiektRownaniaMIMO;
import com.example.inzynierka.objects.MIMODPA;
import com.example.inzynierka.objects.MIMODiscrete;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class InfoService {
    public OdpowiedzInfoMIMO InfoMIMODPA(MultipartFile file) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root;
        ParObiektDPAMIMO[] transmittances;
        MIMODPA object;
        try {
            root = objectMapper.readTree(file.getInputStream());
            transmittances = objectMapper.treeToValue(root.path("ParObiektDPAMIMO"), ParObiektDPAMIMO[].class);
            object = new MIMODPA(transmittances);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        OdpowiedzInfoMIMO odpowiedz = new OdpowiedzInfoMIMO();
        odpowiedz.setWejscia(object.getEntriesNumber());
        odpowiedz.setWyjscia(object.getOutputNumber());
        return odpowiedz;
    }

    public OdpowiedzInfoMIMO InfoMIMODiscrete(MultipartFile file) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root;
        ParObiektRownaniaMIMO[] transmittances;
        MIMODiscrete object;
        try {
            root = objectMapper.readTree(file.getInputStream());
            transmittances = objectMapper.treeToValue(root.path("ParObiektRownaniaMIMO"), ParObiektRownaniaMIMO[].class);
            object = new MIMODiscrete(transmittances);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        OdpowiedzInfoMIMO odpowiedz = new OdpowiedzInfoMIMO();
        odpowiedz.setWejscia(object.getEntriesNumber());
        odpowiedz.setWyjscia(object.getOutputNumber());
        return odpowiedz;
    }

    public OdpowiedzInfoMIMO InfoMIMODiscreteDisturbance(MultipartFile file) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root;
        DisturbanceDiscrete[] transmittances;
        try {
            root = objectMapper.readTree(file.getInputStream());
            transmittances = objectMapper.treeToValue(root.path("ZakloceniaRownania"), DisturbanceDiscrete[].class);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        OdpowiedzInfoMIMO odpowiedz = new OdpowiedzInfoMIMO();
        odpowiedz.setWejscia(transmittances.length);
        odpowiedz.setWyjscia(transmittances[0].getB1().length);
        return odpowiedz;
    }
}
