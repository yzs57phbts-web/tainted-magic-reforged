import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
WORK = ROOT.parents[1]
OLD = WORK / "reference/Tainted-Magic-8.2.0/src/main/java/taintedmagic/common/registry"
TC = ROOT.parent / "thaumcraft-4-reforged-1.21.1"
JAVA = ROOT / "src/main/java/taintedmagic"
RES = ROOT / "src/main/resources"
MAP = json.loads((ROOT / "tools/id_map.json").read_text())
TCMAP = json.loads((TC / "tools/id_map.json").read_text())
VAN = json.loads((TC / "tools/vanilla_ids.json").read_text())
FIELDS = dict(re.findall(r'GameRegistry\.registerItem\((\w+),\s*"(\w+)"', (WORK / "reference/tc4-gear-decompiled/thaumcraft/common/config/ConfigItems.java").read_text()))


def split(text):
    parts, start, depth, quote, escape = [], 0, 0, None, False
    for i, c in enumerate(text):
        if quote:
            if escape:
                escape = False
            elif c == "\\":
                escape = True
            elif c == quote:
                quote = None
        elif c in "\"'":
            quote = c
        elif c in "([{":
            depth += 1
        elif c in ")]}":
            depth -= 1
        elif c == "," and depth == 0:
            parts.append(text[start:i].strip())
            start = i + 1
    if text[start:].strip():
        parts.append(text[start:].strip())
    return parts


def calls(text, token):
    out = []
    for match in re.finditer(re.escape(token) + r"\s*\(", text):
        start = match.end()
        depth, quote, escape = 1, None, False
        for i in range(start, len(text)):
            c = text[i]
            if quote:
                if escape:
                    escape = False
                elif c == "\\":
                    escape = True
                elif c == quote:
                    quote = None
            elif c in "\"'":
                quote = c
            elif c == "(":
                depth += 1
            elif c == ")":
                depth -= 1
                if depth == 0:
                    out.append((match.start(), i + 1, text[start:i]))
                    break
    return out


def ids(ref, meta=0):
    field = ref.split(".")[-1]
    wildcard = meta == 32767
    if ref.startswith(("ItemRegistry.", "BlockRegistry.")):
        names = MAP[field]
        return ["taintedmagic:" + name for name in (names if wildcard else [names[meta if len(names) > 1 else 0]])]
    if ref.startswith(("ConfigItems.", "ConfigBlocks.")):
        old = FIELDS.get(field, field)
        rows = [r for r in TCMAP["items" if ref.startswith("ConfigItems.") else "blocks"] if r["old"] == old]
        rows = [r for r in rows if wildcard or r["meta"] in (None, meta)]
        values = ["thaumcraft:" + r["id"] for r in rows if r.get("id") and not r.get("skip")]
        if values:
            return list(dict.fromkeys(values))
        for row in rows:
            replaced = re.search(r"replaced by vanilla (minecraft:\w+)", row.get("skip") or "")
            if replaced:
                return [replaced[1]]
        raise ValueError((ref, meta, rows))
    kind = "items" if ref.startswith("Items.") else "blocks"
    base = VAN["mcp_fields"][ref]
    value = VAN["flatten"][kind].get(f"{base}:{meta}") or VAN["names"][kind].get(base)
    if not value:
        raise ValueError((ref, meta))
    return [VAN["profiles"]["1.21.1"].get(value, value)]


def stack_spec(expr):
    expr = expr.strip()
    if "(" not in expr:
        return ids(expr), 1
    args = split(expr[expr.index("(") + 1:expr.rfind(")")])
    ref = args[0]
    count = int(args[1]) if len(args) > 1 else 1
    meta = (32767 if "WILDCARD" in args[2] else int(args[2])) if len(args) > 2 else 0
    return ids(ref, meta), count


def stack(expr):
    names, count = stack_spec(expr)
    assert len(names) == 1, expr
    return f'TcStack.of("{names[0]}", {count})'


