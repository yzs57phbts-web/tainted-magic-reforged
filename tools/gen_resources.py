import json
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "src/main/resources"
ASSETS = RES / "assets/taintedmagic"
DATA = RES / "data"


def put(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def tag(namespace, kind, name, values):
    put(DATA / f"{namespace}/tags/{kind}/{name}.json", {"replace": False, "values": values})


for name, texture in [("warpwood_planks", "blockwarpwoodplanks"), ("warpwood_leaves", "blockwarpwoodleaves_transparent")]:
    model = {"parent": "minecraft:block/cube_all", "textures": {"all": "taintedmagic:blocks/" + texture}}
    if name == "warpwood_leaves": model["render_type"] = "minecraft:cutout_mipped"
    put(ASSETS / f"models/block/{name}.json", model)
    put(ASSETS / f"blockstates/{name}.json", {"variants": {"": {"model": "taintedmagic:block/" + name}}})

put(ASSETS / "models/block/warpwood_log.json", {"parent": "minecraft:block/cube_column", "textures": {"end": "taintedmagic:blocks/blockwarpwoodlog_top", "side": "taintedmagic:blocks/blockwarpwoodlog_side"}})
put(ASSETS / "models/block/warpwood_log_knot.json", {"parent": "minecraft:block/cube_column", "textures": {"end": "taintedmagic:blocks/blockwarpwoodlog_top", "side": "taintedmagic:blocks/blockwarpwoodlog_knot"}})
variants = {}
for axis in "xyz":
    for knot in ("true", "false"):
        model = {"model": "taintedmagic:block/warpwood_log" + ("_knot" if knot == "true" else "")}
        if axis != "y":
            model["x"] = 90
        if axis == "x":
            model["y"] = 90
        variants[f"axis={axis},knot={knot}"] = model
put(ASSETS / "blockstates/warpwood_log.json", {"variants": variants})
for name, texture in [("warpwood_sapling", "blockwarpwoodsapling"), ("nightshade_bush", "blocknightshadebush")]:
    put(ASSETS / f"models/block/{name}.json", {"parent": "minecraft:block/cross", "render_type": "minecraft:cutout", "textures": {"cross": "taintedmagic:blocks/" + texture}})
    put(ASSETS / f"blockstates/{name}.json", {"variants": {"": {"model": "taintedmagic:block/" + name}}})
    put(ASSETS / f"models/item/{name}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": "taintedmagic:blocks/" + texture}})
for name in ["warpwood_log", "warpwood_leaves", "warpwood_planks"]:
    put(ASSETS / f"models/item/{name}.json", {"parent": f"taintedmagic:block/{name}"})

for name in ["warpwood_planks", "warpwood_sapling"]:
    put(DATA / f"taintedmagic/loot_table/blocks/{name}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": "taintedmagic:" + name}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
