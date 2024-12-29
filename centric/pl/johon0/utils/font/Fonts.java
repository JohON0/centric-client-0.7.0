package centric.pl.johon0.utils.font;

import centric.pl.johon0.utils.font.common.Lang;
import centric.pl.johon0.utils.font.styled.StyledFont;
import lombok.SneakyThrows;


public class Fonts {
    public static final String FONT_DIR = "/assets/minecraft/centric/font/";

    public static volatile StyledFont[] notoitalic = new StyledFont[131];
    public static volatile StyledFont[] centricbold = new StyledFont[131];
    public static volatile StyledFont[] iconsall = new StyledFont[131];
    public static volatile StyledFont[] roboto = new StyledFont[131];
    public static volatile StyledFont[] musicfont = new StyledFont[50];

    @SneakyThrows
    public static void init() {
        long time = System.currentTimeMillis();
        for (int i = 10; i < 131;i++) {
            iconsall[i] = new StyledFont("iconscentric.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 10; i < 131;i++) {
            notoitalic[i] = new StyledFont("bold.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 10; i < 131;i++) {
            centricbold[i] = new StyledFont("unbounded_bold.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 10; i < 131;i++) {
            roboto[i] = new StyledFont("Roboto-Medium.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 10; i < 49;i++) {
            musicfont[i] = new StyledFont("musicicons.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        System.out.println("Ўрифты загрузились за: " + (System.currentTimeMillis() - time) + " миллисекунд");

        //fontThread.shutdown();
    }
}