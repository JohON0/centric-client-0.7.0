package centric.pl.functions.impl.movement;

import centric.pl.events.impl.EventPacket;
import centric.pl.events.impl.EventUpdate;
import centric.pl.events.impl.MovingEvent;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.math.StopWatch;
import centric.pl.johon0.utils.player.InventoryUtil;
import centric.pl.johon0.utils.player.MouseUtil;
import centric.pl.johon0.utils.player.MoveUtils;
import com.google.common.eventbus.Subscribe;
import centric.pl.functions.settings.impl.ModeSetting;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.Pose;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;

@FunctionRegister(name = "LongJump", type = Category.Movement, beta = false)
public class LongJump extends Function {

    boolean placed;
    int counter;

    public ModeSetting mod = new ModeSetting("Мод", "Slap", "Slap");

    public LongJump() {
        addSettings(mod);
    }
    StopWatch stopWatch = new StopWatch();
    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mod.is("Slap") && !IMinecraft.mc.player.isInWater()) {

            int slot = InventoryUtil.getSlotInInventoryOrHotbar();
            if (slot == -1) {
                print("У вас нет полублоков в хотбаре!");
                toggle();
                return;
            }
            int old = IMinecraft.mc.player.inventory.currentItem;

            if (MouseUtil.rayTraceResult(2, IMinecraft.mc.player.rotationYaw, 90, IMinecraft.mc.player) instanceof BlockRayTraceResult result) {
                if (MoveUtils.isMoving()) {
                    if (IMinecraft.mc.player.fallDistance >= 0.8 && IMinecraft.mc.world.getBlockState(IMinecraft.mc.player.getPosition()).isAir() && !IMinecraft.mc.world.getBlockState(result.getPos()).isAir() && IMinecraft.mc.world.getBlockState(result.getPos()).isSolid() && !(IMinecraft.mc.world.getBlockState(result.getPos()).getBlock() instanceof SlabBlock) && !(IMinecraft.mc.world.getBlockState(result.getPos()).getBlock() instanceof StairsBlock)) {

                        IMinecraft.mc.player.inventory.currentItem = slot;
                        placed = true;
                        IMinecraft.mc.playerController.processRightClickBlock(IMinecraft.mc.player, IMinecraft.mc.world, Hand.MAIN_HAND, result);
                        IMinecraft.mc.player.inventory.currentItem = old;
                        IMinecraft.mc.player.fallDistance = 0;
                    }
                    IMinecraft.mc.gameSettings.keyBindJump.pressed = false;


                    if ((IMinecraft.mc.player.isOnGround() && !IMinecraft.mc.gameSettings.keyBindJump.pressed)
                            && placed
                            && IMinecraft.mc.world.getBlockState(IMinecraft.mc.player.getPosition()).isAir()
                            && !IMinecraft.mc.world.getBlockState(result.getPos()).isAir()
                            && IMinecraft.mc.world.getBlockState(result.getPos()).isSolid()
                            && !(IMinecraft.mc.world.getBlockState(result.getPos()).getBlock() instanceof SlabBlock) && stopWatch.isReached(750)) {

                        IMinecraft.mc.player.setPose(Pose.STANDING);



                        stopWatch.reset();
                        placed = false;
                    } else if ((IMinecraft.mc.player.isOnGround() && !IMinecraft.mc.gameSettings.keyBindJump.pressed)) {
                        IMinecraft.mc.player.jump();
                        placed = false;
                    }
                }
            } else {
                if ((IMinecraft.mc.player.isOnGround() && !IMinecraft.mc.gameSettings.keyBindJump.pressed)) {
                    IMinecraft.mc.player.jump();
                    placed = false;
                }
            }
        }
    }
    @Subscribe
    public void onMoving(MovingEvent e) {
    }
    @Subscribe
    public void onFlag(EventPacket e) {
        if (e.getPacket() instanceof SPlayerPositionLookPacket p) {
            placed = false;
            counter = 0;
            IMinecraft.mc.player.setPose(Pose.STANDING);
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        counter = 0;
        placed = false;
    }


}
