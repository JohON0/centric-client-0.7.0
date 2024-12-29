package centric.pl.functions.impl.misc;

import com.google.common.eventbus.Subscribe;
import centric.pl.Main;
import centric.pl.events.impl.EventUpdate;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.settings.impl.StringSetting;
import net.minecraft.client.Minecraft;

@FunctionRegister(name = "NameProtect", type = Category.Misc, beta = false)
public class NameProtect extends Function {

    public static String fakeName = "";

    public StringSetting name = new StringSetting(
            "Заменяемое Имя",
            "litvin-antileak",
            "Укажите текст для замены вашего игрового ника"
    );

    public NameProtect() {
        addSettings(name);
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        fakeName = name.get();
    }

    public static String getReplaced(String input) {
        if (Main.getInstance() != null && Main.getInstance().getFunctionRegistry().getNameProtect().isState()) {
            input = input.replace(Minecraft.getInstance().session.getUsername(), fakeName);
        }
        return input;
    }
}
