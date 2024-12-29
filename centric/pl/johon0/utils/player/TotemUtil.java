package centric.pl.johon0.utils.player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import centric.pl.johon0.utils.client.IMinecraft;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class TotemUtil {
    public TotemUtil() {
    }

    public static BlockPos getBlock(float distance, Block block) {
        return (BlockPos)getSphere(getPlayerPosLocal(), distance, 6, false, true, 0).stream().filter((position) -> {
            return IMinecraft.mc.world.getBlockState(position).getBlock() == block;
        }).min(Comparator.comparing((blockPos) -> {
            return getDistanceOfEntityToBlock(IMinecraft.mc.player, blockPos);
        })).orElse((BlockPos) null);
    }

    public static BlockPos getBlock(float distance) {
        return (BlockPos)getSphere(getPlayerPosLocal(), distance, 6, false, true, 0).stream().filter((position) -> {
            return IMinecraft.mc.world.getBlockState(position).getBlock() != Blocks.AIR;
        }).min(Comparator.comparing((blockPos) -> {
            return getDistanceOfEntityToBlock(IMinecraft.mc.player, blockPos);
        })).orElse((BlockPos) null);
    }

    public static BlockPos getBlockFlat(int distance) {
        BlockPos vec = getPlayerPosLocal().add(0, -1, 0);

        for(int x = vec.getX() - distance; x <= vec.getX() + distance; ++x) {
            for(int z = vec.getX() - distance; z <= vec.getZ() + distance; ++z) {
                if (IMinecraft.mc.world.getBlockState(new BlockPos(x, vec.getY(), z)).getBlock() != Blocks.AIR) {
                    return new BlockPos(x, vec.getY(), z);
                }
            }
        }

        return vec;
    }

    public static List<BlockPos> getSphere(BlockPos blockPos, float n, int n2, boolean b, boolean b2, int n3) {
        ArrayList<BlockPos> list = new ArrayList();
        int x = blockPos.getX();
        int y = blockPos.getY();
        int z = blockPos.getZ();

        for(int n4 = x - (int)n; (float)n4 <= (float)x + n; ++n4) {
            for(int n5 = z - (int)n; (float)n5 <= (float)z + n; ++n5) {
                for(int n6 = b2 ? y - (int)n : y; (float)n6 < (b2 ? (float)y + n : (float)(y + n2)); ++n6) {
                    double n7 = (double)((x - n4) * (x - n4) + (z - n5) * (z - n5) + (b2 ? (y - n6) * (y - n6) : 0));
                    if (n7 < (double)(n * n) && (!b || n7 >= (double)((n - 1.0F) * (n - 1.0F)))) {
                        list.add(new BlockPos(n4, n6 + n3, n5));
                    }
                }
            }
        }

        return list;
    }

    public static BlockPos getPlayerPosLocal() {
        return IMinecraft.mc.player == null ? BlockPos.ZERO : new BlockPos(Math.floor(IMinecraft.mc.player.getPosX()), Math.floor(IMinecraft.mc.player.getPosY()), Math.floor(IMinecraft.mc.player.getPosZ()));
    }

    public static double getDistanceOfEntityToBlock(Entity entity, BlockPos blockPos) {
        return getDistance(entity.getPosX(), entity.getPosY(), entity.getPosZ(), (double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());
    }

    public static double getDistance(double n, double n2, double n3, double n4, double n5, double n6) {
        double n7 = n - n4;
        double n8 = n2 - n5;
        double n9 = n3 - n6;
        return (double)MathHelper.sqrt(n7 * n7 + n8 * n8 + n9 * n9);
    }
}
