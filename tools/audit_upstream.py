import hashlib
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
UPSTREAM = ROOT.parents[1] / "reference/Tainted-Magic-8.2.0/src/main"
JAVA = ROOT / "src/main/java/taintedmagic"
RES = ROOT / "src/main/resources"


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


mapping = json.loads((ROOT / "tools/id_map.json").read_text())
registered = set()
for name in ["ItemRegistry", "BlockRegistry"]:
    text = (UPSTREAM / f"java/taintedmagic/common/registry/{name}.java").read_text()
    registered.update(re.findall(r'GameRegistry\.register(?:Item|Block)\([^;]*?,\s*"(\w+)"\s*\)', text))
assert registered <= mapping.keys(), registered - mapping.keys()
item_forms = {item for old, values in mapping.items() if old != "BlockLumos" for item in values}
assert len(item_forms) == 60, len(item_forms)
for item in item_forms:
    assert (RES / f"assets/taintedmagic/models/item/{item}.json").is_file(), item

original_recipes = re.findall(r'ResearchRegistry\.recipes\.put\(\s*"([^"]+)"', (UPSTREAM / "java/taintedmagic/common/registry/RecipeRegistry.java").read_text())
port_recipes = re.findall(r'Recipes\.\w+\("TM:([^"]+)"', (JAVA / "ConvertedRecipes.java").read_text())
assert set(original_recipes) <= set(port_recipes), set(original_recipes) - set(port_recipes)
assert len(original_recipes) == 79 and len(port_recipes) == 85
extra_recipes = ["shadowmetal_ingot", "shadowmetal_nuggets", "warpwood_planks", "void_blood"]
for key in extra_recipes:
    assert (RES / f"data/taintedmagic/recipe/{key}.json").is_file(), key

original_research = re.findall(r'new TMResearchItem\(\s*"([^"]+)"', (UPSTREAM / "java/taintedmagic/common/registry/ResearchRegistry.java").read_text())
port_research = re.findall(r'new TMResearchItem\(\s*"([^"]+)"', (JAVA / "ConvertedResearch.java").read_text())
assert original_research == port_research and len(port_research) == 48
languages = {path.stem: json.loads(path.read_text(encoding="utf-8")) for path in (RES / "assets/taintedmagic/lang").glob("*.json")}
missing_english = [key for key in original_research for key in ["tm.name." + key, "tm.tag." + key] if key not in languages["en_us"]]
assert not missing_english, missing_english

assets = []
for source in (UPSTREAM / "resources/assets/taintedmagic").rglob("*"):
    if not source.is_file() or source.suffix == ".lang":
        continue
    relative = source.relative_to(UPSTREAM / "resources/assets/taintedmagic").as_posix().lower()
    target = RES / "assets/taintedmagic" / relative
    assert target.is_file(), relative
    if source.suffix in [".png", ".ogg"]:
        assert digest(source) == digest(target), relative
    assets.append(relative)

