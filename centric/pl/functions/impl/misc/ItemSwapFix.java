package centric.pl.functions.impl.misc;

import com.google.common.eventbus.Subscribe;
import centric.pl.events.impl.EventPacket;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.server.SHeldItemChangePacket;
import net.minecraft.util.math.MathHelper;

@FunctionRegister(name = "ItemSwapFix", type = Category.Misc, beta = false)
public class ItemSwapFix extends Function {

    @Subscribe
    private void onPacket(EventPacket e) {
        if (mc.player == null) return;
        if (e.getPacket() instanceof SHeldItemChangePacket wrapper) {
            final int serverSlot = wrapper.getHeldItemHotbarIndex();
            if (serverSlot != mc.player.inventory.currentItem) {
                int changed = mc.player.inventory.currentItem >= 8 ? mc.player.inventory.currentItem - 1 : mc.player.inventory.currentItem + 1;

                mc.player.connection.sendPacket(new CHeldItemChangePacket(MathHelper.clamp(changed, 0, 8)));
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                e.cancel();
            }
        }
    }
}
