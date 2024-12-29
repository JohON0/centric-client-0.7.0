package centric.pl.functions.impl.misc;

import centric.pl.functions.api.Category;
import centric.pl.Main;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.johon0.utils.discordrpc.DiscordRichPresenceUtil;
import centric.pl.johon0.utils.math.StopWatch;
import net.minecraft.client.GameConfiguration;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;
import net.optifine.shaders.Shaders;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.util.ArrayList;
import java.util.List;

@FunctionRegister(name = "UnHook", type = Category.Misc, beta = false)
public class UnHook extends Function {
    public boolean enabled = false;
    public String secret = "centric";
    public StopWatch stopWatch = new StopWatch();

    @Override
    public void onEnable() {
        super.onEnable();
        process();
        print("Что бы вернуть чит в исходное состояние напишите слово: " + TextFormatting.RED + "(" + secret + ")");
        print("Все сообщения в чате удалятся через " + TextFormatting.RED + "10 секунд");
        stopWatch.reset();

        new Thread(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            mc.ingameGUI.getChatGUI().clearChatMessages(false);
            toggle();
        }).start();


        enabled = true;

    }

    public List<Function> saved = new ArrayList<>();

    public void process() {
        for (Function function : Main.getInstance().getFunctionRegistry().getFunctions()) {
            if (function == this) continue;
            if (function.isState()) {
                saved.add(function);
                function.setState(false,false);
            }
        }
        mc.fileResourcepacks = new File(System.getenv("appdata") + "\\.tlauncher\\legacy\\Minecraft\\game" + "\\resourcepacks");
        Shaders.shaderPacksDir = new File(System.getenv("appdata") + "\\.tlauncher\\legacy\\Minecraft\\game" + "\\shaderpacks");

        File folder = new File("C:\\Centric");
        DiscordRichPresenceUtil.shutdownDiscord();
        hiddenFolder(folder, true);
    }

    public void hook() {
        DiscordRichPresenceUtil.startDiscord();
        for (Function function : saved) {
            if (function == this) continue;
            if (!function.isState()) {
                function.setState(true,false);
            }
        }
        File folder = new File("C:\\Centric");
        hiddenFolder(folder, false);
        mc.fileResourcepacks = GameConfiguration.instance.folderInfo.resourcePacksDir;
        Shaders.shaderPacksDir = new File(Minecraft.getInstance().gameDir, "shaderpacks");
        enabled = false;
    }

    private void hiddenFolder(File folder, boolean hide) {
        if (folder.exists()) {
            try {
                Path folderPathObj = folder.toPath();
                DosFileAttributeView attributes = Files.getFileAttributeView(folderPathObj, DosFileAttributeView.class);
                attributes.setHidden(false);
            } catch (IOException e) {
                System.out.println("Не удалось скрыть папку: " + e.getMessage());
            }
        }
    }
}
