package centric.pl.ui.clickgui.components.settings;

import centric.pl.johon0.utils.render.ColorUtils;
import centric.pl.johon0.utils.render.DisplayUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import centric.pl.functions.settings.impl.BooleanSetting;
import centric.pl.functions.settings.impl.ModeListSetting;
import centric.pl.ui.clickgui.impl.Component;
import centric.pl.johon0.utils.math.MathUtil;
import centric.pl.johon0.utils.font.Fonts;

import static centric.pl.managers.styleManager.ThemeSwitcher.modesetting;
import static centric.pl.managers.styleManager.ThemeSwitcher.textcolor;

public class BoxComponent extends Component {

    final ModeListSetting setting;

    float width = 0;
    float heightPadding = 0;

    public BoxComponent(ModeListSetting setting) {
        this.setting = setting;
        setHeight(22);
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        super.render(stack, mouseX, mouseY);
        Fonts.notoitalic[11].drawString(stack,setting.getName() + ":",getX() + 4, getY() + 4, textcolor);

        float offset = 0;
        float heightoff = 0;
        boolean plused = false;
        boolean anyHovered = false;
        for (BooleanSetting text : setting.get()) {
            float off = Fonts.notoitalic[11].getWidth(text.getName()) + 5;
            if (offset + off >= (getWidth() - 15)) {
                offset = 0;
                heightoff += 13;
                plused = true;
            }
            if (MathUtil.isInRegion(mouseX, mouseY, getX() + 5 + offset, getY() + 11.5f + heightoff,
                    Fonts.notoitalic[11].getWidth(text.getName()), Fonts.notoitalic[11].getFontHeight())) {
                anyHovered = true;
            }
            if (text.get()) {
                DisplayUtils.drawGradientRound(getX() + 5 + offset, getY() + 11.5f + heightoff, Fonts.notoitalic[11].getWidth(text.getName()) + 2.0f, 8.0f, 1.0f, ColorUtils.getColor(100),ColorUtils.getColor(200),ColorUtils.getColor(300),ColorUtils.getColor(400));

            } else {
                DisplayUtils.drawGradientRound(getX() + 5 + offset, getY() + 11.5f + heightoff, Fonts.notoitalic[11].getWidth(text.getName()) + 2.0f, 8.0f, 1.0f, modesetting,modesetting,modesetting,modesetting);

            }
            Fonts.notoitalic[11].drawString(stack, text.getName(), (double)(getX() + 6.0f + (float)offset), (double)(this.getY() + 14.5f + heightoff), -1);

            offset += off;
        }
        width = plused ? getWidth() - 15 : offset;
        setHeight(22 + heightoff);
        heightPadding = heightoff;
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {

        float offset = 0;
        float heightoff = 0;
        for (BooleanSetting text : setting.get()) {
            float off = Fonts.notoitalic[11].getWidth(text.getName()) + 5;
            if (offset + off >= (getWidth() - 15)) {
                offset = 0;
                heightoff += 13;
            }
            if (MathUtil.isInRegion(mouseX, mouseY, getX() + 5 + offset, getY() + 11.5f + heightoff,
                    Fonts.notoitalic[11].getWidth(text.getName()), Fonts.notoitalic[11].getFontHeight() + 1)) {
                text.set(!text.get());
            }
            offset += off;
        }


        super.mouseClick(mouseX, mouseY, mouse);
    }

    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }

}
