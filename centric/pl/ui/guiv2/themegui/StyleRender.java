/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package centric.pl.ui.guiv2.themegui;

import centric.pl.Main;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.font.Fonts;
import centric.pl.johon0.utils.math.MathUtil;
import centric.pl.johon0.utils.render.ColorUtils;
import centric.pl.johon0.utils.render.DisplayUtils;
import centric.pl.managers.styleManager.Style;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import org.joml.Vector4f;
import org.joml.Vector4i;

public class StyleRender {
    public float x;
    public float y;
    public float width;
    public float height;
    public Style style;
    private float animation;

    public StyleRender(Style style) {
        this.style = style;
    }
    private double scale = 0.0D;
    public void draw(MatrixStack stack, int mouseX, int mouseY) {
        animation = (float) MathUtil.lerp(animation, Main.getInstance().getStyleManager().getCurrentStyle() == style ? 1 : MathUtil.isInRegion(mouseX,mouseY,this.x + this.width - 30.0f, this.y + 3.5f, 25.0f, 10.0f) ? 0.5f : 0, 5);
        GlStateManager.pushMatrix();
        IMinecraft.mc.gameRenderer.setupOverlayRendering(2);
        Vector4i colors = new Vector4i();
        if (this.style.getStyleName().equalsIgnoreCase("\u0410\u0441\u0442\u043e\u043b\u044c\u0444\u043e")) {
            colors = new Vector4i(this.style.getColor(0), this.style.getColor(0), this.style.getColor(90), this.style.getColor(90));
        }
        if (this.style.getStyleName().equalsIgnoreCase("\u0420\u0430\u0434\u0443\u0436\u043d\u044b\u0439")) {
            colors = new Vector4i(this.style.getColor(0), this.style.getColor(0), this.style.getColor(90), this.style.getColor(90));
        }
        float off = 0.0f;
        for (String ss : this.style.getStyleName().split("  ")) {
            DisplayUtils.drawShadow((this.x + 5), (this.y + 4f + off), Fonts.notoitalic[16].getWidth(ss) + 2, Fonts.notoitalic[16].getFontHeight(),6, ColorUtils.setAlpha(style.getColor(0), (int) (100*animation)));

            Fonts.notoitalic[16].drawString(stack, ss, (double)(this.x + 5.0f), (double)(this.y + 6.0f + off), ColorUtils.gradient(this.style.getColor(0), this.style.getColor(2),100,80));
            off += 10.0f;
        }

        if (style.getStyleName().equalsIgnoreCase("Свой Цвет")) {
            colors = new Vector4i(
                    style.getColor(0),
                    style.getColor(0),
                    style.getColor(90),
                    style.getColor(90)
            );
        }
        DisplayUtils.drawRoundedRect(this.x + this.width - 30.0f, this.y + 3.5f, 25.0f, 10.0f, new Vector4f(2.0f, 2.0f, 2.0f, 2.0f), colors);
        GlStateManager.popMatrix();
    }
}

