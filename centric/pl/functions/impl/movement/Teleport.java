package centric.pl.functions.impl.movement;

import centric.pl.events.impl.EventStartRiding;
import centric.pl.events.impl.EventUpdate;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.math.StopWatch;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

import java.util.*;

@FunctionRegister(name = "Teleport", type = Category.Movement, beta = false)
public class Teleport extends Function {
    private final StopWatch timer = new StopWatch();
    private final Random random = new Random();
    private final Set<BlockPos> visitedPositions = new HashSet<>();
    private final int range = 4;

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (timer.isReached(10))
            processEventUpdate();

    }

    @Subscribe
    public void onRiding(EventStartRiding e) {
        processEventStartRiding(e);
    }


    private void processEventUpdate() {
        Block randomBlock = findRandomNearbyBlock();
        if (randomBlock != null) {
            sitOnSlab(randomBlock);
        }
        timer.reset();
    }

    private void processEventStartRiding(EventStartRiding event) {
        Entity entity = event.e;
        visitedPositions.add(entity.getPosition());
        System.out.println("1");
        new Thread(() -> {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            IMinecraft.mc.player.stopRiding();
        }).start();
    }

    private Block findRandomNearbyBlock() {
        List<BlockPos> potentialPositions = getPotentialPositions();
        if (!potentialPositions.isEmpty()) {
            BlockPos selectedPos = potentialPositions.get(random.nextInt(potentialPositions.size()));
            return IMinecraft.mc.world.getBlockState(selectedPos).getBlock();
        }
        return null;
    }

    private List<BlockPos> getPotentialPositions() {
        Vector3d playerPos = IMinecraft.mc.player.getPositionVec();
        List<BlockPos> potentialPositions = new ArrayList<>();

        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = new BlockPos(playerPos.x + x, playerPos.y + y, playerPos.z + z);
                    if (isPositionEligible(pos)) {
                        potentialPositions.add(pos);
                    }
                }
            }
        }
        return potentialPositions;
    }

    public static double getDistanceOfEntityToBlock(final Entity entity, final BlockPos blockPos) {
        return IMinecraft.mc.player.getDistance(blockPos);
    }

    private boolean isPositionEligible(BlockPos pos) {
        return !visitedPositions.contains(pos)
                && IMinecraft.mc.world.getBlockState(pos).getBlock() instanceof SlabBlock || IMinecraft.mc.world.getBlockState(pos).getBlock() instanceof StairsBlock
                && IMinecraft.mc.world.isAirBlock(pos.up()) && IMinecraft.mc.world.isAirBlock(pos.up(2));
    }

    private void sitOnSlab(Block block) {
        BlockPos pos = findSlabPosition(block);
        if (pos != null && !visitedPositions.contains(pos)) {
            Vector3d hitVec = new Vector3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
            BlockRayTraceResult blockRayTraceResult = new BlockRayTraceResult(hitVec, Direction.UP, pos, false);
            IMinecraft.mc.playerController.processRightClickBlock(IMinecraft.mc.player, IMinecraft.mc.world, Hand.MAIN_HAND, blockRayTraceResult);
        }
    }

    private BlockPos findSlabPosition(Block slab) {
        Vector3d playerPos = IMinecraft.mc.player.getPositionVec();
        BlockPos posr = null;
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = new BlockPos(playerPos.x + x, playerPos.y + y, playerPos.z + z);
                    Block block = IMinecraft.mc.world.getBlockState(pos).getBlock();
                    if (posr == null && block == slab) {
                        posr = pos;
                    }
                    if (block == slab && IMinecraft.mc.player.getDistance(posr) < IMinecraft.mc.player.getDistance(pos)) {
                        posr = pos;
                    }
                }
            }
        }

        return posr;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        visitedPositions.clear();
    }
}