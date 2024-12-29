//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package centric.pl.functions.api;

import centric.pl.Main;
import centric.pl.functions.impl.misc.ClientSounds;
import centric.pl.functions.settings.Setting;
import centric.pl.managers.notificationManager.Notification;
import centric.pl.johon0.utils.client.ClientUtil;
import centric.pl.johon0.utils.client.IMinecraft;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;

import net.minecraft.util.text.TextFormatting;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;

public abstract class Function implements IMinecraft {
    private final String name;
    private final Category category;
    private boolean state;
    private int bind;
    private boolean betafunction;
    private final List<Setting<?>> settings = new ObjectArrayList();
    private final Animation animation = new Animation();
    private Function Managed;

    public Function() {
        this.name = this.getClass().getAnnotation(FunctionRegister.class).name();
        this.category = this.getClass().getAnnotation(FunctionRegister.class).type();
        this.bind = this.getClass().getAnnotation(FunctionRegister.class).key();
        this.betafunction = this.getClass().getAnnotation(FunctionRegister.class).beta();
    }

    public Function(String name) {
        this.name = name;
        this.category = Category.Combat;
    }

    public void addSettings(Setting<?>... settings) {
        this.settings.addAll(List.of(settings));
    }

    public void onEnable() {
        this.animation.animate(1.0, 0.25, Easings.QUINT_IN);
        Main.getInstance().getEventBus().register(this);
    }

    public void onDisable() {
        this.animation.animate(0.0, 0.25, Easings.SINE_BOTH);
        Main.getInstance().getEventBus().unregister(this);
    }

    public final void toggle() {
        if (mc.player != null) {
            if (mc.world != null) {
                this.state = !this.state;
                if (!this.state) {
                    this.onDisable();
                    if (Main.getInstance().getFunctionRegistry().getNotification().functionnotif.get() && Main.getInstance().getFunctionRegistry().getNotification().isState()) {
                        Main.getInstance().getNotification().add("Функция " + this.name + " была выключена.", "", 5, Notification.Type.unsucces);

                    }
                } else {
                    this.onEnable();
                    if (Main.getInstance().getFunctionRegistry().getNotification().functionnotif.get() && Main.getInstance().getFunctionRegistry().getNotification().isState()) {
                        Main.getInstance().getNotification().add("Функция " + this.name + " была включена.", "", 5, Notification.Type.success);
                    }
                }

                FunctionRegistry functionRegistry = Main.getInstance().getFunctionRegistry();
                ClientSounds clientSounds = functionRegistry.getClientSounds();
                if (clientSounds != null && clientSounds.isState()) {
                    String fileName = clientSounds.getFileName(this.state);
                    float volume = clientSounds.volume.get();
                    ClientUtil.playSound(fileName, volume, false);
                }
            }
        }

    }

    public final void setState(boolean newState, boolean config) {
        if (this.state != newState) {
            this.state = newState;

            try {
                if (this.state) {
                    this.onEnable();
                } else {
                    this.onDisable();
                }

                if (!config) {
                    FunctionRegistry functionRegistry = Main.getInstance().getFunctionRegistry();
                    ClientSounds clientSounds = functionRegistry.getClientSounds();
                    if (clientSounds != null && clientSounds.isState()) {
                        String fileName = clientSounds.getFileName(this.state);
                        float volume = (Float)clientSounds.volume.get();
                        ClientUtil.playSound(fileName, volume, false);
                    }
                }
            } catch (Exception var7) {
                this.handleException(this.state ? "onEnable" : "onDisable", var7);
            }

        }
    }
    public void setState(final boolean enabled) {
        if (mc.player == null || mc.world == null) {
            return;
        }
        if (!enabled)
            this.onDisable();
        else
            this.onEnable();

        state = enabled;
    }
    private void handleException(String methodName, Exception e) {
        if (mc.player != null) {
            this.print("[" + this.name + "] Произошла ошибка в методе " + TextFormatting.RED + methodName + TextFormatting.WHITE + "() Предоставьте это сообщение разработчику: " + TextFormatting.GRAY + e.getMessage());
            e.printStackTrace();
        } else {
            System.out.println("[" + this.name + " Error" + methodName + "() Message: " + e.getMessage());
            e.printStackTrace();
        }

    }

    public String getName() {
        return this.name;
    }

    public Category getCategory() {
        return this.category;
    }

    public boolean isState() {
        return this.state;
    }

    public int getBind() {
        return this.bind;
    }
    public boolean getBeta() {
        return this.betafunction;
    }

    public List<Setting<?>> getSettings() {
        return this.settings;
    }

    public Animation getAnimation() {
        return this.animation;
    }

    public void setBind(int bind) {
        this.bind = bind;
    }
}
