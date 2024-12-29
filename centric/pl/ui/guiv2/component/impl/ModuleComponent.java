package centric.pl.ui.guiv2.component.impl;

import centric.pl.Main;
import centric.pl.ui.guiv2.component.ThemeChanger;
import com.mojang.blaze3d.matrix.MatrixStack;
import centric.pl.functions.api.Function;
import centric.pl.functions.settings.Setting;
import centric.pl.functions.settings.impl.*;
import centric.pl.ui.guiv2.ClickGui;
import centric.pl.johon0.utils.client.KeyStorage;
import centric.pl.johon0.utils.math.MathUtil;
import centric.pl.johon0.utils.render.ColorUtils;
import centric.pl.johon0.utils.render.DisplayUtils;
import net.minecraft.util.ResourceLocation;
import org.joml.Vector4f;
import centric.pl.johon0.utils.font.Fonts;


import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class ModuleComponent extends Component {

    public Function function;

    public List<Component> components = new ArrayList<>();


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
                components.add(new ListComponent(mode));
            }
            if (setting instanceof StringSetting string) {
                components.add(new TextComponent(string));
            }
            if (setting instanceof ColorSetting colorSetting) {
                components.add(new ColorComponent(colorSetting));
            }
        }
    }

    Color color;
    Color colorshadow;
    Color textcolor;
    public float animationToggle;
    public static ModuleComponent binding;

    @Override
    public void drawComponent(MatrixStack matrixStack, int mouseX, int mouseY) {
        float totalHeight = 0;

        /**
         * Вычисляем общую высоту компонентов
         */
        for (Component component : components) {
            if (component.setting != null && component.setting.visible.get()) {
                totalHeight += component.height;
            }
        }
        // Темная тема
        if (ThemeChanger.theme == 0) {
            color = new Color(17, 17, 18);
            colorshadow = new Color(17, 17, 17);
            textcolor = new Color(255, 255, 255);;
        }

        // Светлая тема
        if (ThemeChanger.theme == 1) {
            colorshadow = new Color(200, 200, 200);
            color = new Color(180, 180, 180);
            textcolor = new Color(0, 0, 0);
        }

        // Синяя тема
        if (ThemeChanger.theme == 2) {
            color = new Color(10, 10, 25); // Темно-синий фон
            colorshadow = new Color(5, 5, 15); // Темная тень
            textcolor = new Color(255, 255, 255); // Светло-синий текст
        }

        components.forEach(c -> {
            c.function = function;
            c.parent = parent;
        });

        // Анимация переключения
        animationToggle = MathUtil.lerp(animationToggle, function.isState() ? 1 : 0, 10);
        int colortextfunc = ColorUtils.interpolateColor(ColorUtils.IntColor.rgba(100, 100, 100, 255), textcolor.getRGB(), animationToggle);
        // Рисуем тень и фон

        //darktheme
        if (ThemeChanger.theme == 0) {
            DisplayUtils.drawShadow(x, y, width, height + totalHeight-15, 10, colorshadow.getRGB());
            DisplayUtils.drawRoundedRect(x, y, width, height + totalHeight-15, new Vector4f(3.5f, 3.5f, 3.5f, 3.5f), color.getRGB());
            DisplayUtils.drawRoundedRect(x, y, width, 18, new Vector4f(3.5f, 0, 3.5f, 0), ColorUtils.IntColor.rgba(20, 21, 23, 255));
            for (Component component : components) {
                if (component.setting != null && component.setting.visible.get()) {
//                    DisplayUtils.drawRoundedRect(x, y+15, width, 5, 0, ColorUtils.IntColor.rgba(15, 15, 15, 255));
                    DisplayUtils.drawImage(new ResourceLocation("centric/images/gradline.png"),x, y+18, width, 1, Main.getInstance().getStyleManager().getCurrentStyle().getFirstColor().getRGB());

                }
            }
        }
        // Светлая тема
        if (ThemeChanger.theme == 1) {
            DisplayUtils.drawShadow(x, y, width, height + totalHeight - 15, 10, new Color(220, 220, 220, 150).getRGB()); // Светлая тень
            DisplayUtils.drawRoundedRect(x, y, width, height + totalHeight - 15, new Vector4f(3.5f, 3.5f, 3.5f, 3.5f), new Color(241, 243, 249).getRGB()); // Белый фон
            DisplayUtils.drawRoundedRect(x, y, width, 18, new Vector4f(3.5f, 0, 3.5f, 0), ColorUtils.IntColor.rgba(229, 235, 247,255 )); // Очень светлый фон

            for (Component component : components) {
                if (component.setting != null && component.setting.visible.get()) {
//                    DisplayUtils.drawRoundedRect(x, y + 15, width, 5, 0, ColorUtils.IntColor.rgba(200, 200, 200, 255)); // Светлый фон для компонента
                    DisplayUtils.drawImage(new ResourceLocation("centric/images/gradline.png"), x, y + 18, width, 1, Main.getInstance().getStyleManager().getCurrentStyle().getFirstColor().getRGB());
                }
            }

        }



        // Рисуем текст функции
        Fonts.roboto[14].drawCenteredString(matrixStack, function.getName(), x + 70.5f, y + 7f, colortextfunc);

        // Обработка привязки клавиш
        String key = KeyStorage.getKey(function.getBind());
        if (binding == this && key != null) {
            float keyX = x + width - 20 - Fonts.roboto[14].getWidth(key) + 5;
            DisplayUtils.drawRoundedRect(keyX, y + 5, 10 + Fonts.roboto[14].getWidth(key), 10, 2, color.brighter().getRGB());
            Fonts.roboto[14].drawCenteredString(matrixStack, key, keyX + (10 + Fonts.roboto[14].getWidth(key)) / 2, y + 9, colortextfunc);
        }

        // Рисуем дочерние компоненты
        float offsetY = 0;
        for (Component component : components) {
            if (component.setting != null && component.setting.visible.get()) {
                component.setPosition(x+2, y + height + offsetY-15, width, 20);
                component.drawComponent(matrixStack, mouseX, mouseY);
                offsetY += component.height;
            }
        }
    }


    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (MathUtil.isInRegion(mouseX, mouseY, x + 5, y, width - 10, 17) && mouseButton <= 1) {
            function.toggle();
        }

        if (binding == this && mouseButton > 2) {
            function.setBind(-100 + mouseButton);
            binding = null;
        }

        if (MathUtil.isInRegion(mouseX, mouseY, x + 5, y, width - 10, 20)) {
            if (mouseButton == 2) {
                ClickGui.typing = false;
                binding = this;
            }
        }
        components.forEach(component -> component.mouseClicked(mouseX, mouseY, mouseButton));
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        components.forEach(component -> component.mouseReleased(mouseX, mouseY, mouseButton));
    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {
        components.forEach(component -> component.keyTyped(keyCode, scanCode, modifiers));
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
        components.forEach(component -> component.charTyped(codePoint, modifiers));
    }
}
