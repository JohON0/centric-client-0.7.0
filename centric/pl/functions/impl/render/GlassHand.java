package centric.pl.functions.impl.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.platform.GlStateManager;
import centric.pl.events.impl.EventDisplay;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.johon0.utils.CustomFramebuffer;
import centric.pl.johon0.utils.render.ColorUtils;
import centric.pl.johon0.utils.render.KawaseBlur;
import centric.pl.johon0.utils.shader.impl.Outline;
import net.minecraft.client.settings.PointOfView;
import org.lwjgl.opengl.GL11;

@FunctionRegister(name = "Glass Hand", type = Category.Render, beta = true)
public class GlassHand extends Function {
    public CustomFramebuffer hands = new CustomFramebuffer(false).setLinear();
    public CustomFramebuffer mask = new CustomFramebuffer(false).setLinear();

    @Subscribe
    public void onRender(EventDisplay eventDisplay) {
        if (eventDisplay.getType() != EventDisplay.Type.HIGH) {
            return;
        }
        if (GlassHand.mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON) {
            KawaseBlur.blur.updateBlur(3.0f, 4);
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.enableAlphaTest();
            ColorUtils.setColor(ColorUtils.getColor(0));
            KawaseBlur.blur.render(this::drawhand);
            Outline.registerRenderCall(this::drawhand);
            GlStateManager.disableAlphaTest();
            GlStateManager.popMatrix();
        }
    }

    public static void setSaturation(float saturation) {
        float[] matrix = new float[]{
                0.3086f * (1.0f - saturation) + saturation, 0.6094f * (1.0f - saturation), 0.082f * (1.0f - saturation), 0.0f, 0.0f,
                0.3086f * (1.0f - saturation), 0.6094f * (1.0f - saturation) + saturation, 0.082f * (1.0f - saturation), 0.0f, 0.0f,
                0.3086f * (1.0f - saturation), 0.6094f * (1.0f - saturation), 0.082f * (1.0f - saturation) + saturation, 0.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f, 0.0f
        };
        GL11.glLoadMatrixf(matrix);
    }
    private void drawhand() {
        this.hands.draw();
    }
}