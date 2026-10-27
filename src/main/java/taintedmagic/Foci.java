package taintedmagic;

import thaumcraft.aspect.*;
import thaumcraft.wand.focus.FocusUpgradeType;

public final class Foci {
    public static FocusUpgradeType SANITY, ANTIBODY, CORROSIVE, PERSISTENT, DIFFUSION;
    public static void init() {
        SANITY = register(StartupConfig.SANITY.get(), "sanity", new AspectList().add(Aspect.MIND, 1).add(Aspect.HEAL, 1));
        ANTIBODY = register(StartupConfig.ANTIBODY.get(), "antibody", new AspectList().add(Aspect.TAINT, 1).add(Aspect.HEAL, 1));
        CORROSIVE = register(StartupConfig.CORROSIVE.get(), "corrosive", new AspectList().add(Aspect.TAINT, 1).add(Aspect.POISON, 1));
        PERSISTENT = register(StartupConfig.PERSISTENT.get(), "persistent", new AspectList().add(Aspect.ARMOR, 1).add(Aspect.MOTION, 1).add(Aspect.ENERGY, 1));
        DIFFUSION = register(StartupConfig.DIFFUSION.get(), "diffusion", new AspectList().add(Aspect.DARKNESS, 1).add(Aspect.ELDRITCH, 2).add(Aspect.AURA, 4));
    }
    private static FocusUpgradeType register(int id, String name, AspectList cost) {
        if (FocusUpgradeType.byId(id) != null) throw new IllegalStateException("Tainted Magic focus upgrade ID conflict: " + id);
        return new FocusUpgradeType(id, name, TaintedMagic.id("textures/foci/icon" + name + ".png"), cost);
    }
}
