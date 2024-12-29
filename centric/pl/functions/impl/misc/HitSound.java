package centric.pl.functions.impl.misc;

import com.google.common.eventbus.Subscribe;
import centric.pl.events.impl.AttackEvent;
import centric.pl.functions.settings.impl.ModeSetting;
import centric.pl.functions.settings.impl.SliderSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Random;

import static java.lang.Math.*;
import static java.lang.Math.signum;
import static net.minecraft.util.math.MathHelper.wrapDegrees;

@FunctionRegister(name = "HitSound", type = Category.Misc, beta = false)
public class HitSound extends Function {

    private final ModeSetting sound = new ModeSetting("Звук",
            "skeet",
            "motherglebxman", "uwu", "marker"
    );
    SliderSetting volume = new SliderSetting("Громкость", 35.0f, 5.0f, 100.0f, 5.0f);
    Random random = new Random();
    public HitSound() {
        addSettings(sound, volume);
    }

    @Subscribe
    public void onPacket(AttackEvent e) {
        if (mc.player == null || mc.world == null) return;
        playSound(e.entity);
    }

    public void playSound(Entity e) {
        try {
            Clip clip = AudioSystem.getClip();
            InputStream is;
            int randomNumber = random.nextInt(4) + 1;
            if (sound.is("motherglebxman")) {
                is = mc.getResourceManager().getResource(new ResourceLocation("centric/sounds/hitsounds/" + "moan" + randomNumber + ".wav")).getInputStream();
            } else {
                is = mc.getResourceManager().getResource(new ResourceLocation("centric/sounds/hitsounds/" + sound.get() + ".wav")).getInputStream();
            }
            BufferedInputStream bis = new BufferedInputStream(is);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bis);
            if (audioInputStream == null) {
                System.out.println("Sound not found!");
                return;
            }
            clip.open(audioInputStream);
            clip.start();

            FloatControl floatControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            if (e != null) {
                FloatControl balance = (FloatControl) clip.getControl(FloatControl.Type.BALANCE);
                Vector3d vec = e.getPositionVec().subtract(Minecraft.getInstance().player.getPositionVec());


                double yaw = wrapDegrees(toDegrees(atan2(vec.z, vec.x)) - 90);
                double delta = wrapDegrees(yaw - mc.player.rotationYaw);

                if (abs(delta) > 180) delta -= signum(delta) * 360;
                try {
                    balance.setValue((float) delta / 180);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            floatControl.setValue(-(mc.player.getDistance(e) * 5) - (volume.max / volume.get()));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
