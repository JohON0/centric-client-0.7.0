package centric.pl.ui.guiv2;

import centric.pl.functions.impl.render.HUD;
import centric.pl.johon0.utils.animations.Animation;
import centric.pl.johon0.utils.animations.Direction;
import centric.pl.johon0.utils.animations.impl.EaseBackIn;
import centric.pl.johon0.utils.render.ColorUtils;
import centric.pl.managers.styleManager.ThemeSwitcher;
import centric.pl.ui.guiv2.component.ThemeChanger;
import centric.pl.ui.guiv2.themegui.ThemesRender;
import com.mojang.blaze3d.matrix.MatrixStack;
import centric.pl.Main;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.ui.guiv2.component.impl.*;
import centric.pl.ui.guiv2.component.impl.Component;
import centric.pl.managers.styleManager.Style;
import centric.pl.johon0.utils.client.ClientUtil;
import centric.pl.johon0.utils.client.Vec2i;
import centric.pl.johon0.utils.font.Fonts;
import centric.pl.johon0.utils.math.MathUtil;
import centric.pl.johon0.utils.render.DisplayUtils;
import centric.pl.johon0.utils.render.Scissor;
import centric.pl.johon0.utils.animations.impl.DecelerateAnimation;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector4f;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static centric.pl.ui.guiv2.component.impl.ModuleComponent.binding;
import static centric.pl.johon0.utils.client.IMinecraft.mc;

public class ClickGui extends Screen {
    private ThemesRender themeDrawing = new ThemesRender();
    public static final Animation openAnimation = new EaseBackIn(400, 1, 1);
    public ClickGui() {
        super(new StringTextComponent("GUI"));
        for (Function function : Main.getInstance().getFunctionRegistry().getFunctions()) {
            objects.add(new ModuleComponent(function));
        }
        cfg.clear();
        for (String config : Main.getInstance().getConfigStorage().getConfigsByName()) {
            cfg.add(new ConfigComponent(Main.getInstance().getConfigStorage().findConfig(config)));
        }
    }

    double xPanel, yPanel;
    Category current = Category.Combat;
    float animation;


    public ArrayList<ModuleComponent> objects = new ArrayList<>();

    private CopyOnWriteArrayList<ConfigComponent> config = new CopyOnWriteArrayList<>();


    public float scroll = 0;
    public float animateScroll = 0;

