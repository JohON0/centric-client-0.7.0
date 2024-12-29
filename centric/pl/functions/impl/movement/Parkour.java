package centric.pl.functions.impl.movement;

import centric.pl.events.impl.EventUpdate;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.player.MoveUtils;
import com.google.common.eventbus.Subscribe;

@FunctionRegister(name = "Parkour", type = Category.Movement, beta = false)
public class Parkour extends Function {

    @Subscribe
    private void onUpdate(EventUpdate e) {

        if (MoveUtils.isBlockUnder(0.001f) && IMinecraft.mc.player.isOnGround()) {
            IMinecraft.mc.player.jump();
        }
    }

}
