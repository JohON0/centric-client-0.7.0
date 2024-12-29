package centric.pl.functions.impl.combat;


import java.util.Iterator;

import centric.pl.Main;
import centric.pl.command.friends.FriendStorage;
import centric.pl.events.impl.EventUpdate;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.settings.impl.BooleanSetting;
import centric.pl.functions.settings.impl.SliderSetting;
import centric.pl.managers.notificationManager.Notification;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.math.StopWatch;
import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Items;
import net.minecraft.util.text.TextFormatting;

@FunctionRegister(
        name = "ShieldWarning", type = Category.Combat, beta = false)
public class ShieldWarning extends Function {
    private final StopWatch timerUtil = new StopWatch();
    private final SliderSetting distance = new SliderSetting("Дистанция проверки", 3.0F, 0.01F, 6.0F, 0.01F);
    private final SliderSetting delay = new SliderSetting("Задержка отжатия", 0.0F, 0.0F, 3000.0F, 0.01F);
    public BooleanSetting actions = new BooleanSetting("Писать сообщение о топоре", true);
    public BooleanSetting nofriend = new BooleanSetting("Не проверять друзей", true);

    public ShieldWarning() {
        this.addSettings(this.distance, this.delay, this.actions, this.nofriend);
    }
    @Subscribe
    private void onUpdate(EventUpdate event) {
        if (event instanceof EventUpdate) {
            if (IMinecraft.mc.player == null || IMinecraft.mc.world == null) {
            }

            Iterator var2 = IMinecraft.mc.world.getPlayers().iterator();

            while(var2.hasNext()) {
                Entity entity = (Entity)var2.next();
                if (entity instanceof PlayerEntity && IMinecraft.mc.player.getDistance(entity) < this.distance.get().floatValue()) {
                    boolean check = ((PlayerEntity)entity).getHeldItemMainhand().getItem() instanceof AxeItem;
                    if (IMinecraft.mc.player.getHeldItemOffhand().getItem() == Items.SHIELD && IMinecraft.mc.player.isHandActive() && ((PlayerEntity)entity).getHeldItemMainhand().getItem() instanceof AxeItem) {
                        if (this.nofriend.get() && FriendStorage.isFriend(entity.getScoreboardName())) {
                        }

                        if (IMinecraft.mc.gameSettings.keyBindUseItem.isKeyDown() && this.timerUtil.hasTimeElapsed(delay.get().longValue())) {
                            IMinecraft.mc.gameSettings.keyBindUseItem.setPressed(false);
                        }

                        if (this.actions.get() && this.timerUtil.hasTimeElapsed(delay.get().longValue())) {
                            Main.getInstance().getNotification().add( "Противник взял " + TextFormatting.RED + "топор! ", "Отжимаю немедленно щит", 5, Notification.Type.warning);
                            this.timerUtil.reset();
                        }
                    }
                }
            }
        }
    }
}
