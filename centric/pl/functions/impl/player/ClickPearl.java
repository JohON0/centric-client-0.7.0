package centric.pl.functions.impl.player;

import centric.pl.Main;
import centric.pl.events.impl.EventKey;
import centric.pl.events.impl.EventMotion;
import centric.pl.events.impl.EventPacket;
import centric.pl.events.impl.EventUpdate;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.api.FunctionRegistry;
import centric.pl.functions.settings.impl.BindSetting;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.player.InventoryUtil;
import com.google.common.eventbus.Subscribe;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;

@FieldDefaults(level = AccessLevel.PRIVATE)
@FunctionRegister(name = "ClickPearl", type = Category.Player, beta = false)
public class ClickPearl extends Function {
    final BindSetting throwKey = new BindSetting("Êíîïêà", -98);
    final InventoryUtil.Hand handUtil = new InventoryUtil.Hand();

    long delay;
    boolean throwPearl;

    public ClickPearl(ItemCooldown itemCooldown) {
        addSettings(throwKey);
    }

    @Subscribe
    public void onKey(EventKey e) {
        throwPearl = e.getKey() == throwKey.get();
    }

    @Subscribe
    private void onMotion(EventMotion e) {
        if (throwPearl) {
            if (!IMinecraft.mc.player.getCooldownTracker().hasCooldown(Items.ENDER_PEARL)) {
                boolean isOffhandEnderPearl = IMinecraft.mc.player.getHeldItemOffhand().getItem() instanceof EnderPearlItem;
                if (isOffhandEnderPearl) {
                    IMinecraft.mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                    IMinecraft.mc.player.swingArm(Hand.MAIN_HAND);
                } else {
                    int slot = findPearlAndThrow();
                    if (slot > 8) {
                        IMinecraft.mc.playerController.pickItem(slot);
                    }
                }
            }
            throwPearl = false;
        }
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
                this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);
    }

    @Subscribe
    private void onPacket(EventPacket e) {
        this.handUtil.onEventPacket(e);
    }

    private int findPearlAndThrow() {
        int hbSlot = InventoryUtil.getInstance().getSlotInInventoryOrHotbar(Items.ENDER_PEARL, true);
        if (hbSlot != -1) {
            if (hbSlot != IMinecraft.mc.player.inventory.currentItem) {
                IMinecraft.mc.player.connection.sendPacket(new CHeldItemChangePacket(hbSlot));
            }
            IMinecraft.mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            IMinecraft.mc.player.swingArm(Hand.MAIN_HAND);

            FunctionRegistry functionRegistry = Main.getInstance().getFunctionRegistry();
            ItemCooldown itemCooldown = functionRegistry.getItemCooldown();
            ItemCooldown.ItemEnum itemEnum = ItemCooldown.ItemEnum.getItemEnum(Items.ENDER_PEARL);

            if (itemCooldown.isState() && itemEnum != null && itemCooldown.isCurrentItem(itemEnum)) {
                itemCooldown.lastUseItemTime.put(itemEnum.getItem(), System.currentTimeMillis());
            }

            if (hbSlot != IMinecraft.mc.player.inventory.currentItem) {
                IMinecraft.mc.player.connection.sendPacket(new CHeldItemChangePacket(IMinecraft.mc.player.inventory.currentItem));
            }
            this.delay = System.currentTimeMillis();
            return hbSlot;
        }

        int invSlot = InventoryUtil.getInstance().getSlotInInventoryOrHotbar(Items.ENDER_PEARL, false);

        if (invSlot != -1) {
            handUtil.setOriginalSlot(IMinecraft.mc.player.inventory.currentItem);
            IMinecraft.mc.playerController.pickItem(invSlot);
            IMinecraft.mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            IMinecraft.mc.player.swingArm(Hand.MAIN_HAND);

            FunctionRegistry functionRegistry = Main.getInstance().getFunctionRegistry();
            ItemCooldown itemCooldown = functionRegistry.getItemCooldown();
            ItemCooldown.ItemEnum itemEnum = ItemCooldown.ItemEnum.getItemEnum(Items.ENDER_PEARL);

            if (itemCooldown.isState() && itemEnum != null && itemCooldown.isCurrentItem(itemEnum)) {
                itemCooldown.lastUseItemTime.put(itemEnum.getItem(), System.currentTimeMillis());
            }
            this.delay = System.currentTimeMillis();
            return invSlot;
        }
        return -1;
    }


    @Override
    public void onDisable() {
        throwPearl = false;
        delay = 0;
        super.onDisable();
    }
}