def ing(expr):
    expr = expr.strip()
    if expr.startswith('"'):
        tags = {"ingotShadow": "c:ingots/shadowmetal", "ingotThaumium": "c:ingots/thaumium", "ingotVoid": "c:ingots/void_metal", "ingotIron": "c:ingots/iron", "nuggetGold": "c:nuggets/gold"}
        return f'TcIngredient.tag("{tags[json.loads(expr)]}")'
    names, count = stack_spec(expr)
    if len(names) > 1:
        return "TcIngredient.anyOf(" + ", ".join(f'TcIngredient.item("{n}")' for n in names) + ")"
    return f'TcIngredient.item("{names[0]}")'


def aspects(expr):
    return re.sub(r"\s+", " ", expr).replace("TaintedMagicHelper.getPrimals", "Magic.primals")


def write(path, text):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8")


def jsonwrite(path, value):
    write(path, json.dumps(value, ensure_ascii=False, indent=2) + "\n")


text = (OLD / "RecipeRegistry.java").read_text()
text = re.sub(r"/\*.*?\*/|//[^\n]*", "", text, flags=re.S)
recipes = []
for start, end, body in calls(text, "ResearchRegistry.recipes.put"):
    key, expr = split(body)
    key = json.loads(key)
    typ = expr[:expr.index("(")].split(".")[-1]
    args = split(expr[expr.index("(") + 1:expr.rfind(")")])
    recipes.append((key, typ, args))

lines = []
counts = {}
for key, typ, args in recipes:
    counts[typ] = counts.get(typ, 0) + 1
    if typ == "addInfusionCraftingRecipe":
        research, result, inst, cost, central, components = args
        parts = split(components[components.index("{") + 1:components.rindex("}")])
        if result.startswith("new Object"):
            prop, val = split(result[result.index("{") + 1:result.rindex("}")])
            prop = '"inscription"' if "TAG_INSCRIPTION" in prop else prop
            val = re.search(r"(\d+)\s*\)\s*$", val).group(1)
            names, _ = stack_spec(central)
            for index, name in enumerate(names):
                actualkey = key if index == 0 else key + f"_{index}"
                output = f'TcStack.withData("{name}", 1, Map.of({prop}, "{val}"))'
                lines.append(f'        Recipes.infusion("TM:{actualkey}", {research}, {output}, {inst}, {aspects(cost)}, TcIngredient.item("{name}"), ' + ", ".join(ing(p) for p in parts) + ");")
        else:
            lines.append(f'        Recipes.infusion("TM:{key}", {research}, {stack(result)}, {inst}, {aspects(cost)}, {ing(central)}, ' + ", ".join(ing(p) for p in parts) + ");")
    elif typ == "addCrucibleRecipe":
        research, result, catalyst, cost = args
        lines.append(f'        Recipes.crucible("TM:{key}", {research}, {stack(result)}, {ing(catalyst)}, {aspects(cost)});')
    elif typ in ("addArcaneCraftingRecipe", "addShapelessArcaneCraftingRecipe"):
        research, result, cost, *params = args
        if typ == "addShapelessArcaneCraftingRecipe":
            lines.append(f'        Recipes.arcaneShapeless("TM:{key}", {research}, {stack(result)}, {aspects(cost)}, ' + ", ".join(ing(p) for p in params) + ");")
        else:
            pattern = []
            while params and params[0].startswith('"'):
                pattern.append(params.pop(0))
            symbols = []
            while params:
                symbols.extend([params.pop(0), ing(params.pop(0))])
            lines.append(f'        Recipes.arcane("TM:{key}", {research}, {stack(result)}, {aspects(cost)}, new String[] {{' + ", ".join(pattern) + "}, " + ", ".join(symbols) + ");")
    elif typ == "addShapedRecipe":
        result, *params = args
        pattern = []
        while params and params[0].startswith('"'):
            pattern.append(json.loads(params.pop(0)))
        symbols = {}
        while params:
            letter, expr = params.pop(0), params.pop(0)
            name = stack_spec(expr)[0][0]
            symbols[letter[1:-1]] = {"item": name}
        name = key.lower()
        result_names, count = stack_spec(result)
        jsonwrite(RES / f"data/taintedmagic/recipe/{name}.json", {"type": "minecraft:crafting_shaped", "pattern": pattern, "key": symbols, "result": {"id": result_names[0], "count": count}})
        lines.append(f'        Recipes.vanilla("TM:{key}", "SHADOWMETAL", "{name}", {stack(result)});')
    else:
        raise ValueError(typ)

