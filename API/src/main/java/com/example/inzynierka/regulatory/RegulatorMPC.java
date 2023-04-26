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
    protected Matrix Mp;
    private Matrix K;
    protected Matrix dU;
    protected double duMax;
    protected List<Double> Lambda;
}
