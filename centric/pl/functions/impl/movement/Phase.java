package centric.pl.functions.impl.movement;

import centric.pl.events.impl.EventUpdate;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.johon0.utils.client.IMinecraft;
import com.google.common.eventbus.Subscribe;

@FunctionRegister(name = "Phase", type = Category.Movement, beta = false)
public class Phase extends Function {

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (!collisionPredict()) {
            if (IMinecraft.mc.gameSettings.keyBindJump.pressed) {
                IMinecraft.mc.player.setOnGround(true);
            }
        }
    }

    public boolean collisionPredict() {
        boolean prevCollision = IMinecraft.mc.world
                .getCollisionShapes(IMinecraft.mc.player, IMinecraft.mc.player.getBoundingBox().shrink(0.0625D)).toList().isEmpty();

        return IMinecraft.mc.world.getCollisionShapes(IMinecraft.mc.player, IMinecraft.mc.player.getBoundingBox().shrink(0.0625D))
                .toList().isEmpty() && prevCollision;
    }
}
