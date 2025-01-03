package centric.pl.functions.impl.combat;

import centric.pl.events.impl.EventKey;
import centric.pl.events.impl.EventPacket;
import centric.pl.events.impl.EventUpdate;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.settings.impl.BindSetting;
import centric.pl.functions.settings.impl.ModeSetting;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.math.StopWatch;
import centric.pl.johon0.utils.player.InventoryUtil;
import com.google.common.eventbus.Subscribe;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@FunctionRegister(
        name = "AutoSwap",
        type = Category.Combat,
        beta = false
)
public class AutoSwap extends Function {
    final ModeSetting itemType = new ModeSetting("�������", "���", "���", "�����", "�����", "���");
    final ModeSetting swapType = new ModeSetting("������� ��", "�����", "���", "�����", "�����", "���");
    final BindSetting keyToSwap = new BindSetting("������", -1);
    final StopWatch stopWatch = new StopWatch();
    @NonFinal
    long delay;
    @NonFinal
    boolean keyPressed;

    final InventoryUtil.Hand pickItemUtil = new InventoryUtil.Hand();

    public AutoSwap() {
        addSettings(itemType, swapType, keyToSwap);
    }

    @Subscribe
    public void onKey(EventKey keyEvent) {
        keyPressed = keyEvent.isKeyDown(keyToSwap.get());
    }


    @Subscribe
    public void onTick(EventUpdate e) {
        this.pickItemUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);

        if (keyPressed) {
            Item currentItem = getSelectedItem();
            Item swapItem = getSwapItem();
            int currentItemSlot = getSlot(currentItem);
            int swapItemSlot = getSlot(swapItem);

            if (!itemInHand(currentItem)) {
                if (stopWatch.isReached(100L)) {
                    swapToCurrentItem(currentItemSlot);
                    stopWatch.reset();
                }
                return;
            }

            swapBack(swapItemSlot);
            keyPressed = false;
        }
    }


    private void swapBack(int swapItemSlot) {
        if (this.stopWatch.isReached(400)) {
            swapToCurrentItem(swapItemSlot);
            stopWatch.reset();
        }
    }

    private void swapToCurrentItem(int slot) {
        if (slot != -1) {
            final CPlayerDiggingPacket.Action action = CPlayerDiggingPacket.Action.SWAP_ITEM_WITH_OFFHAND;
            final BlockPos blockPos = BlockPos.ZERO;
            final Direction direction = Direction.DOWN;
            final int currentItem = IMinecraft.mc.player.inventory.currentItem;
            if (slot > 8) {
                pickItemUtil.setOriginalSlot(currentItem);
                IMinecraft.mc.playerController.pickItem(slot);
                IMinecraft.mc.player.connection.sendPacketWithoutEvent(new CPlayerDiggingPacket(action, blockPos, direction));
                IMinecraft.mc.playerController.pickItem(slot);
                delay = System.currentTimeMillis();
            } else {
                if (currentItem != slot)
                    IMinecraft.mc.player.connection.sendPacketWithoutEvent(new CHeldItemChangePacket(slot));
                IMinecraft.mc.player.connection.sendPacketWithoutEvent(new CPlayerDiggingPacket(action, blockPos, direction));
                if (currentItem != slot)
                    IMinecraft.mc.player.connection.sendPacketWithoutEvent(new CHeldItemChangePacket(currentItem));
            }
        }
    }

    public boolean itemInHand(Item item) {
        return IMinecraft.mc.player.getHeldItemOffhand().getItem() == item;
    }

    public int getStackSlot(ItemStack targetItemStack, Item targetItem) {
        if (targetItemStack == null) {
            return -1;
        }

        for (int i = 0; i < 45; ++i) {
            ItemStack currentStack = IMinecraft.mc.player.inventory.getStackInSlot(i);
            if (ItemStack.areItemStacksEqual(currentStack, targetItemStack) && currentStack.getItem() == targetItem) {
                return i;
            }
        }

        return -1;
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        this.pickItemUtil.onEventPacket(e);

    }


    private Item getSwapItem() {
        return getItemByType(swapType.get());
    }

    private Item getSelectedItem() {
        return getItemByType(itemType.get());
    }

    private Item getItemByType(String itemType) {
        return switch (itemType) {
            case "���" -> Items.SHIELD;
            case "�����" -> Items.TOTEM_OF_UNDYING;
            case "�����" -> Items.GOLDEN_APPLE;
            case "���" -> Items.PLAYER_HEAD;
            default -> Items.AIR;
        };
    }

    private int getSlot(Item item) {
        int finalSlot = -1;
        for (int i = 0; i < 36; i++) {
            if (IMinecraft.mc.player.inventory.getStackInSlot(i).getItem() == item) {
                if (IMinecraft.mc.player.inventory.getStackInSlot(i).isEnchanted()) {
                    finalSlot = i;
                    break;
                } else {
                    finalSlot = i;
                }
            }
        }
        return finalSlot;
    }
}