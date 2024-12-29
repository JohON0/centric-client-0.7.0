package centric.pl.functions.impl.movement;

import centric.pl.events.impl.EventPacket;
import centric.pl.events.impl.EventUpdate;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.settings.impl.ModeSetting;
import centric.pl.functions.settings.impl.SliderSetting;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.player.MoveUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CConfirmTeleportPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.math.vector.Vector3d;

@FunctionRegister(name = "Fly", type = Category.Movement, beta = false)
public class Fly extends Function {

    private final ModeSetting mode = new ModeSetting("Мод", "Vanilla", "Vanilla", "Matrix Jump", "Matrix Glide",
            "GrimAC");
    private final SliderSetting horizontal = new SliderSetting("По горизонтали", 0.5f, 0f, 5f, 0.1f);
    private final SliderSetting vertical = new SliderSetting("По вертикали", 0.5f, 0f, 5f, 0.1f);

    public Fly() {
        addSettings(mode, horizontal, vertical);
    }

    public Entity vehicle;

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null)
            return;

        switch (mode.getIndex()) {
            case 0 -> {
                updatePlayerMotion();
            }

            case 1 -> { // Matrix Jump
                if (IMinecraft.mc.player.isOnGround())
                    IMinecraft.mc.player.jump();
                else {
                    MoveUtils.setMotion(Math.min(horizontal.get(), 1.97f));
                    IMinecraft.mc.player.motion.y = vertical.get();
                }
            }

            case 2 -> { // Matrix Glide
                IMinecraft.mc.player.motion = Vector3d.ZERO;
                MoveUtils.setMotion(horizontal.get());
                IMinecraft.mc.player.setMotion(IMinecraft.mc.player.getMotion().x, -0.003, IMinecraft.mc.player.getMotion().z);
            }

            case 3 -> { // GrimAC
                for (Entity en : IMinecraft.mc.world.getAllEntities()) {
                    if (en instanceof BoatEntity) {
                        if (IMinecraft.mc.player.getDistance(en) <= 2) {
                            MoveUtils.setMotion(1.2f);
                            IMinecraft.mc.player.motion.y = 1;
                            break;
                        }
                    }
                }
            }

            case 4 -> { // GrimAC Elytra
                if (IMinecraft.mc.player.ticksExisted % 2 != 0) return;

                int slot = -1;

                for (ItemStack stack : IMinecraft.mc.player.inventory.mainInventory) {
                    if (stack.getItem() instanceof ElytraItem) {
                        slot = IMinecraft.mc.player.inventory.mainInventory.indexOf(stack);
                    }
                }

                IMinecraft.mc.player.abilities.isFlying = false;

                if (slot == -1) return;

                int chestSlot = 6;

                IMinecraft.mc.playerController.windowClick(0, slot, 0, ClickType.PICKUP, IMinecraft.mc.player);
                IMinecraft.mc.playerController.windowClick(0, chestSlot, 0, ClickType.PICKUP, IMinecraft.mc.player);
                IMinecraft.mc.playerController.windowClick(0, slot, 0, ClickType.PICKUP, IMinecraft.mc.player);

                IMinecraft.mc.player.connection.sendPacket(new CEntityActionPacket(IMinecraft.mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
                IMinecraft.mc.player.abilities.isFlying = true;

                IMinecraft.mc.playerController.windowClick(0, slot, 0, ClickType.PICKUP, IMinecraft.mc.player);
                IMinecraft.mc.playerController.windowClick(0, chestSlot, 0, ClickType.PICKUP, IMinecraft.mc.player);
                IMinecraft.mc.playerController.windowClick(0, slot, 0, ClickType.PICKUP, IMinecraft.mc.player);
            }
        }
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null)
            return;

        switch (mode.getIndex()) {
            case 1 -> { // Matrix Jump
                if (e.getPacket() instanceof SPlayerPositionLookPacket p) {
                    if (IMinecraft.mc.player == null)
                        toggle();
                    IMinecraft.mc.player.setPosition(p.getX(), p.getY(), p.getZ());
                    IMinecraft.mc.player.connection.sendPacket(new CConfirmTeleportPacket(p.getTeleportId()));
                    e.cancel();
                    toggle();
                }
            }
            case 3 -> { // GrimAC
                if (e.getPacket() instanceof SPlayerPositionLookPacket p) {

                    toggle();
                }
            }
        }
    }

    private void updatePlayerMotion() {
        double motionX = IMinecraft.mc.player.getMotion().x;
        double motionY = getMotionY();
        double motionZ = IMinecraft.mc.player.getMotion().z;

//            MoveUtils.setMotion(horizontal.get());
        IMinecraft.mc.player.motion.y = motionY;
    }

    private double getMotionY() {
        return IMinecraft.mc.gameSettings.keyBindSneak.pressed ? -vertical.get()
                : IMinecraft.mc.gameSettings.keyBindJump.pressed ? vertical.get() : 0;
    }

    @Override
    public void onDisable() {
        super.onDisable();

        IMinecraft.mc.player.abilities.isFlying = false;
    }
}
