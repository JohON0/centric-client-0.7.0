package centric.pl.functions.impl.render;

import centric.pl.command.friends.FriendStorage;
import centric.pl.events.impl.EventDisplay;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.settings.impl.BooleanSetting;
import centric.pl.functions.settings.impl.ModeListSetting;
import centric.pl.functions.settings.impl.ModeSetting;
import centric.pl.johon0.utils.client.ClientUtil;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.font.Fonts;
import centric.pl.johon0.utils.math.MathUtil;
import centric.pl.johon0.utils.projections.ProjectionUtil;
import centric.pl.johon0.utils.render.ColorUtils;
import centric.pl.johon0.utils.render.DisplayUtils;
import centric.pl.johon0.utils.render.font.FontsUtil;
import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import centric.pl.functions.impl.combat.AntiBot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.math.vector.Vector4f;
import org.joml.Vector4i;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.minecraft.scoreboard.Score;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.*;
import org.lwjgl.opengl.GL11;

import java.util.*;

import static net.minecraft.client.renderer.WorldRenderer.frustum;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;
@FunctionRegister(
        name = "ESP",
        type = Category.Render,
        beta = false
)
public class ESP extends Function {
    public ModeListSetting remove1 = (new ModeListSetting("Отображать", new BooleanSetting[]{new BooleanSetting("Предметы", true), new BooleanSetting("Эффекты", true), new BooleanSetting("Сферы и талисманы", true)}));
    public BooleanSetting bro9i = new BooleanSetting("Подсвчивать талисманы", true);
    private final HashMap<Entity, net.minecraft.util.math.vector.Vector4f> positions = new HashMap();

    public ESP() {
        this.addSettings(remove1, bro9i);
    }

    @Subscribe
    public void onDisplay(EventDisplay e) {
        if (mc.world != null && e.getType() == EventDisplay.Type.PRE) {
            this.Nursultan(e);
        }
    }

    public boolean isInView(Entity ent) {
        if (mc.getRenderViewEntity() == null) {
            return false;
        } else {
            WorldRenderer.frustum.setCameraPosition(mc.getRenderManager().info.getProjectedView().x, mc.getRenderManager().info.getProjectedView().y, mc.getRenderManager().info.getProjectedView().z);
            return WorldRenderer.frustum.isBoundingBoxInFrustum(ent.getBoundingBox()) || ent.ignoreFrustumCheck;
        }
    }

    private void drawPotions(MatrixStack matrixStack, LivingEntity entity, float posX, float posY) {
        for(Iterator var5 = entity.getActivePotionEffects().iterator(); var5.hasNext(); posY += Fonts.notoitalic[11].getFontHeight()) {
            EffectInstance pot = (EffectInstance)var5.next();
            int amp = pot.getAmplifier();
            String ampStr = "";
            if (amp >= 1 && amp <= 9) {
                String var10000 = "enchantment.level." + (amp + 1);
                ampStr = " " + I18n.format(var10000, new Object[0]);
            }

            String text = I18n.format(pot.getEffectName(), new Object[0]) + ampStr + " - " + EffectUtils.getPotionDurationString(pot, 1.0F);
            Fonts.notoitalic[11].drawString(matrixStack, text, posX, posY, -1);
        }

    }

