package centric.pl.functions.impl.render;

import centric.pl.events.impl.EventDisplay;
import centric.pl.events.impl.EventMotion;
import centric.pl.events.impl.EventPacket;
import centric.pl.events.impl.WorldEvent;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.settings.impl.BooleanSetting;
import centric.pl.functions.settings.impl.ModeListSetting;
import centric.pl.functions.settings.impl.SliderSetting;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.font.Fonts;
import centric.pl.johon0.utils.render.ColorUtils;
import centric.pl.johon0.utils.render.DisplayUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.*;
import net.minecraft.network.play.client.*;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.optifine.render.RenderUtils;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@FunctionRegister(name = "Xray Bypass", type = Category.Render, beta = false)
public class XrayBypass extends Function {
    CopyOnWriteArrayList<BlockPos> waiting = new CopyOnWriteArrayList<>();

    public SliderSetting up = new SliderSetting("�����", 5, 0, 30, 1);

    public SliderSetting down = new SliderSetting("����", 5, 0, 30, 1);
    public SliderSetting radius = new SliderSetting("������", 20, 0, 30, 1);
    public SliderSetting delay = new SliderSetting("��������", 13, 0, 40, 1);
    public SliderSetting skip = new SliderSetting("�������", 3, 1, 5, 1);


    public ModeListSetting ores = new ModeListSetting("������",
            new BooleanSetting("�����", false),
            new BooleanSetting("������", false),
            new BooleanSetting("��������", false),
            new BooleanSetting("������", false),
            new BooleanSetting("���������", false),
            new BooleanSetting("������", false),
            new BooleanSetting("�������", false)
    );

    public XrayBypass() {
        addSettings(radius, up, down, delay, skip, ores);
    }

    CopyOnWriteArrayList<BlockPos> clicked = new CopyOnWriteArrayList<>();

    BlockPos clicking;
    Thread thread;

    @Override
    public void onDisable() {
        super.onDisable();
        if (thread != null) {
            thread.interrupt();
            thread.stop();
        }
        clicking = null;
        clicked.clear();
        waiting.clear();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        waiting = removeEveryOther(getBlocks(), skip.get().intValue());
        thread = new Thread(() -> {
            if (IMinecraft.mc.player != null) {
                for (BlockPos click : waiting) {
                    IMinecraft.mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.START_DESTROY_BLOCK, click, Direction.UP));
                    clicked.add(click);
                    clicking = click;
                    try {
                        Thread.sleep(delay.get().intValue());
                    } catch (InterruptedException e) {
                        System.out.println("Anti Xray: " + e.getMessage());
                    }
                }
            }
        });
        thread.start();
    }

    private int isValid(BlockPos pos) {

        BlockState state = IMinecraft.mc.world.getBlockState(pos);
        Block block = state.getBlock();

        if (block instanceof OreBlock ore) {
            if (ore == Blocks.COAL_ORE && ores.get(0).get()) {
                return ColorUtils.rgba(12, 12, 12, 255);
            }
            if (ore == Blocks.IRON_ORE && ores.get(1).get()) {
                return ColorUtils.rgba(122, 122, 122, 255);
            }
            if (ore == Blocks.REDSTONE_ORE && ores.get(2).get()) {
                return ColorUtils.rgba(255, 82, 82, 255);
            }
            if (ore == Blocks.GOLD_ORE && ores.get(3).get()) {
                return ColorUtils.rgba(247, 255, 102, 255);
            }
            if (ore == Blocks.EMERALD_ORE && ores.get(4).get()) {
                return ColorUtils.rgba(116, 252, 101, 255);
            }
            if (ore == Blocks.DIAMOND_ORE && ores.get(5).get()) {
                return ColorUtils.rgba(77, 219, 255, 255);
            }
            if (ore == Blocks.ANCIENT_DEBRIS && ores.get(6).get()) {
                return ColorUtils.rgba(105, 60, 12, 255);
            }
        }
        return -1;
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (thread != null) {
            if (thread.isAlive()) {
                if (e.isSend()) {
                    if (e.getPacket() instanceof CPlayerPacket) {
                        e.cancel();
                    }
                    if (e.getPacket() instanceof CAnimateHandPacket) {
                        e.cancel();
                    }
                    if (e.getPacket() instanceof CPlayerTryUseItemPacket) {
                        e.cancel();
                    }
                    if (e.getPacket() instanceof CHeldItemChangePacket) {
                        e.cancel();
                    }
                }
            }
        }
    }

    @Subscribe
    public void onMotion(EventMotion e) {

        if (thread != null) {
            if (thread.isAlive()) {
                e.cancel();
            }
        }

    }

    @Subscribe
    public void onDisplay(EventDisplay e) {

        float width = 100;
        float heigth = 15;

        DisplayUtils.drawRoundedRect(IMinecraft.mc.getMainWindow().getScaledWidth() / 2f - width / 2, 10, width, heigth, 4, ColorUtils.rgba(10, 10, 10, 128));
        float x = IMinecraft.mc.getMainWindow().getScaledWidth() / 2f - width / 2;
        Fonts.notoitalic[14].drawString(e.getMatrixStack(), clicked.size() + "/" + waiting.size(), x + 5, 15, -1);

        long millis = ((long) waiting.size() * delay.get().intValue()) - clicked.size() * delay.get().intValue();

        String time = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), // The change is in this line
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));


        Fonts.notoitalic[14].drawString(e.getMatrixStack(), time, x + width - Fonts.notoitalic[14].getWidth(time) - 5, 15, -1);


    }

    @Subscribe
    public void onWorld(WorldEvent e) {
        if (clicked != null && clicking != null) {
            RenderUtils.drawBlockBox(clicking, ColorUtils.rgba(83, 252, 154, 255));
            for (BlockPos click : clicked) {
                int color = isValid(click);
                if (color != -1) {
                    RenderUtils.drawBlockBox(click, color);
                }
            }
        }
    }


    private static <T> CopyOnWriteArrayList<T> removeEveryOther(CopyOnWriteArrayList<T> inList, int offset) {
        if (offset == 1) return inList;
        CopyOnWriteArrayList<T> outList = new CopyOnWriteArrayList<>();
        @SuppressWarnings("unchecked")
        T[] ts = (T[]) inList.toArray();
        for (int i = 0; i < ts.length; i++) {
            if (i % offset == 0) {
                outList.add(ts[i]);
            }
        }
        return outList;
    }

    CopyOnWriteArrayList<BlockPos> getBlocks() {
        CopyOnWriteArrayList<BlockPos> blocks = new CopyOnWriteArrayList<>();
        BlockPos start = IMinecraft.mc.player.getPosition();
        int dis = radius.get().intValue();
        int up = this.up.get().intValue();
        int down = this.down.get().intValue();
        for (int y = up; y >= -down; y--)
            for (int x = dis; x >= -dis; x--)

                for (int z = dis; z >= -dis; z--) {
                    BlockPos pos = start.add(x, y, z);

                    if (pos.getY() > 0) {
                        Block block = IMinecraft.mc.world.getBlockState(pos).getBlock();
                        if (block instanceof AirBlock) continue;
                        blocks.add(pos);
                    }

                }
        return blocks;
    }
}