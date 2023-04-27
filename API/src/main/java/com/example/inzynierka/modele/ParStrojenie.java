package com.example.inzynierka.modele;

import lombok.Data;

@Data
public class ParStrojenie {

    public ParStrojenie(ParRegulator parRegulator, ParObiektDPA parObiektDPA, ParObiektRownania parObiektRownania, ParWizualizacja parWizualizacja,
                        ParObiektDPA parObiektSymulacjiDPA, ParObiektRownania parObiektSymulacjiRownania, Zaklocenia zaklocenia,
                        WizualizacjaZaklocen wizualizacjaZaklocen) {
        this.parRegulator = parRegulator;
        this.parObiektDPA = parObiektDPA;
        this.parObiektRownania = parObiektRownania;
        this.parWizualizacja = parWizualizacja;
        this.parObiektSymulacjiDPA = parObiektSymulacjiDPA;
        this.parObiektSymulacjiRownania = parObiektSymulacjiRownania;
        this.zaklocenia = zaklocenia;
        this.wizualizacjaZaklocen = wizualizacjaZaklocen;
    }

    private ParRegulator parRegulator;
    private ParObiektDPA parObiektDPA;

    private ParObiektRownania parObiektRownania;
    private ParWizualizacja parWizualizacja;
    private ParObiektDPA parObiektSymulacjiDPA;
    private ParObiektRownania parObiektSymulacjiRownania;
    private Zaklocenia zaklocenia;
    private WizualizacjaZaklocen wizualizacjaZaklocen;
}
