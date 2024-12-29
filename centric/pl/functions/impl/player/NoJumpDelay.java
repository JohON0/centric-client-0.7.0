package centric.pl.functions.impl.player;

import centric.pl.events.impl.EventUpdate;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.johon0.utils.client.IMinecraft;
import com.google.common.eventbus.Subscribe;

@FunctionRegister(name = "NoJumpDelay", type = Category.Player, beta = false)
public class NoJumpDelay extends Function {
    @Subscribe
    public void onUpdate(EventUpdate e) {
        IMinecraft.mc.player.jumpTicks = 0;
    }
}
