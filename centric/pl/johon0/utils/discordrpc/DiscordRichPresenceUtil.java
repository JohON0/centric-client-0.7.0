package centric.pl.johon0.utils.discordrpc;

import centric.pl.johon0.utils.client.ClientUtil;
import discordrpc.DiscordEventHandlers;
import discordrpc.DiscordRPC;
import discordrpc.DiscordRichPresence;
import discordrpc.helpers.RPCButton;
import centric.pl.Main;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.net.URLConnection;

public class DiscordRichPresenceUtil {
    private static Thread rpcThread;
    private static final long lastTimeMillis = System.currentTimeMillis();
    public static String avatarUrl;
    public static String userid;
    public static BufferedImage avatar;
    public static String state;
    public static String largeimagekey;
    public static String playingonip;
    public static void startDiscord() {
        DiscordEventHandlers eventHandlers = new DiscordEventHandlers.Builder().ready((user) -> {
            if (user.avatar != null) {
                userid = user.username.toString();
                avatarUrl = "https://cdn.discordapp.com/avatars/" + user.userId + "/" + user.avatar;
                try {
                    URLConnection url = new URL(DiscordRichPresenceUtil.avatarUrl).openConnection();
                    url.setRequestProperty("User-Agent", "Mozilla/5.0");
                    avatar = ImageIO.read(url.getInputStream());
                } catch (Exception ignored) {}
            }
        }).build();
        DiscordRPC.INSTANCE.Discord_Initialize("1253742227415633960", eventHandlers, true, "");

        rpcThread = new Thread(() -> {
            while(true) {
                DiscordRPC.INSTANCE.Discord_RunCallbacks();
                updatePresence();

                try {
                    Thread.sleep(2000L);
                } catch (InterruptedException ignored) {}
            }

        });
        rpcThread.start();
    }

    public static void shutdownDiscord() {
        if (rpcThread != null) {
            rpcThread.interrupt();
            DiscordRPC.INSTANCE.Discord_Shutdown();
        }
    }
    public static String smallImageRPC() {
        if (ClientUtil.isConnectedToServer("mc.funtime.su")) {
            largeimagekey = "https://avatars.mds.yandex.net/get-socsnippets/12849796/2a00000191491f286b12049a0a2a808df8fa/square_83";
        }
        if (ClientUtil.isConnectedToServer("mc.musteryworld.ru")) {
            largeimagekey = "https://avatars.mds.yandex.net/get-socsnippets/10245612/2a000001913a07b35c4194e6a8673a60b9ae/square_83";
        }
        if (ClientUtil.isConnectedToServer("mc.musteryworld.net")) {
            largeimagekey = "https://avatars.mds.yandex.net/get-socsnippets/10245612/2a000001913a07b35c4194e6a8673a60b9ae/square_83";
        }
        if (ClientUtil.isConnectedToServer("mc.reallyworld.ru")) {
            largeimagekey = "https://avatars.mds.yandex.net/get-socsnippets/5320700/2a0000019148d5ff34ab2899f653e30c7735/square_83";
        }
        if (ClientUtil.isConnectedToServer("mc.skytime.su")) {
            largeimagekey = "https://avatars.mds.yandex.net/get-socsnippets/12818097/2a0000019148e4c431bf24d57d7522e24c6b/square_83";
        }
        if (ClientUtil.isConnectedToServer("mc.politmine.ru")) {
            largeimagekey = "https://avatars.mds.yandex.net/get-socsnippets/10245612/2a000001912a5e3e39dcc9f412248ce99064/square_83";
        }
        return largeimagekey;

    }
    public static String playingonserver() {
        if (ClientUtil.isConnectedToServer("mc.funtime.su")) {
            playingonip = "Playing on: " + "mc.funtime.su";
        }
        if (ClientUtil.isConnectedToServer("mc.musteryworld.ru")) {
            playingonip = "Playing on: " + "mc.musteryworld.ru";
        }
        if (ClientUtil.isConnectedToServer("mc.musteryworld.net")) {
            playingonip = "Playing on: " + "mc.musteryworld.net";
        }
        if (ClientUtil.isConnectedToServer("mc.reallyworld.ru")) {
            playingonip = "Playing on: " + "mc.reallyworld.ru";
        }
        if (ClientUtil.isConnectedToServer("mc.skytime.su")) {
            playingonip = "Playing on: " + "mc.skytime.su";
        }
        if (ClientUtil.isConnectedToServer("mc.politmine.ru")) {
            playingonip = "Playing on: " + "mc.politmine.ru";
        }
        return playingonip;
    }
    private static void updatePresence() {
        DiscordRichPresence.Builder builder = new DiscordRichPresence.Builder();
        builder.setStartTimestamp(lastTimeMillis / 1000);
        builder.setState("· version: " + Main.version);
        builder.setDetails(state);
        builder.setLargeImage("https://s12.gifyu.com/images/StHtY.gif");
        builder.setSmallImage(smallImageRPC(), playingonserver());
        builder.setButtons(RPCButton.create("Discord", "https://discord.gg/2Ak4qsXPff"),
                RPCButton.create("Telegram", "https://t.me/centricclient"));
        DiscordRPC.INSTANCE.Discord_UpdatePresence(builder.build());
    }
}