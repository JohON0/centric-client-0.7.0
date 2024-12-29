package centric.pl.managers.styleManager;

import centric.pl.johon0.utils.render.ColorUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.awt.*;

@AllArgsConstructor
@Data
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Style {
    String styleName;
    Color firstColor;
    Color secondColor;

    public int getColor(int index) {
        return ColorUtils.gradient(firstColor.getRGB(), secondColor.getRGB(), index, 15);
    }
}
