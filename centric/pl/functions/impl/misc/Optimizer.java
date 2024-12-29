package centric.pl.functions.impl.misc;

import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.settings.impl.BooleanSetting;

@FunctionRegister(name = "Optimizer", type = Category.Misc, beta = false)
public class Optimizer extends Function {
    public final BooleanSetting shadow = new BooleanSetting("Тень", true);

    public Optimizer() {
        addSettings(shadow);
    }

}
