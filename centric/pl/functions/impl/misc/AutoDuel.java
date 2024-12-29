package centric.pl.functions.impl.misc;


import centric.pl.events.impl.EventPacket;
import centric.pl.events.impl.EventUpdate;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.settings.impl.ModeSetting;
import centric.pl.johon0.utils.math.StopWatch;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SChatPacket;

@FunctionRegister(
        name = "AutoDuel",
        type = Category.Misc,
        beta = false
)
public class AutoDuel extends Function {
    private static final Pattern pattern = Pattern.compile("^\\w{3,16}$");
    private final ModeSetting mode = new ModeSetting("Mode", "Шары", "Шары", "Щит", "Шипы 3", "Незеритка", "Читерский рай", "Лук", "Классик", "Тотемы", "Нодебафф");
    private double lastPosX;
    private double lastPosY;
    private double lastPosZ;
    private final List<String> sent = Lists.newArrayList();
    private final StopWatch counter = new StopWatch();
    private final StopWatch counter2 = new StopWatch();
    private final StopWatch counterChoice = new StopWatch();
    private final StopWatch counterTo = new StopWatch();

    public AutoDuel() {
        this.addSettings(this.mode);
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        List<String> players = this.getOnlinePlayers();
        double distance = Math.sqrt(Math.pow(this.lastPosX - mc.player.getPosX(), 2.0) + Math.pow(this.lastPosY - mc.player.getPosY(), 2.0) + Math.pow(this.lastPosZ - mc.player.getPosZ(), 2.0));
        if (distance > 500.0) {
            this.toggle();
        }

        this.lastPosX = mc.player.getPosX();
        this.lastPosY = mc.player.getPosY();
        this.lastPosZ = mc.player.getPosZ();
        if (this.counter2.isReached(800L * (long)players.size())) {
            this.sent.clear();
            this.counter2.reset();
        }

        Iterator var5 = players.iterator();

        while(var5.hasNext()) {
            String player = (String)var5.next();
            if (!this.sent.contains(player) && !player.equals(mc.session.getProfile().getName()) && this.counter.isReached(1000L)) {
                mc.player.sendChatMessage("/duel " + player);
                this.sent.add(player);
                this.counter.reset();
            }
        }

        Container var10 = mc.player.openContainer;
        if (var10 instanceof ChestContainer chest) {
            if (mc.currentScreen.getTitle().getString().contains("Выбор набора (1/1)")) {
                for(int i = 0; i < chest.getLowerChestInventory().getSizeInventory(); ++i) {
                    List<Integer> slotsID = new ArrayList();
                    int index = 0;
                    slotsID.add(index);
                    ++index;
                    Collections.shuffle(slotsID);
                    if (this.counterChoice.isReached(150L)) {
                        if (this.mode.is("Щит")) {
                            mc.playerController.windowClick(chest.windowId, 0, 0, ClickType.QUICK_MOVE, mc.player);
                        }

                        if (this.mode.is("Шипы 3")) {
                            mc.playerController.windowClick(chest.windowId, 1, 0, ClickType.QUICK_MOVE, mc.player);
                        }

                        if (this.mode.is("Лук")) {
                            mc.playerController.windowClick(chest.windowId, 2, 0, ClickType.QUICK_MOVE, mc.player);
                        }

                        if (this.mode.is("Тотемы")) {
                            mc.playerController.windowClick(chest.windowId, 3, 0, ClickType.QUICK_MOVE, mc.player);
                        }

                        if (this.mode.is("Нодебафф")) {
                            mc.playerController.windowClick(chest.windowId, 4, 0, ClickType.QUICK_MOVE, mc.player);
                        }

                        if (this.mode.is("Шары")) {
                            mc.playerController.windowClick(chest.windowId, 5, 0, ClickType.QUICK_MOVE, mc.player);
                        }

                        if (this.mode.is("Классик")) {
                            mc.playerController.windowClick(chest.windowId, 6, 0, ClickType.QUICK_MOVE, mc.player);
                        }

                        if (this.mode.is("Читерский рай")) {
                            mc.playerController.windowClick(chest.windowId, 7, 0, ClickType.QUICK_MOVE, mc.player);
                        }

                        if (this.mode.is("Незерка")) {
                            mc.playerController.windowClick(chest.windowId, 8, 0, ClickType.QUICK_MOVE, mc.player);
                        }

                        this.counterChoice.reset();
                    }
                }
            } else if (mc.currentScreen.getTitle().getString().contains("Настройка поединка") && this.counterTo.isReached(150L)) {
                mc.playerController.windowClick(chest.windowId, 0, 0, ClickType.QUICK_MOVE, mc.player);
                this.counterTo.reset();
            }
        }

    }

    @Subscribe
    private void onPacket(EventPacket event) {
        if (event.isReceive()) {
            IPacket<?> packet = event.getPacket();
            if (packet instanceof SChatPacket) {
                SChatPacket chat = (SChatPacket)packet;
                String text = chat.getChatComponent().getString().toLowerCase();
                if (text.contains("начало") && text.contains("через") && text.contains("секунд!") || text.equals("дуэли » во время поединка запрещено использовать команды")) {
                    this.toggle();
                }
            }
        }

    }

    private List getOnlinePlayers() {
        return mc.player.connection.getPlayerInfoMap().stream().map(NetworkPlayerInfo::getGameProfile).map(GameProfile::getName).filter((profileName) -> pattern.matcher(profileName).matches()).collect(Collectors.toList());
    }
}
