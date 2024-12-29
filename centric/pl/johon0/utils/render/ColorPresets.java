package centric.pl.johon0.utils.render;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ColorPresets {
    public static final ColorBuilder NURIK_BG = new ColorBuilder(20, 20, 20, 235);
    public static final ColorBuilder NURIK_ALTER = new ColorBuilder(58, 58, 58, 235);
    public static final ColorBuilder NURIK_TEXT = new ColorBuilder(205, 205, 205, 255);

    public static final ColorBuilder LIGHT_BG = new ColorBuilder(255, 255, 255, 235);
    public static final ColorBuilder LIGHT_ALTER = new ColorBuilder(120, 120, 120, 235);
    public static final ColorBuilder LIGHT_TEXT = new ColorBuilder(25, 25, 25, 255);

    public static final ColorBuilder DARK_BG = new ColorBuilder(20, 20, 20, 235);
    public static final ColorBuilder DARK_ALTER = new ColorBuilder(58, 58, 58, 235);
    public static final ColorBuilder DARK_TEXT = new ColorBuilder(205, 205, 205, 255);

    public static ColorBuilder CURRENT_THEME_BG = LIGHT_BG;
    public static ColorBuilder CURRENT_THEME_ALTER = LIGHT_ALTER;
    public static ColorBuilder CURRENT_THEME_TEXT = LIGHT_TEXT;
}

