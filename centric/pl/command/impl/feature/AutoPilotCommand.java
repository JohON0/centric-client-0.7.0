package centric.pl.command.impl.feature;

import centric.pl.command.*;
import centric.pl.command.impl.CommandException;
import centric.pl.Main;
import centric.pl.events.impl.EventUpdate;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.math.StopWatch;
import centric.pl.johon0.utils.player.InventoryUtil;
import com.google.common.eventbus.Subscribe;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TextFormatting;
import org.joml.Vector3d;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class AutoPilotCommand implements Command, IMinecraft {
    final Prefix prefix;
    final Logger logger;
    boolean isFlying = false;
    boolean massage = false;
    Vector3d targetCoordinates = new Vector3d(0, 0, 0);
    private long lastFireworkTime = 0;
    private double delay;
    StopWatch timerutil = new StopWatch();
    public AutoPilotCommand(Prefix prefix, Logger logger) {
        this.prefix = prefix;
        this.logger = logger;
        Main.getInstance().getEventBus().register(this);
    }

    @Override
    public void execute(Parameters parameters) {
        String commandType = parameters.asString(0).orElse("");

        switch (commandType) {
            case "start" -> startAutoPilot(parameters);
            case "stop" -> stopAutoPilot();
            default ->
                    throw new CommandException(TextFormatting.RED + "������� ��� �������: " + TextFormatting.GRAY + "start, stop");
        }
    }

    private void startAutoPilot(Parameters param) {
        double x = param.asDouble(1)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "������� ������ ����������!"));
        double z = param.asDouble(2)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "������� ������ ����������!"));

        targetCoordinates.set(x, 0, z);
        isFlying = true;
        ItemStack chestplate = mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST);

        if (chestplate.getItem() instanceof ElytraItem && mc.player.isElytraFlying()) {
            logger.log("��������� �����������. �����");
        } else if (!mc.player.isElytraFlying()) {
            print("�� ������ ��������");
        }
    }

    private void stopAutoPilot() {
        isFlying = false;
    }

    @Override
    public String name() {
        return "autopilot";
    }

    @Override
    public String description() {
        return "������������� ����� � ��������� �����������.";
    }

    public List<String> adviceMessage() {
        String commandPrefix = prefix.get();
        return List.of(commandPrefix + "autopilot start <x> <z> - ��������� ���������",
                commandPrefix + "autopilot stop - ���������� ���������",
                "������: " + TextFormatting.RED + commandPrefix + "autopilot start 100 100"
        );
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (!isFlying) return;
        ItemStack chestplate = mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST);

        if (chestplate.getItem() instanceof ElytraItem && InventoryUtil.getInstance().getSlotInInventoryOrHotbar(Items.FIREWORK_ROCKET, true) != -1) {
            handleFlight();
        } else {
            stopAutoPilot(); // ���������� ���������, ���� ������ �� ������
        }
        if (mc.player.isElytraFlying()) {
            handleFlight();
        } else {
            stopAutoPilot();
        }
        // ��������, ������ �� ������
        if (chestplate.getItem() instanceof ElytraItem) {
            handleFlight();
        } else {
            logger.log(TextFormatting.RED + "������ �� ������");
            stopAutoPilot(); // ���������� ���������, ���� ������ �� ������
        }
    }

    private void handleFlight() {
        // ���� ����� ����� �� ������, ��������� ������ ������
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFireworkTime >= (35 * 100L)) {
            useFirework();
            lastFireworkTime = currentTime;
        }
        double deltaX = targetCoordinates.x - mc.player.getPosX();
        double deltaY = 200 - mc.player.getPosY();
        double deltaZ = targetCoordinates.z - mc.player.getPosZ();

        // ������������ ���� ��������
        float yaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90;
        float pitch = (float) -Math.toDegrees(Math.atan2(deltaY, Math.sqrt(deltaX * deltaX + deltaZ * deltaZ)));

        // ������������� Yaw � Pitch
        mc.player.rotationYaw = yaw;
        mc.player.rotationPitch = pitch;

        // ���������, �������� �� ���������
        if (((int) mc.player.getPosX()) == targetCoordinates.x && ((int) mc.player.getPosZ()) == targetCoordinates.z) {
            stopAutoPilot();
        }
    }


    private void useFirework() {
        int hbSlot = InventoryUtil.getInstance().getSlotInInventoryOrHotbar(Items.FIREWORK_ROCKET, true);
        if (hbSlot != -1) {
            int currentSlot = mc.player.inventory.currentItem;
            // ������������� �� ���� � �����������
            mc.player.connection.sendPacket(new CHeldItemChangePacket(hbSlot));
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            mc.player.connection.sendPacket(new CHeldItemChangePacket(currentSlot));
        } else {
            print("��������� �� �������");
            stopAutoPilot();
        }
    }
}
