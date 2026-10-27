package taintedmagic;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import thaumcraft.platform.*;

public final class Content {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TaintedMagic.MODID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TaintedMagic.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TaintedMagic.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, TaintedMagic.MODID);
    public static final DeferredRegister<net.minecraft.sounds.SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, TaintedMagic.MODID);
    public static final DeferredHolder<net.minecraft.sounds.SoundEvent, net.minecraft.sounds.SoundEvent> SHOCKWAVE_SOUND = SOUNDS.register("shockwave", () -> net.minecraft.sounds.SoundEvent.createVariableRangeEvent(TaintedMagic.id("shockwave")));
    public static final DeferredHolder<net.minecraft.sounds.SoundEvent, net.minecraft.sounds.SoundEvent> SHARD_SOUND = SOUNDS.register("shard", () -> net.minecraft.sounds.SoundEvent.createVariableRangeEvent(TaintedMagic.id("shard")));
    public static final DeferredRegister<net.minecraft.world.entity.EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, TaintedMagic.MODID);
    public static final DeferredHolder<net.minecraft.world.entity.EntityType<?>, net.minecraft.world.entity.EntityType<MagicProjectile>> PROJECTILE = ENTITIES.register("magic_projectile", () -> net.minecraft.world.entity.EntityType.Builder.<MagicProjectile>of(MagicProjectile::new, net.minecraft.world.entity.MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(8).updateInterval(1).build("taintedmagic:magic_projectile"));
    public static final DeferredHolder<net.minecraft.world.entity.EntityType<?>, net.minecraft.world.entity.EntityType<SummonedSwarm>> SWARM = ENTITIES.register("summoned_swarm", () -> net.minecraft.world.entity.EntityType.Builder.<SummonedSwarm>of(SummonedSwarm::new, net.minecraft.world.entity.MobCategory.MONSTER).sized(2F, 2F).clientTrackingRange(8).build("taintedmagic:summoned_swarm"));
    public static final Map<String, DeferredItem<? extends Item>> ALL = new LinkedHashMap<>();
    public static final TcToolMaterial SHADOW = new TcToolMaterial(2500, 17F, 6F, 30, TagKey.create(Registries.ITEM, TaintedMagic.id("shadowmetal_repair")), BlockTags.INCORRECT_FOR_DIAMOND_TOOL);
    public static final TcToolMaterial HOLLOW = new TcToolMaterial(200, 4F, -0.5F, 5, TagKey.create(Registries.ITEM, TaintedMagic.id("hollow_repair")), BlockTags.INCORRECT_FOR_STONE_TOOL);
    public static final TcToolMaterial PRIMAL = new TcToolMaterial(500, 16F, 16.75F, 30, TagKey.create(Registries.ITEM, TaintedMagic.id("primal_repair")), BlockTags.INCORRECT_FOR_DIAMOND_TOOL);
    public static final DeferredBlock<WarpwoodLog> LOG = BLOCKS.register("warpwood_log", () -> new WarpwoodLog(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG).strength(2.5F, 2.5F)));
    public static final DeferredBlock<Block> PLANKS = BLOCKS.register("warpwood_planks", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(0.8F, 0.8F)));
    public static final DeferredBlock<WarpwoodLeaves> LEAVES = BLOCKS.register("warpwood_leaves", () -> new WarpwoodLeaves(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES).strength(0.1F, 0.1F)));
    public static final DeferredBlock<WarpwoodSapling> SAPLING = BLOCKS.register("warpwood_sapling", () -> new WarpwoodSapling(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING).randomTicks()));
    public static final DeferredBlock<NightshadeBush> NIGHTSHADE = BLOCKS.register("nightshade_bush", () -> new NightshadeBush(BlockBehaviour.Properties.ofFullCopy(Blocks.FERN)));
    public static final DeferredBlock<Block> LUMOS = BLOCKS.register("lumos", () -> new LumosBlock(BlockBehaviour.Properties.of().noCollission().noOcclusion().replaceable().strength(-1).lightLevel(state -> 15).noLootTable()));

    public static void init(IEventBus bus) {
        block("warpwood_log", LOG);
        block("warpwood_planks", PLANKS);
        block("warpwood_leaves", LEAVES);
        block("warpwood_sapling", SAPLING);
        block("nightshade_bush", NIGHTSHADE);
        for (String name : "shadowmetal_ingot shadow_cloth crimson_cloth warped_shard tainted_shard creation_shard thaumic_alloy_plate crimson_alloy_plate shadowmetal_nugget primordial_nodule primordial_mote creation_fragment warpwood_rod warpwood_staff_core shadowmetal_wand_cap cloth_wand_cap crimson_cloth_wand_cap shadow_cloth_wand_cap".split(" ")) {
            add(name, new Item.Properties().rarity(name.contains("creation") || name.startsWith("primordial") ? Rarity.EPIC : Rarity.UNCOMMON), TMItem::new);
        }
        for (String name : "crimson_blood void_blood warp_fertilizer salis_tempestas salis_aevum flyte_charm gate_key".split(" ")) {
            add(name, new Item.Properties().rarity(Rarity.RARE).stacksTo(name.equals("flyte_charm") || name.equals("gate_key") ? 1 : 64), p -> new UtilityItem(name, p));
        }
        add("magic_funguar", Registration.food(new Item.Properties().rarity(Rarity.UNCOMMON), 3, 0.2F, true, 1.6F), p -> new FoodItem(false, p));
        add("nightshade_berries", Registration.food(new Item.Properties().rarity(Rarity.UNCOMMON), 0, 0F, true, 1.6F), p -> new FoodItem(true, p));
        add("lumos_ring", new Item.Properties().stacksTo(1).rarity(Rarity.RARE), p -> new Trinket(false, p));
        add("voidwalker_sash", new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), p -> new Trinket(true, p));
        for (String name : "taint_swarm dark_matter mage_mace shockwave vis_shard lumos".split(" ")) {
            add("focus_" + name, thaumcraft.wand.focus.FocusItem.properties(new Item.Properties()), p -> new MagicFocus(name, p));
        }
        add("hollow_dagger", Registration.sword(new Item.Properties().rarity(Rarity.UNCOMMON), HOLLOW, 4F, -2.4F), p -> new Weapon("hollow", 3.5F, p));
        add("primal_blade", Registration.sword(new Item.Properties().rarity(Rarity.EPIC), PRIMAL, 4F, -2.4F), p -> new Weapon("primal", 20.75F, p));
        for (String type : "pick spade axe hoe sword".split(" ")) {
            Item.Properties properties = new Item.Properties().rarity(Rarity.UNCOMMON);
            properties = switch (type) {
                case "pick" -> Registration.pickaxe(properties, SHADOW, 1F, -2.8F);
                case "spade" -> Registration.shovel(properties, SHADOW, 1.5F, -3F);
                case "axe" -> Registration.axe(properties, SHADOW, 5F, -3F);
                case "hoe" -> Registration.hoe(properties, SHADOW, 0F, -2F);
                default -> Registration.sword(properties, SHADOW, 4F, -2.4F);
            };
            add("shadowmetal_" + type, properties, p -> new ShadowTool(type, p));
        }
        String[] blades = {"thaumium_fortress_blade", "void_fortress_blade", "shadow_fortress_blade"};
        for (int i = 0; i < blades.length; i++) {
            int variant = i;
            float damage = 14.25F + i * 3.25F;
            add(blades[i], Weapon.attributes(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), damage - 1F), p -> new FortressBlade(variant, damage, p));
        }
        add("thaumic_disassembler", Weapon.attributes(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 19F), Disassembler::new);
        for (String metal : new String[] {"void", "shadow"}) {
            TcArmorMaterial material = armor(metal + "_fortress", metal.equals("void") ? 300 : 3000, 4, metal.equals("void") ? 8 : 10, 6, 0);
            for (ArmorSlot slot : new ArmorSlot[] {ArmorSlot.HELMET, ArmorSlot.CHESTPLATE, ArmorSlot.LEGGINGS}) {
                String suffix = switch (slot) { case HELMET -> "helmet"; case CHESTPLATE -> "chestplate"; default -> "leggings"; };
                add(metal + "_fortress_" + suffix, Registration.armor(new Item.Properties().rarity(Rarity.EPIC), material, slot), p -> new FortressArmor(material, slot, metal.equals("shadow"), p));
            }
        }
        TcArmorMaterial warped = armor("warped_goggles", 500, 4, 0, 0, 0);
        TcArmorMaterial voidGoggles = armor("voidmetal_goggles", 25, 1, 3, 2, 1);
        TcArmorMaterial boots = armor("voidwalker", 300, 0, 0, 0, 4);
        add("warped_goggles", Registration.armor(new Item.Properties().rarity(Rarity.RARE), warped, ArmorSlot.HELMET).durability(350), p -> new MagicArmor(warped, ArmorSlot.HELMET, 1, 5, false, p));
        add("voidmetal_goggles", Registration.armor(new Item.Properties().rarity(Rarity.RARE), voidGoggles, ArmorSlot.HELMET).durability(350), p -> new MagicArmor(voidGoggles, ArmorSlot.HELMET, 5, 12, true, p));
        add("voidwalker_boots", Registration.armor(new Item.Properties().rarity(Rarity.EPIC), boots, ArmorSlot.BOOTS), p -> new VoidwalkerBoots(boots, p));
        SERIALIZERS.register("void_blood", () -> new SimpleCraftingRecipeSerializer<>(VoidBloodRecipe::new));
        TABS.register("main", () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.taintedmagic")).icon(() -> new ItemStack(item("creation_shard"))).displayItems((parameters, output) -> ALL.values().forEach(i -> output.accept(i.get()))).build());
        BLOCKS.register(bus);
        ITEMS.register(bus);
        SERIALIZERS.register(bus);
        TABS.register(bus);
        ENTITIES.register(bus);
        SOUNDS.register(bus);
        bus.addListener((net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent event) -> event.put(SWARM.get(), SummonedSwarm.attributes().build()));
    }

    private static TcArmorMaterial armor(String name, int durability, int head, int chest, int legs, int feet) {
        TcArmorMaterial material = new TcArmorMaterial(durability, head, chest, legs, feet, name.equals("warped_goggles") ? 20 : name.equals("voidmetal_goggles") ? 25 : 30, SoundEvents.ARMOR_EQUIP_NETHERITE, 0F, 0F, TagKey.create(Registries.ITEM, TaintedMagic.id("shadowmetal_repair")), TaintedMagic.id(name));
        Registration.armorMaterial(material);
        return material;
    }

    private static void block(String name, DeferredBlock<? extends Block> block) {
        ALL.put(name, ITEMS.registerSimpleBlockItem(name, block));
    }

    private static void add(String name, Item.Properties p, Function<Item.Properties, ? extends Item> factory) {
        ALL.put(name, ITEMS.registerItem(name, factory, p));
    }

    public static Item item(String name) { return ALL.get(name).get(); }
    public static boolean is(ItemStack stack, String name) { return stack.is(item(name)); }
    public static Item tc(String name) { return BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("thaumcraft", name)); }
}
