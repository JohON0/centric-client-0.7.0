package centric.pl.managers.styleManager;


import java.awt.*;

public interface StyleFactory {
    Style createStyle(String name, Color firstColor, Color secondColor);
}
