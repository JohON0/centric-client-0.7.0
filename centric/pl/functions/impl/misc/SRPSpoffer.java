package centric.pl.functions.impl.misc;

import centric.pl.functions.api.Category;
import com.google.common.eventbus.Subscribe;
import centric.pl.events.impl.EventPacket;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import net.minecraft.network.play.client.CResourcePackStatusPacket;
import net.minecraft.network.play.server.SSendResourcePackPacket;

@FunctionRegister(name = "SRPSpoofer", type = Category.Misc, beta = false)
public class SRPSpoffer extends Function {

    @Subscribe
    private void onPacket(EventPacket e) {
        if (e.getPacket() instanceof SSendResourcePackPacket s) {
            e.cancel();
        }
        if (e.getPacket() instanceof CResourcePackStatusPacket s) {
            s.action = CResourcePackStatusPacket.Action.SUCCESSFULLY_LOADED;
        }
    }

}
