package centric.pl.johon0.utils.components;

import centric.pl.johon0.utils.math.MathUtil;
import centric.pl.johon0.utils.render.ColorUtils;
import centric.pl.johon0.utils.render.DisplayUtils;
import centric.pl.johon0.utils.render.font.FontsUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.math.MathHelper;

public class SliderComponent {

    @Setter
    @Getter
    float x, y, width, height;
    public String name;

    public int min,max,current;
    public Enchantment enchantment;

    public SliderComponent(float x, float y, float width, float height, int min, int max, String name) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.name = name;
        this.min = min;
        this.max = max;
        this.current = min - 1;
        fieldComponent.set(String.valueOf(current));
    }

    public SliderComponent(float x, float y, float width, float height, int min, int max, Enchantment enchantment, String name) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.name = name;
        this.min = min;
        this.max = max;
        this.current = min - 1;
        this.enchantment = enchantment;
        fieldComponent.set(String.valueOf(current));
    }




    boolean drag;

    float widthSlider = 0;
    public FieldComponent fieldComponent = new FieldComponent(0,0,0,0);

    public void draw(MatrixStack stack, float mouseX, float mouseY) {
        int current = fieldComponent.get().isEmpty() ? 0 : Integer.parseInt(fieldComponent.get());
        FontsUtil.montserrat.drawText(stack, name, x, y, -1, 6);
        fieldComponent.setX(x + FontsUtil.montserrat.getWidth(name, 6) + 5);
        fieldComponent.setY(y);
        fieldComponent.setWidth(10);
        fieldComponent.setHeight(6);
        fieldComponent.draw(stack,mouseX,mouseY);
        float widh = (width) * (current - (min - 1)) / (max - (min - 1));
        widthSlider = MathHelper.clamp(MathUtil.fast(widthSlider, widh, 15), 0, width);
        DisplayUtils.drawRoundedRect(x,y + 7,width,height - 7, 1, ColorUtils.rgba(27, 27, 27, 255));
        DisplayUtils.drawRoundedRect(x,y + 7,widthSlider,height - 7, 1, ColorUtils.getColor(0));

        if (drag) {
            current = (int) MathHelper.clamp(MathUtil.round((mouseX - x) / (width) * (max - (min - 1)) + (min - 1), 1), min - 1, max);
            fieldComponent.set(String.valueOf(current));
        }
    }

    public void click(int mouseX, int mouseY) {
        if (MathUtil.isInRegion(mouseX,mouseY, x,y + 7 - 2,width,height - 7 + 4)) {
            drag = true;

        }
        fieldComponent.click(mouseX,mouseY);
    }

    public void unpress() {
        drag = false;
    }

    public void key(int key) {
        fieldComponent.key(key);
    }
    public void charTyped(char c) {
        if (Character.isDigit(c)) {
            fieldComponent.charTyped(c);
        }
    }

}