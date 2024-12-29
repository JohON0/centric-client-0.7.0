package centric.pl.ui.clickgui.components.settings;

import centric.pl.managers.styleManager.ThemeSwitcher;
import centric.pl.johon0.utils.font.Fonts;
import centric.pl.johon0.utils.math.MathUtil;
import centric.pl.johon0.utils.render.ColorUtils;
import centric.pl.johon0.utils.render.DisplayUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import centric.pl.functions.settings.impl.ModeSetting;
import centric.pl.ui.clickgui.impl.Component;

public class ModeComponent extends Component {

    final ModeSetting setting;

    public ModeComponent(ModeSetting setting) {
        this.setting = setting;
        setHeight(22);
    }


    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        super.render(stack, mouseX, mouseY);
        float x = getX() + 2;
        int offset = 0;
        float size = 90.0f;
        int lines = 1;
        for (String mode : this.setting.strings) {
            float preOfsset = (float)offset + Fonts.notoitalic[11].getWidth(mode) + 7.0f;
            if (preOfsset > size) {
                ++lines;
                offset = 0;
            }
            offset = (int)((float)offset + (Fonts.notoitalic[11].getWidth(mode) + 7.0f));
        }
        Fonts.notoitalic[11].drawString(stack, this.setting.getName() + ":", (double)(x + 4.0f), (double)(this.getY() + 4.0f), ThemeSwitcher.textcolor);
        offset = 0;
        float offsetY = 0.0f;
        int i = 0;
        for (String mode : this.setting.strings) {
            float preOfsset = (float)offset + Fonts.notoitalic[11].getWidth(mode) + 7.0f;
            if (preOfsset > size) {
                offset = 0;
                offsetY += 13.0f;
            }
            if (this.setting.getIndex() == i) {
                DisplayUtils.drawGradientRound(x + 4.0f + (float)offset, this.getY() + 10.8f + offsetY, Fonts.notoitalic[11].getWidth(mode) + 4.0f, 8.0f, 1.0f, ColorUtils.getColor(100),ColorUtils.getColor(200),ColorUtils.getColor(300),ColorUtils.getColor(400));
            } else {
                DisplayUtils.drawGradientRound(x + 4.0f + (float)offset, this.getY() + 10.8f + offsetY, Fonts.notoitalic[11].getWidth(mode) + 4.0f, 8.0f, 1.0f, ThemeSwitcher.modesetting, ThemeSwitcher.modesetting, ThemeSwitcher.modesetting, ThemeSwitcher.modesetting);
            }
            Fonts.notoitalic[11].drawString(stack, mode, (double)(x + 6.0f + (float)offset), (double)(this.getY() + 13.5f + offsetY), -1);
            offset = (int)((float)offset + (Fonts.notoitalic[11].getWidth(mode) + 7.0f));
            ++i;
        }
        this.setHeight(22.0f + offsetY);
    }

    @Override
    public void keyPressed(int key, int scanCode, int modifiers) {
        super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {
        super.mouseClick(mouseX, mouseY, mouse);
        int offset = 0;
        float offsetY = 0.0f;
        float size = 90.0f;
        int i = 0;
        for (String mode : this.setting.strings) {
            float preOfsset = (float)offset + Fonts.notoitalic[11].getWidth(mode) + 7.0f;
            if (preOfsset > size) {
                offset = 0;
                offsetY += 13.0f;
            }
            if (MathUtil.isInRegion(mouseX, mouseY, getX() + 4.0f + (float)offset, this.getY() + 8.5f + offsetY, Fonts.notoitalic[11].getWidth(mode) + 1.0f, Fonts.notoitalic[11].getFontHeight() / 2.0f + 5.0f)) {
                this.setting.set(mode);
            }
            offset = (int)((float)offset + (Fonts.notoitalic[11].getWidth(mode) + 7.0f));
            ++i;
        }
    }
}

