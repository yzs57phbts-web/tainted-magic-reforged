package taintedmagic;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class StartupConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue RESEARCH_TAGS, CUSTOM_BACKGROUND;
    public static final ModConfigSpec.IntValue SANITY, ANTIBODY, CORROSIVE, PERSISTENT, DIFFUSION;
    static {
        var builder = new ModConfigSpec.Builder();
        builder.push("research");
        RESEARCH_TAGS = builder.define("researchTags", true);
        CUSTOM_BACKGROUND = builder.define("customResearchBackground", false);
        builder.pop().push("focus_upgrade_ids");
        SANITY = builder.defineInRange("sanity", 64, 20, Short.MAX_VALUE);
        ANTIBODY = builder.defineInRange("antibody", 65, 20, Short.MAX_VALUE);
        CORROSIVE = builder.defineInRange("corrosive", 66, 20, Short.MAX_VALUE);
        PERSISTENT = builder.defineInRange("persistent", 67, 20, Short.MAX_VALUE);
        DIFFUSION = builder.defineInRange("diffusion", 68, 20, Short.MAX_VALUE);
        builder.pop();
        SPEC = builder.build();
    }
}
