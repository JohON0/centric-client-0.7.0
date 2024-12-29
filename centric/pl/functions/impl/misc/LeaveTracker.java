package centric.pl.functions.impl.misc;

import com.google.common.eventbus.Subscribe;
import centric.pl.events.impl.EventEntityLeave;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.TextFormatting;

@FunctionRegister(name = "LeaveTracker", type = Category.Misc, beta = false)
public class LeaveTracker extends Function {


    @Subscribe
    private void onEntityLeave(EventEntityLeave eel) {
        Entity entity = eel.getEntity();

        if (!isEntityValid(entity)) {
            return;
        }

        String message = "Игрок "
                + entity.getDisplayName().getString()
                + TextFormatting.WHITE +  " ливнул на "
                + entity.getStringPosition();

        print(message);
    }

    private boolean isEntityValid(Entity entity) {
        if (!(entity instanceof AbstractClientPlayerEntity) || entity instanceof ClientPlayerEntity) {
            return false;
        }

        return !(mc.player.getDistance(entity) < 100);
    }
}
