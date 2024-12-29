package centric.pl.functions.impl.render;

import centric.pl.Main;
import centric.pl.command.staffs.StaffStorage;
import centric.pl.events.impl.EventDisplay;
import centric.pl.events.impl.EventUpdate;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.impl.combat.KillAura;
import centric.pl.functions.settings.impl.BooleanSetting;
import centric.pl.functions.settings.impl.ModeListSetting;
import centric.pl.functions.settings.impl.ModeSetting;
import centric.pl.johon0.utils.render.*;
import centric.pl.managers.styleManager.ThemeSwitcher;
import centric.pl.managers.StyleManager;
import centric.pl.johon0.utils.animations.Animation;
import centric.pl.johon0.utils.animations.Direction;
import centric.pl.johon0.utils.animations.impl.EaseBackIn;
import centric.pl.johon0.utils.client.ClientUtil;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.client.KeyStorage;
import centric.pl.johon0.utils.drag.Dragging;
import centric.pl.johon0.utils.font.Fonts;
import centric.pl.johon0.utils.math.MathUtil;
import centric.pl.johon0.utils.math.StopWatch;
import centric.pl.johon0.utils.particles.ParticleManager;
import centric.pl.johon0.utils.text.GradientUtil;
import centric.pl.ui.clickgui.MainScreen;
import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.*;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.texture.PotionSpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.minecraft.client.gui.AbstractGui.blit;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@FunctionRegister(name = "HUD", type = Category.Render,beta = false)
public class HUD extends Function {

    public ModeListSetting elements = new ModeListSetting("Элементы",
            new BooleanSetting("Логотип", true),
            new BooleanSetting("Таргет худ", true),
            new BooleanSetting("Эффекты", true),
            new BooleanSetting("Информация", true),
            new BooleanSetting("Броня", true),
            new BooleanSetting("Список Модераторов", true),
            new BooleanSetting("Список биндов", true),
            new BooleanSetting("Здоровье", true),
            new BooleanSetting("Список модулей", true),
            new BooleanSetting("Инвентарь", true),
            new BooleanSetting("Кулдаун", true)
    );

    public ModeSetting targetHudMode = new ModeSetting("Мод", "Обычный", "Обычный", "Новый");

    public ModeListSetting watermarkElement = new ModeListSetting("Элементы логотипа",
            new BooleanSetting("Название чита", true),
            new BooleanSetting("Ник в игре", true),
            new BooleanSetting("Счетчик кадров", true),
            new BooleanSetting("Пинг в игре", true)).setVisible(() -> elements.get(0).get());

    public BooleanSetting themeopen = new BooleanSetting("Показывать список тем", true);

    public static BooleanSetting tikva = new BooleanSetting("Тыквенные семечки", false);

    public BooleanSetting bypasshp = new BooleanSetting("Обход хп на FunTime / Saturn / ReallyWorld", true).setVisible(() -> elements.get(1).get());

    public HUD() {
        addSettings(elements, watermarkElement, targetHudMode, bypasshp, tikva, themeopen);
    }

    private final ParticleManager particleManager = new ParticleManager();
    public static float offs = 0;

