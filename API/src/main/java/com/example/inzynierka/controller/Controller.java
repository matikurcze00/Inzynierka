package com.example.inzynierka.controller;

import com.example.inzynierka.modele.*;
import com.example.inzynierka.serwisy.InfoService;
import com.example.inzynierka.serwisy.OdpowiedzService;
import com.example.inzynierka.serwisy.StrojenieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
public class Controller {

    private final StrojenieService strojenieService;
    private final OdpowiedzService odpowiedzService;
    private final InfoService infoService;

    @Autowired
    public Controller(StrojenieService strojenieService, OdpowiedzService odpowiedzService,
                      InfoService infoService) {
        this.strojenieService = strojenieService;
        this.odpowiedzService = odpowiedzService;
        this.infoService = infoService;
    }

    @RequestMapping(value = "/strojenie/SISO", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<OdpowiedzStrojenie> strojenieSISO(@RequestBody ParStrojenie parStrojenie) {
        System.out.println("strojenieSISO::start ");
        OdpowiedzStrojenie odpowiedz = strojenieService.SISOStrojenie(parStrojenie);

        if (odpowiedz == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            return ResponseEntity.ok(odpowiedz);
        }

    }
    @RequestMapping(value = "/info/MIMO/DPA", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<OdpowiedzInfoMIMO> infoMIMODPA(@RequestPart("file") MultipartFile file) {
        System.out.println("infoMIMO:: start ");
        OdpowiedzInfoMIMO odpowiedz = infoService.InfoWejsciaWyjsciaDPA(file);
        System.out.println("infoMIMO:: koniec");
        if(odpowiedz == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            return ResponseEntity.ok(odpowiedz);
        }
    }
    @RequestMapping(value = "/info/MIMO/Rownania", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<OdpowiedzInfoMIMO> infoMIMORownania(@RequestPart("file") MultipartFile file) {
        System.out.println("infoMIMO:: start ");
        OdpowiedzInfoMIMO odpowiedz = infoService.InfoWejsciaWyjsciaRownania(file);
        System.out.println("infoMIMO:: koniec");
        if(odpowiedz == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            return ResponseEntity.ok(odpowiedz);
        }
    }
    @RequestMapping(value = "/info/MIMO/Rownania/Zaklocenia", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<OdpowiedzInfoMIMO> infoMIMOZakloceniaRownania(@RequestPart("file") MultipartFile file) {
        System.out.println("infoMIMO:: start ");
        OdpowiedzInfoMIMO odpowiedz = infoService.InfoWejsciaWyjsciaZakloceniaRownania(file);
        System.out.println("infoMIMO:: koniec");
        if(odpowiedz == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            return ResponseEntity.ok(odpowiedz);
        }
    }
    @RequestMapping(value = "/strojenie/MIMO", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<OdpowiedzStrojenieMIMO> strojenieMIMO(@RequestPart("file") MultipartFile[] file, @ModelAttribute ParRegulator parRegulator, @ModelAttribute ParWizualizacja parWizualizacja, @ModelAttribute WizualizacjaZaklocen wizualizacjaZaklocen) {
        System.out.println("strojenieMIMO::start ");
        OdpowiedzStrojenieMIMO odpowiedz = strojenieService.MIMOStrojenie(file, parRegulator, parWizualizacja, wizualizacjaZaklocen);
        if (odpowiedz == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            return ResponseEntity.ok(odpowiedz);
        }
    }

    @RequestMapping(value = "/odpowiedz/SISO", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<OdpowiedzSkokowa> odpowiedzSISO(@RequestBody ParStrojenie parStrojenie) {
        System.out.println("odpowiedzSISO::start ");
        OdpowiedzSkokowa odpowiedzSkokowa = odpowiedzService.SISOOdpowiedz(parStrojenie);
        if (odpowiedzSkokowa == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            return ResponseEntity.ok(odpowiedzSkokowa);
        }
    }

    @RequestMapping(value = "/odpowiedz/MIMO", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<OdpowiedzSkokowa> odpowiedzMIMO(@RequestPart("file") MultipartFile file, @ModelAttribute ParRegulator parRegulator, @ModelAttribute ParWizualizacja parWizualizacja) {
        System.out.println("odpowiedzMIMO::start ");
        OdpowiedzSkokowa odpowiedzSkokowa = odpowiedzService.MIMOOdpowiedz(file, parRegulator, parWizualizacja);
        if (odpowiedzSkokowa == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            return ResponseEntity.ok(odpowiedzSkokowa);
        }
    }
}
