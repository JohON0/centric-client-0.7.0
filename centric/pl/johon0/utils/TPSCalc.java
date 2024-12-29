package centric.pl.johon0.utils;

import centric.pl.events.impl.EventPacket;
import com.google.common.eventbus.Subscribe;
import centric.pl.Main;
import lombok.Getter;
import net.minecraft.network.play.server.SUpdateTimePacket;
import net.minecraft.util.math.MathHelper;

@Getter
public class TPSCalc {

    private float TPS = 20;
    private float adjustTicks = 0;

    private long timestamp;

    public TPSCalc() {
        Main.getInstance().getEventBus().register(this);
    }

    @Subscribe
    private void onPacket(EventPacket e) {
        if (e.getPacket() instanceof SUpdateTimePacket) {
            updateTPS();
        }
    }

    private void updateTPS() {
        long delay = System.nanoTime() - timestamp;

        float maxTPS = 20;
        float rawTPS = maxTPS * (1e9f / delay);

        float boundedTPS = MathHelper.clamp(rawTPS, 0, maxTPS);

        TPS = (float) round(boundedTPS);

        adjustTicks = boundedTPS - maxTPS;

        timestamp = System.nanoTime();
    }

    public double round(
            final double input
    ) {
        return Math.round(input * 100.0) / 100.0;
    }
}