    @Override
    public void onClose() {
        super.onClose();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        float scale = 2f;
        float width = 665 / scale;
        float height = 550 / scale;
        float leftPanel = 150 / scale;
        float x = MathUtil.calculateXPosition(mc.getMainWindow().scaledWidth() / 2f, width);
        float y = MathUtil.calculateXPosition(mc.getMainWindow().scaledHeight() / 2f, height);
        if (MathUtil.isInRegion((float) mouseX, (float) mouseY,x,y,width,height)) {
            scroll += delta * 15;
        }
        ColorComponent.opened = null;
        this.themeDrawing.scroll(mouseX, mouseY, delta);
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void closeScreen() {
        if (typing || !searchText.isEmpty()) {
            typing = false;
            searchText = "";
        }
        if (configTyping || !configName.isEmpty()) {
            configTyping = false;
            configName = "";
        }
    }
    @Override
    public boolean isPauseScreen() {
        return false;
    }

    boolean searchOpened;
    float seacrh;

    private String searchText = "";
    public static boolean typing;

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (openAnimation.getOutput() == 0.0f && openAnimation.isDone()) {
            minecraft.displayGuiScreen(null);
        }
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.minecraft.keyboardListener.enableRepeatEvents(true);
        float scale = 2f;
        float width = 665 / scale;
        float height = 550 / scale;
        float leftPanel = 150 / scale;
        float x = MathUtil.calculateXPosition(mc.getMainWindow().scaledWidth() / 2f, width);
        float y = MathUtil.calculateXPosition(mc.getMainWindow().scaledHeight() / 2f, height);
        xPanel = x;
        yPanel = y;
        animation = MathUtil.lerp(animation, 0, 5);

        Vec2i fixed = ClientUtil.getMouse((int) mouseX, (int) mouseY);
        mouseX = fixed.getX();
        mouseY = fixed.getY();

        int finalMouseX = mouseX;
        int finalMouseY = mouseY;

        mc.gameRenderer.setupOverlayRendering(2);

        GlStateManager.pushMatrix();

        sizeAnimation(x + (width / 2), y + (height / 2), openAnimation.getOutput());
        renderBackground(matrixStack, x, y, width, height, leftPanel, finalMouseX, finalMouseY);
        renderChangerThemes(matrixStack, x, y, width, height,4);
        renderButtonDir(matrixStack, x, y, width, height, leftPanel, finalMouseX, finalMouseY);
        renderCategories(matrixStack, x, y, width, finalMouseX, finalMouseY);
        Scissor.push();
        Scissor.setFromComponentCoordinates(x, y + 70 / 2f, width, height - 150 / 2f);
        renderComponents(matrixStack, x, y, width, height, leftPanel, finalMouseX, finalMouseY);
        Scissor.unset();
        Scissor.pop();
        renderColorPicker(matrixStack, x, y, width, height, leftPanel, finalMouseX, finalMouseY);
        //renderSearchBar(matrixStack, x, y, width, height, leftPanel, finalMouseX, finalMouseY);

        GlStateManager.popMatrix();
        if (Main.getInstance().getFunctionRegistry().getHud().themeopen.get()) {
            DisplayUtils.drawRoundedRect(mc.getMainWindow().getScaledWidth() - 143, 6, 136.0f, 166.0f, 3.0f, ThemeSwitcher.bgcolor);
            DisplayUtils.drawRoundedRect(mc.getMainWindow().getScaledWidth() - 138, 11, 126.0f, 156.0f, 3.0f, ThemeSwitcher.backgroundcolor);
            DisplayUtils.drawRoundedRect(mc.getMainWindow().getScaledWidth() - 134, 16, 118.0f, 20, 3.0f, ThemeSwitcher.bgcolor);
            Fonts.centricbold[21].drawCenteredString(matrixStack, "Themes",mc.getMainWindow().getScaledWidth() - 75, 23, ThemeSwitcher.textcolor);
            this.themeDrawing.draw(matrixStack, mouseX, mouseY, mc.getMainWindow().getScaledWidth() - 138, 35.0f, 130.0f, 133.0f);
        }
        mc.gameRenderer.setupOverlayRendering();

    }

    void renderColorPicker(MatrixStack matrixStack, float x, float y, float width, float height, float leftPanel, int mouseX, int mouseY) {
        if (ColorComponent.opened != null) {
            ColorComponent.opened.draw(matrixStack, mouseX, mouseY);
        }
    }

