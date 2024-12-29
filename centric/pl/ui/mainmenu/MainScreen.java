package centric.pl.ui.mainmenu;

import centric.pl.johon0.utils.animations.GifUtil;
import centric.pl.johon0.utils.discordrpc.DiscordRichPresenceUtil;
import centric.pl.managers.notificationManager.Notification;
import centric.pl.managers.styleManager.ThemeSwitcher;
import centric.pl.johon0.utils.client.ClientUtil;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.client.Vec2i;
import centric.pl.johon0.utils.font.Fonts;
import centric.pl.johon0.utils.math.MathUtil;
import centric.pl.johon0.utils.render.ColorUtils;
import centric.pl.johon0.utils.render.DisplayUtils;
import centric.pl.johon0.utils.render.KawaseBlur;
import centric.pl.johon0.utils.render.Stencil;
import centric.pl.ui.mainmenu.altmanager.Alt;
import centric.pl.ui.mainmenu.altmanager.AltConfig;
import centric.pl.ui.mainmenu.altmanager.AltWidget;
import com.mojang.blaze3d.matrix.MatrixStack;
import centric.pl.Main;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("all")
public class MainScreen extends Screen implements IMinecraft {
    private float animation2;
    private float animation3;
    private String altName1 = "";
    public boolean open;
    private boolean typing;
    private float animationtext;
    private float changeloganim;
    private float animatetext;
    public float altManageranim;
    public static float widthButton = 215 / 2f;


    public MainScreen() {
        super(ITextComponent.getTextComponentOrEmpty(""));

    }
    private static boolean changelogopen = false;
    public static boolean altmanageropen = true;
    
