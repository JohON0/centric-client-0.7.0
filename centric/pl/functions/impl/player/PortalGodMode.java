package centric.pl.functions.impl.player;

import centric.pl.events.impl.EventPacket;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import com.google.common.eventbus.Subscribe;
import net.minecraft.network.play.client.CConfirmTeleportPacket;

@FunctionRegister(name = "PortalGodMode", type = Category.Player, beta = false)
public class PortalGodMode extends Function {

    @Subscribe
    public void onPacket(EventPacket e) {
        if (e.getPacket() instanceof CConfirmTeleportPacket) {
            e.cancel();
        }
    }
}
