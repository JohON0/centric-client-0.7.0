package centric.pl.functions.impl.movement;

import centric.pl.events.impl.*;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.impl.combat.KillAura;
import centric.pl.functions.settings.impl.SliderSetting;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.player.DamagePlayerUtil;
import centric.pl.johon0.utils.player.MoveUtils;
import centric.pl.johon0.utils.player.StrafeMovement;
import com.google.common.eventbus.Subscribe;
//import centric.pl.functions.impl.combat.KillAura;
import centric.pl.functions.settings.impl.BooleanSetting;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoulSandBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;


@FunctionRegister(name = "TargetStrafe", type = Category.Movement, beta = false)
public class TargetStrafe extends Function {

    private final SliderSetting distanceSetting = new SliderSetting("Дистанция", 1f, 0.1f, 6f, 0.05f);
    private final BooleanSetting damageBoostSetting = new BooleanSetting("Буст с дамагом", true);
    private final SliderSetting boostValueSetting = new SliderSetting("Значение буста", 1.5F, 0.1f, 5.0f, 0.05f);
    private final SliderSetting timeSetting = new SliderSetting("Время буста", 10.0f, 1.0f, 20.0f, 1.0f);
    private final BooleanSetting saveTarget = new BooleanSetting("Сохранять цель", true);


    private float side = 1;
    private LivingEntity target = null;
    private final DamagePlayerUtil damageUtil = new DamagePlayerUtil();
    private String targetName = "";
    public StrafeMovement strafeMovement = new StrafeMovement();
    private final KillAura killAura;

    public TargetStrafe(KillAura killAura) {
        this.killAura = killAura;
        addSettings(distanceSetting, damageBoostSetting, timeSetting, saveTarget);
    }

    @Subscribe
    private void onAction(ActionEvent e) {
        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null) return;
        handleEventAction(e);
    }

    @Subscribe
    public void onMotion(final MovingEvent event) {

        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null || IMinecraft.mc.player.ticksExisted < 10) return;
        boolean isLeftKeyPressed = InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_KEY_A);
        boolean isRightKeyPressed = InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_KEY_D);

        LivingEntity auraTarget = getTarget();

        if (auraTarget != null) {
            targetName = auraTarget.getName().getString();
        }

        if (shouldSaveTarget(auraTarget)) {
            target = updateTarget(target);
        } else target = auraTarget;

        if (target != null && target.isAlive() && target.getHealth() > 0.0f) {
            if (IMinecraft.mc.player.collidedHorizontally) side *= -1;
            if (isLeftKeyPressed) side = 1;
            if (isRightKeyPressed) side = -1;
            double angle = Math.atan2(IMinecraft.mc.player.getPosZ() - target.getPosZ(), IMinecraft.mc.player.getPosX() - target.getPosX());
            angle += MoveUtils.getMotion() / Math.max(IMinecraft.mc.player.getDistance(target), distanceSetting.min) * side;
            double x = target.getPosX() + distanceSetting.get() * Math.cos(angle);
            double z = target.getPosZ() + distanceSetting.get() * Math.sin(angle);

            double yaw = getYaw(IMinecraft.mc.player, x, z);

            this.damageUtil.time(timeSetting.get().longValue() * 100);

            final float damageSpeed = boostValueSetting.get() / 10.0F;

            final double speed = strafeMovement.calculateSpeed(event, damageBoostSetting.get(),
                    damageUtil.isNormalDamage(),
                    true, damageSpeed);

            event.getMotion().x = (speed * -Math.sin(Math.toRadians(yaw)));
            event.getMotion().z = (speed * Math.cos(Math.toRadians(yaw)));
        }
    }

    @Subscribe
    private void onPostMove(PostMoveEvent e) {
        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null) return;
        strafeMovement.postMove(e.getHorizontalMove());
    }

    @Subscribe
    private void onPacket(EventPacket e) {
        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null) return;
        if (e.getType() == EventPacket.Type.RECEIVE) {
            damageUtil.onPacketEvent(e);
            if (e.getPacket() instanceof SPlayerPositionLookPacket) {
                strafeMovement.setOldSpeed(0);
            }
        }
    }

    @Subscribe
    private void onDamage(EventDamageReceive e) {
        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null) return;
        damageUtil.processDamage(e);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (IMinecraft.mc.player.isOnGround() && !IMinecraft.mc.gameSettings.keyBindJump.pressed && target != null && target.isAlive()) {
            IMinecraft.mc.player.jump();
        }
    }

    @Override
    public void onEnable() {
        strafeMovement.setOldSpeed(0);
        target = null;
        super.onEnable();
    }

    private void handleEventAction(ActionEvent action) {
        if (strafes()) {
            if (CEntityActionPacket.lastUpdatedSprint != strafeMovement.isNeedSprintState()) {
                action.setSprintState(!CEntityActionPacket.lastUpdatedSprint);
            }
        }
        if (strafeMovement.isNeedSwap()) {
            action.setSprintState(!IMinecraft.mc.player.serverSprintState);
            strafeMovement.setNeedSprintState(false);
        }
    }

    private LivingEntity getTarget() {
        return killAura.isState() ? killAura.getTarget() : null;
    }

    private LivingEntity updateTarget(LivingEntity currentTarget) {
        for (Entity entity : IMinecraft.mc.world.getAllEntities()) {
            if (entity instanceof PlayerEntity && entity.getName().getString().equalsIgnoreCase(targetName)) {
                return (LivingEntity) entity;
            }
        }
        return currentTarget;
    }

    private boolean shouldSaveTarget(LivingEntity target) {
        boolean settingIsEnabled = saveTarget.get();
        boolean targetAndTargetNameExist = target != null && targetName != null;

        return settingIsEnabled && targetAndTargetNameExist && killAura.isState();
    }

    private double getYaw(LivingEntity entity, double x, double z) {
        return Math.toDegrees(Math.atan2(z - entity.getPosZ(), x - entity.getPosX())) - 90F;
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

}
