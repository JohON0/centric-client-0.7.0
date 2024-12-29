package centric.pl.ui.guiv2.component.impl;

import centric.pl.functions.impl.render.HUD;
import centric.pl.ui.guiv2.component.ThemeChanger;
import com.mojang.blaze3d.matrix.MatrixStack;
import centric.pl.functions.settings.impl.BooleanSetting;
import centric.pl.johon0.utils.font.Fonts;
import centric.pl.johon0.utils.math.MathUtil;
import centric.pl.johon0.utils.render.ColorUtils;
import centric.pl.johon0.utils.render.DisplayUtils;
import centric.pl.johon0.utils.render.Scissor;

import java.awt.*;

import static centric.pl.johon0.utils.render.DisplayUtils.reAlphaInt;


public class BooleanComponent extends Component {

    public BooleanSetting option;

    public BooleanComponent(BooleanSetting option) {
        this.option = option;
        this.setting = option;
    }

    public float animationToggle;

    @Override
    public void drawComponent(MatrixStack matrixStack, int mouseX, int mouseY) {
        height = 15;
        float off = 0.5f;
        animationToggle = MathUtil.lerp(animationToggle, option.get() ? 1 : 0, 10);

        int color = ColorUtils.interpolateColor(ThemeChanger.theme == 0 ? ColorUtils.IntColor.rgba(26, 29, 33, 255): new Color(223, 229, 243).getRGB(), HUD.getColor(100), animationToggle);

        DisplayUtils.drawShadow(x + 5 + width-20, y + 1 + off, 10, 10, 8, reAlphaInt(color, 50));
        DisplayUtils.drawRoundedRect(x + 5 + width-20, y + 1 + off, 10, 10, 1f, color);
        Scissor.push();

        Scissor.setFromComponentCoordinates(x + 7 + width-20, y + 1 + off, 10 * animationToggle, 10);
        Fonts.iconsall[13].drawString(matrixStack, "Q", x + 7 + width-20, y + 5 + off, ThemeChanger.theme == 0 ? new Color(0,0,0).getRGB() : -1);
        Scissor.unset();
        Scissor.pop();

        Fonts.roboto[13].drawString(matrixStack, option.getName(), x + 5, y + 4.5f + off, new Color(69,71,84).getRGB());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (MathUtil.isInRegion(mouseX, mouseY, x + 5 + width-20, y, 10, 15)) {

            option.set(!option.get());
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {

    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {

    }

    @Override
    public void charTyped(char codePoint, int modifiers) {

    }
}
