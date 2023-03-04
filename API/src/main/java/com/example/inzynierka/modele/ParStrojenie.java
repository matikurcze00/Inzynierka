package com.example.inzynierka.modele;

import lombok.Data;

@Data
public class ParStrojenie {
    public ParStrojenie(ParRegulator parRegulator, ParObiekt parObiekt, ParObiekt parObiektSymulacji) {
        this.parRegulator = parRegulator;
        this.parObiekt = parObiekt;
        this.parObiektSymulacji = parObiektSymulacji;
    }

    private ParRegulator parRegulator;
    private ParObiekt parObiekt;
    private ParWizualizacja parWizualizacja;
    private ParObiekt parObiektSymulacji;
}
