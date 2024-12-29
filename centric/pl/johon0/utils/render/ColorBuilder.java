package centric.pl.johon0.utils.render;

import lombok.AllArgsConstructor;

import java.awt.*;

@AllArgsConstructor
public class ColorBuilder {
    private int red;
    private int green;
    private int blue;
    private int alpha;

    public ColorBuilder(int rgba) {
        this(new Color(rgba));
    }

    public ColorBuilder(Color rgba) {
        this(rgba.getRed(), rgba.getGreen(), rgba.getBlue(), rgba.getAlpha());
    }

    public int red() {
        return this.red;
    }

    public int green() {
        return this.green;
    }

    public int blue() {
        return this.blue;
    }

    public int alpha() {
        return this.alpha;
    }

    public Color build() {
        return new Color(this.red, this.green, this.blue, this.alpha);
    }

    public int rgb() {
        return this.build().getRGB();
    }

    public ColorBuilder red(int red) {
        return new ColorBuilder(red, this.green, this.blue, this.alpha);
    }

    public ColorBuilder green(int green) {
        return new ColorBuilder(this.red, green, this.blue, this.alpha);
    }

    public ColorBuilder blue(int blue) {
        return new ColorBuilder(this.red, this.green, blue, this.alpha);
    }

    public ColorBuilder alpha(int alpha) {
        return new ColorBuilder(this.red, this.green, this.blue, alpha);
    }
}