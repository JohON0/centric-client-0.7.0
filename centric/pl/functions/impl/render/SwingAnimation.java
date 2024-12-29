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

    public ModeSetting animationMode = new ModeSetting("Мод", "360", "360", "Круговой жест", "Шаг вперед", "Скользящее движение");
    public SliderSetting swingPower = new SliderSetting("Сила", 5.0f, 1.0f, 10.0f, 0.05f);
    public SliderSetting swingSpeed = new SliderSetting("Скорость", 10.0f, 3.0f, 10.0f, 1.0f);
    public SliderSetting scale = new SliderSetting("Размер", 1.0f, 0.5f, 1.5f, 0.05f);
    public final BooleanSetting lefthand = new BooleanSetting("Левая рука", true).setVisible(
            () -> animationMode.is("360")
    );
    public final BooleanSetting onlyAura = new BooleanSetting("Только с киллаурой", true);
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



            case 1: // Анимация "Круговой жест"
                stack.scale(scale.get(), scale.get(), scale.get());
                stack.translate(0.2f, 0.2f, -0.5f);
                stack.rotate(Vector3f.YP.rotationDegrees(90 * anim)); // Поворот по Y
                stack.rotate(Vector3f.ZP.rotationDegrees(45 * anim)); // Поворот по Z
                stack.rotate(Vector3f.XP.rotationDegrees(-60 * anim)); // Поворот по X
                break;

            case 2: // Анимация "Шаг вперед"
                stack.scale(scale.get(), scale.get(), scale.get());
                stack.translate(0.4f, -0.1f, -0.4f);
                stack.rotate(Vector3f.YP.rotationDegrees(-20 * anim)); // Поворот по Y
                stack.rotate(Vector3f.XP.rotationDegrees(20 * anim)); // Подъем руки
                stack.rotate(Vector3f.ZP.rotationDegrees(30 * anim)); // Поворот вокруг Z
                break;

            case 3: // Анимация "Скользящее движение"
                stack.scale(scale.get(), scale.get(), scale.get());
                stack.translate(0.1f, 0.1f, -0.5f);
                stack.rotate(Vector3f.YP.rotationDegrees(60 * anim)); // Поворот по Y
                stack.rotate(Vector3f.XP.rotationDegrees(-30 * anim)); // Поворот по X
                stack.rotate(Vector3f.ZP.rotationDegrees(-30 * anim)); // Поворот вокруг Z
                break;

            case 4: // Анимация "Переворот"
                stack.scale(scale.get(), scale.get(), scale.get());
                stack.translate(0.0f, 0.3f, -0.5f);
                stack.rotate(Vector3f.YP.rotationDegrees(-90 * anim)); // Поворот по Y
                stack.rotate(Vector3f.ZP.rotationDegrees(90 * anim)); // Поворот по Z
                stack.rotate(Vector3f.XP.rotationDegrees(60 * anim)); // Подъем руки
                break;

            default:
                stack.scale(scale.get(), scale.get(), scale.get());
                runnable.run();
                break;
        }
    }
}
