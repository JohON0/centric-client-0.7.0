package centric.pl.functions.impl.misc;

import com.google.common.eventbus.Subscribe;
import centric.pl.events.impl.EventKey;
import centric.pl.events.impl.EventPacket;
import centric.pl.events.impl.EventUpdate;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.settings.impl.BindSetting;
import centric.pl.functions.settings.impl.BooleanSetting;
import centric.pl.johon0.utils.client.ClientUtil;
import centric.pl.johon0.utils.math.StopWatch;
import centric.pl.johon0.utils.player.InventoryUtil;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TextFormatting;

@FieldDefaults(level = AccessLevel.PRIVATE)
@FunctionRegister(name = "ElytraHelper", type = Category.Misc, beta = false)
public class ElytraHelper extends Function {

    final BindSetting swapChestKey = new BindSetting("Кнопка свапа", -1);
    final BindSetting fireWorkKey = new BindSetting("Кнопка феерверков", -1);
    final BooleanSetting autoFly = new BooleanSetting("Авто взлёт", true);
    final BooleanSetting onlyNotPVP = new BooleanSetting("Не использовать в PVP", true);
    final BooleanSetting bypassmw = new BooleanSetting("Bypass MusteryWorld", false);

    final InventoryUtil.Hand handUtil = new InventoryUtil.Hand();

    public ElytraHelper() {
        addSettings(swapChestKey, fireWorkKey, autoFly, onlyNotPVP,bypassmw);
    }

    ItemStack currentStack = ItemStack.EMPTY;
    public static StopWatch stopWatch = new StopWatch();
    long delay;
    boolean fireworkUsed;

    @Subscribe
    private void onEventKey(EventKey e) {

        if (isNotPvP()) {
            return;
        }

        if (e.getKey() == swapChestKey.get() && stopWatch.isReached(100L)) {
            changeChestPlate(currentStack);
            stopWatch.reset();
        }

        if (e.getKey() == fireWorkKey.get() && stopWatch.isReached(200L)) {
            if (bypassmw.get()) {
                useFirework();
            } else {
                fireworkUsed = true;
            }
            stopWatch.reset();
        }
    }
    private void useFirework() {
        if (InventoryUtil.getInstance().getSlotInInventory(Items.FIREWORK_ROCKET) == -1) {
            print("У вас отсутствуют фейерверки!");
        } else {
            InventoryUtil.inventorySwapClick(Items.FIREWORK_ROCKET, false);
        }
    }
    @Subscribe
    private void onUpdate(EventUpdate e) {
        ItemStack mouseStack = mc.player.inventory.getItemStack();



        this.currentStack = mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST);

        if (isNotPvP()) {
            return;
        }
        if (autoFly.get() && currentStack.getItem() == Items.ELYTRA) {
            if (mc.player.isOnGround()) {
                mc.player.jump();
            } else if (ElytraItem.isUsable(currentStack) && !mc.player.isElytraFlying()) {
                mc.player.startFallFlying();
                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
            }
        }


        if (fireworkUsed) {
            int hbSlot = InventoryUtil.getInstance().getSlotInInventoryOrHotbar(Items.FIREWORK_ROCKET, true);
            int invSlot = InventoryUtil.getInstance().getSlotInInventoryOrHotbar(Items.FIREWORK_ROCKET, false);


            if (invSlot == -1 && hbSlot == -1) {
                print("Феерверки не найдены!");
                fireworkUsed = false;
                return;
            }

            int slot = findAndTrowItem(hbSlot, invSlot);
            if (slot > 8) {
                mc.playerController.pickItem(slot);
            }

            fireworkUsed = false;
        }
        this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);

    }

    public boolean isNotPvP() {
        return onlyNotPVP.get() && ClientUtil.isPvP();
    }

    @Subscribe
    private void onPacket(EventPacket e) {
        handUtil.onEventPacket(e);
    }

    private int findAndTrowItem(int hbSlot, int invSlot) {
        if (hbSlot != -1) {
            this.handUtil.setOriginalSlot(mc.player.inventory.currentItem);
            if (hbSlot != mc.player.inventory.currentItem) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(hbSlot));
            }

            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));

            if (hbSlot != mc.player.inventory.currentItem) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
            }
            this.delay = System.currentTimeMillis();
            return hbSlot;
        }
        if (invSlot != -1) {
            handUtil.setOriginalSlot(mc.player.inventory.currentItem);
            mc.playerController.pickItem(invSlot);
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            this.delay = System.currentTimeMillis();
            return invSlot;
        }
        return -1;
    }


    private void changeChestPlate(ItemStack stack) {
        if (mc.currentScreen != null) {
            return;
        }

        if (stack.getItem() != Items.ELYTRA) {
            int elytraSlot = getItemSlot(Items.ELYTRA);
            if (elytraSlot >= 0) {
                InventoryUtil.moveItem(elytraSlot, 6);
                print(TextFormatting.RED + "Свапнул на элитру!");
                return;
            } else {
                print("Элитра не найдена!");
            }
        }
        int armorSlot = getChestPlateSlot();
        if (armorSlot >= 0 && stack.getItem() == Items.ELYTRA) {
            InventoryUtil.moveItem(armorSlot, 6);
            print(TextFormatting.RED + "Свапнул на нагрудник!");
        } else {
            print("Нагрудник не найден!");
        }
    }


    private int getChestPlateSlot() {
        Item[] items = {Items.NETHERITE_CHESTPLATE, Items.DIAMOND_CHESTPLATE};

        for (Item item : items) {
            for (int i = 0; i < 36; ++i) {
                Item stack = mc.player.inventory.getStackInSlot(i).getItem();
                if (stack == item) {
                    if (i < 9) {
                        i += 36;
                    }
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public void onDisable() {
        stopWatch.reset();
        super.onDisable();
    }

    private int getItemSlot(Item input) {
        int slot = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.inventory.getStackInSlot(i);
            if (s.getItem() == input) {
                slot = i;
                break;
            }
        }
        if (slot < 9 && slot != -1) {
            slot = slot + 36;
        }
        return slot;
    }
}
