package centric.pl.johon0.utils.components;

import centric.pl.johon0.utils.math.MathUtil;
import centric.pl.johon0.utils.render.ColorUtils;
import centric.pl.johon0.utils.render.DisplayUtils;
import centric.pl.johon0.utils.render.font.FontsUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.glfw.GLFW;

public class FieldComponent {
    @Setter
    @Getter
    float x, y, width, height;
    public String name;
    String input = "";
    boolean inputing;
    boolean hide = false;
    public FieldComponent(float x, float y, float width, float height, String name) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.name = name;
    }

    public FieldComponent(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.hide = true;
    }


    public void draw(MatrixStack stack, float mouseX, float mouseY) {
        if (hide) {
            DisplayUtils.drawRoundedRect(x,y,width,height, 1, MathUtil.isInRegion(mouseX,mouseY,x,y,width,height) ? ColorUtils.rgba(31, 31, 31, 255) : ColorUtils.rgba(27, 27, 27, 255));
            FontsUtil.montserrat.drawCenteredText(stack,input + (inputing ? System.currentTimeMillis() % 1000 >= 500 ? "|" : "" : ""),x + 2 + (width / 2f) / 2f,y  +0.5f, -1,5);
            return;
        }
        FontsUtil.montserrat.drawText(stack, name, x, y, -1, 6);
        DisplayUtils.drawRoundedRect(x,y + 7,width,height - 7, 3, MathUtil.isInRegion(mouseX,mouseY,x,y + 7,width,height - 7) ? ColorUtils.rgba(31, 31, 31, 255) : ColorUtils.rgba(27, 27, 27, 255));
        FontsUtil.montserrat.drawText(stack,input + (inputing ? System.currentTimeMillis() % 1000 >= 500 ? "|" : "" : ""),x + 2,y + height / 2f + 1, -1,5);
    }

    public void click(int mouseX, int mouseY) {
        if (MathUtil.isInRegion(mouseX,mouseY,x,y,width,height)) {
            inputing = !inputing;
        }
    }

    public void key(int key) {

        if (inputing && key == GLFW.GLFW_KEY_BACKSPACE) {
            if (!input.isEmpty()) {
                input = input.substring(0, input.length() - 1);
            }
        }
        if (inputing && key == GLFW.GLFW_KEY_ENTER) {
            inputing = false;
        }
    }

    public String get() {
        return input;
    }
    public void set(String text) {
        input = text;
    }

    public void charTyped(char c) {
        if (inputing) {
            if (FontsUtil.montserrat.getWidth(input + c, 5) > width - FontsUtil.montserrat.getWidth(String.valueOf(c), 5)) return;
            input += c;
        }
    }

}
