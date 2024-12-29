package centric.pl.functions.impl.movement;

import centric.pl.events.impl.EventUpdate;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.settings.impl.SliderSetting;
import centric.pl.johon0.utils.client.IMinecraft;
import com.google.common.eventbus.Subscribe;

@FunctionRegister(name = "Timer", type = Category.Movement, beta = false)
public class Timer extends Function {

    private final SliderSetting speed = new SliderSetting("Скорость", 2f, 0.1f, 10f, 0.1f);

    public Timer() {
        addSettings(speed);
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        IMinecraft.mc.timer.timerSpeed = speed.get();
    }

    private void reset() {
        IMinecraft.mc.timer.timerSpeed = 1;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        reset();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        reset();
    }
}
