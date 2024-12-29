package centric.pl.johon0.utils.animations;

public class GifUtil {
    public int getFrame(int totalFrames, int frameDelay, boolean countFromZero) {
        long currentTime = System.currentTimeMillis();
        int i;
        i = (int) ((currentTime / frameDelay) % totalFrames) + (countFromZero ? 0 : 1);
        return i;
    }
}
