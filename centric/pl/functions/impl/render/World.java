package centric.pl.functions.impl.render;

import centric.pl.events.impl.EventPacket;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.settings.impl.BooleanSetting;
import centric.pl.functions.settings.impl.ColorSetting;
import centric.pl.functions.settings.impl.ModeSetting;
import centric.pl.functions.settings.impl.SliderSetting;
import centric.pl.johon0.utils.render.ColorUtils;
import com.google.common.eventbus.Subscribe;

import lombok.Getter;
import net.minecraft.network.play.server.SUpdateTimePacket;

@Getter
@FunctionRegister(name = "World", type = Category.Render, beta = false)
public class World extends Function {

    public ModeSetting time = new ModeSetting("Time", "День", "День", "Ночь");
    public BooleanSetting fog = new BooleanSetting("Цветной туман", false);
    public BooleanSetting clientcolorfog = new BooleanSetting("Туман под цвет клиента", false).setVisible(() -> fog.get());
    public ColorSetting colorfog = new ColorSetting("Цвет Тумана", ColorUtils.rgb(255,255,255)).setVisible(() -> !clientcolorfog.get() && fog.get());
    public SliderSetting distancefog = new SliderSetting("Дистанция тумана", 5,0,10,0.5f).setVisible(() -> fog.get());


    public World() {
        addSettings(time,fog,clientcolorfog,colorfog);
    }
    @Subscribe
    public void onPacket(EventPacket e) {
        if (e.getPacket() instanceof SUpdateTimePacket p) {
            if (time.get().equalsIgnoreCase("День"))
                p.worldTime = 1000L;
            else
                p.worldTime = 15000L;
        }
    }
}
