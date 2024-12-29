//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package centric.pl.functions.impl.render;

import centric.pl.Main;
import centric.pl.events.impl.EventDisplay;
import centric.pl.events.impl.WorldEvent;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.impl.combat.KillAura;
import centric.pl.functions.settings.impl.ModeSetting;
import centric.pl.functions.settings.impl.SliderSetting;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.math.MathUtil;
import centric.pl.johon0.utils.math.Vector4i;
import centric.pl.johon0.utils.projections.ProjectionUtil;
import centric.pl.johon0.utils.render.ColorUtils;
import centric.pl.johon0.utils.render.DisplayUtils;
import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.optifine.shaders.Shaders;
import org.lwjgl.opengl.GL11;

import static com.mojang.blaze3d.platform.GlStateManager.GL_QUADS;
import static com.mojang.blaze3d.systems.RenderSystem.depthMask;
import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_COLOR_TEX;

/**
 * @author JohON0 // attack.dev // 10.08.2024
 */
@FunctionRegister(
        name = "TargetESP",
        type = Category.Render, beta = false
)
public class TargetESP extends Function {
    private final KillAura killAura;
    public static long startTime = System.currentTimeMillis();
    public ModeSetting mod = new ModeSetting("Мод", "Квадрат", "Квадрат", "Призраки","Кругляшок","Окружность");
    public SliderSetting speed = (new SliderSetting("Скорость", 3.0F, 0.7F, 9.0F, 1.0F)).setVisible(() -> {
        return this.mod.is("Призраки");
    });
    public SliderSetting size = (new SliderSetting("Размер", 30.0F, 5.0F, 140.0F, 1.0F)).setVisible(() -> {
        return this.mod.is("Призраки");
    });
    public SliderSetting bright = (new SliderSetting("Яркость", 255.0F, 1.0F, 255.0F, 1.0F)).setVisible(() -> {
        return this.mod.is("Призраки");
    });
    long lastTime = System.currentTimeMillis();

    public TargetESP(KillAura killAura) {
        this.killAura = killAura;
        this.addSettings(this.mod, this.speed, this.size, this.bright);
    }

