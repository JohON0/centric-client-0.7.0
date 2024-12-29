package centric.pl.functions.impl.render;

import centric.pl.events.impl.EventUpdate;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.johon0.utils.client.IMinecraft;
import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.player.PlayerEntity;

@FunctionRegister(name = "SeeInvisibles", type = Category.Render, beta = false)
public class SeeInvisibles extends Function {


    @Subscribe
    private void onUpdate(EventUpdate e) {
        for (PlayerEntity player : IMinecraft.mc.world.getPlayers()) {
            if (player != IMinecraft.mc.player && player.isInvisible()) {
                player.setInvisible(false);
            }
        }
    }

}
