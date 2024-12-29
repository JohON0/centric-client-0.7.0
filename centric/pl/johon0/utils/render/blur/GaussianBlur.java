package centric.pl.johon0.utils.render.blur;


import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.render.StencilUtil;
import centric.pl.johon0.utils.shader.ShaderUtil;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

public class GaussianBlur implements IMinecraft {

    private static final ShaderUtil gaussianBlur = new ShaderUtil("bloom");
    private static Framebuffer framebuffer = new Framebuffer(1, 1, false, false);

    private static void setupUniforms(float dir1, float dir2, float radius) {
        gaussianBlur.setUniform("textureIn", 0);
        gaussianBlur.setUniformf("texelSize", 1.0F / (float) mc.getMainWindow().getWidth(), 1.0F / (float) mc.getMainWindow().getHeight());
        gaussianBlur.setUniformf("direction", dir1, dir2);
        gaussianBlur.setUniformf("radius", radius);

        final FloatBuffer weightBuffer = BufferUtils.createFloatBuffer(256);
        for (int i = 0; i <= radius; i++) {
            weightBuffer.put(calculateGaussianValue(i, radius / 2));
        }

        weightBuffer.rewind();
        RenderSystem.glUniform1(gaussianBlur.getUniform("weights"), weightBuffer);
    }

    public static void startBlur() {
        StencilUtil.initStencilToWrite();
    }

    public static void endBlur(float radius, float compression) {
        applyBlur(radius, compression);

        StencilUtil.readStencilBuffer(1);
        framebuffer.framebufferClear(false);
        framebuffer.bindFramebuffer(false);
        gaussianBlur.attach();
        setupUniforms(compression, 0, radius);
        GlStateManager.bindTexture(mc.getFramebuffer().framebufferTexture);
        ShaderUtil.drawQuads();
        framebuffer.unbindFramebuffer();
        gaussianBlur.detach();

        mc.getFramebuffer().bindFramebuffer(false);
        gaussianBlur.attach();
        setupUniforms(0, compression, radius);
        GlStateManager.bindTexture(framebuffer.framebufferTexture);
        ShaderUtil.drawQuads();
        gaussianBlur.detach();

        StencilUtil.uninitStencilBuffer();
    }

    public static void blur(float radius, float compression, float r, float g, float b, float a) {
        applyBlur(radius, compression);
        applyColor(r, g, b, a);
    }

    private static void applyBlur(float radius, float compression) {
        framebuffer = ShaderUtil.createFrameBuffer(framebuffer);
        framebuffer.framebufferClear(false);
        framebuffer.bindFramebuffer(false);
        gaussianBlur.attach();
        setupUniforms(compression, 0, radius);
        GlStateManager.bindTexture(mc.getFramebuffer().framebufferTexture);
        ShaderUtil.drawQuads();
        framebuffer.unbindFramebuffer();
        gaussianBlur.detach();

        mc.getFramebuffer().bindFramebuffer(false);
        gaussianBlur.attach();
        setupUniforms(0, compression, radius);
        GlStateManager.bindTexture(framebuffer.framebufferTexture);
        ShaderUtil.drawQuads();
        gaussianBlur.detach();
    }

    private static void applyColor(float r, float g, float b, float a) {
        RenderSystem.color4f(r, g, b, a);
        GlStateManager.bindTexture(0);
    }

    public static float calculateGaussianValue(float x, float sigma) {
        double output = 1.0 / Math.sqrt(2.0 * Math.PI * (sigma * sigma));
        return (float) (output * Math.exp(-(x * x) / (2.0 * (sigma * sigma))));
    }
}

