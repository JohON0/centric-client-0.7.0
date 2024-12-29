
package centric.pl.functions.impl.combat;

import centric.pl.johon0.utils.client.ClientUtil;
import com.google.common.eventbus.Subscribe;
import centric.pl.Main;
import centric.pl.command.friends.FriendStorage;
import centric.pl.events.impl.EventInput;
import centric.pl.events.impl.EventMotion;
import centric.pl.events.impl.EventUpdate;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.settings.impl.BooleanSetting;
import centric.pl.functions.settings.impl.ModeListSetting;
import centric.pl.functions.settings.impl.ModeSetting;
import centric.pl.functions.settings.impl.SliderSetting;
import centric.pl.johon0.utils.math.SensUtils;
import centric.pl.johon0.utils.math.StopWatch;
import centric.pl.johon0.utils.player.InventoryUtil;
import centric.pl.johon0.utils.player.MouseUtil;
import centric.pl.johon0.utils.player.MoveUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import com.jhlabs.image.ImageMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.optifine.CustomColors;

@FunctionRegister(
        name = "KillAura", type = Category.Combat, beta = false)
public class KillAura extends Function {
    private final ModeSetting type = new ModeSetting("Тип", "Плавная", "Плавная", "Резкая", "ReallyWorld", "FunTime");
    private final SliderSetting attackRange = new SliderSetting("Дистанция аттаки", 3.0F, 3.0F, 6.0F, 0.1F);
    final ModeListSetting targets = new ModeListSetting("Таргеты", new BooleanSetting("Игроки", true), new BooleanSetting("Голые", true), new BooleanSetting("Мобы", false), new BooleanSetting("Животные", false), new BooleanSetting("Друзья", false), new BooleanSetting("Голые невидимки", true), new BooleanSetting("Невидимки", true));
    public final ModeListSetting options = new ModeListSetting("Опции", new BooleanSetting("Только криты", true), new BooleanSetting("Ломать щит", true), new BooleanSetting("Отжимать щит", true), new BooleanSetting("Ускорять ротацию при атаке", false), new BooleanSetting("Синхронизировать атаку с ТПС", false), new BooleanSetting("Фокусировать одну цель", true), new BooleanSetting("Коррекция движения", true));
    final ModeSetting correctionType = new ModeSetting("Тип коррекции", "Незаметный", "Незаметный", "Сфокусированный");
    private final BooleanSetting checkWallObstruction = new BooleanSetting("Не бить через стену", true);
    private final BooleanSetting checkEating = new BooleanSetting("Не бить когда ешь", true);
    private final StopWatch stopWatch = new StopWatch();
    final ElytraTarget elytraTarget = new ElytraTarget();
    public Vector2f rotateVector = new Vector2f(0.0F, 0.0F);
    private LivingEntity target;
    private Entity selected;
    int ticks = 0;
    boolean isRotated;
    float lastYaw;
    float lastPitch;

    public KillAura() {
        this.addSettings(this.type, this.attackRange, this.targets, this.options, this.correctionType, this.checkWallObstruction, this.checkEating);
    }

    @Subscribe
    public void onInput(EventInput eventInput) {
        if ((Boolean)this.options.getValueByName("Коррекция движения").get() && this.correctionType.is("Незаметная") && this.target != null && mc.player != null) {
            MoveUtils.fixMovement(eventInput, this.rotateVector.x);
        }

    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if ((Boolean)this.options.getValueByName("Фокусировать одну цель").get() && (this.target == null || !this.isValid(this.target)) || !(Boolean)this.options.getValueByName("Фокусировать одну цель").get()) {
            this.updateTarget();
        }

        if (this.target == null) {
            this.stopWatch.setLastMS(0L);
            this.reset();
        } else if (!(Boolean)this.checkWallObstruction.get() || this.canSeeThroughWall(this.target)) {
            this.isRotated = false;
            if (this.shouldPlayerFalling() && this.stopWatch.hasTimeElapsed()) {
                this.updateAttack();
                this.ticks = 2;
            }

            if (this.type.is("Резкая")) {
                if (this.ticks > 0) {
                    this.updateRotation(true, 180.0F, 90.0F);
                    --this.ticks;
                } else {
                    this.reset();
                }
            } else if (!this.isRotated) {
                this.updateRotation(false, 80.0F, 35.0F);
            }
        }

    }

