package centric.pl.functions.impl.movement;

import centric.pl.events.impl.EventUpdate;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.player.MoveUtils;
import com.google.common.eventbus.Subscribe;

@FunctionRegister(name = "Jesus", type = Category.Movement, beta = false)
public class Jesus extends Function {

    @Subscribe
    private void onUpdate(EventUpdate update) {
        if (IMinecraft.mc.player.isInWater()) {
            float moveSpeed = 10.0f;
            moveSpeed /= 100.0f;

            double moveX = IMinecraft.mc.player.getForward().x * moveSpeed;
            double moveZ = IMinecraft.mc.player.getForward().z * moveSpeed;
            IMinecraft.mc.player.motion.y = 0f;
            if (MoveUtils.isMoving()) {
                if (MoveUtils.getMotion() < 0.9f) {
                    IMinecraft.mc.player.motion.x *= 1.25f;
                    IMinecraft.mc.player.motion.z *= 1.25f;
                }
            }
        }
    }
}