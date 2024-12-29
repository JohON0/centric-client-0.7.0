package centric.pl.johon0.utils;

import centric.pl.johon0.utils.client.IMinecraft;
import lombok.experimental.UtilityClass;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;

@UtilityClass
public class EntityUtils implements IMinecraft {

    public Vector3d getPrevPositionVec(
            final Entity entity
    ) {
        return new Vector3d(
                entity.prevPosX,
                entity.prevPosY,
                entity.prevPosZ
        );
    }

    public Vector3d getInterpolatedPositionVec(final Entity entity) {
        final Vector3d prev = getPrevPositionVec(entity);

        return prev.add(entity.getPositionVec()
                .subtract(prev)
                .scale(mc.getRenderPartialTicks())
        );
    }
}