package centric.pl.functions.impl.movement;

import centric.pl.events.impl.EventPacket;
import centric.pl.events.impl.EventUpdate;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.settings.impl.BooleanSetting;
import centric.pl.functions.settings.impl.SliderSetting;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.player.MoveUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.network.play.client.CEntityActionPacket;

@FunctionRegister(name = "Pig Fly", type = Category.Movement, beta = false)
public class BoatFly extends Function {
    final SliderSetting speed = new SliderSetting("Скорость", 10.f, 1.f, 20.f, 0.05f);
    final BooleanSetting noDismount = new BooleanSetting("Не вылезать", true);
    final BooleanSetting savePig = new BooleanSetting("Спасать свинью", true);


    public BoatFly() {
        addSettings(speed, noDismount, savePig);
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (IMinecraft.mc.player.getRidingEntity() != null) {
            if (IMinecraft.mc.player.getRidingEntity() instanceof PigEntity) {

                IMinecraft.mc.player.getRidingEntity().motion.y = 0;
                if (IMinecraft.mc.player.isPassenger()) {
                    if (IMinecraft.mc.gameSettings.keyBindJump.isKeyDown()) {
                        IMinecraft.mc.player.getRidingEntity().motion.y = 1;
                    } else if (IMinecraft.mc.gameSettings.keyBindSneak.isKeyDown()) {
                        IMinecraft.mc.player.getRidingEntity().motion.y = -1;
                    }


                    if (MoveUtils.isMoving()) {
                        final double yaw = MoveUtils.getDirection(true);

                        IMinecraft.mc.player.getRidingEntity().motion.x = -Math.sin(yaw) * speed.get();
                        IMinecraft.mc.player.getRidingEntity().motion.z = Math.cos(yaw) * speed.get();
                    } else {
                        IMinecraft.mc.player.getRidingEntity().motion.x = 0;
                        IMinecraft.mc.player.getRidingEntity().motion.z = 0;
                    }
                    if ((!MoveUtils.isBlockUnder(4f) || IMinecraft.mc.player.collidedHorizontally || IMinecraft.mc.player.collidedVertically) && savePig.get()) {
                        IMinecraft.mc.player.getRidingEntity().motion.y += 1;
                    }
                }
            }
        }
    }

    @Subscribe
    private void onPacket(EventPacket e) {


        if (e.getPacket() instanceof CEntityActionPacket actionPacket) {
            if (!noDismount.get() || !(IMinecraft.mc.player.getRidingEntity() instanceof BoatEntity)) return;
            CEntityActionPacket.Action action = actionPacket.getAction();
            if (action == CEntityActionPacket.Action.PRESS_SHIFT_KEY || action == CEntityActionPacket.Action.RELEASE_SHIFT_KEY)
                e.cancel();
        }
    }

    public boolean notStopRidding() {
        return this.isState() && noDismount.get();
    }
}
