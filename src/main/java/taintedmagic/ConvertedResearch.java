package taintedmagic;

import net.minecraft.resources.ResourceLocation;
import thaumcraft.aspect.*;
import thaumcraft.crafting.*;
import thaumcraft.research.*;

public final class ConvertedResearch {
    public static void init() {
        ResearchCategories.registerCategory("TAINTEDMAGIC", TaintedMagic.id("textures/misc/tab_tm.png"), StartupConfig.CUSTOM_BACKGROUND.get() ? TaintedMagic.id("textures/gui/gui_tm_researchback.png") : ResourceLocation.parse("thaumcraft:textures/gui/gui_researchback.png"));
        new TMResearchItem(
                        "TAINTEDMAGIC", new AspectList(), 2, -1, TcStack.of("thaumcraft:jar_label", 1), 0, 0)
                .setPages(ResearchPage.text("1"), ResearchPage.text("2"))
                .setRound()
                .setAutoUnlock()
                .registerResearchItem();

        new TMResearchItem(
                        "SHADOWMETAL",
                        new AspectList()
                                .add(Aspect.METAL, 1)
                                .add(Aspect.DARKNESS, 1)
                                .add(Aspect.MAGIC, 1),
                        0,
                        1,
                        TcStack.of("taintedmagic:shadowmetal_ingot", 1),
                        1,
                        1)
                .setPages(
                        ResearchPage.text("1"),
                        ResearchPage.crucible("TM:ItemMaterial:0"),
                        ResearchPage.crafting("TM:ItemShadowmetalPick"),
                        ResearchPage.crafting("TM:ItemShadowmetalSpade"),
                        ResearchPage.crafting("TM:ItemShadowmetalAxe"),
                        ResearchPage.crafting("TM:ItemShadowmetalHoe"),
                        ResearchPage.crafting("TM:ItemShadowmetalSword"))
                .setParents("TAINTEDMAGIC")
                .setConcealed()
                .registerResearchItem();

        new TMResearchItem(
                        "CAP_shadowmetal",
                        new AspectList()
                                .add(Aspect.METAL, 4)
                                .add(Aspect.DARKNESS, 4)
                                .add(Aspect.ELDRITCH, 4)
                                .add(Aspect.MAGIC, 4),
                        -3,
                        2,
                        TcStack.of("taintedmagic:shadowmetal_wand_cap", 1),
                        2,
                        4)
                .setPages(ResearchPage.text("1"), ResearchPage.infusion("TM:ItemWandCap:0"))
                .setParents("SHADOWMETAL", "CAP_void", "PRIMPEARL")
                .setConcealed()
                .registerResearchItem();

        new TMResearchItem(
                        "SHADOWCLOTH",
                        new AspectList().add(Aspect.CLOTH, 1).add(Aspect.DARKNESS, 1),
                        -2,
                        4,
                        TcStack.of("taintedmagic:shadow_cloth", 1),
                        0,
                        0)
                .setPages(ResearchPage.text("1"), ResearchPage.arcane("TM:ItemMaterial:1"))
                .setConcealed()
                .setSecondary()
                .setParents("ENCHFABRIC", "SHADOWMETAL")
                .registerResearchItem();

        new TMResearchItem(
                        "ROD_warpwood",
                        new AspectList()
                                .add(Aspect.TREE, 4)
                                .add(Aspect.DARKNESS, 4)
                                .add(Aspect.ELDRITCH, 4),
                        8,
                        0,
                        TcStack.of("taintedmagic:warpwood_rod", 1),
                        3,
                        5)
                .setPages(ResearchPage.text("1"), ResearchPage.infusion("TM:ItemWandRod:0"))
                .setParents("WARPTREE", "VOIDMETAL", "PRIMPEARL", "ROD_primal_staff")
                .setParentsHidden("SHADOWMETAL")
                .setConcealed()
                .registerResearchItem();

        new TMResearchItem(
                        "ROD_warpwood_staff",
                        new AspectList()
                                .add(Aspect.TREE, 4)
                                .add(Aspect.DARKNESS, 4)
                                .add(Aspect.ELDRITCH, 4),
                        10,
                        -1,
                        TcStack.of("taintedmagic:warpwood_staff_core", 1),
                        2,
                        0)
                .setPages(ResearchPage.text("1"), ResearchPage.arcane("TM:ItemWandRod:1"))
                .setConcealed()
                .setParents("ROD_warpwood")
                .setParentsHidden("CREATIONSHARD")
                .setSecondary()
                .registerResearchItem();

        new TMResearchItem(
                        "UNBALANCEDSHARDS",
                        new AspectList().add(Aspect.CRYSTAL, 4).add(Aspect.DARKNESS, 4),
                        4,
                        -1,
                        TcStack.of("taintedmagic:tainted_shard", 1),
                        0,
                        1)
                .setPages(ResearchPage.text("1"), ResearchPage.crucible("TM:ItemMaterial:4"), ResearchPage.crucible("TM:ItemMaterial:3"))
                .setParents("TAINTEDMAGIC")
                .setConcealed()
                .setSecondary()
                .registerResearchItem();

        new TMResearchItem(
                        "WARPTREE",
                        new AspectList()
                                .add(Aspect.MAGIC, 4)
                                .add(Aspect.DARKNESS, 4)
                                .add(Aspect.TREE, 4),
                        6,
                        -2,
                        TcStack.of("taintedmagic:warp_fertilizer", 1),
                        3,
                        2)
                .setPages(ResearchPage.text("1"), ResearchPage.arcane("TM:ItemWarpFertilizer"))
                .setParents("UNBALANCEDSHARDS")
                .setConcealed()
                .registerResearchItem();

        new TMResearchItem(
                        "CRIMSONROBES",
                        new AspectList()
                                .add(Aspect.CLOTH, 4)
                                .add(Aspect.EXCHANGE, 4)
                                .add(Aspect.ARMOR, 4),
                        -2,
                        -1,
                        TcStack.of("taintedmagic:crimson_cloth", 1),
                        2,
                        0)
                .setPages(
                        ResearchPage.text("1"),
                        ResearchPage.arcane("TM:ItemMaterial:2"),
                        ResearchPage.arcane("TM:ItemHelmetCultistRobe"),
                        ResearchPage.arcane("TM:ItemChestCultistRobe"),
                        ResearchPage.arcane("TM:ItemLegsCultistRobe"),
                        ResearchPage.arcane("TM:ItemBootsCultist"))
                .setParents("HOLLOWDAGGER")
                .setConcealed()
                .registerResearchItem();

        new TMResearchItem(
                        "VOIDFORTRESS",
                        new AspectList()
                                .add(Aspect.ARMOR, 5)
                                .add(Aspect.ELDRITCH, 3)
                                .add(Aspect.DARKNESS, 3)
                                .add(Aspect.VOID, 5),
                        8,
                        -9,
                        TcStack.of("taintedmagic:void_fortress_helmet", 1),
                        2,
                        2)
                .setPages(
                        ResearchPage.text("1"),
                        ResearchPage.infusion("TM:ItemVoidFortressHelmet"),
                        ResearchPage.infusion("TM:ItemVoidFortressChestplate"),
                        ResearchPage.infusion("TM:ItemVoidFortressLeggings"))
                .setParentsHidden("ARMORVOIDFORTRESS", "ARMORFORTRESS")
                .setParents("TMELDRITCHMAJOR")
                .setConcealed()
                .registerResearchItem();

        new TMResearchItem(
                        "WARPEDGOGGLES",
                        new AspectList()
                                .add(Aspect.ARMOR, 4)
                                .add(Aspect.ELDRITCH, 4)
                                .add(Aspect.DARKNESS, 4)
                                .add(Aspect.ARMOR, 4),
                        2,
                        4,
                        TcStack.of("taintedmagic:warped_goggles", 1),
                        1,
                        0)
                .setPages(ResearchPage.text("1"), ResearchPage.infusion("TM:ItemWarpedGoggles"))
                .setParentsHidden("TAINTEDMAGIC", "GOGGLES")
                .setConcealed()
                .setSecondary()
                .setParents("SHADOWMETAL")
                .registerResearchItem();

        new TMResearchItem(
                        "KNIGHTROBES",
                        new AspectList()
                                .add(Aspect.CLOTH, 4)
                                .add(Aspect.DARKNESS, 4)
                                .add(Aspect.ARMOR, 4),
                        -4,
                        -2,
                        TcStack.of("taintedmagic:crimson_alloy_plate", 1),
                        2,
                        0)
                .setPages(
                        ResearchPage.text("1"),
                        ResearchPage.infusion("TM:ItemMaterial:7"),
                        ResearchPage.arcane("TM:ItemHelmetCultistPlate"),
                        ResearchPage.arcane("TM:ItemChestCultistPlate"),
                        ResearchPage.arcane("TM:ItemLegsCultistPlate"))
                .setParentsHidden("ELDRITCHMINOR")
                .setParents("CRIMSONROBES")
                .setConcealed()
                .registerResearchItem();

        new TMResearchItem(
                        "PRAETORARMOR",
                        new AspectList()
                                .add(Aspect.CLOTH, 4)
                                .add(Aspect.DARKNESS, 4)
                                .add(Aspect.ARMOR, 4)
                                .add(Aspect.ELDRITCH, 4),
                        -6,
                        0,
                        TcStack.of("thaumcraft:crimson_praetor_helm"),
                        2,
                        0)
                .setPages(
                        ResearchPage.text("1"),
                        ResearchPage.arcane("TM:ItemHelmetCultistLeaderPlate"),
                        ResearchPage.arcane("TM:ItemChestCultistLeaderPlate"),
                        ResearchPage.arcane("TM:ItemLegsCultistLeaderPlate"))
                .setParents("KNIGHTROBES")
                .setParentsHidden("CRIMSONROBES", "TMELDRITCHMAJOR", "VOIDMETAL")
                .setConcealed()
                .registerResearchItem();

        new TMResearchItem(
                        "VOIDBLOOD",
                        new AspectList()
                                .add(Aspect.VOID, 14)
                                .add(Aspect.DARKNESS, 8)
                                .add(Aspect.ARMOR, 18)
                                .add(Aspect.AURA, 4),
                        -2,
                        -5,
                        TcStack.of("taintedmagic:void_blood", 1),
                        0,
                        3)
                .setPages(ResearchPage.text("1"), ResearchPage.arcane("TM:ItemVoidBlood"))
                .setSecondary()
                .setParents("HOLLOWDAGGER")
                .setParentsHidden("ELDRITCHMAJOR", "BREAKPEARL")
                .setConcealed()
                .registerResearchItem();

        new TMResearchItem(
                        "VOIDWALKERBOOTS",
                        new AspectList()
                                .add(Aspect.MAGIC, 4)
                                .add(Aspect.DARKNESS, 8)
                                .add(Aspect.ARMOR, 8)
                                .add(Aspect.ELDRITCH, 8),
                        4,
                        -8,
                        TcStack.of("taintedmagic:voidwalker_boots", 1),
                        3,
                        4)
                .setPages(ResearchPage.text("1"), ResearchPage.infusion("TM:ItemVoidwalkerBoots"))
                .setParentsHidden("PRIMPEARL", "BOOTSTRAVELLER", "SHADOWCLOTH", "ARMORVOIDFORTRESS")
                .setConcealed()
                .setParents("TMELDRITCHMAJOR")
                .registerResearchItem();

        new TMResearchItem(
                        "BREAKPEARL",
                        Magic.primals(4).add(Aspect.ENTROPY, 12),
                        8,
                        -6,
                        TcStack.of("taintedmagic:primordial_nodule", 1),
                        0,
                        0)
                .setPages(ResearchPage.text("1"), ResearchPage.arcane("TM:ItemMaterial:9"), ResearchPage.arcane("TM:ItemMaterial:10"))
                .setConcealed()
                .setParents("PRIMPEARL")
                .setSecondary()
                .registerResearchItem();

        new TMResearchItem(
                        "CREATIONSHARD",
                        Magic.primals(16),
                        9,
                        -7,
                        TcStack.of("taintedmagic:creation_shard", 1),
                        3,
                        9)
                .setPages(ResearchPage.text("1"), ResearchPage.infusion("TM:ItemMaterial:5"), ResearchPage.arcane("TM:ItemMaterial:11"))
                .setConcealed()
                .setParents("TMELDRITCHMAJOR")
                .setParentsHidden("PRIMPEARL")
                .setSpecial()
                .registerResearchItem();

        new TMResearchItem(
                        "CREATION",
                        new AspectList(),
                        11,
                        -5,
                        ResourceLocation.parse("taintedmagic:textures/misc/r_creation.png"),
                        0,
                        0)
                .setPages(
                        ResearchPage.text("1"),
                        ResearchPage.text("2"),
                        ResearchPage.text("3"),
                        ResearchPage.text("4"),
                        ResearchPage.text("5"),
                        ResearchPage.text("6"))
                .setParents("CREATIONSHARD")
                .setConcealed()
                .setRound()
                .setSpecial()
                .setHidden()
                .registerResearchItem();

        new TMResearchItem(
                        "SKYSALT",
                        new AspectList().add(Magic.primals(4)).add(Aspect.WEATHER, 4),
                        12,
                        -7,
                        TcStack.of("taintedmagic:salis_tempestas", 1),
                        0,
                        0)
                .setPages(ResearchPage.text("1"), ResearchPage.infusion("TM:ItemSalis:0"))
                .setConcealed()
                .setParents("CREATION")
                .setSecondary()
                .registerResearchItem();

        new TMResearchItem(
                        "TIMESALT",
                        new AspectList().add(Magic.primals(4)).add(Aspect.EXCHANGE, 4),
                        13,
                        -6,
                        TcStack.of("taintedmagic:salis_aevum", 1),
                        0,
                        0)
                .setPages(ResearchPage.text("1"), ResearchPage.infusion("TM:ItemSalis:1"))
                .setConcealed()
                .setParents("CREATION")
                .setSecondary()
                .registerResearchItem();

        new TMResearchItem(
                        "THAUMICDISASSEMBLER",
                        new AspectList()
                                .add(Aspect.METAL, 4)
                                .add(Aspect.WEAPON, 8)
                                .add(Aspect.TOOL, 4),
                        6,
                        -9,
                        TcStack.of("taintedmagic:thaumic_disassembler", 1),
                        3,
                        0)
                .setPages(ResearchPage.text("1"), ResearchPage.infusion("TM:ItemThaumicDisassembler"), ResearchPage.arcane("TM:ItemMaterial:6"))
                .setParentsHidden("THAUMIUM", "VOIDMETAL", "PRIMPEARL")
                .setConcealed()
                .setParents("TMELDRITCHMAJOR")
                .registerResearchItem();

        new TMResearchItem(
                        "VOIDSASH",
                        new AspectList()
                                .add(Aspect.VOID, 4)
                                .add(Aspect.METAL, 8)
                                .add(Aspect.ARMOR, 4),
                        3,
                        -9,
                        TcStack.of("taintedmagic:voidwalker_sash", 1),
                        0,
                        3)
                .setPages(ResearchPage.text("1"), ResearchPage.infusion("TM:ItemVoidwalkerSash"))
                .setParentsHidden("PRIMPEARL")
                .setParents("VOIDWALKERBOOTS")
                .setConcealed()
                .setSecondary()
                .registerResearchItem();

        new TMResearchItem(
                        "MAGICFUNGUAR",
                        new AspectList()
                                .add(Aspect.MAGIC, 2)
                                .add(Aspect.HUNGER, 4)
                                .add(Aspect.PLANT, 2),
                        2,
                        -4,
                        TcStack.of("taintedmagic:magic_funguar", 1),
                        0,
                        0)
                .setPages(ResearchPage.text("1"), ResearchPage.arcane("TM:ItemMagicFunguar"))
                .setSecondary()
                .setParents("VISHROOMCRAFT")
                .setConcealed()
                .registerResearchItem();

        new TMResearchItem(
                        "VISHROOMCRAFT",
                        new AspectList()
                                .add(Aspect.MAGIC, 4)
                                .add(Aspect.CRAFT, 2)
                                .add(Aspect.PLANT, 3),
                        3,
                        -3,
                        TcStack.of("thaumcraft:vishroom", 1),
                        0,
                        0)
                .setPages(ResearchPage.text("1"), ResearchPage.crucible("TM:Vishroom_red"), ResearchPage.crucible("TM:Vishroom_brown"))
                .setSecondary()
                .setConcealed()
                .setHidden()
                .setItemTriggers("thaumcraft:vishroom")
                .registerResearchItem();

        new TMResearchItem(
                        "HOLLOWDAGGER",
                        new AspectList()
                                .add(Aspect.WEAPON, 4)
                                .add(Aspect.FIRE, 4)
                                .add(Aspect.HEAL, 4),
                        0,
                        -3,
                        TcStack.of("taintedmagic:hollow_dagger", 1),
                        2,
                        2)
                .setPages(ResearchPage.text("1"), ResearchPage.arcane("TM:ItemHollowDagger"))
                .setParentsHidden("ENCHFABRIC", "ESSENTIACRYSTAL", "ELDRITCHMINOR")
                .setParents("TAINTEDMAGIC")
                .registerResearchItem();

        new TMResearchItem(
                        "CRIMSONBLADE",
                        new AspectList()
                                .add(Aspect.ENTROPY, 8)
                                .add(Aspect.ELDRITCH, 2)
                                .add(Aspect.WEAPON, 16)
                                .add(Aspect.VOID, 6),
                        -6,
                        2,
                        TcStack.of("thaumcraft:crimson_blade", 1),
                        0,
                        5)
                .setPages(ResearchPage.text("1"), ResearchPage.infusion("TM:ItemSwordCrimson"))
                .setParents("PRAETORARMOR")
                .setConcealed()
                .setSecondary()
                .registerResearchItem();

        new TMResearchItem(
                        "SHADOWFORTRESS",
                        new AspectList()
                                .add(Aspect.METAL, 4)
                                .add(Aspect.DARKNESS, 8)
                                .add(Aspect.ARMOR, 8)
                                .add(Aspect.VOID, 2),
                        0,
                        3,
                        TcStack.of("taintedmagic:shadow_fortress_helmet", 1),
                        3,
                        3)
                .setPages(
                        ResearchPage.text("1"),
                        ResearchPage.infusion("TM:ItemShadowFortressHelmet"),
                        ResearchPage.infusion("TM:ItemShadowFortressChestplate"),
                        ResearchPage.infusion("TM:ItemShadowFortressLeggings"))
                .setParents("SHADOWMETAL")
                .setConcealed()
                .setParentsHidden("VOIDFORTRESS", "TMELDRITCHMAJOR", "UNBALANCEDSHARDS")
                .registerResearchItem();

        new TMResearchItem(
                        "CAP_cloth",
                        new AspectList().add(Aspect.MAGIC, 4).add(Aspect.CLOTH, 4),
                        3,
                        1,
                        TcStack.of("taintedmagic:cloth_wand_cap", 1),
                        0,
                        0)
                .setPages(ResearchPage.text("1"), ResearchPage.arcane("TM:ItemWandCap:1"))
                .setParentsHidden("CAP_gold", "ENCHFABRIC")
                .setConcealed()
                .setSecondary()
                .registerResearchItem();

        new TMResearchItem(
                        "CAP_crimsoncloth",
                        new AspectList()
                                .add(Aspect.MAGIC, 8)
                                .add(Aspect.CLOTH, 4)
                                .add(Aspect.HEAL, 8),
                        -1,
                        -1,
                        TcStack.of("taintedmagic:crimson_cloth_wand_cap", 1),
                        0,
                        0)
                .setPages(ResearchPage.text("1"), ResearchPage.arcane("TM:ItemWandCap:2"))
                .setParentsHidden("CAP_cloth")
                .setSecondary()
                .setConcealed()
                .setParents("CRIMSONROBES")
                .registerResearchItem();

        new TMResearchItem(
                        "CAP_shadowcloth",
                        new AspectList()
                                .add(Aspect.MAGIC, 16)
                                .add(Aspect.CLOTH, 8)
                                .add(Aspect.VOID, 16)
                                .add(Aspect.DARKNESS, 8),
                        -3,
                        5,
                        TcStack.of("taintedmagic:shadow_cloth_wand_cap", 1),
                        0,
                        1)
                .setPages(ResearchPage.text("1"), ResearchPage.arcane("TM:ItemWandCap:3"))
                .setParentsHidden("CAP_cloth")
                .setSecondary()
                .setConcealed()
                .setParents("SHADOWCLOTH")
                .registerResearchItem();

        new TMResearchItem(
                        "PRIMALBLADE",
                        new AspectList()
                                .add(Aspect.MAGIC, 1)
                                .add(Aspect.ELDRITCH, 1)
                                .add(Aspect.WEAPON, 1)
                                .add(Aspect.VOID, 1)
                                .add(Aspect.AURA, 1),
                        11,
                        -3,
                        TcStack.of("taintedmagic:primal_blade", 1),
                        3,
                        5)
                .setPages(ResearchPage.text("1"), ResearchPage.infusion("TM:ItemPrimalBlade"))
                .setParentsHidden("VOIDMETAL", "PRIMALCRUSHER", "UNBALANCEDSHARDS", "HOLLOWDAGGER")
                .setConcealed()
                .setParents("CREATION")
                .registerResearchItem();

        new TMResearchItem(
                        "THAUMIUMKATANA",
                        new AspectList()
                                .add(Aspect.METAL, 8)
                                .add(Aspect.MAGIC, 4)
                                .add(Aspect.WEAPON, 6),
                        12,
                        -1,
                        TcStack.of("taintedmagic:thaumium_fortress_blade", 1),
                        3,
                        0)
                .setPages(ResearchPage.text("1"), ResearchPage.infusion("TM:ItemKatana:0"))
                .setParentsHidden("ARMORFORTRESS")
                .setConcealed()
                .registerResearchItem();

        new TMResearchItem(
                        "VOIDMETALKATANA",
                        new AspectList()
                                .add(Aspect.METAL, 16)
                                .add(Aspect.MAGIC, 8)
                                .add(Aspect.WEAPON, 12)
                                .add(Aspect.VOID, 12),
                        7,
                        -10,
                        TcStack.of("taintedmagic:void_fortress_blade", 1),
                        3,
                        3)
                .setPages(ResearchPage.text("1"), ResearchPage.infusion("TM:ItemKatana:1"))
                .setParentsHidden("THAUMIUMKATANA")
                .setParents("VOIDFORTRESS")
                .setConcealed()
                .setSecondary()
                .registerResearchItem();

        new TMResearchItem(
                        "SHADOWMETALKATANA",
                        new AspectList()
                                .add(Aspect.METAL, 16)
                                .add(Aspect.MAGIC, 8)
                                .add(Aspect.WEAPON, 12)
                                .add(Aspect.VOID, 12)
                                .add(Aspect.DARKNESS, 14),
                        0,
                        5,
                        TcStack.of("taintedmagic:shadow_fortress_blade", 1),
                        3,
                        5)
                .setPages(ResearchPage.text("1"), ResearchPage.infusion("TM:ItemKatana:2"))
                .setParentsHidden("VOIDMETALKATANA")
                .setParents("SHADOWFORTRESS")
                .setConcealed()
                .setSecondary()
                .registerResearchItem();

        new TMResearchItem(
                        "INSCRIPTIONFIRE",
                        new AspectList()
                                .add(Aspect.FIRE, 8)
                                .add(Aspect.ENTROPY, 4)
                                .add(Aspect.METAL, 6),
                        14,
                        0,
                        ResourceLocation.parse("taintedmagic:textures/misc/r_inscription0.png"),
                        0,
                        0)
                .setPages(ResearchPage.text("1"), ResearchPage.infusion("TM:ItemKatanaThaumium:inscription0"))
                .setParents("THAUMIUMKATANA")
                .setParentsHidden("FOCUSFIRE")
                .setConcealed()
                .setSecondary()
                .registerResearchItem();

        new TMResearchItem(
                        "INSCRIPTIONTHUNDER",
                        new AspectList()
                                .add(Aspect.ENTROPY, 8)
                                .add(Aspect.MOTION, 4)
                                .add(Aspect.METAL, 6),
                        14,
                        1,
                        ResourceLocation.parse("taintedmagic:textures/misc/r_inscription1.png"),
                        0,
                        0)
                .setPages(ResearchPage.text("1"), ResearchPage.infusion("TM:ItemKatanaThaumium:inscription1"))
                .setParents("THAUMIUMKATANA")
                .setParentsHidden("FOCUSSHOCKWAVE")
                .setConcealed()
                .setSecondary()
                .registerResearchItem();

        new TMResearchItem(
                        "INSCRIPTIONHEAL",
                        new AspectList()
                                .add(Aspect.HEAL, 8)
                                .add(Aspect.UNDEAD, 4)
                                .add(Aspect.METAL, 6),
                        14,
                        2,
                        ResourceLocation.parse("taintedmagic:textures/misc/r_inscription2.png"),
                        0,
                        2)
                .setPages(ResearchPage.text("1"), ResearchPage.infusion("TM:ItemKatanaThaumium:inscription2"))
                .setParents("THAUMIUMKATANA")
                .setParentsHidden("BATHSALTS")
                .setConcealed()
                .setSecondary()
                .registerResearchItem();

        new TMResearchItem(
                        "VOIDGOGGLES",
                        new AspectList()
                                .add(Aspect.VOID, 8)
                                .add(Aspect.DARKNESS, 4)
                                .add(Aspect.MAGIC, 6)
                                .add(Aspect.SENSES, 12),
                        3,
                        3,
                        TcStack.of("taintedmagic:voidmetal_goggles", 1),
                        0,
                        2)
                .setPages(ResearchPage.text("1"), ResearchPage.infusion("TM:ItemVoidmetalGoggles"))
                .setParents("WARPEDGOGGLES")
                .setParentsHidden("VOIDMETAL")
                .setConcealed()
                .setSecondary()
                .registerResearchItem();

        new TMResearchItem(
                        "ELDRITCHFOCUS",
                        new AspectList()
                                .add(Aspect.ELDRITCH, 22)
                                .add(Aspect.ENTROPY, 14)
                                .add(Aspect.AIR, 4)
                                .add(Aspect.DARKNESS, 6),
                        5,
                        -5,
                        TcStack.of("taintedmagic:focus_dark_matter", 1),
                        3,
                        7)
                .setPages(ResearchPage.text("1"), ResearchPage.infusion("TM:ItemFocusDarkMatter"))
                .setParentsHidden("OUTERREV")
                .setParents("TMELDRITCHMAJOR")
                .setConcealed()
                .setSpecial()
                .registerResearchItem();

        new TMResearchItem(
                        "DIFFUSIONUPGRADE",
                        new AspectList()
                                .add(Aspect.MAGIC, 4)
                                .add(Aspect.DARKNESS, 8)
                                .add(Aspect.WEAPON, 8)
                                .add(Aspect.ELDRITCH, 10),
                        6,
                        -4,
                        ResourceLocation.parse("taintedmagic:textures/foci/icondiffusion.png"),
                        0,
                        2)
                .setPages(ResearchPage.text("1"))
                .setParentsHidden("FOCALMANIPULATION")
                .setConcealed()
                .setParents("ELDRITCHFOCUS")
                .setSecondary()
                .registerResearchItem();

        new TMResearchItem(
                        "MACEFOCUS",
                        new AspectList()
                                .add(Aspect.ENTROPY, 10)
                                .add(Aspect.EARTH, 4)
                                .add(Aspect.WEAPON, 6)
                                .add(Aspect.MAGIC, 8),
                        7,
                        3,
                        TcStack.of("taintedmagic:focus_mage_mace", 1),
                        2,
                        0)
                .setPages(ResearchPage.text("1"), ResearchPage.infusion("TM:ItemFocusMageMace"))
                .setConcealed()
                .setParentsHidden("THAUMIUM", "FOCUSFIRE")
                .registerResearchItem();

        new TMResearchItem(
                        "FOCUSSHOCKWAVE",
                        new AspectList()
                                .add(Aspect.MAGIC, 20)
                                .add(Aspect.ENTROPY, 12)
                                .add(Aspect.AIR, 6)
                                .add(Aspect.MOTION, 12),
                        7,
                        2,
                        TcStack.of("taintedmagic:focus_shockwave", 1),
                        0,
                        0)
                .setPages(ResearchPage.text("1"), ResearchPage.infusion("TM:ItemFocusShockwave"))
                .setParentsHidden("FOCUSSHOCK")
                .setConcealed()
                .setSecondary()
                .registerResearchItem();

        new TMResearchItem(
                        "FOCUSSHARD",
                        new AspectList()
                                .add(Aspect.CRYSTAL, 2)
                                .add(Aspect.MAGIC, 4)
                                .add(Aspect.MOTION, 6),
                        6,
                        1,
                        TcStack.of("taintedmagic:focus_vis_shard", 1),
                        0,
                        0)
                .setPages(ResearchPage.text("1"), ResearchPage.arcane("TM:ItemFocusVisShard"))
                .setParents("UNBALANCEDSHARDS")
                .setParentsHidden("FOCUSFIRE")
                .setConcealed()
                .setSecondary()
                .registerResearchItem();

        new TMResearchItem(
                        "TAINTFOCUS",
                        new AspectList()
                                .add(Aspect.TAINT, 4)
                                .add(Aspect.LIFE, 4)
                                .add(Aspect.MOTION, 4),
                        5,
                        2,
                        TcStack.of("taintedmagic:focus_taint_swarm", 1),
                        3,
                        3)
                .setPages(ResearchPage.text("1"), ResearchPage.infusion("TM:ItemFocusTaintSwarm"))
                .setParentsHidden("INFUSION", "BOTTLETAINT")
                .setParents("FOCUSSHARD")
                .setConcealed()
                .registerResearchItem();

        new TMResearchItem(
                        "FOCUSLUMOS",
                        new AspectList()
                                .add(Aspect.FIRE, 16)
                                .add(Aspect.LIGHT, 24)
                                .add(Aspect.ENERGY, 8),
                        5,
                        3,
                        TcStack.of("taintedmagic:focus_lumos", 1),
                        0,
                        0)
                .setPages(ResearchPage.text("1"), ResearchPage.arcane("TM:ItemFocusLumos"))
                .setParentsHidden("FOCUSFIRE")
                .setConcealed()
                .setSecondary()
                .registerResearchItem();

        new TMResearchItem(
                        "LUMOSRING",
                        new AspectList()
                                .add(Aspect.ARMOR, 16)
                                .add(Aspect.AURA, 12)
                                .add(Aspect.LIGHT, 24)
                                .add(Aspect.ENERGY, 8),
                        6,
                        4,
                        TcStack.of("taintedmagic:lumos_ring", 1),
                        2,
                        0)
                .setPages(ResearchPage.text("1"), ResearchPage.infusion("TM:ItemLumosRing"))
                .setParents("FOCUSLUMOS")
                .setParentsHidden("RUNICARMOR")
                .setConcealed()
                .registerResearchItem();

        new TMResearchItem(
                        "FLYTECHARM",
                        new AspectList()
                                .add(Aspect.FLIGHT, 15)
                                .add(Aspect.AIR, 20)
                                .add(Aspect.SENSES, 8)
                                .add(Aspect.MAGIC, 12),
                        13,
                        -4,
                        TcStack.of("taintedmagic:flyte_charm", 1),
                        3,
                        0)
                .setPages(ResearchPage.text("1"), ResearchPage.infusion("TM:ItemFlyteCharm"))
                .setParents("CREATION")
                .setParentsHidden("VOIDSASH", "PRIMALARROW")
                .setConcealed()
                .registerResearchItem();

        new TMResearchItem(
                        "GATEKEY",
                        new AspectList()
                                .add(Aspect.TRAVEL, 30)
                                .add(Aspect.EXCHANGE, 15)
                                .add(Aspect.AURA, 20)
                                .add(Aspect.FLIGHT, 10),
                        9,
                        -4,
                        TcStack.of("taintedmagic:gate_key", 1),
                        3,
                        0)
                .setPages(ResearchPage.text("1"), ResearchPage.infusion("TM:ItemGateKey"))
                .setParents("CREATION")
                .setConcealed()
                .registerResearchItem();
    }
}
