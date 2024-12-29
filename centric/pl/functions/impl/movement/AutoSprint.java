package centric.pl.functions.impl.movement;

import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.settings.impl.BooleanSetting;

@FunctionRegister(name = "AutoSprint", type = Category.Movement, beta = false)
public class AutoSprint extends Function {
    public BooleanSetting saveSprint = new BooleanSetting("Сохранять спринт", true);
    public AutoSprint() {
        addSettings(saveSprint);
    }
}
