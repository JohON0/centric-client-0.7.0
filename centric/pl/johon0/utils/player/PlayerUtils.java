package centric.pl.johon0.utils.player;

import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;

import java.util.Objects;
import java.util.regex.Pattern;


@UtilityClass
public class PlayerUtils {

    Minecraft mc = Minecraft.getInstance();

    private final Pattern NAME_REGEX = Pattern.compile("^[A-z�-�0-9_]{3,16}$");

    public boolean isNameValid(String name) {
        return NAME_REGEX.matcher(name).matches();
    }

    public static boolean isInHell() {
        if (mc.world == null) return false;
        return Objects.equals(mc.world.getDimensionKey(), "the_nether");
    }

    public static float calculateCorrectYawOffset(float yaw) {
        // ������������� ����������
        double xDiff = mc.player.getPosX() - mc.player.prevPosX;
        double zDiff = mc.player.getPosZ() - mc.player.prevPosZ;
        float distSquared = (float) (xDiff * xDiff + zDiff * zDiff);
        float renderYawOffset = mc.player.prevRenderYawOffset;
        float offset = renderYawOffset;
        float yawOffsetDiff;

        // ���������� ��������, ���� ���������� ������ ���������� ��������
        if (distSquared > 0.0025000002f) {
            offset = (float) MathHelper.atan2(zDiff, xDiff) * 180.0f / (float) Math.PI - 90.0f;
        }

        // ��������� �������� ������ ���� ��������, ���� ����� ����� �����
        if (mc.player != null && mc.player.swingProgress > 0.0f) {
            offset = yaw;
        }

        // ����������� ������� ��������
        yawOffsetDiff = MathHelper.wrapDegrees(yaw - (renderYawOffset + MathHelper.wrapDegrees(offset - renderYawOffset) * 0.3f));
        yawOffsetDiff = MathHelper.clamp(yawOffsetDiff, -75.0f, 75.0f);

        // ���������� ��������� ��������
        renderYawOffset = yaw - yawOffsetDiff;
        if (yawOffsetDiff * yawOffsetDiff > 2500.0f) {
            renderYawOffset += yawOffsetDiff * 0.2f;
        }

        return renderYawOffset;
    }
}

