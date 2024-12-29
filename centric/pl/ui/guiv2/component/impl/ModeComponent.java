package centric.pl.ui.guiv2.component.impl;

import centric.pl.ui.guiv2.component.ThemeChanger;
import com.mojang.blaze3d.matrix.MatrixStack;
import centric.pl.functions.settings.impl.ModeSetting;
import centric.pl.johon0.utils.font.Fonts;
import centric.pl.johon0.utils.math.MathUtil;
import centric.pl.johon0.utils.render.DisplayUtils;

import java.awt.*;
import java.util.HashMap;

public class ModeComponent extends Component {

    public ModeSetting option;

    public boolean opened;
    public HashMap<String, Float> animation = new HashMap<>();

    public ModeComponent(ModeSetting option) {
        this.option = option;
        for (String s : option.strings) {
            animation.put(s, 0f);
        }
        this.setting = option;
    }

    @Override
    public void drawComponent(MatrixStack matrixStack, int mouseX, int mouseY) {
        float off = 4;
        float offset = 17 - 8;
        for (String s : option.strings) {
            offset += 9;
        }
        if (!opened) offset = 0;
        Fonts.roboto[13].drawString(matrixStack, option.getName(), x + 5, y + 15, new Color(67, 71, 84).getRGB());

        off += Fonts.roboto[14].getFontHeight() / 2f + 2;
        height += offset + 7;
        if (ThemeChanger.theme == 0) {
            DisplayUtils.drawShadow(x + 62, y + off, width - 70, 20 - 6, 10, new Color(22, 23, 25, 50).getRGB());
            DisplayUtils.drawRoundedRect(x + 62, y + off, width - 70, 20 - 6, 1, new Color(22, 23, 25).getRGB());
            DisplayUtils.drawShadow(x + 62, y + off + 17, width - 70, offset, 12, new Color(0, 0, 0, 100).getRGB());
            DisplayUtils.drawRoundedRect(x + 62, y + off + 17, width - 70, offset, 1, new Color(22, 23, 25).getRGB());
            Fonts.roboto[13].drawString(matrixStack, option.get(), x + 66, y + 20 - 4, -1);
            if (opened) {
                Fonts.iconsall[14].drawString(matrixStack, "A", x + 66 + width - 85, y + 20 - 4, new Color(67, 71, 84).getRGB());
            } else {
                Fonts.iconsall[14].drawString(matrixStack, "B", x + 66 + width - 85, y + 20 - 4, new Color(67, 71, 84).getRGB());
            }
        }
        if (ThemeChanger.theme == 1) {
// Тень для первого прямоугольника
            DisplayUtils.drawShadow(x + 62, y + off, width - 70, 20 - 6, 10, new Color(223, 229, 243, 50).getRGB());

// Закругленный прямоугольник
            DisplayUtils.drawRoundedRect(x + 62, y + off, width - 70, 20 - 6, 1, new Color(223, 229, 243).getRGB());

// Тень для второго прямоугольника
            DisplayUtils.drawShadow(x + 62, y + off + 17, width - 70, offset, 12, new Color(223, 229, 243, 100).getRGB());

// Закругленный прямоугольник второго блока
            DisplayUtils.drawRoundedRect(x + 62, y + off + 17, width - 70, offset, 1, new Color(223, 229, 243).getRGB());

// Отображение текста
            Fonts.roboto[13].drawString(matrixStack, option.get(), x + 66, y + 20 - 4,new Color(30, 30, 30).getRGB());

// Условное отображение иконки
            if (opened) {
                Fonts.iconsall[14].drawString(matrixStack, "A", x + 66 + width - 85, y + 20 - 4, new Color(70, 70, 70).getRGB());
            } else {
                Fonts.iconsall[14].drawString(matrixStack, "B", x + 66 + width - 85, y + 20 - 4, new Color(70, 70, 70).getRGB());
            }


        }


        if (opened) {
            int i = 1;
            for (String s : option.strings) {
                boolean hovered = MathUtil.isInRegion(mouseX, mouseY, x, y + off + 20 + i, width, 8);
                animation.put(s, MathUtil.lerp(animation.get(s), hovered ? 2 : 0, 10));
                Fonts.roboto[14].drawString(matrixStack, s, x + 66 + animation.get(s), y + off + 23.5F + i, option.get().equals(s) ? ThemeChanger.theme == 0 ? new Color(255, 255, 255).getRGB() : new Color(0, 0, 0).getRGB() : new Color(98, 98, 98).getRGB());
                i += 9;
            }
            height += 3;
        }

    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        float off = 3;
        off += Fonts.roboto[14].getFontHeight() / 2f + 2;
        if (MathUtil.isInRegion(mouseX, mouseY, x + 62, y + off, width - 10, 20 - 5)) {
            opened = !opened;
        }


        if (!opened) return;
        int i = 1;
        for (String s : option.strings) {
            if (MathUtil.isInRegion(mouseX, mouseY, x, y + off + 20F + i, width, 8))
                option.set(s);
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