    void renderChangerThemes(MatrixStack matrixStack, float x, float y, float width,float height, float radius) {
        if (ThemeChanger.theme == 0) {
            DisplayUtils.drawRoundedRect(x + 10, y + 240, 60, 28, 2, new Color(17, 17, 18).getRGB());
            DisplayUtils.drawRoundedRect(x + 15, y + 245, 22, 17, 2, new Color(22, 23, 25).getRGB());
            DisplayUtils.drawRoundedRect(x + 15 + 22 + 5, y + 245, 22, 17, 2, new Color(22, 23, 25).getRGB());
            Fonts.iconsall[20].drawString(matrixStack, "S", x + 22, y + 251, ThemeChanger.theme == 1 ? HUD.getColor(100) : new Color(62, 64, 76).getRGB());
            Fonts.iconsall[20].drawString(matrixStack, "T", x + 22 + 22 + 5, y + 251, ThemeChanger.theme == 0 ? HUD.getColor(100) : new Color(62, 64, 76).getRGB());
            DisplayUtils.drawRoundedRect(x + 15 + 22 + 5 + 38, y + 245, 2, 20, 1, new Color(19, 19, 19, 255).getRGB());
        }
        if (ThemeChanger.theme == 1) {
            DisplayUtils.drawRoundedRect(x + 10, y + 240, 60, 28, 2, new Color(241, 243, 249).getRGB());
            DisplayUtils.drawRoundedRect(x + 15, y + 245, 22, 17, 2, new Color(223, 229, 243).getRGB());
            DisplayUtils.drawRoundedRect(x + 15 + 22 + 5, y + 245, 22, 17, 2, new Color(223, 229, 243).getRGB());

            Fonts.iconsall[20].drawString(matrixStack, "S", x + 22, y + 251,
                    ThemeChanger.theme == 1 ? HUD.getColor(100) : new Color(100, 100, 100).getRGB());
            Fonts.iconsall[20].drawString(matrixStack, "T", x + 22 + 22 + 5, y + 251,
                    ThemeChanger.theme == 0 ? HUD.getColor(100) : new Color(100, 100, 100).getRGB());
            DisplayUtils.drawRoundedRect(x + 15 + 22 + 5 + 38, y + 245, 2, 20, 1, new Color(242, 244, 250).getRGB());
        }
    }
    void renderBackground(MatrixStack matrixStack, float x, float y, float width, float height, float leftPanel, int mouseX, int mouseY) {
        if (ThemeChanger.theme == 0) {

            // Тень
            DisplayUtils.drawShadow(x, y, width, height, 20, new Color(0, 0, 0, 200).getRGB()); // Очень темная тень

            DisplayUtils.drawRoundedRect(x-5, y-5, width +10, height+10, new Vector4f(4, 4, 4, 4), new Color(18, 18, 20).getRGB()); // Почти черный фон

            // Основной фон
            DisplayUtils.drawRoundedRect(x, y, width, height, new Vector4f(4, 4, 4, 4), new Color(10, 10, 10).getRGB()); // Почти черный фон

            DisplayUtils.drawRoundedRect(x + 10,y + 5, width-20,25,4,new Color(17,17,18).getRGB());
//            Scissor.push();
//            Scissor.setFromComponentCoordinates(x + 10,y + 5, width-20,25);
//            DisplayUtils.drawCircle(x + 45+120+60+60+15-10,y + 30,30,HUD.getColor(100));
//            DisplayUtils.drawCircle(x + 45+120+60+60+25-10,y + 20,30,HUD.getColor(100));
//            DisplayUtils.drawRoundedRect(x + 45+120+60+60-1,y + 14,5,5,3,HUD.getColor(100));
//            DisplayUtils.drawShadow(x + 45+120+60+45,y + 15,30,22,15,ThemeChanger.theme == 1 ? new Color(230, 230, 230).getRGB() : new Color(17,17,18,150).getRGB());
//            Scissor.unset();
//            Scissor.pop();
            Fonts.centricbold[20].drawCenteredString(matrixStack,"CENTRIC RECODE",x + 166,y+14,-1);


        }

        //светлая тема
        if (ThemeChanger.theme == 1) {
// Тень
            DisplayUtils.drawShadow(x, y, width, height, 20, new Color(200, 200, 200, 100).getRGB()); // Светлая тень

            DisplayUtils.drawRoundedRect(x - 5, y - 5, width + 10, height + 10, new Vector4f(4, 4, 4, 4), new Color(255, 255, 255).getRGB()); // Светлый фон

// Основной фон
            DisplayUtils.drawRoundedRect(x, y, width, height, new Vector4f(4, 4, 4, 4), new Color(255, 255, 255).getRGB()); // Белый фон

            DisplayUtils.drawRoundedRect(x + 10, y + 5, width - 20, 25, 4, new Color(241, 243, 249).getRGB()); // Очень светлый фон
//            Scissor.push();
//            Scissor.setFromComponentCoordinates(x + 10,y + 5, width-20,25);
//            DisplayUtils.drawCircle(x + 45+120+60+60+15-10,y + 30,30,HUD.getColor(100));
//            DisplayUtils.drawCircle(x + 45+120+60+60+25-10,y + 20,30,HUD.getColor(100));
//            DisplayUtils.drawRoundedRect(x + 45+120+60+60-1,y + 14,5,5,3,HUD.getColor(100));
//            DisplayUtils.drawShadow(x + 45+120+60+45,y + 15,30,22,15,ThemeChanger.theme == 1 ? new Color(241, 243, 249).getRGB() : new Color(17,17,18,150).getRGB());
//            Scissor.unset();
//            Scissor.pop();
            Fonts.centricbold[20].drawCenteredString(matrixStack,"CENTRIC RECODE",x + 166,y+14,new Color(0,0,0).getRGB());

        }


    }




