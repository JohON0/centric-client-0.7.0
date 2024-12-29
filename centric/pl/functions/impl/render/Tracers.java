package centric.pl.functions.impl.render;

import centric.pl.command.friends.FriendStorage;
import centric.pl.events.impl.WorldEvent;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.settings.impl.BooleanSetting;
import centric.pl.functions.settings.impl.ColorSetting;
import centric.pl.johon0.utils.EntityUtils;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.render.ColorUtils;
import com.google.common.eventbus.Subscribe;
import centric.pl.functions.impl.combat.AntiBot;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Vector3d;

import static org.lwjgl.opengl.GL11.*;

@FunctionRegister(name = "Tracers", type = Category.Render, beta = false)
public class Tracers extends Function {
    private final BooleanSetting ignoreNaked = new BooleanSetting("»гнорировать голых", true);
    private final ColorSetting color = new ColorSetting("÷вет", ColorUtils.rgb(255, 255, 255));

    public Tracers() {
        addSettings(ignoreNaked, color);
    }

    @Subscribe
    public void onRender(WorldEvent e) {
        glPushMatrix();

        glDisable(GL_TEXTURE_2D);
        glDisable(GL_DEPTH_TEST);

        glEnable(GL_BLEND);
        glEnable(GL_LINE_SMOOTH);

        glLineWidth(1);

        Vector3d cam = new Vector3d(0, 0, 150)
                .rotatePitch((float) -(Math.toRadians(IMinecraft.mc.getRenderManager().info.getPitch())))
                .rotateYaw((float) -Math.toRadians(IMinecraft.mc.getRenderManager().info.getYaw()));

        for (AbstractClientPlayerEntity player : IMinecraft.mc.world.getPlayers()) {
            if (player == IMinecraft.mc.player) continue;
            if (!player.isAlive()
                    || AntiBot.isBot(player)
                    || player.getTotalArmorValue() == 0.0f && ignoreNaked.get()) continue;

            Vector3d pos = EntityUtils.getInterpolatedPositionVec(player)
                    .subtract(IMinecraft.mc.getRenderManager().info.getProjectedView());

            ColorUtils.setColor(FriendStorage.isFriend(player.getGameProfile().getName()) ? FriendStorage.getColor() : color.get());

            IMinecraft.buffer.begin(1, DefaultVertexFormats.POSITION);

            IMinecraft.buffer.pos(cam.x, cam.y, cam.z).endVertex();
            IMinecraft.buffer.pos(pos.x, pos.y, pos.z).endVertex();


            IMinecraft.tessellator.draw();
        }

        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_DEPTH_TEST);

        glPopMatrix();
    }
}
