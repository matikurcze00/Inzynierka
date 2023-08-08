package com.example.inzynierka.models;

import lombok.Data;

@Data
public class OdpowiedzSkokowa {
    private double[][] przebieg;

    public OdpowiedzSkokowa(double[][] przebieg) {
        this.przebieg = przebieg;
    }
}
