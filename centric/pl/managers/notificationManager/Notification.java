package centric.pl.managers.notificationManager;

import centric.pl.managers.styleManager.ThemeSwitcher;
import centric.pl.johon0.utils.animations.impl.DecelerateAnimation;
import centric.pl.johon0.utils.font.Fonts;
import com.mojang.blaze3d.matrix.MatrixStack;

import java.util.concurrent.CopyOnWriteArrayList;

import lombok.Getter;
import lombok.Setter;
import centric.pl.johon0.utils.animations.Animation;
import centric.pl.johon0.utils.animations.Direction;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.math.MathUtil;
import centric.pl.johon0.utils.render.ColorUtils;
import centric.pl.johon0.utils.render.DisplayUtils;

import static centric.pl.functions.impl.render.HUD.getColor;

@SuppressWarnings("all")
public class Notification {
    private final CopyOnWriteArrayList<NotificationRender> notifications = new CopyOnWriteArrayList();
    private MathUtil AnimationMath;
    private static Type Type;
    boolean state;

    public void add(String text, String content, int time, Type type) {
        this.notifications.add(new NotificationRender(text, content, time, type));
    }

    public void draw(MatrixStack stack) {
        int yOffset = 0;
        for (NotificationRender notification : notifications) {

            if (System.currentTimeMillis() - notification.getTime() > (notification.time2 * 1000L) - 300) {
                notification.animation.setDirection(Direction.BACKWARDS);
            } else {
                notification.yAnimation.setDirection(Direction.FORWARDS);
                notification.animation.setDirection(Direction.FORWARDS);
            }
            notification.alpha = (float) notification.animation.getOutput();
            if (System.currentTimeMillis() - notification.getTime() > notification.time2 * 1000L) {
                notification.yAnimation.setDirection(Direction.BACKWARDS);
            }
            if (notification.yAnimation.finished(Direction.BACKWARDS)) {
                notifications.remove(notification);
                continue;
            }
            float xcenter = IMinecraft.mc.getMainWindow().scaledWidth() / 2f-68;
            float ycenter = IMinecraft.mc.getMainWindow().scaledHeight() / 2f+10;
            notification.yAnimation.setEndPoint(yOffset);
            notification.yAnimation.setDuration(300);
            ycenter += (float) (notification.draw(stack) * notification.yAnimation.getOutput());
            notification.setX(xcenter);
            notification.setY(AnimationMath.fast(notification.getY(), ycenter, 15));
            yOffset++;
        }
    }

    public class NotificationRender {

        @Getter
        @Setter
        private float x, y = IMinecraft.mc.getMainWindow().scaledHeight() + 24;

        @Getter
        private String text;
        @Getter
        private String content;
        private Type icon;
        @Getter
        private long time = System.currentTimeMillis();

        public Animation animation = new DecelerateAnimation(500, 1, Direction.FORWARDS);
        public Animation yAnimation = new DecelerateAnimation(500, 1, Direction.FORWARDS);
        static float alpha;
        int time2;
        public static void drawStyledRect(float x,
                                          float y,
                                          float width,
                                          float height,
                                          float radius) {
            DisplayUtils.drawShadow(x, y, width, height, 3, ColorUtils.reAlphaInt(ThemeSwitcher.bgcolor, (int) (255 * alpha)));
            DisplayUtils.drawRoundedRect(x, y, width, height, radius, ColorUtils.reAlphaInt(ThemeSwitcher.bgcolor, (int) (255 * alpha)));

        }
        public NotificationRender(String text, String content, int time, Notification.Type type) {
            this.text = text;
            this.content = content;
            time2 = time;
            this.icon = type;
        }
        String iconrender;
        int coloricon;
        public float draw(MatrixStack stack) {
            float width = Fonts.notoitalic[14].getWidth(content + " | " + text) + 14;
            drawStyledRect(x, y, width, 13, 3);
            if (icon == Notification.Type.success) {
                iconrender = "Q";
                coloricon = ColorUtils.green;
            }
            if (icon == Notification.Type.unsucces) {
                iconrender = "R";
                coloricon = ColorUtils.red;
            }
            if (icon == Notification.Type.spec) {
                iconrender = "L";
                coloricon = ColorUtils.red;
            }
            if (icon == Notification.Type.warning) {
                iconrender = "W";
                coloricon = ColorUtils.yellow;
            }
            int yicon = 0;

            if (icon == Notification.Type.success) {
                yicon -=2;
            }
            if (icon == Notification.Type.unsucces) {
                yicon -=1;
            }
            if (icon == Notification.Type.spec) {
            }
            if (icon == Notification.Type.warning) {
            }
            Fonts.iconsall[16].drawString(stack,iconrender, x+5, y+6 + yicon, ColorUtils.reAlphaInt(coloricon, (int) (255 * alpha)));
            Fonts.notoitalic[14].drawString(stack, content, x + 12, y + 5, ColorUtils.reAlphaInt(ThemeSwitcher.textcolor, (int) (255 * alpha)));
            Fonts.notoitalic[14].drawString(stack, " | " + text, x + Fonts.notoitalic[14].getWidth(content) + 12, y + 5, ColorUtils.reAlphaInt(ThemeSwitcher.textcolor, (int) (255 * alpha)));
            return 15;
        }
    }


    public enum Type {
        success,
        unsucces,
        warning,
        spec;
    }
}