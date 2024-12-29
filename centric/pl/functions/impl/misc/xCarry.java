package centric.pl.functions.impl.misc;

import centric.pl.functions.api.Category;
import com.google.common.eventbus.Subscribe;
import centric.pl.events.impl.EventPacket;
import centric.pl.functions.api.Function;
import net.minecraft.network.play.client.CCloseWindowPacket;
import centric.pl.functions.api.FunctionRegister;

@FunctionRegister(name = "xCarry", type = Category.Misc, beta = false)
public class xCarry extends Function {

    @Subscribe
    public void onPacket(EventPacket e) {
        if (mc.player == null) return;

        if (e.getPacket() instanceof CCloseWindowPacket) {
            e.cancel();
        }
    }
}
