package centric.pl.functions.impl.misc;

import com.google.common.eventbus.Subscribe;
import centric.pl.events.impl.EventUpdate;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;

import java.util.concurrent.ThreadLocalRandom;

@FunctionRegister(name = "AntiAFK", type = Category.Player, beta = false)
public class AntiAFK extends Function {

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (mc.player.ticksExisted % 200 != 0) return;

        if (mc.player.isOnGround()) mc.player.jump();

        mc.player.rotationYaw += ThreadLocalRandom.current().nextFloat(-10, 10);
    }
}
