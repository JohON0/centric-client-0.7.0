package centric.pl.command.impl.feature;

import centric.pl.command.*;
import centric.pl.command.impl.CommandException;
import centric.pl.functions.api.FunctionRegistry;
import centric.pl.functions.impl.misc.UnHook;
import centric.pl.functions.impl.render.HUD;
import centric.pl.johon0.utils.font.Fonts;
import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.platform.GlStateManager;
import centric.pl.Main;
import centric.pl.events.impl.EventDisplay;
import centric.pl.functions.impl.render.Pointers;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.render.ColorUtils;
import centric.pl.johon0.utils.render.DisplayUtils;
import centric.pl.johon0.utils.render.font.FontsUtil;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TextFormatting;
import org.joml.Vector4i;

import java.awt.*;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class GPSCommand implements Command, CommandWithAdvice, IMinecraft {
    final Prefix prefix;
    final Logger logger;
    Vector2f cordsMap = new Vector2f(0, 0);

    public GPSCommand(Prefix prefix, Logger logger) {
        this.prefix = prefix;
        this.logger = logger;
        Main.getInstance().getEventBus().register(this);
    }

    @Override
    public void execute(Parameters parameters) {
        String commandType = parameters.asString(0).orElse("");

        switch (commandType) {
            case "add" -> addGPS(parameters);
            case "off" -> removeGPS();
            default ->
                    throw new CommandException(TextFormatting.RED + "Укажите тип команды:" + TextFormatting.GRAY + " add, off");
        }
    }

    private void addGPS(Parameters param) {
        int x = param.asInt(1)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите первую координату!"));
        int z = param.asInt(2)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите вторую координату!"));

        if (x == 0 && z == 0) {
            logger.log("Координаты должны быть больше нуля.");
            return;
        }

        cordsMap = new Vector2f(x, z);

    }

    private void removeGPS() {
        cordsMap = new Vector2f(0, 0);
    }

    @Override
    public String name() {
        return "gps";
    }

    @Override
    public String description() {
        return "Показывает стрелочку которая ведёт к координатам";
    }

    @Override
    public List<String> adviceMessage() {
        String commandPrefix = prefix.get();
        return List.of(commandPrefix + "gps add <x, z> - Проложить путь",
                commandPrefix + "gps off - Удалить GPS",
                "Пример: " + TextFormatting.RED + commandPrefix + "gps add 100 150"
        );
    }

    @Subscribe
    private void onDisplay(EventDisplay e) {
        FunctionRegistry functionRegistry = Main.getInstance().getFunctionRegistry();
        UnHook selfDestruct = functionRegistry.getUnHook();

        if (selfDestruct.enabled || cordsMap.x == 0 && cordsMap.y == 0) {
            return;
        }

        Vector3d vec3d = new Vector3d(
                cordsMap.x + 0.5,
                100 + 0.5,
                cordsMap.y + 0.5
        );
        int dst = (int) Math.sqrt(Math.pow(vec3d.x - mc.player.getPosX(), 2) + Math.pow(vec3d.z - mc.player.getPosZ(), 2));
        Vector3d localVec = vec3d.subtract(mc.getRenderManager().info.getProjectedView());

        double x = localVec.getX();
        double z = localVec.getZ();

        double cos = MathHelper.cos((float) (mc.getRenderManager().info.getYaw() * (Math.PI * 2 / 360)));
        double sin = MathHelper.sin((float) (mc.getRenderManager().info.getYaw() * (Math.PI * 2 / 360)));
        double rotY = -(z * cos - x * sin);
        double rotX = -(x * cos + z * sin);


        float angle = (float) (Math.atan2(rotY, rotX) * 180 / Math.PI);

        double x2 = 30 * MathHelper.cos((float) Math.toRadians(angle)) + window.getScaledWidth() / 2f;
        double y2 = 30 * MathHelper.sin((float) Math.toRadians(angle)) + window.getScaledHeight() / 2f;

        GlStateManager.pushMatrix();
        GlStateManager.disableBlend();
        GlStateManager.translated(x2, y2, 0);
        GlStateManager.rotatef(angle, 0, 0, 1);

        DisplayUtils.drawImage(new ResourceLocation("centric/images/arrowgpss.png"), -14.5F, -7F, 18.0F, 18.0F, -1);

        GlStateManager.rotatef(90, 0, 0, 1);
//        HUD.drawStyledRect(-6f, 16, Fonts.notoitalic[14].getWidth(text)+5, Fonts.notoitalic[14].getFontHeight(),4);
        Fonts.notoitalic[14].drawCenteredString(e.getMatrixStack(), (int) dst + "m", 2, 18, -1);

        GlStateManager.enableBlend();
        GlStateManager.popMatrix();
    }
}
