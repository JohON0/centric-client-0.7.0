package centric.pl.ui.guiv2.component.impl;

import centric.pl.johon0.utils.render.ColorUtils;
import centric.pl.ui.guiv2.component.ThemeChanger;
import com.mojang.blaze3d.matrix.MatrixStack;
import centric.pl.functions.settings.impl.BindSetting;
import centric.pl.johon0.utils.client.KeyStorage;
import centric.pl.johon0.utils.font.Fonts;
import centric.pl.johon0.utils.render.DisplayUtils;

import java.awt.*;

public class BindComponent extends Component {

    public BindSetting option;
    boolean bind;


    public BindComponent(BindSetting option) {
        this.option = option;
        this.setting = option;
    }

    @Override
    public void drawComponent(MatrixStack matrixStack, int mouseX, int mouseY) {

        height -= 3;

        String bindString = option.get() == 0 ? "None" : KeyStorage.getKey(option.get());

        if (bindString == null) {
            bindString = "";
        }

        float width = Fonts.roboto[12].getWidth(bindString) + 4;
        if (ThemeChanger.theme == 0) {
            DisplayUtils.drawRoundedRect(x + 120 - width/2 + 5, y + 2, width, 10, 2, bind ? new Color(17, 18, 21).brighter().brighter().getRGB() : new Color(17, 18, 21).brighter().getRGB());

        }
        if (ThemeChanger.theme == 1) {
            DisplayUtils.drawRoundedRect(x + 120 - width / 2 + 5, y + 2, width, 10, 2,
                    bind ? new Color(223, 229, 243).darker().getRGB() :  new Color(223, 229, 243).getRGB());


        }
        Fonts.roboto[12].drawCenteredString(matrixStack, bindString, x + 125, y + 6, ThemeChanger.theme == 0 ? -1 : ColorUtils.rgb(0,0,0));
        Fonts.roboto[12].drawString(matrixStack, option.getName(), x +5, y + 6, new Color(69,71,84).getRGB());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (bind && mouseButton > 1) {
            option.set(-100 + mouseButton);
            bind = false;
        }
        if (isHovered(mouseX, mouseY) && mouseButton == 0) {
            bind = true;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {

    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {
        if (bind) {
            if (keyCode == 261) {
                option.set(0);
                bind = false;
                return;
            }
            option.set(keyCode);
            bind = false;
        }
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {

    }
}
