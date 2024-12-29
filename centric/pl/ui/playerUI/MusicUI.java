package centric.pl.ui.playerUI;

import centric.pl.functions.impl.render.HUD;
import centric.pl.johon0.utils.animations.Animation;
import centric.pl.johon0.utils.animations.Direction;
import centric.pl.johon0.utils.animations.impl.EaseBackIn;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.font.Fonts;
import centric.pl.johon0.utils.math.MathUtil;
import centric.pl.johon0.utils.render.ColorUtils;
import centric.pl.johon0.utils.render.DisplayUtils;
import centric.pl.johon0.utils.render.Scissor;
import centric.pl.managers.styleManager.ThemeSwitcher;
import com.mojang.blaze3d.matrix.MatrixStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.FloatControl.Type;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;
import ru.hogoshi.util.Easings;

/**
 * @authors: JohON0 & XsK1LLz
 * @datefinished: 5.10.24
 * @clients: Centric && Churka
 */

public class MusicUI extends Screen implements IMinecraft {
    private List<File> musicFiles = new ArrayList();
    //Пусть с папкой музыки
    private File musicDirectory = new File("jre/music/");
    private String message = "Папка с музыкой пуста =(";
    public static final Animation openAnimation = new EaseBackIn(400, 1.0, 1.0F);
    public static ru.hogoshi.Animation animation = new ru.hogoshi.Animation();
    private float x;
    private float y;
    private float width;
    private float height;
    private int selectedMusicIndex = -1;
    private boolean isPlaying = false;
    private boolean isRepeat = false;
    private Clip clip;
    private float volume = 0.3F;
    private int scrollOffset = 0;
    private int maxVisibleItems = 10;
    private int maxScroll;
    private float itemHeight = 20.0F;
    private Category currentCategory;

    public MusicUI(ITextComponent titleIn) {
        super(titleIn);
        this.currentCategory = Category.LOCAL;
    }

    protected void init() {

        animation = animation.animate(1.0, 0.25, Easings.EXPO_OUT);
        //animation opened by johon0<3
        openAnimation.setDirection(Direction.FORWARDS);
        super.init();
        this.loadMusicFiles();
    }

    private void loadMusicFiles() {
        this.musicFiles.clear();

        try {
            if (this.currentCategory == Category.LOCAL) {
                if (this.musicDirectory.exists() && this.musicDirectory.isDirectory()) {
                    File[] files = this.musicDirectory.listFiles((dir, name) -> name.endsWith(".wav"));
                    if (files != null) {
                        File[] var2 = files;
                        int var3 = files.length;

                        for (int var4 = 0; var4 < var3; ++var4) {
                            File file = var2[var4];
                            this.musicFiles.add(file);
                        }
                    }
                }

                if (!this.musicDirectory.exists()) {
                    this.musicDirectory.mkdir();
                }

                if (this.musicFiles.isEmpty()) {
                    this.message = "Папка с музыкой пуста =(\n";
                }
            }
        } catch (Exception var6) {
            var6.printStackTrace();
            this.message = "Произошла ошибка при загрузке треков.";
            this.musicFiles.clear();
        }

    }
    /**
     * Рендер UI
     */
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);

        int windowWidth = mc.getMainWindow().getScaledWidth();
        int windowHeight = mc.getMainWindow().getScaledHeight();
        width = 250.0F;
        height = 250.0F;
        x = (float) windowWidth / 2.0F - width / 2.0F;
        y = (float) windowHeight / 2.0F - height / 2.0F;
        GlStateManager.pushMatrix();
        //Анимация открытия
        //sizeAnimation(x + (width / 2), y + (height / 2), openAnimation.getOutput());
        sizeAnimation(x + (width / 2), y + (height / 2), animation.getValue());
        animation.update();
        if (animation.getValue() < 0.1) {
            this.closeScreen();
        }

        //Главный рект
        DisplayUtils.drawShadow(x, y, width, height, 10, ColorUtils.rgba(10, 10, 10, 255));
        DisplayUtils.drawRoundedRect(x, y, width, height, 7.0F, ColorUtils.rgba(10, 10, 10, 255));

        //Полоска нахуй
        DisplayUtils.drawImage(new ResourceLocation("centric/images/gradline.png"), x, y + 35, width, 1, -1);
