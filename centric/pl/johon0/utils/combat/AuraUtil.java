package centric.pl.johon0.utils.combat;


import centric.pl.johon0.utils.client.IMinecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;

public class AuraUtil implements IMinecraft {
    public static boolean isNoVisible;

    public AuraUtil() {
    }

    private static Vector3d calculateVector(LivingEntity target, double distance) {
        double yOffset = MathHelper.clamp(mc.player.getPosYEye() - target.getPosYEye(), 0.2, (double)target.getEyeHeight());
        return target.getPositionVec().add(0.0, yOffset, 0.0);
    }

    public static Vector3d getBestVec3d(Vector3d pos, AxisAlignedBB axisAlignedBB) {
        double lastDistance = Double.MAX_VALUE;
        Vector3d bestVec = null;
        double xWidth = axisAlignedBB.maxX - axisAlignedBB.minX;
        double zWidth = axisAlignedBB.maxZ - axisAlignedBB.minZ;
        double height = axisAlignedBB.maxY - axisAlignedBB.minY;

        for(float x = 0.0F; x < 1.0F; x += 0.1F) {
            for(float y = 0.0F; y < 1.0F; y += 0.1F) {
                for(float z = 0.0F; z < 1.0F; z += 0.1F) {
                    Vector3d hitVec = new Vector3d(axisAlignedBB.minX + xWidth * (double)x, axisAlignedBB.minY + height * (double)y, axisAlignedBB.minZ + zWidth * (double)z);
                    double distance = pos.distanceTo(hitVec);
                    if (isHitBoxNotVisible(hitVec) && distance < lastDistance) {
                        bestVec = hitVec;
                        lastDistance = distance;
                    }
                }
            }
        }

        return bestVec;
    }

    public static boolean isHitBoxNotVisible(Vector3d vec3d) {
        RayTraceContext rayTraceContext = new RayTraceContext(mc.player.getEyePosition(1.0F), vec3d, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, mc.player);
        BlockRayTraceResult blockHitResult = mc.world.rayTraceBlocks(rayTraceContext);
        return blockHitResult.getType() == RayTraceResult.Type.MISS;
    }

    public static Vector3d getVector(LivingEntity target) {
        double wHalf = (double)(target.getWidth() / 2.0F);
        double yExpand = MathHelper.clamp(target.getPosYEye() - target.getPosY(), 0.0, (double)target.getHeight());
        double xExpand = MathHelper.clamp(mc.player.getPosX() - target.getPosX(), -wHalf, wHalf);
        double zExpand = MathHelper.clamp(mc.player.getPosZ() - target.getPosZ(), -wHalf, wHalf);
        return new Vector3d(target.getPosX() - mc.player.getPosX() + xExpand, target.getPosY() - mc.player.getPosYEye() + yExpand, target.getPosZ() - mc.player.getPosZ() + zExpand);
    }
}
