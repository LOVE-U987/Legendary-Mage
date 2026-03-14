package net.ender.ess_requiem.entity.mobs.nightmare;

import io.redspace.ironsspellbooks.entity.mobs.wizards.GenericAnimatedWarlockAttackGoal;
import net.ender.ess_requiem.registries.GGSoundRegistry;

public class NightmareAnimatedWarlockAttackGoal  extends GenericAnimatedWarlockAttackGoal<NightmareEntity> {
    final NightmareEntity nightmare;

    public NightmareAnimatedWarlockAttackGoal(NightmareEntity entity, double pSpeedModifier, int minAttackInterval, int maxAttackInterval) {
        super(entity, pSpeedModifier, minAttackInterval, maxAttackInterval);
        this.nightmare = entity;
        this.wantsToMelee = true;
    }

    @Override
    public void playSwingSound() {
        nightmare.playSound(GGSoundRegistry.NIGHTMARE_ATTACK.get(), 10.0F, 2);
    }

}
