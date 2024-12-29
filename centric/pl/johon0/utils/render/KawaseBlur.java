package centric.pl.johon0.utils.render;

import centric.pl.johon0.utils.CustomFramebuffer;
import centric.pl.johon0.utils.shader.ShaderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;

public class KawaseBlur {

    public static KawaseBlur blur = new KawaseBlur();

    public final CustomFramebuffer BLURRED;
    public final CustomFramebuffer ADDITIONAL;
    private final CustomFramebuffer[] buffers;

    public KawaseBlur() {
        BLURRED = new CustomFramebuffer(false).setLinear();
        ADDITIONAL = new CustomFramebuffer(false).setLinear();
        buffers = new CustomFramebuffer[]{ADDITIONAL, BLURRED};
    }

    public void render(Runnable run) {
        Stencil.initStencilToWrite();
        run.run();
        Stencil.readStencilBuffer(1);
        BLURRED.draw();
        Stencil.uninitStencilBuffer();
    }

    public void updateBlur(float offset, int steps) {
        Minecraft mc = Minecraft.getInstance();
        Framebuffer mcFramebuffer = mc.getFramebuffer();

        if (mcFramebuffer == null) return; // Проверка на существование фреймбуфера

        mcFramebuffer.bindFramebufferTexture();

        // Шаг вниз
        ShaderUtil.kawaseDown.attach();
        setShaderUniforms(offset, mc);
        performBlurPass(steps, true); // Downsampling
        ShaderUtil.kawaseDown.detach();

        // Шаг вверх
        ShaderUtil.kawaseUp.attach();
        setShaderUniforms(offset, mc);
        performBlurPass(steps, false); // Upsampling
        ShaderUtil.kawaseUp.detach();

        mcFramebuffer.bindFramebuffer(false);
    }

    private void setShaderUniforms(float offset, Minecraft mc) {
        ShaderUtil.kawaseDown.setUniform("offset", offset);
        ShaderUtil.kawaseDown.setUniformf("resolution",
                1f / mc.getMainWindow().getWidth(),
                1f / mc.getMainWindow().getHeight());
    }

    private void performBlurPass(int steps, boolean down) {
        CustomFramebuffer sourceBuffer = down ? ADDITIONAL : BLURRED;
        CustomFramebuffer targetBuffer = down ? BLURRED : ADDITIONAL;

        sourceBuffer.setup();
        CustomFramebuffer.drawTexture();

        for (int i = 0; i < steps; ++i) {
            targetBuffer.setup();
            sourceBuffer.draw();
            CustomFramebuffer temp = sourceBuffer; // swap buffers
            sourceBuffer = targetBuffer;
            targetBuffer = temp;
        }
    }
}
