//package centric.pl.functions.impl.movement;
//
//import centric.pl.Main;
//import centric.pl.events.impl.EventMotion;
//import centric.pl.functions.api.Category;
//import centric.pl.functions.api.Function;
//import centric.pl.functions.api.FunctionRegister;
//import centric.pl.functions.settings.impl.SliderSetting;
//import centric.pl.johon0.utils.client.IMinecraft;
//import centric.pl.johon0.utils.math.StopWatch;
//import centric.pl.johon0.utils.player.MouseUtil;
//import com.google.common.eventbus.Subscribe;
//import centric.pl.functions.settings.impl.ModeSetting;
//import net.minecraft.item.BlockItem;
//import net.minecraft.item.Items;
//import net.minecraft.util.Hand;
//import net.minecraft.util.math.BlockRayTraceResult;
//import net.minecraft.util.math.MathHelper;
//import net.minecraft.util.math.RayTraceResult;
//
//@FunctionRegister(name = "Spider", type = Category.Movement)
//public class Spider extends Function {
//    public ModeSetting mode = new ModeSetting("Mode", "Grim", "Grim", "Matrix");
//    private final SliderSetting spiderSpeed = new SliderSetting(
//            "Speed",
//            2.0f,
//            1.0f,
//            10.0f,
//            0.05f
//    ).setVisible(() -> !mode.is("Grim"));
//
//    StopWatch stopWatch = new StopWatch();
//
//
//    public Spider() {
//        addSettings(spiderSpeed, mode);
//    }
//
//    @Subscribe
//    private void onMotion(EventMotion motion) {
//        switch (mode.get()) {
//            case "Matrix" -> {
//                if (!IMinecraft.mc.player.collidedHorizontally) {
//                    return;
//                }
//                long speed = MathHelper.clamp(500 - (spiderSpeed.get().longValue() / 2 * 100), 0, 500);
//                if (stopWatch.isReached(speed)) {
//                    motion.setOnGround(true);
//                    IMinecraft.mc.player.setOnGround(true);
//                    IMinecraft.mc.player.collidedVertically = true;
//                    IMinecraft.mc.player.collidedHorizontally = true;
//                    IMinecraft.mc.player.isAirBorne = true;
//                    IMinecraft.mc.player.jump();
//                    stopWatch.reset();
//                }
//            }
//            case "Grim" -> {
//                int slotInHotBar = getSlotInInventoryOrHotbar(true);
//
//                if (slotInHotBar == -1) {
//                    print("Блоки не найдены!");
//                    toggle();
//                    return;
//                }
//                if (!IMinecraft.mc.player.collidedHorizontally) {
//                    Main.getInstance().getFunctionRegistry().getKillAura().canAttackWithSpider = true;
//                    return;
//                }
//
//                Main.getInstance().getFunctionRegistry().getKillAura().nextAttackDelay = 10;
//                Main.getInstance().getFunctionRegistry().getKillAura().canAttackWithSpider = false;
//                Main.getInstance().getFunctionRegistry().getKillAura().lastPosY = 0;
//
//                if (IMinecraft.mc.player.isOnGround()) {
//                    motion.setOnGround(true);
//                    IMinecraft.mc.player.setOnGround(true);
//                    IMinecraft.mc.player.jump();
//                }
//                if (IMinecraft.mc.player.fallDistance > 0 && IMinecraft.mc.player.fallDistance < 2) {
//
//                    int last = IMinecraft.mc.player.inventory.currentItem;
//                    IMinecraft.mc.player.inventory.currentItem = slotInHotBar;
//
//                    motion.setPitch(80);
//                    motion.setYaw(IMinecraft.mc.player.getHorizontalFacing().getHorizontalAngle());
//                    IMinecraft.mc.player.rotationPitchHead = 80;
//                    IMinecraft.mc.player.rotationYawHead = IMinecraft.mc.player.getHorizontalFacing().getHorizontalAngle();
//                    IMinecraft.mc.player.renderYawOffset = IMinecraft.mc.player.getHorizontalFacing().getHorizontalAngle();
//
//                    RayTraceResult result = MouseUtil.rayTrace(4, motion.getYaw(), motion.getPitch(), IMinecraft.mc.player);
//                    if (result instanceof BlockRayTraceResult blockRayTraceResult) {
//                        IMinecraft.mc.playerController.processRightClickBlock(IMinecraft.mc.player, IMinecraft.mc.world, Hand.MAIN_HAND, blockRayTraceResult);
//                        IMinecraft.mc.player.swingArm(Hand.MAIN_HAND);
//                    }
//
//                    IMinecraft.mc.player.inventory.currentItem = last;
//                    IMinecraft.mc.player.fallDistance = 0;
//                }
//            }
//        }
//    }
//
//    private void placeBlocks(EventMotion motion, int block) {
//        int last = IMinecraft.mc.player.inventory.currentItem;
//        IMinecraft.mc.player.inventory.currentItem = block;
//        motion.setPitch(80);
//        motion.setYaw(IMinecraft.mc.player.getHorizontalFacing().getHorizontalAngle());
//        IMinecraft.mc.player.rotationPitchHead = 80;
//        IMinecraft.mc.player.rotationYawHead = IMinecraft.mc.player.getHorizontalFacing().getHorizontalAngle();
//        IMinecraft.mc.player.renderYawOffset = IMinecraft.mc.player.getHorizontalFacing().getHorizontalAngle();
//        BlockRayTraceResult r = (BlockRayTraceResult) MouseUtil.rayTrace(4, motion.getYaw(), motion.getPitch(), IMinecraft.mc.player);
//        IMinecraft.mc.player.swingArm(Hand.MAIN_HAND);
//        IMinecraft.mc.playerController.processRightClickBlock(IMinecraft.mc.player, IMinecraft.mc.world, Hand.MAIN_HAND, r);
//        IMinecraft.mc.player.inventory.currentItem = last;
//        IMinecraft.mc.player.fallDistance = 0;
//    }
//
//    public int getSlotInInventoryOrHotbar(boolean inHotBar) {
//        int firstSlot = inHotBar ? 0 : 9;
//        int lastSlot = inHotBar ? 9 : 36;
//        int finalSlot = -1;
//        for (int i = firstSlot; i < lastSlot; i++) {
//            if (IMinecraft.mc.player.inventory.getStackInSlot(i).getItem() == Items.TORCH) {
//                continue;
//            }
//
//            if (IMinecraft.mc.player.inventory.getStackInSlot(i).getItem() instanceof BlockItem
//                    || IMinecraft.mc.player.inventory.getStackInSlot(i).getItem() == Items.WATER_BUCKET) {
//                finalSlot = i;
//            }
//        }
//
//        return finalSlot;
//    }
//
//    @Override
//    public void onDisable() {
//        Main.getInstance().getFunctionRegistry().getKillAura().nextAttackDelay = 0;
//        Main.getInstance().getFunctionRegistry().getKillAura().lastPosY = 0;
//        Main.getInstance().getFunctionRegistry().getKillAura().canAttackWithSpider = true;
//        super.onDisable();
//    }
//}
