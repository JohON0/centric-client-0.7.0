package centric.pl.functions.impl.combat.killAura;

import centric.pl.johon0.utils.client.IMinecraft;

public class ClickScheduler implements IMinecraft {
    int clickTicks = 0;

    public ClickScheduler() {
        clickTicks = 0;
    }

    public boolean readyToAttack() {
        return mc.player.getCooledAttackStrength(1.5F) >= 0.91F;
    }

    public void cleanup() {
        clickTicks = 0;
    }

    public void update() {
        if (clickTicks > 0) {
            clickTicks--;
        }
    }

    public boolean goingToClick() {
        return clickTicks == 0;
    }

    public void resetCounter() {
        clickTicks = 10;
    }
}
