package centric.pl.functions.impl.movement;

import centric.pl.events.impl.MovingEvent;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.johon0.utils.client.IMinecraft;
import com.google.common.eventbus.Subscribe;
import net.minecraft.util.math.vector.Vector3d;

@FunctionRegister(name = "NoClip", type = Category.Movement, beta = false)
public class NoClip extends Function {

    @Subscribe
    private void onMoving(MovingEvent move) {
            if (!collisionPredict(move.getTo())) {
                if (move.isCollidedHorizontal())
                    move.setIgnoreHorizontal(true);
                if (move.getMotion().y > 0 || IMinecraft.mc.player.isSneaking()) {
                    move.setIgnoreVertical(true);
                }
                move.getMotion().y = Math.min(move.getMotion().y, 99999);
        }
    }


    public boolean collisionPredict(Vector3d to) {
        boolean prevCollision = IMinecraft.mc.world
                .getCollisionShapes(IMinecraft.mc.player, IMinecraft.mc.player.getBoundingBox().shrink(0.0625D)).toList().isEmpty();
        Vector3d backUp = new Vector3d(IMinecraft.mc.player.getPosX(), IMinecraft.mc.player.getPosY(), IMinecraft.mc.player.getPosZ());
        IMinecraft.mc.player.setPosition(to.x, to.y, to.z);
        boolean collision = IMinecraft.mc.world.getCollisionShapes(IMinecraft.mc.player, IMinecraft.mc.player.getBoundingBox().shrink(0.0625D))
                .toList().isEmpty() && prevCollision;
        IMinecraft.mc.player.setPosition(backUp.x, backUp.y, backUp.z);
        return collision;
    }
}