    @Subscribe
    private void onWalking(EventMotion e) {
        if (this.target != null) {
            if ((Boolean)this.checkWallObstruction.get() && !this.canSeeThroughWall(this.target)) {
                this.target = null;
                this.stopWatch.setLastMS(0L);
                this.reset();
            } else {
                float yaw = this.rotateVector.x;
                float pitch = this.rotateVector.y;
                e.setYaw(yaw);
                e.setPitch(pitch);
                mc.player.rotationYawHead = yaw;
                mc.player.renderYawOffset = yaw;
                mc.player.rotationPitchHead = pitch;
            }
        }

    }

    private void updateTarget() {
        List<LivingEntity> targets = new ArrayList();
        Iterator var2 = mc.world.getAllEntities().iterator();

        while(var2.hasNext()) {
            Entity entity = (Entity)var2.next();
            if (entity instanceof LivingEntity living) {
                if (this.isValid(living)) {
                    targets.add(living);
                }
            }
        }

        if (targets.isEmpty()) {
            this.target = null;
        } else if (targets.size() == 1) {
            this.target = (LivingEntity)targets.get(0);
        } else {
            targets.sort(Comparator.comparingDouble((object) -> {
                if (object instanceof PlayerEntity player) {
                    return -this.getEntityArmor(player);
                } else if (object instanceof LivingEntity base) {
                    return (double)(-base.getTotalArmorValue());
                } else {
                    return 0.0;
                }
            }).thenComparing((object, object2) -> {
                double d2 = this.getEntityHealth((LivingEntity)object);
                double d3 = this.getEntityHealth((LivingEntity)object2);
                return Double.compare(d2, d3);
            }).thenComparing((object, object2) -> {
                double d2 = (double)mc.player.getDistance((LivingEntity)object);
                double d3 = (double)mc.player.getDistance((LivingEntity)object2);
                return Double.compare(d2, d3);
            }));
            this.target = (LivingEntity)targets.get(0);
        }

    }

    private double gaussianRandom(double mean, double stdDev) {
        return mean + CustomColors.random.nextGaussian() * stdDev;
    }

