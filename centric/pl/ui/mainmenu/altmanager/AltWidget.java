package centric.pl.ui.mainmenu.altmanager;

import centric.pl.Main;
import centric.pl.johon0.utils.font.Fonts;
import centric.pl.managers.notificationManager.Notification;
import centric.pl.managers.styleManager.ThemeSwitcher;
import centric.pl.johon0.utils.client.ClientUtil;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.math.MathUtil;
import centric.pl.johon0.utils.render.ColorUtils;
import centric.pl.johon0.utils.render.DisplayUtils;
import centric.pl.johon0.utils.render.Scissor;
import centric.pl.johon0.utils.render.font.FontsUtil;
import centric.pl.ui.mainmenu.MainScreen;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;


public class AltWidget implements IMinecraft {

    public static final List<Alt> alts = new LinkedList<>();

    private float x;
    private float y;

    public AltWidget() {

    }


    public boolean open = true;

    private String altName = "";
    private boolean typing;
    private float altsanim;
    private float scrollPre;
    private float scroll;

    public void updateScroll(int mouseX, int mouseY, float delta) {

        if (MathUtil.isInRegion(mouseX, mouseY, this.x, this.y, 145, 150)) {
            scrollPre += delta * 10;
        }
    }

    public void render(MatrixStack stack) {
        Minecraft.getInstance().keyboardListener.enableRepeatEvents(true);
        altsanim = MathUtil.lerp(altsanim, (MainScreen.altmanageropen) ? 0f : 1f, 10);
        scroll = MathUtil.fast(scroll, scrollPre, 10);
        float widthButton = 215 / 2f;
        this.x = ClientUtil.calc(900) / 2f - widthButton / 2f + 150;
        this.y = mc.getMainWindow().scaledHeight() / 2f - 20;

        float width = 145;

        float height = Math.min(50 + (open ? 10 + (alts.size() + 1) * (17) : 0), 100);
        Fonts.centricbold[25].drawCenteredString(stack, "Alt Manager", this.x + 125, this.y + 25, ColorUtils.setAlpha(ThemeSwitcher.textcolor, (int) (255 * altsanim)));
        Scissor.push();
        Scissor.setFromComponentCoordinates(this.x, this.y, width - 16, height);
        Scissor.unset();
        Scissor.pop();
        Scissor.push();
        Scissor.setFromComponentCoordinates(this.x, this.y + 40, width + 100, 180f - 80);
        float i = 0;

        for (Alt alt : alts) {
            DisplayUtils.drawRoundedRect(this.x + 5, this.y + 46 + i * 22 + scroll, width + 95, 20, 3, mc.session.getUsername().equals(alt.name) ? ColorUtils.setAlpha(ThemeSwitcher.altselect, (int) (255 * altsanim)) : ColorUtils.setAlpha(ThemeSwitcher.altnoselect, (int) (255 * altsanim)));
            Fonts.notoitalic[25].drawCenteredString(stack, alt.name, this.x + 125, this.y + 48 + i * 22 + 4 + scroll, ColorUtils.setAlpha(ThemeSwitcher.textcolor, (int) (255 * altsanim)));
            i++;
        }
        if (!alts.isEmpty() && 20 + (open ? 10 + (alts.size() + 1) * (17) : 0) > 100)
            scrollPre = MathHelper.clamp(scrollPre, -i * 17 + 50, 0);
        else {
            scrollPre = 0;
        }
        Scissor.unset();
        Scissor.pop();
    }

    public void onChar(char typed) {
        if (typing) {
            if (FontsUtil.montserrat.getWidth(altName, 6f) < 145 - 50) {
                altName += typed;
            }
        }
    }

    public void onKey(int key) {
        boolean ctrlDown = GLFW.glfwGetKey(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS ||
                GLFW.glfwGetKey(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            Minecraft.getInstance().keyboardListener.enableRepeatEvents(false);
        }
        if (typing) {
            if (ctrlDown && key == GLFW.GLFW_KEY_V) {
                try {
                    altName += GLFW.glfwGetClipboardString(mc.getMainWindow().getHandle());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (key == GLFW.GLFW_KEY_BACKSPACE) {
                if (!altName.isEmpty()) {
                    altName = altName.substring(0, altName.length() - 1);
                }
            }
            if (key == GLFW.GLFW_KEY_ENTER) {
                if (altName.length() >= 3) {
                    Main.getInstance().getNotification().add("Аккаунт " + altName + " успешно сосздан!", "", 5, Notification.Type.success);
                    alts.add(new Alt(altName));
                    AltConfig.updateFile();

                } else {
                    Main.getInstance().getNotification().add("Никнейм должен быть больше 3-х символов!", "", 5, Notification.Type.warning);

                }
                Minecraft.getInstance().keyboardListener.enableRepeatEvents(false);
                typing = false;
            }
        }
    }

    public void click(int mouseX, int mouseY, int button) {
        if (!MainScreen.altmanageropen) {
            float width = 145;
            List<Alt> toRemove = new ArrayList<>();
            if (open) {
                float i = 0;
                for (Alt alt : alts) {

                    if (MathUtil.isInRegion(mouseX, mouseY, this.x + 5, this.y + 46 + i * 22 + scroll, width - 10, 15)) {
                        if (button == 0) {
                            AltConfig.updateFile();
                            mc.session = new Session(alt.name, UUID.randomUUID().toString(), "", "mojang");
                        } else {
                            toRemove.add(alt);
                            AltConfig.updateFile();
                        }
                    }
                    i++;
                }
                alts.removeAll(toRemove);

            }
            if (MathUtil.isInRegion(mouseX, mouseY, this.x + 5, this.y + 46 + alts.size() * 17 + scroll, width - 10, 15)) {
                typing = !typing;
            }
        }
    }
}
