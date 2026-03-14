package net.ender.ess_requiem.particle.particle_managers;

import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public abstract class GGSpherePManager {
    private static final Random RANDOM = new Random();

    public static void spawnParticles(Level level, LivingEntity entity, int particleCount, ParticleOptions particleType, GGParticleDirection direction, double parameter) {
        if (!level.isClientSide) {
            double centerY = entity.getY() + (double)entity.getBbHeight() * (double)0.5F;

            for(int i = 0; i < particleCount; ++i) {
                double theta = (Math.PI * 2D) * RANDOM.nextDouble();
                double phi = Math.acos((double)2.0F * RANDOM.nextDouble() - (double)1.0F);
                double xOffset = Math.sin(phi) * Math.cos(theta);
                double yOffset = Math.sin(phi) * Math.sin(theta);
                double zOffset = Math.cos(phi);
                if (direction == GGParticleDirection.INWARD) {
                    xOffset *= parameter;
                    yOffset *= parameter;
                    zOffset *= parameter;
                    Vec3 directionVector = (new Vec3(entity.getX() - (entity.getX() + xOffset), centerY - (centerY + yOffset), entity.getZ() - (entity.getZ() + zOffset))).normalize();
                    MagicManager.spawnParticles(level, particleType, entity.getX() + xOffset, centerY + yOffset, entity.getZ() + zOffset, 0, directionVector.x, directionVector.y, directionVector.z, 0.1, true);
                } else {
                    Vec3 directionVector = (new Vec3(xOffset, yOffset, zOffset)).normalize().scale(parameter);
                    MagicManager.spawnParticles(level, particleType, entity.getX(), centerY, entity.getZ(), 0, directionVector.x, directionVector.y, directionVector.z, 0.1, true);
                }
            }
        }

    }
}
