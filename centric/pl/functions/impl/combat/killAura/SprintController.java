package centric.pl.functions.impl.combat.killAura;

import centric.pl.johon0.utils.client.IMinecraft;
import net.minecraft.network.play.client.CEntityActionPacket;

public class SprintController implements IMinecraft {

    boolean stopSprint;


    public boolean isReady() {
        return stopSprint;
    }

    public void reset() {
        if (stopSprint) {
            if (mc.player.serverSprintState && !mc.player.isElytraFlying()) {
                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_SPRINTING));
            }
            stopSprint = false;
        }
    }
    public void sendStopSprintPacket() {
        if (!stopSprint) {
            if (mc.player.serverSprintState && !mc.player.isElytraFlying()) {
                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.STOP_SPRINTING));
            }
            stopSprint = true;
        }
    }
}