    @Subscribe
    private void onDisplay(EventDisplay e) {
        double sin;
        float size;
        Vector3d interpolated;
        Vector2f pos;
        if (this.mod.is("Квадрат")) {
            if (e.getType() != EventDisplay.Type.PRE) {
                return;
            }

            if (this.killAura.isState() && this.killAura.getTarget() != null) {
                sin = Math.sin((double) System.currentTimeMillis() / 1000.0);
                size = 100.0F;
                interpolated = this.killAura.getTarget().getPositon(e.getPartialTicks());
                pos = ProjectionUtil.project(interpolated.x, interpolated.y + (double) (this.killAura.getTarget().getHeight() / 2.0F), interpolated.z);
                GlStateManager.pushMatrix();
                GlStateManager.translatef(pos.x, pos.y, 0.0F);
                GlStateManager.rotatef((float) sin * 360.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.translatef(-pos.x, -pos.y, 0.0F);
                DisplayUtils.drawImage(new ResourceLocation("centric/images/target.png"), pos.x - size / 2.0F, pos.y - size / 2.0F, size, size, new Vector4i(ColorUtils.rgb(255, 255, 255), ColorUtils.setAlpha(HUD.getColor(90, 1.0F), 220), ColorUtils.setAlpha(HUD.getColor(180, 1.0F), 220), ColorUtils.setAlpha(HUD.getColor(270, 1.0F), 220)));
                GlStateManager.popMatrix();
            }
        }
    }

    @Subscribe
    private void onWorldEvent(WorldEvent e) {
        if (this.mod.is("Призраки")) {
            MatrixStack stack = new MatrixStack();
            EntityRendererManager rm = IMinecraft.mc.getRenderManager();
            float c = (float) ((double) ((float) (System.currentTimeMillis() - startTime) / 1500.0F) + Math.sin((double) ((float) (System.currentTimeMillis() - startTime) / 1500.0F)) / 10.0);

            // Получаем цель
            Entity target = this.killAura.getTarget();

            // Проверяем, не равна ли цель null
            if (target != null) {
                double x = target.lastTickPosX + (target.getPosX() - target.lastTickPosX) * (double) e.getPartialTicks() - rm.info.getProjectedView().getX();
                double y = target.lastTickPosY + (target.getPosY() - target.lastTickPosY) * (double) e.getPartialTicks() - rm.info.getProjectedView().getY();
                double z = target.lastTickPosZ + (target.getPosZ() - target.lastTickPosZ) * (double) e.getPartialTicks() - rm.info.getProjectedView().getZ();
                float alpha = Shaders.shaderPackLoaded ? 1.0F : 0.5F;
                alpha *= 0.2F;
                float pl = 0.0F;
                boolean fa = true;

                for (int b = 0; b < 3; ++b) {
                    for (float i = c * 360.0F; i < c * 360.0F + 70.0F; i += 2.0F) {
                        float min = c * 360.0F;
                        float max = c * 360.0F + 70.0F;
                        float dc = MathHelper.normalize(i, c * 360.0F - 45.0F, max);
                        int color = HUD.getColor(0);
                        int color2 = HUD.getColor(90);
                        float rf = 0.7F;
                        double radians = Math.toRadians((double) i);
                        double plY = (double) pl + Math.sin(radians * 1.2000000476837158) * 0.10000000149011612;
                        stack.push();
                        stack.translate(x, y, z);
                        stack.rotate(Vector3f.YP.rotationDegrees(-rm.info.getYaw()));
                        depthMask(false);
                        float q = (!fa ? 0.15F : 0.15F) * (Math.max(fa ? 0.15F : 0.15F, fa ? dc : (1.0F - -(0.4F - dc)) / 2.0F) + 0.45F);
                        float w = q * (1.5F + (0.5F - alpha) * 1.5F);
                        DisplayUtils.drawImage(stack, new ResourceLocation("centric/images/glow.png"), Math.cos(radians) * (double) rf - (double) (w / 2.0F), plY + 1.0 - 0.7, Math.sin(radians) * (double) rf - (double) (w / 2.0F), (double) w, (double) w, color, color2, color2, color);
                        GL11.glEnable(2929);
                        depthMask(true);
                        stack.pop();
                    }
                    c *= -1.25F;
                    fa = !fa;
                    pl += 0.45F;
                }
            }
        }
        if (this.mod.is("Окружность")) {
            KillAura killAura = Main.getInstance().getFunctionRegistry().getHitAura();
            if (killAura.isState() && killAura.getTarget() != null) {
                MatrixStack ms = new MatrixStack();
                ms.push();
                RenderSystem.pushMatrix();

                RenderSystem.disableLighting();
                RenderSystem.depthMask(false);
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE); // Аддитивное смешивание
                RenderSystem.shadeModel(GL11.GL_SMOOTH);
                RenderSystem.disableCull();

                Vector3d targetPos = killAura.getTarget().getPositionVec();
                ms.translate(-mc.getRenderManager().info.getProjectedView().getX(), -mc.getRenderManager().info.getProjectedView().getY(), -mc.getRenderManager().info.getProjectedView().getZ());
                ms.translate(targetPos.x, targetPos.y + 0.8, targetPos.z); // Повышаем на 0.5 для центрального положения

                // Устанавливаем параметры анимации
                long time = System.currentTimeMillis();
                float pulsateScale = 2.0f + (float) Math.sin(time * 0.004) * 0.5f; // Увеличенный масштаб пульсации от 1.5 до 2.5
                float alpha = Math.abs((float) Math.sin(time * 0.003)) * 255; // Изменение альфа от 0 до 255

                mc.getTextureManager().bindTexture(new ResourceLocation("centric/images/glow.png")); // Текстура для эффекта

                // Яркий цвет (например, красный)
                int brightColor = HUD.getColor(100); // Получение клиентского цвета
                int brightAlphaColor = DisplayUtils.reAlphaInt(brightColor, (int) alpha);

                // Отрисовка яркого пульсирующего круга
                buffer.begin(GL_QUADS, POSITION_COLOR_TEX);

                // Рисуем квадраты с пульсирующим масштабом
                buffer.pos(ms.getLast().getMatrix(), -0.5f * pulsateScale, -0.5f * pulsateScale, 0).color(brightAlphaColor).tex(0, 0).endVertex();
                buffer.pos(ms.getLast().getMatrix(), 0.5f * pulsateScale, -0.5f * pulsateScale, 0).color(brightAlphaColor).tex(1, 0).endVertex();
                buffer.pos(ms.getLast().getMatrix(), 0.5f * pulsateScale, 0.5f * pulsateScale, 0).color(brightAlphaColor).tex(1, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -0.5f * pulsateScale, 0.5f * pulsateScale, 0).color(brightAlphaColor).tex(0, 1).endVertex();

                tessellator.draw();

                RenderSystem.defaultBlendFunc();
                RenderSystem.disableBlend();
                RenderSystem.enableCull();
                RenderSystem.depthMask(true);
                RenderSystem.popMatrix();
                ms.pop();
            }
        }
         if (this.mod.is("Кругляшок")) {
             KillAura killAura = Main.getInstance().getFunctionRegistry().getHitAura();
            if (killAura.isState() && killAura.getTarget() != null) {
                MatrixStack ms = new MatrixStack();
                ms.push();
                RenderSystem.pushMatrix();

                RenderSystem.disableLighting();
                RenderSystem.depthMask(false);
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE); // Аддитивное смешивание
                RenderSystem.shadeModel(GL11.GL_SMOOTH);
                RenderSystem.disableCull();

                double radius = 0.65f; // Радиус круга
                float size = 0.4f; // Размер текстуры
                int textureCount = 8; // Количество текстур
                int length = 120; // Длина эффекта
                int maxAlpha = 50; // Максимальная альфа-значение для эффекта
                float rotationAngle = 15.0f; // Угол поворота

                ActiveRenderInfo camera = mc.getRenderManager().info;
                Vector3d targetPos = killAura.getTarget().getPositionVec();
                ms.translate(-camera.getProjectedView().getX(), -camera.getProjectedView().getY(), -camera.getProjectedView().getZ());

                // Вычисляем смещение по высоте с использованием синусоиды
                double animationOffset = Math.sin(System.currentTimeMillis() * 0.006) * 0.1; // Смещение по Y
                Vector3d interpolated = MathUtil.interpolate(targetPos, new Vector3d(killAura.getTarget().lastTickPosX, killAura.getTarget().lastTickPosY, killAura.getTarget().lastTickPosZ), e.getPartialTicks());

                // Плавно поднимаем и опускаем по высоте сущности
                interpolated.y += animationOffset; // Добавляем анимационное смещение
                ms.translate(interpolated.x, interpolated.y, interpolated.z);

                // Применяем поворот на 45 градусов
                ms.rotate(Vector3f.ZP.rotationDegrees(rotationAngle));

                mc.getTextureManager().bindTexture(new ResourceLocation("centric/images/glow.png"));

                // Получаем цвет клиента
                int clientColor = Main.getInstance().getStyleManager().getCurrentStyle().getColor(100); // Get the client color


                for (int j = 0; j < textureCount; j++) { // Рендерим несколько текстур
                    for (int i = 0; i < length; i++) {
                        double angle = (System.currentTimeMillis() + i * 100) * 0.002 + j * (Math.PI / textureCount);
                        double s = Math.sin(angle) * radius+0.2;
                        double c = Math.cos(angle) * radius;

                        ms.push();
                        ms.translate(s, 1, -c);
                        ms.rotate(camera.getRotation());

                        int alpha = MathHelper.clamp(maxAlpha - (i * (maxAlpha / length)), 0, maxAlpha);
                        buffer.begin(GL_QUADS, POSITION_COLOR_TEX);

                        // Применяем цвет клиента с изменённой альфа-прозрачностью
                        buffer.pos(ms.getLast().getMatrix(), -size / 2f, -size / 2f, 0).color(DisplayUtils.reAlphaInt(clientColor, alpha)).tex(0, 0).endVertex();
                        buffer.pos(ms.getLast().getMatrix(), size / 2f, -size / 2f, 0).color(DisplayUtils.reAlphaInt(clientColor, alpha)).tex(1, 0).endVertex();
                        buffer.pos(ms.getLast().getMatrix(), size / 2f, size / 2f, 0).color(DisplayUtils.reAlphaInt(clientColor, alpha)).tex(1, 1).endVertex();
                        buffer.pos(ms.getLast().getMatrix(), -size / 2f, size / 2f, 0).color(DisplayUtils.reAlphaInt(clientColor, alpha)).tex(0, 1).endVertex();

                        tessellator.draw();
                        ms.pop();
                    }
                }

                RenderSystem.defaultBlendFunc();
                RenderSystem.disableBlend();
                RenderSystem.enableCull();
                depthMask(true);
                RenderSystem.popMatrix();
                ms.pop();
            }
        }
    }
}
