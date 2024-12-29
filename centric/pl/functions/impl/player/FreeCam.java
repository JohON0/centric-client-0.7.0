package centric.pl.functions.impl.player;

import centric.pl.events.impl.EventPacket;
import centric.pl.events.impl.EventUpdate;
import centric.pl.events.impl.EventWorldChange;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.settings.impl.SliderSetting;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.player.MoveUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.client.gui.screen.DownloadTerrainScreen;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CPlayerAbilitiesPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.network.play.server.SRespawnPacket;
import net.minecraft.util.math.vector.Vector3d;

@FunctionRegister(name = "FreeCam", type = Category.Player, beta = false)
public class FreeCam extends Function {

    private final SliderSetting speed = new SliderSetting("Скорость по XZ", 2.0f, 0.1f, 5.0f, 0.05f);

    public FreeCam() {
        addSettings(speed);
    }

    private Vector3d clientPosition = new Vector3d(0, 0, 0);
    public RemoteClientPlayerEntity fakePlayer = null;

    @Subscribe
    public void onWorldChange(EventWorldChange e) {
        toggle();
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        IPacket<?> packet = e.getPacket();

        if (IMinecraft.mc.player == null || !IMinecraft.mc.player.isAlive() || IMinecraft.mc.world == null) {
            this.toggle();
            return;
        }

        if (e.isSend()) {
            if (packet instanceof CPlayerPacket || packet instanceof CPlayerAbilitiesPacket) {
                e.cancel();
            }
        }

        if (e.isReceive()) {
            if (packet instanceof SPlayerPositionLookPacket) {
                e.cancel();
            }
            if (packet instanceof SRespawnPacket) {
                toggle();
            }
        }
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (IMinecraft.mc.currentScreen instanceof DownloadTerrainScreen) {
            toggle();
            return;
        }

        if (IMinecraft.mc.player == null || !IMinecraft.mc.player.isAlive() || IMinecraft.mc.world == null) {
            this.toggle();
            return;
        }

        if (IMinecraft.mc.gameSettings.keyBindSneak.isKeyDown()) {
            IMinecraft.mc.player.motion.y = -speed.get();
        } else if (IMinecraft.mc.gameSettings.keyBindJump.isKeyDown()) {
            IMinecraft.mc.player.motion.y = speed.get();
        } else {
            IMinecraft.mc.player.setMotion(0.0, 0.0, 0.0);
        }
        MoveUtils.setMotion(speed.get());
    }

    @Override
    public void onEnable() {
        if (IMinecraft.mc.player == null || !IMinecraft.mc.player.isAlive() || IMinecraft.mc.world == null) {
            return;
        }
        this.clientPosition = IMinecraft.mc.player.getPositionVec();
        fakePlayer = new RemoteClientPlayerEntity(IMinecraft.mc.world, IMinecraft.mc.player.getGameProfile());
        fakePlayer.inventory = IMinecraft.mc.player.inventory;
        fakePlayer.setHealth(IMinecraft.mc.player.getHealth());
        fakePlayer.setPositionAndRotation(this.clientPosition.x, IMinecraft.mc.player.getBoundingBox().minY, this.clientPosition.z, IMinecraft.mc.player.rotationYaw, IMinecraft.mc.player.rotationPitch);
        fakePlayer.rotationYawHead = IMinecraft.mc.player.rotationYawHead;
        fakePlayer.rotationPitchHead = IMinecraft.mc.player.rotationPitchHead;
        IMinecraft.mc.world.addEntity(-7287138, fakePlayer);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (IMinecraft.mc.player == null || !IMinecraft.mc.player.isAlive() || IMinecraft.mc.world == null) {
            return;
        }
        IMinecraft.mc.player.setMotion(0.0, 0.0, 0.0);
        IMinecraft.mc.player.setVelocity(0.0, 0.0, 0.0);
        IMinecraft.mc.player.setPosition(this.clientPosition.x, this.clientPosition.y, this.clientPosition.z);
        IMinecraft.mc.world.removeEntityFromWorld(-7287138);
        super.onDisable();
    }
}
