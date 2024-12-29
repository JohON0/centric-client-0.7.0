package centric.pl.functions.impl.movement;

import centric.pl.events.impl.NoSlowEvent;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.math.StopWatch;
import centric.pl.johon0.utils.player.MoveUtils;
import com.google.common.eventbus.Subscribe;
import centric.pl.functions.settings.impl.BooleanSetting;
import centric.pl.functions.settings.impl.ModeSetting;
import lombok.ToString;
import net.minecraft.item.UseAction;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;

@ToString
@FunctionRegister(name = "NoSlow", type = Category.Movement, beta = false)
public class NoSlow extends Function {

    private final ModeSetting mode = new ModeSetting("Мод", "Matrix", "Matrix", "Grim", "MusteryWorld");
    private final BooleanSetting rightHand = new BooleanSetting("В правой руке", false).setVisible(() -> mode.is("Grim"));
    StopWatch stopWatch = new StopWatch();

    public NoSlow() {
        addSettings(mode, rightHand);
    }


    @Subscribe
    public void onEating(NoSlowEvent e) {
        if (IMinecraft.mc.player.isHandActive()) {
            switch (mode.get()) {
                case "Grim" -> handleGrimACMode(e);
                case "Matrix" -> handleMatrixMode(e);
                case "MusteryWorld" -> musteryWorldMode(e);
            }
        }
    }

    private void mwMode(NoSlowEvent noSlowEvent) {
        if (!(NoSlow.mc.player.getHeldItemOffhand().getUseAction() == UseAction.BLOCK && NoSlow.mc.player.getActiveHand() == Hand.MAIN_HAND || NoSlow.mc.player.getHeldItemOffhand().getUseAction() == UseAction.EAT && NoSlow.mc.player.getActiveHand() == Hand.MAIN_HAND)) {
            if (NoSlow.mc.player.getActiveHand() == Hand.MAIN_HAND) {
                NoSlow.mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
                noSlowEvent.cancel();
            } else {
                noSlowEvent.cancel();
                this.sendItemChangePacket();
            }
        }
    }
    boolean restart = false;
    boolean restart1 = false;
    private boolean wasHoldingItem = false;
    private long lastActionTime = 0L;
    private void musteryWorldMode(NoSlowEvent noSlow) {
        boolean isHoldingItem = NoSlow.mc.player.isHandActive();
        long currentTime = System.currentTimeMillis();
        if (isHoldingItem) {
            if (currentTime - this.lastActionTime >= 97L) {
                this.lastActionTime = currentTime;
                if ((NoSlow.mc.player.isInWater() || this.restart) && NoSlow.mc.player.getActiveHand() == Hand.MAIN_HAND && NoSlow.mc.player.getHeldItemOffhand().getUseAction() == UseAction.NONE) {
                    NoSlow.mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
                    noSlow.cancel();
                }
                if (NoSlow.mc.player.getActiveHand() == Hand.OFF_HAND) {
                    NoSlow.mc.player.connection.sendPacket(new CHeldItemChangePacket(NoSlow.mc.player.inventory.currentItem % 8 + 1));
                    NoSlow.mc.player.connection.sendPacket(new CHeldItemChangePacket(NoSlow.mc.player.inventory.currentItem));
                    noSlow.cancel();
                }
            }
            this.wasHoldingItem = true;
        } else if (this.wasHoldingItem) {
            this.lastActionTime = 0L;
            this.wasHoldingItem = false;
        }
        if (NoSlow.mc.player.getActiveHand() == Hand.OFF_HAND && !NoSlow.mc.player.isOnGround() && MoveUtils.isMoving()) {
            NoSlow.mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            noSlow.cancel();
            }
        if (NoSlow.mc.player.getActiveHand() == Hand.MAIN_HAND) {
            NoSlow.mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
            noSlow.cancel();
        }
    }
    private void sendItemChangePacket() {
        if (MoveUtils.isMoving()) {
            NoSlow.mc.player.connection.sendPacket(new CHeldItemChangePacket(NoSlow.mc.player.inventory.currentItem % 8 + 1));
            NoSlow.mc.player.connection.sendPacket(new CHeldItemChangePacket(NoSlow.mc.player.inventory.currentItem));
        }
    }
    private void handleMatrixMode(NoSlowEvent eventNoSlow) {
        boolean isFalling = (double) IMinecraft.mc.player.fallDistance > 0.725;
        float speedMultiplier;
        eventNoSlow.cancel();
        if (IMinecraft.mc.player.isOnGround() && !IMinecraft.mc.player.movementInput.jump) {
            if (IMinecraft.mc.player.ticksExisted % 2 == 0) {
                boolean isNotStrafing = IMinecraft.mc.player.moveStrafing == 0.0F;
                speedMultiplier = isNotStrafing ? 0.5F : 0.4F;
                IMinecraft.mc.player.motion.x *= speedMultiplier;
                IMinecraft.mc.player.motion.z *= speedMultiplier;
            }
        } else if (isFalling) {
            boolean isVeryFastFalling = (double) IMinecraft.mc.player.fallDistance > 1.4;
            speedMultiplier = isVeryFastFalling ? 0.95F : 0.97F;
            IMinecraft.mc.player.motion.x *= speedMultiplier;
            IMinecraft.mc.player.motion.z *= speedMultiplier;
        }
    }

    private void handleGrimACMode(NoSlowEvent noSlow) {
        boolean offHandActive = IMinecraft.mc.player.isHandActive() && IMinecraft.mc.player.getActiveHand() == Hand.OFF_HAND;
        boolean mainHandActive = IMinecraft.mc.player.isHandActive() && IMinecraft.mc.player.getActiveHand() == Hand.MAIN_HAND;
        if (IMinecraft.mc.player.isHandActive() && !IMinecraft.mc.player.isPassenger() && IMinecraft.mc.player.getItemInUseCount() > 3) {
            IMinecraft.mc.playerController.syncCurrentPlayItem();
            if (offHandActive && !IMinecraft.mc.player.getCooldownTracker().hasCooldown(IMinecraft.mc.player.getHeldItemOffhand().getItem())) {
                int old = IMinecraft.mc.player.inventory.currentItem;
                IMinecraft.mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
                IMinecraft.mc.player.connection.sendPacket(new CHeldItemChangePacket(old + 1 > 8 ? old - 1 : old + 1));
                IMinecraft.mc.player.connection.sendPacket(new CHeldItemChangePacket(IMinecraft.mc.player.inventory.currentItem));
                noSlow.cancel();
            }
            if (mainHandActive && !IMinecraft.mc.player.getCooldownTracker().hasCooldown(IMinecraft.mc.player.getHeldItemMainhand().getItem()) && rightHand.get()) {
                IMinecraft.mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
                if (IMinecraft.mc.player.getHeldItemOffhand().getUseAction().equals(UseAction.NONE)) {
                    noSlow.cancel();
                }
            }
            IMinecraft.mc.playerController.syncCurrentPlayItem();
        }
    }
}
