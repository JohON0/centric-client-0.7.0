package centric.pl.functions.impl.misc;

import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.settings.impl.BooleanSetting;
import centric.pl.functions.settings.impl.ModeListSetting;
import lombok.Getter;

@Getter
@FunctionRegister(name = "AntiPush", type = Category.Player, beta = false)
public class AntiPush extends Function {

    private final ModeListSetting modes = new ModeListSetting("���",
            new BooleanSetting("������", true),
            new BooleanSetting("����", false),
            new BooleanSetting("�����", true));

    public AntiPush() {
        addSettings(modes);
    }

}
