package com.example.inzynierka.modele;

import lombok.Data;

@Data
public class ParStrojenie {
    public ParStrojenie(ParRegulator parRegulator, ParObiekt parObiekt) {
        this.parRegulator = parRegulator;
        this.parObiekt = parObiekt;
    }

    private ParRegulator parRegulator;
    private ParObiekt parObiekt;
    private ParWizualizacja parWizualizacja;
}