    private void drawItems(MatrixStack matrixStack, LivingEntity entity, int posX, int posY) {
        int size = 8;
        int padding = 6;
        float fontHeight = Fonts.notoitalic[11].getFontHeight();
        List<ItemStack> items = new ArrayList();
        ItemStack mainStack = entity.getHeldItemMainhand();
        if (!mainStack.isEmpty()) {
            items.add(mainStack);
        }

        Iterator var10 = entity.getArmorInventoryList().iterator();

        while(var10.hasNext()) {
            ItemStack itemStack = (ItemStack)var10.next();
            if (!itemStack.isEmpty()) {
                items.add(itemStack);
            }
        }

        ItemStack offStack = entity.getHeldItemOffhand();
        if (!offStack.isEmpty()) {
            items.add(offStack);
        }

        posX = (int)((float)posX - (float)(items.size() * (size + padding)) / 2.0F);
        Iterator var21 = items.iterator();

        while(true) {
            ItemStack itemStack;
            do {
                if (!var21.hasNext()) {
                    return;
                }

                itemStack = (ItemStack)var21.next();
            } while(itemStack.isEmpty());

            GL11.glPushMatrix();
            this.glCenteredScale((float)posX, (float)posY, (float)size / 2.0F, (float)size / 2.0F, 0.5F);
            mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, posX, posY);
            mc.getItemRenderer().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, posX, posY, (String)null);
            GL11.glPopMatrix();
//            if (itemStack.isEnchanted() && (Boolean)this.remove.getValueByName("Зачарования").get()) {
//                int ePosY = (int)((float)posY - fontHeight);
//                Map<Enchantment, Integer> enchantmentsMap = EnchantmentHelper.getEnchantments(itemStack);
//                Iterator var15 = enchantmentsMap.keySet().iterator();
//
//                while(var15.hasNext()) {
//                    Enchantment enchantment = (Enchantment)var15.next();
//                    int level = (Integer)enchantmentsMap.get(enchantment);
//                    if (level >= 1 && enchantment.canApply(itemStack)) {
//                        IFormattableTextComponent iformattabletextcomponent = new TranslationTextComponent(enchantment.getName());
//                        String var10000 = iformattabletextcomponent.getString().substring(0, 2);
//                        String enchText = var10000 + level;
//                        Fonts.notoitalic[11].drawString(matrixStack, enchText, (float)posX, (float)ePosY, -1);
//                        ePosY -= (int)fontHeight;
//                    }
//                }
//            }