    void renderCategories(MatrixStack matrixStack, float x, float y, float leftPanel, int mouseX, int mouseY) {
        if (ThemeChanger.theme == 0) {
            float heightCategory = 60 / 2f;
            DisplayUtils.drawRoundedRect(x + 90, y + 240, 155, 28, 2, new Color(17, 17, 18).getRGB());
            for (Category cat : Category.values()) {
                if (cat == current) {
                    DisplayUtils.drawRoundedRect(x + 101 + cat.ordinal() * heightCategory, y + 266, 15, 2, new Vector4f(2, 0, 2, 0), HUD.getColor(100));

                }

            }
            for (Category t : Category.values()) {

                DisplayUtils.drawRoundedRect(x + 95 + t.ordinal() * heightCategory, y + 245, Fonts.iconsall[30].getWidth(t.icon) + 10, 17, 2, new Color(22, 23, 25).getRGB());

                Fonts.iconsall[20].drawString(matrixStack, t.icon, x + 103 + t.ordinal() * heightCategory, y + 251, t == current ? HUD.getColor(100) : new Color(62, 64, 76).getRGB());
            }
            DisplayUtils.drawRoundedRect(x + 15 + 22 + 5 + 155 + 58, y + 245, 2, 20, 1, new Color(19, 19, 19, 255).getRGB());
        }
        if (ThemeChanger.theme == 1) {
            float heightCategory = 60 / 2f;

            // Светлый фон для категории
            DisplayUtils.drawRoundedRect(x + 90, y + 240, 155, 28, 2, new Color(241, 243, 249).getRGB());

            for (Category cat : Category.values()) {
                if (cat == current) {
                    // Цвет для активной категории
                    DisplayUtils.drawRoundedRect(x + 101 + cat.ordinal() * heightCategory, y + 266, 15, 2, new Vector4f(2, 0, 2, 0), HUD.getColor(100));
                }
            }

            for (Category t : Category.values()) {
                // Светлый фон для каждой категории
                DisplayUtils.drawRoundedRect(x + 95 + t.ordinal() * heightCategory, y + 245, Fonts.iconsall[30].getWidth(t.icon) + 10, 17, 2, new Color(223, 229, 243).getRGB());

                // Цвет текста для иконки категории
                Fonts.iconsall[20].drawString(matrixStack, t.icon, x + 103 + t.ordinal() * heightCategory, y + 251, t == current ? HUD.getColor(100) : new Color(50, 50, 50).getRGB());
            }

            // Светлая линия
            DisplayUtils.drawRoundedRect(x + 15 + 22 + 5 + 155 + 58, y + 245, 2, 20, 1, new Color(242, 244, 250).getRGB());
        }
    }

