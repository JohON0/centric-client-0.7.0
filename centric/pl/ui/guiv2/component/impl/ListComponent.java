package centric.pl.ui.guiv2.component.impl;

import centric.pl.ui.guiv2.component.ThemeChanger;
import com.mojang.blaze3d.matrix.MatrixStack;
import centric.pl.functions.settings.impl.BooleanSetting;
import centric.pl.functions.settings.impl.ModeListSetting;
import centric.pl.johon0.utils.font.Fonts;
import centric.pl.johon0.utils.math.MathUtil;
import centric.pl.johon0.utils.render.DisplayUtils;
import centric.pl.johon0.utils.render.Scissor;

import java.awt.*;

public class ListComponent extends Component {

    public ModeListSetting option;

    public boolean opened;

    public ListComponent(ModeListSetting option) {
        this.option = option;
        this.setting = option;
    }

    @Override
    public void drawComponent(MatrixStack matrixStack, int mouseX, int mouseY) {
        float off = 4;
        float offset = 17 - 8;
        for (BooleanSetting s : option.get()) {
            offset += 9;
        }
        if (!opened) offset = 0;
        Fonts.roboto[14].drawString(matrixStack, option.getName(), x + 5, y + 15, new Color(67, 71, 84).getRGB());
        off += Fonts.roboto[14].getFontHeight() / 2f + 2;
        height += offset + 7;
        if (ThemeChanger.theme == 0) {
            DisplayUtils.drawShadow(x + 62, y + off, width - 70, 20 - 6, 10, new Color(22, 23, 25, 50).getRGB());
            DisplayUtils.drawRoundedRect(x + 62, y + off, width - 70, 20 - 6, 1, new Color(22, 23, 25).getRGB());
            DisplayUtils.drawShadow(x + 62, y + off + 17, width - 70, offset, 12, new Color(0, 0, 0, 100).getRGB());
            DisplayUtils.drawRoundedRect(x + 62, y + off + 17, width - 70, offset, 1, new Color(22, 23, 25).getRGB());
            Scissor.push();
            Scissor.setFromComponentCoordinates(x + 66, y + off, width - 85, 20 - 6);
            Fonts.roboto[14].drawString(matrixStack, option.getNames(), x + 66, y + 20 - 4, -1);
            Scissor.unset();
            Scissor.pop();
            if (opened) {
                Fonts.iconsall[14].drawString(matrixStack, "A", x + 68 + width - 85, y + 22 - 4, new Color(67, 71, 84).getRGB());
            } else {
                Fonts.iconsall[14].drawString(matrixStack, "B", x + 68 + width - 85, y + 22 - 4, new Color(67, 71, 84).getRGB());
            }
        }
        if (ThemeChanger.theme == 1) {
            DisplayUtils.drawShadow(x + 62, y + off, width - 70, 20 - 6, 10,  new Color(223, 229, 243, 50).getRGB());
            DisplayUtils.drawRoundedRect(x + 62, y + off, width - 70, 20 - 6, 1, new Color(223, 229, 243).getRGB());
            DisplayUtils.drawShadow(x + 62, y + off + 17, width - 70, offset, 12, new Color(223, 229, 243, 100).getRGB());
            DisplayUtils.drawRoundedRect(x + 62, y + off + 17, width - 70, offset, 1, new Color(223, 229, 243).getRGB());
            Scissor.push();
            Scissor.setFromComponentCoordinates(x + 66, y + off, width - 85, 20 - 6);
            Fonts.roboto[14].drawString(matrixStack, option.getNames(), x + 66, y + 20 - 4, new Color(30, 30, 30).getRGB());
            Scissor.unset();
            Scissor.pop();
            if (opened) {
                Fonts.iconsall[14].drawString(matrixStack, "A", x + 68 + width - 85, y + 22 - 4, new Color(70, 70, 70).getRGB());
            } else {
                Fonts.iconsall[14].drawString(matrixStack, "B", x + 68 + width - 85, y + 22 - 4, new Color(70, 70, 70).getRGB());
            }
        }

        if (opened) {
            int i = 1;
            for (BooleanSetting s : option.get()) {
                boolean hovered = MathUtil.isInRegion(mouseX, mouseY, x, y + off + 20 + i, width, 8);
                s.anim = MathUtil.lerp(s.anim, (hovered ? 2 : 0), 10);
                Fonts.roboto[14].drawString(matrixStack, s.getName(), x + 66 + s.anim, y + off + 23.5F + i, option.getValueByName(s.getName()).get() ? ThemeChanger.theme == 0 ? new Color(255, 255, 255).getRGB() : new Color(0, 0, 0).getRGB() : new Color(98, 98, 98).getRGB());
                i += 9;
            }
            height += 3;
        }

    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

        float off = 3;
        off += Fonts.roboto[14].getFontHeight() / 2f + 2;
        if (MathUtil.isInRegion(mouseX, mouseY, x + 5, y + off, width - 10, 20 - 5)) {
            opened = !opened;
        }


        if (!opened) return;
        int i = 1;
        for (BooleanSetting s : option.get()) {
            if (MathUtil.isInRegion(mouseX, mouseY, x, y + off + 20F + i, width, 8))
                option.set((i - 1) / 9, !option.getValueByName(s.getName()).get());
            i += 9;
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
