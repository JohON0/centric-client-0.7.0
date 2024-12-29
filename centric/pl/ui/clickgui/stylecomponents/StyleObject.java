/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package centric.pl.ui.clickgui.stylecomponents;

import centric.pl.managers.styleManager.ThemeSwitcher;
import centric.pl.managers.styleManager.Style;
import centric.pl.johon0.utils.client.ClientUtil;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.client.Vec2i;
import centric.pl.johon0.utils.math.MathUtil;
import centric.pl.johon0.utils.render.ColorUtils;
import centric.pl.johon0.utils.render.DisplayUtils;
import centric.pl.johon0.utils.render.Scissor;
import com.mojang.blaze3d.matrix.MatrixStack;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import centric.pl.Main;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector2f;
public class StyleObject {
    private final Vector2f position;
    private List<StyleRender> objects = new ArrayList<StyleRender>();
    private float scroll2;
    private float animatedScrool2;
    public Style style;
    float animation;
    public int edit;
    float x;
    float y;
    float width;
    float height;
    float hsb;
    float satur;
    float brithe;
    float max = 0.0f;


    public StyleObject() {
        this.position = new Vector2f();
        Style custom = Main.getInstance().getStyleManager().getStyleList().get(Main.getInstance().getStyleManager().getStyleList().size() - 1);
        for (Style style : Main.getInstance().getStyleManager().getStyleList()) {
            if (style.getStyleName().equalsIgnoreCase("���� ����")) continue;
            this.objects.add(new StyleRender(style));
        }
        float[] rgb2 = ColorUtils.IntColor.rgb(custom.getColor(this.edit));
        float[] hsb = Color.RGBtoHSB((int)(rgb2[0] * 255.0f), (int)(rgb2[1] * 255.0f), (int)(rgb2[2] * 255.0f), null);
        this.hsb = hsb[0];
        this.satur = hsb[1];
        this.brithe = hsb[2];
    }

    public void draw(MatrixStack stack, int mouseX, int mouseY, float x, float y, float width2, float height2) {
        IMinecraft.mc.gameRenderer.setupOverlayRendering(2);
        this.animatedScrool2 = MathUtil.fast(this.animatedScrool2, this.scroll2, 15.0f);
        this.x = x;
        this.y = y;
        this.width = width2;
        this.height = height2;
        float offsetY = 4.0f;
        float headerTest = 25.0f;
        Scissor.push();
        Scissor.setFromComponentCoordinates(x, y + 3.0f, width2, height2 - 8.0f);
        float yPos = y;
        for (StyleRender object : this.objects) {
            object.x = x + 3.0f;
            object.y = yPos + 4.0f + this.animatedScrool2;
            object.width = 120.0f;
            object.height = 17.0f;
            DisplayUtils.drawRoundedRect(object.x, object.y, 120.0f, object.height, 5.0f, ThemeSwitcher.buttoncolor);
            yPos += offsetY + 16.0f;
        }
        for (StyleRender object : this.objects) {
            object.draw(stack, mouseX, mouseY);
        }


        Scissor.unset();
        Scissor.pop();
        this.max = yPos;
    }

    public void scroll(double mouseX, double mouseY, double delta) {
        Vec2i fixed = ClientUtil.getMouse((int)mouseX, (int)mouseY);
        mouseX = fixed.getX();
        mouseY = fixed.getY();
        float header = -37.0f;
        if (this.max > this.height - 100.0f) {
            this.setScroll2(MathHelper.clamp(this.getScroll2(), -this.max + this.height - header - 10.0f, 0.0f));
            this.setAnimatedScrool2(MathHelper.clamp(this.getAnimatedScrool2(), -this.max + this.height - header - 10.0f, 0.0f));
            if (MathUtil.isInRegion((float)mouseX, (float)mouseY, (float)IMinecraft.mc.getMainWindow().getScaledWidth() - 132.5f, 8.0f, 127.0f, 150.0f)) {
                this.scroll2 += (float)(delta * 16.0);
            }
        } else {
            this.setScroll2(0.0f);
            this.setAnimatedScrool2(0.0f);
        }
    }

