package centric.pl.johon0.utils.drag;

import centric.pl.functions.api.Function;
import centric.pl.johon0.utils.font.Fonts;
import centric.pl.johon0.utils.math.MathUtil;
import centric.pl.johon0.utils.client.ClientUtil;
import centric.pl.johon0.utils.client.Vec2i;
import centric.pl.johon0.utils.render.ColorUtils;
import centric.pl.johon0.utils.render.DisplayUtils;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;

public class Dragging {
    @Expose
    @SerializedName("x")
    private float xPos;
    @Expose
    @SerializedName("y")
    private float yPos;
    public float initialXVal;
    public float initialYVal;
    private float startX;
    private float startY;
    private boolean dragging;
    private float width;
    private float height;
    @Expose
    @SerializedName("name")
    private final String name;
    private final Function module;
    private static final float grid = 20.0F;
    private static final float snap_thr = 5.0F;
    private float lineAlpha = 0.0F;
    private long lastUpdateTime;
    private boolean showVerticalLine = false;
    private boolean showHorizontalLine = false;
    private float closestVerticalLine = 0.0F;
    private float closestHorizontalLine = 0.0F;
    private final float resetButtonWidth = 110.0F;
    private final float resetButtonHeight = 16.0F;
    private final String resetButtonText = "Reset Draggables";

    public Dragging(Function module, String name, float initialXVal, float initialYVal) {
        this.module = module;
        this.name = name;
        this.xPos = initialXVal;
        this.yPos = initialYVal;
        this.initialXVal = initialXVal;
        this.initialYVal = initialYVal;
    }

    public Function getModule() {
        return this.module;
    }

    public String getName() {
        return this.name;
    }

    public float getWidth() {
        return this.width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return this.height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getX() {
        return this.xPos;
    }

    public void setX(float x) {
        this.xPos = x;
    }

    public float getY() {
        return this.yPos;
    }

    public void setY(float y) {
        this.yPos = y;
    }

    public final void onDraw(int mouseX, int mouseY, MainWindow res) {
        Vec2i fixed = ClientUtil.getMouse(mouseX, mouseY);
        mouseX = fixed.getX();
        mouseY = fixed.getY();
        if (this.dragging) {
            this.xPos = (float)mouseX - this.startX;
            this.yPos = (float)mouseY - this.startY;
            this.xPos = this.snap(this.xPos, 20.0F, 5.0F);
            this.yPos = this.snap(this.yPos, 20.0F, 5.0F);
            if (this.xPos + this.width > (float)res.scaledWidth()) {
                this.xPos = (float)res.scaledWidth() - this.width;
            }

            if (this.yPos + this.height > (float)res.scaledHeight()) {
                this.yPos = (float)res.scaledHeight() - this.height;
            }

            if (this.xPos < 0.0F) {
                this.xPos = 0.0F;
            }

            if (this.yPos < 0.0F) {
                this.yPos = 0.0F;
            }

            this.updateLineAlpha(true);
            this.checkClosestGridLines();
        } else {
            this.updateLineAlpha(false);
            this.showVerticalLine = false;
            this.showHorizontalLine = false;
        }

        this.drawGridLines(res);
    }

    private float snap(float pos, float gridSpacing, float snapThreshold) {
        float gridPos = (float)Math.round(pos / gridSpacing) * gridSpacing;
        return Math.abs(pos - gridPos) < snapThreshold ? gridPos : pos;
    }

    private void updateLineAlpha(boolean increasing) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (float)(currentTime - this.lastUpdateTime) / 1000.0F;
        this.lastUpdateTime = currentTime;
        if (increasing) {
            this.lineAlpha += deltaTime * 4.0F;
            if (this.lineAlpha > 1.0F) {
                this.lineAlpha = 1.0F;
            }
        } else {
            this.lineAlpha -= deltaTime * 4.0F;
            if (this.lineAlpha < 0.0F) {
                this.lineAlpha = 0.0F;
            }
        }

    }

    private void checkClosestGridLines() {
        this.closestVerticalLine = (float)Math.round(this.xPos / 20.0F) * 20.0F;
        this.closestHorizontalLine = (float)Math.round(this.yPos / 20.0F) * 20.0F;
        this.showVerticalLine = Math.abs(this.xPos - this.closestVerticalLine) < 5.0F;
        this.showHorizontalLine = Math.abs(this.yPos - this.closestHorizontalLine) < 5.0F;
    }

    private void drawGridLines(MainWindow res) {
        Minecraft.getInstance().gameRenderer.setupOverlayRendering(2);
        float alpha = this.lineAlpha * 1.0F;
        int color = (int)(alpha * 255.0F) << 24 | 16777215;
        if (this.showVerticalLine) {
            DisplayUtils.drawRoundedRect(this.closestVerticalLine, 0.0F, 0.5f, (float)res.scaledHeight(), 1.0F, ColorUtils.setAlpha(color,100));
        }

        if (this.showHorizontalLine) {
            DisplayUtils.drawRoundedRect(0.0F, this.closestHorizontalLine, (float)res.scaledWidth(), 1f, 1.0F, ColorUtils.setAlpha(color,100));
        }

        Minecraft.getInstance().gameRenderer.setupOverlayRendering();
    }


    public final boolean onClick(double mouseX, double mouseY, int button) {
        Vec2i fixed = ClientUtil.getMouse((int)mouseX, (int)mouseY);
        mouseX = (double)fixed.getX();
        mouseY = (double)fixed.getY();
        if (button == 0 && MathUtil.isInRegion((float)mouseX, (float)mouseY, this.xPos, this.yPos, this.width, this.height)) {
            this.dragging = true;
            this.startX = (float)((int)(mouseX - (double)this.xPos));
            this.startY = (float)((int)(mouseY - (double)this.yPos));
            return true;
        } else {
            return false;
        }
    }

    public final void onRelease(int button) {
        if (button == 0) {
            this.dragging = false;
        }

    }

    public void resetPosition() {
        this.xPos = this.initialXVal;
        this.yPos = this.initialYVal;
    }
}