put(DATA / "taintedmagic/loot_table/blocks/warpwood_log.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:alternatives", "children": [{"type": "minecraft:item", "name": "thaumcraft:void_seed", "conditions": [{"condition": "minecraft:block_state_property", "block": "taintedmagic:warpwood_log", "properties": {"knot": "true"}}], "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 5}}]}, {"type": "minecraft:item", "name": "taintedmagic:warpwood_log"}]}]}]})
shears = {"condition": "minecraft:match_tool", "predicate": {"items": "minecraft:shears"}}
silk = {"condition": "minecraft:match_tool", "predicate": {"predicates": {"minecraft:enchantments": [{"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}
put(DATA / "taintedmagic/loot_table/blocks/warpwood_leaves.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "conditions": [{"condition": "minecraft:any_of", "terms": [shears, silk]}], "entries": [{"type": "minecraft:item", "name": "taintedmagic:warpwood_leaves"}]}]})
put(DATA / "taintedmagic/loot_table/blocks/nightshade_bush.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:alternatives", "children": [{"type": "minecraft:item", "name": "taintedmagic:nightshade_bush", "conditions": [shears]}, {"type": "minecraft:item", "name": "taintedmagic:nightshade_berries", "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 3}}]}]}]}]})
tag("minecraft", "block", "logs", ["taintedmagic:warpwood_log"])
tag("minecraft", "block", "logs_that_burn", ["taintedmagic:warpwood_log"])
tag("minecraft", "block", "leaves", ["taintedmagic:warpwood_leaves"])
tag("minecraft", "block", "saplings", ["taintedmagic:warpwood_sapling"])
tag("minecraft", "block", "mineable/axe", ["taintedmagic:warpwood_log", "taintedmagic:warpwood_planks"])
tag("minecraft", "item", "logs", ["taintedmagic:warpwood_log"])
tag("minecraft", "item", "logs_that_burn", ["taintedmagic:warpwood_log"])
tag("minecraft", "item", "planks", ["taintedmagic:warpwood_planks"])
tag("minecraft", "item", "leaves", ["taintedmagic:warpwood_leaves"])
tag("minecraft", "item", "saplings", ["taintedmagic:warpwood_sapling"])
tag("c", "item", "ingots/shadowmetal", ["taintedmagic:shadowmetal_ingot"])
tag("c", "item", "nuggets/shadowmetal", ["taintedmagic:shadowmetal_nugget"])
tag("taintedmagic", "item", "shadowmetal_repair", ["taintedmagic:shadowmetal_ingot"])
tag("taintedmagic", "item", "hollow_repair", ["minecraft:iron_ingot"])
tag("taintedmagic", "item", "primal_repair", ["thaumcraft:void_metal_ingot"])
tag("curios", "item", "ring", ["taintedmagic:lumos_ring"])
tag("curios", "item", "belt", ["taintedmagic:voidwalker_sash"])
swords = ["shadowmetal_sword", "hollow_dagger", "primal_blade"]
diggers = ["shadowmetal_pick", "shadowmetal_spade", "shadowmetal_axe", "shadowmetal_hoe"]
head = ["warped_goggles", "voidmetal_goggles", "void_fortress_helmet", "shadow_fortress_helmet"]
chest = ["void_fortress_chestplate", "shadow_fortress_chestplate"]
legs = ["void_fortress_leggings", "shadow_fortress_leggings"]
feet = ["voidwalker_boots"]
armor = head + chest + legs + feet
for suffix, names in {"weapon": swords + ["shadowmetal_axe"], "sharp_weapon": swords + ["shadowmetal_axe"], "melee_weapon": swords, "sweeping": swords, "fire_aspect": swords, "mining": diggers, "mining_loot": diggers, "durability": swords + diggers + armor, "armor": armor, "head_armor": head, "chest_armor": chest, "leg_armor": legs, "foot_armor": feet}.items():
    tag("minecraft", "item", "enchantable/" + suffix, ["taintedmagic:" + name for name in names])
for kind, names in {"swords": swords, "axes": ["shadowmetal_axe"], "pickaxes": ["shadowmetal_pick"], "shovels": ["shadowmetal_spade"], "hoes": ["shadowmetal_hoe"], "head_armor": head, "chest_armor": chest, "leg_armor": legs, "foot_armor": feet}.items():
    tag("minecraft", "item", kind, ["taintedmagic:" + name for name in names])
tag("thaumcraft", "item", "enchantable/repair", ["taintedmagic:" + name for name in swords + diggers + armor + ["thaumium_fortress_blade", "void_fortress_blade", "shadow_fortress_blade"]])
put(DATA / "taintedmagic/recipe/void_blood.json", {"type": "taintedmagic:void_blood", "category": "misc"})
put(DATA / "taintedmagic/damage_type/charged_strike.json", {"message_id": "player", "exhaustion": 0.1, "scaling": "when_caused_by_living_non_player"})
tag("minecraft", "damage_type", "bypasses_armor", ["taintedmagic:charged_strike"])

for name, texture in [("void_fortress", "modelvoidfortressarmor"), ("shadow_fortress", "modelshadowfortressarmor"), ("warped_goggles", "modelwarpedgoggles"), ("voidmetal_goggles", "modelvoidmetalgoggles"), ("voidwalker", "modelvoidwalkerboots")]:
    for layer in (1, 2):
        target = ASSETS / f"textures/models/armor/{name}_layer_{layer}.png"
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(ASSETS / f"textures/models/{texture}.png", target)

for name in ["thaumium_fortress_blade", "void_fortress_blade", "shadow_fortress_blade"]:
    display = {
        "thirdperson_righthand": {"rotation": [45, -65, 0], "translation": [0, 0, 0], "scale": [0.85] * 3},
        "thirdperson_lefthand": {"rotation": [45, 65, 0], "translation": [0, 0, 0], "scale": [0.85] * 3},
        "firstperson_righthand": {"rotation": [0, -65, -25], "translation": [-2, 5, 0], "scale": [0.68] * 3},
        "firstperson_lefthand": {"rotation": [0, 65, 25], "translation": [-2, 5, 0], "scale": [0.68] * 3},
        "gui": {"rotation": [0, 45, -40], "scale": [1] * 3},
        "ground": {"translation": [0, 3, 0], "scale": [0.5] * 3},
        "fixed": {"rotation": [0, 180, -40], "scale": [0.8] * 3},
    }
    put(ASSETS / f"models/item/{name}.json", {"parent": "builtin/entity", "gui_light": "front", "display": display})
put(ASSETS / "models/item/gate_key.json", {"parent": "minecraft:item/generated", "textures": {"layer0": "taintedmagic:items/itemgatekey", "layer1": "taintedmagic:items/itemgatekey_overlay"}})
sounds = json.loads((ASSETS / "sounds.json").read_text())
for event in sounds.values():
    event.pop("category", None)
    for sound in event["sounds"]:
        if ":" not in sound["name"]:
            sound["name"] = "taintedmagic:" + sound["name"]
put(ASSETS / "sounds.json", sounds)
for path in ASSETS.glob("lang/*.json"):
    values = json.loads(path.read_text(encoding="utf-8"))
    values["tc.research_category.TAINTEDMAGIC"] = "Tainted Magic"
    put(path, values)
for old, new in [("items", "item"), ("blocks", "block")]:
    for source in (ASSETS / f"textures/{old}").glob("*"):
        target = ASSETS / f"textures/{new}" / source.name
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(source, target)
for path in (ASSETS / "models").rglob("*.json"):
    path.write_text(path.read_text(encoding="utf-8").replace("taintedmagic:items/", "taintedmagic:item/").replace("taintedmagic:blocks/", "taintedmagic:block/"), encoding="utf-8")
put(ASSETS / "models/block/lumos.json", {"elements": [], "textures": {"particle": "minecraft:block/glass"}})
put(ASSETS / "blockstates/lumos.json", {"variants": {"": {"model": "taintedmagic:block/lumos"}}})
print("Resources generated")

for name, base, ornament in [("dark_matter", "darkmatter", "thaumcraft:item/focus_warding_orn"), ("taint_swarm", "taintswarm", "thaumcraft:item/focus_whatever_orn"), ("mage_mace", "magemace", "taintedmagic:item/itemfocusmagemace_orn")]:
    put(ASSETS / f"models/item/focus_{name}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": ornament, "layer1": "taintedmagic:item/itemfocus" + base}})