groups = {
    "ItemFocus": ["MagicFocus", "Foci", "MagicProjectile", "SummonedSwarm", "client/ProjectileRenderer"],
    "ItemShadowmetal": ["ShadowTool"],
    "ItemShadowFortressArmor": ["FortressArmor"],
    "ItemVoidFortressArmor": ["FortressArmor"],
    "ItemVoidmetalGoggles": ["MagicArmor"],
    "ItemWarpedGoggles": ["MagicArmor"],
    "ItemVoidwalkerBoots": ["VoidwalkerBoots", "Events"],
    "ItemVoidwalkerSash": ["Trinket", "Events"],
    "ItemLumosRing": ["Trinket", "Events"],
    "ItemKatana": ["FortressBlade", "client/BladeRenderer", "client/Client"],
    "RenderItemKatana": ["client/BladeRenderer"],
    "ModelKatana": ["client/BladeModel"],
    "ModelSaya": ["client/BladeModel"],
    "ItemHollowDagger": ["Weapon", "Events"],
    "ItemPrimalBlade": ["Weapon"],
    "ItemThaumicDisassembler": ["Disassembler", "Events"],
    "ItemMagicFunguar": ["FoodItem"],
    "ItemNightshadeBerries": ["FoodItem"],
    "ItemMaterial": ["Content", "TMItem"],
    "ItemWandRod": ["Content", "Magic"],
    "ItemWandCap": ["Content", "Magic"],
    "ItemCrimsonBlood": ["UtilityItem", "Events"],
    "ItemVoidBlood": ["UtilityItem", "VoidBloodRecipe"],
    "ItemSalis": ["UtilityItem"],
    "ItemGateKey": ["UtilityItem"],
    "ItemWarpFertilizer": ["UtilityItem"],
    "ItemFlyteCharm": ["UtilityItem", "Events", "client/Client"],
    "BlockLumos": ["LumosBlock"],
    "BlockNightshadeBush": ["NightshadeBush"],
    "BlockWarpwoodLog": ["WarpwoodLog"],
    "BlockWarpwoodLeaves": ["WarpwoodLeaves", "Content", "tools/gen_resources.py"],
    "BlockWarpwoodPlanks": ["Content"],
    "BlockWarpwoodSapling": ["WarpwoodSapling"],
    "WorldGenWarpwoodTree": ["WarpwoodSapling"],
    "RecipeVoidBlood": ["VoidBloodRecipe"],
    "EntityDarkMatter": ["MagicProjectile"],
    "EntityDiffusion": ["MagicProjectile"],
    "EntityHomingShard": ["MagicProjectile"],
    "RenderEntity": ["client/ProjectileRenderer"],
    "TMFocusUpgrades": ["Foci", "StartupConfig"],
    "ConfigHandler": ["Config", "StartupConfig"],
    "WandHandler": ["Magic"],
    "TMEventHandler": ["Events"],
    "ResearchRegistry": ["ConvertedResearch", "EldritchResearchLink"],
    "TMResearchItem": ["TMResearchItem"],
    "ThaumcraftResearchItem": ["EldritchResearchLink"],
    "RecipeRegistry": ["ConvertedRecipes", "Recipes"],
    "OreDictRegistry": ["tools/gen_resources.py"],
    "ItemRegistry": ["Content"],
    "BlockRegistry": ["Content"],
    "TMEntityRegistry": ["Content", "client/Client"],
    "TMMaterials": ["Content"],
    "RenderItemSyncHandler": ["Events", "VisualState"],
    "PacketSyncRenderItems": ["VisualState"],
    "PacketDisplayString": ["Trinket", "UtilityItem", "Events"],
    "PacketHandler": ["VisualState", "Sparkles"],
    "HUDHandler": ["client/Client"],
    "IRenderInventoryItem": ["VisualState", "client/Client"],
    "IHeldItemHUD": ["client/Client"],
    "TaintedMagicHelper": ["Magic", "Events"],
    "TaintedMagic": ["TaintedMagic"],
    "TMCreativeTab": ["Content"],
    "ClientProxy": ["client/Client"],
    "ClientHandler": ["client/Client"],
    "CommonProxy": ["TaintedMagic"],
}
classes = []
for source in (UPSTREAM / "java/taintedmagic").rglob("*.java"):
    matches = [paths for prefix, paths in groups.items() if source.stem.startswith(prefix)]
    assert matches, source
    targets = sorted({target for paths in matches for target in paths})
    for target in targets:
        path = ROOT / target if target.endswith(".py") else JAVA / (target + ".java")
        assert path.is_file(), path
    classes.append({"original": source.relative_to(UPSTREAM / "java").as_posix(), "source_sha256": digest(source), "ported_in": targets})

report = {
    "reference": "Tainted Magic 8.2.0 for Minecraft 1.7.10",
    "item_and_block_id_map": mapping,
    "item_forms": len(item_forms),
    "ordinary_recipe_additions": extra_recipes,
    "original_recipe_keys": original_recipes,
    "ported_recipe_keys": port_recipes,
    "original_recipe_total": len(original_recipes) + len(extra_recipes),
    "research_keys": original_research + ["TMELDRITCHMAJOR"],
    "languages": sorted(languages),
    "preserved_assets": len(assets),
    "source_classes": classes,
    "modern_adaptations": [
        "Legacy metadata variants use stable separate registry IDs; both Warpwood log states share one block ID.",
        "Three wildcard inscription recipes expand to all three blade materials (+6 registry recipes).",
        "Minecraft 1.21.1 equipment components, enchantment tags, Curios slots and native melee recovery replace the 1.7.10 APIs.",
        "By user request, staff base damage increases from 7 to 9 with 1 attack/sec; wand mace uses 1.6 attacks/sec, staff mace 1 attack/sec.",
        "Damage sources respect PvP, team and world edit permissions. Gate Key supports modern negative build heights.",
        "Leaves use native distance based decay and modern collision uses surface normals for shard ricochets.",
        "Decorative particles run through the existing Thaumcraft particle engine and its quality setting.",
    ],
}
destination = ROOT / "tools/upstream-audit.json"
destination.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
print(f"Audited {len(classes)} original classes, {len(item_forms)} item forms, {report['original_recipe_total']} original recipes, {len(report['research_keys'])} research entries, {len(assets)} original assets.")
