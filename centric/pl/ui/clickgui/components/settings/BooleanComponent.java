/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package centric.pl.ui.clickgui.components.settings;

import centric.pl.johon0.utils.math.MathUtil;
import centric.pl.johon0.utils.math.StopWatch;
import centric.pl.johon0.utils.render.ColorUtils;
import centric.pl.johon0.utils.render.DisplayUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import centric.pl.functions.settings.impl.BooleanSetting;
import centric.pl.ui.clickgui.impl.Component;
import centric.pl.johon0.utils.font.Fonts;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import static centric.pl.managers.styleManager.ThemeSwitcher.*;


public class BooleanComponent
        extends Component {
    private BooleanSetting booleanOption;
    public float enabledAnimation;
    float animation2;
    float animation3;
    private boolean hovered = false;
    private boolean hoveredOnSquare = false;
    private float textOffset = 0.0F;
    StopWatch timeUtil = new StopWatch();
    public BooleanComponent(BooleanSetting booleanOption) {
        this.booleanOption = booleanOption;
        this.setWidth(40.0f);
        this.setHeight(12.0f);
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        if (this.hovered && !this.hoveredOnSquare) {
            this.textOffset += 0.6F;
            if (this.textOffset >= this.getWidth()) {
                this.textOffset = -Fonts.notoitalic[12].getWidth(this.booleanOption.getName());
            }
        } else if (!this.hovered) {
            this.textOffset = 0.0F;
        }
        float textX = this.getX() + 5.0F + textOffset;


        int y = (int) (getY() - 14);
        this.enabledAnimation = MathUtil.fast(this.enabledAnimation, this.booleanOption.get() ? 8.0f : -1.0f, 8.0f);
        this.animation2 = MathUtil.fast(this.animation2, this.booleanOption.get() ? 1.0f : 0.0f, 12.0f);
        this.animation3 = MathUtil.fast(this.animation3, !this.booleanOption.get() ? 1.0f : 0.0f, 10.0f);
        float off = 0.0f;
        for (String ss : this.booleanOption.getName().split("  ")) {
            Fonts.notoitalic[12].drawString(stack, ss, textX, (double) (y + this.getHeight() + 6.5f + off), textcolor);

            off += 6.0f;
        }
        DisplayUtils.drawRoundedRect(this.getX() + 92.0f, y + this.getHeight() + 3.0f, 10.0f, 10.0f, 2.0f, bgcolor);
        Fonts.iconsall[14].drawString(stack, "Q", (double) (this.getX() + 94f), (double) (y + this.getHeight() + 6.5f), ColorUtils.rgba(0, 255, 0, (int) (255.0f * this.animation2)));
        if (!this.booleanOption.get()) {
            Fonts.iconsall[14].drawString(stack, "R", (double) (this.getX() + 94.3f), (double) (y + this.getHeight() + 6.5f), ColorUtils.rgba(235, 0, 0, (int) (255.0f * this.animation3)));
        }
        this.hoveredOnSquare = MathUtil.isInRegion(mouseX, mouseY, this.getX() + this.getWidth(), this.getY() + this.getHeight() / 2.0F, getWidth() - 4.0F, getHeight() + 2.5F);
        if (this.isHovered(mouseX, mouseY)) {
            if (booleanOption.getName().length() > 22) {
                this.hovered = true;
            } else {

            }
        } else {
                this.hovered = false;
                this.hoveredOnSquare = false;
            }
        }

    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {
        // TODO Auto-generated method stub
        if (MathUtil.isInRegion(mouseX, mouseY, this.getX() + 92.0f, getY() + this.getHeight() - 8.0f, 10.0f, 10.0f)) {
            booleanOption.set(!booleanOption.get());
        }
        super.mouseClick(mouseX, mouseY, mouse);
    }

    @Override
    public boolean isVisible() {
        return booleanOption.visible.get();
    }


}

