package com.example.inzynierka.controller;

import com.example.inzynierka.models.*;
import com.example.inzynierka.services.InfoService;
import com.example.inzynierka.services.StepResponseService;
import com.example.inzynierka.services.TuningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
public class Controller {

    private final TuningService tuningService;
    private final StepResponseService stepResponseService;
    private final InfoService infoService;

    @Autowired
    public Controller(TuningService tuningService, StepResponseService stepResponseService,
                      InfoService infoService) {
        this.tuningService = tuningService;
        this.stepResponseService = stepResponseService;
        this.infoService = infoService;
    }

    @PostMapping(value = "/strojenie/SISO")
    @ResponseBody
    public ResponseEntity<OdpowiedzStrojenie> tuningSISO(@RequestBody ParStrojenie parStrojenie) {
        System.out.println("strojenieSISO::start ");
        OdpowiedzStrojenie odpowiedz = tuningService.SISOTuning(parStrojenie);


        return ResponseEntity.ok(odpowiedz);


    }

    @PostMapping(value = "/info/MIMO/DPA", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<OdpowiedzInfoMIMO> infoMIMODPA(@RequestPart("file") MultipartFile file) {
        System.out.println("infoMIMO:: start ");
        OdpowiedzInfoMIMO odpowiedz = infoService.InfoMIMODPA(file);
        System.out.println("infoMIMO:: koniec");
        if (odpowiedz == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            return ResponseEntity.ok(odpowiedz);
        }
    }

    @PostMapping(value = "/info/MIMO/Rownania", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<OdpowiedzInfoMIMO> infoMIMODiscrete(@RequestPart("file") MultipartFile file) {
        OdpowiedzInfoMIMO odpowiedz = infoService.InfoMIMODiscrete(file);
        if (odpowiedz == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            return ResponseEntity.ok(odpowiedz);
        }
    }

    @PostMapping(value = "/info/MIMO/Rownania/Zaklocenia", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<OdpowiedzInfoMIMO> infoMIMODiscreteDisturbance(@RequestPart("file") MultipartFile file) {
        System.out.println("infoMIMO:: start ");
        OdpowiedzInfoMIMO odpowiedz = infoService.InfoMIMODiscreteDisturbance(file);
        System.out.println("infoMIMO:: koniec");
        if (odpowiedz == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            return ResponseEntity.ok(odpowiedz);
        }
    }

    @PostMapping(value = "/strojenie/MIMO", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<OdpowiedzStrojenieMIMO> tuningMIMO(@RequestPart("file") MultipartFile[] file, @ModelAttribute ParRegulator parRegulator,
                                                             @ModelAttribute ParWizualizacja parWizualizacja,
                                                             @ModelAttribute WizualizacjaZaklocen wizualizacjaZaklocen) {
        System.out.println("strojenieMIMO::start ");
        OdpowiedzStrojenieMIMO odpowiedz = tuningService.MIMOTuning(file, parRegulator, parWizualizacja, wizualizacjaZaklocen);
        if (odpowiedz == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            return ResponseEntity.ok(odpowiedz);
        }
    }

    @PostMapping(value = "/odpowiedz/SISO")
    @ResponseBody
    public ResponseEntity<OdpowiedzSkokowa> stepResponseSISO(@RequestBody ParStrojenie parStrojenie) {
        System.out.println("odpowiedzSISO::start ");
        OdpowiedzSkokowa odpowiedzSkokowa = stepResponseService.SISOOdpowiedz(parStrojenie);
        if (odpowiedzSkokowa == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            return ResponseEntity.ok(odpowiedzSkokowa);
        }
    }

    @PostMapping(value = "/odpowiedz/MIMO", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<OdpowiedzSkokowa> stepResponseMIMO(@RequestPart("file") MultipartFile file, @ModelAttribute ParRegulator parRegulator,
                                                             @ModelAttribute ParWizualizacja parWizualizacja) {
        System.out.println("odpowiedzMIMO::start ");
        OdpowiedzSkokowa odpowiedzSkokowa = stepResponseService.MIMOStepResponse(file, parRegulator, parWizualizacja);
        if (odpowiedzSkokowa == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            return ResponseEntity.ok(odpowiedzSkokowa);
        }
    }
}
