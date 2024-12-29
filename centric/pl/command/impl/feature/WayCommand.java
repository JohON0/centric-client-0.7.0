//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package centric.pl.command.impl.feature;

import centric.pl.command.impl.CommandException;
import centric.pl.functions.api.FunctionRegistry;
import centric.pl.functions.impl.misc.UnHook;
import centric.pl.johon0.utils.font.Fonts;
import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.platform.GlStateManager;
import centric.pl.Main;
import centric.pl.command.Command;
import centric.pl.command.CommandWithAdvice;
import centric.pl.command.Logger;
import centric.pl.command.MultiNamedCommand;
import centric.pl.command.Parameters;
import centric.pl.command.Prefix;
import centric.pl.events.impl.EventDisplay;
import centric.pl.functions.impl.render.Pointers;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.projections.ProjectionUtil;
import centric.pl.johon0.utils.render.ColorUtils;
import centric.pl.johon0.utils.render.DisplayUtils;
import centric.pl.johon0.utils.render.font.FontsUtil;
import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.TextFormatting;

public class WayCommand implements Command, CommandWithAdvice, MultiNamedCommand, IMinecraft {
    private final Prefix prefix;
    private final Logger logger;
    private final Map<String, Vector3i> waysMap = new LinkedHashMap();

    public WayCommand(Prefix prefix, Logger logger) {
        this.prefix = prefix;
        this.logger = logger;
        Main.getInstance().getEventBus().register(this);
    }

    public void execute(Parameters parameters) {
        switch ((String)parameters.asString(0).orElse("")) {
            case "add":
                this.addWayPoint(parameters);
                break;
            case "remove":
                this.removeWayPoint(parameters);
                break;
            case "clear":
                this.waysMap.clear();
                this.logger.log("Все пути были удалены!");
                break;
            case "list":
                this.logger.log("Список путей:");
                Iterator var5 = this.waysMap.keySet().iterator();

                while(var5.hasNext()) {
                    String s = (String)var5.next();
                    this.logger.log("- " + s + " " + this.waysMap.get(s));
                }

                return;
            default:
                throw new CommandException(TextFormatting.RED + "Укажите тип команды:" + TextFormatting.GRAY + " add, remove, clear");
        }

    }

    private void addWayPoint(Parameters param) {
        String name = (String)param.asString(1).orElseThrow(() -> {
            return new CommandException(TextFormatting.RED + "Укажите имя координаты!");
        });
        int x = (Integer)param.asInt(2).orElseThrow(() -> {
            return new CommandException(TextFormatting.RED + "Укажите первую координату!");
        });
        int y = (Integer)param.asInt(3).orElseThrow(() -> {
            return new CommandException(TextFormatting.RED + "Укажите вторую координату!");
        });
        int z = (Integer)param.asInt(4).orElseThrow(() -> {
            return new CommandException(TextFormatting.RED + "Укажите третью координату!");
        });
        Vector3i vec = new Vector3i(x, y, z);
        this.waysMap.put(name, vec);
        this.logger.log("Путь " + name + " был добавлен!");
    }

    private void removeWayPoint(Parameters param) {
        String name = (String)param.asString(1).orElseThrow(() -> {
            return new CommandException(TextFormatting.RED + "Укажите имя координаты!");
        });
        this.waysMap.remove(name);
        this.logger.log("Путь " + name + " был удалён!");
    }

    public String name() {
        return "way";
    }

    public String description() {
        return "Позволяет работать с координатами путей";
    }

    public List<String> adviceMessage() {
        String commandPrefix = this.prefix.get();
        return List.of(commandPrefix + "waypoint add <имя, x, y, z> - Проложить путь к WayPoint'у", commandPrefix + "waypoint remove <имя> - Удалить WayPoint", commandPrefix + "waypoint list - Список WayPoint'ов", commandPrefix + "waypoint clear - Очистить список WayPoint'ов", "Пример: " + TextFormatting.RED + commandPrefix + "way add аирдроп 1000 100 1000");
    }

    @Subscribe
    private void onDisplay(EventDisplay e) {
        FunctionRegistry functionRegistry = Main.getInstance().getFunctionRegistry();
        UnHook selfDestruct = functionRegistry.getUnHook();
        if (!selfDestruct.enabled) {
            if (!this.waysMap.isEmpty()) {
                Iterator var4 = this.waysMap.keySet().iterator();

                while(var4.hasNext()) {
                    String name = (String)var4.next();
                    Vector3i vec3i = (Vector3i)this.waysMap.get(name);
                    Vector3d vec3d = new Vector3d((double)vec3i.getX() + 0.5, (double)vec3i.getY() + 0.5, (double)vec3i.getZ() + 0.5);
                    Vector2f vec2f = ProjectionUtil.project(vec3d.x, vec3d.y, vec3d.z);
                    int distance = (int)Minecraft.getInstance().player.getPositionVec().distanceTo(vec3d);
                    String text = name + " (" + distance + "M)";
                    if (vec2f.equals(new Vector2f(Float.MAX_VALUE, Float.MAX_VALUE))) {
                        Vector3d localVec = vec3d.subtract(mc.getRenderManager().info.getProjectedView());
                        double x = localVec.getX();
                        double z = localVec.getZ();
                        double cos = (double)MathHelper.cos((float)((double)mc.getRenderManager().info.getYaw() * 0.017453292519943295));
                        double sin = (double)MathHelper.sin((float)((double)mc.getRenderManager().info.getYaw() * 0.017453292519943295));
                        double rotY = -(z * cos - x * sin);
                        double rotX = -(x * cos + z * sin);
                        float angle = (float)(Math.atan2(rotY, rotX) * 180.0 / Math.PI);
                        double x2 = (double)(30.0F * MathHelper.cos((float)Math.toRadians((double)angle)) + (float)window.getScaledWidth() / 2.0F);
                        double y2 = (double)(30.0F * MathHelper.sin((float)Math.toRadians((double)angle)) + (float)window.getScaledHeight() / 2.0F);
                        GlStateManager.pushMatrix();
                        GlStateManager.disableBlend();
                        GlStateManager.translated(x2, y2, 0.0);
                        GlStateManager.rotatef(angle, 0.0F, 0.0F, 1.0F);
                        GlStateManager.enableBlend();
                        GlStateManager.popMatrix();
                    } else {

                        float textWith = Fonts.notoitalic[13].getWidth(text);
                        float fontHeight = Fonts.notoitalic[13].getFontHeight();

                        float posX = vec2f.x - textWith / 2;
                        float posY = vec2f.y - fontHeight / 2;

                        float padding = 2;
                        DisplayUtils.drawShadow(posX - padding, posY - padding, padding + textWith + padding, padding + fontHeight + padding, 4, ColorUtils.rgba(0, 0, 0, 128));
                        DisplayUtils.drawRoundedRect(posX - padding, posY - padding, padding + textWith + padding, padding + fontHeight + padding, 4, ColorUtils.rgba(0, 0, 0, 128));
                        Fonts.notoitalic[13].drawString(e.getMatrixStack(), text, posX, posY + 3, -1);
                    }
                }

            }
        }
    }

    public List<String> aliases() {
        return List.of("waypoint");
    }
}
