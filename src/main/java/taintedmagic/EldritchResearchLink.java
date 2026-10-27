package taintedmagic;

import java.util.List;
import thaumcraft.aspect.AspectList;
import thaumcraft.research.*;

public final class EldritchResearchLink extends ResearchItem {
    public EldritchResearchLink() {
        super("TMELDRITCHMAJOR", "TAINTEDMAGIC", new AspectList(), 7, -7, 1, net.minecraft.resources.ResourceLocation.parse("thaumcraft:textures/misc/r_eldritchmajor.png"));
        setStub(); setHidden(); setSpecial(); setConcealed(); setRound();
    }
    @Override public String nameKey() { return "tc.research_name.ELDRITCHMAJOR"; }
    @Override public String textKey() { return "tc.research_text.ELDRITCHMAJOR"; }
    @Override public List<ResearchPage> pages() {
        ResearchItem original = ResearchCategories.getResearch("ELDRITCHMAJOR");
        return original == null ? List.of() : original.pages();
    }
}