    private void updateRotation(boolean attack, float rotationYawSpeed, float rotationPitchSpeed) {
        Vector3d var10000 = this.target.getPositionVec();
        double var26 = mc.player.getPosYEye() - this.target.getPosY();
        double var10004 = (double)this.target.getHeight();
        var10000 = var10000.add(0.0, MathHelper.clamp(var26, 0.0, var10004 * (mc.player.getDistanceEyePos(this.target) / (double)(Float)this.attackRange.get())), 0.0);
        Vector3d vec = var10000.subtract(mc.player.getEyePosition(1.0F));
        this.isRotated = true;
        float yawToTarget = (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90.0);
        float pitchToTarget = (float)(-Math.toDegrees(Math.atan2(vec.y, Math.hypot(vec.x, vec.z))));
        float yawDelta = MathHelper.wrapDegrees(yawToTarget - this.rotateVector.x);
        float pitchDelta = MathHelper.wrapDegrees(pitchToTarget - this.rotateVector.y);
        int roundedYaw = (int)yawDelta;
        float clampedYaw;
        float pitchStep;
        float gcd;
        float randomYawOffset;
        float randomPitchOffset;
        float yawRandomOffset;
        float pitchRandomOffset;
        float attackPatternOffset;
        float smoothCorrectionFactor;
        switch ((String)this.type.get()) {
            case "Плавная":
                clampedYaw = rotationYawSpeed * 0.22F;
                pitchStep = rotationPitchSpeed * 0.22F;
                if (attack && this.selected != this.target && (Boolean)this.options.getValueByName("Ускорять ротацию при атаке").get()) {
                    pitchStep = rotationPitchSpeed * 0.15F;
                }

                Vector2f var25;
                if (Math.abs(yawDelta) > clampedYaw) {
                    var25 = this.rotateVector;
                    var25.x += yawDelta > 0.0F ? clampedYaw : -clampedYaw;
                } else {
                    this.rotateVector.x = yawToTarget;
                }

                if (Math.abs(pitchDelta) > pitchStep) {
                    var25 = this.rotateVector;
                    var25.y += pitchDelta > 0.0F ? pitchStep : -pitchStep;
                } else {
                    this.rotateVector.y = pitchToTarget;
                }

                gcd = SensUtils.getGCD();
                var25 = this.rotateVector;
                var25.x -= (this.rotateVector.x - mc.player.rotationYaw) % gcd;
                var25 = this.rotateVector;
                var25.y -= (this.rotateVector.y - mc.player.rotationPitch) % gcd;
                if ((Boolean)this.options.getValueByName("Коррекция движения").get()) {
                    mc.player.rotationYawOffset = this.rotateVector.x;
                }
                break;
            case "Резкая":
                clampedYaw = this.rotateVector.x + (float)roundedYaw;
                pitchStep = MathHelper.clamp(this.rotateVector.y + pitchDelta, -90.0F, 90.0F);
                gcd = SensUtils.getGCD();
                clampedYaw -= (clampedYaw - this.rotateVector.x) % gcd;
                pitchStep -= (pitchStep - this.rotateVector.y) % gcd;
                this.rotateVector = new Vector2f(clampedYaw, pitchStep);
                if ((Boolean)this.options.getValueByName("Коррекция движения").get()) {
                    mc.player.rotationYawOffset = clampedYaw;
                }
                break;
            case "ReallyWorld":
                clampedYaw = Math.min(Math.max(Math.abs(yawDelta), 1.4F), rotationYawSpeed);
                pitchStep = Math.min(Math.max(Math.abs(pitchDelta), 1.2F), rotationPitchSpeed);
                gcd = (float)(Math.random() * 6.0 - 3.0);
                randomYawOffset = (float)(Math.random() * 0.85 - 0.425);
                if (attack && this.selected != this.target && (Boolean)this.options.getValueByName("Ускорять ротацию").get()) {
                    pitchStep = Math.max(Math.abs(pitchDelta), 1.0F);
                } else {
                    pitchStep /= 3.0F;
                }

                randomPitchOffset = this.rotateVector.x + (yawDelta > 0.0F ? clampedYaw : -clampedYaw);
                yawRandomOffset = MathHelper.clamp(this.rotateVector.y + (pitchDelta > 0.0F ? pitchStep : -pitchStep), -89.0F, 89.0F);
                pitchRandomOffset = randomPitchOffset + gcd;
                attackPatternOffset = yawRandomOffset + randomYawOffset;
                smoothCorrectionFactor = SensUtils.getGCD();
                pitchRandomOffset -= (pitchRandomOffset - this.rotateVector.x) % smoothCorrectionFactor;
                attackPatternOffset -= (attackPatternOffset - this.rotateVector.y) % smoothCorrectionFactor;
                this.rotateVector = new Vector2f(pitchRandomOffset, attackPatternOffset);
                this.lastYaw = clampedYaw;
                this.lastPitch = pitchStep;
                if ((Boolean)this.options.getValueByName("Коррекция движения").get()) {
                    mc.player.rotationYawOffset = pitchRandomOffset;
                }
                break;
            case "FunTime":
                clampedYaw = Math.min(Math.max(Math.abs(yawDelta), 1.14F), rotationYawSpeed);
                pitchStep = Math.min(Math.max(Math.abs(pitchDelta), 1.14F), rotationPitchSpeed);
                if (attack && this.selected != this.target && (Boolean)this.options.getValueByName("Ускорять ротацию").get()) {
                    pitchStep = Math.max(Math.abs(pitchDelta), 1.0F);
                } else {
                    pitchStep /= 3.0F;
                }

                clampedYaw *= 1.8F;
                gcd = (float)this.gaussianRandom(0.0, (double)(clampedYaw / 7.6F));
                if (Math.abs(clampedYaw - this.lastYaw) <= 3.0F) {
                    clampedYaw = this.lastYaw + 2.8F;
                }

                randomYawOffset = (float)this.gaussianRandom(-0.5, 0.5);
                randomPitchOffset = (float)this.gaussianRandom(-0.5, 0.5);
                yawRandomOffset = (float)this.gaussianRandom(0.0, 0.8999999761581421) + randomYawOffset;
                pitchRandomOffset = (float)this.gaussianRandom(0.0, -0.8999999761581421) + randomPitchOffset;
                attackPatternOffset = (float)(Math.sin((double)(System.currentTimeMillis() % 1000L) / 1000.0 * Math.PI * 2.0) * 1.5);
                smoothCorrectionFactor = 0.78F;
                float yaw = this.rotateVector.x + (yawDelta > 0.0F ? (clampedYaw + yawRandomOffset + attackPatternOffset) * smoothCorrectionFactor : -(clampedYaw + yawRandomOffset + attackPatternOffset) * smoothCorrectionFactor);
                float pitch = MathHelper.clamp(this.rotateVector.y + (pitchDelta > 0.0F ? (pitchStep + pitchRandomOffset + gcd) * smoothCorrectionFactor : -(pitchStep + pitchRandomOffset + gcd) * smoothCorrectionFactor), -89.0F, 89.0F);
                yaw -= (yaw - this.rotateVector.x) % gcd;
                pitch -= (pitch - this.rotateVector.y) % gcd;
                this.rotateVector = new Vector2f(yaw, pitch);
                this.lastYaw = clampedYaw;
                this.lastPitch = pitchStep;
                if ((Boolean)this.options.getValueByName("Коррекция движения").get()) {
                    Minecraft var24 = mc;
                    mc.player.rotationYawOffset = yaw;
                }
        }

    }

