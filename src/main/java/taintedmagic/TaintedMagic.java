package taintedmagic;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(TaintedMagic.MODID)
public final class TaintedMagic {
    public static final String MODID = "taintedmagic";

    public TaintedMagic(IEventBus bus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        container.registerConfig(ModConfig.Type.COMMON, StartupConfig.SPEC);
        Content.init(bus);
        bus.addListener(VisualState::register);
        bus.addListener(Sparkles::register);
        bus.addListener(Effects::register);
        bus.addListener((net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent event) -> event.enqueueWork(() -> {
            Foci.init();
            ConvertedResearch.init();
            new EldritchResearchLink().registerResearchItem();
        }));
        Magic.init();
        ConvertedRecipes.init();
        Events.init();
    }

    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(MODID, path); }
}
