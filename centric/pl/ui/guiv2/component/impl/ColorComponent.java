package centric.pl.ui.guiv2.component.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import centric.pl.functions.settings.impl.ColorSetting;
import centric.pl.ui.guiv2.component.ColorWindow;
import centric.pl.johon0.utils.font.Fonts;
import centric.pl.johon0.utils.math.MathUtil;
import centric.pl.johon0.utils.render.DisplayUtils;

import java.awt.*;

public class ColorComponent extends Component {

    public static ColorWindow opened;
    public ColorSetting option;
    public ColorWindow setted;

    public ColorComponent(ColorSetting option) {
        this.option = option;
        setted = new ColorWindow(this);
        this.setting = option;
    }

    @Override
    public void drawComponent(MatrixStack matrixStack, int mouseX, int mouseY) {
// Предположим, option.get() возвращает строку
        String optionValue = String.valueOf(option.get());
        String hexValue1;
// Преобразуем строку в целое число
        int value = Integer.parseInt(optionValue);

// Преобразуем целое число в шестнадцатеричное значение
        String hexValue = Integer.toHexString(value);

// Проверяем, достаточно ли длинная строка
        if (hexValue.length() > 3) {
            hexValue = hexValue.substring(3); // Убираем первые 3 символа
        } else {
            hexValue1 = ""; // Если строка короче или равна 3 символам, делаем её пустой
        }

// Если нужно добавить ведущий ноль для 2-значного формата
        if (hexValue.length() < 6) {
            hexValue = String.format("%06x", value);
        }

        Fonts.notoitalic[12].drawString(matrixStack, option.getName(), x + 5, y + height / 2f - 1, new Color(69,71,84).getRGB());

        float size = 8;
        DisplayUtils.drawRoundedRect(x + width - 50 - size /2f, y + height / 2f - size /2f,45,14,1,new Color(22,23,25).getRGB());
        Fonts.notoitalic[12].drawString(matrixStack, "#" + hexValue, x + width - 50 - size /2f, y + height / 2f - size /2f+5, option.get());

    }



    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        float size = 12;
        if (MathUtil.isInRegion(mouseX,mouseY, x + width - 10 - size /2f, y + height / 2f - size /2f, size,size)) {
            if (setted == opened) {
                opened = null;
                return;
            }
            opened = setted;
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

    @Override
    public void onConfigUpdate() {
        super.onConfigUpdate();
        setted.onConfigUpdate();
    }
}
