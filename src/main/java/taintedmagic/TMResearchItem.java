package taintedmagic;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcStack;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchPage;

public final class TMResearchItem extends ResearchItem {
    public TMResearchItem(String key, AspectList tags, int column, int row, TcStack icon, int complexity, int warp) {
        super(key, "TAINTEDMAGIC", tags, column, row, complexity, icon);
        setWarp(warp);
    }

    public TMResearchItem(String key, AspectList tags, int column, int row, ResourceLocation icon, int complexity, int warp) {
        super(key, "TAINTEDMAGIC", tags, column, row, complexity, icon);
        setWarp(warp);
    }

    @Override
    public ResearchItem setPages(ResearchPage... pages) {
        ResearchPage[] resolved = new ResearchPage[pages.length];
        for (int i = 0; i < pages.length; i++) {
            resolved[i] = pages[i] instanceof ResearchPage.Text text ? ResearchPage.text("tm.text." + key() + "." + text.text()) : pages[i];
        }
        return super.setPages(resolved);
    }

    @Override
    public ResearchItem registerResearchItem() {
        if (pages().stream().anyMatch(ResearchPage.Infusion.class::isInstance)) {
            List<String> hidden = new ArrayList<>(parentsHidden());
            if (!hidden.contains("INFUSION")) hidden.add("INFUSION");
            setParentsHidden(hidden.toArray(String[]::new));
        }
        return super.registerResearchItem();
    }

    @Override
    public String nameKey() { return "tm.name." + key(); }

    @Override
    public String textKey() { return "tm.tag." + key(); }
    @Override public net.minecraft.network.chat.Component description() { return net.minecraft.network.chat.Component.literal(StartupConfig.RESEARCH_TAGS.get() ? "[TM] " : "").append(super.description()); }
}
