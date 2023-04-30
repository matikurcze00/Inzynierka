package com.example.inzynierka.regulatory;

import Jama.Matrix;
import lombok.Data;

import java.util.List;

@Data
abstract public class RegulatorMPC extends Regulator{
    protected Integer D;
    protected Integer N;
    protected Integer Nu;
    protected Matrix M;
    private Matrix K;
    protected double duMax;
    protected List<Double> Lambda;
}
