package centric.pl.ui.clickgui.components;

import centric.pl.functions.api.Function;
import centric.pl.functions.settings.impl.*;
import centric.pl.ui.clickgui.components.settings.*;
import centric.pl.managers.styleManager.ThemeSwitcher;
import centric.pl.johon0.utils.client.KeyStorage;
import centric.pl.johon0.utils.font.Fonts;
import centric.pl.johon0.utils.math.Vector4i;
import centric.pl.johon0.utils.render.ColorUtils;
import centric.pl.johon0.utils.render.DisplayUtils;
import centric.pl.johon0.utils.render.Stencil;
import centric.pl.johon0.utils.render.font.FontsUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import centric.pl.functions.settings.Setting;
import centric.pl.ui.clickgui.impl.Component;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.glfw.GLFW;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;

import static centric.pl.functions.impl.render.HUD.getColor;

@Getter
public class ModuleComponent extends Component {
    private final Vector4f ROUNDING_VECTOR = new Vector4f(7, 7, 7, 7);
    private final Vector4i BORDER_COLOR = new Vector4i(ColorUtils.rgb(45, 46, 53), ColorUtils.rgb(25, 26, 31), ColorUtils.rgb(45, 46, 53), ColorUtils.rgb(25, 26, 31));

    private final Function function;
    public Animation animation = new Animation();
    public boolean open;
    private boolean bind;

    private final ObjectArrayList<Component> components = new ObjectArrayList<>();

    public ModuleComponent(Function function) {
        this.function = function;
        for (Setting<?> setting : function.getSettings()) {
            if (setting instanceof BooleanSetting bool) {
                components.add(new BooleanComponent(bool));
            }
            if (setting instanceof SliderSetting slider) {
                components.add(new SliderComponent(slider));
            }
            if (setting instanceof BindSetting bind) {
                components.add(new BindComponent(bind));
            }
            if (setting instanceof ModeSetting mode) {
                components.add(new ModeComponent(mode));
            }
            if (setting instanceof ModeListSetting mode) {
                components.add(new BoxComponent(mode));
            }
            if (setting instanceof StringSetting string) {
                components.add(new StringComponent(string));
            }
            if (setting instanceof ColorSetting colorSetting) {
                components.add(new ColorComponent(colorSetting));
            }
        }
        animation = animation.animate(open ? 1 : 0, 0.3);
    }

    // draw components
    public void drawComponents(MatrixStack stack, float mouseX, float mouseY) {
        if (animation.getValue() > 0) {
            if (animation.getValue() > 0.1 && components.stream().filter(Component::isVisible).count() >= 1) {
                DisplayUtils.drawRoundedRect(this.getX() + 2f, this.getY() + 15, this.getWidth() - 16, this.getHeight() - 17.0f, 2.0f, ThemeSwitcher.backgroundcolor);
            }

            Stencil.initStencilToWrite();
            DisplayUtils.drawRoundedRect(getX() + 2, getY() + 1, getWidth() - 16, getHeight() - 1, 4, ColorUtils.rgba(23, 23, 23, (int) (255 * 0.33)));
            Stencil.readStencilBuffer(1);
            float y = getY() + 15;
            for (Component component : components) {
                if (component.isVisible()) {
                    component.setX(getX());
                    component.setY(y);
                    component.setWidth(getWidth());
                    component.render(stack, mouseX, mouseY);
                    y += component.getHeight();
                }
            }
            Stencil.uninitStencilBuffer();

        }
    }

    @Override
    public void mouseRelease(float mouseX, float mouseY, int mouse) {
        // TODO Auto-generated method stub

        for (Component component : components) {
            component.mouseRelease(mouseX, mouseY, mouse);
        }

        super.mouseRelease(mouseX, mouseY, mouse);
    }

    private boolean hovered = false;

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        int color = ColorUtils.interpolate(ThemeSwitcher.textcolor, ColorUtils.rgb(100, 100, 100), (float) function.getAnimation().getValue());

        function.getAnimation().update();
        super.render(stack, mouseX, mouseY);

        drawOutlinedRect(mouseX, mouseY, color);
        drawText(stack, color);
        drawComponents(stack, mouseX, mouseY);
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int button) {
        if (isHovered(mouseX, mouseY, 20)) {
            if (button == 0) function.toggle();
            if (button == 1) {
                open = !open;
                animation = animation.animate(open ? 1 : 0, 0.2, Easings.CIRC_OUT);
            }
            if (button == 2) {
                bind = !bind;
            }
        }
        if (isHovered(mouseX, mouseY)) {
            if (open) {
                for (Component component : components) {
                    if (component.isVisible()) component.mouseClick(mouseX, mouseY, button);
                }
            }
        }
        super.mouseClick(mouseX, mouseY, button);
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
        for (Component component : components) {
            if (component.isVisible()) component.charTyped(codePoint, modifiers);
        }
        super.charTyped(codePoint, modifiers);
    }

    @Override
    public void keyPressed(int key, int scanCode, int modifiers) {
        for (Component component : components) {
            if (component.isVisible()) component.keyPressed(key, scanCode, modifiers);
        }
        if (bind) {
            if (key == GLFW.GLFW_KEY_DELETE) {
                function.setBind(0);
            } else function.setBind(key);
            bind = false;
        }

        super.keyPressed(key, scanCode, modifiers);
    }
    private void drawOutlinedRect(float mouseX, float mouseY, int color) {
        int imgay = (int) (255 * 0.83);
        DisplayUtils.drawRoundedRect(this.getX()+2, this.getY(), this.getWidth()-15, this.getHeight()-1, 2.0f, ColorUtils.setAlpha(ThemeSwitcher.bgcolor,imgay));

    }

    private void drawText(MatrixStack stack, int color) {
        DisplayUtils.drawShadow(getX() + 5, getY() + 4.5f, Fonts.notoitalic[15].getWidth(function.getName()) + 3, Fonts.notoitalic[15].getFontHeight(), 5, ColorUtils.setAlpha(color, (int) (32 * function.getAnimation().getValue())));

        Fonts.notoitalic[15].drawString(stack, function.getName(), getX() + 6, getY() + 6.5f, color);
        if (function.getBeta()) {
            Fonts.notoitalic[15].drawString(stack, "Beta",getX() + Fonts.notoitalic[15].getWidth(function.getName()) + 10.0f, getY() + 6.5f, ColorUtils.setAlpha(ColorUtils.gradient(getColor(100),getColor(200),10,10), 100));
        }
        String bindText = function.getBind() == 0 ? "..." : KeyStorage.getKey(function.getBind());

        if (bindText.length() > 6) {
            bindText = bindText.substring(0, 6) + "...";
        }

        if (components.stream().filter(Component::isVisible).count() >= 1) {


            if (bind) {
                Fonts.notoitalic[15].drawString(stack, bindText, this.getX() + getWidth()-22, this.getY() + 6.5f, ThemeSwitcher.textcolor);
            } else
                FontsUtil.icons.drawText(stack, !open ? "B" : "C", this.getX() + getWidth()-22, this.getY() + 6.5f, ThemeSwitcher.textcolor, 6);
        } else {
            if (bind) {
                Fonts.notoitalic[15].drawString(stack, bindText, this.getX() + getWidth()-22, this.getY() + 6.5f, ThemeSwitcher.textcolor);
            }
        }
    }
}