    void renderButtonDir(MatrixStack matrixStack, float x, float y, float width, float height, float leftPanel, int mouseX, int mouseY) {
        if (ThemeChanger.theme == 0) {
            DisplayUtils.drawRoundedRect(x + 90 + 175, y + 240, 60, 28, 2, new Color(17, 17, 18).getRGB());
            DisplayUtils.drawRoundedRect(x + 90 + 165 + 15, y + 245, 22, 17, 2, new Color(22, 23, 25).getRGB());
            DisplayUtils.drawRoundedRect(x + 90 + 165 + 15 + 22 + 5, y + 245, 22, 17, 2, new Color(22, 23, 25).getRGB());
            Fonts.iconsall[20].drawString(matrixStack, "O", x + 90 + 165 + 22, y + 251, MathUtil.isInRegion(mouseX, mouseY, x + 90 + 155 + 22, y + 251, 22, 17) ? HUD.getColor(100) : new Color(62, 64, 76).getRGB());
            Fonts.iconsall[20].drawString(matrixStack, "X", x + 90 + 165 + 22 + 22 + 5, y + 251, MathUtil.isInRegion(mouseX, mouseY, x + 90 + 155 + 22 + 22 + 5, y + 251, 22, 17) ? HUD.getColor(100) : new Color(62, 64, 76).getRGB());

        }
        if (ThemeChanger.theme == 1) {
            DisplayUtils.drawRoundedRect(x + 90 + 175, y + 240, 60, 28, 2, new Color(241, 243, 249).getRGB());
            DisplayUtils.drawRoundedRect(x + 90 + 165 + 15, y + 245, 22, 17, 2, new Color(223, 229, 243).getRGB());
            DisplayUtils.drawRoundedRect(x + 90 + 165 + 15 + 22 + 5, y + 245, 22, 17, 2, new Color(223, 229, 243).getRGB());

            Fonts.iconsall[20].drawString(matrixStack, "O", x + 90 + 165 + 22, y + 251,
                    MathUtil.isInRegion(mouseX, mouseY, x + 90 + 155 + 22, y + 251, 22, 17) ? HUD.getColor(100) : new Color(100, 100, 100).getRGB());
            Fonts.iconsall[20].drawString(matrixStack, "X", x + 90 + 165 + 22 + 22 + 5, y + 251,
                    MathUtil.isInRegion(mouseX, mouseY, x + 90 + 155 + 22 + 22 + 5, y + 251, 22, 17) ? HUD.getColor(100) : new Color(100, 100, 100).getRGB());

        }
    }

    void renderComponents(MatrixStack matrixStack, float x, float y, float width, float height, float leftPanel, int mouseX, int mouseY) {
        Scissor.push();
        Scissor.setFromComponentCoordinates(x, y + 70 / 2f, width, height - 150 / 2f);
        drawComponents(matrixStack, mouseX, mouseY);
        DisplayUtils.drawRoundedRect(x+ 15, y + 40 / 2f, width, height - 90 / 2f, new Vector4f(0, 0, 6, 6), ColorUtils.setAlpha(ThemeChanger.theme == 0 ? new Color(10, 10, 10).getRGB() : new Color(255, 255, 255).getRGB(), (int) (255 * animation)));
        Scissor.unset();
        Scissor.pop();
    }

    public CopyOnWriteArrayList<ConfigComponent> cfg = new CopyOnWriteArrayList<>();

    private String configName = "";
    private boolean configTyping;
    public static String confign;

