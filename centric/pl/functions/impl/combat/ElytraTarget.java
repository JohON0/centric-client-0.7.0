package centric.pl.functions.impl.combat;

import centric.pl.events.impl.EventUpdate;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.settings.impl.BooleanSetting;
import centric.pl.functions.settings.impl.SliderSetting;
import centric.pl.johon0.utils.player.InventoryUtil;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@FunctionRegister(
        name = "ElytraTarget",
        type = Category.Combat,
        beta = true)
public class ElytraTarget extends Function {
    private Set<PlayerEntity> targetedPlayers = new HashSet<>();
    private boolean isTargeting = false;
    private long lastFireworkTime = 0;
    private long fireworkCooldown = 750; // Изначальный кулдаун
    private long lastChatMessageTime = 0;
    private long chatMessageInterval = 5000; // Интервал между сообщениями в чат
    public Vector2f rotate = new Vector2f(0.0f, 0.0f);

    private BooleanSetting save = new BooleanSetting("Безопасность", true);
    private BooleanSetting autofirework = new BooleanSetting("Авто-Фейерверк", true);
    private BooleanSetting deadtoggle = new BooleanSetting("Оключать при смерти таргета",true);
    private SliderSetting distanse = new SliderSetting("Дистанция",50,5,50,1);
    private SliderSetting fireworkdelay = new SliderSetting("Задержка отправки феерверка",20,10,50,10);
    private SliderSetting hptoggle = new SliderSetting("Хп для отключение",6,0,20,1).setVisible(() -> save.get());


    public ElytraTarget() {
        addSettings(save,autofirework,deadtoggle,distanse,fireworkdelay,hptoggle);
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (Minecraft.getInstance().player.isElytraFlying()) {
            if (!isTargeting) {
                targetPlayer();
            } else {
                updateRotationToPlayer();
                useFirework();
            }
        } else if (isTargeting) {
            stopTargeting();
        }
    }

    private void targetPlayer() {
        World world = Minecraft.getInstance().world;
        if (world != null) {
            List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(Minecraft.getInstance().player,
                    new AxisAlignedBB(Minecraft.getInstance().player.getPosX() - 5, Minecraft.getInstance().player.getPosY() - 5, Minecraft.getInstance().player.getPosZ() - 5,
                            Minecraft.getInstance().player.getPosX() + 5, Minecraft.getInstance().player.getPosY() + 5, Minecraft.getInstance().player.getPosZ() + 5));

            for (Entity entity : entities) {
                if (entity instanceof PlayerEntity && entity.isAlive()) {
                    PlayerEntity target = (PlayerEntity) entity;
                    if (!targetedPlayers.contains(target)) {
                        targetedPlayers.clear();
                        targetedPlayers.add(target);
                        isTargeting = true;
                        setRotationToPlayer(target);
                        return;
                    }
                }
            }
        }
    }
    private void setRotationToPlayer(PlayerEntity player) {
        if (player!= null) {
            double deltaX = player.getPosX() - Minecraft.getInstance().player.getPosX();
            double deltaZ = player.getPosZ() - Minecraft.getInstance().player.getPosZ();
            double deltaY = player.getPosY() - Minecraft.getInstance().player.getPosY();

            double yaw = Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90;
            double pitch = -Math.toDegrees(Math.atan2(deltaY, Math.sqrt(deltaX * deltaX + deltaZ * deltaZ)));

            Minecraft.getInstance().player.rotationYaw = (float) yaw;
            Minecraft.getInstance().player.rotationPitch = (float) pitch;
        }
    }

    private void updateRotationToPlayer() {
        if (!targetedPlayers.isEmpty()) {
            PlayerEntity target = targetedPlayers.iterator().next();
            setRotationToPlayer(target);
        }
    }

    private void useFirework() {
        long currentTime = System.currentTimeMillis();
        if (((Boolean) this.autofirework.get()).booleanValue() && currentTime - lastFireworkTime >= (fireworkdelay.get().intValue() * 100L)) {
            int hbSlot = InventoryUtil.getInstance().getSlotInInventoryOrHotbar(Items.FIREWORK_ROCKET, true);
            int invSlot = InventoryUtil.getInstance().getSlotInInventoryOrHotbar(Items.FIREWORK_ROCKET, false);

            if (invSlot == -1 && hbSlot == -1) {
                print("Феерверки не найдены");
                return;
            }

// Сохраняем текущий выбранный слот
            int currentSlot = Minecraft.getInstance().player.inventory.currentItem;

// Переключаемся на слот с фейерверком
            Minecraft.getInstance().player.connection.sendPacket(new CHeldItemChangePacket(hbSlot));

// Используем фейерверк
            Minecraft.getInstance().player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));

// Возвращаемся к предыдущему слоту
            Minecraft.getInstance().player.connection.sendPacket(new CHeldItemChangePacket(currentSlot));

// Обновляем время последнего использования фейерверка
            lastFireworkTime = currentTime;

// Обновляем кулдаун в зависимости от расстояния до цели
            double distanceToTarget = Minecraft.getInstance().player.getDistance(targetedPlayers.iterator().next());
            if (distanceToTarget > distanse.get()) {
                fireworkCooldown = 550; // Уменьшаем кулдаун, если цель дальше 50 блоков
            } else {
                fireworkCooldown = 230; // Возвращаем изначальный кулдаун, если цель ближе 50 блоков
            }
        }
    }

    private void stopTargeting() {
        targetedPlayers.clear();
        isTargeting = false;
    }

    private void checkChatMessage() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastChatMessageTime >= chatMessageInterval) {
            if (!targetedPlayers.isEmpty()) {
                PlayerEntity target = targetedPlayers.iterator().next();
                if (target != null) {
                    float mchealth = target.getHealth();
                    if (mchealth <= 0.01f) {
                        targetedPlayers.clear();
                        print("Таргет был убит!");
                        onDisable();
                    }
                }
            }
            lastChatMessageTime = currentTime;
        }
        float mchealth = mc.player.getHealth();
        if (save.get() && mchealth < hptoggle.get()) {
            double deltaX = mc.player.getPosX() - Minecraft.getInstance().player.getPosX();
            double deltaZ = mc.player.getPosZ() - Minecraft.getInstance().player.getPosZ();
            double deltaY = mc.player.getPosY() - Minecraft.getInstance().player.getPosY();
            double yaw = Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 185;
            double pitch = -Math.toDegrees(Math.atan2(deltaY, Math.sqrt(deltaX * deltaX + deltaZ * deltaZ)));
            mc.player.rotationYaw = (float) yaw;
            mc.player.rotationPitch = (float) pitch;
            targetedPlayers.clear();
            stopTargeting();
            onDisable();
            print("Модуль был выключен, из-за " + mchealth + "ХП");
        }
    }

    public PlayerEntity[] getTargetedPlayers() {
        return targetedPlayers.toArray(new PlayerEntity[0]);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