for name, result, count, ingredient in [
    ("shadowmetal_nuggets", "shadowmetal_nugget", 9, "shadowmetal_ingot"),
    ("warpwood_planks", "warpwood_planks", 4, "warpwood_log"),
]:
    jsonwrite(RES / f"data/taintedmagic/recipe/{name}.json", {"type": "minecraft:crafting_shapeless", "ingredients": [{"item": "taintedmagic:" + ingredient}], "result": {"id": "taintedmagic:" + result, "count": count}})
jsonwrite(RES / "data/taintedmagic/recipe/shadowmetal_ingot.json", {"type": "minecraft:crafting_shaped", "pattern": ["AAA"] * 3, "key": {"A": {"item": "taintedmagic:shadowmetal_nugget"}}, "result": {"id": "taintedmagic:shadowmetal_ingot", "count": 1}})
write(JAVA / "ConvertedRecipes.java", "package taintedmagic;\n\nimport java.util.Map;\nimport thaumcraft.aspect.*;\nimport thaumcraft.crafting.*;\n\npublic final class ConvertedRecipes {\n    public static void init() {\n" + "\n".join(lines) + "\n    }\n}\n")

text = (OLD / "ResearchRegistry.java").read_text()
start = text.index("new TMResearchItem(")
text = text[start:text.index("\n    }", start)]
text = re.sub(r"/\*.*?\*/|//[^\n]*", "", text, flags=re.S)
for start, end, body in reversed(calls(text, "new ItemStack")):
    text = text[:start] + stack("new ItemStack(" + body + ")") + text[end:]
text = text.replace("new ResearchPage(", "ResearchPage.text(")
text = re.sub(r'\.setItemTriggers\(TcStack.of\("([^"]+)", 1\)\)', r'.setItemTriggers("\1")', text)
text = text.replace("TaintedMagicHelper.getPrimals", "Magic.primals")
text = text.replace("new ResourceLocation(", "ResourceLocation.parse(")
for name, typ in [("recipePage", "crafting"), ("arcanePage", "arcane"), ("infusionPage", "infusion"), ("cruciblePage", "crucible")]:
    text = re.sub(name + r'\("([^"]+)"\)', lambda m: f'ResearchPage.{typ}("TM:{m[1]}")', text)
text = text.replace("ItemApi.getItem(\"itemResource\", 1)", 'TcStack.of("thaumcraft:nitor")')
text = re.sub(r'ItemApi.getItem\("(\w+)", (\d+)\)', lambda m: f'TcStack.of("{ids("ConfigItems." + m[1], int(m[2]))[0]}")', text)
text = text.replace("taintedmagic:textures/misc/", "taintedmagic:textures/misc/")
for match in re.findall(r'ResourceLocation.parse\("([^"]+)"\)', text):
    text = text.replace(match, match.lower())
write(JAVA / "ConvertedResearch.java", "package taintedmagic;\n\nimport net.minecraft.resources.ResourceLocation;\nimport thaumcraft.aspect.*;\nimport thaumcraft.crafting.*;\nimport thaumcraft.research.*;\n\npublic final class ConvertedResearch {\n    public static void init() {\n        ResearchCategories.registerCategory(\"TAINTEDMAGIC\", TaintedMagic.id(\"textures/misc/tab_tm.png\"), StartupConfig.CUSTOM_BACKGROUND.get() ? TaintedMagic.id(\"textures/gui/gui_tm_researchback.png\") : ResourceLocation.parse(\"thaumcraft:textures/gui/gui_researchback.png\"));\n        " + text + "\n    }\n}\n")
jsonwrite(ROOT / "tools/conversion-report.json", {"upstream_recipes": len(recipes), "types": counts, "research_entries": text.count("new TMResearchItem(")})
print("Converted:", len(recipes), "recipes,", text.count("new TMResearchItem("), "research entries")