    void drawComponents(MatrixStack stack, int mouseX, int mouseY) {

        List<ModuleComponent> moduleComponentList = objects.stream()
                .filter(moduleObject -> {
                    if (!searchText.isEmpty()) {
                        return true;
                    } else {
                        return moduleObject.function.getCategory() == current;
                    }
                }).toList();

        List<ModuleComponent> first = moduleComponentList
                .stream()
                .filter(moduleObject -> objects.indexOf(moduleObject) % 2 == 0)
                .toList();

        List<ModuleComponent> second = moduleComponentList
                .stream()
                .filter(moduleObject -> objects.indexOf(moduleObject) % 2 != 0)
                .toList();


        float scale = 2f;
        animateScroll = MathUtil.lerp(animateScroll, scroll, 10);
        float height = 550 / scale;
//        if (current == Category.Configurations || current == Category.Theme) {
//
//            DisplayUtils.drawRoundedRect(x + leftPanel + 10, y + 64 / 2F + 10, width - leftPanel - 20, height - 64 / 2F - 20, 5, new Color(17, 18, 21).getRGB());
//            if (current == Category.Configurations) {
//                DisplayUtils.drawRoundedRect(x + leftPanel + 15, y + 64 / 2F + 15, width - leftPanel - 35 - 35 * 2 + 3, 32 / 2f, 4, new Color(22, 24, 28).getRGB());
//
//                DisplayUtils.drawRoundedRect(x + width - 45 - 2, y + 64 / 2F + 15, 35 - 2, 32 / 2f, 4, new Color(22, 24, 28).getRGB());
//                Fonts.roboto[14].drawCenteredString(stack, "Create", x + width - 45 - 2 + (35 - 2) / 2f, y + 64 / 2F + 21.5F, -1);
//                DisplayUtils.drawRoundedRect(x + width - 45 - 35 - 2, y + 64 / 2F + 15, 35 - 2, 32 / 2f, 4, new Color(22, 24, 28).getRGB());
//                Fonts.roboto[14].drawCenteredString(stack, "Reload", x + width - 45 - 35 - 2 + (35 - 2) / 2f, y + 64 / 2F + 21.5F, -1);
//                float fontTextWidth = Fonts.roboto[16].getWidth(configName);
//
//                float xOffset = 0;
//
//                if (fontTextWidth > width - leftPanel - 35 - 35 * 2 ) {
//                    // Вычисляем xOffset
//                    xOffset = fontTextWidth - (width - leftPanel - 35 - 35 * 2 -8);
//                }
//
//                Stencil.initStencilToWrite();
//                DisplayUtils.drawRectW(x + leftPanel + 17, y + 64 / 2F + 15, width - leftPanel - 35 - 35 * 2, 32 / 2f,-1);
//                Stencil.readStencilBuffer(1);
//                Fonts.roboto[16].drawString(stack, configName + (configTyping ? System.currentTimeMillis() % 1000 > 500 ? "_" : "" : ""), x + leftPanel + 18 - xOffset, y + 64 / 2F + 20, -1);
//                Stencil.uninitStencilBuffer();
//                config = cfg;
//            }
//            Scissor.push();
//            Scissor.setFromComponentCoordinates(x + leftPanel + 10, (float) (yPanel + (64 / 2f) + 35), width - leftPanel - 20, height - 64 / 2F - 45);
//            float offset = (float) (yPanel + (64 / 2f) + 8) + animateScroll;
//            for (ConfigComponent component : config) {
//                if (current != Category.Configurations) continue;
//                component.parent = this;
//                component.selected = component == selectedCfg;
//                component.setPosition((float) (xPanel + (100f + 12)), offset + 29, 314 + 12, 20);
//                component.drawComponent(stack, mouseX, mouseY);
//                offset += component.height + 2;
//            }
//            Scissor.unset();
//            Scissor.pop();
//
//            Scissor.push();
//            Scissor.setFromComponentCoordinates(x + leftPanel + 10, (float) (yPanel + (64 / 2f)) + 10, width - leftPanel - 20, height - 64 / 2F - 20);
//            float offset2 = (float) (yPanel + (64 / 2f) - 12) + animateScroll;
//            for (ThemeComponent component : theme) {
//                if (current != Category.Theme) continue;
//                component.parent = this;
//                component.setPosition((float) (xPanel + (100f + 12)), offset2 + 29, 314 + 12, 20);
//                component.drawComponent(stack, mouseX, mouseY);
//                offset2 += component.height + 2;
//            }
//            Scissor.unset();
//            Scissor.pop();
//
//
//            scroll = Math.min(scroll, 0);
//
//
//        }


        float offset = (float) (yPanel + 40) + animateScroll;
        float size1 = 0;
        for (ModuleComponent component : first) {
            if (searchText.isEmpty()) {
                if (component.function.getCategory() != current) continue;
            } else {
                if (!component.function.getName().toLowerCase().contains(searchText.toLowerCase())) continue;
            }
            component.parent = this;
            component.setPosition((float) (xPanel + (15)), offset, 294 / 2f, 37);
            component.drawComponent(stack, mouseX, mouseY);
            if (!component.components.isEmpty()) {
                for (Component settingComp : component.components) {
                    if (settingComp.setting != null && settingComp.setting.visible.get()) {
                        offset += settingComp.height;
                        size1 += settingComp.height;
                    }
                }
            }
            offset += component.height - 10;
            size1 += component.height - 10;
        }

        float offset2 = (float) (yPanel + 40) + animateScroll;
        float size2 = 0;
        for (ModuleComponent component : second) {
            if (searchText.isEmpty()) {
                if (component.function.getCategory() != current) continue;
            } else {
                if (!component.function.getName().toLowerCase().contains(searchText.toLowerCase())) continue;
            }
            component.parent = this;
            component.setPosition((float) (xPanel + (5) + 314 / 2f + 10), offset2, 294 / 2f, 37);
            component.drawComponent(stack, mouseX, mouseY);
            if (!component.components.isEmpty()) {
                for (Component settingComp : component.components) {
                    if (settingComp.setting != null && settingComp.setting.visible.get()) {
                        offset2 += settingComp.height;
                        size2 += settingComp.height;
                    }
                }
            }
            offset2 += component.height - 10;
            size2 += component.height - 10;
        }


        float max = Math.max(size1, size2);
        if (max < height) {
            scroll = 0;
        } else {
            scroll = MathHelper.clamp(scroll, -(max - height + 80),0);
        }
    }

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);
        openAnimation.setDirection(Direction.FORWARDS);
        ColorComponent.opened = null;
        typing = false;
        configTyping = false;
        configOpened = false;
        configName = "";

    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {

        Vec2i fixed = ClientUtil.getMouse((int) mouseX, (int) mouseY);
        mouseX = fixed.getX();
        mouseY = fixed.getY();

        for (ModuleComponent m : objects) {
            if (searchText.isEmpty()) {
                if (m.function.getCategory() != current) continue;
            } else {
                if (!m.function.getName().toLowerCase().contains(searchText.toLowerCase())) continue;
            }
            m.mouseReleased((int) mouseX, (int) mouseY, button);
        }
//        for (ThemeComponent component : theme) {
//            if (current != Category.Theme) continue;
//            component.parent = this;
//            component.mouseReleased((int) mouseX, (int) mouseY, button);
//        }
        if (ColorComponent.opened != null) {
            ColorComponent.opened.unclick((int) mouseX, (int) mouseY);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            openAnimation.setDirection(Direction.BACKWARDS);
            return false;
        }
        boolean ctrlDown = Screen.hasControlDown();
        if (typing) {
//            if (!(current == Category.Configurations || current == Category.Theme)) {
//                if (ctrlDown && keyCode == GLFW.GLFW_KEY_V) {
//                    String pasteText = GLFW.glfwGetClipboardString(Minecraft.getInstance().getMainWindow().getHandle());
//                    searchText += pasteText;
//                }
//                if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
//                    if (!searchText.isEmpty()) {
//                        searchText = searchText.substring(0, searchText.length() - 1);
//                    }
//                }
//                if (keyCode == GLFW.GLFW_KEY_DELETE) {
//                    searchText = "";
//                }
//                if (keyCode == GLFW.GLFW_KEY_ENTER) {
//                    typing = false;
//                }
//            }
        }

        for (ModuleComponent m : objects) {
            if (searchText.isEmpty()) {
                if (m.function.getCategory() != current) continue;
            } else {
                if (!m.function.getName().toLowerCase().contains(searchText.toLowerCase())) continue;
            }
            m.keyTyped(keyCode, scanCode, modifiers);
        }

        if (binding != null) {
            if (keyCode == GLFW.GLFW_KEY_DELETE) {
                binding.function.setBind(0);
            } else {
                binding.function.setBind(keyCode);
            }
            binding = null;
        }

        if (configTyping) {
            if (ctrlDown && keyCode == GLFW.GLFW_KEY_V) {
                String pasteText = GLFW.glfwGetClipboardString(Minecraft.getInstance().getMainWindow().getHandle());
                configName += pasteText;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!configName.isEmpty()) {
                    configName = configName.substring(0, configName.length() - 1);
                }
            }
            if (keyCode == GLFW.GLFW_KEY_DELETE) {
                configName = "";
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                configTyping = false;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (typing)
            searchText += codePoint;
        if (configTyping)
            configName += codePoint;

        for (ModuleComponent m : objects) {
            if (searchText.isEmpty()) {
                if (m.function.getCategory() != current) continue;
            } else {
                if (!m.function.getName().toLowerCase().contains(searchText.toLowerCase())) continue;
            }
            m.charTyped(codePoint, modifiers);
        }
        return super.charTyped(codePoint, modifiers);
    }

    private boolean configOpened;

    private ConfigComponent selectedCfg;

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        Vec2i fixed = ClientUtil.getMouse((int) mouseX, (int) mouseY);

        mouseX = fixed.getX();
        mouseY = fixed.getY();

        float scale = 2f;
        float width = 800 / scale;
        float height = 550 / scale;
        float leftPanel = 150 / scale;
        float x = MathUtil.calculateXPosition(mc.getMainWindow().scaledWidth() / 2f, width);
        float y = MathUtil.calculateXPosition(mc.getMainWindow().scaledHeight() / 2f, height);
        float heightCategory = 60 / 2f;
        if (MathUtil.isInRegion((float) mouseX, (float) mouseY, x + 40,y + 241, 25,25)) {
            ThemeChanger.theme = 1;
            ThemeSwitcher.setTheme(false);
        }
        if (MathUtil.isInRegion((float) mouseX, (float) mouseY, x + 40+22+5,y + 241, 25,25)) {
            ThemeChanger.theme = 0;
            ThemeSwitcher.setTheme(true);
        }
//        if (MathUtil.isInRegion((float) mouseX, (float) mouseY, mc.getMainWindow().getScaledWidth()/1.12f + 65,mc.getMainWindow().getScaledHeight()/1.08f + 2.5f, 25,25)) {
//            ThemeChanger.theme =2;
//        }
        this.themeDrawing.click((int) mouseX, (int) mouseY, button);
        if (MathUtil.isInRegion((float) mouseX, (float) mouseY,x + 90 + 155 + 22,y + 251,22,17)) {
           Util.getOSType().openURI("https://t.me/centricclient");
        }
        if (MathUtil.isInRegion((float) mouseX, (float) mouseY,x + 90 + 155 + 22 + 22 + 5,y + 251,22,17)) {
            try {
                Runtime.getRuntime().exec("explorer " + Main.getInstance().getConfigStorage().CONFIG_DIR);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
            if (ColorComponent.opened != null) {
            if (!ColorComponent.opened.click((int) mouseX, (int) mouseY))
                return super.mouseClicked(mouseX, mouseY, button);
        }
        //click category
        for (Category t : Category.values()) {
            if (MathUtil.isInRegion((float) mouseX, (float) mouseY, x + 123 + t.ordinal() * heightCategory, y + 251, leftPanel, heightCategory)) {
                if (current == t) continue;
                current = t;
                animation = 1;
                scroll = 0;
                searchText = "";
                ColorComponent.opened = null;
                typing = false;
            }
        }

        if (MathUtil.isInRegion((float) mouseX, (float) mouseY, x + width - 45 - 2, y + 64 / 2F + 15, 35 - 2, 32 / 2f)) {
            Main.getInstance().getConfigStorage().saveConfiguration(configName);
            configName = "";
            configTyping = false;
            cfg.clear();
            for (String config : Main.getInstance().getConfigStorage().getConfigsByName()) {
                cfg.add(new ConfigComponent(Main.getInstance().getConfigStorage().findConfig(config)));
            }
        }
        if (MathUtil.isInRegion((float) mouseX, (float) mouseY, x + width - 45 - 35 - 2, y + 64 / 2F + 15, 35 - 2, 32 / 2f)) {
            cfg.clear();
            for (String config : Main.getInstance().getConfigStorage().getConfigsByName()) {
                cfg.add(new ConfigComponent(Main.getInstance().getConfigStorage().findConfig(config)));
            }
        }

        if (MathUtil.isInRegion((float) mouseX, (float) mouseY, x, y, width, height - 64 / 2f)) {
            for (ModuleComponent m : objects) {
                if (searchText.isEmpty()) {
                    if (m.function.getCategory() != current) continue;
                } else {
                    if (!m.function.getName().toLowerCase().contains(searchText.toLowerCase())) continue;
                }
                m.mouseClicked((int) mouseX, (int) mouseY, button);
            }
        }



        return super.mouseClicked(mouseX, mouseY, button);
    }

    public static void sizeAnimation(double width, double height, double scale) {
        GlStateManager.translated(width, height, 0);
        GlStateManager.scaled(scale, scale, scale);
        GlStateManager.translated(-width, -height, 0);
    }
}
