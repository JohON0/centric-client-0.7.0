package centric.pl.johon0.utils.render;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class ScaleUtil {
    public static float size = 2.0f;

//    public static void scale_pre() {
//        double scale = (double)scaledRes.getScaleFactor() / Math.pow(scaledRes.getScaleFactor(), 2.0);
//        GL11.glPushMatrix();
//        GL11.glScaled((double)(scale * (double)size), (double)(scale * (double)size), (double)(scale * (double)size));
//    }

    public static void scale_post() {
        GL11.glScaled((double)size, (double)size, (double)size);
        GL11.glPopMatrix();
    }
}
