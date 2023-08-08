package com.example.inzynierka.models;

import lombok.Data;

@Data
public class ParStrojenie {


    public ParStrojenie(ParRegulator parRegulator, ParObiektDPA parObiektDPA, ParObiektRownania parObiektRownania, ParWizualizacja parWizualizacja,
                        ParObiektDPA parObiektSymulacjiDPA, ParObiektRownania parObiektSymulacjiRownania, ZakloceniaDPA zakloceniaDPA,
                        DisturbanceDiscrete disturbanceDiscrete, WizualizacjaZaklocen wizualizacjaZaklocen) {
        this.parRegulator = parRegulator;
        this.parObiektDPA = parObiektDPA;
        this.parObiektRownania = parObiektRownania;
        this.parWizualizacja = parWizualizacja;
        this.parObiektSymulacjiDPA = parObiektSymulacjiDPA;
        this.parObiektSymulacjiRownania = parObiektSymulacjiRownania;
        this.zakloceniaDPA = zakloceniaDPA;
        this.disturbanceDiscrete = disturbanceDiscrete;
        this.wizualizacjaZaklocen = wizualizacjaZaklocen;
    }

    private ParRegulator parRegulator;
    private ParObiektDPA parObiektDPA;

    private ParObiektRownania parObiektRownania;
    private ParWizualizacja parWizualizacja;
    private ParObiektDPA parObiektSymulacjiDPA;
    private ParObiektRownania parObiektSymulacjiRownania;
    private ZakloceniaDPA zakloceniaDPA;
    private DisturbanceDiscrete disturbanceDiscrete;
    private WizualizacjaZaklocen wizualizacjaZaklocen;
}
