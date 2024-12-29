package centric.pl.functions.settings.impl;


import centric.pl.functions.settings.Setting;

import java.util.function.Supplier;

public class BooleanSetting extends Setting<Boolean> {

    public float anim;

    public BooleanSetting(String name, Boolean defaultVal) {
        super(name, defaultVal);
    }

    @Override
    public BooleanSetting setVisible(Supplier<Boolean> bool) {
        return (BooleanSetting) super.setVisible(bool);
    }

}