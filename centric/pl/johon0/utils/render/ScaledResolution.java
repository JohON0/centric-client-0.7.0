//package centric.pl.johon0.utils.render;
//
//import net.minecraft.client.Minecraft;
//import net.minecraft.util.math.MathHelper;
//
//public class ScaledResolution
//{
//    private final double scaledWidthD;
//    private final double scaledHeightD;
//    private int scaledWidth;
//    private int scaledHeight;
//    private static int scaleFactor;
//
//    public ScaledResolution(Minecraft minecraftClient)
//    {
//        this.scaledWidth = minecraftClient.getMainWindow().;
//        this.scaledHeight = minecraftClient.displayHeight;
//        this.scaleFactor = 1;
//        boolean flag = minecraftClient.getForceUnicodeFont();
//        int i = minecraftClient.gameSettings.guiScale;
//
//        if (i == 0)
//        {
//            i = 1000;
//        }
//
//        while (this.scaleFactor < i && this.scaledWidth / (this.scaleFactor + 1) >= 320 && this.scaledHeight / (this.scaleFactor + 1) >= 240)
//        {
//            ++this.scaleFactor;
//        }
//
//        if (flag && this.scaleFactor % 2 != 0 && this.scaleFactor != 1)
//        {
//            --this.scaleFactor;
//        }
//
//        this.scaledWidthD = (double)this.scaledWidth / (double)this.scaleFactor;
//        this.scaledHeightD = (double)this.scaledHeight / (double)this.scaleFactor;
//        this.scaledWidth = MathHelper.ceil(this.scaledWidthD);
//        this.scaledHeight = MathHelper.ceil(this.scaledHeightD);
//    }
//
//    public int getScaledWidth()
//    {
//        return this.scaledWidth;
//    }
//
//    public int getScaledHeight()
//    {
//        return this.scaledHeight;
//    }
//
//    public int getScaleFactor()
//    {
//        return scaleFactor;
//    }
//}
