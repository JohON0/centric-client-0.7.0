package centric.pl.functions.impl.render;

import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.settings.impl.BooleanSetting;
import centric.pl.functions.settings.impl.ModeSetting;

@FunctionRegister(name = "ClickGui", type = Category.Render,beta = false)
public class ClickGUI extends Function {


    public final BooleanSetting blur = new BooleanSetting("�������� ����", false);
    public final BooleanSetting wooman = new BooleanSetting("�������", false);
//    public static BooleanSetting images = new BooleanSetting("��������", true);
//    public static ModeSetting imageType = new ModeSetting("��������", "����", "����", "���� 2", "Bottle").setVisible(() -> images.get());



    public ClickGUI() {
        addSettings(wooman,blur);

    }
    public void onEnable() {
        toggle();
    }
}