    public void click(int mouseX, int mouseY, int button) {
        float[] hsb;
        float[] rgb2;
        Style custom = Main.getInstance().getStyleManager().getStyleList().get(Main.getInstance().getStyleManager().getStyleList().size() - 1);
        float colorX = this.x + 3.0f;
        float colorY = this.y + this.height - 157.5f;
        if (MathUtil.isInRegion(mouseX, mouseY, colorX + 8.0f, colorY + 6.0f, 34.0f, 8.0f)) {
            this.edit = 0;
            rgb2 = ColorUtils.IntColor.rgb(custom.getColor(this.edit));
            hsb = Color.RGBtoHSB((int)(rgb2[0] * 255.0f), (int)(rgb2[1] * 255.0f), (int)(rgb2[2] * 255.0f), null);
            this.hsb = hsb[0];
            this.satur = hsb[1];
            this.brithe = hsb[2];
        }
        if (MathUtil.isInRegion(mouseX, mouseY, colorX + 8.0f + 25.0f, colorY + 6.0f, 34.0f, 8.0f)) {
            this.edit = 1;
            rgb2 = ColorUtils.IntColor.rgb(custom.getColor(this.edit));
            hsb = Color.RGBtoHSB((int)(rgb2[0] * 255.0f), (int)(rgb2[1] * 255.0f), (int)(rgb2[2] * 255.0f), null);
            this.hsb = hsb[0];
            this.satur = hsb[1];
            this.brithe = hsb[2];
        }
        if (MathUtil.isInRegion(mouseX, mouseY, this.x + 10.0f, this.y + this.height - 65.0f, this.width - 20.0f, 50.0f) && button == 0 && Main.getInstance().getFunctionRegistry().getHud().themeopen.get()) {
            Style c = Main.getInstance().getStyleManager().getStyleList().get(Main.getInstance().getStyleManager().getStyleList().size() - 1);
            Main.getInstance().getStyleManager().setCurrentStyle(c);
        }
        for (StyleRender object : this.objects) {
            if (!MathUtil.isInRegion(mouseX, mouseY, IMinecraft.mc.getMainWindow().getScaledWidth() - 132, 22.0f, 126.0f, 136.0f) || !Main.getInstance().getFunctionRegistry().getHud().themeopen.get() || !MathUtil.isInRegion(mouseX, mouseY, object.x, object.y, 120.0f, 140.0f)) continue;
            Main.getInstance().getStyleManager().setCurrentStyle(object.style);
        }
    }

    public Vector2f getPosition() {
        return this.position;
    }

    public List<StyleRender> getObjects() {
        return this.objects;
    }

    public float getScroll2() {
        return this.scroll2;
    }

    public float getAnimatedScrool2() {
        return this.animatedScrool2;
    }

    public float getAnimation() {
        return this.animation;
    }

    public int getEdit() {
        return this.edit;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getWidth() {
        return this.width;
    }

    public float getHeight() {
        return this.height;
    }

    public float getHsb() {
        return this.hsb;
    }

    public float getSatur() {
        return this.satur;
    }

    public float getBrithe() {
        return this.brithe;
    }

    public float getMax() {
        return this.max;
    }

    public void setObjects(List<StyleRender> objects) {
        this.objects = objects;
    }

    public void setScroll2(float scroll2) {
        this.scroll2 = scroll2;
    }

    public void setAnimatedScrool2(float animatedScrool2) {
        this.animatedScrool2 = animatedScrool2;
    }

    public void setAnimation(float animation) {
        this.animation = animation;
    }

    public void setEdit(int edit) {
        this.edit = edit;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setWidth(float width2) {
        this.width = width2;
    }

    public void setHeight(float height2) {
        this.height = height2;
    }

    public void setHsb(float hsb) {
        this.hsb = hsb;
    }

    public void setSatur(float satur) {
        this.satur = satur;
    }

    public void setBrithe(float brithe) {
        this.brithe = brithe;
    }

    public void setMax(float max2) {
        this.max = max2;
    }
}

