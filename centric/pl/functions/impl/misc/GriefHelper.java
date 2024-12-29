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
import centric.pl.functions.settings.impl.ModeListSetting;
import centric.pl.johon0.utils.math.StopWatch;
import centric.pl.johon0.utils.player.InventoryUtil;
import net.minecraft.item.AirItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TextFormatting;


@FunctionRegister(name = "GriefHelper", type = Category.Misc, beta = false)
public class GriefHelper extends Function {


    private final ModeListSetting mode = new ModeListSetting("Тип",
            new BooleanSetting("Использование по бинду", true),
            new BooleanSetting("Закрывать меню", true));


    private final BindSetting disorientationKey = new BindSetting("Кнопка дезориентации", -1)
            .setVisible(() -> mode.getValueByName("Использование по бинду").get());
    private final BindSetting trapKey = new BindSetting("Кнопка трапки", -1)
            .setVisible(() -> mode.getValueByName("Использование по бинду").get());
    private final BindSetting plastkey = new BindSetting("Кнопка Пласта", -1)
            .setVisible(() -> mode.getValueByName("Использование по бинду").get());
    private final BooleanSetting bypassmw = new BooleanSetting("Bypass MusteryWorld", false);
    final StopWatch stopWatch = new StopWatch();

    InventoryUtil.Hand handUtil = new InventoryUtil.Hand();
    long delay;
    boolean disorientationThrow, trapThrow,plastThrow;

    public GriefHelper() {
        addSettings(mode, disorientationKey, trapKey,bypassmw);
    }

    @Subscribe
    private void onKey(EventKey e) {
        if (e.getKey() == disorientationKey.get()) {
            disorientationThrow = true;
        }
        if (e.getKey() == trapKey.get()) {
            trapThrow = true;
        }
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (plastThrow) {
            if (plastThrow) {
                this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);
                int hbSlot = getItemForName("дезориентация", true);
                int invSlot = getItemForName("дезориентация", false);
                if (bypassmw.get() && stopWatch.isReached(200L)) {
                    useampula();
                    stopWatch.reset();
                }
                if (!bypassmw.get())
                    if (invSlot == -1 && hbSlot == -1) {
                        print("Дезориентация не найдена!");
                        plastThrow = false;
                        return;
                    }
                if (!bypassmw.get())
                    if (!mc.player.getCooldownTracker().hasCooldown(Items.ENDER_EYE)) {
                        print("Заюзал Дезориентацию!");
                        int slot = findAndTrowItem(hbSlot, invSlot);
                        if (slot > 8) {
                            mc.playerController.pickItem(slot);
                        }
                    }
            }
            plastThrow = false;
        }
        if (disorientationThrow) {
            this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);
            int hbSlot = getItemForName("дезориентация", true);
            int invSlot = getItemForName("дезориентация", false);
            if (bypassmw.get() && stopWatch.isReached(200L)) {
                useampula();
                stopWatch.reset();
            }
            if (!bypassmw.get())
            if (invSlot == -1 && hbSlot == -1) {
                print("Дезориентация не найдена!");
                disorientationThrow = false;
                return;
            }
            if (!bypassmw.get())
                if (!mc.player.getCooldownTracker().hasCooldown(Items.ENDER_EYE)) {
                    print("Заюзал Дезориентацию!");
                    int slot = findAndTrowItem(hbSlot, invSlot);
                    if (slot > 8) {
                        mc.playerController.pickItem(slot);
                    }
                }
            }
            disorientationThrow = false;
        if (trapThrow) {
            int hbSlot = getItemForName("трапка", true);
            int invSlot = getItemForName("трапка", false);
            if (bypassmw.get() && stopWatch.isReached(200L)) {
                useTrap();

                stopWatch.reset();
            }
            if (!bypassmw.get())
            if (invSlot == -1 && hbSlot == -1) {
                print("Трапка не найдена");
                trapThrow = false;
                return;
            }
            if (!bypassmw.get())
                if (!mc.player.getCooldownTracker().hasCooldown(Items.NETHERITE_SCRAP)) {
                    print("Заюзал трапку!");
                    int old = mc.player.inventory.currentItem;

                    int slot = findAndTrowItem(hbSlot, invSlot);
                    if (slot > 8) {
                        mc.playerController.pickItem(slot);
                    }
                    if (InventoryUtil.findEmptySlot(true) != -1 && mc.player.inventory.currentItem != old) {
                        mc.player.inventory.currentItem = old;
                    }
                }
                trapThrow = false;
            }
            this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);

    }

    @Subscribe
    private void onPacket(EventPacket e) {
        this.handUtil.onEventPacket(e);
    }

    private int findAndTrowItem(int hbSlot, int invSlot) {
        if (hbSlot != -1) {
            if (hbSlot != mc.player.inventory.currentItem) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(hbSlot));
            }
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            mc.player.swingArm(Hand.MAIN_HAND);

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
            mc.player.swingArm(Hand.MAIN_HAND);
            this.delay = System.currentTimeMillis();
            return invSlot;
        }
        return -1;
    }

    @Override
    public void onDisable() {
        disorientationThrow = false;
        trapThrow = false;
        delay = 0;
        super.onDisable();
    }
    private void useTrap() {
        if (InventoryUtil.getInstance().getSlotInInventory(Items.NETHERITE_SCRAP) == -1) {
            print(TextFormatting.RED + "У вас отсутствуют трапка!");
        } else {
            InventoryUtil.inventorySwapClick(Items.NETHERITE_SCRAP, false);
            print("Заюзал трапку");
        }
    }
    private void useampula() {
        if (InventoryUtil.getInstance().getSlotInInventory(Items.ENDER_EYE) == -1) {
            print(TextFormatting.RED + "У вас отсутствуют Ампула!");
        } else {
            InventoryUtil.inventorySwapClick(Items.ENDER_EYE, false);
            print("Заюзал ампулу");
        }
    }
    private int getItemForName(String name, boolean inHotBar) {
        int firstSlot = inHotBar ? 0 : 9;
        int lastSlot = inHotBar ? 9 : 36;
        for (int i = firstSlot; i < lastSlot; i++) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);

            if (itemStack.getItem() instanceof AirItem) {
                continue;
            }

            String displayName = TextFormatting.getTextWithoutFormattingCodes(itemStack.getDisplayName().getString());
            if (displayName != null && displayName.toLowerCase().contains(name)) {
                return i;
            }
        }
        return -1;
    }
}
