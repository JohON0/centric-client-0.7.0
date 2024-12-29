package centric.pl.functions.impl.render;

import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.settings.impl.BooleanSetting;
import centric.pl.functions.settings.impl.ModeSetting;

@FunctionRegister(name = "ClickGui", type = Category.Render,beta = false)
public class ClickGUI extends Function {


    public final BooleanSetting blur = new BooleanSetting("Размытие Фона", false);
    public final BooleanSetting wooman = new BooleanSetting("Женщина", false);
//    public static BooleanSetting images = new BooleanSetting("Картинки", true);
//    public static ModeSetting imageType = new ModeSetting("Текстура", "Няша", "Няша", "Няша 2", "Bottle").setVisible(() -> images.get());



    public ClickGUI() {
        addSettings(wooman,blur);

    }
    public void onEnable() {
        toggle();
    }
}
