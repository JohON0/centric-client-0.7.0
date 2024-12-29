package centric.pl.johon0.utils.animations.impl;


import centric.pl.johon0.utils.animations.Animation;

public class EaseInOutCubic extends Animation {
    public EaseInOutCubic(int ms, double endPoint) {
        super(ms, endPoint);
    }

    @Override
    protected double getEquation(double x) {
        return x < 0.5 ? 4 * x * x * x : (float) (1 - Math.pow(-2 * x + 2, 3) / 2);
    }
}
