package centric.pl.functions.impl.combat;

import centric.pl.events.impl.EventUpdate;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.settings.impl.BooleanSetting;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.math.StopWatch;
import centric.pl.johon0.utils.player.AttackUtil;
import centric.pl.johon0.utils.player.InventoryUtil;
import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;

@FunctionRegister(name = "TriggerBot", type = Category.Combat, beta = false)
public class TriggerBot extends Function {

    private final BooleanSetting players = new BooleanSetting("Игроки", true);
    private final BooleanSetting mobs = new BooleanSetting("Мобы", true);
    private final BooleanSetting animals = new BooleanSetting("Животные", true);
    private final BooleanSetting onlyCrit = new BooleanSetting("Только криты", true);
    private final BooleanSetting shieldBreak = new BooleanSetting("Ломать щит", false);

    public TriggerBot() {
        addSettings(players, mobs, animals, onlyCrit, shieldBreak);
    }

    private final StopWatch stopWatch = new StopWatch();

    @Subscribe
    public void onUpdate(EventUpdate e) {
        Entity entity = getValidEntity();

        if (entity == null || IMinecraft.mc.player == null) {
            return;
        }

        if (shouldAttack()) {
            stopWatch.setLastMS(500);
            attackEntity(entity);
        }
    }

    private boolean shouldAttack() {
        return AttackUtil.isPlayerFalling(onlyCrit.get(), true, false) && (stopWatch.hasTimeElapsed());
    }

    private void attackEntity(Entity entity) {
        boolean shouldStopSprinting = false;
        if (onlyCrit.get() && CEntityActionPacket.lastUpdatedSprint) {
            IMinecraft.mc.player.connection.sendPacket(new CEntityActionPacket(IMinecraft.mc.player, CEntityActionPacket.Action.STOP_SPRINTING));
            shouldStopSprinting = true;
        }

        IMinecraft.mc.playerController.attackEntity(IMinecraft.mc.player, entity);
        IMinecraft.mc.player.swingArm(Hand.MAIN_HAND);
        if (shieldBreak.get() && entity instanceof PlayerEntity)
            breakShieldPlayer(entity);

        if (shouldStopSprinting) {
            IMinecraft.mc.player.connection.sendPacket(new CEntityActionPacket(IMinecraft.mc.player, CEntityActionPacket.Action.START_SPRINTING));
        }
    }

    private Entity getValidEntity() {
        if (IMinecraft.mc.objectMouseOver.getType() == RayTraceResult.Type.ENTITY) {
            Entity entity = ((EntityRayTraceResult) IMinecraft.mc.objectMouseOver).getEntity();
            if (checkEntity((LivingEntity) entity)) {
                return entity;
            }
        }
        return null;
    }

    public static void breakShieldPlayer(Entity entity) {
        if (((LivingEntity) entity).isBlocking()) {
            int invSlot = InventoryUtil.getInstance().getAxeInInventory(false);
            int hotBarSlot = InventoryUtil.getInstance().getAxeInInventory(true);

            if (hotBarSlot == -1 && invSlot != -1) {
                int bestSlot = InventoryUtil.getInstance().findBestSlotInHotBar();
                IMinecraft.mc.playerController.windowClick(0, invSlot, 0, ClickType.PICKUP, IMinecraft.mc.player);
                IMinecraft.mc.playerController.windowClick(0, bestSlot + 36, 0, ClickType.PICKUP, IMinecraft.mc.player);

                IMinecraft.mc.player.connection.sendPacket(new CHeldItemChangePacket(bestSlot));
                IMinecraft.mc.playerController.attackEntity(IMinecraft.mc.player, entity);
                IMinecraft.mc.player.swingArm(Hand.MAIN_HAND);
                IMinecraft.mc.player.connection.sendPacket(new CHeldItemChangePacket(IMinecraft.mc.player.inventory.currentItem));

                IMinecraft.mc.playerController.windowClick(0, bestSlot + 36, 0, ClickType.PICKUP, IMinecraft.mc.player);
                IMinecraft.mc.playerController.windowClick(0, invSlot, 0, ClickType.PICKUP, IMinecraft.mc.player);
            }

            if (hotBarSlot != -1) {
                IMinecraft.mc.player.connection.sendPacket(new CHeldItemChangePacket(hotBarSlot));
                IMinecraft.mc.playerController.attackEntity(IMinecraft.mc.player, entity);
                IMinecraft.mc.player.swingArm(Hand.MAIN_HAND);
                IMinecraft.mc.player.connection.sendPacket(new CHeldItemChangePacket(IMinecraft.mc.player.inventory.currentItem));
            }
        }
    }

    private boolean checkEntity(LivingEntity entity) {
        AttackUtil entitySelector = new AttackUtil();

        if (players.get()) {
            entitySelector.apply(AttackUtil.EntityType.PLAYERS);
        }
        if (mobs.get()) {
            entitySelector.apply(AttackUtil.EntityType.MOBS);
        }
        if (animals.get()) {
            entitySelector.apply(AttackUtil.EntityType.ANIMALS);
        }
        return entitySelector.ofType(entity, entitySelector.build()) != null && entity.isAlive();
    }

    @Override
    public void onDisable() {

        stopWatch.reset();
        super.onDisable();
    }
}
