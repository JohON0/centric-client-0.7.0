package centric.pl.functions.impl.render;

import java.util.List;

import centric.pl.events.impl.WorldEvent;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.settings.impl.BooleanSetting;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.math.MathUtil;
import centric.pl.johon0.utils.math.StopWatch;
import centric.pl.johon0.utils.render.ColorUtils;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import org.lwjgl.opengl.GL11;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.systems.RenderSystem;

import lombok.Getter;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
@FunctionRegister(name = "Trails", type = Category.Render, beta = false)
public class Trails extends Function {
    private final BooleanSetting firstperson = new BooleanSetting("От первого лица", false);



    public Trails() {
        addSettings(firstperson);
    }
    @Subscribe
    public void onRender(WorldEvent event) {
        // Обновление точек для всех игроков
        for (PlayerEntity entity : IMinecraft.mc.world.getPlayers()) {
            entity.points.removeIf(p -> p.time.isReached(500));
            if (!firstperson.get()) {
                if (entity instanceof ClientPlayerEntity && entity == IMinecraft.mc.player
                        && IMinecraft.mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON) {
                    continue; // Пропускаем игрока в первом лице
                }
            }

            Vector3d playerPos = new Vector3d(
                    MathUtil.interpolate(entity.getPosX(), entity.lastTickPosX, event.getPartialTicks()),
                    MathUtil.interpolate(entity.getPosY(), entity.lastTickPosY, event.getPartialTicks()),
                    MathUtil.interpolate(entity.getPosZ(), entity.lastTickPosZ, event.getPartialTicks())
            );

            entity.points.add(new Point(playerPos));
        }

        // Подготовка к рендерингу
        RenderSystem.pushMatrix();
        Vector3d projection = IMinecraft.mc.getRenderManager().info.getProjectedView();
        RenderSystem.translated(-projection.x, -projection.y, -projection.z);
        setupRenderSettings();

        // Рендеринг трейлов
        for (Entity entity : IMinecraft.mc.world.getAllEntities()) {
            List<Point> points = entity.points;
            if (points.isEmpty()) continue; // Пропускаем, если нет точек

            renderTrail(points, entity);
        }

        cleanupRenderSettings();
        RenderSystem.popMatrix(); // Возвращаем матрицу в предыдущее состояние
    }

    private void setupRenderSettings() {
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.disableTexture();
        RenderSystem.blendFunc(770, 771);
        RenderSystem.shadeModel(7425);
        RenderSystem.disableAlphaTest();
        RenderSystem.depthMask(false);
        RenderSystem.lineWidth(3);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
    }

    private void cleanupRenderSettings() {
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_DONT_CARE);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableCull();
        RenderSystem.shadeModel(7424);
        RenderSystem.depthMask(true);
    }

    private void renderTrail(List<Point> points, Entity entity) {
        renderQuadStrip(points, entity);
        renderAnimatedLineStrip(points, entity, false); // Нижняя линия с анимацией
        renderAnimatedLineStrip(points, entity, true);  // Верхняя линия с анимацией
    }

    private void renderAnimatedLineStrip(List<Point> points, Entity entity, boolean isUpper) {
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            float alpha = (float) i / points.size();
            // Генерация случайного значения для изменения цвета
            float oscillation = (float) Math.sin(System.currentTimeMillis() * 0.001 + i) * 0.5f + 0.5f;
            ColorUtils.setAlphaColor(HUD.getColor(i, 2), alpha * oscillation);
            GL11.glVertex3d(point.getPosition().x, point.getPosition().y + (isUpper ? entity.getHeight() : 0), point.getPosition().z);
        }
        GL11.glEnd();
    }


    private void renderQuadStrip(List<Point> points, Entity entity) {
        GL11.glBegin(GL11.GL_QUAD_STRIP);
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            float alpha = (float) i / points.size();
            ColorUtils.setAlphaColor(HUD.getColor(i, 2), alpha * 0.5f);
            GL11.glVertex3d(point.getPosition().x, point.getPosition().y, point.getPosition().z);
            GL11.glVertex3d(point.getPosition().x, point.getPosition().y + entity.getHeight(), point.getPosition().z);
        }
        GL11.glEnd();
    }

    @Getter
    public static class Point {
        private final Vector3d position;
        private final StopWatch time = new StopWatch();

        public Point(Vector3d position) {
            this.position = position;
        }
    }
}