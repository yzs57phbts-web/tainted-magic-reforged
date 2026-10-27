import json
import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
WORK = ROOT.parents[1]
OLD = WORK / "reference/Tainted-Magic-8.2.0"
TC = ROOT.parent / "thaumcraft-4-reforged-1.21.1"
TT = ROOT.parent / "thaumic-tinkerer-reforged-1.21.1"
JAVA = ROOT / "src/main/java/taintedmagic"
RES = ROOT / "src/main/resources"
JAVA.mkdir(parents=True, exist_ok=True)


def write(path, text):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8")


def js(path, value):
    write(path, json.dumps(value, ensure_ascii=False, indent=2) + "\n")


for name in ("gradlew", "gradlew.bat", "gradle/wrapper/gradle-wrapper.jar", "gradle/wrapper/gradle-wrapper.properties"):
    target = ROOT / name
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(TC / name, target)

write(ROOT / "settings.gradle", """pluginManagement { repositories { gradlePluginPortal() } }
plugins { id 'org.gradle.toolchains.foojay-resolver-convention' version '1.0.0' }
rootProject.name = 'tainted-magic-reforged'
""")
write(ROOT / "gradle.properties", """org.gradle.jvmargs=-Xmx3G
org.gradle.caching=true
org.gradle.configuration-cache=true
org.gradle.java.installations.paths=C:/Users/123/Documents/ChatGPT/Perpek/.tools/gradle-home/jdks/eclipse_adoptium-21-amd64-windows.2
mod_version=8.2.0-port.1
""")
write(ROOT / "build.gradle", """plugins {
    id 'java-library'
    id 'net.neoforged.moddev' version '2.0.148'
}
version = mod_version
group = 'taintedmagic'
base { archivesName = 'tainted-magic-reforged' }
java.toolchain.languageVersion = JavaLanguageVersion.of(21)
repositories {
    maven { url = 'https://maven.theillusivec4.top/' }
    maven { url = 'https://maven.minecraftforge.net/' }
}
neoForge {
    version = '21.1.256'
    parchment { minecraftVersion = '1.21.1'; mappingsVersion = '2024.11.17' }
    runs {
        client { client(); gameDirectory = file('run/client') }
        server { server(); gameDirectory = file('run/server'); programArgument '--nogui' }
        gameTestServer {
            type = 'gameTestServer'
            gameDirectory = file('run/gametest')
            programArgument '--world'
            programArgument file('build/gametestworld').absolutePath
            systemProperty 'neoforge.enabledGameTestNamespaces', 'taintedmagic'
        }
        configureEach { logLevel = org.slf4j.event.Level.INFO }
    }
    mods { taintedmagic { sourceSet(sourceSets.main) } }
}
dependencies {
    implementation files('../thaumcraft-4-reforged-1.21.1/build/libs/thaumcraft-4-reforged-1.1-bbtt.7-1.21.1.jar')
    implementation 'com.github.glitchfiend:TerraBlender-neoforge:1.21.1-4.1.0.1'
    implementation 'top.theillusivec4.curios:curios-neoforge:9.5.1+1.21.1'
}
tasks.withType(JavaCompile).configureEach { options.encoding = 'UTF-8' }
processResources { inputs.property('version', mod_version); filesMatching('META-INF/neoforge.mods.toml') { expand(version: mod_version) } }
tasks.named('jar', Jar) { archiveFileName = "tainted-magic-reforged-${mod_version}-1.21.1.jar" }
tasks.named('runGameTestServer') {
    doFirst { file('run/gametest/logs/latest.log').delete() }
    doLast {
        def log = file('run/gametest/logs/latest.log')
        if (!log.isFile() || !(log.text =~ /All [1-9][0-9]* required tests passed/)) {
            throw new GradleException('GameTest server did not finish a successful test run')
        }
    }
}
""")
write(RES / "META-INF/neoforge.mods.toml", '''modLoader="javafml"
loaderVersion="[4,)"
license="CC BY-NC-ND 4.0 (original); private local adaptation"
[[mods]]
modId="taintedmagic"
version="${version}"
displayName="Tainted Magic: Reforged"
authors="John Yorke (original), local 1.21.1 adaptation"
description="Tainted Magic 8.2.0 adapted to Thaumcraft 4: Reforged on Minecraft 1.21.1."
[[dependencies.taintedmagic]]
modId="neoforge"
type="required"
versionRange="[21.1.256,)"
ordering="NONE"
side="BOTH"
[[dependencies.taintedmagic]]
modId="minecraft"
type="required"
versionRange="[1.21.1]"
ordering="NONE"
side="BOTH"
[[dependencies.taintedmagic]]
modId="thaumcraft"
type="required"
versionRange="[1.1,)"
ordering="AFTER"
side="BOTH"
[[dependencies.taintedmagic]]
modId="curios"
type="required"
versionRange="[9.5,)"
ordering="AFTER"
side="BOTH"
''')
js(RES / "pack.mcmeta", {"pack": {"pack_format": 34, "description": "Tainted Magic resources"}})
shutil.copyfile(OLD / "README.md", ROOT / "ORIGINAL-README.md")
write(ROOT / "UPSTREAM.txt", "Original: https://github.com/yorkeJohn/Tainted-Magic\nReference commit: e7f1640dbcb39127046b16a7f4fff506c0cad75f\nOriginal author: John Yorke\nOriginal license: CC BY-NC-ND 4.0\nPrivate local adaptation for Minecraft 1.21.1 and Thaumcraft 4: Reforged.\n")

