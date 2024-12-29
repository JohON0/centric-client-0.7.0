package centric.pl.functions.impl.misc;

import centric.pl.Main;
import centric.pl.events.impl.EventMotion;
import centric.pl.events.impl.EventUpdate;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
//import centric.pl.functions.impl.combat.KillAura;
import centric.pl.functions.impl.combat.KillAura;
import centric.pl.functions.settings.impl.BooleanSetting;
import centric.pl.functions.settings.impl.SliderSetting;
import centric.pl.managers.notificationManager.Notification;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.settings.PointOfView;

@FunctionRegister(name = "FreeLook", type = Category.Misc, beta = true)
public class FreeLook extends Function {
    public BooleanSetting free = new BooleanSetting("Свободная камера", false);
    public BooleanSetting cameraViewer = new BooleanSetting("Просмотр камеры", true);
    public SliderSetting position = new SliderSetting("Положение", 0, -5,5,1).setVisible(() -> cameraViewer.get());
    public FreeLook() {
        addSettings(free,cameraViewer,position);
    }

    private float Yaw, Pitch;


    @Override
    public void onDisable(){
        if(isFree()) {
            mc.player.rotationYawOffset = Integer.MIN_VALUE;
            mc.gameSettings.setPointOfView(PointOfView.FIRST_PERSON);
            mc.player.rotationYaw = Yaw;
            mc.player.rotationPitch = Pitch;
        }
        super.onDisable();
    }
    @Override
    public void onEnable(){
        if(isFree()) {
            Yaw = mc.player.rotationYaw;
            Pitch = mc.player.rotationPitch;
        }
        super.onEnable();
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        KillAura aura = Main.getInstance().getFunctionRegistry().getHitAura();
        if (free.get()) {
            if (! aura.isState() && aura.getTarget() == null) {
                mc.gameSettings.setPointOfView(PointOfView.THIRD_PERSON_BACK);
                mc.player.rotationYawOffset = Yaw;
            } else {
                Main.getInstance().getNotification().add("FreeLook нельзя использовать с KillAura","", 5, Notification.Type.warning);
                this.setState(false, false);
            }
        }
    }

    @Subscribe
    public void onMotion(EventMotion e){
        if(free.get()) {
            e.setYaw(Yaw);
            e.setPitch(Pitch);
            e.setOnGround(mc.player.isOnGround());
            mc.player.rotationYawHead = mc.player.rotationYawOffset;
            mc.player.renderYawOffset = mc.player.rotationYawOffset;
            mc.player.rotationPitchHead = Pitch;
        }
    }

    public boolean isFree(){
        return free.get();
    }
}