package centric.pl.functions.impl.render;

import centric.pl.Main;
import centric.pl.events.impl.EventDisplay;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.settings.impl.BooleanSetting;
import centric.pl.johon0.utils.font.Fonts;
import centric.pl.johon0.utils.font.styled.StyledFont;
import centric.pl.johon0.utils.render.ColorUtils;
import centric.pl.johon0.utils.rotation.ProjectionUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.Iterator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.vector.Vector3d;
import org.joml.Vector2d;
import org.lwjgl.opengl.GL11;


@FunctionRegister(
        name = "Pearl Prediction",
        type = Category.Render,
        beta = false
)
public class Predictions extends Function {
    private final BooleanSetting tag = new BooleanSetting("Рисовать теги", false);

    public Predictions() {
        this.addSettings(this.tag);
    }

    public void onDisplay(EventDisplay event) {
        if (this.tag.get()) {
            this.tag(event);
        }
                RenderSystem.pushMatrix();
                RenderSystem.translated(-mc.getRenderManager().pointedEntity.getPosX(), -mc.getRenderManager().pointedEntity.getPosY(), -mc.getRenderManager().pointedEntity.getPosZ());
                RenderSystem.enableBlend();
                RenderSystem.disableTexture();
                RenderSystem.disableDepthTest();
                GL11.glEnable(2848);
                RenderSystem.lineWidth(2.0F);
                buffer.begin(1, DefaultVertexFormats.POSITION_COLOR);
                Iterator var3 = mc.world.getAllEntities().iterator();

                while(var3.hasNext()) {
                    Entity e = (Entity)var3.next();
                    if (e instanceof EnderPearlEntity) {
                        EnderPearlEntity pearl = (EnderPearlEntity)e;
                        this.renderLine(pearl);
                    }
                }

                buffer.finishDrawing();
                WorldVertexBufferUploader.draw(buffer);
                RenderSystem.enableDepthTest();
                RenderSystem.enableTexture();
                RenderSystem.disableBlend();
                GL11.glDisable(2848);
                RenderSystem.popMatrix();
            }

    private double calculateInterpolatedPosition(double previousPos, double currentPos, float partialTicks) {
        return -(previousPos + (currentPos - previousPos) * (double)partialTicks);
    }

    private void renderLine(EnderPearlEntity pearl) {
        Vector3d pearlPosition = pearl.getPositionVec().add(0.0, 0.0, 0.0);
        Vector3d pearlMotion = pearl.getMotion();

        for(int i = 0; i <= 300; ++i) {
            Vector3d lastPosition = pearlPosition;
            pearlPosition = pearlPosition.add(pearlMotion);
            pearlMotion = this.updatePearlMotion(pearl, pearlMotion);
            if (this.shouldEntityHit(pearlPosition.add(0.0, 1.0, 0.0), lastPosition.add(0.0, 1.0, 0.0)) || pearlPosition.y <= 0.0) {
                break;
            }

            float[] colors = this.getLineColor(i);
            buffer.pos(lastPosition.x, lastPosition.y, lastPosition.z).color(colors[0], colors[1], colors[2], 1.0F).endVertex();
            buffer.pos(pearlPosition.x, pearlPosition.y, pearlPosition.z).color(colors[0], colors[1], colors[2], 1.0F).endVertex();
        }

    }

    private Vector3d updatePearlMotion(EnderPearlEntity pearl, Vector3d originalPearlMotion) {
        Vector3d pearlMotion;
        if (pearl.isInWater()) {
            pearlMotion = originalPearlMotion.scale(0.800000011920929);
        } else {
            pearlMotion = originalPearlMotion.scale(0.9900000095367432);
        }

        if (!pearl.hasNoGravity()) {
            pearlMotion.y -= (double)pearl.getGravityVelocity();
        }

        return pearlMotion;
    }

    private boolean shouldEntityHit(Vector3d pearlPosition, Vector3d lastPosition) {
        RayTraceContext rayTraceContext = new RayTraceContext(lastPosition, pearlPosition, BlockMode.COLLIDER, FluidMode.NONE, mc.player);
        BlockRayTraceResult blockHitResult = mc.world.rayTraceBlocks(rayTraceContext);
        return blockHitResult.getType() == net.minecraft.util.math.RayTraceResult.Type.BLOCK;
    }

    private float[] getLineColor(int index) {
        int color = Main.getInstance().getStyleManager().getCurrentStyle().getColor(index * 2);
        return ColorUtils.IntColor.rgb(color);
    }

    private void tag(EventDisplay e) {
        float width = 40.0F;
        float height = 6.0F;
        StyledFont small = Fonts.notoitalic[11];
        Iterator var5 = mc.world.getAllEntities().iterator();

        while(var5.hasNext()) {
            Entity entity = (Entity)var5.next();
            if (entity instanceof EnderPearlEntity) {
                double x1 = entity.lastTickPosX + (entity.getPosX() - entity.lastTickPosX) * (double)e.getPartialTicks();
                double y1 = entity.lastTickPosY + (entity.getPosY() - entity.lastTickPosY) * (double)e.getPartialTicks();
                double z1 = entity.lastTickPosZ + (entity.getPosZ() - entity.lastTickPosZ) * (double)e.getPartialTicks();
                Vector2d vector2d = ProjectionUtils.project(x1, y1 + 0.6, z1);
                String text = entity.getName().getString();
                float textWidth = small.getWidth(text) + 1.0F;
                float centr = -2.0F + textWidth / 2.0F;
                if (vector2d != null) {
                    GL11.glPushMatrix();
                    HUD.drawStyledRect((float)vector2d.x - 1.0F - centr, (float)vector2d.y - 1.0F, textWidth, height, 2.0F);
                    small.drawString(e.getMatrixStack(), entity.getName(), (double)((float)vector2d.x - centr), (double)((float)vector2d.y + 1.0F), (new Color(255, 255, 255, 255)).getRGB());
                    GL11.glPopMatrix();
                }
            }
        }

    }
}