            posX += size + padding;
        }
    }

    public boolean isValid(Entity e) {
        return AntiBot.isBot(e) ? false : this.isInView(e);
    }

    public void glCenteredScale(float x, float y, float w, float h, float f) {
        GL11.glTranslatef(x + w / 2.0F, y + h / 2.0F, 0.0F);
        GL11.glScalef(f, f, 1.0F);
        GL11.glTranslatef(-x - w / 2.0F, -y - h / 2.0F, 0.0F);
    }

    public static void drawMcRect(double left, double top, double right, double bottom, int color) {
        double j;
        if (left < right) {
            j = left;
            left = right;
            right = j;
        }

        if (top < bottom) {
            j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        bufferbuilder.pos(left, bottom, 1.0).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(right, bottom, 1.0).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(right, top, 1.0).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(left, top, 1.0).color(f, f1, f2, f3).endVertex();
    }

    private void Nursultan(EventDisplay e) {
        this.positions.clear();
        new Vector4i(HUD.getColor(0, 1.0F), HUD.getColor(90, 1.0F), HUD.getColor(180, 1.0F), HUD.getColor(270, 1.0F));
        new Vector4i(ColorUtils.rgb(144, 238, 144), ColorUtils.rgb(0, 139, 0), ColorUtils.rgb(144, 238, 144), ColorUtils.rgb(0, 139, 0));
        int friendColorInt = FriendStorage.getColor();
        int colorInt = HUD.getColor(270);
        Minecraft var10000 = mc;
        Iterator var6 = mc.world.getAllEntities().iterator();

        while (true) {
            Entity entity;
            double z;
            do {
                do {
                    do {
                        if (!var6.hasNext()) {
                            RenderSystem.enableBlend();
                            RenderSystem.disableTexture();
                            RenderSystem.defaultBlendFunc();
                            RenderSystem.shadeModel(7425);
                            buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                            var6 = this.positions.entrySet().iterator();

                            LivingEntity living;
                            float hpui;
                            float nick;
                            Map.Entry entry;
                            while (var6.hasNext()) {
                                entry = (Map.Entry) var6.next();
                                net.minecraft.util.math.vector.Vector4f position = (net.minecraft.util.math.vector.Vector4f) entry.getValue();
                                Object var38 = entry.getKey();
                                if (var38 instanceof LivingEntity) {
                                    living = (LivingEntity) var38;
                                    float sect = 10.0F;
                                    float lineWidth = 1.0F;
                                    var10000 = mc;
                                    z = mc.player.getPosX();
                                    var10000 = mc;
                                    double playerY = mc.player.getPosY();
                                    var10000 = mc;
                                    double playerZ = mc.player.getPosZ();
                                    double entityX = (double) position.x;
                                    double entityY = (double) position.y;
                                    double entityZ = (double) position.z;
                                    double distance = Math.sqrt(Math.pow(z - entityX, 2.0) + Math.pow(playerY - entityY, 2.0) + Math.pow(playerZ - entityZ, 2.0));
                                    float sectChange = (float) (distance / 10.0);
                                    float calcSect = sect - sectChange;
                                    calcSect = Math.max(3.0F, calcSect);
                                    hpui = 3.0F;
                                    nick = 0.5F;
                                }
                            }

                            Tessellator.getInstance().draw();
                            RenderSystem.shadeModel(7424);
                            RenderSystem.enableTexture();
                            RenderSystem.disableBlend();
                            var6 = this.positions.entrySet().iterator();

                            while (true) {
                                while (var6.hasNext()) {
                                    entry = (Map.Entry) var6.next();
                                    Entity entity1 = (Entity) entry.getKey();
                                    float length;
                                    float hp;
                                    if (entity1 instanceof LivingEntity) {
                                        living = (LivingEntity) entity1;
                                        var10000 = mc;
                                        Scoreboard var59 = mc.world.getScoreboard();
                                        String var58 = living.getScoreboardName();
                                        Minecraft var10002 = mc;
                                        Score score = var59.getOrCreateScore(var58, mc.world.getScoreboard().getObjectiveInDisplaySlot(2));
                                        hp = living.getHealth();
                                        length = living.getMaxHealth();
                                        String header = mc.ingameGUI.getTabList().header == null ? " " : mc.ingameGUI.getTabList().header.getString().toLowerCase();
                                        if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP.contains("funtime") && (header.contains("анархия") || header.contains("гриферский"))) {
                                            hp = (float) score.getScorePoints();
                                            length = 20.0F;
                                        }

                                        net.minecraft.util.math.vector.Vector4f position = (net.minecraft.util.math.vector.Vector4f) entry.getValue();
                                        float width = position.z - position.x;
                                        String hpText = "[" + (int) hp + "]";
                                        float hpWidth = Fonts.notoitalic[24].getWidth(hpText);
                                        float hpPercent = MathHelper.clamp(hp / length, 0.0F, 1.0F);
                                        float var60 = position.y + (position.w - position.y) * (1.0F - hpPercent);
                                        float length1 = Fonts.notoitalic[24].getWidth(entity1.getDisplayName().toString());
                                        GL11.glPushMatrix();
                                        this.glCenteredScale(position.x + width / 2.0F - length1 / 2.0F, position.y - 7.0F, length1, 10.0F, 0.5F);
                                        String friendPrefix = FriendStorage.isFriend(entity1.getName().getString()) ? TextFormatting.GREEN + "[F] " : "";
                                        TextComponent name = (TextComponent) ITextComponent.getTextComponentOrEmpty(friendPrefix);
                                        name.append(entity1.getDisplayName());
                                        ItemStack stack = ((LivingEntity) entity1).getHeldItemOffhand();
                                        String nameS = "";
                                        String itemName = "";
                                        String nameItem = stack.getDisplayName().getString();
                                        PlayerEntity player;
                                        if (entity1 instanceof PlayerEntity) {
                                            player = (PlayerEntity) entity1;
                                            stack = player.getHeldItemOffhand();
                                            nameS = "";
                                            itemName = "";
                                        }

                                        if (entity1 instanceof PlayerEntity) {
                                            player = (PlayerEntity) entity1;
                                            if ((Boolean) this.remove1.getValueByName("Сферы и талисманы").get()) {
                                                stack = player.getHeldItemOffhand();
                                                nameS = "";
                                                itemName = stack.getDisplayName().getString();
                                                nameItem = stack.getDisplayName().getString();
                                                if (stack.getItem() == Items.PLAYER_HEAD || stack.getItem() == Items.TOTEM_OF_UNDYING) {
                                                    CompoundNBT tag = stack.getTag();
                                                    CompoundNBT display;
                                                    ListNBT lore;
                                                    String firstLore;
                                                    int levelIndex;
                                                    String gata;
                                                    if (tag != null && tag.contains("display", 10)) {
                                                        display = tag.getCompound("display");
                                                        if (display.contains("Lore", 9)) {
                                                            lore = display.getList("Lore", 8);
                                                            if (!lore.isEmpty()) {
                                                                firstLore = lore.getString(0);
                                                                levelIndex = firstLore.indexOf("Уровень");
                                                                if (levelIndex != -1) {
                                                                    gata = firstLore.substring(levelIndex + "Уровень".length()).trim();
                                                                    if (gata.contains("1/3")) {
                                                                        nameS = " 1/3";
                                                                    } else if (gata.contains("2/3")) {
                                                                        nameS = " 2/3";
                                                                    } else if (gata.contains("MAX")) {
                                                                        nameS = " MAX";
                                                                    } else {
                                                                        nameS = "";
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }

                                                    if (itemName.contains("Сфера")) {
                                                        if (itemName.contains("Афина")) {
                                                            itemName = "AFINA";
                                                        } else if (itemName.contains("Панекея")) {
                                                            itemName = "PANAKEYA";
                                                        } else if (itemName.contains("Магмы")) {
                                                            itemName = "MAGMA";
                                                        } else if (itemName.contains("Теургия")) {
                                                            itemName = "TEURGIYA";
                                                        } else if (itemName.contains("Иасо")) {
                                                            itemName = "IASO";
                                                        } else if (itemName.contains("Скифа")) {
                                                            itemName = "SKIFA";
                                                        } else if (itemName.contains("Абанты")) {
                                                            itemName = "ABANTA";
                                                        } else if (itemName.contains("Филона")) {
                                                            itemName = "FILONA";
                                                        } else if (itemName.contains("Сорана")) {
                                                            itemName = "SORANA";
                                                        } else if (itemName.contains("Эпиона")) {
                                                            itemName = "EPIONA";
                                                        } else if (itemName.contains("Пандо")) {
                                                            itemName = "PANDORA";
                                                        } else if (itemName.contains("Аполл")) {
                                                            itemName = "APOLLONA";
                                                        } else if (itemName.contains("Тит")) {
                                                            if ((Boolean) this.bro9i.get()) {
                                                                itemName = "FRIKA";
                                                            } else if (!(Boolean) this.bro9i.get()) {
                                                                itemName = "TITANA";
                                                            }
                                                        } else if (itemName.contains("Осир")) {
                                                            itemName = "OSIRISA";
                                                        } else if (itemName.contains("Андро")) {
                                                            itemName = "ANDROMEDA";
                                                        } else if (itemName.contains("Хим")) {
                                                            itemName = "XIMERI";
                                                        } else if (itemName.contains("Астр")) {
                                                            itemName = "ASTREYA";
                                                        }
                                                    } else if (itemName.contains("Талисман")) {
                                                        display = tag.getCompound("display");
                                                        lore = display.getList("Lore", 8);
                                                        firstLore = lore.getString(0);
                                                        levelIndex = firstLore.indexOf("Уровень");
                                                        if (levelIndex != -1) {
                                                            gata = firstLore.substring(levelIndex + "Уровень".length()).trim();
                                                            if (gata.contains("1/3")) {
                                                                nameS = " 1/3";
                                                            } else if (gata.contains("2/3")) {
                                                                nameS = " 2/3";
                                                            } else if (gata.contains("MAX")) {
                                                                nameS = " MAX";
                                                            } else {
                                                                nameS = "";
                                                            }
                                                        }

                                                        if (itemName.contains("Фугу")) {
                                                            itemName = "FUGU";
                                                        } else if (itemName.contains("Эгида")) {
                                                            itemName = "EGIDA";
                                                        } else if (itemName.contains("Крайта")) {
                                                            itemName = "KRAITA";
                                                        } else if (itemName.contains("Лекаря")) {
                                                            itemName = "LEKARYA";
                                                        } else if (itemName.contains("Манеса")) {
                                                            itemName = "MANESA";
                                                        } else if (itemName.contains("Кобры")) {
                                                            itemName = "KOBRA";
                                                        } else if (itemName.contains("Диониса")) {
                                                            itemName = "DIONISA";
                                                        } else if (itemName.contains("Гефеста")) {
                                                            itemName = "GEFESTA";
                                                        } else if (itemName.contains("Хауберка")) {
                                                            itemName = "HAUBERKA";
                                                        } else if (itemName.contains("Крушителя")) {
                                                            itemName = "KRUSH";
                                                        } else if (itemName.contains("Грани")) {
                                                            itemName = "GRANI";
                                                        } else if (itemName.contains("Дедала")) {
                                                            itemName = "DEDALA";
                                                        } else if (itemName.contains("Тритона")) {
                                                            itemName = "TRITONA";
                                                        } else if (itemName.contains("Гармонии")) {
                                                            itemName = "GARMONII";
                                                        } else if (itemName.contains("Феникса")) {
                                                            itemName = "FENIXA";
                                                        } else if (itemName.contains("Ехидны")) {
                                                            itemName = "EHIDNA";
                                                        } else if (itemName.contains("Карателя")) {
                                                            itemName = "KARATEL";
                                                        }
                                                    } else {
                                                        itemName = "";
                                                        nameS = "";
                                                        nameItem = "";
                                                    }
                                                }
                                            }
                                        }

                                        itemName = TextFormatting.GRAY + "[" + TextFormatting.RED + itemName;
                                        nameS = TextFormatting.RED + nameS;
                                        if (ClientUtil.isConnectedToServer("funtime".toLowerCase()) && stack.getItem() == Items.TOTEM_OF_UNDYING) {
                                            itemName = "";
                                            nameS = "";
                                        }

                                        hpui = Fonts.notoitalic[24].getWidth(TextFormatting.GRAY + "[" + TextFormatting.RED + (int) hp + TextFormatting.GRAY + "]");
                                        nick = Fonts.notoitalic[24].getWidth(name.getString() + " ") - Fonts.notoitalic[24].getWidth(name.getString()) / 2.0F;
                                        if ((Boolean) this.remove1.getValueByName("Сферы и талисманы").get()) {
                                            if ((stack.getDisplayName().getString().contains("Сфера") || stack.getDisplayName().getString().contains("Талисман")) && !ClientUtil.isConnectedToServer("funtime")) {
                                                DisplayUtils.drawRoundedRect(position.x - Fonts.notoitalic[24].getWidth(name.getString()) / 2.0F - 2.5F + 8.25F, position.y - 10.5F, Fonts.notoitalic[24].getWidth(name.getString()) + 9.0F + Fonts.notoitalic[24].getWidth(TextFormatting.GRAY + "[" + TextFormatting.RED + (int) hp + TextFormatting.GRAY + "]") + Fonts.notoitalic[24].getWidth(itemName + nameS) + 3.0F, 17.0F, 0.0F, ColorUtils.rgba(0, 0, 0, 150));
                                            }

                                            if (stack.getItem() == Items.PLAYER_HEAD && ClientUtil.isConnectedToServer("funtime") && stack.getDisplayName().getString().contains("Сфера")) {
                                                DisplayUtils.drawRoundedRect(position.x - Fonts.notoitalic[24].getWidth(name.getString()) / 2.0F - 2.5F + 8.25F, position.y - 10.5F, Fonts.notoitalic[24].getWidth(name.getString()) + 9.0F + Fonts.notoitalic[24].getWidth(TextFormatting.GRAY + "[" + TextFormatting.RED + (int) hp + TextFormatting.GRAY + "]") + Fonts.notoitalic[24].getWidth(itemName + nameS) + 3.0F, 17.0F, 0.0F, ColorUtils.rgba(0, 0, 0, 150));
                                            } else if (!stack.getDisplayName().getString().contains("Сфера") && !stack.getDisplayName().getString().contains("Талисман")) {
                                                DisplayUtils.drawRoundedRect(position.x - Fonts.notoitalic[24].getWidth(name.getString()) / 2.0F - 2.5F + 8.25F, position.y - 10.5F, Fonts.notoitalic[24].getWidth(name.getString()) + 5.0F + Fonts.notoitalic[24].getWidth(TextFormatting.GRAY + "[" + TextFormatting.RED + (int) hp + TextFormatting.GRAY + "]") + 3.0F, 17.0F, 0.0F, ColorUtils.rgba(0, 0, 0, 150));
                                            }
                                        }

                                        if (!(Boolean) this.remove1.getValueByName("Сферы и талисманы").get()) {
                                            DisplayUtils.drawRoundedRect(position.x - Fonts.notoitalic[24].getWidth(name.getString()) / 2.0F - 2.5F + 8.25F, position.y - 10.5F, Fonts.notoitalic[24].getWidth(name.getString()) + 5.0F + Fonts.notoitalic[24].getWidth(TextFormatting.GRAY + "[" + TextFormatting.RED + (int) hp + TextFormatting.GRAY + "]") + 3.0F, 17.0F, 0.0F, ColorUtils.rgba(0, 0, 0, 150));
                                        }

                                        Fonts.notoitalic[24].drawString(e.getMatrixStack(), name.getString() + " ", (double) (position.x - Fonts.notoitalic[24].getWidth(name.getString()) / 2.0F + 9.5F), (double) (position.y - 7.0F), -1);
                                        Fonts.notoitalic[24].drawString(e.getMatrixStack(), TextFormatting.GRAY + "[" + TextFormatting.RED + (int) hp + TextFormatting.GRAY + "]", (double) (position.x + Fonts.notoitalic[24].getWidth(name.getString()) - Fonts.notoitalic[24].getWidth(name.getString()) / 2.0F + 1.5F + 9.5F), (double) (position.y - 7.0F), ColorUtils.rgb(230, 100, 100));
                                        if ((Boolean) this.remove1.getValueByName("Сферы и талисманы").get() && (nameItem.contains("Талисман") || nameItem.contains("Сфера")) && !itemName.contains("бессмертия")) {
                                            if (nameItem.contains("Сфера")) {
                                                Fonts.notoitalic[24].drawString(e.getMatrixStack(), itemName + nameS + TextFormatting.GRAY + "]", (double) (position.x + nick + hpui - 1.0F + 9.5F), (double) (position.y - 7.0F), ColorUtils.rgb(255, 255, 255));
                                            } else if (nameItem.contains("Талисман")) {
                                                Fonts.notoitalic[24].drawString(e.getMatrixStack(), itemName + nameS + TextFormatting.GRAY + "]", (double) (position.x + nick + hpui - 1.0F + 9.5F), (double) (position.y - 7.0F), ColorUtils.rgb(255, 255, 255));
                                            }
                                        }

                                        GL11.glPopMatrix();
                                        if ((Boolean) this.remove1.getValueByName("Эффекты").get()) {
                                            this.drawPotions(e.getMatrixStack(), living, position.z + 2.0F, position.y + 10);
                                        }

                                        if ((Boolean) this.remove1.getValueByName("Предметы").get()) {
                                            this.drawItems(e.getMatrixStack(), living, (int) (position.x + width / 2.0F), (int) (position.y - 20.0F));
                                        }
                                    } else if (entity1 instanceof ItemEntity) {
                                        ItemEntity item = (ItemEntity) entity1;
                                        net.minecraft.util.math.vector.Vector4f position = (net.minecraft.util.math.vector.Vector4f) entry.getValue();
                                        hp = position.z - position.x;
                                        length = (float) mc.fontRenderer.getStringPropertyWidth(entity1.getDisplayName());
                                        GL11.glPushMatrix();
                                        this.glCenteredScale(position.x + hp / 2.0F - length / 2.0F, position.y - 7.0F, length, 10.0F, 0.5F);
                                        GL11.glPopMatrix();
                                    }
                                }

                                return;
                            }
                        }

                        entity = (Entity) var6.next();
                    } while (!this.isValid(entity));
                } while (!(entity instanceof PlayerEntity) && !(entity instanceof ItemEntity));

                Minecraft var10001 = mc;
            } while (entity == mc.player && mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON);

            double x = MathUtil.interpolate(entity.getPosX(), entity.lastTickPosX, (double) e.getPartialTicks());
            double y = MathUtil.interpolate(entity.getPosY(), entity.lastTickPosY, (double) e.getPartialTicks());
            z = MathUtil.interpolate(entity.getPosZ(), entity.lastTickPosZ, (double) e.getPartialTicks());
            Vector3d size = new Vector3d(entity.getBoundingBox().maxX - entity.getBoundingBox().minX, entity.getBoundingBox().maxY - entity.getBoundingBox().minY, entity.getBoundingBox().maxZ - entity.getBoundingBox().minZ);
            AxisAlignedBB aabb = new AxisAlignedBB(x - size.x / 2.0, y, z - size.z / 2.0, x + size.x / 2.0, y + size.y, z + size.z / 2.0);
            net.minecraft.util.math.vector.Vector4f position = null;

            for (int i = 0; i < 8; ++i) {
                Vector2f vector = ProjectionUtil.project(i % 2 == 0 ? aabb.minX : aabb.maxX, i / 2 % 2 == 0 ? aabb.minY : aabb.maxY, i / 4 % 2 == 0 ? aabb.minZ : aabb.maxZ);
                if (position == null) {
                    position = new Vector4f(vector.x, vector.y, 1.0F, 1.0F);
                } else {
                    position.x = Math.min(vector.x, position.x);
                    position.y = Math.min(vector.y, position.y);
                    position.z = Math.max(vector.x, position.z);
                    position.w = Math.max(vector.y, position.w);
                }
            }

            this.positions.put(entity, position);
        }
    }
}
