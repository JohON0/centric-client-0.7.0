package centric.pl.ui.clickgui;

import java.util.ArrayList;
import java.util.List;

import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.impl.render.ClickGUI;
import centric.pl.johon0.utils.render.ColorUtils;
import centric.pl.johon0.utils.render.StencilUtil;
import centric.pl.ui.clickgui.components.ModuleComponent;
import centric.pl.ui.clickgui.impl.Component;
import centric.pl.ui.clickgui.impl.IBuilder;
import centric.pl.managers.styleManager.ThemeSwitcher;
import centric.pl.johon0.utils.font.Fonts;
import centric.pl.johon0.utils.math.MathUtil;
import centric.pl.johon0.utils.render.DisplayUtils;
import com.mojang.blaze3d.matrix.MatrixStack;

import centric.pl.Main;
import net.minecraft.util.ResourceLocation;


public class PanelRender
        implements IBuilder {
    Category category;
    protected float x;
    protected float y;
    protected final float width = 120.0f;
    private List<ModuleComponent> modules = new ArrayList<ModuleComponent>();
    private float scroll;
    private float animatedScrool;
    float max = 0.0f;
    private float targetHeight;
    private float currentHeight;
    public PanelRender(Category category) {
        this.category = category;
        for (Function function : Main.getInstance().getFunctionRegistry().getFunctions()) {
            if (function.getCategory() != category) continue;
            ModuleComponent component = new ModuleComponent(function);
            component.setPanel(this);
            this.modules.add(component);
        }
        updateHeight();
        currentHeight = targetHeight;
    }


    private void updateHeight() {
        float headerHeight = 50 / 2f;
        float textHeight = 10;

        if (modules.isEmpty()) {
            targetHeight = textHeight;
        } else {
            targetHeight = headerHeight;
            for (ModuleComponent component : modules) {
                targetHeight += 20;
                if (component.animation.getValue() > 0) {
                    for (Component subComponent : component.getComponents()) {
                        if (subComponent.isVisible()) {
                            targetHeight += subComponent.getHeight();
                        }
                    }
                }
            }
        }
    }

    public float getCurrentHeight () {
        return this.currentHeight;
    }
    int woomantype;
    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        updateHeight();
        currentHeight = MathUtil.lerp(currentHeight, targetHeight, 10f);
        float header = 48 / 2f;
        ClickGUI clickGUI = Main.getInstance().getFunctionRegistry().getClickGUI();
            if (Main.getInstance().getFunctionRegistry().getClickGUI().wooman.get()) {
            DisplayUtils.drawShadow(this.x-2.5f, this.y-2.5f, 115.0f, currentHeight + 12-5, 3, ThemeSwitcher.bgcolor);
            DisplayUtils.drawRoundedRect(this.x-2.5f, this.y-2.5f, 115.0f, currentHeight + 12-5, 3.0f, ThemeSwitcher.bgcolor);
            StencilUtil.initStencilToWrite();
            DisplayUtils.drawRoundedRect(this.x, this.y, 110.0f, currentHeight + 2, 4.0f, -1);
            StencilUtil.readStencilBuffer(5);
            DisplayUtils.drawImage(new ResourceLocation("centric/images/wooman/wooman" + MainScreen.count + ".png"),this.x, this.y, 110.0f, currentHeight + 2, -1);
            StencilUtil.uninitStencilBuffer();
            DisplayUtils.drawRoundedRect(this.x-0.3f, this.y-0.3f, 110.4f, currentHeight + 2.4f, 4.0f, ColorUtils.setAlpha(ThemeSwitcher.backgroundcolor, 150));

        } else {

            DisplayUtils.drawShadow(this.x-2.5f, this.y-2.5f, 115.0f, currentHeight + 12-5, 3, ThemeSwitcher.bgcolor);
            DisplayUtils.drawRoundedRect(this.x-2.5f, this.y-2.5f, 115.0f, currentHeight + 12-5, 3.0f, ThemeSwitcher.bgcolor);
            DisplayUtils.drawRoundedRect(this.x, this.y, 110.0f, currentHeight + 2, 3.0f, ThemeSwitcher.backgroundcolor);
        }

        Fonts.centricbold[21].drawCenteredString(stack, category.name(), x + width - 66, y + header / 2f - 7 / 2f, ThemeSwitcher.textcolor);
        if (!modules.isEmpty()) {
            drawComponents(stack, mouseX, mouseY);
        }

    }

    private void drawComponents(MatrixStack stack, float mouseX, float mouseY) {
        float header = 50 / 2f;
        float offset = 0;

        for (ModuleComponent component : modules) {
            component.setX(getX() + 2);
            component.setY(getY() + header + offset + 0);
            component.setWidth(getWidth() - 2);
            component.setHeight(18);
            component.animation.update();
            if (component.animation.getValue() > 0) {
                float componentOffset = 0;
                for (Component component2 : component.getComponents()) {
                    if (component2.isVisible()) {
                        componentOffset += component2.getHeight();
                    }
                }
                componentOffset *= component.animation.getValue();
                component.setHeight(component.getHeight() + componentOffset);
            }
            component.render(stack, mouseX, mouseY);
            offset += component.getHeight() + 2;
        }
    }

    @Override
    public void mouseRelease(float mouseX, float mouseY, int button) {
        for (ModuleComponent component : modules) {
            component.mouseRelease(mouseX, mouseY, button);
        }
    }
    @Override
    public void mouseClick(float mouseX, float mouseY, int button) {
        for (ModuleComponent component : modules) {
            component.mouseClick(mouseX, mouseY, button);
        }
    }

    @Override
    public void keyPressed(int key, int scanCode, int modifiers) {
        for (ModuleComponent component : modules) {
            component.keyPressed(key, scanCode, modifiers);
        }
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
        for (ModuleComponent component : modules) {
            component.charTyped(codePoint, modifiers);
        }
    }



    public Category getType() {
        return this.category;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getWidth() {
        return this.width;
    }

    public List<ModuleComponent> getModules() {
        return this.modules;
    }

    public float getScroll() {
        return this.scroll;
    }

    public float getAnimatedScrool() {
        return this.animatedScrool;
    }

    public float getMax() {
        return this.max;
    }

    public void setType(Category category) {
        this.category = category;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setModules(List<ModuleComponent> modules) {
        this.modules = modules;
    }

    public void setScroll(float scroll) {
        this.scroll = scroll;
    }

    public void setAnimatedScrool(float animatedScrool) {
        this.animatedScrool = animatedScrool;
    }

    public void setMax(float max2) {
        this.max = max2;
    }
}

