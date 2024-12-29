package centric.pl.functions.settings.impl;


import centric.pl.functions.settings.Setting;

import java.util.function.Supplier;

public class ColorSetting extends Setting<Integer> {

    public ColorSetting(String name, Integer defaultVal) {
        super(name, defaultVal);
    }
    @Override
    public ColorSetting setVisible(Supplier<Boolean> bool) {
        return (ColorSetting) super.setVisible(bool);
    }
}