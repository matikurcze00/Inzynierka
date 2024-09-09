package com.example.inzynierka.models;

import lombok.Data;

@Data
public class ParStrojenie {


    private ParRegulator parRegulator;
    private ParObiektDPA parObiektDPA;
    private ParObiektRownania parObiektRownania;
    private ParWizualizacja parWizualizacja;
    private ParObiektDPA parObiektSymulacjiDPA;
    private ParObiektRownania parObiektSymulacjiRownania;
    private DisturbanceDPA disturbanceDPA;
    private DisturbanceDiscrete zakloceniaRownania;
    private WizualizacjaZaklocen wizualizacjaZaklocen;
    public ParStrojenie(ParRegulator parRegulator, ParObiektDPA parObiektDPA, ParObiektRownania parObiektRownania, ParWizualizacja parWizualizacja,
                        ParObiektDPA parObiektSymulacjiDPA, ParObiektRownania parObiektSymulacjiRownania, DisturbanceDPA disturbanceDPA,
                        DisturbanceDiscrete zakloceniaRownania, WizualizacjaZaklocen wizualizacjaZaklocen) {
        this.parRegulator = parRegulator;
        this.parObiektDPA = parObiektDPA;
        this.parObiektRownania = parObiektRownania;
        this.parWizualizacja = parWizualizacja;
        this.parObiektSymulacjiDPA = parObiektSymulacjiDPA;
        this.parObiektSymulacjiRownania = parObiektSymulacjiRownania;
        this.disturbanceDPA = disturbanceDPA;
        this.zakloceniaRownania = zakloceniaRownania;
        this.wizualizacjaZaklocen = wizualizacjaZaklocen;
    }
}
