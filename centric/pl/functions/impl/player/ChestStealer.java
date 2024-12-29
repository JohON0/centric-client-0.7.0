package centric.pl.functions.impl.player;

import centric.pl.events.impl.EventUpdate;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.settings.impl.BooleanSetting;
import centric.pl.functions.settings.impl.SliderSetting;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.math.StopWatch;
import com.google.common.eventbus.Subscribe;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@FieldDefaults(level = AccessLevel.PRIVATE)
@FunctionRegister(name = "ChestStealer", type = Category.Player, beta = false)
public class ChestStealer extends Function {
    final BooleanSetting mw = new BooleanSetting("Подбирать автоматически", true);
    final SliderSetting delay = new SliderSetting("Задержка", 100.0f, 0.0f, 1000.0f, 1.0f).setVisible(() -> !mw.get());



    public ChestStealer() {
        addSettings(mw,delay);
    }

    final StopWatch stopWatch = new StopWatch();



    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (IMinecraft.mc.player.openContainer instanceof ChestContainer container) {
            IInventory lowerChestInventory = container.getLowerChestInventory();
            for (int index = 0; index < lowerChestInventory.getSizeInventory(); ++index) {
                ItemStack stack = lowerChestInventory.getStackInSlot(index);
                if (!shouldMoveItem(container, index)) {
                    continue;
                }
                if (delay.get() == 0.0f) {
                    moveItem(container, index, lowerChestInventory.getSizeInventory());
                } else {
                    if (stopWatch.isReached(mw.get() ? 60 : delay.get().longValue())) {
                        IMinecraft.mc.playerController.windowClick(container.windowId, index, 0, ClickType.QUICK_MOVE, IMinecraft.mc.player);
                        stopWatch.reset();
                    }
                }
            }
        }
    }

    private boolean shouldMoveItem(ChestContainer container, int index) {
        ItemStack itemStack = container.getLowerChestInventory().getStackInSlot(index);
        return itemStack.getItem() != Item.getItemById(0);
    }

    private void moveItem(ChestContainer container, int index, int multi) {
        for (int i = 0; i < multi; i++) {
            IMinecraft.mc.playerController.windowClick(container.windowId, index + i, 0, ClickType.QUICK_MOVE, IMinecraft.mc.player);
        }
    }
}
