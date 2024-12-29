package centric.pl.functions.impl.render;

import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.impl.combat.KillAura;
import centric.pl.functions.settings.impl.BooleanSetting;
import centric.pl.functions.settings.impl.ModeSetting;
import centric.pl.functions.settings.impl.SliderSetting;
import com.mojang.blaze3d.matrix.MatrixStack;
//import centric.pl.functions.impl.combat.KillAura;
import net.minecraft.util.math.vector.Vector3f;


@FunctionRegister(name = "SwingAnimation", type = Category.Render, beta = false)
public class SwingAnimation extends Function {

    public ModeSetting animationMode = new ModeSetting("���", "360", "360", "�������� ����", "��� ������", "���������� ��������");
    public SliderSetting swingPower = new SliderSetting("����", 5.0f, 1.0f, 10.0f, 0.05f);
    public SliderSetting swingSpeed = new SliderSetting("��������", 10.0f, 3.0f, 10.0f, 1.0f);
    public SliderSetting scale = new SliderSetting("������", 1.0f, 0.5f, 1.5f, 0.05f);
    public final BooleanSetting lefthand = new BooleanSetting("����� ����", true).setVisible(
            () -> animationMode.is("360")
    );
    public final BooleanSetting onlyAura = new BooleanSetting("������ � ���������", true);
    public KillAura killAura;

    public SwingAnimation(KillAura killAura) {
        this.killAura = killAura;
        addSettings(animationMode, swingPower, swingSpeed, scale, lefthand, onlyAura);
    }

    public void animationProcess(MatrixStack stack, float swingProgress, Runnable runnable) {
        float anim = (float) Math.sin(swingProgress * (Math.PI / 2) * 2);

        if (onlyAura.get() && killAura.getTarget() == null) {
            runnable.run();
            return;
        }

        switch (animationMode.getIndex()) {
            case 0: // 360
                stack.scale(scale.get(), scale.get(), scale.get());
                stack.translate(0.3f, 0.0f, -0.5f);
                stack.rotate(Vector3f.YP.rotationDegrees((System.currentTimeMillis() / 5) % 360));
                stack.rotate(Vector3f.XP.rotationDegrees(anim * 50));
                stack.rotate(Vector3f.ZP.rotationDegrees(-90 * anim));
                break;



            case 1: // �������� "�������� ����"
                stack.scale(scale.get(), scale.get(), scale.get());
                stack.translate(0.2f, 0.2f, -0.5f);
                stack.rotate(Vector3f.YP.rotationDegrees(90 * anim)); // ������� �� Y
                stack.rotate(Vector3f.ZP.rotationDegrees(45 * anim)); // ������� �� Z
                stack.rotate(Vector3f.XP.rotationDegrees(-60 * anim)); // ������� �� X
                break;

            case 2: // �������� "��� ������"
                stack.scale(scale.get(), scale.get(), scale.get());
                stack.translate(0.4f, -0.1f, -0.4f);
                stack.rotate(Vector3f.YP.rotationDegrees(-20 * anim)); // ������� �� Y
                stack.rotate(Vector3f.XP.rotationDegrees(20 * anim)); // ������ ����
                stack.rotate(Vector3f.ZP.rotationDegrees(30 * anim)); // ������� ������ Z
                break;

            case 3: // �������� "���������� ��������"
                stack.scale(scale.get(), scale.get(), scale.get());
                stack.translate(0.1f, 0.1f, -0.5f);
                stack.rotate(Vector3f.YP.rotationDegrees(60 * anim)); // ������� �� Y
                stack.rotate(Vector3f.XP.rotationDegrees(-30 * anim)); // ������� �� X
                stack.rotate(Vector3f.ZP.rotationDegrees(-30 * anim)); // ������� ������ Z
                break;

            case 4: // �������� "���������"
                stack.scale(scale.get(), scale.get(), scale.get());
                stack.translate(0.0f, 0.3f, -0.5f);
                stack.rotate(Vector3f.YP.rotationDegrees(-90 * anim)); // ������� �� Y
                stack.rotate(Vector3f.ZP.rotationDegrees(90 * anim)); // ������� �� Z
                stack.rotate(Vector3f.XP.rotationDegrees(60 * anim)); // ������ ����
                break;

            default:
                stack.scale(scale.get(), scale.get(), scale.get());
                runnable.run();
                break;
        }
    }
}