     private final List<Button> buttons = new ArrayList<>();

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);
        this.animation2 = MathUtil.fast(this.animation2, ThemeSwitcher.themelightofdark ? 1.0f : 0.0f, 12.0f);
        this.animation3 = MathUtil.fast(this.animation3, !ThemeSwitcher.themelightofdark ? 1.0f : 0.0f, 10.0f);
        float widthButton = 215 / 2f;

         float x = ClientUtil.calc(width) / 2f - widthButton / 2f;
         float y = Math.round(ClientUtil.calc(height) / 2f + 1);
        buttons.clear();

        buttons.add(new Button(x, y, widthButton, 48 / 2f, "SinglePlayer", () -> {
            mc.displayGuiScreen(new WorldSelectionScreen(this));
        }));
        y += 48 / 2f + 5;
        buttons.add(new Button(x, y, widthButton, 48 / 2f, "Multiplayer", () -> {
            mc.displayGuiScreen(new MultiplayerScreen(this));
        }));
        y += 48 / 2f + 5;
        buttons.add(new Button(x, y, widthButton, 48 / 2f, "Alt Manager", () -> {
             altmanageropen = !altmanageropen;
        }));
        y += 48 / 2f + 5;
        buttons.add(new Button(x, y, widthButton, 48 / 2f, "Options", () -> {
            mc.displayGuiScreen(new OptionsScreen(this, mc.gameSettings));
        }));
        y += 48 / 2f + 5;
        buttons.add(new Button(x, y, widthButton, 48 / 2f, "Quit", mc::shutdownMinecraftApplet));

        buttons.add(new Button(mc.getMainWindow().getWindowX() + 10, mc.getMainWindow().getWindowX() + 10, widthButton, 48 / 2f, "Changelog", () -> {
            changelogopen = !changelogopen;
            System.out.println(String.valueOf(width +" " + height));
        }));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!altmanageropen) {
            Main.getInstance().getAltWidget().updateScroll((int) mouseX, (int) mouseY, (float) delta);
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        float widthButton = 215 / 2f;
        float x = ClientUtil.calc(width) / 2f - widthButton / 2f + 53;
        float y = Math.round(ClientUtil.calc(height) / 2f - 30);
        String image = "mainmenu";
        String path = "centric/mainscreen/";
            GifUtil gifUtils = new GifUtil();
            int totalFrames = 0;
            int frameDelay = 0;
            boolean fromZero = false;
                totalFrames = 50;
                frameDelay = 30;

            if (Arrays.asList("mainmenu").contains("mainmenu")) {
                int i = gifUtils.getFrame(totalFrames, frameDelay, fromZero);
                path = "centric/mainscreen/" + "frame_" + i;
                image = "";
            }

            int size = (int) (512f / 2);
            float x1 = (float) (width - size);
            float x2 = width;
            float y1 = height - size;
            float y2 = height;
            DisplayUtils.drawImage(new ResourceLocation(path + image + ".png"), 0,0, width, height, ColorUtils.reAlphaInt(-1, 255));

        drawName(matrixStack,"CENTRIC",x,y, ThemeSwitcher.textcolor);
        drawinfo(matrixStack,"dlc for minecraft 1.16.5",x,y+250, ThemeSwitcher.textcolor);

//
        drawChangelog(matrixStack);
        renderAltManager(matrixStack,mouseX,mouseY);
        Main.getInstance().getAltWidget().render(matrixStack);
        mc.gameRenderer.setupOverlayRendering(2);
        drawButtons(matrixStack, mouseX, mouseY, partialTicks);
        Main.getInstance().getNotification().draw(matrixStack);

        mc.gameRenderer.setupOverlayRendering();


    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!altmanageropen) {
            if (typing) {
                altName1 += codePoint;
            }
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean ctrlDown = GLFW.glfwGetKey(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS ||
                GLFW.glfwGetKey(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
        if (!altmanageropen) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                Minecraft.getInstance().keyboardListener.enableRepeatEvents(false);
            }
            if (typing) {
                if (ctrlDown && keyCode == GLFW.GLFW_KEY_V) {
                    try {
                        altName1 += GLFW.glfwGetClipboardString(mc.getMainWindow().getHandle());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                    if (!altName1.isEmpty()) {
                        altName1 = altName1.substring(0, altName1.length() - 1);
                    }
                }
                if (keyCode == GLFW.GLFW_KEY_ENTER) {
                    if (altName1.length() >= 3) {
                        AltWidget.alts.add(new Alt(altName1));
                        AltConfig.updateFile();
                    }
                    Minecraft.getInstance().keyboardListener.enableRepeatEvents(false);
                    typing = false;
                    System.out.println(mc.getSession().getUsername());
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vec2i fixed = ClientUtil.getMouse((int) mouseX, (int) mouseY);
        buttons.forEach(b -> b.click(fixed.getX(), fixed.getY(), button));
//        if (MathUtil.isInRegion((float) mouseX, (float) mouseY,
//                mc.getMainWindow().getScaledWidth() - 50,
//                mc.getMainWindow().getScaledHeight() - 50,
//                50, 50)) {
//
//            // Переключение темы
//            ThemeSwitcher.setTheme(!ThemeSwitcher.themelightofdark);
//        }

        float widthButton = 215 / 2f;
        float x = ClientUtil.calc(width) / 2f - widthButton / 2f + 120;
        float y = Math.round(ClientUtil.calc(height) / 2f - 25);
        float ybuttons = Math.round(ClientUtil.calc(height) / 2f + 125);
        if (!altmanageropen) {
            if (MathUtil.isInRegion((float) mouseX, (float) mouseY, x + 70, ybuttons, 110, 15)) {
                typing = !typing;
            }
            if (MathUtil.isInRegion((float) mouseX, (float) mouseY, x + 5, ybuttons, 60, 15)) {
                if (altName1.length() >= 3) {
                    Main.getInstance().getNotification().add("Аккаунт " + altName1 + " успешно создан!", "", 5, Notification.Type.success);
                    AltWidget.alts.add(new Alt(altName1));
                    AltConfig.updateFile();
                } else {
                    Main.getInstance().getNotification().add("Никнейм должен быть больше 3-х символов!", "", 5, Notification.Type.warning);

                }
                Minecraft.getInstance().keyboardListener.enableRepeatEvents(false);
                typing = false;
            }
            if (MathUtil.isInRegion((float) mouseX, (float) mouseY, x + 185, ybuttons, 60, 15)) {
                AltWidget.alts.clear();
                Main.getInstance().getNotification().add("Альт Менеджер успешно очищен!","", 5, Notification.Type.success);
            }
            Main.getInstance().getAltWidget().click(fixed.getX(), fixed.getY(), button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderAltManager(MatrixStack matrixStack,float mouseX,float mouseY) {
        int colortextbuttoncreate;
        int colortextbuttonrandom;
        altManageranim = MathUtil.lerp(altManageranim, (altmanageropen) ? 0f : 1f, 10);
        float widthButton = 215 / 2f;
        float x = ClientUtil.calc(width) / 2f - widthButton / 2f + 120;
        float y = Math.round(ClientUtil.calc(height) / 2f + 2);
        float ybuttons = Math.round(ClientUtil.calc(height) / 2f + 125);
        String textToDraw = altName1;

        if (!typing && altName1.isEmpty()) {
            textToDraw = "Введите никнейм";
        }
        if (MathUtil.isInRegion(mouseX,mouseY,x + 5,ybuttons,60,15)) {
            colortextbuttoncreate = ColorUtils.setAlpha(ThemeSwitcher.textcolor, 255);
        } else {
            colortextbuttoncreate = ColorUtils.rgb(100, 100, 100);
        }
        if (MathUtil.isInRegion(mouseX,mouseY,x + 185,ybuttons,60,15)) {
            colortextbuttonrandom = ColorUtils.setAlpha(ThemeSwitcher.textcolor, 255);
        } else {
            colortextbuttonrandom = ColorUtils.rgb(100, 100, 100);
        }
        //фон
        DisplayUtils.drawRoundedRect(x,y,250,140,new Vector4f(5,5,5,5),ColorUtils.setAlpha(ColorUtils.rgb(12,12,12), (int) (255 * altManageranim)));
        //кнопка создания
        DisplayUtils.drawRoundedRect(x + 5,ybuttons,60,15,new Vector4f(5,5,5,5),ColorUtils.setAlpha(ColorUtils.rgb(14,14,14), (int) (255 * altManageranim)));
        Fonts.notoitalic[16].drawCenteredString(matrixStack,"Создать",x + 35,ybuttons+5, ColorUtils.setAlpha(colortextbuttoncreate, (int) (255 * altManageranim)));
        //поле ввода ника
        DisplayUtils.drawRoundedRect(x + 70,ybuttons,110,15,new Vector4f(5,5,5,5),ColorUtils.setAlpha(ColorUtils.rgb(14,14,14), (int) (255 * altManageranim)));
        String name = (textToDraw + (typing ? (System.currentTimeMillis() % 1000 > 500 ? "_" : "") : ""));
        String substring = name.substring(0, Math.min(name.length(), 20));
        Fonts.notoitalic[16].drawString(matrixStack,substring,x + 75,ybuttons+5, ColorUtils.setAlpha(-1, (int) (255 * altManageranim)));
        DisplayUtils.drawShadow(x + 165,ybuttons,15,15,5,ColorUtils.setAlpha(ColorUtils.rgb(14,14,14), (int) (255 * altManageranim)));
        //кнопка рандомного ника
        DisplayUtils.drawRoundedRect(x + 185,ybuttons,60,15,new Vector4f(5,5,5,5),ColorUtils.setAlpha(ColorUtils.rgb(14,14,14), (int) (255 * altManageranim)));
        Fonts.notoitalic[16].drawCenteredString(matrixStack,"Очистить всё",x + 215,ybuttons+5, ColorUtils.setAlpha(colortextbuttonrandom, (int) (255 * altManageranim)));


    }




    private void drawChangelog(MatrixStack stack) {
        //animation by johon0
        animationtext = MathUtil.lerp(animationtext, (changelogopen) ? 1f : 0f, 10);
        changeloganim = MathUtil.lerp(animationtext, (changelogopen) ? 0 : 1, 10);
        // colors
        int fix =  ColorUtils.setAlpha(ColorUtils.interpolateColor(ColorUtils.rgba(255, 174, 51,255), ColorUtils.rgba(255, 174, 51,255), animationtext), (int) (255 * animationtext));
        int add = ColorUtils.setAlpha(ColorUtils.interpolateColor(ColorUtils.rgba(0, 255, 12,255), ColorUtils.rgba(0, 255, 12,255), animationtext), (int) (255 * animationtext));
        int del =  ColorUtils.setAlpha(ColorUtils.interpolateColor(ColorUtils.rgba(255, 0, 0,255), ColorUtils.rgba(255, 0, 0,255), animationtext), (int) (255 * animationtext));
        int optimized = ColorUtils.setAlpha(ColorUtils.interpolateColor(ColorUtils.rgba(139, 99, 255,255), ColorUtils.rgba(139, 99, 255,255), animationtext), (int) (255 * animationtext));
        // fonts
        int fonts = ColorUtils.setAlpha(ThemeSwitcher.textcolor, (int) (255 * animationtext));
        int fontsdark = ColorUtils.setAlpha(ColorUtils.interpolateColor(Color.GRAY.getRGB(), Color.GRAY.getRGB(), animationtext), (int) (255 * animationtext));
        int changelogrectanimate = ColorUtils.setAlpha(ColorUtils.rgb(12,12,12), (int) (255 * animationtext));

        DisplayUtils.drawRoundedRect(10, 42,320,190,new Vector4f(4,4,4,4), changelogrectanimate);

        //fixed
        Fonts.notoitalic[16].drawString(stack,"Добавили Модуль MusicPlayerUI §7(Коллаба с ChurkaClient)",22, 48, fonts);
        Fonts.notoitalic[16].drawString(stack,"Новые ТаргетESP §7(Кругляшок, Окружность)",22, 60, fonts);
        Fonts.notoitalic[16].drawString(stack,"Сделал поменьше тем §7(Оставил самые нормальные)",22, 70, fonts);
        Fonts.notoitalic[16].drawString(stack,"Добавлен ElytraTarget",22, 80, fonts);
        Fonts.notoitalic[16].drawString(stack,"Добавил команду .autopilot §7(пока что бета)",22, 90, fonts);
        Fonts.notoitalic[16].drawString(stack,"Переделал KillAura §7(Работает на FunTime, ReallyWorld, MusteryWorld)",22, 100, fonts);
        Fonts.notoitalic[16].drawString(stack,"Пофикшен флаг с NoSlow §7(Работает Коректно)",22, 110, fonts);
        Fonts.notoitalic[16].drawString(stack,"Добавил Женщин =) §7(сами увидите)",22, 120, fonts);
        Fonts.notoitalic[16].drawString(stack,"Переделал Кнопки §7(красива)",22, 130, fonts);
        Fonts.notoitalic[16].drawString(stack,"Чуточку доработал худ",22, 140, fonts);
        Fonts.notoitalic[16].drawString(stack,"Доработал FreeLook",22, 150, fonts);
        Fonts.notoitalic[16].drawString(stack,"Новые ESP в стиле Nursultan",22, 160, fonts);
        Fonts.notoitalic[16].drawString(stack,"Новый фон в MainMenu §7(Заметели?)§f)",22, 170, fonts);
        Fonts.notoitalic[16].drawString(stack,"Добавил цвет тумана в модуль World",22, 180, fonts);
        Fonts.notoitalic[16].drawString(stack,"Переделал ChinaHat",22, 190, fonts);
        Fonts.notoitalic[16].drawString(stack,"Убрал StorageESP",22, 200, fonts);
        Fonts.notoitalic[16].drawString(stack,"Добавил модуль AutoDuel для ReallyWorld",22, 210, fonts);
        Fonts.notoitalic[16].drawString(stack,"Исправил многие баги в клиенте, которые нашел сам",22, 220, fonts);













        DisplayUtils.drawCircle(16, 50,6,add);
        DisplayUtils.drawCircle(16, 62,6,add);
        DisplayUtils.drawCircle(16, 72,6,add);
        DisplayUtils.drawCircle(16, 82,6,add);
        DisplayUtils.drawCircle(16, 92,6,add);
        DisplayUtils.drawCircle(16, 102,6,fix);
        DisplayUtils.drawCircle(16, 112,6,fix);
        DisplayUtils.drawCircle(16, 122,6,add);
        DisplayUtils.drawCircle(16, 132,6,fix);
        DisplayUtils.drawCircle(16, 142,6,fix);
        DisplayUtils.drawCircle(16, 152,6,fix);
        DisplayUtils.drawCircle(16, 162,6,add);
        // optimized
        DisplayUtils.drawCircle(16, 172,6,add);
        DisplayUtils.drawCircle(16, 182,6,add);
        DisplayUtils.drawCircle(16, 192,6,fix);
        DisplayUtils.drawCircle(16, 202,6,del);
        DisplayUtils.drawCircle(16, 212,6,add);
        DisplayUtils.drawCircle(16, 222,6,fix);
//        DisplayUtils.drawCircle(16, 133,6,optimized);
//         add
//        DisplayUtils.drawCircle(16, 153,6,add);
//        DisplayUtils.drawCircle(16, 173,6,add);
//        DisplayUtils.drawCircle(16, 183,6,add);
    }

//    private void changeTheme(MatrixStack matrixStack) {
//        Fonts.iconsall[100].drawString(matrixStack, ThemeSwitcher.themelightofdark ? "T" : "S", mc.getMainWindow().getScaledWidth() - 50, mc.getMainWindow().getScaledHeight() - 50, ThemeSwitcher.themelightofdark ? ColorUtils.rgba(0, 0, 0, (int)(255.0f)) : ColorUtils.rgba(255, 255, 255, (int)(255.0f)));
//    }

    private void drawName(MatrixStack stack, String name, float x,float y,int color) {
        Fonts.centricbold[50].drawCenteredString(stack,name,x,y,color);
    }
    private void drawinfo(MatrixStack stack, String name, float x,float y,int color) {
        Fonts.centricbold[14].drawCenteredString(stack,name,x+2,y,ColorUtils.setAlpha(color,100));
    }

    private void drawButtons(MatrixStack stack, int mX, int mY, float pt) {

        buttons.forEach(b -> b.render(stack, mX, mY, pt));
    }


    @AllArgsConstructor
    private class Button {
        @Getter
        private final float x, y, width, height;
        private String text;
        private Runnable action;
        
        public void render(MatrixStack stack, int mouseX, int mouseY, float pt) {
            int color;
            DisplayUtils.drawRoundedRect(x, y + 2, width, height, 3, ColorUtils.rgba(10,10,10,255));
            color = ColorUtils.reAlphaInt(ColorUtils.rgb(100,100,100), (int) (255));
            if ((MathUtil.isInRegion(mouseX, mouseY, x, y, width, height))) {
                color = ColorUtils.reAlphaInt(ThemeSwitcher.textcolor, (int) (255));
            }

           Fonts.notoitalic[18].drawCenteredString(stack, text, x + width / 2f, y + height / 2f - 2.5f + 2, color);


        }

        public void click(int mouseX, int mouseY, int button) {
            if (MathUtil.isInRegion(mouseX, mouseY, x, y, width, height)) {
                action.run();
            }
        }

    }

}
