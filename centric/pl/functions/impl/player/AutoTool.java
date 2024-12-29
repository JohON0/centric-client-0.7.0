package centric.pl.functions.impl.player;

import centric.pl.events.impl.EventUpdate;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.settings.impl.BooleanSetting;
import centric.pl.johon0.utils.client.IMinecraft;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.Block;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.util.math.BlockRayTraceResult;

@FunctionRegister(name = "AutoTool", type = Category.Player, beta = false)
public class AutoTool extends Function {

    public final BooleanSetting silent = new BooleanSetting("Незаметный", true);

    public int itemIndex = -1, oldSlot = -1;
    boolean status;
    boolean clicked;
    public AutoTool() {
        addSettings(silent);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (IMinecraft.mc.player == null || IMinecraft.mc.player.isCreative()) {
            itemIndex = -1;
            return;
        }

        if (isMousePressed()) {
            itemIndex = findBestToolSlotInHotBar();
            if (itemIndex != -1) {
                status = true;

                if (oldSlot == -1) {
                    oldSlot = IMinecraft.mc.player.inventory.currentItem;
                }

                if (silent.get()) {
                    IMinecraft.mc.player.connection.sendPacket(new CHeldItemChangePacket(itemIndex));
                } else {
                    IMinecraft.mc.player.inventory.currentItem = itemIndex;
                }
            }
        } else if (status && oldSlot != -1) {
            if (silent.get()) {
                IMinecraft.mc.player.connection.sendPacket(new CHeldItemChangePacket(oldSlot));
            } else {
                IMinecraft.mc.player.inventory.currentItem = oldSlot;
            }

            itemIndex = oldSlot;
            status = false;
            oldSlot = -1;
        }
    }

    @Override
    public void onDisable() {
        status = false;
        itemIndex = -1;
        oldSlot = -1;
        super.onDisable();
    }

    private int findBestToolSlotInHotBar() {
        if (IMinecraft.mc.objectMouseOver instanceof BlockRayTraceResult blockRayTraceResult) {
            Block block = IMinecraft.mc.world.getBlockState(blockRayTraceResult.getPos()).getBlock();

            int bestSlot = -1;
            float bestSpeed = 1.0f;

            for (int slot = 0; slot < 9; slot++) {
                float speed = IMinecraft.mc.player.inventory.getStackInSlot(slot)
                        .getDestroySpeed(block.getDefaultState());

                if (speed > bestSpeed) {
                    bestSpeed = speed;
                    bestSlot = slot;
                }
            }
            return bestSlot;
        }
        return -1;
    }


    private boolean isMousePressed() {
        return IMinecraft.mc.objectMouseOver != null && IMinecraft.mc.gameSettings.keyBindAttack.isKeyDown();
    }
}