    private void updateAttack() {
        if (options.getValueByName("Коррекция движения").get() && mc.player.isBlocking()) {
            mc.playerController.onStoppedUsingItem(mc.player);
        }

        boolean sprint = false;
        if (CEntityActionPacket.lastUpdatedSprint && !mc.player.isInWater()) {
            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.STOP_SPRINTING));
            sprint = true;
        }
        // Удар по цели
        mc.playerController.attackEntity(mc.player, this.target);
        mc.player.swingArm(Hand.MAIN_HAND);
        this.stopWatch.setLastMS(500L); // Перезапускаем таймер ожидания


        if (target instanceof PlayerEntity player && options.getValueByName("Ломать щит").get()) {
            breakShieldPlayer(player);
        }

        if (sprint) {
            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_SPRINTING));
        }
    }

    // Проверяем, можем ли мы нанести удар
    private boolean isAttackAllowed() {
        float attackStrength = mc.player.getCooledAttackStrength((Boolean)this.options.getValueByName("Синхронизировать атаку с ТПС").get() ? Main.getInstance().getTpsCalc().getAdjustTicks() : 1.5F);
        return attackStrength >= 0.92F && !mc.player.isBlocking() && !canEat() && !mc.player.isHandActive();
    }


    private boolean canSeeThroughWall(Entity entity) {
        ClientWorld var3 = mc.world;
        Vector3d var4 = mc.player.getEyePosition(1.0F);
        Vector3d var10004 = entity.getEyePosition(1.0F);
        RayTraceResult result = var3.rayTraceBlocks(new RayTraceContext(var4, var10004, BlockMode.COLLIDER, FluidMode.NONE, mc.player));
        return result.getType() == Type.MISS;
    }

    private boolean canEat() {
        return mc.player.isHandActive();
    }

    private boolean shouldPlayerFalling() {
        boolean var3;
        if ((!mc.player.isInWater() || !mc.player.areEyesInFluid(FluidTags.WATER)) && !mc.player.isInLava() && !mc.player.isOnLadder() && !mc.player.isPassenger() && !mc.player.abilities.isFlying) {
            var3 = false;
        } else {
            var3 = true;
        }

        float attackStrength = mc.player.getCooledAttackStrength((Boolean)this.options.getValueByName("Синхронизировать атаку с ТПС").get() ? Main.getInstance().getTpsCalc().getAdjustTicks() : 1.5F);
        if (attackStrength < 0.92F) {
            return false;
        } else if (!var3 && (Boolean)this.options.getValueByName("Только криты").get()) {
            if (!mc.player.isOnGround() && mc.player.fallDistance > 0.0F) {
                var3 = true;
                return var3;
            } else {
                var3 = false;
                return var3;
            }
        } else {
            return true;
        }
    }

    private boolean isValid(LivingEntity entity) {
        if (entity instanceof ClientPlayerEntity) {
            return false;
        } else if (entity.ticksExisted < 3) {
            return false;
        } else {
            Minecraft var10000 = mc;
            if (mc.player.getDistanceEyePos(entity) > (double)(Float)this.attackRange.get()) {
                return false;
            } else {
                if (entity instanceof PlayerEntity) {
                    PlayerEntity p = (PlayerEntity)entity;
                    if (AntiBot.isBot(entity)) {
                        return false;
                    }

                    if (!(Boolean)this.targets.getValueByName("Друзья").get() && FriendStorage.isFriend(p.getName().getString())) {
                        return false;
                    }

                    String var3 = p.getName().getString();
                    if (var3.equalsIgnoreCase(mc.player.getName().getString())) {
                        return false;
                    }
                }

                if (entity instanceof PlayerEntity && !(Boolean)this.targets.getValueByName("Игроки").get()) {
                    return false;
                } else if (entity instanceof PlayerEntity && entity.getTotalArmorValue() == 0 && !(Boolean)this.targets.getValueByName("Голые").get()) {
                    return false;
                } else if (entity instanceof PlayerEntity && entity.isInvisible() && entity.getTotalArmorValue() == 0 && !(Boolean)this.targets.getValueByName("Голые невидимки").get()) {
                    return false;
                } else if (entity instanceof PlayerEntity && entity.isInvisible() && !(Boolean)this.targets.getValueByName("Невидимки").get()) {
                    return false;
                } else if (entity instanceof MonsterEntity && !(Boolean)this.targets.getValueByName("Мобы").get()) {
                    return false;
                } else if (entity instanceof AnimalEntity && !(Boolean)this.targets.getValueByName("Животные").get()) {
                    return false;
                } else {
                    return !entity.isInvulnerable() && entity.isAlive() && !(entity instanceof ArmorStandEntity);
                }
            }
        }
    }

    private void breakShieldPlayer(PlayerEntity entity) {
        if (entity.isBlocking()) {
            int invSlot = InventoryUtil.getInstance().getAxeInInventory(false);
            int hotBarSlot = InventoryUtil.getInstance().getAxeInInventory(true);
            if (hotBarSlot == -1 && invSlot != -1) {
                int bestSlot = InventoryUtil.getInstance().findBestSlotInHotBar();
                mc.playerController.windowClick(0, invSlot, 0, ClickType.PICKUP, mc.player);
                int var10002 = bestSlot + 36;
                mc.playerController.windowClick(0, var10002, 0, ClickType.PICKUP, mc.player);
                mc.player.connection.sendPacket(new CHeldItemChangePacket(bestSlot));
                mc.playerController.attackEntity(mc.player, entity);
                mc.player.swingArm(Hand.MAIN_HAND);
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                var10002 = bestSlot + 36;
                mc.playerController.windowClick(0, var10002, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, invSlot, 0, ClickType.PICKUP, mc.player);
            }

            if (hotBarSlot != -1) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(hotBarSlot));
                mc.playerController.attackEntity(mc.player, entity);
                mc.player.swingArm(Hand.MAIN_HAND);
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
            }
        }

    }

    private void reset() {
        if ((Boolean)this.options.getValueByName("Коррекция движения").get()) {
            mc.player.rotationYawOffset = -2.14748365E9F;
        }

        this.rotateVector = new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);
    }

    public void onEnable() {
        super.onEnable();
        this.reset();
        this.target = null;
    }

    public void onDisable() {
        super.onDisable();
        this.reset();
        this.stopWatch.setLastMS(0L);
        this.target = null;
    }

    private double getEntityArmor(PlayerEntity entityPlayer2) {
        double d2 = 0.0;

        for(int i2 = 0; i2 < 4; ++i2) {
            ItemStack is = (ItemStack)entityPlayer2.inventory.armorInventory.get(i2);
            if (is.getItem() instanceof ArmorItem) {
                d2 += this.getProtectionLvl(is);
            }
        }

        return d2;
    }

    private double getProtectionLvl(ItemStack stack) {
        Item var3 = stack.getItem();
        if (var3 instanceof ArmorItem i) {
            double damageReduceAmount = (double)i.getDamageReduceAmount();
            if (stack.isEnchanted()) {
                damageReduceAmount += (double)EnchantmentHelper.getEnchantmentLevel(Enchantments.PROTECTION, stack) * 0.25;
            }

            return damageReduceAmount;
        } else {
            return 0.0;
        }
    }

    private double getEntityHealth(LivingEntity ent) {
        if (ent instanceof PlayerEntity player) {
            return (double)(player.getHealth() + player.getAbsorptionAmount()) * (this.getEntityArmor(player) / 20.0);
        } else {
            return (double)(ent.getHealth() + ent.getAbsorptionAmount());
        }
    }

    public ModeSetting getType() {
        return this.type;
    }

    public ModeListSetting getOptions() {
        return this.options;
    }

    public StopWatch getStopWatch() {
        return this.stopWatch;
    }

    public LivingEntity getTarget() {
        return this.target;
    }
}
