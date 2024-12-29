package centric.pl.functions.impl.player;

import centric.pl.events.impl.EventPacket;
import centric.pl.events.impl.EventUpdate;
import centric.pl.events.impl.InventoryCloseEvent;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.johon0.utils.client.ClientUtil;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.math.StopWatch;
import centric.pl.johon0.utils.player.MoveUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.EditSignScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CClickWindowPacket;

import java.util.ArrayList;
import java.util.List;

@FunctionRegister(name = "InventoryMove", type = Category.Player, beta = false)
public class InventoryMove extends Function {

    private final List<IPacket<?>> packet = new ArrayList<>();

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (IMinecraft.mc.player != null) {

            final KeyBinding[] pressedKeys = {IMinecraft.mc.gameSettings.keyBindForward, IMinecraft.mc.gameSettings.keyBindBack,
                    IMinecraft.mc.gameSettings.keyBindLeft, IMinecraft.mc.gameSettings.keyBindRight, IMinecraft.mc.gameSettings.keyBindJump,
                    IMinecraft.mc.gameSettings.keyBindSprint};
            if (ClientUtil.isConnectedToServer("funtime")) {
                if (!wait.isReached(400)) {
                    for (KeyBinding keyBinding : pressedKeys) {
                        keyBinding.setPressed(false);
                    }
                    return;
                }
            }


            if (IMinecraft.mc.currentScreen instanceof ChatScreen || IMinecraft.mc.currentScreen instanceof EditSignScreen) {
                return;
            }

            updateKeyBindingState(pressedKeys);

        }
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (ClientUtil.isConnectedToServer("funtime")) {
            if (e.getPacket() instanceof CClickWindowPacket p && MoveUtils.isMoving()) {
                if (IMinecraft.mc.currentScreen instanceof InventoryScreen) {
                    packet.add(p);
                    e.cancel();
                }
            }
        }
    }

    public StopWatch wait = new StopWatch();

    @Subscribe
    public void onClose(InventoryCloseEvent e) {
        if (ClientUtil.isConnectedToServer("funtime")) {
            if (IMinecraft.mc.currentScreen instanceof InventoryScreen && !packet.isEmpty() && MoveUtils.isMoving()) {
                new Thread(() -> {
                    wait.reset();
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                    for (IPacket p : packet) {
                        IMinecraft.mc.player.connection.sendPacketWithoutEvent(p);
                    }
                    packet.clear();
                }).start();
                e.cancel();
            }
        }
    }

    private void updateKeyBindingState(KeyBinding[] keyBindings) {
        for (KeyBinding keyBinding : keyBindings) {
            boolean isKeyPressed = InputMappings.isKeyDown(IMinecraft.mc.getMainWindow().getHandle(), keyBinding.getDefault().getKeyCode());
            keyBinding.setPressed(isKeyPressed);
        }
    }
}
