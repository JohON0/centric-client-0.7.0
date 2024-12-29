package centric.pl.functions.impl.movement;

import centric.pl.events.impl.EventUpdate;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.math.StopWatch;
import centric.pl.johon0.utils.player.InventoryUtil;
import centric.pl.johon0.utils.player.MoveUtils;
import com.google.common.eventbus.Subscribe;

import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;

@FunctionRegister(name = "ElytraFly", type = Category.Movement, beta = false)
public class ElytraFly extends Function {
    StopWatch stopWatch = new StopWatch();
    int oldSlot = -1;
    int bestSlot = -1;
    long delay;

    @Subscribe
    public void onUpdate(EventUpdate e) {
        bestSlot = InventoryUtil.getInstance().findBestSlotInHotBar();
        boolean slotNotNull = IMinecraft.mc.player.inventory.getStackInSlot(bestSlot).getItem() != Items.AIR;
        int invSlot = InventoryUtil.getInstance().getSlotInInventoryOrHotbar(Items.FIREWORK_ROCKET, false);
        int hbSlot = InventoryUtil.getInstance().getSlotInInventoryOrHotbar(Items.FIREWORK_ROCKET, true);

        if (InventoryUtil.getInstance().getSlotInInventory(Items.FIREWORK_ROCKET) == -1) {
            return;
        }

        int elytraSlot = InventoryUtil.getInstance().getSlotInInventoryOrHotbar(Items.ELYTRA, true);
        if (elytraSlot == -1) {
            print("Возьмите элитру в хотбар.");
            toggle();
            return;
        }

        if (IMinecraft.mc.player.isOnGround() && !IMinecraft.mc.gameSettings.keyBindJump.pressed) {
            IMinecraft.mc.gameSettings.keyBindJump.setPressed(true);
        }
        if (!IMinecraft.mc.player.isInWater() && !IMinecraft.mc.player.isOnGround() && !IMinecraft.mc.player.isElytraFlying()) {
            if (!(IMinecraft.mc.player.inventory.armorItemInSlot(2).getItem() instanceof ElytraItem)) {
                IMinecraft.mc.playerController.windowClick(0, 6, elytraSlot, ClickType.SWAP, IMinecraft.mc.player);
                IMinecraft.mc.player.connection.sendPacket(new CEntityActionPacket(IMinecraft.mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
                IMinecraft.mc.playerController.windowClick(0, 6, elytraSlot, ClickType.SWAP, IMinecraft.mc.player);
                if (this.stopWatch.isReached(500)) {
                    swapAndUseFireWorkFromInv(invSlot, hbSlot, slotNotNull);
                    this.stopWatch.reset();
                }
            } else if (bestSlot != -1){
                IMinecraft.mc.playerController.windowClick(0, 6, bestSlot, ClickType.SWAP, IMinecraft.mc.player);
            }
        }
    }

    public static int findNullSlot() {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = IMinecraft.mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() instanceof AirItem) {
                if (i < 9) {
                    i += 36;
                }
                return i;
            }
        }
        return 999;
    }

    private void swapAndUseFireWorkFromInv(int slot, int hbSlot, boolean slotNotNull) {
        if (hbSlot == -1) {
            if (slot >= 0) {
                InventoryUtil.moveItem(slot, bestSlot + 36, slotNotNull);
                if (slotNotNull && oldSlot == -1) {
                    oldSlot = slot;
                }

                IMinecraft.mc.player.connection.sendPacket(new CHeldItemChangePacket(bestSlot));
                IMinecraft.mc.playerController.syncCurrentPlayItem();
                IMinecraft.mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                IMinecraft.mc.player.swingArm(Hand.MAIN_HAND);
                IMinecraft.mc.player.connection.sendPacket(new CHeldItemChangePacket(IMinecraft.mc.player.inventory.currentItem));
                IMinecraft.mc.playerController.syncCurrentPlayItem();
                MoveUtils.setMotion(MoveUtils.getMotion());
                if (oldSlot != -1) {
                    IMinecraft.mc.playerController.windowClick(0, oldSlot, 0, ClickType.PICKUP, IMinecraft.mc.player);
                    IMinecraft.mc.playerController.windowClick(0, bestSlot + 36, 0, ClickType.PICKUP, IMinecraft.mc.player);
                    IMinecraft.mc.playerController.windowClickFixed(0, oldSlot, 0, ClickType.PICKUP, IMinecraft.mc.player, 100);
                    oldSlot = -1;
                    bestSlot = -1;
                    IMinecraft.mc.player.resetActiveHand();
                }

            } else {
                print("Феерверки не найдены!");
            }
        } else {
            useItem(hbSlot, Hand.MAIN_HAND);
        }
    }

    private void useItem(int slot, Hand hand) {
        if (IMinecraft.mc.player.getHeldItemMainhand().getItem() != Items.FIREWORK_ROCKET) {
            IMinecraft.mc.player.connection.sendPacket(new CHeldItemChangePacket(slot));
        }
        IMinecraft.mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(hand));
        IMinecraft.mc.player.swingArm(Hand.MAIN_HAND);
        if (IMinecraft.mc.player.getHeldItemMainhand().getItem() != Items.FIREWORK_ROCKET) {
            IMinecraft.mc.player.connection.sendPacket(new CHeldItemChangePacket(IMinecraft.mc.player.inventory.currentItem));
        }
    }

}
