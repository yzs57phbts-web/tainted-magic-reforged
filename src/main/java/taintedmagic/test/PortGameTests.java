package taintedmagic.test;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import taintedmagic.*;
import thaumcraft.aspect.*;
import thaumcraft.crafting.*;
import thaumcraft.infusion.InfusionKeys;
import thaumcraft.research.*;
import thaumcraft.wand.*;

@GameTestHolder("taintedmagic")
@PrefixGameTestTemplate(false)
public final class PortGameTests {
    @GameTest(template = "test/platform")
    public static void everyFocusKeepsItsOwnRadialEntryWithTinkererInstalled(GameTestHelper helper) {
        var keys = new HashMap<String, Item>();
        for (Item item : net.minecraft.core.registries.BuiltInRegistries.ITEM) {
            if (!(item instanceof thaumcraft.wand.focus.FocusItem focus)) continue;
            String key = focus.getSortingHelper(new ItemStack(item));
            helper.assertTrue(keys.put(key, item) == null, "Focus sort key collision: " + item + " (" + key + ")");
        }
        helper.assertTrue(keys.size() >= 16, "Thaumcraft and addon focus registries were not loaded");
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void maceNeedsVisForEachSuccessfulMeleeHit(GameTestHelper helper) {
        var player = player(helper);
        var wand = Wands.create(WandRegistry.CAP_GOLD, WandRegistry.ROD_SILVERWOOD, false);
        Wands.setFocus(wand, new ItemStack(Content.item("focus_mage_mace")));
        player.setItemInHand(InteractionHand.MAIN_HAND, wand);
        var target = EntityType.ZOMBIE.create(helper.getLevel());
        helper.getLevel().addFreshEntity(target);
        float health = target.getHealth();
        target.hurt(helper.getLevel().damageSources().playerAttack(player), 4);
        helper.assertTrue(target.getHealth() == health, "An empty mace wand must not deal damage");
        Wands.storeVis(wand, Aspect.ENTROPY, 100);
        target.invulnerableTime = 0;
        target.hurt(helper.getLevel().damageSources().playerAttack(player), 4);
        helper.assertTrue(target.getHealth() < health && Wands.getVis(wand, Aspect.ENTROPY) < 100, "A melee mace hit must spend entropy vis");
        target.discard();
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void gogglesSupportRunicInfusionAndOriginalRepairs(GameTestHelper helper) {
        var player = player(helper);
        var goggles = new ItemStack(Content.item("warped_goggles"));
        InfusionKeys.HARDEN.set(goggles, 2);
        player.setItemSlot(EquipmentSlot.HEAD, goggles);
        helper.assertTrue(thaumcraft.infusion.InfusionHooks.getFinalCharge(goggles) == 2, "Warped goggles must retain ItemGoggles runic infusion support");
        helper.assertTrue(goggles.getEnchantmentValue() == 20 && new ItemStack(Content.item("voidmetal_goggles")).getEnchantmentValue() == 25, "Goggles enchantability differs from original materials");
        helper.assertTrue(goggles.getItem().isValidRepairItem(goggles, new ItemStack(Items.GOLD_INGOT)) && goggles.getItem().isValidRepairItem(goggles, new ItemStack(Content.item("shadowmetal_ingot"))), "Warped goggles must accept both inherited gold repair and shadowmetal");
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void bootsBoostForwardMovementAndRestoreStepWhenRemoved(GameTestHelper helper) {
        var player = player(helper);
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(Content.item("voidwalker_boots")));
        player.setOnGround(true);
        player.setDeltaMovement(Vec3.ZERO);
        thaumcraft.item.armor.ArmorEvents.setMovingForward(player, false);
        Events.tick(new PlayerTickEvent.Post(player));
        helper.assertTrue(player.getDeltaMovement().horizontalDistanceSqr() == 0, "Boots must not apply the forward impulse while standing still");
        thaumcraft.item.armor.ArmorEvents.setMovingForward(player, true);
        Events.tick(new PlayerTickEvent.Post(player));
        helper.assertTrue(player.getDeltaMovement().horizontalDistanceSqr() > 0.01 && player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT) > 0.9, "Boots must supply the original forward impulse and full-block step");
        player.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
        Events.tick(new PlayerTickEvent.Post(player));
        helper.assertTrue(player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT) < 0.9, "Removing boots must restore normal step height");
        helper.succeed();
    }
    @GameTest(template = "test/platform")
    public static void radialCanSelectEveryAddonFocus(GameTestHelper helper) {
        var player = player(helper);
        ItemStack wand = Wands.create(WandRegistry.CAP_GOLD, WandRegistry.ROD_SILVERWOOD, false);
        player.setItemInHand(InteractionHand.MAIN_HAND, wand);
        String[] kinds = {"taint_swarm", "dark_matter", "mage_mace", "shockwave", "vis_shard", "lumos"};
        Set<String> keys = new HashSet<>();
        for (int i = 0; i < kinds.length; i++) {
            ItemStack focus = new ItemStack(Content.item("focus_" + kinds[i]));
            player.getInventory().setItem(i + 1, focus);
            helper.assertTrue(keys.add(((MagicFocus)focus.getItem()).getSortingHelper(focus)), "Focus menu collision: " + kinds[i]);
        }
        for (String kind : kinds) {
            ItemStack focus = new ItemStack(Content.item("focus_" + kind));
            WandManager.changeFocus(wand, helper.getLevel(), player, ((MagicFocus)focus.getItem()).getSortingHelper(focus));
            helper.assertTrue(Wands.getFocus(wand) == focus.getItem(), "Cannot equip " + kind + " through server selection");
            int total = 1;
            for (ItemStack stack : player.getInventory().items) if (stack.getItem() instanceof MagicFocus) total++;
            helper.assertTrue(total == 6, "Focus was lost or duplicated while selecting " + kind);
        }
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void staffAndMaceUseVanillaMeleeCooldowns(GameTestHelper helper) {
        var player = player(helper);
        ItemStack staff = Wands.create(WandRegistry.CAP_GOLD, WandRegistry.rod("silverwood_staff"), false);
        player.setItemInHand(InteractionHand.MAIN_HAND, staff);
        applyModifiers(player, staff.getAttributeModifiers(), true);
        helper.assertTrue(Math.abs(player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED) - 1) < 0.001, "Staff must attack once per second, like a diamond axe");
        helper.assertTrue(Math.abs(player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) - 9) < 0.001, "Staff must deal 9 base damage instead of 7");
        helper.assertTrue(Math.abs(player.getCurrentItemAttackStrengthDelay() - 20) < 0.001, "Staff recovery must last 20 ticks");
        applyModifiers(player, staff.getAttributeModifiers(), false);
        ItemStack mace = new ItemStack(Content.item("focus_mage_mace"));
        Wands.setFocus(staff, mace);
        var modifiers = staff.getAttributeModifiers();
        applyModifiers(player, modifiers, true);
        helper.assertTrue(Math.abs(player.getCurrentItemAttackStrengthDelay() - 20) < 0.001, "Mace must not remove staff cooldown");
        helper.assertTrue(Math.abs(player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) - 16) < 0.001, "Staff mace must retain its bonus and the +2 axe adjustment");
        applyModifiers(player, modifiers, false);
        ItemStack wand = Wands.create(WandRegistry.CAP_GOLD, WandRegistry.ROD_SILVERWOOD, false);
        Wands.setFocus(wand, mace);
        player.setItemInHand(InteractionHand.MAIN_HAND, wand);
        modifiers = wand.getAttributeModifiers();
        applyModifiers(player, modifiers, true);
        helper.assertTrue(Math.abs(player.getCurrentItemAttackStrengthDelay() - 12.5) < 0.001, "Wand mace must recover like a sword (1.6 attacks/sec)");
        helper.assertTrue(Math.abs(player.getAttackStrengthScale(0)) < 0.001, "Newly equipped mace must not begin with a charged attack");
        applyModifiers(player, modifiers, false);
        Wands.setFocus(wand, ItemStack.EMPTY);
        helper.assertTrue(wand.getAttributeModifiers().modifiers().isEmpty(), "Removing mace must remove its melee modifiers");
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void ordinaryWandHasNoArcaneCost(GameTestHelper helper) {
        var player = player(helper);
        List<ItemStack> grid = new ArrayList<>(Collections.nCopies(9, ItemStack.EMPTY));
        grid.set(2, new ItemStack(Content.tc("iron_wand_cap")));
        grid.set(6, new ItemStack(Content.tc("iron_wand_cap")));
        grid.set(4, new ItemStack(Items.STICK));
        helper.assertTrue(!grid.get(2).isEmpty(), "Iron cap registry ID changed");
        CraftingInput input = CraftingInput.of(3, 3, grid);
        helper.assertTrue(helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel()).isPresent(), "Starter wand must remain an ordinary crafting recipe");
        helper.assertTrue(ArcaneCrafting.findAspects(grid, player).size() == 0, "Ordinary wand must not display a fake cost of 1 of each primal");
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void shadowFortressRetainsItsStrongerSetProtection(GameTestHelper helper) {
        var player = player(helper);
        var chest = new ItemStack(Content.item("shadow_fortress_chestplate"));
        player.setItemSlot(EquipmentSlot.CHEST, chest);
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Content.item("shadow_fortress_helmet")));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Content.item("shadow_fortress_leggings")));
        var armor = (FortressArmor)chest.getItem();
        double ratio = armor.protection(chest, player, helper.getLevel().damageSources().mobAttack(EntityType.ZOMBIE.create(helper.getLevel())), EquipmentSlot.CHEST).ratio();
        helper.assertTrue(Math.abs(ratio - 10D / 25D * 1.2) < 0.00001, "Shadow set must use its original 0.75 + 0.15 per piece multiplier");
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void runicChargeStillAbsorbsDamage(GameTestHelper helper) {
        var player = player(helper);
        var chest = new ItemStack(Content.item("shadow_fortress_chestplate"));
        InfusionKeys.HARDEN.set(chest, 20);
        player.setItemSlot(EquipmentSlot.CHEST, chest);
        var wand = Wands.create(WandRegistry.CAP_GOLD, WandRegistry.ROD_SILVERWOOD, false);
        Wands.fill(wand);
        player.setItemInHand(InteractionHand.MAIN_HAND, wand);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
        helper.assertTrue(thaumcraft.item.gear.RunicShield.getCharge(player) > 0, "Addon armor must charge its runic shield");
        float health = player.getHealth();
        player.hurt(helper.getLevel().damageSources().genericKill(), 1);
        helper.assertTrue(player.getHealth() == health && thaumcraft.item.gear.RunicShield.getCharge(player) == 0, "Runic shield must absorb damage and report depleted charge");
        helper.succeed();
    }
    private static void applyModifiers(ServerPlayer player, net.minecraft.world.item.component.ItemAttributeModifiers modifiers, boolean add) {
        modifiers.forEach(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            var instance = player.getAttribute(attribute);
            if (instance != null) { instance.removeModifier(modifier.id()); if (add) instance.addTransientModifier(modifier); }
        });
    }

    private static ServerPlayer player(GameTestHelper helper) {
        ServerPlayer player = thaumcraft.test.TestSupport.mockServerPlayer(helper);
        player.setGameMode(GameType.SURVIVAL);
        player.setPos(helper.absolutePos(new BlockPos(3, 2, 3)).getCenter());
        return player;
    }

    @GameTest(template = "test/platform")
    public static void itemsAndResearchAreComplete(GameTestHelper helper) {
        helper.assertTrue(Content.ALL.size() == 60, "Expected all 60 item forms, got " + Content.ALL.size());
        var category = ResearchCategories.get("TAINTEDMAGIC");
        helper.assertTrue(category != null && category.research().size() == 49, "All 48 research entries and the Eldritch link must register");
        for (ResearchItem item : category.research()) {
            for (String parent : item.parents()) helper.assertTrue(ResearchCategories.getResearch(parent) != null, item.key() + " missing parent " + parent);
            for (String parent : item.parentsHidden()) helper.assertTrue(ResearchCategories.getResearch(parent) != null, item.key() + " missing hidden parent " + parent);
            if (item.iconItem() != null) helper.assertTrue(item.iconItem().exists(), "Missing icon for " + item.key());
        }
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void allRecipesResolve(GameTestHelper helper) {
        int count = 0;
        for (TcRecipe recipe : TcRecipes.all()) {
            if (!recipe.key().startsWith("TM:")) continue;
            count++;
            helper.assertTrue(ResearchCategories.getResearch(recipe.research()) != null, recipe.key() + " missing research " + recipe.research());
            if (recipe instanceof InfusionRecipe infusion) {
                helper.assertTrue(infusion.result().exists(), "Missing result " + recipe.key());
                helper.assertTrue(infusion.central().exists(), "Missing central " + recipe.key());
                for (TcIngredient part : infusion.components()) helper.assertTrue(part.exists(), "Missing infusion ingredient " + recipe.key() + " " + part);
            } else if (recipe instanceof ArcaneRecipe arcane) {
                helper.assertTrue(arcane.result().exists(), "Missing result " + recipe.key());
                List<TcIngredient> ingredients = arcane instanceof ShapedArcaneRecipe shaped ? shaped.grid() : ((ShapelessArcaneRecipe)arcane).ingredients();
                for (TcIngredient part : ingredients) helper.assertTrue(part.exists(), "Missing arcane ingredient " + recipe.key() + " " + part);
            } else if (recipe instanceof CrucibleRecipe crucible) {
                helper.assertTrue(crucible.result().exists() && crucible.catalyst().exists(), "Missing crucible ingredient/result " + recipe.key());
            }
        }
        helper.assertTrue(count >= 79, "Missing converted recipes: " + count);
        helper.assertTrue(helper.getLevel().getRecipeManager().byKey(TaintedMagic.id("void_blood")).isPresent(), "Void blood crafting recipe missing");
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void voidBloodPreservesAndRepairsArmor(GameTestHelper helper) {
        ItemStack armor = new ItemStack(Items.DIAMOND_CHESTPLATE);
        armor.setDamageValue(20);
        armor.set(DataComponents.CUSTOM_NAME, Component.literal("Named armor"));
        var input = CraftingInput.of(2, 1, List.of(armor, new ItemStack(Content.item("void_blood"))));
        var recipe = new VoidBloodRecipe(CraftingBookCategory.MISC);
        helper.assertTrue(recipe.matches(input, helper.getLevel()), "Void blood must accept vanilla armor");
        ItemStack result = recipe.assemble(input, helper.getLevel().registryAccess());
        helper.assertTrue(result.getDamageValue() == 20 && result.getHoverName().getString().equals("Named armor") && Data.integer(result, "voidtouched", 0) == 1, "Blood recipe must preserve armor components");
        ServerPlayer player = player(helper);
        player.getInventory().items.set(0, result);
        player.tickCount = 20;
        Events.tick(new PlayerTickEvent.Post(player));
        helper.assertTrue(result.getDamageValue() == 19, "Voidtouched armor must repair once each second");
        helper.assertTrue(armor.getDamageValue() == 20 && Data.integer(armor, "voidtouched", 0) == 0, "Input armor mutated");
        helper.assertTrue(new ItemStack(Content.item("void_blood")).getCraftingRemainingItem().is(Content.tc("glass_phial")), "Blood recipes must return an empty phial");
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void disassemblerConsumesAndRechargesVis(GameTestHelper helper) {
        var tool = new ItemStack(Content.item("thaumic_disassembler"));
        ServerPlayer player = player(helper);
        var wand = Wands.create(WandRegistry.CAP_GOLD, WandRegistry.ROD_SILVERWOOD, false);
        Wands.storeVis(wand, Aspect.ENTROPY, 500);
        player.getInventory().items.set(0, wand);
        player.tickCount = 20;
        ((Disassembler)tool.getItem()).inventoryTick(tool, helper.getLevel(), player, 1, false);
        helper.assertTrue(Disassembler.charge(tool) == 100 && Wands.getVis(wand, Aspect.ENTROPY) == 400, "Recharge must transfer 100 real vis units");
        Disassembler.consume(tool, 1000);
        helper.assertTrue(Disassembler.charge(tool) == 0 && tool.getItem().getDestroySpeed(tool, Blocks.STONE.defaultBlockState()) == 1F, "Empty tool must not keep powered mining speed or negative charge");
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void warpwoodWandUsesPermanentWarp(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        var wand = Wands.create(WandRegistry.cap("shadowmetal"), WandRegistry.rod("warpwood"), false);
        Knowledge.get(player).setWarp(WarpType.PERMANENT, 100);
        player.tickCount = 100;
        Wands.getRod(wand).onUpdate().onUpdate(wand, player);
        for (Aspect aspect : Aspect.getPrimalAspects()) helper.assertTrue(Wands.getVis(wand, aspect) == 100, "Warpwood must regenerate all primal aspects");
        helper.assertTrue(Wands.getMaxVis(wand) == 25000 && WandRegistry.rod("warpwood_staff").capacity() == 500, "Warpwood capacities changed");
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void inscriptionsPreserveBladeComponents(GameTestHelper helper) {
        ItemStack blade = new ItemStack(Content.item("shadow_fortress_blade"));
        blade.set(DataComponents.CUSTOM_NAME, Component.literal("Custom blade"));
        ItemStack result = InfusionCrafting.result(helper.getLevel(), InfusionCrafting.TYPE_RECIPE, "TM:ItemKatana:inscription2_2", blade);
        helper.assertTrue(Content.is(result, "shadow_fortress_blade") && result.getHoverName().getString().equals("Custom blade"), "Inscription must preserve material and components");
        helper.assertTrue(Data.integer(result, "inscription", -1) == 2, "Inscription must be usable on the final blade tier");
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void chargedBladeCannotHitThroughWalls(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.setYRot(0); player.setXRot(0);
        ItemStack blade = new ItemStack(Content.item("thaumium_fortress_blade"));
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        target.setPos(player.position().add(0, 0, 2.5));
        target.setNoAi(true);
        helper.getLevel().addFreshEntity(target);
        BlockPos wall = BlockPos.containing(player.getEyePosition()).offset(0, 0, 1);
        helper.getLevel().setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
        ((FortressBlade)blade.getItem()).releaseUsing(blade, helper.getLevel(), player, 71980);
        helper.assertTrue(target.getHealth() == target.getMaxHealth(), "Charged strike must respect walls");
        helper.getLevel().removeBlock(wall, false);
        ((FortressBlade)blade.getItem()).releaseUsing(blade, helper.getLevel(), player, 71980);
        helper.assertTrue(target.getHealth() < target.getMaxHealth(), "Charged strike must hit a visible target");
        target.discard();
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void nightVisionDoesNotRemovePotionEffects(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.NIGHT_VISION, 500, 0));
        Events.tick(new PlayerTickEvent.Post(player));
        helper.assertTrue(player.getEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION) != null, "Lumos must not remove normal potion night vision");
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void warpwoodTreeProducesKnotsAndNightshade(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(3, 2, 3));
        for (BlockPos point : BlockPos.betweenClosed(pos.offset(-8, -1, -8), pos.offset(8, 17, 8))) level.setBlock(point, point.getY() == pos.getY() - 1 ? Blocks.DIRT.defaultBlockState() : Blocks.AIR.defaultBlockState(), 2);
        helper.assertTrue(WarpwoodSapling.grow(level, pos, net.minecraft.util.RandomSource.create(12345)), "Warpwood must grow on dirt");
        int logs = 0, knots = 0, bushes = 0;
        for (BlockPos point : BlockPos.betweenClosed(pos.offset(-8, 0, -8), pos.offset(8, 17, 8))) {
            var state = level.getBlockState(point);
            if (state.is(Content.LOG.get())) { logs++; if (state.getValue(WarpwoodLog.KNOT)) knots++; }
            if (state.is(Content.NIGHTSHADE.get())) bushes++;
        }
        helper.assertTrue(logs > 40 && knots > 0 && bushes > 0, "Warpwood must retain its large trunk, void-seed knots and nightshade: " + logs + "/" + knots + "/" + bushes);
        helper.succeed();
    }

    @GameTest(template = "test/platform", timeoutTicks = 120)
    public static void summonedSwarmAttacksChosenTarget(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        helper.getLevel().addNewPlayer(player);
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        target.setPos(player.position().add(0, 0, 2));
        target.setNoAi(true);
        helper.getLevel().addFreshEntity(target);
        SummonedSwarm swarm = new SummonedSwarm(Content.SWARM.get(), helper.getLevel());
        swarm.setPos(player.position().add(0, 0.5, 1));
        swarm.bind(player, target, 7);
        helper.getLevel().addFreshEntity(swarm);
        helper.runAfterDelay(5, () -> {
            helper.assertTrue(target.getHealth() < target.getMaxHealth(), "Summoned swarm must damage the selected target");
            helper.assertTrue(player.getHealth() == player.getMaxHealth(), "Summoned swarm must not attack its summoner");
            target.discard(); swarm.discard();
            helper.succeed();
        });
    }

    @GameTest(template = "test/platform", timeoutTicks = 150)
    public static void homingFocusConsumesVisAndHits(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        helper.getLevel().addNewPlayer(player);
        player.setYRot(0); player.setXRot(0);
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        target.setPos(player.position().add(0, 0, 4)); target.setNoAi(true);
        helper.getLevel().addFreshEntity(target);
        ItemStack wand = Wands.create(WandRegistry.CAP_GOLD, WandRegistry.ROD_SILVERWOOD, false);
        Wands.fill(wand);
        ItemStack focus = new ItemStack(Content.item("focus_vis_shard"));
        Wands.setFocus(wand, focus);
        player.setItemInHand(InteractionHand.MAIN_HAND, wand);
        int before = Wands.getVis(wand, Aspect.AIR);
        InteractionResult result = ((MagicFocus)focus.getItem()).onFocusRightClick(wand, helper.getLevel(), player, null);
        helper.assertTrue(result == InteractionResult.SUCCESS && Wands.getVis(wand, Aspect.AIR) < before, "Homing focus must find target and consume vis");
        helper.succeedWhen(() -> {
            helper.assertTrue(target.getHealth() < target.getMaxHealth(), "Homing shard must reach and damage target");
        });
    }

    @GameTest(template = "test/platform")
    public static void focusUpgradesAndDiffusionPersist(GameTestHelper helper) {
        ItemStack focus = new ItemStack(Content.item("focus_dark_matter"));
        MagicFocus item = (MagicFocus)focus.getItem();
        item.applyUpgrade(focus, Foci.CORROSIVE, 3);
        item.applyUpgrade(focus, Foci.DIFFUSION, 5);
        helper.assertTrue(item.getVisCost(focus).getAmount(Aspect.ENTROPY) == 15 && item.getVisCost(focus).getAmount(Aspect.WATER) == 5 && item.isVisCostPerTick(focus), "Diffusion must use reduced per-tick cost and keep corrosive upgrade");
        var player = player(helper);
        var projectile = MagicProjectile.create(helper.getLevel(), player, 2, 13.5F, 1, true, null, false);
        var tag = new net.minecraft.nbt.CompoundTag();
        projectile.saveWithoutId(tag);
        var loaded = new MagicProjectile(Content.PROJECTILE.get(), helper.getLevel());
        loaded.load(tag);
        var saved = new net.minecraft.nbt.CompoundTag();
        loaded.saveWithoutId(saved);
        helper.assertTrue(loaded.mode() == 2 && saved.getBoolean("corrosive") && saved.getFloat("damage") == 13.5F, "Reload must preserve fractional damage and corrosive diffusion");
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void flyteCharmDrainsVisAndReleasesFlight(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        var wand = Wands.create(WandRegistry.CAP_GOLD, WandRegistry.ROD_SILVERWOOD, false);
        Wands.storeVis(wand, Aspect.AIR, 30);
        player.getInventory().items.set(0, wand);
        player.getInventory().items.set(1, new ItemStack(Content.item("flyte_charm")));
        Events.tick(new PlayerTickEvent.Post(player));
        helper.assertTrue(player.getAbilities().mayfly, "Flyte charm must grant flight with vis available");
        player.getAbilities().flying = true;
        Events.tick(new PlayerTickEvent.Post(player));
        helper.assertTrue(Wands.getVis(wand, Aspect.AIR) == 15, "Flying must consume 15 real air vis each tick");
        player.getInventory().items.set(1, ItemStack.EMPTY);
        Events.tick(new PlayerTickEvent.Post(player));
        helper.assertTrue(!player.getAbilities().mayfly && !player.getAbilities().flying, "Removing charm must release flight granted by it");
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void daggerFillsOnlyOnePhialAfterDamage(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Content.item("hollow_dagger")));
        player.getInventory().items.set(1, new ItemStack(Content.tc("glass_phial"), 3));
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        helper.getLevel().addFreshEntity(target);
        target.hurt(helper.getLevel().damageSources().playerAttack(player), 2);
        helper.assertTrue(player.getInventory().getItem(1).getCount() == 2 && player.getInventory().countItem(Content.item("crimson_blood")) == 1, "Melee dagger hit must fill exactly one phial");
        target.hurt(helper.getLevel().damageSources().playerAttack(player), 1);
        helper.assertTrue(player.getInventory().countItem(Content.item("crimson_blood")) == 1, "Rejected damage must not fill another phial");
        target.discard();
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void gateKeyBindsAndReturnsWithinDimension(GameTestHelper helper) {
        var player = player(helper);
        ItemStack key = new ItemStack(Content.item("gate_key"));
        player.setItemInHand(InteractionHand.MAIN_HAND, key);
        BlockPos destination = player.blockPosition();
        helper.getLevel().setBlockAndUpdate(destination.below(), Blocks.STONE.defaultBlockState());
        helper.getLevel().removeBlock(destination, false);
        helper.getLevel().removeBlock(destination.above(), false);
        var context = new net.minecraft.world.item.context.UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(destination.below()), Direction.UP, destination.below(), false));
        key.getItem().useOn(context);
        helper.assertTrue(Data.tag(key).getBoolean("bound"), "Gate key must bind on a block");
        player.setPos(player.position().add(5, 0, 0));
        key.getItem().finishUsingItem(key, helper.getLevel(), player);
        helper.assertTrue(player.blockPosition().equals(destination), "Gate key must return player to bound position");
        Data.update(key, tag -> tag.putString("dimension", "minecraft:the_end"));
        player.setPos(player.position().add(5, 0, 0));
        Vec3 before = player.position();
        key.getItem().finishUsingItem(key, helper.getLevel(), player);
        helper.assertTrue(player.position().equals(before), "Gate key must not teleport to a different dimension");
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void lumosFocusPlacesLightAndConsumesVis(GameTestHelper helper) {
        var player = player(helper);
        ItemStack wand = Wands.create(WandRegistry.CAP_GOLD, WandRegistry.ROD_SILVERWOOD, false);
        Wands.fill(wand);
        ItemStack focus = new ItemStack(Content.item("focus_lumos"));
        Wands.setFocus(wand, focus);
        player.setItemInHand(InteractionHand.MAIN_HAND, wand);
        BlockPos floor = player.blockPosition().below();
        helper.getLevel().setBlockAndUpdate(floor, Blocks.STONE.defaultBlockState());
        helper.getLevel().removeBlock(floor.above(), false);
        int before = Wands.getVis(wand, Aspect.FIRE);
        ((MagicFocus)focus.getItem()).onFocusRightClick(wand, helper.getLevel(), player, new BlockHitResult(Vec3.atCenterOf(floor), Direction.UP, floor, false));
        helper.assertTrue(helper.getLevel().getBlockState(floor.above()).is(Content.LUMOS.get()), "Lumos must place persistent light");
        helper.assertTrue(Wands.getVis(wand, Aspect.FIRE) < before, "Lumos must consume vis");
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void equipmentSupportsEnchanting(GameTestHelper helper) {
        var enchantments = helper.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        var sharpness = enchantments.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SHARPNESS).value();
        var efficiency = enchantments.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.EFFICIENCY).value();
        var protection = enchantments.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.PROTECTION).value();
        var repair = enchantments.getOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENCHANTMENT, net.minecraft.resources.ResourceLocation.parse("thaumcraft:repair"))).value();
        ItemStack sword = new ItemStack(Content.item("shadowmetal_sword"));
        ItemStack pick = new ItemStack(Content.item("shadowmetal_pick"));
        helper.assertTrue(sharpness.canEnchant(sword) && efficiency.canEnchant(pick), "Shadowmetal tools must support their vanilla enchantments");
        helper.assertTrue(sword.getEnchantmentValue() == 30 && pick.getEnchantmentValue() == 30, "Shadowmetal enchantability must retain original value 30");
        helper.assertTrue(protection.canEnchant(new ItemStack(Content.item("void_fortress_chestplate"))), "Fortress armor must support protection");
        helper.assertTrue(repair.canEnchant(sword), "Shadowmetal must support Thaumcraft Repair infusion");
        helper.succeed();
    }
}
