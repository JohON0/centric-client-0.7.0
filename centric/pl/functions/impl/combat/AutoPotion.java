package centric.pl.functions.impl.combat;

import centric.pl.events.impl.EventMotion;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.johon0.utils.math.MathUtil;
import centric.pl.johon0.utils.math.StopWatch;
import centric.pl.johon0.utils.player.InventoryUtil;
import centric.pl.johon0.utils.player.MoveUtils;
import com.google.common.eventbus.Subscribe;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.potion.*;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

@FunctionRegister(
        name = "AutoPotion",
        type = Category.Combat,
        beta = false)
public class AutoPotion extends Function {
    float previousPitch;
    final StopWatch stopWatch = new StopWatch();

    public AutoPotion() {
    }

    @Subscribe
    public void onMotion(EventMotion e) {
        if (this.canThrowPotion()) {
            if (this.isActive()) {
                Vector3d posPoint = this.findNearestCollision();
                Vector2f vector2f;
                if (posPoint == null) {
                    vector2f = new Vector2f(mc.player.rotationYaw, 90.0F);
                } else {
                    vector2f = MathUtil.rotationToVec(posPoint);
                }

                Vector2f rotationVector = vector2f;
                this.previousPitch = rotationVector.y;
                e.setYaw(rotationVector.x);
                e.setPitch(this.previousPitch);
                Minecraft var4 = mc;
                mc.player.rotationPitchHead = this.previousPitch;
            }

            e.setPostMotion(() -> {
                boolean pitchIsValid = this.previousPitch == e.getPitch();
                int oldCurrentItem = mc.player.inventory.currentItem;
                Potions[] var4 = Potions.values();
                int var5 = var4.length;

                for(int var6 = 0; var6 < var5; ++var6) {
                    Potions potion = var4[var6];
                    potion.state = true;
                    if (!this.shouldUsePotion(potion) && potion.state && pitchIsValid) {
                        this.sendPotion(potion);
                        
                        mc.player.connection.sendPacket(new CHeldItemChangePacket(oldCurrentItem));
                        mc.playerController.syncCurrentPlayItem();
                    }
                }

            });
        }
    }

    @Subscribe
    private void onWalking(EventMotion e) {
        if (this.isActive()) {
            Vector3d posPoint = this.findNearestCollision();
            Vector2f vector2f;
            if (posPoint == null) {

                vector2f = new Vector2f(mc.player.rotationYaw, 90.0F);
            } else {
                vector2f = MathUtil.rotationToVec(posPoint);
            }

            Vector2f rotationVector = vector2f;
            float yaw = rotationVector.x;
            float pitch = rotationVector.y;
            e.setYaw(yaw);
            e.setPitch(pitch);
            mc.player.rotationYawHead = yaw;
            mc.player.renderYawOffset = yaw;
            mc.player.rotationPitchHead = pitch;
        }
    }

    public boolean isActive() {
        Potions[] var1 = Potions.values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            Potions potionType = var1[var3];
            if (!this.shouldUsePotion(potionType) && potionType.isState()) {
                return true;
            }
        }

        return false;
    }

    public boolean canThrowPotion() {
        boolean var4;
        Throw: {
            if (MoveUtils.isBlockUnder(0.5F)) {
                
                if (!mc.player.isOnGround()) {
                    var4 = false;
                    break Throw;
                }
            }

            var4 = true;
        }

        boolean isOnGround = var4;
        boolean timeIsReached = this.stopWatch.isReached(400L);
        
        boolean ticksExisted = mc.player.ticksExisted > 100;
        return isOnGround && timeIsReached && ticksExisted;
    }

    private boolean shouldUsePotion(Potions potions) {
        if (mc.player.isPotionActive(potions.getPotion())) {
            potions.state = false;
            return true;
        } else {
            int potionId = potions.getId();
            if (this.findPotionSlot(potionId, true) == -1 && this.findPotionSlot(potionId, false) == -1) {
                potions.state = false;
                return true;
            } else {
                return false;
            }
        }
    }

    private void sendPotion(Potions potions) {
        int potionId = potions.getId();
        int hotBarSlot = this.findPotionSlot(potionId, true);
        int inventorySlot = this.findPotionSlot(potionId, false);
        if (mc.player.isPotionActive(potions.getPotion())) {
            potions.state = false;
        }

        if (hotBarSlot != -1) {
            this.sendUsePacket(hotBarSlot, Hand.MAIN_HAND);
        } else {
            if (inventorySlot != -1) {
                int bestSlotInHotBar = InventoryUtil.getInstance().findBestSlotInHotBar();
                
                ItemStack inventoryStack = mc.player.inventory.getStackInSlot(inventorySlot);
                
                ItemStack bestSlotStack = mc.player.inventory.getStackInSlot(bestSlotInHotBar);
                InventoryUtil.moveItem(inventorySlot, bestSlotInHotBar + 36, bestSlotStack.getItem() != Items.AIR);
                this.sendUsePacket(bestSlotInHotBar, Hand.MAIN_HAND);
            }

        }
    }

    private void sendUsePacket(int slot, Hand hand) {
        mc.player.connection.sendPacket(new CHeldItemChangePacket(slot));
        
        mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(hand));

        mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
        this.previousPitch = 0.0F;
        this.stopWatch.reset();
    }

    private int findPotionSlot(int id, boolean inHotBar) {
        int start = inHotBar ? 0 : 9;
        int end = inHotBar ? 9 : 36;

        for(int i = start; i < end; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof SplashPotionItem) {
                List<EffectInstance> potionEffects = PotionUtils.getEffectsFromStack(stack);

                for (EffectInstance effectInstance : potionEffects) {
                    if (effectInstance.getPotion() == Effect.get(id)) {
                        return i;
                    }
                }
            }
        }

        return -1;
    }

    private Vector3d findNearestCollision() {
        return mc.world.getCollisionShapes(mc.player, mc.player.getBoundingBox().grow(0.0, 0.5, 0.0)).toList().stream().min(Comparator.comparingDouble((box) -> {
            Vector3d vector3d = box.getBoundingBox().getCenter();
            return vector3d.squareDistanceTo(mc.player.getPositionVec());
        })).map((box) -> {
            return box.getBoundingBox().getCenter();
        }).orElse(null);
    }
    public enum Potions {
         STRENGTH(Effects.STRENGTH, 5),
        SPEED(Effects.SPEED, 1),
        FIRE_RESIST(Effects.FIRE_RESISTANCE, 12);

        private final Effect potion;
        private final int id;
        private boolean state;

        private Potions(Effect potion, int potionId) {
            this.potion = potion;
            this.id = potionId;
        }

        public Effect getPotion() {
            return this.potion;
        }

        public int getId() {
            return this.id;
        }

        public boolean isState() {
            return this.state;
        }
    }

}
