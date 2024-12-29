package centric.pl.johon0.utils.font.styled;

import java.awt.*;
import java.util.Locale;

import centric.pl.johon0.utils.math.MathUtil;
import centric.pl.johon0.utils.render.ColorUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import centric.pl.functions.impl.render.HUD;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import centric.pl.johon0.utils.font.common.AbstractFont;
import centric.pl.johon0.utils.font.common.Lang;

public final class StyledFont {

    private final GlyphPage regular;

    public StyledFont(String fileName, int size, float stretching, float spacing, float lifting, boolean antialiasing, Lang lang) {
        int[] codes = lang.getCharCodes();
        char[] chars = new char[(codes[1] - codes[0] + codes[3] - codes[2])];

        int c = 0;
        for (int d = 0; d <= 2; d += 2) {
            for (int i = codes[d]; i <= codes[d + 1] - 1; i++) {
                chars[c] = (char) i;
                c++;
            }
        }

        this.regular = new GlyphPage(AbstractFont.getFont(fileName, Font.PLAIN, size), chars, stretching, spacing, lifting, antialiasing);
    }

    public StyledFont(String fileName, int size, float stretching, float spacing, float lifting, boolean antialiasing, Lang lang, boolean wind) {
        int[] codes = lang.getCharCodes();
        char[] chars = new char[(codes[1] - codes[0] + codes[3] - codes[2])];

        int c = 0;
        for (int d = 0; d <= 2; d += 2) {
            for (int i = codes[d]; i <= codes[d + 1] - 1; i++) {
                chars[c] = (char) i;
                c++;
            }
        }

        this.regular = new GlyphPage(AbstractFont.getFontWindows(fileName, Font.PLAIN, size), chars, stretching, spacing, lifting, antialiasing);
    }


    public float renderGlyph(Matrix4f matrix, char c, float x, float y, boolean bold, boolean italic, float red, float green, float blue, float alpha) {
        return getGlyphPage().renderGlyph(matrix, c, x, y, red, green, blue, alpha);
    }

    public void drawStringWithShadow(MatrixStack matrixStack, ITextComponent text, double x, double y, int color) {
        StyledFontRenderer.drawShadowedString(matrixStack, this, text, x, y, color);
    }

    public void drawString(MatrixStack matrixStack, String text, double x, double y, int color) {
        StyledFontRenderer.drawString(matrixStack, this, text, x, y, color);
    }

    public void drawStringTest(MatrixStack matrixStack, ITextComponent text, double x, double y, int color) {
        StyledFontRenderer.renderStringGradient(matrixStack, this, text, x, y, false, color);
    }


    public void drawString(MatrixStack matrixStack, ITextComponent text, double x, double y, int color) {
        StyledFontRenderer.drawString(matrixStack, this, text, x, y, color);
    }

    public void drawStringWithShadow(MatrixStack matrixStack, String text, double x, double y, int color) {
        StyledFontRenderer.drawShadowedString(matrixStack, this, text, x, y, color);
    }

    public void drawCenteredString(MatrixStack matrixStack, String text, double x, double y, int color) {
        StyledFontRenderer.drawCenteredXString(matrixStack, this, text, x, y, color);
    }

    public void drawCenteredString(MatrixStack matrixStack, ITextComponent text, double x, double y, int color) {
        StyledFontRenderer.drawCenteredString(matrixStack, this, text, x, y, color);
    }

    public void drawStringWithOutline(MatrixStack stack, String text, double x, double y, int color) {
        int c = HUD.getColor(100);
        x = MathUtil.round(x, 0.5F);
        y = MathUtil.round(y, 0.5F);
        StyledFontRenderer.drawString(stack, this, text, x - 0.2, y, c);
        StyledFontRenderer.drawString(stack, this, text, x + 0.2, y, c);
        StyledFontRenderer.drawString(stack, this, text, x, y - 0.52f, c);
        StyledFontRenderer.drawString(stack, this, text, x, y + 0.5f, c);

        drawString(stack, text, x, y, color);
    }

    public void drawCenteredStringWithOutline(MatrixStack stack, String text, double x, double y, int color) {
        drawStringWithOutline(stack, text, x - getWidth(text) / 2F, y, color);
    }
    public int draw(String text, float x, float y, int color) {
        MatrixStack matrices = new MatrixStack();
        draw(matrices, text, x, y, color, false);
        return (int) getWidth(text);
    }  public int draw(MatrixStack matrices, String text, float x, float y, int color, boolean dropShadow) {
        if (ColorUtils.getAlpha(color) < 10) return 0;
        y = y;
        int i;

        if (dropShadow) {
            i = this.draw(matrices, text, x + 1.0F, y + 1.0F, color, true);

            i = Math.max(i, this.draw(matrices, text, x, y, color, false));
        } else {
            i = this.draw(matrices, text, x, y, color, false);
        }

        return i;
    }
    public void drawUltraTest(ITextComponent text, float x, float y, int alpha) {
        float offset = 0;
        for (ITextComponent it : text.getSiblings()) {
            for (ITextComponent it1 : it.getSiblings()) {
                String draw = it1.getString();

                if (it1.getStyle().getColor() != null) {
                    draw(draw, x + offset, y, ColorUtils.setAlpha(ColorUtils.toColor(it1.getStyle().getColor().getHex()), alpha));
                } else {
                    draw(draw, x + offset, y, ColorUtils.setAlpha(Color.WHITE.getRGB(), alpha));
                }
                offset += getWidth(draw) + 2;
            }
            if (it.getSiblings().size() <= 1) {
                String draw = TextFormatting.getTextWithoutFormattingCodes(it.getString());

                draw(draw, x + offset + 2, y, ColorUtils.setAlpha(it.getStyle().getColor() == null ? Color.WHITE.getRGB() : it.getStyle().getColor().getColor(), alpha));
                offset += getWidth(draw) + 2;
            }
        }
        if (text.getSiblings().isEmpty()) {
            String draw = TextFormatting.getTextWithoutFormattingCodes(text.getString());

            draw(draw, x + offset + 2 , y, ColorUtils.setAlpha(text.getStyle().getColor() == null ? Color.WHITE.getRGB(): text.getStyle().getColor().getColor(), alpha));
            getWidth(draw);
        }
    }
    public float getWidth(String text) {
        float width = 0.0f;

        for (int i = 0; i < text.length(); i++) {
            char c0 = text.charAt(i);
            if (c0 == 167 && i + 1 < text.length() &&
                    StyledFontRenderer.STYLE_CODES.indexOf(text.toLowerCase(Locale.ENGLISH).charAt(i + 1)) != -1) {
                i++;
            } else {
                width += getGlyphPage().getWidth(c0) + regular.getSpacing();
            }
        }

        return (width - regular.getSpacing()) / 2.0f;
    }

    private GlyphPage getGlyphPage() {
        return regular;
    }

    public float getFontHeight() {
        return regular.getFontHeight();
    }

    public float getLifting() {
        return regular.getLifting();
    }

}
