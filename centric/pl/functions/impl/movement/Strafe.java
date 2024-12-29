package centric.pl.functions.impl.movement;

import centric.pl.events.impl.*;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.impl.combat.KillAura;
import centric.pl.functions.settings.impl.BooleanSetting;
import centric.pl.functions.settings.impl.SliderSetting;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.player.DamagePlayerUtil;
import centric.pl.johon0.utils.player.MoveUtils;
import centric.pl.johon0.utils.player.StrafeMovement;
import com.google.common.eventbus.Subscribe;
//import centric.pl.functions.impl.combat.KillAura;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoulSandBlock;
import net.minecraft.block.material.Material;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;


@FunctionRegister(name = "Strafe", type = Category.Movement, beta = false)
public class Strafe extends Function {
    private final BooleanSetting damageBoost = new BooleanSetting("Буст с дамагом", false);

    private final SliderSetting boostSpeed = new SliderSetting("Значение буста", 0.7f, 0.1F, 5.0f, 0.1F);

    private final DamagePlayerUtil damageUtil = new DamagePlayerUtil();
    private final StrafeMovement strafeMovement = new StrafeMovement();

    private final TargetStrafe targetStrafe;
    private final KillAura killAura;

    public Strafe(TargetStrafe targetStrafe, KillAura killAura) {
        this.targetStrafe = targetStrafe;
        this.killAura = killAura;
        addSettings(damageBoost, boostSpeed);
    }

    @Subscribe
    private void onAction(ActionEvent e) {
        handleEventAction(e);
    }

    @Subscribe
    private void onMoving(MovingEvent e) {
        handleEventMove(e);
    }

    @Subscribe
    private void onPostMove(PostMoveEvent e) {
        handleEventPostMove(e);
    }

    @Subscribe
    private void onPacket(EventPacket e) {
        handleEventPacket(e);
    }

    @Subscribe
    private void onDamage(EventDamageReceive e) {
        handleDamageEvent(e);
    }

    private void handleDamageEvent(EventDamageReceive damage) {
        if (damageBoost.get()) {
            damageUtil.processDamage(damage);
        }
    }

    private void handleEventAction(ActionEvent action) {
        if (strafes()) {
            handleStrafesEventAction(action);
        }
        if (strafeMovement.isNeedSwap()) {
            handleNeedSwapEventAction(action);
        }
    }

    private void handleEventMove(MovingEvent eventMove) {
        if (strafes()) {
            handleStrafesEventMove(eventMove);
        } else {
            strafeMovement.setOldSpeed(0);
        }
    }

    private void handleEventPostMove(PostMoveEvent eventPostMove) {
        strafeMovement.postMove(eventPostMove.getHorizontalMove());
    }

    private void handleEventPacket(EventPacket packet) {

        if (packet.getType() == EventPacket.Type.RECEIVE) {
            if (damageBoost.get()) {
                damageUtil.onPacketEvent(packet);
            }
            handleReceivePacketEventPacket(packet);
        }
    }

    private void handleStrafesEventAction(ActionEvent action) {
        if (CEntityActionPacket.lastUpdatedSprint != strafeMovement.isNeedSprintState()) {
            action.setSprintState(!CEntityActionPacket.lastUpdatedSprint);
        }
    }

    private void handleStrafesEventMove(MovingEvent eventMove) {
        if (targetStrafe.isState() && (killAura.isState() && killAura.getTarget() != null)) {
            return;
        }


        if (damageBoost.get())
            this.damageUtil.time(700L);

        final float damageSpeed = boostSpeed.get().floatValue() / 10.0F;
        final double speed = strafeMovement.calculateSpeed(eventMove, damageBoost.get(), damageUtil.isNormalDamage(), false, damageSpeed);

        MoveUtils.MoveEvent.setMoveMotion(eventMove, speed);
    }

    private void handleNeedSwapEventAction(ActionEvent action) {
        action.setSprintState(!IMinecraft.mc.player.serverSprintState);
        strafeMovement.setNeedSwap(false);
    }

    private void handleReceivePacketEventPacket(EventPacket packet) {
        if (packet.getPacket() instanceof SPlayerPositionLookPacket) {
            strafeMovement.setOldSpeed(0);
        }

    }

    public boolean strafes() {
        if (isInvalidPlayerState()) {
            return false;
        }

        BlockPos playerPosition = new BlockPos(IMinecraft.mc.player.getPositionVec());
        BlockPos abovePosition = playerPosition.up();
        BlockPos belowPosition = playerPosition.down();

        if (isSurfaceLiquid(abovePosition, belowPosition)) {
            return false;
        }

        if (isPlayerInWebOrSoulSand(playerPosition)) {
            return false;
        }

        return isPlayerAbleToStrafe();
    }

    private boolean isInvalidPlayerState() {
        return IMinecraft.mc.player == null || IMinecraft.mc.world == null
                || IMinecraft.mc.player.isSneaking()
                || IMinecraft.mc.player.isElytraFlying()
                || IMinecraft.mc.player.isInWater()
                || IMinecraft.mc.player.isInLava();
    }

    private boolean isSurfaceLiquid(BlockPos abovePosition, BlockPos belowPosition) {
        Block aboveBlock = IMinecraft.mc.world.getBlockState(abovePosition).getBlock();
        Block belowBlock = IMinecraft.mc.world.getBlockState(belowPosition).getBlock();

        return aboveBlock instanceof AirBlock && belowBlock == Blocks.WATER;
    }

    private boolean isPlayerInWebOrSoulSand(BlockPos playerPosition) {
        Material playerMaterial = IMinecraft.mc.world.getBlockState(playerPosition).getMaterial();
        Block oneBelowBlock = IMinecraft.mc.world.getBlockState(playerPosition.down()).getBlock();

        return playerMaterial == Material.WEB || oneBelowBlock instanceof SoulSandBlock;
    }

    private boolean isPlayerAbleToStrafe() {
        return !IMinecraft.mc.player.abilities.isFlying && !IMinecraft.mc.player.isPotionActive(Effects.LEVITATION);
    }

    @Override
    public void onEnable() {
        strafeMovement.setOldSpeed(0);
        super.onEnable();
    }
}
