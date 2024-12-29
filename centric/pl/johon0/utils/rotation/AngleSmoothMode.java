package centric.pl.johon0.utils.rotation;

import centric.pl.johon0.utils.combat.Rotation;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;

public abstract class AngleSmoothMode{

    public abstract Rotation limitAngleChange(Rotation currentRotation, Rotation targetRotation,
                                              @Nullable Vector3d vec3d, @Nullable Entity entity);
    public abstract Rotation limitAngleChange(Rotation currentRotation, Rotation targetRotation);

    public abstract int howLongToReach(Rotation currentRotation, Rotation targetRotation);
}
