package centric.pl;

import centric.pl.command.*;
import centric.pl.command.friends.FriendStorage;
import centric.pl.command.impl.*;
import centric.pl.command.impl.feature.*;
import centric.pl.command.staffs.StaffStorage;
import centric.pl.managers.StyleManager;
import centric.pl.managers.configManager.ConfigStorage;
import centric.pl.events.impl.EventKey;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegistry;
import centric.pl.managers.MacroManager;
import centric.pl.managers.notificationManager.Notification;
import centric.pl.managers.styleManager.*;
import centric.pl.ui.clickgui.MainScreen;
import centric.pl.ui.guiv2.ClickGui;
import centric.pl.ui.imgui.Imgui;
import centric.pl.ui.mainmenu.altmanager.AltConfig;
import centric.pl.ui.mainmenu.altmanager.AltWidget;
import centric.pl.johon0.utils.TPSCalc;
import centric.pl.johon0.utils.client.ServerTPS;
import centric.pl.johon0.utils.discordrpc.DiscordRichPresenceUtil;
import centric.pl.johon0.utils.drag.DragManager;
import centric.pl.johon0.utils.drag.Dragging;
import centric.pl.johon0.utils.font.Fonts;
//import centric.pl.ui.midnight.ClickGui;
import centric.pl.ui.playerUI.MusicUI;
import com.google.common.eventbus.EventBus;
import centric.pl.functions.impl.combat.killAura.rotation.RotationHandler;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;
import via.ViaMCP;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.mojang.blaze3d.vertex.IVertexBuilder.LOGGER;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Main {
    final Random random = new Random();

    public boolean playerOnServer = false;
    public static final String CLIENT_NAME = "centric recode";
    @Getter
    private static Main instance;

    // Менеджеры
    private FunctionRegistry functionRegistry;

    private ConfigStorage configStorage;
    private CommandDispatcher commandDispatcher;
    private ServerTPS serverTPS;
    private Notification notification;
    private MacroManager macroManager;
    private StyleManager styleManager;
    public static String version = "0.8.0";
    public static int image;
    // Менеджер событий и скриптов
    private final EventBus eventBus = new EventBus();

    // Директории
    private final File clientDir = new File(Minecraft.getInstance().gameDir + "\\jre");
    private final File filesDir = new File(Minecraft.getInstance().gameDir + "\\jre\\files");

    // Элементы интерфейса
    private AltWidget altWidget;
    private AltConfig altConfig;
    private Imgui mainScreen;
    private MusicUI musicPlayerUI;

    // Конфигурация и обработчики
    private ViaMCP viaMCP;
    private TPSCalc tpsCalc;
    private RotationHandler rotationHandler;
    public Main() {
        instance = this;

        if (!clientDir.exists()) {
            clientDir.mkdirs();
        }
        if (!filesDir.exists()) {
            filesDir.mkdirs();
        }

        clientLoad();
        FriendStorage.load();
        StaffStorage.load();
        ThemeSwitcher.themelightofdark = true;
    }

    public Dragging createDrag(Function module, String name, float x, float y) {
        DragManager.draggables.put(name, new Dragging(module, name, x, y));
        return DragManager.draggables.get(name);
    }
    private void clientLoad() {
        image = random.nextInt(1, 5);
        functionRegistry = new FunctionRegistry();
        viaMCP = new ViaMCP();
        serverTPS = new ServerTPS();
        macroManager = new MacroManager();
        configStorage = new ConfigStorage();
        notification = new Notification();
        initCommands();
        initStyles();
        altWidget = new AltWidget();
        altConfig = new AltConfig();
        tpsCalc = new TPSCalc();
        rotationHandler = new RotationHandler();

        try {
            altConfig.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            configStorage.init();
        } catch (IOException e) {
            LOGGER.error("Ошибка при подгрузке конфига.");
        }
        try {
            macroManager.init();
        } catch (IOException e) {
            LOGGER.error("Ошибка при подгрузке конфига макросов.");
        }
        DragManager.load();
        mainScreen = new Imgui();
        musicPlayerUI = new MusicUI(new StringTextComponent("A"));

        Fonts.init();
        ThemeSwitcher.setTheme(ThemeSwitcher.themelightofdark = true);
        eventBus.register(this);
        DiscordRichPresenceUtil.startDiscord();
        System.out.println("стартуем");
        if (!Minecraft.getInstance().getSession().getUsername().equals("JohON0")) {
            Util.getOSType().openURI("https://t.me/centricclient");
            Util.getOSType().openURI("https://discord.com/invite/UagEAQuUFK");
        }
    }


    public void onKeyPressed(int key) {
        if (functionRegistry.getUnHook().enabled) return;
        Main.getInstance().getEventBus().post(new EventKey(key));

        macroManager.onKeyPressed(key);

        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            Minecraft.getInstance().displayGuiScreen(mainScreen);
        }

        if (this.functionRegistry.getMusicPlayerUI().isState() && (Integer)this.functionRegistry.getMusicPlayerUI().setting.get() == key) {
            Minecraft.getInstance().displayGuiScreen(musicPlayerUI);
        }

    }
    private void initCommands() {
        Minecraft mc = Minecraft.getInstance();
        Logger logger = new MultiLogger(List.of(new ConsoleLogger(), new MinecraftLogger()));
        List<Command> commands = new ArrayList<>();
        Prefix prefix = new PrefixImpl();
        commands.add(new ListCommand(commands, logger));
        commands.add(new FriendCommand(prefix, logger, mc));
        commands.add(new BindCommand(prefix, logger));
        commands.add(new GPSCommand(prefix, logger));
        commands.add(new WayCommand(prefix, logger));
        commands.add(new ConfigCommand(configStorage, prefix, logger));
        commands.add(new MacroCommand(macroManager, prefix, logger));
        commands.add(new VClipCommand(prefix, logger, mc));
        commands.add(new HClipCommand(prefix, logger, mc));
        commands.add(new StaffCommand(prefix, logger));
        commands.add(new MemoryCommand(logger));
        commands.add(new PanicCommand(logger));
        commands.add(new RCTCommand(logger, mc));
        commands.add(new AutoPilotCommand(prefix,logger));

        AdviceCommandFactory adviceCommandFactory = new AdviceCommandFactoryImpl(logger);
        ParametersFactory parametersFactory = new ParametersFactoryImpl();

        commandDispatcher = new StandaloneCommandDispatcher(commands, adviceCommandFactory, prefix, parametersFactory, logger);
    }
    private void initStyles() {
        StyleFactory styleFactory = new StyleFactoryImpl();
        List<Style> styles = new ArrayList<>();
        styles.add(styleFactory.createStyle("Лавандовый", new Color(230, 230, 250), new Color(138, 43, 226)));
        styles.add(styleFactory.createStyle("Теплый коралл", new Color(255, 127, 80), new Color(255, 99, 71)));
        styles.add(styleFactory.createStyle("Мятный", new Color(152, 255, 152), new Color(0, 128, 0)));
        styles.add(styleFactory.createStyle("Огненно-оранжевый", new Color(255, 69, 0), new Color(255, 165, 0)));
        styles.add(styleFactory.createStyle("Нежный голубой", new Color(173, 216, 230), new Color(70, 130, 180)));
        styles.add(styleFactory.createStyle("Сахарная вата", new Color(255, 182, 193), new Color(255, 228, 225)));
        styles.add(styleFactory.createStyle("Лесной зеленый", new Color(34, 139, 34), new Color(85, 107, 47)));
        styles.add(styleFactory.createStyle("Темный индиго", new Color(75, 0, 130), new Color(148, 0, 211)));
        styles.add(styleFactory.createStyle("Морская пена", new Color(173, 255, 47), new Color(0, 204, 204)));
        styles.add(styleFactory.createStyle("Серый вельвет", new Color(190, 190, 190), new Color(128, 128, 128)));
        styleManager = new StyleManager(styles, styles.get(0));
    }

}