//        DisplayUtils.renderClientLines(matrixStack, x, y + 35, width, 1, -1, 0);

        //Текст
        Fonts.notoitalic[20].drawCenteredString(matrixStack, "MusicPlayer UI", x + width / 2.0F, y + 10.0F, ColorUtils.rgb(255, 255, 255));
        Fonts.notoitalic[12].drawCenteredString(matrixStack, "made by Churka Client (XsK1LLz) && centric dlc (johon0)", x + width / 2.0F, y + 25.0F, ColorUtils.rgb(255, 255, 255));

        //Кнопка открытия папки с музыкой
        float openFolderX = x + width - 65.0F + 1.0F;
        float openFolderY = y + 8.0F;
        int colorfolder = ColorUtils.rgb(200,200,200);
        boolean hoveredfolder = MathUtil.isInRegion(mouseX,mouseY,openFolderX + 35.0F, openFolderY, 20.0F, 20.0F);
        if (hoveredfolder) {
            colorfolder = ColorUtils.rgb(255,255,255);
        }
        DisplayUtils.drawRoundedRect(openFolderX + 35.0F, openFolderY, 20.0F, 20.0F, 5.0F, ColorUtils.rgba(12, 12, 12, 255));
        Fonts.iconsall[20].drawString(matrixStack,"X", openFolderX + 40.0F, openFolderY + 7.0F, colorfolder); // iconsall

        //Предупреждение, что нужно закинуьть музыку в папку
        if (this.musicFiles.isEmpty()) {
            String message2 = "чтобы добавить музыку, поместите её в папку";
            String message3 = "Чтобы открыть папку нажмите на папочку сверху интерфейса =)";
            Fonts.notoitalic[12].drawCenteredString(matrixStack, this.message, x + width / 2.0F, y + height / 2.0F - 30.0F, ColorUtils.rgb(255, 0, 0));
            Fonts.notoitalic[12].drawCenteredString(matrixStack, message2, x + width / 2.0F, y + height / 2.0F - 17.5F, ColorUtils.rgb(255, 0, 0));
            Fonts.notoitalic[12].drawCenteredString(matrixStack, message3, x + width / 2.0F, y + height / 2.0F - 5.0F, ColorUtils.rgb(255, 0, 0));
        }
        //Если музыка есть, тогда выводить её
        else {
            Scissor.push();
            //Scissor.setFromComponentCoordinates((double) x, (double) (y + 35.0F), (double) (width + 300.0F), (double) (height - 10.0F));
            Scissor.setFromComponentCoordinates((double) x, (double) (y + 35.0F), (double) (width + 300.0F), (double) (height + 130.0F));
            float listX = x + 20.0F;
            float listY = y + 32;
            int visibleItems = 10;
            maxScroll = Math.max(0, this.musicFiles.size() - visibleItems);
            int scrollOffset1 = Math.max(0, Math.min(this.scrollOffset, maxScroll));
            int startIndex = Math.max(0, scrollOffset1);
            int endIndex = Math.min(this.musicFiles.size(), startIndex + visibleItems);

            for (int i = startIndex; i < endIndex; ++i) {
                File music = this.musicFiles.get(i);
                boolean hovered = MathUtil.isInRegion((float) mouseX, (float) mouseY, listX, listY + (float) (i - startIndex) * this.itemHeight + 10.0F, width - 40.0F, 20);
                if (this.scrollOffset > 0) {
                    hovered = MathUtil.isInRegion((float) mouseX, (float) mouseY, listX, listY + (float) (i - startIndex) * this.itemHeight + 10.0F, width - 40.0F, 20);
                }

                String songName = this.trimSongName(music.getName().replaceAll("\\.wav", ""), width - 80.0F);
                int color = ColorUtils.rgb(15,15,15);
                if (hovered) {
                    color = ColorUtils.rgb(17,17,17);
                }
                if (this.selectedMusicIndex == i) {
                    color = ColorUtils.rgb(18,18,18);
                }

                DisplayUtils.drawRoundedRect(listX, listY + (float) (i - startIndex) * this.itemHeight + 10.0F, width - 40.0F, 20, 3.0F, color);
                Fonts.notoitalic[16].drawString(matrixStack, songName, listX + 5.0F, listY + (float) (i - startIndex) * this.itemHeight + 18.0F, ColorUtils.rgb(255, 255, 255));
            }

            Scissor.unset();
            Scissor.pop();
            this.renderPlayerPanel(matrixStack, x + width, y - height, width, height);
        }
        GlStateManager.popMatrix();
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
    //Сокращать название песни
    private String trimSongName(String name, float maxWidth) {
        float textWidth = Fonts.notoitalic[12].getWidth(name);
        if (textWidth > maxWidth) {
            while (true) {
                if (!(textWidth > maxWidth) || name.length() <= 3) {
                    name = name + "...";
                    break;
                }

                name = name.substring(0, name.length() - 1);
                textWidth = Fonts.notoitalic[12].getWidth(name + "...");
            }
        }

        return name;
    }
    //Рендер Панели проигрывателя
    private void renderPlayerPanel(MatrixStack matrixStack, float x, float y, float width, float height) {
        float panelHeight = 100.0F;
        float panelY = y + height - panelHeight+ 50;
        if (this.selectedMusicIndex != -1) {
            //Главный рект
            DisplayUtils.drawRoundedRect(x + 2.5f, panelY + 50.0F, 110, 50, 7, ColorUtils.rgba(10, 10, 10, 255));
            //Рендер кнопок и слайдеров
            this.renderPlayer(matrixStack, x + 11.5f, panelY + 10.0F, 110, 90);
        }
    }
    //Рендер кнопок и слайдеров
    private void renderPlayer(MatrixStack matrixStack, float x, float y, float width, float height) {
        //Рендер Имени песни
        //String songName = this.trimSongName(this.musicFiles.get(this.selectedMusicIndex).getName().replaceAll("\\.wav", ""), width-15);
        String songName = this.trimSongName(this.musicFiles.get(this.selectedMusicIndex).getName().replaceAll("\\.wav", ""), width-34);
        Fonts.notoitalic[13].drawString(matrixStack, songName, x - 5, y + height/2 + 2, ColorUtils.rgb(255, 255, 255));

        float buttonSize = 15;
        float prevButtonX = x - 3;
        float prevButtonY = y + height / 2.0F - buttonSize / 2.0F + 32.0F;
        // Кнопка переключения на предыдущую песню
        DisplayUtils.drawRoundedRect(prevButtonX, prevButtonY, buttonSize, buttonSize, 2, ColorUtils.rgba(15, 15, 15, 255));
        Fonts.musicfont[14].drawCenteredString(matrixStack, "C", prevButtonX + buttonSize / 2.0F, prevButtonY + 6.0F, ColorUtils.rgb(255, 255, 255));
        // Кнопка переключения паузы и плэй
        float playPauseButtonX = x + 10 + 5;
        float playPauseButtonY = y + height / 2.0F - buttonSize / 2.0F + 32.0F;
        DisplayUtils.drawRoundedRect(playPauseButtonX, playPauseButtonY, buttonSize, buttonSize,  2, ColorUtils.rgba(15, 15, 15, 255));
        Fonts.musicfont[14].drawCenteredString(matrixStack, this.isPlaying ? "A" : "X", playPauseButtonX + buttonSize / 2.0F+ 2, playPauseButtonY + 6.0F, ColorUtils.rgb(255, 255, 255));
        // Кнопка переключения на следущую песню
        float nextButtonX = x + 28 +5;
        float nextButtonY = y + height / 2.0F - buttonSize / 2.0F + 32.0F;
        DisplayUtils.drawRoundedRect(nextButtonX, nextButtonY, buttonSize, buttonSize, 2, ColorUtils.rgba(15, 15, 15, 255));
        Fonts.musicfont[14].drawCenteredString(matrixStack, "B", nextButtonX + buttonSize / 2.0F, nextButtonY + 6.0F, ColorUtils.rgb(255, 255, 255));
        // Кнопка повтора (не доделана)
        /**
         * Кнопка повтора
         */
        float repeatButtonX = nextButtonX + buttonSize - 20 + 55;
        float repeatButtonY =  nextButtonY - 27;
        int repeatButtonColor = this.isRepeat ? ColorUtils.rgba(0, 255, 0, 255) : ColorUtils.rgba(200, 200, 200, 255);
        DisplayUtils.drawRoundedRect(repeatButtonX, nextButtonY - 27, buttonSize, buttonSize - 1, 2, ColorUtils.rgba(15, 15, 15, 255));
        Fonts.musicfont[18].drawCenteredString(matrixStack, "U", repeatButtonX + buttonSize / 2.0F + 1, repeatButtonY + 5f, repeatButtonColor);
        //Слайдер Громкости
        float volumeSliderX = x + 28 + 25;
        float volumeSliderY = y + height / 2.0F - buttonSize / 2.0F + 36.0F;
        float volumeSliderWidth = 45;
        DisplayUtils.drawRoundedRect(volumeSliderX, volumeSliderY, volumeSliderWidth, 5, 2, ColorUtils.rgba(15, 15, 15, 255));
        DisplayUtils.drawRoundedRect(volumeSliderX, volumeSliderY, volumeSliderWidth * this.volume, 5, 2.0F,
                ColorUtils.gradient(HUD.getColor(100),HUD.getColor(200),100,5));
        //Слайдер Перемотки
        float timeSliderX = x - 5;
        float timeSliderY = y + height - 30.0f;
        float timeSliderWidth = width - 5;
        DisplayUtils.drawRoundedRect(timeSliderX, timeSliderY, timeSliderWidth, 5.0F, 2F, ColorUtils.rgba(15, 15, 15, 255));
        DisplayUtils.drawRoundedRect(timeSliderX, timeSliderY, timeSliderWidth * this.getCurrentPlaybackPosition(), 5.0F, 2F,
                ColorUtils.gradient(HUD.getColor(100),HUD.getColor(200),100,5));
    }

    public float getCurrentPlaybackPosition() {
        return this.clip != null ? (float) this.clip.getMicrosecondPosition() / (float) this.clip.getMicrosecondLength() : 0.0F;
    }


    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            float panelX = x + width + 10;
            float panelY = y - height + 5;
            float buttonSize = 15;
            float prevButtonX = panelX - 3;
            float prevButtonY = panelY + height - buttonSize / 2.0F + 32.0F;
            //выбор музыки
            if (MathUtil.isInRegion((float) mouseX, (float) mouseY, prevButtonX, prevButtonY, buttonSize, buttonSize)) {
                this.selectPreviousMusic();
                return true;
            }

            //MouseClicked Проигрывателя

            // Кнопка переключения паузы и плэй
            float playPauseButtonX = panelX + 10 + 5;
            float playPauseButtonY = panelY + height - buttonSize / 2.0F + 32.0F;
            if (MathUtil.isInRegion((float) mouseX, (float) mouseY, playPauseButtonX, playPauseButtonY, buttonSize, buttonSize)) {
                this.togglePlayPause();
                return true;
            }

            // Кнопка переключения на следующую песню
            float nextButtonX = panelX + 28 + 5;
            float nextButtonY = panelY + height - buttonSize / 2.0F + 32.0F;
            if (MathUtil.isInRegion((float) mouseX, (float) mouseY, nextButtonX, nextButtonY, buttonSize, buttonSize)) {
                this.selectNextMusic();
                return true;
            }

            // Слайдер переключения громкости
            float volumeSliderX = panelX + 28 + 25;
            float volumeSliderY = panelY + height - buttonSize / 2.0F + 36.0F;
            float volumeSliderWidth = 45;

            if (MathUtil.isInRegion((float) mouseX, (float) mouseY, volumeSliderX, volumeSliderY, volumeSliderWidth, 10.0F)) {
                this.volume = (float) ((mouseX - (double) volumeSliderX) / (double) volumeSliderWidth);
                this.setVolume(this.volume);
                return true;
            }

            // Слайдер перемотки времени
            float timeSliderX = panelX -5;
            float timeSliderY = panelY + height - buttonSize / 2.0F + 24.0F;
            float timeSliderWidth = 110 - 5;
            if (MathUtil.isInRegion((float) mouseX, (float) mouseY, timeSliderX, timeSliderY, timeSliderWidth, 5.0F)) {
                System.out.println(1);
                this.seek((float) ((mouseX - (double) timeSliderX) / (double) timeSliderWidth));
                return true;
            }

            // Кнопка Открытия папки с песнями
            float openFolderX = x + width - 30.0F + 1.5F;
            float openFolderY = y + 8.0F;
            if (MathUtil.isInRegion((float) mouseX, (float) mouseY, openFolderX, openFolderY, 20.0F, 20.0F)) {
                this.openMusicFolder();
                return true;
            }

            float listY = y + 35.0F;
            float listX = x + 20.0F;
            int visibleItems = 10;
            int maxScroll = Math.max(0, this.musicFiles.size() - visibleItems);
            int scrollOffset1 = Math.max(0, Math.min(this.scrollOffset, maxScroll));
            int startIndex = Math.max(0, scrollOffset1);
            int endIndex = Math.min(this.musicFiles.size(), startIndex + visibleItems);


            for (int i = 0; i < this.musicFiles.size(); ++i) {
                if (MathUtil.isInRegion((float) mouseX, (float) mouseY, listX, listY + (float) (i - startIndex) * this.itemHeight + 10.0F, width - 40.0F, 20) && this.scrollOffset == 0) {
                    this.selectedMusicIndex = i;
                    this.playSelectedMusic();
                    return true;
                }
            }

