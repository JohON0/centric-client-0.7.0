package centric.pl.functions.impl.render;

import centric.pl.events.impl.EventCancelOverlay;
import centric.pl.events.impl.EventUpdate;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.settings.impl.BooleanSetting;
import centric.pl.functions.settings.impl.ModeListSetting;
import centric.pl.johon0.utils.client.IMinecraft;
import com.google.common.eventbus.Subscribe;
import net.minecraft.potion.Effects;
@FunctionRegister(name = "NoRender", type = Category.Render, beta = false)
public class NoRender extends Function {

    public ModeListSetting element = new ModeListSetting("Удалять",
            new BooleanSetting("Огонь на экране", true),
            new BooleanSetting("Линия босса", true),
            new BooleanSetting("Анимация тотема", true),
            new BooleanSetting("Тайтлы", true),
            new BooleanSetting("Таблица", true),
            new BooleanSetting("Эффект визера", true),
            new BooleanSetting("Туман", true),
            new BooleanSetting("Тряску камеры", true),
            new BooleanSetting("Плохие эффекты", true),
            new BooleanSetting("Дождь", true),
            new BooleanSetting("Кустики", true));

    public NoRender() {
        addSettings(element);
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        handleEventUpdate(e);
    }

    @Subscribe
    private void onEventCancelOverlay(EventCancelOverlay e) {
        handleEventOverlaysRender(e);
    }

    private void handleEventOverlaysRender(EventCancelOverlay event) {
        boolean cancelOverlay = switch (event.overlayType) {
            case FIRE_OVERLAY -> element.getValueByName("Огонь на экране").get();
            case BOSS_LINE -> element.getValueByName("Линия босса").get();
            case SCOREBOARD -> element.getValueByName("Таблица").get();
            case TITLES -> element.getValueByName("Тайтлы").get();
            case TOTEM -> element.getValueByName("Анимация тотема").get();
            case FOG -> element.getValueByName("Туман").get();
            case HURT -> element.getValueByName("Тряску камеры").get();
        };

        if (cancelOverlay) {
            event.cancel();
        }
    }

    private void handleEventUpdate(EventUpdate event) {
        boolean isRaining = IMinecraft.mc.world.isRaining() && element.getValueByName("Дождь").get();

        boolean hasEffects = (IMinecraft.mc.player.isPotionActive(Effects.BLINDNESS)
                || IMinecraft.mc.player.isPotionActive(Effects.NAUSEA)) && element.getValueByName("Плохие эффекты").get();

        if (isRaining) {
            IMinecraft.mc.world.setRainStrength(0);
            IMinecraft.mc.world.setThunderStrength(0);
        }

        if (hasEffects) {
            IMinecraft.mc.player.removePotionEffect(Effects.NAUSEA);
            IMinecraft.mc.player.removePotionEffect(Effects.BLINDNESS);
        }
    }
}
