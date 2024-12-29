package centric.pl.functions.impl.misc;

import com.google.common.eventbus.Subscribe;
import centric.pl.events.impl.EventPacket;
import centric.pl.events.impl.EventUpdate;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.settings.impl.StringSetting;
import centric.pl.johon0.utils.client.ClientUtil;
import centric.pl.johon0.utils.math.StopWatch;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.util.text.TextFormatting;

@FunctionRegister(name = "AutoTransfer", type = Category.Misc, beta = false)
public class AutoTransfer extends Function {

    private final StringSetting targetString = new StringSetting("����", "", "������� ����� �������", true);

    public AutoTransfer() {
        addSettings(targetString);
    }

    private final StopWatch stopWatch = new StopWatch();
    private boolean waiting;
    private boolean isSell;
    private Action action;
    private int from, target;

    @Subscribe
    private void onPacket(EventPacket packetEvent) {
        if (packetEvent.getPacket() instanceof SChatPacket p) {
            String rawText = TextFormatting.getTextWithoutFormattingCodes(p.getChatComponent().getString());

            if (rawText == null) {
                return;
            }

            if (rawText.contains("��������� �� �������!")) {
                waiting = false;
                return;
            }

            if (rawText.contains("���������� ��������� ��� ������� �������� � �������")) {
                waiting = false;
                action = Action.SWITCH;
                return;
            }

            if (rawText.contains("����� ����� �� ����� ���������� ������� ��������� ����� �������������� ��������. ���������")) {
                toggle();
            }
        }
    }

    boolean notify = false;

    @Subscribe
    private void onUpdate(EventUpdate e) {
        int currentAnarchy = getAnarchyNumber();

        if (action == null) {
            action = Action.SELL;
        }

        switch (action) {
            case WAIT -> {
                if (!notify) {
                    print(TextFormatting.RED + "��������� 30 ������, AutoTransfer ��������� ������.");
                    notify = true;
                }
                if (stopWatch.isReached(30_000)) {
                    action = Action.SELL;
                }
            }
            case SELL -> {
                if (waiting || !stopWatch.isReached(150) || currentAnarchy != from) {
                    return;
                }

                for (int i = 0; i < 9; i++) {
                    ItemStack itemStack = mc.player.inventory.mainInventory.get(i);

                    if (!itemStack.isEmpty()) {
                        mc.player.inventory.currentItem = i;
                        mc.playerController.syncCurrentPlayItem();
                        mc.player.sendChatMessage("/ah dsell 10");
                        isSell = true;
                        stopWatch.reset();
                        waiting = true;
                        break;
                    }
                }

                if (!waiting && isSell) {
                    action = Action.SWITCH;
                    isSell = false;
                }
            }
            case SWITCH -> {
                if (!waiting && currentAnarchy == from) {
                    mc.player.sendChatMessage("/an" + target);
                    action = Action.BUY;
                }
            }
            case BUY -> {
                if (!waiting && currentAnarchy == target) {
                    mc.player.sendChatMessage("/ah " + mc.getSession().getUsername());
                    stopWatch.reset();
                    waiting = true;
                    return;
                }

                Container container = mc.player.openContainer;

                if (container instanceof ChestContainer chestContainer) {
                    if (chestContainer.getSlot(0).getHasStack()) {
                        mc.playerController.windowClick(chestContainer.windowId, 0, 0, ClickType.QUICK_MOVE, mc.player);
                    }
                }

                if (stopWatch.isReached(1_000)) {
                    mc.player.closeScreen();
                    mc.player.sendChatMessage("/an" + from);
                    stopWatch.reset();
                    action = Action.WAIT;
                    notify = false;
                    waiting = false;
                }
            }
        }
    }

    private int getAnarchyNumber() {
        ScoreObjective objective = mc.world.getScoreboard().getObjective("TAB-Scoreboard");

        if (objective != null) {
            String rawTitle = objective.getDisplayName().getString();
            String clearTitle = TextFormatting.getTextWithoutFormattingCodes(rawTitle);

            if (clearTitle != null && clearTitle.contains("�������-")) {
                return Integer.parseInt(clearTitle.substring(clearTitle.lastIndexOf('-') + 1));
            }
        }

        return -1;
    }

    private void reset() {
        waiting = false;
        isSell = false;
        action = null;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        reset();

        if (!ClientUtil.isConnectedToServer("funtime")) {
            print("������ �������� ������ �� FunTime!");
            toggle();
            return;
        }

        int number = getAnarchyNumber();

        if (number == -1) {
            print("����� �� �������!");
            toggle();
            return;
        }

        from = number;

        if (targetString.get().isEmpty()) {
            print("������� ����� �������!");
            toggle();
            return;
        }

        target = Integer.parseInt(targetString.get());

        if (target < 1 || 999 < target) {
            print("������� ���������� ����� �������!");
            toggle();
            return;
        }

        if (from == target) {
            print("�� ��� �� ���� �������!");
            toggle();
            return;
        }

        //print("[debug] current: " + from + ", target: " + target);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        reset();
        notify = false;
    }

    private enum Action {
        SELL,
        SWITCH,
        BUY,
        WAIT
    }
}