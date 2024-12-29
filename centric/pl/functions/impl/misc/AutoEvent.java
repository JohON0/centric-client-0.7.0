package centric.pl.functions.impl.misc;

import centric.pl.events.impl.EventPacket;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.settings.impl.BooleanSetting;
import centric.pl.functions.settings.impl.ModeListSetting;
import centric.pl.johon0.utils.client.ClientUtil;
import com.google.common.eventbus.Subscribe;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.util.text.TextFormatting;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@FunctionRegister(
        name = "AutoGPS",
        type = Category.Misc,
        beta = false
)
public class AutoEvent extends Function {
    private final BooleanSetting autoGps = new BooleanSetting("Авто точка", true);
    private final ModeListSetting typeGps = (new ModeListSetting("Просматривать: ", new BooleanSetting("/event delay", true), new BooleanSetting("Смерть", true), new BooleanSetting("Чат", true))).setVisible(() -> this.autoGps.get());

    public AutoEvent() {
        this.addSettings(this.autoGps, this.typeGps);
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (mc.player != null) {
            if (mc.world != null) {
                if (this.autoGps.get()) {
                    IPacket packet = e.getPacket();
                    if (packet instanceof SChatPacket) {
                        SChatPacket p = (SChatPacket)packet;
                        String raw = p.getChatComponent().getString().toLowerCase(Locale.ROOT);
                        String coords;
                        if (raw.contains("координатах") && raw.contains("?????????????????????????") && (ClientUtil.isConnectedToServer("funtime") || ClientUtil.isConnectedToServer("funsky") || ClientUtil.isConnectedToServer("skytime") || ClientUtil.isConnectedToServer("spookytime") || ClientUtil.isConnectedToServer("holytime")) && this.typeGps.getValueByName("Чат").get()) {
                            coords = extractCoordinates(raw, Type.EVENT);
                            mc.player.sendChatMessage(".gps add Событие " + coords);
                            this.print(TextFormatting.GREEN + "Поставил точку \"Событие\" на " + coords);
                        }

                        if (raw.contains("координаты") && raw.contains("||") && (ClientUtil.isConnectedToServer("funtime") || ClientUtil.isConnectedToServer("funsky") || ClientUtil.isConnectedToServer("skytime") || ClientUtil.isConnectedToServer("spookytime") || ClientUtil.isConnectedToServer("holytime")) && (Boolean)this.typeGps.getValueByName("/event delay").get()) {
                            coords = extractCoordinates(raw, Type.EVENT);
                            mc.player.sendChatMessage(".gps add Событие " + coords);
                            this.print(TextFormatting.GREEN + "Поставил точку \"Событие\" на " + coords);
                        }

//                        if (raw.contains("вы погибли") && raw.contains("вас убил") && (ClientUtil.isConnectedToServer("funtime") || ClientUtil.isConnectedToServer("funsky") || ClientUtil.isConnectedToServer("skytime") || ClientUtil.isConnectedToServer("spookytime") || ClientUtil.isConnectedToServer("holytime")) && (Boolean)this.typeGps.getValueByName("Смерть").get()) {
                            coords = extractCoordinates(raw, Type.DEATH);
                            mc.player.sendChatMessage(".gps add Смерть " + coords);
                            print(coords);
                            this.print(TextFormatting.GREEN + "Поставил точку \"Смерть\" на " + coords);
                        //}
                    }
                }
            }
        }

    }

    public static String extractCoordinates(String input, Type type) {
        Pattern pattern;
        if (type == Type.EVENT) {
            pattern = Pattern.compile("\\[(-?\\d+)\\s+(-?\\d+)\\s+(-?\\d+)\\]");
        } else {
            if (type != Type.DEATH) {
                return null;
            }

            pattern = Pattern.compile("\\[(-?\\d+\\.\\d+),\\s*(-?\\d+\\.\\d+),\\s*(-?\\d+\\.\\d+)\\]");
        }

        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            if (type == Type.EVENT) {
                String group = matcher.group(1);
                return group + " " + matcher.group(2) + " " + matcher.group(3);
            }

            int x = (int) Math.floor(Double.parseDouble(matcher.group(1)));
            int y = (int)Math.floor(Double.parseDouble(matcher.group(2)));
            int z = (int)Math.floor(Double.parseDouble(matcher.group(3)));
            return "" + x + " " + y + " " + z;
        }

        return null;
    }
    public enum Type {
        EVENT,
        DEATH;

        Type() {
        }
    }
}

