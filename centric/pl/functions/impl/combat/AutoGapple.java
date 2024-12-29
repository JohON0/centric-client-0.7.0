package centric.pl.functions.impl.combat;

import centric.pl.events.impl.EventUpdate;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.settings.impl.BooleanSetting;
import centric.pl.functions.settings.impl.SliderSetting;
import centric.pl.johon0.utils.client.IMinecraft;
import com.google.common.eventbus.Subscribe;
import net.minecraft.item.Items;
/**
 * @author JohON0 // 30.08.24 // 14:08 (MSK+4)
 */
@FunctionRegister(name = "AutoGApple", type = Category.Combat, beta = false)
public class AutoGapple extends Function {
    private final SliderSetting healthThreshold = new SliderSetting("Здоровье", 13.0F, 3.0F, 20.0F, 0.05f);
    private final BooleanSetting withAbsorption = new BooleanSetting("Учитывать золотые сердца", false);
    public boolean isEating;

    public AutoGapple() {
        this.addSettings(healthThreshold, withAbsorption);
    }

    @Subscribe
    private void onUpdate(EventUpdate event) {
        handleEating();
    }
    private void handleEating() {
        if (canEat()) {
            startEating();
        } else if (isEating) {
            stopEating();
        }
    }
    public boolean canEat() {
        float health = IMinecraft.mc.player.getHealth();
        if (withAbsorption.get()) {
            health += IMinecraft.mc.player.getAbsorptionAmount();
        }

        return !IMinecraft.mc.player.getShouldBeDead()
                && IMinecraft.mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE
                && health <= healthThreshold.get().floatValue()
                && !IMinecraft.mc.player.getCooldownTracker().hasCooldown(Items.GOLDEN_APPLE);
    }

    private void startEating() {
        if (!IMinecraft.mc.gameSettings.keyBindUseItem.isKeyDown()) {
            IMinecraft.mc.gameSettings.keyBindUseItem.setPressed(true);
            isEating = true;
        }
    }
    private void stopEating() {
        IMinecraft.mc.gameSettings.keyBindUseItem.setPressed(false);
        isEating = false;
    }
}

