package centric.pl.ui.clickgui.components.settings;

import centric.pl.managers.styleManager.ThemeSwitcher;
import centric.pl.johon0.utils.font.Fonts;
import centric.pl.johon0.utils.math.MathUtil;
import centric.pl.johon0.utils.render.ColorUtils;
import centric.pl.johon0.utils.render.DisplayUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import centric.pl.functions.settings.impl.SliderSetting;
import centric.pl.ui.clickgui.impl.Component;
import net.minecraft.util.math.MathHelper;

/**
 * SliderComponent
 */
public class SliderComponent extends Component {

    private final SliderSetting setting;

    public SliderComponent(SliderSetting setting) {
        this.setting = setting;
        this.setHeight(18);
    }

    private float anim;
    private boolean drag;
    private boolean hovered = false;

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {

        super.render(stack, mouseX, mouseY);
        float x = getX() + 2;
        Fonts.notoitalic[12].drawString(stack, this.setting.getName() + ":", (double)(x + 4.0f), (double)(this.getY()+4), ThemeSwitcher.textcolor);
        Fonts.notoitalic[12].drawCenteredString(stack, String.valueOf(this.setting.get()), (double)(x + 92.0f), (double)(this.getY() + 10.5f), ThemeSwitcher.textcolor);
        float sliderWidth = this.anim = MathUtil.fast(this.anim, 72.0f * (this.setting.get().floatValue() - this.setting.min) / (this.setting.max - this.setting.min), 20.0f);
        DisplayUtils.drawGradientRound(x + 5.0f, this.getY() + 11.5f, 71.5f, 2, 1f, ColorUtils.rgba(40, 40, 40, 200), ColorUtils.rgba(40, 40, 40, 200), ColorUtils.rgba(40, 40, 40, 200), ColorUtils.rgba(40, 40, 40, 200));
        DisplayUtils.drawGradientRound(x + 5.0f, this.getY() + 11.5f, sliderWidth, 2, 1f, ColorUtils.getColor(90), ColorUtils.getColor(0), ColorUtils.getColor(90), ColorUtils.getColor(0));
        DisplayUtils.drawCircle(x + sliderWidth+5,this.getY() + 12.3f,7, ThemeSwitcher.circlecolor);

        if (this.drag) {
            this.setting.set((float)MathHelper.clamp(MathUtil.round((double)((mouseX - x - 5.0f) / 72.0f * (this.setting.max - this.setting.min) + this.setting.min), this.setting.increment), (double)this.setting.min, (double)this.setting.max));
        }
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {
        float x = getX() + 2;
        if (MathUtil.isInRegion(mouseX, mouseY, x + 5.0f, this.getY() + 9.5f, 75.0f, 4.0f)) {
            this.drag = true;
        }
        super.mouseClick(mouseX, mouseY, mouse);
    }
    

    @Override
    public void mouseRelease(float mouseX, float mouseY, int mouse) {
        this.drag = false;
        super.mouseRelease(mouseX, mouseY, mouse);
    }

    @Override
    public boolean isVisible() {
        return (Boolean)this.setting.visible.get();
    }
}