//            float closeButtonX = x + width / 2.0F - buttonSize / 2.0F - 60.0F;
//            if (MathUtil.isInRegion((float) mouseX, (float) mouseY, closeButtonX, prevButtonY, buttonSize, buttonSize)) {
//                this.clip.stop();
//                this.clip.close();
//                this.clip = null;
//                this.selectedMusicIndex = -1;
//                return true;
//            }

            for (int i = startIndex; i < endIndex; ++i) {
                if (MathUtil.isInRegion((float) mouseX, (float) mouseY, listX, listY + (float) (i - startIndex) * this.itemHeight + 10.0F, width - 40.0F, 20) && this.scrollOffset > 0) {
                    this.selectedMusicIndex = i;
                    this.playSelectedMusic();
                    return true;
                }
            }

            float repeatButtonX = nextButtonX + buttonSize - 20 + 55;
            float repeatButtonY = nextButtonY - 27;
            if (MathUtil.isInRegion((float) mouseX, (float) mouseY, repeatButtonX, repeatButtonY, buttonSize, buttonSize)) {
                isRepeat = !isRepeat;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }


    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

        // Закрытие
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            animation = animation.animate(0.0, 0.25, Easings.EXPO_OUT);
            openAnimation.setDirection(Direction.BACKWARDS);
            return false;
        }
        else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        //Scroll (говно ебаное Даня спаси)
        int maxOffset = this.musicFiles.size() - this.maxVisibleItems;
        if (maxOffset > 0) {
            this.scrollOffset = (int) MathUtil.lerp((double) (this.scrollOffset - (int) delta), 0.0, (double) maxOffset);
        }
        if (maxScroll > height - 30.0f) {
            if (MathUtil.isInRegion((float)mouseX, (float)mouseY, x, y, width, height)) {
                this.scrollOffset += (float)(delta * 16.0);
            }
        }
        this.handleMouseWheel((int) delta);
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
    public void handleMouseWheel(int delta) {
        this.scrollOffset -= delta * 3;
        this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, Math.max(0, this.musicFiles.size() - 10)));
    }

    private void selectPreviousMusic() {
        if (!this.musicFiles.isEmpty()) {
            if (this.selectedMusicIndex > 0) {
                --this.selectedMusicIndex;
            } else {
                this.selectedMusicIndex = this.musicFiles.size() - 1;
            }

            this.playSelectedMusic();
        }
    }

    private void selectNextMusic() {
        if (!this.musicFiles.isEmpty()) {
            if (this.selectedMusicIndex < this.musicFiles.size() - 1) {
                ++this.selectedMusicIndex;
            } else {
                this.selectedMusicIndex = 0;
            }

            this.playSelectedMusic();
        }
    }

    private void togglePlayPause() {
        if (this.clip != null) {
            if (this.isPlaying) {
                this.clip.stop();
                this.isPlaying = false;
            } else {
                this.clip.setFramePosition(this.clip.getFramePosition());
                this.clip.start();
                this.isPlaying = true;
            }

        }
    }

    private void openMusicFolder() {
        try {
            Runtime.getRuntime().exec("explorer " + this.musicDirectory.getAbsolutePath());
        } catch (IOException var2) {
            var2.printStackTrace();
        }

    }

    private void setVolume(float volume) {
        if (this.clip != null && this.clip.isControlSupported(Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) this.clip.getControl(Type.MASTER_GAIN);
            float dB = (float) (Math.log((double) volume) / Math.log(10.0) * 20.0);
            gainControl.setValue(dB);
        }

    }

    private void seek(float position) {
        if (this.clip != null) {
            this.clip.setMicrosecondPosition((long) ((float) this.clip.getMicrosecondLength() * position));
        }

    }

    public boolean isVisible() {
        return this.minecraft.currentScreen == this;
    }

    public boolean isPlaying() {
        return this.isPlaying;
    }

    private void playSelectedMusic() {
        if (this.selectedMusicIndex != -1 && !this.musicFiles.isEmpty()) {
            if (this.clip != null && this.clip.isOpen()) {
                this.clip.stop();
                this.clip.close();
            }

            File music = this.musicFiles.get(this.selectedMusicIndex);

            try {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(music);
                this.clip = AudioSystem.getClip();
                this.clip.open(audioInputStream);
                this.setVolume(this.volume);
                this.isPlaying = true;
                this.clip.addLineListener((event) -> {
                    if (event.getType() == javax.sound.sampled.LineEvent.Type.STOP && this.isPlaying && this.clip.getMicrosecondPosition() >= this.clip.getMicrosecondLength()) {
                        if (this.isRepeat) {
                            this.clip.setFramePosition(0);
                            this.clip.start();
                        } else {
                            this.selectNextMusic();
                        }
                    }

                });
                this.clip.start();
            } catch (IOException | LineUnavailableException | UnsupportedAudioFileException var3) {
                var3.printStackTrace();
            }

        }
    }
    public static void sizeAnimation(double width, double height, double scale) {
        GlStateManager.translated(width, height, 0.0);
        GlStateManager.scaled(scale, scale, scale);
        GlStateManager.translated(-width, -height, 0.0);
    }

    public boolean isPauseScreen() {
        return false;
    }

    enum Category {
        LOCAL;
        Category() {
        }
    }
}
