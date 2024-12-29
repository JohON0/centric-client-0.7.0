package centric.pl.ui.clickgui;

import centric.pl.functions.api.Category;
import centric.pl.functions.impl.render.ClickGUI;
import centric.pl.johon0.utils.animations.GifUtil;
import centric.pl.johon0.utils.render.*;
import centric.pl.managers.styleManager.ThemeSwitcher;
import centric.pl.johon0.utils.client.ClientUtil;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.client.Vec2i;
import centric.pl.johon0.utils.font.Fonts;
import centric.pl.johon0.utils.math.MathUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import centric.pl.Main;
import centric.pl.ui.clickgui.stylecomponents.StyleObject;
import centric.pl.johon0.utils.CustomFramebuffer;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector4f;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainScreen extends Screen implements IMinecraft {
    private float scroll3;
    private float animatedScrool3;
    private final List<PanelRender> panels = new ArrayList<>();
    @Getter
    private static Animation animation = new Animation();
    private float animation4;
    private StyleObject themeDrawing = new StyleObject();
    private static final Animation gradientAnimation = new Animation();
    private float glebxmanpaster = 0;
    private float animation2;
    private float animation3;

    public static int count = 1;
    private final int maxCount = 5;
    private final int minCount = 1;

    public MainScreen(ITextComponent titleIn) {
        super(titleIn);
        for (Category category : Category.values()) {
            panels.add(new PanelRender(category));
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        animation = animation.animate(1, 0.5f, Easings.EXPO_OUT);
        super.init();
    }
    public static float scale = 1.0f;

    @Override
    public void closeScreen() {

        super.closeScreen();
        GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        // TODO Auto-generated method stub
        Vec2i fixMouse = adjustMouseCoordinates((int) mouseX, (int) mouseY);

        Vec2i fix = ClientUtil.getMouse(fixMouse.getX(), fixMouse.getY());
        mouseX = fix.getX();
        mouseY = fix.getY();
        this.themeDrawing.scroll(mouseX, mouseY, delta);
        if (MathUtil.isInRegion((float)mouseX, (float)mouseY, (float)IMinecraft.mc.getMainWindow().getWindowX(), mc.getMainWindow().getWindowY(),
                mc.getMainWindow().getScaledWidth()-150,mc.getMainWindow().getScaledHeight())) {
            this.scroll3 += (float)(delta * 16.0);
        }
        for (PanelRender panel : panels) {
            if (MathUtil.isInRegion((float) mouseX, (float) mouseY, panel.getX(), panel.getY(), panel.getWidth(),
                    panel.getCurrentHeight())) {
                panel.setScroll((float) (panel.getScroll() + (delta * 20)));
            }
        }

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        for (PanelRender panel : panels) {
            panel.charTyped(codePoint, modifiers);
        }
        return super.charTyped(codePoint, modifiers);
    }


    @Override
    public void tick() {
        super.tick();
        final KeyBinding[] pressedKeys = {mc.gameSettings.keyBindForward, mc.gameSettings.keyBindBack,
                mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindJump,
                mc.gameSettings.keyBindSprint};

        for (KeyBinding keyBinding : pressedKeys) {
            boolean isKeyPressed = InputMappings.isKeyDown(mc.getMainWindow().getHandle(), keyBinding.getDefault().getKeyCode());
            keyBinding.setPressed(isKeyPressed);
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.animation2 = MathUtil.fast(this.animation2, ThemeSwitcher.themelightofdark ? 1.0f : 0.0f, 12.0f);
        this.animation3 = MathUtil.fast(this.animation3, !ThemeSwitcher.themelightofdark ? 1.0f : 0.0f, 10.0f);
        if (Main.getInstance().getFunctionRegistry().getClickGUI().blur.get()) {
            KawaseBlur.blur.updateBlur(3, 3);
        } else {
        }

        mc.gameRenderer.setupOverlayRendering(2);
        animation.update();
        this.animatedScrool3 = MathUtil.fast(this.animatedScrool3, this.scroll3, 15.0f);
        if (animation.getValue() < 0.1) {
            closeScreen();
        }
        final float off = 10;
        float width = panels.size() * (135 + off);

        updateScaleBasedOnScreenWidth();

        int windowWidth = ClientUtil.calc(mc.getMainWindow().getScaledWidth());
        int windowHeight = ClientUtil.calc(mc.getMainWindow().getScaledHeight());

        Vec2i fixMouse = adjustMouseCoordinates(mouseX, mouseY);

        Vec2i fix = ClientUtil.getMouse(fixMouse.getX(), fixMouse.getY());
        mouseX = fix.getX();
        mouseY = fix.getY();

        GlStateManager.pushMatrix();
        GlStateManager.translatef(windowWidth / 2f, windowHeight / 2f, 0);
        GlStateManager.scaled(animation.getValue(), animation.getValue(), 1);
        GlStateManager.scaled(scale, scale, 1);
        GlStateManager.translatef(-windowWidth / 2f, -windowHeight / 2f, 0);

        for (PanelRender panel : panels) {
//            DisplayUtils.drawRoundedRect(panel.getX(), panel.getY(), panel.getWidth(),
//                    panel.getCurrentHeight() - 2,
//                    new Vector4f(7, 7, 7, 7), -1);
        }

        GlStateManager.popMatrix();
        Stencil.readStencilBuffer(1);
        if (Main.getInstance().getFunctionRegistry().getClickGUI().blur.get()) {
            GlStateManager.bindTexture(KawaseBlur.blur.BLURRED.framebufferTexture);
            CustomFramebuffer.drawTexture();
        } else {
        }

        Stencil.uninitStencilBuffer();
        if (Main.getInstance().getFunctionRegistry().getHud().themeopen.get()) {
            DisplayUtils.drawRoundedRect(mc.getMainWindow().getScaledWidth() - 137, 6.0f, 136.0f, 166.0f, 3.0f, ThemeSwitcher.bgcolor);
            DisplayUtils.drawRoundedRect(mc.getMainWindow().getScaledWidth() - 132, 11.0f, 126.0f, 156.0f, 3.0f, ThemeSwitcher.backgroundcolor);
        Fonts.centricbold[21].drawCenteredString(matrixStack, "Themes", (double) ((float) mc.getMainWindow().getScaledWidth() - 132.5f + 63.0f), 16.0, ThemeSwitcher.textcolor);
        this.themeDrawing.draw(matrixStack, mouseX, mouseY, mc.getMainWindow().getScaledWidth() - 133, 26.0f, 130.0f, 143.0f);
        }
        Fonts.iconsall[100].drawString(matrixStack, "T",mc.getMainWindow().getScaledWidth() - 50, mc.getMainWindow().getScaledHeight() - 50, ColorUtils.rgba(0, 0, 0,  (int)(255.0f * this.animation2)));
        if (!ThemeSwitcher.themelightofdark) {
            Fonts.iconsall[100].drawString(matrixStack, "S",mc.getMainWindow().getScaledWidth() - 50, mc.getMainWindow().getScaledHeight() - 50, ColorUtils.rgba(255, 255, 255, (int)(255.0f * this.animation3)));
        }
        ClickGUI clickGUI = Main.getInstance().getFunctionRegistry().getClickGUI();
        animation4 = MathUtil.fast(animation4,MathUtil.isInRegion(mouseX,mouseY,mc.getMainWindow().getScaledWidth() - 53, 172.0f,Fonts.notoitalic[12].getWidth("Изменить фон") + 5, 12) ? 0 : 1, 10);
        if (clickGUI.wooman.get()) {
            DisplayUtils.drawRoundedRect(mc.getMainWindow().getScaledWidth() - 55, 173.0f, Fonts.notoitalic[12].getWidth("Изменить фон") + 5, 12,2, ThemeSwitcher.backgroundcolor);
            Fonts.notoitalic[12].drawString(matrixStack, "Изменить фон",mc.getMainWindow().getScaledWidth() - 53, 178.0f,
                    MathUtil.isInRegion(mouseX,mouseY,mc.getMainWindow().getScaledWidth() - 53, 172.0f,Fonts.notoitalic[12].getWidth("Изменить фон") + 5, 12) ? ThemeSwitcher.textcolor : ThemeSwitcher.textcolorcom);
        }
//        if (clickGUI.images.get()) {
//            GifUtil gifUtils = new GifUtil();
//            String image = clickGUI.imageType.get().toLowerCase();
//            String path = "centric/images/gui/";
//            int totalFrames = 0;
//            int frameDelay = 0;
//            boolean fromZero = false;
//
//            if (clickGUI.imageType.is("Bottle")) {
//                totalFrames = 30;
//                frameDelay = 30;
//            }
//
//            if (Arrays.asList("Bottle").contains(clickGUI.imageType.get())) {
//                int i = gifUtils.getFrame(totalFrames, frameDelay, fromZero);
//                path = "centric/gifs/" + clickGUI.imageType.get().toLowerCase() + "/frame_" + i;
//                image = "";
//            }
//
//            int size = (int) (512f / 2);
//            float x1 = (float) (windowWidth - size);
//            float x2 = windowWidth;
//            float y1 = windowHeight - size;
//            float y2 = windowHeight;
//            DisplayUtils.drawImage(new ResourceLocation(path + image + ".png"), x1 - 50, y1, x2 - x1, y2 - y1, ColorUtils.reAlphaInt(-1, 255));
//        }


        GlStateManager.pushMatrix();
        GlStateManager.translatef(windowWidth / 2f, windowHeight / 2f, 0);
        GlStateManager.scaled(animation.getValue(), animation.getValue(), 1);
        GlStateManager.scaled(scale, scale, 1);
        GlStateManager.translatef(-windowWidth / 2f, -windowHeight / 2f, 0);

        for (PanelRender panel : this.panels) {
            panel.setY(10 + this.animatedScrool3);
            panel.setX(10 + panel.getType().ordinal() * (115 + off) + glebxmanpaster);
            float animationValue = (float) animation.getValue() * scale;


            panel.render(matrixStack, mouseX, mouseY);


        }
        GlStateManager.popMatrix();
        mc.gameRenderer.setupOverlayRendering();


    }

    private void updateScaleBasedOnScreenWidth() {
        final float PANEL_WIDTH = 135;
        final float MARGIN = 10;
        final float MIN_SCALE = 0.5f;

        float totalPanelWidth = panels.size() * (PANEL_WIDTH + MARGIN);
        float screenWidth = mc.getMainWindow().getScaledWidth();

        if (totalPanelWidth >= screenWidth) {
            scale = screenWidth / totalPanelWidth;
            scale = MathHelper.clamp(scale, MIN_SCALE, 1.0f);
        } else {
            scale = 1f;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (PanelRender panel : panels) {
            panel.keyPressed(keyCode, scanCode, modifiers);
        }
        // TODO Auto-generated method stub
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            animation = animation.animate(0, 0.5f, Easings.EXPO_OUT);
            return false;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            glebxmanpaster += 5;
        }
        if (keyCode == GLFW.GLFW_KEY_LEFT) {
            glebxmanpaster -= 5;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private Vec2i adjustMouseCoordinates(int mouseX, int mouseY) {
        int windowWidth = mc.getMainWindow().getScaledWidth();
        int windowHeight = mc.getMainWindow().getScaledHeight();

        float adjustedMouseX = (mouseX - windowWidth / 2f) / scale + windowWidth / 2f;
        float adjustedMouseY = (mouseY - windowHeight / 2f) / scale + windowHeight / 2f;

        return new Vec2i((int) adjustedMouseX, (int) adjustedMouseY);
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vec2i fixMouse = adjustMouseCoordinates((int) mouseX, (int) mouseY);

        Vec2i fix = ClientUtil.getMouse(fixMouse.getX(), fixMouse.getY());
        mouseX = fix.getX();
        mouseY = fix.getY();
        this.themeDrawing.click((int)mouseX, (int)mouseY, button);
        for (PanelRender panel : panels) {
            panel.mouseClick((float) mouseX, (float) mouseY, button);
        }
        if (MathUtil.isInRegion((float) mouseX, (float) mouseY,
                mc.getMainWindow().getScaledWidth() - 53,
                172.0f,
                Fonts.notoitalic[12].getWidth("Изменить фон") + 5, 12)) {
            if (count < maxCount) {
                count++;
            } else {
                count = minCount;
            }
        }
        if (MathUtil.isInRegion((float) mouseX, (float) mouseY,
                mc.getMainWindow().getScaledWidth() - 50,
                mc.getMainWindow().getScaledHeight() - 50,
                50, 50)) {

            ThemeSwitcher.setTheme(!ThemeSwitcher.themelightofdark);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // TODO Auto-generated method stub
        Vec2i fixMouse = adjustMouseCoordinates((int) mouseX, (int) mouseY);

        Vec2i fix = ClientUtil.getMouse(fixMouse.getX(), fixMouse.getY());
        mouseX = fix.getX();
        mouseY = fix.getY();
        for (PanelRender panel : panels) {
            panel.mouseRelease((float) mouseX, (float) mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

}
