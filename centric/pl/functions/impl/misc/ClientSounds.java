package centric.pl.functions.impl.misc;

import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.settings.impl.ModeSetting;
import centric.pl.functions.settings.impl.SliderSetting;

@FunctionRegister(name = "ClientSounds", type = Category.Misc, beta = false)
public class ClientSounds extends Function {

    public ModeSetting mode = new ModeSetting("Тип", "Обычный", "Обычный");
    public SliderSetting volume = new SliderSetting("Громкость", 70.0f, 0.0f, 100.0f, 1.0f);

    public ClientSounds() {
        addSettings(mode, volume);
    }


    public String getFileName(boolean state) {
        switch (mode.get()) {
            case "Обычный" -> {
                return state ? "clientsounds/enable" : "clientsounds/disable".toString();
            }
        }
        return "";
    }
}
