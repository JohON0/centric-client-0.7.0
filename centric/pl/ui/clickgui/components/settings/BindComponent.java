package centric.pl.ui.clickgui.components.settings;

import centric.pl.managers.styleManager.ThemeSwitcher;
import centric.pl.johon0.utils.client.KeyStorage;
import centric.pl.johon0.utils.render.ColorUtils;
import centric.pl.johon0.utils.render.DisplayUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import centric.pl.functions.settings.impl.BindSetting;
import centric.pl.ui.clickgui.impl.Component;
import centric.pl.johon0.utils.font.Fonts;
import org.lwjgl.glfw.GLFW;

public class BindComponent extends Component {

    final BindSetting setting;

    public BindComponent(BindSetting setting) {
        this.setting = setting;
        this.setHeight(16);
    }

    boolean activated;
    boolean hovered = false;

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        super.render(stack, mouseX, mouseY);
        Fonts.notoitalic[13].drawString(stack, setting.getName(), getX() + 5, getY() + 9.5f / 2f + 2, ThemeSwitcher.textcolor);
        String bind = KeyStorage.getKey(setting.get());

        if (bind == null || setting.get() == -1) {
            bind = "None";
        }
        boolean next = Fonts.notoitalic[13].getWidth(bind) >= 16;
        float x = next ? getX() + 5 : getX() + getWidth() - 20 - Fonts.notoitalic[13].getWidth(bind);
        float y = getY() + 6.5f / 2f + (5.5f / 2f) + (next ? 8 : 0);
        DisplayUtils.drawRoundedRect(x - 2 + 0.5F, y - 2, Fonts.notoitalic[13].getWidth(bind) + 4, 5.5f + 4, 2, ThemeSwitcher.bgcolor);
        Fonts.notoitalic[13].drawString(stack, bind, x, y + 2, activated ? ThemeSwitcher.textcolor : ColorUtils.rgb(100, 100, 100));
        setHeight(next ? 20 : 16);
    }

    @Override
    public void keyPressed(int key, int scanCode, int modifiers) {
        // TODO Auto-generated method stub
        if (activated) {
            if (key == GLFW.GLFW_KEY_DELETE) {
                setting.set(-1);
                activated = false;
                return;
            }
            setting.set(key);
            activated = false;
        }
        super.keyPressed(key, scanCode, modifiers);
    }


    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {
        if (isHovered(mouseX, mouseY) && mouse == 0) {
            activated = !activated;
        }

        if (activated && mouse >= 1) {
            System.out.println(-100 + mouse);
            setting.set(-100 + mouse);
            activated = false;
        }

        super.mouseClick(mouseX, mouseY, mouse);
    }

    @Override
    public void mouseRelease(float mouseX, float mouseY, int mouse) {
        super.mouseRelease(mouseX, mouseY, mouse);
    }

    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }
}
