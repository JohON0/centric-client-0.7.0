package eva.ware.modules.impl.visual;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import eva.ware.events.EventChangeWorld;
import eva.ware.events.EventPreRender3D;
import eva.ware.events.EventJump;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.SliderSetting;
import eva.ware.utils.render.color.ColorUtility;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@ModuleRegister(name = "JumpCircle", category = Category.Display)
public class JumpCircle extends Module {
    private final SliderSetting radius = new SliderSetting("Радиус", 1, 0.1f, 2, 0.01f);
    private final SliderSetting shadow = new SliderSetting("Тень", 60, 10, 100, 0.01f);
    private final SliderSetting speed = new SliderSetting("Скорость", 1, 1, 5, 0.01f);

    private final List<Circle> circles = new ArrayList<>();
    private final String staticLoc = "eva/images/modules/jumpcircles/";

    public JumpCircle() {
        addSettings(radius, shadow, speed);
    }

    @Subscribe
    private void onJump(EventJump e) {
        addCircle();
    }

    @Subscribe
    private void onRender(EventPreRender3D event) {
        updateCircles();
        renderCircles(event.getMatrix());
    }

    private void addCircle() {
        circles.add(new Circle((float) mc.player.getPosX(), (float) mc.player.getPosY(), (float) mc.player.getPosZ()));
    }

    private void updateCircles() {
        for (Circle circle : circles) {
            circle.alpha = MathHelper.clamp(circle.alpha - speed.get().floatValue() * 0.1f, 0, 1);
        }
        circles.removeIf(circle -> circle.alpha <= 0.005f); // Убираем практически невидимые круги
    }

    private void renderCircles(MatrixStack stack) {
        setupRenderSettings();

        for (Circle circle : circles) {
            drawJumpCircle(circle, radius.get().floatValue(), circle.alpha);
        }

        restoreRenderSettings();
    }

    private void setupRenderSettings() {
        RenderSystem.pushMatrix();
        RenderSystem.disableLighting();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        RenderSystem.disableCull();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void restoreRenderSettings() {
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.popMatrix();
    }

    private void drawJumpCircle(Circle circle, float radius, float alpha) {
        double x = circle.spawnX;
        double y = circle.spawnY + 0.1; // Немного приподнимаем по Y
        double z = circle.spawnZ;

        // Перемещаемся для рисования круга
        RenderSystem.translated(x, y, z);
        mc.getTextureManager().bindTexture(new ResourceLocation(staticLoc + "glow.png")); // Используем текстуру Glow

        // Отрисовка круга
        buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR_TEX);
        buffer.pos(0, 0, 0).color(1.0f, 1.0f, 1.0f, alpha).tex(0.5f, 0.5f).endVertex(); // Центр круга

        for (int i = 0; i <= 360; i++) {
            double angleRad = Math.toRadians(i);
            double sin = MathHelper.sin((float) angleRad) * radius;
            double cos = MathHelper.cos((float) angleRad) * radius;

            buffer.pos(sin, 0, cos)
                    .color(1.0f, 1.0f, 1.0f, alpha) // Цвет круга
                    .tex(0.5f + (float)(sin / (2 * radius)), 0.5f + (float)(cos / (2 * radius)))
                    .endVertex();
        }

        tessellator.draw();
        RenderSystem.translated(-x, -y, -z); // Возвращаемся обратно
    }

    private class Circle {
        public final float spawnX;
        public final float spawnY;
        public final float spawnZ;
        public float alpha;

        public Circle(float spawnX, float spawnY, float spawnZ) {
            this.spawnX = spawnX;
            this.spawnY = spawnY;
            this.spawnZ = spawnZ;
            this.alpha = 1; // Начальное значение alpha
        }
    }
}
