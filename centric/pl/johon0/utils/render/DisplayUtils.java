package centric.pl.johon0.utils.render;

import centric.pl.johon0.utils.shader.ShaderUtil;
import com.jhlabs.image.GaussianFilter;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import centric.pl.johon0.utils.client.IMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.entity.LivingEntity;
import org.joml.Vector4i;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector4f;
import net.optifine.util.TextureUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.*;
import static org.lwjgl.opengl.GL11C.GL_LINE_STRIP;

public class DisplayUtils implements IMinecraft {

    public static void quads(float x, float y, float width, float height, int glQuads, int color) {
        buffer.begin(glQuads, POSITION_TEX_COLOR);
        {
            buffer.pos(x, y, 0).tex(0, 0).color(color).endVertex();
            buffer.pos(x, y + height, 0).tex(0, 1).color(color).endVertex();
            buffer.pos(x + width, y + height, 0).tex(1, 1).color(color).endVertex();
            buffer.pos(x + width, y, 0).tex(1, 0).color(color).endVertex();
        }
        tessellator.draw();
    }
    public static void drawImage(ResourceLocation resourceLocation, float x, float y, float width, float height, centric.pl.johon0.utils.math.Vector4i color) {
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);
        mc.getTextureManager().bindTexture(resourceLocation);
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos((double)x, (double)y, 0.0).tex(0.0F, 0.0F).color(color.x).endVertex();
        buffer.pos((double)x, (double)(y + height), 0.0).tex(0.0F, 1.0F).color(color.y).endVertex();
        buffer.pos((double)(x + width), (double)(y + height), 0.0).tex(1.0F, 1.0F).color(color.z).endVertex();
        buffer.pos((double)(x + width), (double)y, 0.0).tex(1.0F, 0.0F).color(color.w).endVertex();
        tessellator.draw();
        RenderSystem.shadeModel(7424);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.popMatrix();
    }

    /**
     *
     * @param x расположение по координате x
     * @param y расположение по координате y
     * @param width width головы
     * @param height длинна головы
     * @param radius радиус закругления
     * @param target представление игрока на клиентской стороне
     */

    public static void drawRoundFace(float x, float y, float width, float height, float radius, float alpha, AbstractClientPlayerEntity target) {
        try {
            ResourceLocation skin = target.getLocationSkin();
            IMinecraft.mc.getTextureManager().bindTexture(skin);
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(770, 771);
            ShaderUtil.faceRound.attach();
            ShaderUtil.faceRound.setUniform("location", x * 2.0f, (IMinecraft.window.getHeight() - (height * 2.0f)) - (y * 2.0f));
            ShaderUtil.faceRound.setUniform("size", width * 2.0f, height * 2.0f);
            ShaderUtil.faceRound.setUniform("texture", 0);
            ShaderUtil.faceRound.setUniform("radius", radius * 2.0f);
            ShaderUtil.faceRound.setUniform("alpha", alpha);
            ShaderUtil.faceRound.setUniform("u", 0.125f);
            ShaderUtil.faceRound.setUniform("v", 0.125f);
            ShaderUtil.faceRound.setUniform("w", 0.125f);
            ShaderUtil.faceRound.setUniform("h", 0.125f);
            quadsBegin(x, y, width, height, 7);
            ShaderUtil.faceRound.detach();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void quadsBegin(float x, float y, float width, float height, int glQuads) {
        buffer.begin(glQuads, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y, 0.0d).tex(0.0f, 0.0f).endVertex();
        buffer.pos(x, y + height, 0.0d).tex(0.0f, 1.0f).endVertex();
        buffer.pos(x + width, y + height, 0.0d).tex(1.0f, 1.0f).endVertex();
        buffer.pos(x + width, y, 0.0d).tex(1.0f, 0.0f).endVertex();
        Tessellator.getInstance().draw();
    }

    public static void drawShadowFace(float x, float y, int width, int height, float radius, AbstractClientPlayerEntity target) {
        // Инициализация буфера трафарета для записи
        StencilUtil.initStencilToWrite();
        // Рисуем закругленный прямоугольник
        DisplayUtils.drawShadow(x, y,  width, height, (int) radius, -1);
        // Читаем буфер трафарета для применения маски трафарета
        StencilUtil.readStencilBuffer(1);
        // Рисуем лицо в пределах маски трафарета
        drawFace(x, y, 8.0f, 8.0f, 8, 8, width, height, 64.0f, 64.0f, target);
        // Деинициализация буфера трафарета
        StencilUtil.uninitStencilBuffer();

    }


    public static void drawFace(float x, float y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight, AbstractClientPlayerEntity target) {
        try {
            ResourceLocation skin = target.getLocationSkin();
            GL11.glPushMatrix();
            GL11.glEnable(3042);
            mc.getTextureManager().bindTexture(skin);
            float hurtPercent = ((float)target.hurtTime - (target.hurtTime != 0 ? mc.timer.renderPartialTicks : 0.0F)) / 30.0F;
            GL11.glColor4f(1.0F, 1.0F - hurtPercent, 1.0F - hurtPercent, 1.0F);
            AbstractGui.drawScaledCustomSizeModalRect(x, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glPopMatrix();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void drawImage(MatrixStack stack, ResourceLocation image, double x, double y, double z, double width, double height, int color1, int color2, int color3, int color4) {
        Minecraft minecraft = Minecraft.getInstance();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 1);
        GL11.glShadeModel(7425);
        GL11.glAlphaFunc(516, 0.0F);
        minecraft.getTextureManager().bindTexture(image);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP);
        bufferBuilder.pos(stack.getLast().getMatrix(), (float)x, (float)(y + height), (float)z).color(color1 >> 16 & 255, color1 >> 8 & 255, color1 & 255, color1 >>> 24).tex(0.0F, 0.99F).lightmap(0, 240).endVertex();
        bufferBuilder.pos(stack.getLast().getMatrix(), (float)(x + width), (float)(y + height), (float)z).color(color2 >> 16 & 255, color2 >> 8 & 255, color2 & 255, color2 >>> 24).tex(1.0F, 0.99F).lightmap(0, 240).endVertex();
        bufferBuilder.pos(stack.getLast().getMatrix(), (float)(x + width), (float)y, (float)z).color(color3 >> 16 & 255, color3 >> 8 & 255, color3 & 255, color3 >>> 24).tex(1.0F, 0.0F).lightmap(0, 240).endVertex();
        bufferBuilder.pos(stack.getLast().getMatrix(), (float)x, (float)y, (float)z).color(color4 >> 16 & 255, color4 >> 8 & 255, color4 & 255, color4 >>> 24).tex(0.0F, 0.0F).lightmap(0, 240).endVertex();
        tessellator.draw();
        RenderSystem.disableBlend();
    }
    public static void scissor(double x, double y, double width, double height) {

        final double scale = mc.getMainWindow().getGuiScaleFactor();

        y = mc.getMainWindow().getScaledHeight() - y;

        x *= scale;
        y *= scale;
        width *= scale;
        height *= scale;

        GL11.glScissor((int) x, (int) (y - height), (int) width, (int) height);
    }
    public static int reAlphaInt(final int color, final int alpha) {
        return (MathHelper.clamp(alpha, 0, 255) << 24) | (color & 16777215);
    }

    private static final HashMap<Integer, Integer> shadowCache = new HashMap<Integer, Integer>();
    public static void drawGradientRound(float x, float y, float width, float height, float radius, int bottomLeft, int topLeft, int bottomRight, int topRight) {
        RenderSystem.color4f(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        ShaderUtil.gradientRound.attach();
        ShaderUtil.setupRoundedRectUniforms(x, y, width, height, radius, ShaderUtil.gradientRound);

        ShaderUtil.gradientRound.setUniform("color1", ColorUtils.IntColor.rgb(bottomLeft));
        ShaderUtil.gradientRound.setUniform("color2", ColorUtils.IntColor.rgb(topLeft));
        ShaderUtil.gradientRound.setUniform("color3", ColorUtils.IntColor.rgb(bottomRight));
        ShaderUtil.gradientRound.setUniform("color4", ColorUtils.IntColor.rgb(topRight));

        ShaderUtil.drawQuads(x -1,y -1,width + 2,height + 2);
        ShaderUtil.gradientRound.detach();
        RenderSystem.disableBlend();
    }

    public static void drawShadow(float x, float y, float width, float height, int radius, int color, int i) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.01f);
        GlStateManager.disableAlphaTest();
        GL11.glShadeModel(7425);

        x -= radius;
        y -= radius;
        width = width + radius * 2;
        height = height + radius * 2;
        x -= 0.25f;
        y += 0.25f;

        int identifier = Objects.hash(width, height, radius);
        int textureId;

        if (shadowCache.containsKey(identifier)) {
            textureId = shadowCache.get(identifier);
            GlStateManager.bindTexture(textureId);
        } else {
            if (width <= 0) {
                width = 1;
            }

            if (height <= 0) {
                height = 1;
            }

            BufferedImage originalImage = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB_PRE);
            Graphics2D graphics = originalImage.createGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(radius, radius, (int) (width - radius * 2), (int) (height - radius * 2));
            graphics.dispose();

            GaussianFilter filter = new GaussianFilter(radius);
            BufferedImage blurredImage = filter.filter(originalImage, null);
            DynamicTexture texture = new DynamicTexture(TextureUtils.toNativeImage(blurredImage));
            texture.setBlurMipmap(true, true);
            textureId = texture.getGlTextureId();
            shadowCache.put(identifier, textureId);
        }

        float[] startColorComponents = ColorUtils.rgba(color);
        float[] i1 = ColorUtils.rgba(i);
        buffer.begin(GL11.GL_QUADS, POSITION_COLOR_TEX);
        buffer.pos(x, y, 0.0f)
                .color(startColorComponents[0], startColorComponents[1], startColorComponents[2],
                        startColorComponents[3])
                .tex(0.0f, 0.0f)
                .endVertex();

        buffer.pos(x, y + (float) ((int) height), 0.0f)
                .color(startColorComponents[0], startColorComponents[1], startColorComponents[2],
                        startColorComponents[3])
                .tex(0.0f, 1.0f)
                .endVertex();

        buffer.pos(x + (float) ((int) width), y + (float) ((int) height), 0.0f)
                .color(i1[0], i1[1], i1[2],
                        i1[3])
                .tex(1.0f, 1.0f)
                .endVertex();

        buffer.pos(x + (float) ((int) width), y, 0.0f)
                .color(i1[0], i1[1], i1[2],
                        i1[3])
                .tex(1.0f, 0.0f)
                .endVertex();

        tessellator.draw();
        GlStateManager.enableAlphaTest();
        GL11.glShadeModel(7424);
        GlStateManager.bindTexture(0);
        GlStateManager.disableBlend();
    }
    public static void drawShadow(float x, float y, float width, float height, int radius, int bottomLeft, int topLeft, int bottomRight, int topRight) {
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA.param,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.param, GlStateManager.SourceFactor.ONE.param,
                GlStateManager.DestFactor.ZERO.param);
        GlStateManager.shadeModel(7425);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.01f);

        x -= radius;
        y -= radius;
        width = width + radius * 2;
        height = height + radius * 2;
        x -= 0.25f;
        y += 0.25f;

        int identifier = Objects.hash(width, height, radius);
        int textureId;

        if (shadowCache.containsKey(identifier)) {
            textureId = shadowCache.get(identifier);
            GlStateManager.bindTexture(textureId);
        } else {
            if (width <= 0) {
                width = 1;
            }

            if (height <= 0) {
                height = 1;
            }

            BufferedImage originalImage = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB_PRE);
            Graphics2D graphics = originalImage.createGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(radius, radius, (int) (width - radius * 2), (int) (height - radius * 2));
            graphics.dispose();

            GaussianFilter filter = new GaussianFilter(radius);
            BufferedImage blurredImage = filter.filter(originalImage, null);
            DynamicTexture texture = new DynamicTexture(TextureUtils.toNativeImage(blurredImage));
            texture.setBlurMipmap(true, true);
            textureId = texture.getGlTextureId();
            shadowCache.put(identifier, textureId);
        }

        float[] bottomLefts = ColorUtils.IntColor.rgb(bottomLeft);
        float[] topLefts = ColorUtils.IntColor.rgb(topLeft);
        float[] bottomRights = ColorUtils.IntColor.rgb(bottomRight);
        float[] topRights = ColorUtils.IntColor.rgb(topRight);

        buffer.begin(GL11.GL_QUADS, POSITION_COLOR_TEX);
        buffer.pos(x, y, 0.0f)
                .color(bottomLefts[0], bottomLefts[1], bottomLefts[2], bottomLefts[3])
                .tex(0.0f, 0.0f)
                .endVertex();

        buffer.pos(x, y + (float) ((int) height), 0.0f)
                .color(topLefts[0], topLefts[1], topLefts[2], topLefts[3])
                .tex(0.0f, 1.0f)
                .endVertex();

        buffer.pos(x + (float) ((int) width), y + (float) ((int) height), 0.0f)
                .color(topRights[0], topRights[1], topRights[2], topRights[3])
                .tex(1.0f, 1.0f)
                .endVertex();

        buffer.pos(x + (float) ((int) width), y, 0.0f)
                .color(bottomRights[0], bottomRights[1], bottomRights[2], bottomRights[3])
                .tex(1.0f, 0.0f)
                .endVertex();

        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.bindTexture(0);
        GlStateManager.disableBlend();
    }

    public static void drawShadowVertical(float x, float y, float width, float height, int radius, int color, int i) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.01f);
        GlStateManager.disableAlphaTest();
        GL11.glShadeModel(7425);

        x -= radius;
        y -= radius;
        width = width + radius * 2;
        height = height + radius * 2;
        x -= 0.25f;
        y += 0.25f;

        int identifier = Objects.hash(width, height, radius);
        int textureId;

        if (shadowCache.containsKey(identifier)) {
            textureId = shadowCache.get(identifier);
            GlStateManager.bindTexture(textureId);
        } else {
            if (width <= 0) {
                width = 1;
            }

            if (height <= 0) {
                height = 1;
            }

            BufferedImage originalImage = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB_PRE);
            Graphics2D graphics = originalImage.createGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(radius, radius, (int) (width - radius * 2), (int) (height - radius * 2));
            graphics.dispose();

            GaussianFilter filter = new GaussianFilter(radius);
            BufferedImage blurredImage = filter.filter(originalImage, null);
            DynamicTexture texture = new DynamicTexture(TextureUtils.toNativeImage(blurredImage));
            texture.setBlurMipmap(true, true);
            try {
                textureId = texture.getGlTextureId();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            shadowCache.put(identifier, textureId);
        }

        float[] startColorComponents = ColorUtils.rgba(color);
        float[] i1 = ColorUtils.rgba(i);
        buffer.begin(GL11.GL_QUADS, POSITION_COLOR_TEX);
        buffer.pos(x, y, 0.0f)
                .color(startColorComponents[0], startColorComponents[1], startColorComponents[2],
                        startColorComponents[3])
                .tex(0.0f, 0.0f)
                .endVertex();

        buffer.pos(x, y + (float) ((int) height), 0.0f)
                .color(i1[0], i1[1], i1[2],
                        i1[3])
                .tex(0.0f, 1.0f)
                .endVertex();

        buffer.pos(x + (float) ((int) width), y + (float) ((int) height), 0.0f)
                .color(startColorComponents[0], startColorComponents[1], startColorComponents[2],
                        startColorComponents[3])
                .tex(1.0f, 1.0f)
                .endVertex();

        buffer.pos(x + (float) ((int) width), y, 0.0f)
                .color(i1[0], i1[1], i1[2],
                        i1[3])
                .tex(1.0f, 0.0f)
                .endVertex();

        tessellator.draw();
        GlStateManager.enableAlphaTest();
        GL11.glShadeModel(7424);
        GlStateManager.bindTexture(0);
        GlStateManager.disableBlend();
    }

    public static void drawShadow(float x, float y, float width, float height, int radius, int color) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.01f);
        GlStateManager.disableAlphaTest();

        x -= radius;
        y -= radius;
        width = width + radius * 2;
        height = height + radius * 2;
        x -= 0.25f;
        y += 0.25f;

        int identifier = Objects.hash(width, height, radius);
        int textureId;

        if (shadowCache.containsKey(identifier)) {
            textureId = shadowCache.get(identifier);
            GlStateManager.bindTexture(textureId);
        } else {
            if (width <= 0) {
                width = 1;
            }

            if (height <= 0) {
                height = 1;
            }

            BufferedImage originalImage = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB_PRE);
            Graphics2D graphics = originalImage.createGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(radius, radius, (int) (width - radius * 2), (int) (height - radius * 2));
            graphics.dispose();

            GaussianFilter filter = new GaussianFilter(radius);
            BufferedImage blurredImage = filter.filter(originalImage, null);
            DynamicTexture texture = new DynamicTexture(TextureUtils.toNativeImage(blurredImage));
            texture.setBlurMipmap(true, true);
            try {
                textureId = texture.getGlTextureId();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            shadowCache.put(identifier, textureId);
        }

        float[] startColorComponents = ColorUtils.rgba(color);

        buffer.begin(GL11.GL_QUADS, POSITION_COLOR_TEX);
        buffer.pos(x, y, 0.0f)
                .color(startColorComponents[0], startColorComponents[1], startColorComponents[2],
                        startColorComponents[3])
                .tex(0.0f, 0.0f)
                .endVertex();

        buffer.pos(x, y + (float) ((int) height), 0.0f)
                .color(startColorComponents[0], startColorComponents[1], startColorComponents[2],
                        startColorComponents[3])
                .tex(0.0f, 1.0f)
                .endVertex();

        buffer.pos(x + (float) ((int) width), y + (float) ((int) height), 0.0f)
                .color(startColorComponents[0], startColorComponents[1], startColorComponents[2],
                        startColorComponents[3])
                .tex(1.0f, 1.0f)
                .endVertex();

        buffer.pos(x + (float) ((int) width), y, 0.0f)
                .color(startColorComponents[0], startColorComponents[1], startColorComponents[2],
                        startColorComponents[3])
                .tex(1.0f, 0.0f)
                .endVertex();

        tessellator.draw();
        GlStateManager.enableAlphaTest();
        GlStateManager.bindTexture(0);
        GlStateManager.disableBlend();
    }
    public static void drawGif(ResourceLocation resourceLocation, float x, float y, float width, float height, int frame) throws IOException {
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Загрузите изображение GIF в BufferedImage
        ImageIcon gif = new ImageIcon(mc.getResourceManager().getResource(resourceLocation).getInputStream().toString());
        BufferedImage img = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = img.getGraphics();

        // Рисуем текущий кадр GIF
        g.drawImage(gif.getImage(), 0, 0, (int) width, (int) height, null);
        g.dispose();

        // Привязываем текстуру и рисуем
        mc.getTextureManager().bindTexture(resourceLocation);
        quads(x, y, width, height, 7, 0xFFFFFFFF); // используем белый цвет для кадра

        RenderSystem.popMatrix();
    }

    public static void drawImage(ResourceLocation resourceLocation, float x, float y, float width, float height,
                                 int color) {
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);
        mc.getTextureManager().bindTexture(resourceLocation);
        quads(x, y, width, height, 7, color);
        RenderSystem.shadeModel(7424);
        RenderSystem.color4f(1, 1, 1, 1);
        RenderSystem.popMatrix();
    }

    public static void drawImage(ResourceLocation resourceLocation, float x, float y, float width, float height,
                                 Vector4i color) {
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);
        mc.getTextureManager().bindTexture(resourceLocation);
        buffer.begin(7, POSITION_TEX_COLOR);
        {
            buffer.pos(x, y, 0).tex(0, 0).color(color.x).endVertex();
            buffer.pos(x, y + height, 0).tex(0, 1).color(color.y).endVertex();
            buffer.pos(x + width, y + height, 0).tex(1, 1).color(color.z).endVertex();
            buffer.pos(x + width, y, 0).tex(1, 0).color(color.w).endVertex();
        }
        tessellator.draw();
        RenderSystem.shadeModel(7424);
        RenderSystem.color4f(1, 1, 1, 1);
        RenderSystem.popMatrix();

    }

    public static void drawRectWBuilding(
            double left,
            double top,
            double right,
            double bottom,
            int color) {
        right += left;
        bottom += top;

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        bufferbuilder.pos(left, bottom, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(right, bottom, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(right, top, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(left, top, 0.0F).color(f, f1, f2, f3).endVertex();
    }

    public static void drawRectBuilding(
            double left,
            double top,
            double right,
            double bottom,
            int color) {
        if (left < right) {
            double i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            double j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        bufferbuilder.pos(left, bottom, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(right, bottom, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(right, top, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(left, top, 0.0F).color(f, f1, f2, f3).endVertex();
    }

    public static void drawMCVerticalBuilding(double x,
                                              double y,
                                              double width,
                                              double height,
                                              int start,
                                              int end) {

        float f = (float) (start >> 24 & 255) / 255.0F;
        float f1 = (float) (start >> 16 & 255) / 255.0F;
        float f2 = (float) (start >> 8 & 255) / 255.0F;
        float f3 = (float) (start & 255) / 255.0F;
        float f4 = (float) (end >> 24 & 255) / 255.0F;
        float f5 = (float) (end >> 16 & 255) / 255.0F;
        float f6 = (float) (end >> 8 & 255) / 255.0F;
        float f7 = (float) (end & 255) / 255.0F;


        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        bufferbuilder.pos(x, height, 0f).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos(width, height, 0f).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos(width, y, 0f).color(f5, f6, f7, f4).endVertex();
        bufferbuilder.pos(x, y, 0f).color(f5, f6, f7, f4).endVertex();
    }

    public static void drawMCHorizontalBuilding(double x,
                                                double y,
                                                double width,
                                                double height,
                                                int start,
                                                int end) {


        float f = (float) (start >> 24 & 255) / 255.0F;
        float f1 = (float) (start >> 16 & 255) / 255.0F;
        float f2 = (float) (start >> 8 & 255) / 255.0F;
        float f3 = (float) (start & 255) / 255.0F;
        float f4 = (float) (end >> 24 & 255) / 255.0F;
        float f5 = (float) (end >> 16 & 255) / 255.0F;
        float f6 = (float) (end >> 8 & 255) / 255.0F;
        float f7 = (float) (end & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.pos(x, height, 0f).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos(width, height, 0f).color(f5, f6, f7, f4).endVertex();
        bufferbuilder.pos(width, y, 0f).color(f5, f6, f7, f4).endVertex();
        bufferbuilder.pos(x, y, 0f).color(f1, f2, f3, f).endVertex();
    }

    public static void drawRect(
            double left,
            double top,
            double right,
            double bottom,
            int color) {
        if (left < right) {
            double i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            double j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(left, bottom, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(right, bottom, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(right, top, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(left, top, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.finishDrawing();
        WorldVertexBufferUploader.draw(bufferbuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawRectW(
            double x,
            double y,
            double w,
            double h,
            int color) {

        w = x + w;
        h = y + h;

        if (x < w) {
            double i = x;
            x = w;
            w = i;
        }

        if (y < h) {
            double j = y;
            y = h;
            h = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(x, h, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(w, h, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(w, y, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(x, y, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.finishDrawing();
        WorldVertexBufferUploader.draw(bufferbuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawRectHorizontalW(
            double x,
            double y,
            double w,
            double h,
            int color,
            int color1) {

        w = x + w;
        h = y + h;

        if (x < w) {
            double i = x;
            x = w;
            w = i;
        }

        if (y < h) {
            double j = y;
            y = h;
            h = j;
        }

        float[] colorOne = ColorUtils.rgba(color);
        float[] colorTwo = ColorUtils.rgba(color1);
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.shadeModel(7425);
        RenderSystem.defaultBlendFunc();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(x, h, 0.0F).color(colorTwo[0], colorTwo[1], colorTwo[2], colorTwo[3]).endVertex();
        bufferbuilder.pos(w, h, 0.0F).color(colorTwo[0], colorTwo[1], colorTwo[2], colorTwo[3]).endVertex();
        bufferbuilder.pos(w, y, 0.0F).color(colorOne[0], colorOne[1], colorOne[2], colorOne[3]).endVertex();
        bufferbuilder.pos(x, y, 0.0F).color(colorOne[0], colorOne[1], colorOne[2], colorOne[3]).endVertex();
        bufferbuilder.finishDrawing();
        WorldVertexBufferUploader.draw(bufferbuilder);
        RenderSystem.shadeModel(7424);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawRectVerticalW(
            double x,
            double y,
            double w,
            double h,
            int color,
            int color1) {

        w = x + w;
        h = y + h;

        if (x < w) {
            double i = x;
            x = w;
            w = i;
        }

        if (y < h) {
            double j = y;
            y = h;
            h = j;
        }

        float[] colorOne = ColorUtils.rgba(color);
        float[] colorTwo = ColorUtils.rgba(color1);
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.shadeModel(7425);
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(x, h, 0.0F).color(colorOne[0], colorOne[1], colorOne[2], colorOne[3]).endVertex();
        bufferbuilder.pos(w, h, 0.0F).color(colorTwo[0], colorTwo[1], colorTwo[2], colorTwo[3]).endVertex();
        bufferbuilder.pos(w, y, 0.0F).color(colorTwo[0], colorTwo[1], colorTwo[2], colorTwo[3]).endVertex();
        bufferbuilder.pos(x, y, 0.0F).color(colorOne[0], colorOne[1], colorOne[2], colorOne[3]).endVertex();
        bufferbuilder.finishDrawing();
        WorldVertexBufferUploader.draw(bufferbuilder);
        RenderSystem.enableTexture();
        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();
    }

    public static void drawRoundedRect(float x,
                                       float y,
                                       float width,
                                       float height,
                                       Vector4f vector4f,
                                       int color) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();

        ShaderUtil.rounded.attach();

        ShaderUtil.rounded.setUniform("size", width * 2, height * 2);
        ShaderUtil.rounded.setUniform("round", vector4f.x * 2, vector4f.y * 2, vector4f.z * 2, vector4f.w * 2);

        ShaderUtil.rounded.setUniform("smoothness", 0.f, 1.5f);
        ShaderUtil.rounded.setUniform("color1",
                ColorUtils.rgba(color));
        ShaderUtil.rounded.setUniform("color2",
                ColorUtils.rgba(color));
        ShaderUtil.rounded.setUniform("color3",
                ColorUtils.rgba(color));
        ShaderUtil.rounded.setUniform("color4",
                ColorUtils.rgba(color));
        drawQuads(x, y, width, height, 7);

        ShaderUtil.rounded.detach();
        GlStateManager.disableBlend();

        GlStateManager.popMatrix();
    }
    public static void drawRoundedRect(float x,
                                       float y,
                                       float width,
                                       float height,
                                       Vector4f vector4f,
                                       Vector4i color) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        ShaderUtil.rounded.attach();

        ShaderUtil.rounded.setUniform("size", width * 2, height * 2);
        ShaderUtil.rounded.setUniform("round", vector4f.x * 2, vector4f.y * 2, vector4f.z * 2, vector4f.w * 2);

        ShaderUtil.rounded.setUniform("smoothness", 0.f, 1.5f);
        ShaderUtil.rounded.setUniform("color1",
                ColorUtils.rgba(color.x()));
        ShaderUtil.rounded.setUniform("color2",
                ColorUtils.rgba(color.y()));
        ShaderUtil.rounded.setUniform("color3",
                ColorUtils.rgba(color.z()));
        ShaderUtil.rounded.setUniform("color4",
                ColorUtils.rgba(color.w()));
        drawQuads(x, y, width, height, 7);

        ShaderUtil.rounded.detach();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }


    public static void drawRoundedRect(float x,
                                       float y,
                                       float width,
                                       float height,
                                       float outline,
                                       int color1,
                                       Vector4f vector4f,
                                       Vector4i color) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        ShaderUtil.roundedout.attach();

        ShaderUtil.roundedout.setUniform("size", width * 2, height * 2);
        ShaderUtil.roundedout.setUniform("round", vector4f.x * 2, vector4f.y * 2, vector4f.z * 2, vector4f.w * 2);

        ShaderUtil.roundedout.setUniform("smoothness", 0.f, 1.5f);
        ShaderUtil.roundedout.setUniform("outlineColor",
                ColorUtils.rgba(color.x()));
        ShaderUtil.roundedout.setUniform("outlineColor1",
                ColorUtils.rgba(color.y()));
        ShaderUtil.roundedout.setUniform("outlineColor2",
                ColorUtils.rgba(color.z()));
        ShaderUtil.roundedout.setUniform("outlineColor3",
                ColorUtils.rgba(color.w()));
        ShaderUtil.roundedout.setUniform("color", ColorUtils.rgba(color1));
        ShaderUtil.roundedout.setUniform("outline",
                outline);
        drawQuads(x, y, width, height, 7);

        ShaderUtil.rounded.detach();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private static Framebuffer whiteCache = new Framebuffer(1, 1, false, true);
    private static Framebuffer contrastCache = new Framebuffer(1, 1, false, true);

    public static void drawContrast(float state) {
        state = MathHelper.clamp(state, 0, 1);
        GlStateManager.enableBlend();
        GlStateManager.color4f(1, 1, 1, 1);
        GlStateManager.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        contrastCache = ShaderUtil.createFrameBuffer(contrastCache);

        contrastCache.framebufferClear(false);
        contrastCache.bindFramebuffer(true);

        // prepare image
        ShaderUtil.contrast.attach();
        ShaderUtil.contrast.setUniform("texture", 0);
        ShaderUtil.contrast.setUniformf("contrast", state);
        GlStateManager.bindTexture(mc.getFramebuffer().framebufferTexture);

        ShaderUtil.drawQuads();
        contrastCache.unbindFramebuffer();
        ShaderUtil.contrast.detach();
        mc.getFramebuffer().bindFramebuffer(true);

        // draw image
        ShaderUtil.contrast.attach();
        ShaderUtil.contrast.setUniform("texture", 0);
        ShaderUtil.contrast.setUniformf("contrast", state);
        GlStateManager.bindTexture(contrastCache.framebufferTexture);
        ShaderUtil.drawQuads();
        ShaderUtil.contrast.detach();

        GlStateManager.color4f(1, 1, 1, 1);
        GlStateManager.bindTexture(0);
    }

    public static void drawWhite(float state) {
        state = MathHelper.clamp(state, 0, 1);
        GlStateManager.enableBlend();
        GlStateManager.color4f(1, 1, 1, 1);
        GlStateManager.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        whiteCache = ShaderUtil.createFrameBuffer(whiteCache);

        whiteCache.framebufferClear(false);
        whiteCache.bindFramebuffer(true);

        // prepare image
        ShaderUtil.white.attach();
        ShaderUtil.white.setUniform("texture", 0);
        ShaderUtil.white.setUniformf("state", state);
        GlStateManager.bindTexture(mc.getFramebuffer().framebufferTexture);

        ShaderUtil.drawQuads();
        whiteCache.unbindFramebuffer();
        ShaderUtil.white.detach();
        mc.getFramebuffer().bindFramebuffer(true);

        // draw image
        ShaderUtil.white.attach();
        ShaderUtil.white.setUniform("texture", 0);
        ShaderUtil.white.setUniformf("state", state);
        GlStateManager.bindTexture(whiteCache.framebufferTexture);
        ShaderUtil.drawQuads();
        ShaderUtil.white.detach();

        GlStateManager.color4f(1, 1, 1, 1);
        GlStateManager.bindTexture(0);
    }


    public static void drawRoundedRect(float x,
                                       float y,
                                       float width,
                                       float height,
                                       float radius,
                                       int color) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        ShaderUtil.smooth.attach();

        ShaderUtil.smooth.setUniformf("location", (float) (x * mc.getMainWindow().getGuiScaleFactor()),
                (float) ((mc.getMainWindow().getHeight() - (height * mc.getMainWindow().getGuiScaleFactor()))
                        - (y * mc.getMainWindow().getGuiScaleFactor())));
        ShaderUtil.smooth.setUniformf("rectSize", width * mc.getMainWindow().getGuiScaleFactor(),
                height * mc.getMainWindow().getGuiScaleFactor());
        ShaderUtil.smooth.setUniformf("radius", radius * mc.getMainWindow().getGuiScaleFactor());
        ShaderUtil.smooth.setUniform("blur", 0);
        ShaderUtil.smooth.setUniform("color",
                ColorUtils.rgba(color));
        drawQuads(x, y, width, height, 7);

        ShaderUtil.smooth.detach();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawCircle(float x, float y, float radius, int color) {
        drawRoundedRect(x - radius / 2f, y - radius / 2f, radius, radius,
                radius / 2f, color);
    }

    public static void drawShadowCircle(float x, float y, float radius, int color) {
        drawShadow(x - radius / 2f, y - radius / 2f, radius, radius,
                (int) radius, color);
    }

    public static void drawQuads(float x, float y, float width, float height, int glQuads) {
        buffer.begin(glQuads, POSITION_TEX);
        {
            buffer.pos(x, y, 0).tex(0, 0).endVertex();
            buffer.pos(x, y + height, 0).tex(0, 1).endVertex();
            buffer.pos(x + width, y + height, 0).tex(1, 1).endVertex();
            buffer.pos(x + width, y, 0).tex(1, 0).endVertex();
        }
        Tessellator.getInstance().draw();
    }

    public static void drawBox(double x, double y, double width, double height, double size, int color) {
        drawRectBuilding(x + size, y, width - size, y + size, color);
        drawRectBuilding(x, y, x + size, height, color);

        drawRectBuilding(width - size, y, width, height, color);
        drawRectBuilding(x + size, height - size, width - size, height, color);
    }

    public static void drawBoxTest(double x, double y, double width, double height, double size, Vector4i colors) {
        drawMCHorizontalBuilding(x + size, y, width - size, y + size, colors.x, colors.z);
        drawMCVerticalBuilding(x, y, x + size, height, colors.z, colors.x);

        drawMCVerticalBuilding(width - size, y, width, height, colors.x, colors.z);
        drawMCHorizontalBuilding(x + size, height - size, width - size, height, colors.z, colors.x);
    }


//    public void renderClientLines(MatrixStack matrix, float x, float y, float width, float height, float round, float liney) {
//        int alphaPC = 255;
//        int color1 = HUD.getColor(0, alphaPC / 255f);
//        int color2 = HUD.getColor(90, alphaPC / 255f);
//        int color3 = HUD.getColor(180, alphaPC / 255f);
//        int color4 = HUD.getColor(270, alphaPC / 255f);
//        boolean bloom = true;
//        int zeroC1 = ColorUtils.multAlpha(color1, 0.F), zeroC2 = ColorUtils.multAlpha(color2, 0.F), zeroC3 = ColorUtils.multAlpha(color3, 0.F), zeroC4 = ColorUtils.multAlpha(color4, 0.F);
//
//        List<RectUtil.Vec2fColored> list = new ArrayList<>();
//
//        //lines
//        float lineOff = 0;
//        GL12.glLineWidth(2);
//        //up
//        list.clear();
//        list.add(new RectUtil.Vec2fColored(x + round, y + lineOff + liney, zeroC1));
//        list.add(new RectUtil.Vec2fColored(x + width / 2F, y + lineOff + liney, ColorUtils.rgba(220, 220, 220, 220)));
//        list.add(new RectUtil.Vec2fColored(x + width - round, y + lineOff + liney, zeroC2));
//        RectUtil.drawVertexesList(matrix, list, GL_LINE_STRIP, false, bloom);
//    }

}