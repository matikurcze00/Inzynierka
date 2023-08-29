package com.example.inzynierka.models;

import lombok.Data;

@Data
public class ParStrojenie {


    public ParStrojenie(ParRegulator parRegulator, ParObiektDPA parObiektDPA, ParObiektRownania parObiektRownania, ParWizualizacja parWizualizacja,
                        ParObiektDPA parObiektSymulacjiDPA, ParObiektRownania parObiektSymulacjiRownania, ZakloceniaDPA zakloceniaDPA,
                        DisturbanceDiscrete zakloceniaRownania, WizualizacjaZaklocen wizualizacjaZaklocen) {
        this.parRegulator = parRegulator;
        this.parObiektDPA = parObiektDPA;
        this.parObiektRownania = parObiektRownania;
        this.parWizualizacja = parWizualizacja;
        this.parObiektSymulacjiDPA = parObiektSymulacjiDPA;
        this.parObiektSymulacjiRownania = parObiektSymulacjiRownania;
        this.zakloceniaDPA = zakloceniaDPA;
        this.zakloceniaRownania = zakloceniaRownania;
        this.wizualizacjaZaklocen = wizualizacjaZaklocen;
    }

    private ParRegulator parRegulator;
    private ParObiektDPA parObiektDPA;

    private ParObiektRownania parObiektRownania;
    private ParWizualizacja parWizualizacja;
    private ParObiektDPA parObiektSymulacjiDPA;
    private ParObiektRownania parObiektSymulacjiRownania;
    private ZakloceniaDPA zakloceniaDPA;
    private DisturbanceDiscrete zakloceniaRownania;
    private WizualizacjaZaklocen wizualizacjaZaklocen;
}
