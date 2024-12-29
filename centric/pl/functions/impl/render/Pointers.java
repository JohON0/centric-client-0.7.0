package centric.pl.functions.impl.render;

import centric.pl.command.friends.FriendStorage;
import centric.pl.events.impl.EventDisplay;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.math.MathUtil;
import centric.pl.johon0.utils.player.MoveUtils;
import centric.pl.johon0.utils.player.PlayerUtils;
import centric.pl.johon0.utils.render.ColorUtils;
import centric.pl.johon0.utils.render.DisplayUtils;
import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;

@FunctionRegister(name = "Pointers", type = Category.Render, beta = false)
public class Pointers extends Function {

    public float animationStep;

    private float lastYaw;
    private float lastPitch;

    private float animatedYaw;
    private float animatedPitch;
    float yaw;


    @Subscribe
    public void onDisplay(EventDisplay e) {
        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null || e.getType() != EventDisplay.Type.PRE) {
            return;
        }

        animatedYaw = MathUtil.fast(animatedYaw, (IMinecraft.mc.player.moveStrafing) * 10,
                5);
        animatedPitch = MathUtil.fast(animatedPitch,
                (IMinecraft.mc.player.moveForward) * 10, 5);

        yaw = MathUtil.fast(yaw, IMinecraft.mc.player.rotationYaw, 10);

        float size = 70;

        if (IMinecraft.mc.currentScreen instanceof InventoryScreen) {
            size += 80;
        }

        if (MoveUtils.isMoving()) {
            size += 10;
        }
        animationStep = MathUtil.fast(animationStep, size, 6);
        if (IMinecraft.mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON) {
            for (AbstractClientPlayerEntity player : IMinecraft.mc.world.getPlayers()) {
                if (!PlayerUtils.isNameValid(player.getNameClear()) || IMinecraft.mc.player == player)
                    continue;

                double x = player.lastTickPosX + (player.getPosX() - player.lastTickPosX) * IMinecraft.mc.getRenderPartialTicks()
                        - IMinecraft.mc.getRenderManager().info.getProjectedView().getX();
                double z = player.lastTickPosZ + (player.getPosZ() - player.lastTickPosZ) * IMinecraft.mc.getRenderPartialTicks()
                        - IMinecraft.mc.getRenderManager().info.getProjectedView().getZ();

                double cos = MathHelper.cos((float) (yaw * (Math.PI * 2 / 360)));
                double sin = MathHelper.sin((float) (yaw * (Math.PI * 2 / 360)));
                double rotY = -(z * cos - x * sin);
                double rotX = -(x * cos + z * sin);

                float angle = (float) (Math.atan2(rotY, rotX) * 180 / Math.PI);

                double x2 = animationStep * MathHelper.cos((float) Math.toRadians(angle)) + IMinecraft.window.getScaledWidth() / 2f;
                double y2 = animationStep * MathHelper.sin((float) Math.toRadians(angle)) + IMinecraft.window.getScaledHeight() / 2f;

                x2 += animatedYaw;
                y2 += animatedPitch;

                GlStateManager.pushMatrix();
                GlStateManager.disableBlend();
                GlStateManager.translated(x2, y2, 0);
                GlStateManager.rotatef(angle, 0, 0, 1);

                int color = FriendStorage.isFriend(player.getGameProfile().getName()) ? FriendStorage.getColor() : ColorUtils.getColor(1);

//                DisplayUtils.drawShadowCircle(1, 0, 10, ColorUtils.setAlpha(color, 64));
                DisplayUtils.drawImage(new ResourceLocation("centric/images/arrowgpss.png"), -4, -1F, 18.0F, 18.0F, color);
//                drawTriangle(-4, -1F, 4F, 7F, new Color(0, 0, 0, 32));
//                drawTriangle(-3F, 0F, 3F, 5F, new Color(color));

                GlStateManager.enableBlend();
                GlStateManager.popMatrix();
            }
        }
        lastYaw = IMinecraft.mc.player.rotationYaw;
        lastPitch = IMinecraft.mc.player.rotationPitch;
    }

    public static void drawTriangle(float x, float y, float width, float height, Color color) {
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        enableSmoothLine(1);
        GL11.glRotatef(180 + 90, 0F, 0F, 1.0F);

        // fill.
        GL11.glBegin(9);
        ColorUtils.setColor(color.getRGB());
        GL11.glVertex2f(x, y - 2);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x, y - 2);
        GL11.glEnd();

        GL11.glBegin(9);
        ColorUtils.setColor(color.brighter().getRGB());
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x + width * 2, y - 2);
        GL11.glVertex2f(x + width, y);
        GL11.glEnd();

        // line.
        GL11.glBegin(3);
        ColorUtils.setColor(color.getRGB());
        GL11.glVertex2f(x, y - 2);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x, y - 2);
        GL11.glEnd();

        GL11.glBegin(3);
        ColorUtils.setColor(color.brighter().getRGB());
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x + width * 2, y - 2);
        GL11.glVertex2f(x + width, y);
        GL11.glEnd();

        disableSmoothLine();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glRotatef(-180 - 90, 0F, 0F, 1.0F);
        GL11.glPopMatrix();
    }

    private static void enableSmoothLine(float width) {
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glLineWidth(width);
    }

    private static void disableSmoothLine() {
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
    }

}
