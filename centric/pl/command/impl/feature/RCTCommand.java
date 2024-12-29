package centric.pl.command.impl.feature;

import centric.pl.Main;
import centric.pl.command.Command;
import centric.pl.command.Logger;
import centric.pl.command.MultiNamedCommand;
import centric.pl.command.Parameters;
import centric.pl.managers.notificationManager.Notification;
import centric.pl.johon0.utils.client.ClientUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RCTCommand implements Command, MultiNamedCommand {

    final Logger logger;
    final Minecraft mc;

    @Override
    public void execute(Parameters parameters) {
        if (!ClientUtil.isConnectedToServer("funtime") && !ClientUtil.isConnectedToServer("skytime")) {
            logger.log("Этот RCT работает только на сервере FunTime");
            Main.getInstance().getNotification().add( "Этот RCT работает только на сервере FunTime","",5, Notification.Type.warning);
            return;
        }

        int server = getAnarchyServerNumber();

        if (server == -1) {
            logger.log("Не удалось получить номер анархии.");
            return;
        }

        mc.player.sendChatMessage("/hub");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        mc.player.sendChatMessage("/an" + server);
    }

    private int getAnarchyServerNumber() {
        if (mc.ingameGUI.getTabList().header != null) {
            String serverHeader = TextFormatting.getTextWithoutFormattingCodes(mc.ingameGUI.getTabList().header.getString());
            if (serverHeader != null && serverHeader.contains("Анархия-")) {
                return Integer.parseInt(serverHeader.split("Анархия-")[1].trim());
            }
        }
        return -1;
    }

    @Override
    public String name() {
        return "rct";
    }

    @Override
    public String description() {
        return "Перезаходит на анархию";
    }


    @Override
    public List<String> aliases() {
        return Collections.singletonList("reconnect");
    }
}