materials = "shadowmetal_ingot shadow_cloth crimson_cloth warped_shard tainted_shard creation_shard thaumic_alloy_plate crimson_alloy_plate shadowmetal_nugget primordial_nodule primordial_mote creation_fragment".split()
subtypes = {"ItemMaterial": materials, "ItemSalis": ["salis_tempestas", "salis_aevum"], "ItemWandRod": ["warpwood_rod", "warpwood_staff_core"], "ItemWandCap": ["shadowmetal_wand_cap", "cloth_wand_cap", "crimson_cloth_wand_cap", "shadow_cloth_wand_cap"], "ItemKatana": ["thaumium_fortress_blade", "void_fortress_blade", "shadow_fortress_blade"]}
singles = "ItemMagicFunguar ItemNightshadeBerries ItemVoidBlood ItemCrimsonBlood ItemFlyteCharm ItemGateKey ItemWarpFertilizer ItemFocusTaintSwarm ItemFocusDarkMatter ItemFocusMageMace ItemFocusShockwave ItemFocusVisShard ItemFocusLumos ItemWarpedGoggles ItemVoidmetalGoggles ItemVoidwalkerBoots ItemVoidwalkerSash ItemVoidFortressHelmet ItemVoidFortressChestplate ItemVoidFortressLeggings ItemShadowFortressHelmet ItemShadowFortressChestplate ItemShadowFortressLeggings ItemLumosRing ItemThaumicDisassembler ItemHollowDagger ItemShadowmetalHoe ItemShadowmetalPick ItemShadowmetalSpade ItemShadowmetalAxe ItemShadowmetalSword ItemPrimalBlade".split()
blocks = "BlockWarpwoodLog BlockWarpwoodLeaves BlockWarpwoodSapling BlockWarpwoodPlanks BlockNightshadeBush BlockLumos".split()


def snake(name):
    return re.sub(r"(?<=[a-z0-9])([A-Z])", r"_\1", name).lower()


mapping = {}
for old in singles + blocks:
    mapping[old] = [snake(re.sub(r"^(Item|Block)", "", old))]
mapping.update(subtypes)
js(ROOT / "tools/id_map.json", mapping)

for file in (OLD / "src/main/resources/assets/taintedmagic").rglob("*"):
    if not file.is_file() or file.suffix == ".lang":
        continue
    rel = file.relative_to(OLD / "src/main/resources/assets/taintedmagic")
    target = RES / "assets/taintedmagic" / str(rel).lower()
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(file, target)

for old, names in mapping.items():
    for index, name in enumerate(names):
        if old.startswith("Block"):
            continue
        texture = (old + str(index) if old in subtypes else old).lower()
        if old == "ItemKatana":
            texture = ["itemkatanathaumium", "itemkatanavoidmetal", "itemkatanashadowmetal"][index]
        js(RES / f"assets/taintedmagic/models/item/{name}.json", {"parent": "minecraft:item/handheld" if old.startswith("ItemShadowmetal") or old in ["ItemHollowDagger", "ItemPrimalBlade", "ItemThaumicDisassembler", "ItemKatana"] else "minecraft:item/generated", "textures": {"layer0": f"taintedmagic:items/{texture}"}})

for lang in (OLD / "src/main/resources/assets/taintedmagic/lang").glob("*.lang"):
    values = {}
    for line in lang.read_text(encoding="utf-8-sig").splitlines():
        if not line.strip() or line.lstrip().startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        values[key] = value.replace("\\n", "\n")
    for old, names in mapping.items():
        for index, name in enumerate(names):
            oldkey = ("tile." if old.startswith("Block") else "item.") + old + ("." + str(index) if old in subtypes else "") + ".name"
            newkey = ("block." if old.startswith("Block") else "item.") + "taintedmagic." + name
            if oldkey in values:
                values[newkey] = values[oldkey]
    values["itemGroup.taintedmagic"] = "Tainted Magic"
    js(RES / f"assets/taintedmagic/lang/{lang.stem.lower()}.json", values)

print("Bootstrap complete:", len(mapping), "legacy entries;", sum(len(v) for v in mapping.values()), "flattened entries")
