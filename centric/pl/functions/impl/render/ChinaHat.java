package centric.pl.functions.impl.render;

import centric.pl.events.impl.WorldEvent;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.math.MathUtil;
import centric.pl.johon0.utils.render.ColorUtils;
import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;

@FunctionRegister(name = "China Hat", type = Category.Render, beta = false)
public class ChinaHat extends Function {

    @Subscribe
    private void onRender(WorldEvent e) {
        if (IMinecraft.mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON) return;
        float radius = 0.6f;

        GlStateManager.pushMatrix();

        RenderSystem.translated(-IMinecraft.mc.getRenderManager().info.getProjectedView().x, -IMinecraft.mc.getRenderManager().info.getProjectedView().y, -IMinecraft.mc.getRenderManager().info.getProjectedView().z);
        Vector3d interpolated = MathUtil.interpolate(IMinecraft.mc.player.getPositionVec(), new Vector3d(IMinecraft.mc.player.lastTickPosX, IMinecraft.mc.player.lastTickPosY, IMinecraft.mc.player.lastTickPosZ), e.getPartialTicks());
        interpolated.y -= 0.05f;
        RenderSystem.translated(interpolated.x, interpolated.y + IMinecraft.mc.player.getHeight(), interpolated.z);
        final double yaw = IMinecraft.mc.getRenderManager().info.getYaw();

        GL11.glRotatef((float) -yaw, 0f, 1f, 0f);

        float height = IMinecraft.mc.player.container.getSlot(5).getStack().isEmpty() ? 0.02F : 0.11F;

        RenderSystem.translated(-interpolated.x, -(interpolated.y + IMinecraft.mc.player.getHeight()) + height, -interpolated.z);

        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        RenderSystem.disableTexture();
        RenderSystem.disableCull();
        RenderSystem.blendFunc(770, 771);
        RenderSystem.shadeModel(7425);
        RenderSystem.lineWidth(3);

        int steps = 40;
        double angleStep = 2 * Math.PI / steps;

        float time = (System.currentTimeMillis() % 2000) / 2000f; // Цикл каждые 2 секунды

        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

        // Основной круг
        IMinecraft.buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        IMinecraft.buffer.pos(interpolated.x, interpolated.y + IMinecraft.mc.player.getHeight() + 0.3, interpolated.z).color(255, 255, 255, 128).endVertex();
        for (int i = 0; i <= steps; i++) {
            float x = (float) (interpolated.x + Math.sin(i * angleStep) * radius);
            float z = (float) (interpolated.z + -Math.cos(i * angleStep) * radius);

            // Переливающийся цвет с анимацией
            int r = (int) (Math.sin((time + (i / (float)steps)) * Math.PI * 2) * 127 + 128);
            int g = (int) (Math.sin((time + (i / (float)steps) + (Math.PI / 2)) * Math.PI * 2) * 127 + 128);
            int b = (int) (Math.sin((time + (i / (float)steps) + Math.PI) * Math.PI * 2) * 127 + 128);
            IMinecraft.buffer.pos(x, interpolated.y + IMinecraft.mc.player.getHeight(), z).color(r, g, b, 128).endVertex();
        }
        IMinecraft.tessellator.draw();

        // Обводка
        IMinecraft.buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= steps; i++) {
            float x = (float) (interpolated.x + Math.sin(i * angleStep) * radius);
            float z = (float) (interpolated.z + -Math.cos(i * angleStep) * radius);

            // Переливающийся цвет для обводки
            int r = (int) (Math.sin((time + (i / (float)steps)) * Math.PI * 2) * 127 + 128);
            int g = (int) (Math.sin((time + (i / (float)steps) + (Math.PI / 2)) * Math.PI * 2) * 127 + 128);
            int b = (int) (Math.sin((time + (i / (float)steps) + Math.PI) * Math.PI * 2) * 127 + 128);
            IMinecraft.buffer.pos(x, interpolated.y + IMinecraft.mc.player.getHeight(), z).color(r, g, b, 100).endVertex();
        }
        IMinecraft.tessellator.draw();

        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_DONT_CARE);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.shadeModel(7424);
        GlStateManager.popMatrix();
    }


}