    @Subscribe
    public void onUpdate(EventUpdate eventUpdate) {
        if (!elements.get(6).get()) {
            return;
        }
        staffPlayers.clear();
        IMinecraft.mc.world.getScoreboard().getTeams().stream().sorted(Comparator.comparing(Team::getName)).toList().forEach(team -> {
            String staffName = team.getMembershipCollection().toString().replaceAll("[\\[\\]]", "");
            boolean vanish = true;

            if (IMinecraft.mc.getConnection() != null) {
                for (NetworkPlayerInfo info : IMinecraft.mc.getConnection().getPlayerInfoMap()) {
                    if (info.getGameProfile().getName().equals(staffName)) {
                        vanish = false;
                    }
                }
            }

            if (namePattern.matcher(staffName).matches() && !staffName.equals(IMinecraft.mc.player.getName().getString())) {
                if (!vanish) {
                    if (prefixMatches.matcher(team.getPrefix().getString().toLowerCase(Locale.ROOT)).matches() || StaffStorage.isStaff(staffName)) {
                        staffPlayers.add(new StaffListHelper.StaffData(team.getPrefix(), staffName, StaffListHelper.StaffData.Status.NONE));
                    }
                }
                if (vanish && !team.getPrefix().getString().isEmpty()) {
                    staffPlayers.add(new StaffListHelper.StaffData(team.getPrefix(), staffName, StaffListHelper.StaffData.Status.VANISHED));
                }
            }
        });

        this.staffPlayers = staffPlayers.stream()
                .sorted(Comparator.comparing(this::getPriority))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Subscribe
    public void onEvent(EventDisplay event) {
        handleRender(event);
    }

    private void handleRender(EventDisplay renderEvent) {
        final MatrixStack stack = renderEvent.getMatrixStack();
        if (elements.get(0).get()) {
            onTitleRender(stack);
        }

        if (elements.get(1).get()) {
            if (targetHudMode.is("Обычный")) {
                onRenderTargetHUD(stack);
            }
            if (targetHudMode.is("Новый")) {
                onRenderOldNursultanTHUD(stack);
            }
        }
        if (elements.get(2).get()) {
            potionStatusRender(stack);
        }
        if (elements.get(3).get()) {
            onInformationRender(stack);
        }
        if (elements.get(4).get()) {
            onArmorRender();
        }
        if (elements.get(5).get()) {
            onStaffListRender(renderEvent);
        }
        if (elements.get(6).get()) {
            renderkeybinds(stack);
        }
        if (elements.get(7).get()) {
            healthinforender(stack);
        }
        if (elements.get(8).get()) {
            drawArrayList(stack);
        }
        if (elements.get(9).get()) {
            drawInventory(stack);
        }
    }

    private Set<StaffListHelper.StaffData> staffPlayers = new LinkedHashSet<>();
    private final Pattern namePattern = Pattern.compile("^\\w{3,16}$");
    private final Pattern prefixMatches = Pattern.compile(".*(mod|adm|help|wne|хелп|адм|поддержка|кура|own|taf|curat|dev|supp|yt|сотруд).*");
    float width;
    float height;
    public final Dragging dragging = Main.getInstance().createDrag(this, "stafflist", 4, 28);

    private void onStaffListRender(EventDisplay eventDisplay) {

        float posX = dragging.getX();
        float posY = dragging.getY();
        float padding = 5;
        float fontSize = 6f;
        MatrixStack ms = eventDisplay.getMatrixStack();


        drawStyledRect(posX, posY, width, height, 2);
        DisplayUtils.drawShadow(posX + width / 2 - 16, posY + padding - 0.5f, Fonts.notoitalic[14].getWidth("StaffList") + 2, Fonts.notoitalic[14].getFontHeight() - 2, 5, ColorUtils.setAlpha(ThemeSwitcher.textcolor, 90));
        Fonts.notoitalic[14].drawCenteredString(ms, "StaffList", posX + width / 2, posY + padding + 1f, ThemeSwitcher.textcolor);

        posY += fontSize + padding * 2;

        float maxWidth = Fonts.notoitalic[14].getWidth("StaffList") + padding * 2;
        float localHeight = fontSize * 2 + 2;
        boolean hasRendered = false;
        posY += 3.5f;
        for (StaffListHelper.StaffData f : staffPlayers) {
            ITextComponent prefix = f.getPrefix();
            if (!hasRendered) {
                DisplayUtils.drawImage(new ResourceLocation("centric/images/gradline.png"), posX, posY + padding - 10, width, 1, ThemeSwitcher.textcolor);
                hasRendered = true;
            }
            float prefixWidth = Fonts.notoitalic[12].getWidth(prefix.getString());
            String staff = (prefix.getString().isEmpty() ? "" : " ") + f.getName();
            float nameWidth = Fonts.notoitalic[12].getWidth(staff);

            float localWidth = prefixWidth + nameWidth + Fonts.notoitalic[12].getWidth(f.getStatus().string) + padding * 3;

            Fonts.notoitalic[12].drawString(ms, prefix, posX + padding, posY + 2, -1);
            Fonts.notoitalic[12].drawString(ms, staff, posX + padding + prefixWidth, posY + 2, ThemeSwitcher.textcolor);
            Fonts.iconsall[12].drawString(ms, f.getStatus().string, posX + width - padding - Fonts.notoitalic[12].getWidth(f.getStatus().string), posY + 2, f.getStatus().color);

            if (localWidth > maxWidth) {
                maxWidth = localWidth;
            }

            posY += fontSize + padding;
            localHeight += fontSize + padding;
        }

        width = Math.max(maxWidth, 75);
        height = localHeight + 2.5f;
        dragging.setWidth(width);
        dragging.setHeight(height);
    }

    private void renderItemInfo(MatrixStack ms, float posX, float currentPosY, float fontSize, float padding, ItemStack itemStack, String cooldownText, ResourceLocation icon) {
        String itemName = getLocalizedItemName(itemStack);
        float nameWidth = Fonts.notoitalic[12].getWidth(itemName);
        float cooldownTextWidth = Fonts.notoitalic[12].getWidth(cooldownText);
        if (!cooldownText.isEmpty()) {
//            DisplayUtils.drawRoundedRect(posX + nameWidth + 19.0f, currentPosY - 1.0f, cooldownTextWidth + 5.0f, 10.0f, 2.0f, ColorUtils.rgba(0, 0, 0, 180));
            Fonts.notoitalic[12].drawString(ms, cooldownText, posX + nameWidth + 16.0f, currentPosY + 0.5f, ColorUtils.rgba(150, 150, 210, 255));
        }
        Fonts.notoitalic[12].drawString(ms, itemName, posX + padding + 8.0f, currentPosY + 0.5f, ColorUtils.rgba(255, 255, 255, 255));
        DisplayUtils.drawImage(icon, posX + 1.0f, currentPosY - 1.5f, 10.0f, 10.0f, ColorUtils.rgb(255, 255, 255));
    }

    private String getLocalizedItemName(ItemStack itemStack) {
        if (itemStack.getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
            return "Чарка";
        } else if (itemStack.getItem() == Items.ENDER_EYE) {
            return "Дезориентация";
        } else if (itemStack.getItem() == Items.NETHERITE_SCRAP) {
            return "Трапка";
        } else if (itemStack.getItem() == Items.SUGAR) {
            return "Явная пыль";
        }
        return I18n.format(itemStack.getTranslationKey(), new Object[0]);
    }

    public final Dragging inventory = Main.getInstance().createDrag(this, "Inventory", 4, 80);
    private void drawInventory(MatrixStack stack) {
        float x = this.inventory.getX();
        float y = this.inventory.getY();
        float width = 16.0F;
        float height = 16.0F;
        float y1 = 17.0F;
        float x1 = 0.7F;
        int headerHeight = 16;
        int padding = 4;
        drawStyledRect(x - 2, y - 18, width + 138, headerHeight + padding / 2 + 2 + 51, 5);
        DisplayUtils.drawShadow(x + 56, y - 14, Fonts.notoitalic[14].getWidth("Inventory")+2, Fonts.notoitalic[14].getFontHeight()-2, 5, ColorUtils.setAlpha(ThemeSwitcher.textcolor,90));

        Fonts.notoitalic[14].drawCenteredString(stack,"Inventory", (double) (x + 74), (double) (y - 13), ThemeSwitcher.textcolor);
        DisplayUtils.drawImage(new ResourceLocation("centric/images/gradline.png"), x, y-5, width + 138, 1, ThemeSwitcher.textcolor);
        for (int i = 9; i < 36; ++i) {
            DisplayUtils.drawRoundedRect(x, y, width, height, 3.0F, ThemeSwitcher.backgroundcolor);
            ItemStack slot = mc.player.inventory.getStackInSlot(i);
            drawItemStack(slot, x + 0.6F, y + 1.0F, true, true, 0.9F);
            x += width;
            x += x1;
            if (i == 17) {
                y += y1;
                x -= width * 9.0F;
                x -= x1 * 9.0F;
            }

            if (i == 26) {
                y += y1;
                x -= width * 9.0F;
                x -= x1 * 9.0F;
            }
        }
        this.inventory.setWidth(width * 9.0F + x1 * 9.0F);
        this.inventory.setHeight(height * 3.0F + 1.0F);
    }
    public final Dragging keybinds = Main.getInstance().createDrag(this, "keybinds", 177, 28);
    private float heightDynamic = 0;
    private int activeModules = 0;

    private void renderkeybinds(MatrixStack ms) {
        float posX = keybinds.getX();
        float posY = keybinds.getY();
        int headerHeight = 16;
        int width = 75;
        int padding = 4;
        int offset = 12;
        float height = activeModules * offset;
        this.heightDynamic = MathUtil.fast(this.heightDynamic, height, 11);
        drawStyledRect(posX, posY, width,  headerHeight + heightDynamic,4);
        DisplayUtils.drawShadow(posX + width / 2 - 18, posY + 5.3f, Fonts.notoitalic[14].getWidth("KeyBinds")+3, Fonts.notoitalic[14].getFontHeight()-2, 5, ColorUtils.setAlpha(ThemeSwitcher.textcolor,90));
        Fonts.notoitalic[14].drawCenteredString(ms, "KeyBinds", posX + width / 2, posY + 6.5f, ThemeSwitcher.textcolor);
        boolean hasRendered = false;
        for (Function f : Main.getInstance().getFunctionRegistry().getFunctions()) {
            if (f.getBind() != 0 && f.isState() && !hasRendered) {
                DisplayUtils.drawImage(new ResourceLocation("centric/images/gradline.png"), posX, posY + 14.5f, width, 1, ThemeSwitcher.textcolor);
                hasRendered = true;
            }
        }
        int index = 0;
        for (Function f : Main.getInstance().getFunctionRegistry().getFunctions()) {
            if (f.getBind() != 0 && f.isState()) {
            String text = KeyStorage.getKey(f.getBind());
            if (text == null) {
                continue;
            }
            if (text.length() > 6) {
                text = text.substring(0, 6);
            }
            String bindText = text.toUpperCase();
            float bindWidth = Fonts.notoitalic[12].getWidth(bindText);
            float y = posY - 1 + headerHeight + padding + (index * offset);
                Fonts.notoitalic[12].drawString(ms, f.getName(), posX + padding, y+2, ThemeSwitcher.textcolor);
            Fonts.notoitalic[12].drawString(ms, "(" + bindText + ")", posX - 6 + width - bindWidth - padding, y+2, ThemeSwitcher.textcolor);
            index++;
        }
    }


        activeModules = index;
        keybinds.setWidth(width);
        keybinds.setHeight(activeModules * offset + headerHeight);
}
    public final Dragging potionStatus = Main.getInstance().createDrag(this, "PotionStatus", 88, 28);
    private float hDynamic = 0;
    private int activePotions = 0;
    private void potionStatusRender(MatrixStack ms) {
        float posX = potionStatus.getX();
        float posY = potionStatus.getY();
        int headerHeight = 16;
        int width = 82;
        int padding = 4;
        int offset = 12;
        float height = activePotions * offset;
        this.hDynamic = MathUtil.fast(this.hDynamic, height, 11);
        drawStyledRect(posX, posY, width, headerHeight + hDynamic, 4);
        DisplayUtils.drawShadow(posX + width / 2 - 15, posY + 5.3f, Fonts.notoitalic[14].getWidth("Potions")+3, Fonts.notoitalic[14].getFontHeight()-2, 5, ColorUtils.setAlpha(ThemeSwitcher.textcolor,90));
        Fonts.notoitalic[14].drawCenteredString(ms, "Potions", posX + width / 2, posY + 6.5f, ThemeSwitcher.textcolor);
        boolean hasRendered = false;
        for (EffectInstance p : IMinecraft.mc.player.getActivePotionEffects()) {
            if (p.isShowIcon() && !hasRendered) {
                DisplayUtils.drawImage(new ResourceLocation("centric/images/gradline.png"), posX, posY + 14.5f, width, 1, ThemeSwitcher.textcolor);
                hasRendered = true;
            }
        }
        int index = 0;
        for (EffectInstance p : IMinecraft.mc.player.getActivePotionEffects()) {
                if (p.isShowIcon()) {
                    String durationText = EffectUtils.getPotionDurationString(p, 1);
                    float bindWidth = Fonts.notoitalic[12].getWidth(durationText);
                    float y = posY - 1 + headerHeight + padding + (index * offset);
                    Fonts.notoitalic[12].drawString(ms,I18n.format(p.getEffectName()) + " " + getPotionAmplifer(p), posX + padding, y+2, ThemeSwitcher.textcolor);
                    Fonts.notoitalic[12].drawString(ms,  durationText, posX + width - bindWidth - padding, y+2, ThemeSwitcher.textcolor);
                    index++;
                }

        }


        activePotions = index;
        potionStatus.setWidth(width);
        potionStatus.setHeight(activePotions * offset + headerHeight);
    }
    public final Dragging healthinfodrag = Main.getInstance().createDrag(this, "healthinfo",466,253);
    private void healthinforender(MatrixStack stack) {
        float x = healthinfodrag.getX();
        float y = healthinfodrag.getY();
        int health = (int) IMinecraft.mc.player.getHealth();
        drawStyledRect(x,y,Fonts.notoitalic[16].getWidth("Hp:" + health)+5,Fonts.notoitalic[16].getFontHeight(), 4);
        Fonts.notoitalic[16].drawCenteredString(stack, "Hp:" + health,x + 14,y + 2, ThemeSwitcher.textcolor);
        healthinfodrag.setWidth(Fonts.notoitalic[16].getWidth("Hp:" + health));
        healthinfodrag.setHeight(Fonts.notoitalic[16].getFontHeight());
    }
    private void onInformationRender(final MatrixStack stack) {
        int color = ThemeSwitcher.textcolor;
        float x = 4;
        float y = IMinecraft.mc.getMainWindow().scaledHeight() - Fonts.notoitalic[11].getFontHeight() - (IMinecraft.mc.currentScreen instanceof ChatScreen ? 6 * IMinecraft.mc.gameSettings.guiScale : 0) - 1;
        float w = 7;
        drawStyledRect(x-2,y-10,Fonts.notoitalic[13].getWidth("xyz: " + TextFormatting.GREEN + (int) IMinecraft.mc.player.getPosX() + ", " + (int) IMinecraft.mc.player.getPosY() + ", " + (int) IMinecraft.mc.player.getPosZ() + TextFormatting.RED + " (" + (int) IMinecraft.mc.player.getPosX() / 8 + ", " + (int) IMinecraft.mc.player.getPosY() + ", " + (int) IMinecraft.mc.player.getPosZ() / 8 + ")") + 5,10,4);
        if (Main.getInstance().getFunctionRegistry() != null && Main.getInstance().getFunctionRegistry().getNameProtect().isState()) {
            drawStyledRect(
                x - 2,
                y - 4,
                Fonts.notoitalic[13].getWidth(
                        "nickname: " +
                                ( Main.getInstance().getFunctionRegistry().getNameProtect().name.get() + " | " +
                                        "bps: " + String.format("%.2f", Math.hypot(
                                        IMinecraft.mc.player.getPosX() - IMinecraft.mc.player.prevPosX,
                                        IMinecraft.mc.player.getPosZ() - IMinecraft.mc.player.prevPosZ
                                ) * 20))
                ) + 5,
                10,
                4
        );
            } else {
            drawStyledRect(
                    x - 2,
                    y - 4,
                    Fonts.notoitalic[13].getWidth(
                            "nickname: " +
                                    ( mc.getSession().getUsername() + " | " +
                                            "bps: " + String.format("%.2f", Math.hypot(
                                            IMinecraft.mc.player.getPosX() - IMinecraft.mc.player.prevPosX,
                                            IMinecraft.mc.player.getPosZ() - IMinecraft.mc.player.prevPosZ
                                    ) * 20))
                    ) + 5,
                    10,
                    4
            );
        }
        if (Main.getInstance().getFunctionRegistry() != null && Main.getInstance().getFunctionRegistry().getNameProtect().isState()) {
            String[] texts = {

                    "nickname: " + Main.getInstance().getFunctionRegistry().getNameProtect().name.get() + " | " + "bps: " + String.format("%.2f", Math.hypot(IMinecraft.mc.player.getPosX() - IMinecraft.mc.player.prevPosX, IMinecraft.mc.player.getPosZ() - IMinecraft.mc.player.prevPosZ) * 20),
                    "xyz: " + TextFormatting.GREEN + (int) IMinecraft.mc.player.getPosX() + ", " + (int) IMinecraft.mc.player.getPosY() + ", " + (int) IMinecraft.mc.player.getPosZ() + TextFormatting.RED + " (" + (int) IMinecraft.mc.player.getPosX() / 8 + ", " + (int) IMinecraft.mc.player.getPosY() + ", " + (int) IMinecraft.mc.player.getPosZ() / 8 + ")",};

            for (int i = 0; i < texts.length; i++) {
                Fonts.notoitalic[13].drawString(stack, texts[i], x, y - (w * i), color);
            }
        } else {
            String[] texts = {

                    "nickname: " + IMinecraft.mc.getSession().getUsername() + " | " + "bps: " + String.format("%.2f", Math.hypot(IMinecraft.mc.player.getPosX() - IMinecraft.mc.player.prevPosX, IMinecraft.mc.player.getPosZ() - IMinecraft.mc.player.prevPosZ) * 20),
                    "xyz: " + TextFormatting.GREEN + (int) IMinecraft.mc.player.getPosX() + ", " + (int) IMinecraft.mc.player.getPosY() + ", " + (int) IMinecraft.mc.player.getPosZ() + TextFormatting.RED + " (" + (int) IMinecraft.mc.player.getPosX() / 8 + ", " + (int) IMinecraft.mc.player.getPosY() + ", " + (int) IMinecraft.mc.player.getPosZ() / 8 + ")",};

            for (int i = 0; i < texts.length; i++) {
                Fonts.notoitalic[13].drawString(stack, texts[i], x, y - (w * i), color);
            }
        }
    }

    @AllArgsConstructor
    @Data
    public static class StaffData {
        ITextComponent prefix;
        String name;
        StaffListHelper.StaffData.Status status;

        public enum Status {
            NONE("", -1),
            VANISHED("V", ColorUtils.rgb(254, 68, 68));
            public final String string;
            public final int color;

            Status(String string, int color) {
                this.string = string;
                this.color = color;
            }
        }

        @Override
        public String toString() {
            return prefix.getString();
        }
    }


    private int getPriority(StaffListHelper.StaffData staffData) {
        return switch (staffData.toString()) {
            case "admin", "Админ" -> 0;
            case "ml.admin" -> 1;
            case "gl.moder" -> 2;
            case "st.moder", "s.moder" -> 3;
            case "moder", "Модератор", "Модер" -> 4;
            case "j.moder" -> 5;
            case "st.helper" -> 6;
            case "helper+" -> 7;
            case "helper" -> 8;
            case "yt+" -> 9;
            case "yt" -> 10;
            default -> 11;
        };
    }

    private void onArmorRender() {
        MainWindow mainWindow = Minecraft.getInstance().getMainWindow();
        int count = 0;
        for (int i = 0; i < IMinecraft.mc.player.inventory.getSizeInventory(); i++) {
            ItemStack s = IMinecraft.mc.player.inventory.getStackInSlot(i);
            if (s.getItem() == Items.TOTEM_OF_UNDYING) {
                count++;
            }
        }
        float xPos = mainWindow.scaledWidth() / 2f;
        float yPos = mainWindow.scaledHeight();

        boolean is = IMinecraft.mc.player.inventory.mainInventory.stream().map(ItemStack::getItem).toList().contains(Items.TOTEM_OF_UNDYING);
        int off = is ? +5 : 0;
        for (ItemStack s : IMinecraft.mc.player.inventory.armorInventory) {
            drawItemStack(s, xPos - off + 74, yPos - 56 + (IMinecraft.mc.player.isCreative() ? 20 : 0) - 2, null, false);
            off += 15;
        }
        if (is)
            drawItemStack(new ItemStack(Items.TOTEM_OF_UNDYING), xPos - off + 73, yPos - 56 + (IMinecraft.mc.player.isCreative() ? 20 : 0) - 2, String.valueOf(count), false);

    }

    public static void drawItemStack(ItemStack stack, float x, float y, boolean withoutOverlay, boolean scale, float scaleValue) {
        RenderSystem.pushMatrix();
        RenderSystem.translatef(x, y, 0);
        if (scale) GL11.glScaled(scaleValue, scaleValue, scaleValue);
        IMinecraft.mc.getItemRenderer().renderItemAndEffectIntoGUI(stack, 0, 0);
        if (withoutOverlay) IMinecraft.mc.getItemRenderer().renderItemOverlays(IMinecraft.mc.fontRenderer, stack, 0, 0);
        RenderSystem.popMatrix();
    }

    public void drawItemStack(ItemStack stack,
                              double x,
                              double y,
                              String altText,
                              boolean withoutOverlay) {

        RenderSystem.translated(x, y, 0);
        IMinecraft.mc.getItemRenderer().renderItemAndEffectIntoGUI(stack, 0, 0);
        if (!withoutOverlay)
            IMinecraft.mc.getItemRenderer().renderItemOverlayIntoGUI(IMinecraft.mc.fontRenderer, stack, 0, 0, altText);
        RenderSystem.translated(-x, -y, 0);
    }

    private void onPotionElementsRender(final MatrixStack stack, final EventDisplay renderEvent) {
        float off = Fonts.notoitalic[15].getFontHeight();
        for (EffectInstance e : IMinecraft.mc.player.getActivePotionEffects()) {
            MainWindow mainWindow = Minecraft.getInstance().getMainWindow();
            String effectName = I18n.format(e.getEffectName());
            String level = effectName;
            String duration = EffectUtils.getPotionDurationString(e, 1);
            String effectString = level + " (" + duration + ")";

            float x = mainWindow.scaledWidth() - Fonts.notoitalic[15].getWidth(effectString + " " + I18n.format("enchantment.level." + (e.getAmplifier() + 1))) - 2;
            float y = mainWindow.scaledHeight() - off;
            Fonts.notoitalic[15].drawString(stack, GradientUtil.gradient(level), x, y, -1);
            Fonts.notoitalic[15].drawString(stack, " " + I18n.format("enchantment.level." + (e.getAmplifier() + 1)) + " (" + duration + ")", x + Fonts.notoitalic[15].getWidth(level), y, new Color(230, 230, 230).getRGB());

            PotionSpriteUploader potionspriteuploader = this.mc.getPotionSpriteUploader();
            TextureAtlasSprite textureatlassprite = potionspriteuploader.getSprite(e.getPotion());
            IMinecraft.mc.getTextureManager().bindTexture(textureatlassprite.getAtlasTexture().getTextureLocation());
            blit(stack, (int) x - 10, (int) y - 3, AbstractGui.blitOffset, 10, 10, textureatlassprite);
            off += Fonts.notoitalic[15].getFontHeight();
        }
        offs = off;
    }
    public static void drawStyledRect(float x,
                                      float y,
                                      float width,
                                      float height,
                                      float radius) {
        DisplayUtils.drawShadow(x, y, width, height, 3, ThemeSwitcher.bgcolor);

        if (tikva.get()) {
            StencilUtil.initStencilToWrite();
            DisplayUtils.drawRoundedRect(x, y, width, height, radius, -1);
            StencilUtil.readStencilBuffer(5);
            DisplayUtils.drawImage(new ResourceLocation("centric/images/seme4ki.png"), x, y, width, height, -1);
            StencilUtil.uninitStencilBuffer();
        }
        DisplayUtils.drawRoundedRect(x, y, width, height, radius, tikva.get() ? ColorUtils.setAlpha(ThemeSwitcher.bgcolor,100) : ThemeSwitcher.bgcolor);

    }
    private void drawStyledRect2(float x,
                                 float y,
                                 float width,
                                 float height,
                                 Vector4f radius) {
        DisplayUtils.drawShadow(x, y, width, height, 3, ThemeSwitcher.bgcolor);
        if (tikva.get()) {
            StencilUtil.initStencilToWrite();
            DisplayUtils.drawRoundedRect(x, y, width, height, radius, -1);
            StencilUtil.readStencilBuffer(5);
            DisplayUtils.drawImage(new ResourceLocation("centric/images/seme4ki.png"), x, y, width, height, -1);
            StencilUtil.uninitStencilBuffer();
        }
        DisplayUtils.drawRoundedRect(x, y, width, height, radius, tikva.get() ? ColorUtils.setAlpha(ThemeSwitcher.bgcolor,100) : ThemeSwitcher.bgcolor);


    }
    private void onTitleRender(final MatrixStack stack) {

        int counter = 0;

        StringBuilder titleText = new StringBuilder();
        if (watermarkElement.get(0).get()) {
            titleText.append("Centric Recode");
            counter++;
        }
        if (watermarkElement.get(1).get()) {
            if (counter > 0) {
                titleText.append(TextFormatting.DARK_GRAY + " | " + (ThemeSwitcher.themelightofdark ? TextFormatting.WHITE : TextFormatting.BLACK));
            }
            titleText.append(Main.getInstance().getFunctionRegistry().getNameProtect().isState() ? Main.getInstance().getFunctionRegistry().getNameProtect().name.get() : IMinecraft.mc.getSession().getUsername());

            counter++;
        }
        if (watermarkElement.get(2).get()) {
            if (counter > 0) {
                titleText.append(TextFormatting.DARK_GRAY + " | " + (ThemeSwitcher.themelightofdark ? TextFormatting.WHITE : TextFormatting.BLACK));
            }
            titleText.append(IMinecraft.mc.debugFPS + "fps");
            counter++;
        }
        if (watermarkElement.get(3).get()) {
            if (counter > 0) {
                titleText.append(TextFormatting.DARK_GRAY + " | " + (ThemeSwitcher.themelightofdark ? TextFormatting.WHITE : TextFormatting.BLACK));
            }
            titleText.append(calculatePing() + "ms");
        }


        final float x = 5, y = 9, titleWidth = Fonts.notoitalic[13].getWidth(titleText.toString()) + 10, titleHeight = Fonts.notoitalic[13].getFontHeight() + 5;
        drawStyledRect(x, y, titleWidth, titleHeight, 3);
        Fonts.notoitalic[13].drawString(stack, titleText.toString(), x + 5, y + Fonts.notoitalic[13].getFontHeight() / 2f + 1, ThemeSwitcher.textcolor);
    }


    private int lastIndex;
    List<Function> list;
    StopWatch stopWatch = new StopWatch();
    private void drawArrayList(MatrixStack ms) {
        if (stopWatch.isReached(1000)) {
            list = Main.getInstance().getFunctionRegistry().getSorted(Fonts.notoitalic[13])
                    .stream()
                    .filter(m -> m.getCategory() != Category.Render)
                    .filter(m -> m.getCategory() != Category.Misc)
                    .toList();
            stopWatch.reset();
        }
        float rounding = 3;
        float padding = 2f;
        float posX = 4;
        float posY = 0;
        int index = 0;

        if (list == null) return;
        index = 0;
        posY = 4 + 28;
        for (Function f : list) {
            float fontSize = 8;
            ru.hogoshi.Animation anim = f.getAnimation();
            anim.update();

            float value = (float) anim.getValue();

            String text = f.getName();
            float textWidth = Fonts.notoitalic[13].getWidth(text);

            if (value != 0) {
                float localFontSize = fontSize * value;
                float localTextWidth = textWidth * value;

                boolean isFirst = index == 0;
                boolean isLast = index == lastIndex;

                float localRounding = rounding;

                for (Function f2 : list.subList(list.indexOf(f) + 1, list.size())) {
                    if (f2.getAnimation().getValue() != 0) {
                        localRounding = isLast ? rounding : Math.min(textWidth - Fonts.notoitalic[13].getWidth(f2.getName()), rounding);
                        break;
                    }
                }

                Vector4f rectVec = new Vector4f(isFirst ? rounding : 0, isLast ? rounding : 0, isFirst ? rounding : 0, isLast ? rounding : localRounding);

                drawStyledRect2(posX, posY, localTextWidth + padding * 2, localFontSize + padding * 2, rectVec);

                Fonts.notoitalic[13].drawString(ms, f.getName(), posX + padding, posY + padding+2, ColorUtils.setAlpha(ThemeSwitcher.textcolor, (int) (255 * value)));

                posY += (fontSize + padding * 2) * value;
                index++;
            }
        }

        lastIndex = index - 1;
    }

    float health = 0;
    public final Dragging targetHUD = Main.getInstance().createDrag(this, "targetDraggable", 7, 55F);
    private final Animation targetHudAnimation = new EaseBackIn(200, 1, 1.5f);
    private PlayerEntity target = null;
    private double scale = 0.0D;
    private void onRenderTargetHUD(final MatrixStack stack) {
        this.target = getTarget(this.target);
        this.targetHudAnimation.setDuration(300);
        this.scale = targetHudAnimation.getOutput();

        if (scale == 0.0F) {
            target = null;
        }

        if (target == null) {
            return;
        }

        final String targetName = this.target.getName().getString();
        final float nameWidth = Fonts.notoitalic[14].getWidth(targetName);
        final float xPosition = this.targetHUD.getX();
        final float yPosition = this.targetHUD.getY();
        final float maxWidth = 95;
        final float maxHeight = 30;
        String finalhealth;
        float currentHealth = fix1000Health(target);
        float maxHealth = MathHelper.clamp(target.getMaxHealth(), 0, 20);
        this.health = MathUtil.fast(health, currentHealth / maxHealth, 5);
        this.health = MathHelper.clamp(this.health, 0, 1);

        GlStateManager.pushMatrix();
        sizeAnimation(xPosition + (maxWidth / 2), yPosition + (maxHeight / 2), scale);
        drawStyledRect(xPosition, yPosition, maxWidth, maxHeight, 4);
        DisplayUtils.drawRoundFace(xPosition + 3.0f, yPosition + 3.0f, 25.0f, 25.0f, 4.0f,255, (AbstractClientPlayerEntity) target);

        int colorhealth = ColorUtils.interpolateColor(Color.RED.getRGB(), Color.GREEN.getRGB(), health);
        DisplayUtils.drawRoundedRect(xPosition + 32, yPosition + 21, 60, 5, 2, ThemeSwitcher.backgroundcolor);
        DisplayUtils.drawRoundedRect(xPosition + 32, yPosition + 21, 60 * this.health, 5, 2, ColorUtils.gradient(getColor(100), getColor(200), 100, 10));

        drawItemStack(xPosition + 2, yPosition - 12.5f, 12);
        Fonts.notoitalic[15].drawString(stack, targetName.substring(0, Math.min(targetName.length(), 10)), xPosition + 32, yPosition + 5, ThemeSwitcher.textcolor);

        if ((int) MathUtil.round(this.health * 20, 0.5f) == 0) {
            finalhealth = "Неизвестно";
        } else {
            finalhealth = (int) MathUtil.round(this.health * 20, 0.05f) + "";
        }
        Fonts.notoitalic[12].drawString(stack, "Hp: " + finalhealth, xPosition + 32, yPosition + 14, ThemeSwitcher.textcolor);


        GlStateManager.popMatrix();

        this.targetHUD.setWidth(maxWidth);
        this.targetHUD.setHeight(maxHeight);
    }
    private void onRenderOldNursultanTHUD(final MatrixStack stack) {
        this.target = getTarget(this.target);
        this.targetHudAnimation.setDuration(300);
        this.scale = targetHudAnimation.getOutput();

        if (scale == 0.0F) {
            target = null;
        }

        if (target == null) {
            return;
        }

        final String targetName = this.target.getName().getString();
        final float nameWidth = Fonts.notoitalic[14].getWidth(targetName);
        final float xPosition = this.targetHUD.getX();
        final float yPosition = this.targetHUD.getY();
        final float maxWidth = 95;
        final float maxHeight = 35;
        String finalhealth;
        float currentHealth = fix1000Health(target);
        float maxHealth = MathHelper.clamp(target.getMaxHealth(), 0, 20);
        this.health = MathUtil.fast(health, currentHealth / maxHealth, 5);
        this.health = MathHelper.clamp(this.health, 0, 1);

        GlStateManager.pushMatrix();
        sizeAnimation(xPosition + (maxWidth / 2), yPosition + (maxHeight / 2), scale);
        drawStyledRect(xPosition, yPosition, maxWidth, maxHeight, 4);
        DisplayUtils.drawRoundFace(xPosition + 2.5f, yPosition + 2.5f, 22, 22, 4, 255, (AbstractClientPlayerEntity) target);



        int colorhealth = ColorUtils.interpolateColor(ColorUtils.red, ColorUtils.green, health);

        DisplayUtils.drawRoundedRect(xPosition + 2, yPosition + maxHeight - 8, (maxWidth - 3), 5, 2, ColorUtils.rgba(30,30,30,255));
        DisplayUtils.drawShadow(xPosition + 2, yPosition + maxHeight - 8, (maxWidth - 3) * this.health, 5, 4, colorhealth);
        DisplayUtils.drawRoundedRect(xPosition + 2, yPosition + maxHeight - 8, (maxWidth - 3) * this.health, 5, 2, colorhealth);

        drawItemStack(xPosition + 27, yPosition + 11, 12);
        Fonts.notoitalic[15].drawString(stack, targetName.substring(0, Math.min(targetName.length(), 10)), xPosition + 28, yPosition + 4, ThemeSwitcher.textcolor);

//        if ((int) MathUtil.round(this.health * 20, 0.5f) == 0) {
//            finalhealth = "Неизвестно";
//        } else {
//            finalhealth = (int) MathUtil.round(this.health * 20, 0.05f) + "";
//        }
//        Fonts.notoitalic[12].drawString(stack, "Hp: " + finalhealth, xPosition + 32, yPosition + 14, ThemeSwitcher.textcolor);


        GlStateManager.popMatrix();

        this.targetHUD.setWidth(maxWidth);
        this.targetHUD.setHeight(maxHeight);
    }


    // Проверка, находится ли цель в пределах досягаемости
    private boolean isTargetInRange(AbstractClientPlayerEntity target) {
        float distance = this.target.getDistance(target);
        return distance < 10.0F; // Установите желаемое расстояние
    }

    // Метод для рендеринга информации при наведении
    private void renderHoveredInfo(final MatrixStack stack, AbstractClientPlayerEntity target) {
        String hoveringInfo = "Имя: " + target.getName().getString() + "\nHP: " + getCurrentHealthText(target);
        // Отобразите информацию с помощью вашего метода отрисовки
        Fonts.notoitalic[12].drawString(stack, hoveringInfo, 50, 50, ThemeSwitcher.textcolor); // Позиции могут быть изменены
    }

    // Метод для получения текста текущего здоровья
    private String getCurrentHealthText(AbstractClientPlayerEntity target) {
        float currentHealth = fix1000Health(target);
        return currentHealth + "/" + target.getMaxHealth();
    }
    public void drawTargetHead(LivingEntity entity, float x, float y, float width, float height) {
        if (entity != null) {
            EntityRenderer<? super LivingEntity> rendererManager = mc.getRenderManager().getRenderer(entity);
            this.drawFace(rendererManager.getEntityTexture(entity), x, y, 8.0F, 8.0F, 8.0F, 8.0F, width, height, 64.0F, 64.0F, entity);
        }

    }
    public void drawFace(ResourceLocation res, float d, float y, float u, float v, float uWidth, float vHeight, float width, float height, float tileWidth, float tileHeight, LivingEntity target) {
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        mc.getTextureManager().bindTexture(res);
        float hurtPercent = ((float)target.hurtTime - (target.hurtTime != 0 ? mc.timer.renderPartialTicks : 0.0F)) / 30.0F;
        GL11.glColor4f(1.0F, 1.0F - hurtPercent, 1.0F - hurtPercent, 1.0F);
        AbstractGui.drawScaledCustomSizeModalRect(d, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
    }

    public static void sizeAnimation(double width, double height, double scale) {
        GlStateManager.translated(width, height, 0);
        GlStateManager.scaled(scale, scale, scale);
        GlStateManager.translated(-width, -height, 0);
    }

    private void drawItemStack(float x, float y, float offset) {
        List<ItemStack> stackList = new ArrayList<>(Arrays.asList(target.getHeldItemMainhand(), target.getHeldItemOffhand()));
        stackList.addAll((Collection<? extends ItemStack>) target.getArmorInventoryList());

        final AtomicReference<Float> posX = new AtomicReference<>(x);

        stackList.stream()
                .filter(stack -> !stack.isEmpty())
                .forEach(stack -> drawItemStack(stack,
                        posX.getAndAccumulate(offset, Float::sum),
                        y,
                        true,
                        true, 0.5f));
    }


    private PlayerEntity getTarget(PlayerEntity nullTarget) {
        PlayerEntity target = nullTarget;

        KillAura testAura = Main.getInstance().getFunctionRegistry().getHitAura();

        if (testAura.getTarget() instanceof PlayerEntity) {
            target = (PlayerEntity) testAura.getTarget();
            targetHudAnimation.setDirection(Direction.FORWARDS);
        } else if (IMinecraft.mc.currentScreen instanceof ChatScreen) {
            target = IMinecraft.mc.player;
            targetHudAnimation.setDirection(Direction.FORWARDS);
        } else {
            targetHudAnimation.setDirection(Direction.BACKWARDS);
        }

        return target;
    }

    public static int getColor(int index) {
        return getColor(index, 16);
    }

    public static int getColor(int index, float multitude) {
        StyleManager styleManager = Main.getInstance().getStyleManager();
        return ColorUtils.gradient(styleManager.getCurrentStyle().getFirstColor().getRGB(),
                styleManager.getCurrentStyle().getSecondColor().getRGB(), (int) (index * multitude), Math.max(20 - 5, 1));
    }

    public static int getColor(int firstColor, int secondColor, int index, float multitude) {
        return ColorUtils.gradient(firstColor, secondColor, (int) (index * multitude), Math.max(20 - 5, 1));
    }

    private float fix1000Health(Entity entity) {
        Score score = IMinecraft.mc.world.getScoreboard().getOrCreateScore(entity.getScoreboardName(),
                IMinecraft.mc.world.getScoreboard().getObjectiveInDisplaySlot(2));

        LivingEntity living = (LivingEntity) entity;

        return bypasshp.get() ? score.getScorePoints() : MathHelper.clamp(living.getHealth(), 0, 20);
    }


    public String getPotionAmplifer(EffectInstance e) {
        if (e.getAmplifier() == 1) {
            return "2";
        } else if (e.getAmplifier() == 2) {
            return "3";
        } else if (e.getAmplifier() == 3) {
            return "4";
        } else if (e.getAmplifier() == 4) {
            return "5";
        } else if (e.getAmplifier() == 5) {
            return "6";
        } else if (e.getAmplifier() == 6) {
            return "7";
        } else if (e.getAmplifier() == 7) {
            return "8";
        } else if (e.getAmplifier() == 8) {
            return "9";
        } else if (e.getAmplifier() == 9) {
            return "10";
        } else {
            return "";
        }
    }
    private boolean userConnectedToFunTimeAndEntityIsPlayer(Entity entity) {
        String header = IMinecraft.mc.ingameGUI.getTabList().header == null ? " " : IMinecraft.mc.ingameGUI.getTabList().header.getString().toLowerCase();
        return ClientUtil.isConnectedToServer("funtime") && ClientUtil.isConnectedToServer("play.saturn-x.space") && entity instanceof PlayerEntity;
    }

    public static int calculatePing() {
        return IMinecraft.mc.player.connection.getPlayerInfo(IMinecraft.mc.player.getUniqueID()) != null ?
                IMinecraft.mc.player.connection.getPlayerInfo(IMinecraft.mc.player.getUniqueID()).getResponseTime() : 0;
    }
}