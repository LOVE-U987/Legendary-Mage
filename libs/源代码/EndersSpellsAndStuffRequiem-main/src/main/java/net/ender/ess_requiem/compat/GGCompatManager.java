package net.ender.ess_requiem.compat;

import net.neoforged.fml.ModList;

public class GGCompatManager {

    public static boolean isDTELoaded()
    {
        return ModList.get().isLoaded("discerning_the_eldritch");
    }


}

