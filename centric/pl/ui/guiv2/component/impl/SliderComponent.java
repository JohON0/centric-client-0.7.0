package centric.pl.ui.guiv2.component.impl;

import centric.pl.functions.impl.render.HUD;
import centric.pl.ui.guiv2.component.ThemeChanger;
import com.mojang.blaze3d.matrix.MatrixStack;
import centric.pl.functions.settings.impl.SliderSetting;
import centric.pl.johon0.utils.render.DisplayUtils;
import net.minecraft.util.math.MathHelper;
import centric.pl.johon0.utils.font.Fonts;
import centric.pl.johon0.utils.math.MathUtil;
import org.joml.Vector4f;

import java.awt.*;

public class SliderComponent extends Component {

    public SliderSetting option;

    public SliderComponent(SliderSetting option) {
        this.option = option;
        this.setting = option;

    }

    boolean drag;

    float anim;

    @Override
    public void drawComponent(MatrixStack matrixStack, int mouseX, int mouseY) {
        height += 2;
        float sliderWidth = ((option.get() - option.min) / (option.max - option.min)) * (width - 12);
        anim = MathUtil.lerp(anim, sliderWidth, 10);
        Fonts.roboto[12].drawString(matrixStack, option.getName(), x + 4, y + 4, new Color(69,71,84).getRGB());
        Fonts.roboto[12].drawString(matrixStack, String.valueOf(option.get()), x + width - Fonts.roboto[14].getWidth(String.valueOf(option.get())) - 4, y + 4, new Color(69,71,84).getRGB());
        DisplayUtils.drawRoundedRect(x + 4, y + 13, width - 12, 3, new Vector4f(2, 2, 2, 2), ThemeChanger.theme == 1 ? new Color(223, 229, 243).getRGB() : new Color(26, 29, 33).getRGB());

        DisplayUtils.drawShadow(x + 4, y + 14, anim, 3, 8, HUD.getColor(100));

        DisplayUtils.drawRoundedRect(x + 4, y + 13, anim, 3, new Vector4f(2, 2,
                option.max == option.get() ? 2 : 0, 2), HUD.getColor(100));

//        DisplayUtils.drawCircle(x + 5 + anim, y + 14.5f, 10, new Color(17, 18, 21).getRGB());
//        DisplayUtils.drawCircle(x + 5 + anim, y + 14.5f, 8, new Color(74, 166, 218).getRGB());
//        DisplayUtils.drawCircle(x + 5 + anim, y + 14.5f, 6, new Color(17, 18, 21).getRGB());
        if (drag) {
            float draggingValue = (float) MathHelper.clamp(MathUtil.round((mouseX - x + 4) / (width - 12)
                    * (option.max - option.min) + option.min, option.increment), option.min, option.max);
            option.set(draggingValue);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isHovered(mouseX, mouseY)) {
            drag = true;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        drag = false;
    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {

    }

    @Override
    public void charTyped(char codePoint, int modifiers) {

    }
}
