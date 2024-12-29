package centric.pl.functions.impl.render;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.managers.notificationManager.Notification.Type;
import centric.pl.Main;
import centric.pl.events.impl.EventPacket;
import centric.pl.functions.api.Function;
import centric.pl.functions.settings.impl.BooleanSetting;
import centric.pl.functions.settings.impl.StringSetting;
import com.google.common.eventbus.Subscribe;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;
import java.util.Locale;

@FunctionRegister(name = "Notification", type = Category.Render, beta = false)
public class Notification extends Function {
    public StringSetting name = new StringSetting("������ �� ������", "", "������� ������� �� �������� ����� ��������� �����������");
    public BooleanSetting specnotif = new BooleanSetting("����������� � �����", false);
    public BooleanSetting warpnotif = new BooleanSetting("����������� � �����", false);
    public BooleanSetting functionnotif = new BooleanSetting("����������� ������", true);
    private static final String[] SPECTEXTS = {"Spec", "����", "spec", "����", "SPEC", "����", "����"};
    private static final String[] WARPTEXTS = {"/Warp", "/����", "warp", "����", "WARP", "����", "����"};
    public Notification() {
        addSettings(name, specnotif,warpnotif,functionnotif);
    }

    @Subscribe
    public void onEvent(EventPacket packetEvent) {
        if (packetEvent.isReceive() && packetEvent.getPacket() instanceof SChatPacket packetChat) {
            handleReceivePacket(packetChat);
        }
    }

    private void handleReceivePacket(SChatPacket packet) {
        String originalMessage = packet.getChatComponent().getString();
        String formattedMessage = TextFormatting.getTextWithoutFormattingCodes(originalMessage);

        String lowerCaseMessage = originalMessage.toLowerCase(Locale.ROOT);

        if (isSpecMessage(lowerCaseMessage)) {
            if (specnotif.get()) {
                renderSpecNotification(formattedMessage);
            }
        }
        if (isWarpMessage(lowerCaseMessage)) {
            if (warpnotif.get()) {
                renderWarpNotification(formattedMessage);
            }
        }
    }

    private boolean isSpecMessage(String lowerCaseMessage) {
        return Arrays.stream(SPECTEXTS)
                .map(String::toLowerCase)
                .anyMatch(lowerCaseMessage::contains);
    }

    private boolean isWarpMessage(String lowerCaseMessage) {
        return Arrays.stream(WARPTEXTS)
                .map(String::toLowerCase)
                .anyMatch(lowerCaseMessage::contains);
    }


    private void renderSpecNotification(String lowerCaseMessage) {
        Main.getInstance().getNotification().add(lowerCaseMessage + " ������ ���������� �� ���!", "", 5, Type.spec);
    }

    private void renderWarpNotification(String lowerCaseMessage) {
        Main.getInstance().getNotification().add(lowerCaseMessage + " ������ ���������� ��� ����!", "", 5, Type.warning);
    }
}
