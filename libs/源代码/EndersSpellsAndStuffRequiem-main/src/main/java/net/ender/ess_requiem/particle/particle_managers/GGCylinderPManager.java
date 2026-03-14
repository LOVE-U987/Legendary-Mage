package net.ender.ess_requiem.particle.particle_managers;


import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public abstract class GGCylinderPManager {
    private static final Random RANDOM = new Random();

    public static void spawnParticles(Level level, LivingEntity entity, int particleCount, ParticleOptions particleType, GGParticleDirection direction, double radius, double height, double yOffset) {
        if (!level.isClientSide) {
            double baseY = entity.getY() + yOffset;

            for(int i = 0; i < particleCount; ++i) {
                double theta = (Math.PI * 2D) * RANDOM.nextDouble();
                double yPosition = baseY + RANDOM.nextDouble() * height;
                double xOffset = radius * Math.cos(theta);
                double zOffset = radius * Math.sin(theta);
                Vec3 directionVector;
                if (direction == GGParticleDirection.UPWARD) {
                    directionVector = (new Vec3((double)0.0F, (double)1.0F, (double)0.0F)).normalize();
                } else if (direction == GGParticleDirection.DOWNWARD) {
                    directionVector = (new Vec3((double)0.0F, (double)-1.0F, (double)0.0F)).normalize();
                } else if (direction == GGParticleDirection.INWARD) {
                    directionVector = (new Vec3(-xOffset, (double)0.0F, -zOffset)).normalize();
                } else {
                    directionVector = (new Vec3(xOffset, (double)0.0F, zOffset)).normalize();
                }

                MagicManager.spawnParticles(level, particleType, entity.getX() + xOffset, yPosition, entity.getZ() + zOffset, 0, directionVector.x, directionVector.y, directionVector.z, 0.1, true);
            }
        }

    }
}
