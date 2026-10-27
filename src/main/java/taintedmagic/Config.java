package taintedmagic;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue MACE_DAMAGE;
    public static final ModConfigSpec.IntValue WARP_REFRESH;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("wands_and_foci");
        MACE_DAMAGE = builder.defineInRange("mageMaceDamage", 8.0, 1.0, 100.0);
        WARP_REFRESH = builder.defineInRange("warpwoodRefreshBase", 10000, 100, 100000);
        builder.pop();
        SPEC = builder.build();
    }
}
