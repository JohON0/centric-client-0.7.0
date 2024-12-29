package centric.pl.managers.styleManager;

import centric.pl.johon0.utils.render.ColorUtils;

public class ThemeSwitcher {
    public static boolean themelightofdark;
    public static int textcolor;
    public static int textcolorcom;
    public static int backgroundcolor;
    public static int buttoncolor;
    public static int bgcolor;
    public static int circlecolor;
    public static int modesetting;
    //цвета главного меню
    public static int backgroundmenu;
    public static int buttonmenucolor;
    //altmanager
    public static int altbgcolor;
    public static int altselect;
    public static int altnoselect;

    public ThemeSwitcher(boolean themelightofdark) {
        setTheme(themelightofdark);
    }

    public static void setTheme(boolean n) {
        themelightofdark = n;
        if (themelightofdark) {
            textcolor = -1; // Белый текст
            textcolorcom = ColorUtils.rgba(140, 140, 140, 255) ; // Белый текст

            backgroundcolor = ColorUtils.rgba(10, 10, 10, 245);
            bgcolor = ColorUtils.rgba(15, 15, 15, 245);
            circlecolor = -1; // Белый круг
            buttoncolor = ColorUtils.rgba(23, 23, 23, 255);
            modesetting = ColorUtils.rgba(35,35,35,255);
            //цвета главного меню
            backgroundmenu = ColorUtils.rgba(12,11,14,255);
            buttonmenucolor = ColorUtils.rgba(15,17,18,255);
            //altmanager
            altbgcolor = ColorUtils.rgba(20,22,23,255);
            altnoselect = ColorUtils.rgba(15,15,15,255);
            altselect = ColorUtils.rgba(35,35,35,255);




        } else {
            textcolor = ColorUtils.rgba(0, 0, 0, 255); // Черный текст
            backgroundcolor = ColorUtils.rgba(255, 255, 255, 255); // Белый фон
            bgcolor = ColorUtils.rgba(240, 240, 240, 255); // Светло-серый фон
            circlecolor = ColorUtils.rgba(0, 0, 0, 255); // Черный круг
            buttoncolor = ColorUtils.rgba(220, 220, 220, 255); // Светло-серый цвет кнопки
            modesetting = ColorUtils.rgba(120,120,120,255); // мод-компонент серый цвет
            //цвета главного меню
            backgroundmenu = ColorUtils.rgba(145,145,145,255);
            buttonmenucolor = ColorUtils.rgba(199,199,199,255);
            altbgcolor = ColorUtils.rgba(240, 240, 240, 255); // Светлый фон
            altnoselect = ColorUtils.rgba(220, 220, 220, 255); // Цвет невыбранного элемента
            altselect = ColorUtils.rgba(180, 180, 180, 255); // Цвет выбранного элемента

        }
    }
}
