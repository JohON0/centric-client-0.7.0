package centric.pl.functions.impl.misc;

import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.settings.impl.BooleanSetting;

@FunctionRegister(name = "BetterMinecraft", type = Category.Misc, beta = false)
public class BetterMinecraft extends Function {

    public final BooleanSetting smoothCamera = new BooleanSetting("������� ������", true);
    //public final BooleanSetting smoothTab = new BooleanSetting("������� ���", true); // ���
    public final BooleanSetting betterTab = new BooleanSetting("���������� ���", true);

    public BetterMinecraft() {
        addSettings(smoothCamera, betterTab);
    }
}
