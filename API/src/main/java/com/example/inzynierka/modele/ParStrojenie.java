package com.example.inzynierka.modele;

import lombok.Data;

@Data
public class ParStrojenie {

    public ParStrojenie(ParRegulator parRegulator, ParObiekt parObiekt, ParWizualizacja parWizualizacja, ParObiekt parObiektSymulacji, Zaklocenia zaklocenia,
                        WizualizacjaZaklocen wizualizacjaZaklocen) {
        this.parRegulator = parRegulator;
        this.parObiekt = parObiekt;
        this.parWizualizacja = parWizualizacja;
        this.parObiektSymulacji = parObiektSymulacji;
        this.zaklocenia = zaklocenia;
        this.wizualizacjaZaklocen = wizualizacjaZaklocen;
    }

    private ParRegulator parRegulator;
    private ParObiekt parObiekt;
    private ParWizualizacja parWizualizacja;
    private ParObiekt parObiektSymulacji;
    private Zaklocenia zaklocenia;
    private WizualizacjaZaklocen wizualizacjaZaklocen;
}
