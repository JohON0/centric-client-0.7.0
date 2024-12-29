package centric.pl.functions.impl.misc;

import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.settings.impl.BindSetting;

@FunctionRegister(name = "MusicPlayerUI", type = Category.Misc, beta = true)
public class MusicPlayerUI extends Function {
    public BindSetting setting = new BindSetting("Кнопка открытия", -1);

    public MusicPlayerUI() {
        addSettings(setting);
    }
}
