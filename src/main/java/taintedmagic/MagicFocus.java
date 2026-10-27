package taintedmagic;

import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import thaumcraft.aspect.*;
import thaumcraft.entity.CombatUtils;
import thaumcraft.entity.monster.taint.TaintSwarm;
import thaumcraft.research.*;
import thaumcraft.wand.*;
import thaumcraft.wand.focus.*;

public final class MagicFocus extends FocusItem {
    public final String kind;
    public MagicFocus(String kind, Properties p) { super(p); this.kind = kind; }
    @Override public String getSortingHelper(ItemStack stack) { return switch (kind) { case "dark_matter" -> "ELDRITCH"; case "mage_mace" -> "MACE"; case "vis_shard" -> "SHARD"; case "taint_swarm" -> "TAINT"; default -> kind.toUpperCase(java.util.Locale.ROOT); } + super.getSortingHelper(stack); }
    @Override public ResourceLocation getFocusTexture(ItemStack stack) { return TaintedMagic.id("item/" + legacy()); }
    @Override public ResourceLocation getDepthLayerTexture(ItemStack stack) { return kind.equals("dark_matter") || kind.equals("taint_swarm") || kind.equals("shockwave") ? TaintedMagic.id("item/" + legacy() + "_depth") : null; }
    @Override public ResourceLocation getOrnamentTexture(ItemStack stack) { return kind.equals("mage_mace") ? TaintedMagic.id("item/itemfocusmagemace_orn") : kind.equals("dark_matter") ? ResourceLocation.parse("thaumcraft:item/focus_warding_orn") : kind.equals("taint_swarm") ? ResourceLocation.parse("thaumcraft:item/focus_whatever_orn") : null; }
    private String legacy() { return "itemfocus" + switch (kind) { case "dark_matter" -> "darkmatter"; case "taint_swarm" -> "taintswarm"; case "mage_mace" -> "magemace"; case "vis_shard" -> "visshard"; default -> kind; }; }
    @Override public int getFocusColor(ItemStack stack) { return switch (kind) { case "dark_matter" -> 0x000018; case "taint_swarm", "vis_shard" -> 0x9929BD; case "lumos" -> 0xC9D2F8; case "shockwave" -> 0xB0B7C4; default -> 0xACA9B5; }; }
    @Override public AspectList getVisCost(ItemStack stack) {
        AspectList cost = switch (kind) {
            case "dark_matter" -> new AspectList().add(Aspect.ENTROPY, 150).add(Aspect.FIRE, 100);
            case "taint_swarm" -> new AspectList().add(Aspect.EARTH, 50).add(Aspect.WATER, 50);
            case "mage_mace" -> new AspectList().add(Aspect.ENTROPY, 10);
            case "shockwave" -> new AspectList().add(Aspect.AIR, 150).add(Aspect.ENTROPY, 100);
            case "vis_shard" -> new AspectList().add(Aspect.FIRE, 10).add(Aspect.ENTROPY, 10).add(Aspect.AIR, 10);
            default -> new AspectList().add(Aspect.AIR, 10).add(Aspect.FIRE, 25);
        };
        if (isUpgradedWith(stack, Foci.SANITY)) cost.add(Aspect.ORDER, 50);
        else if (isUpgradedWith(stack, Foci.CORROSIVE)) cost.add(Aspect.WATER, 50);
        if (isUpgradedWith(stack, Foci.ANTIBODY)) cost.add(Aspect.ORDER, 10);
        if (isUpgradedWith(stack, Foci.PERSISTENT)) cost.add(Aspect.WATER, 10);
        if (isUpgradedWith(stack, Foci.DIFFUSION)) for (Aspect aspect : cost.getAspects()) cost.reduce(aspect, (int)(cost.getAmount(aspect) * 0.9));
        return cost;
    }
    @Override public int getActivationCooldown(ItemStack stack) { return switch (kind) { case "shockwave" -> 10000; case "taint_swarm" -> 3000; case "vis_shard" -> 300; case "mage_mace" -> -1; default -> isUpgradedWith(stack, Foci.DIFFUSION) ? -1 : 1000; }; }
    @Override public void appendCostText(ItemStack stack, java.util.List<net.minecraft.network.chat.Component> lines) {
        int start = lines.size();
        super.appendCostText(stack, lines);
        if (kind.equals("mage_mace") && lines.size() > start) lines.set(start, net.minecraft.network.chat.Component.translatable("item.Focus.cost3"));
    }
    @Override public void addFocusInformation(ItemStack stack, java.util.List<net.minecraft.network.chat.Component> lines) {
        super.addFocusInformation(stack, lines);
        if (kind.equals("mage_mace")) lines.add(net.minecraft.network.chat.Component.literal("+" + new java.text.DecimalFormat("0.#").format(Config.MACE_DAMAGE.get() + getUpgradeLevel(stack, FocusUpgradeType.POTENCY)) + " ").append(net.minecraft.network.chat.Component.translatable("text.attackdamageequipped")).withStyle(net.minecraft.ChatFormatting.BLUE));
        if (kind.equals("shockwave")) lines.add(net.minecraft.network.chat.Component.literal("+" + (15 + getUpgradeLevel(stack, FocusUpgradeType.ENLARGE)) + " ").append(net.minecraft.network.chat.Component.translatable("text.radius")).withStyle(net.minecraft.ChatFormatting.BLUE));
    }
    @Override public boolean isVisCostPerTick(ItemStack stack) { return isUpgradedWith(stack, Foci.DIFFUSION); }
    @Override public WandFocusAnimation getAnimation(ItemStack stack) { return isUpgradedWith(stack, Foci.DIFFUSION) ? WandFocusAnimation.CHARGE : WandFocusAnimation.WAVE; }
    @Override public boolean canApplyUpgrade(ItemStack stack, Player player, FocusUpgradeType upgrade, int rank) { return upgrade != Foci.DIFFUSION || ResearchManager.isComplete(player, "DIFFUSIONUPGRADE"); }
    @Override public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack stack, int rank) {
        if (rank < 1 || rank > 5) return null;
        var options = new ArrayList<FocusUpgradeType>();
        options.add(FocusUpgradeType.FRUGAL);
        if (!kind.equals("lumos")) options.add(FocusUpgradeType.POTENCY);
        if (kind.equals("dark_matter") || kind.equals("shockwave")) options.add(FocusUpgradeType.ENLARGE);
        if (kind.equals("dark_matter") && rank == 3) { options.add(Foci.SANITY); options.add(Foci.CORROSIVE); }
        if (kind.equals("dark_matter") && rank == 5) options.add(Foci.DIFFUSION);
        if (kind.equals("taint_swarm") && rank == 3) options.add(Foci.ANTIBODY);
        if (kind.equals("vis_shard") && rank == 5) options.add(Foci.PERSISTENT);
        return options.toArray(FocusUpgradeType[]::new);
    }
    @Override public InteractionResult onFocusRightClick(ItemStack wand, Level level, Player player, HitResult hit) {
        ItemStack focus = Wands.getFocusItem(wand);
        if (kind.equals("mage_mace")) return InteractionResult.PASS;
        if (isUpgradedWith(focus, Foci.DIFFUSION)) {
            CombatUtils.startUsingWand(player, wand);
            WandManager.setCooldown(player, -1);
            return InteractionResult.CONSUME;
        }
        LivingEntity target = kind.equals("vis_shard") || kind.equals("taint_swarm") ? Events.target(player, 32) : null;
        if ((kind.equals("vis_shard") || kind.equals("taint_swarm")) && target == null) return InteractionResult.PASS;
        BlockPos light = kind.equals("lumos") && hit instanceof BlockHitResult block && block.getType() == HitResult.Type.BLOCK ? block.getBlockPos().relative(block.getDirection()) : null;
        if (kind.equals("lumos") && (light == null || !player.mayUseItemAt(light, net.minecraft.core.Direction.UP, wand) || !level.getBlockState(light).canBeReplaced())) return InteractionResult.PASS;
        if (!(wand.getItem() instanceof WandItem item) || !item.consumeAllVis(wand, player, getVisCost(focus), !level.isClientSide(), false)) return InteractionResult.PASS;
        if (level instanceof ServerLevel server) {
            int potency = Wands.getFocusPotency(wand);
            switch (kind) {
                case "dark_matter" -> {
                    server.addFreshEntity(MagicProjectile.create(server, player, 0, 16 + potency, Wands.getFocusEnlarge(wand), isUpgradedWith(focus, Foci.CORROSIVE), null, false));
                    server.playSound(null, player.blockPosition(), thaumcraft.registry.ModSounds.ELDRITCH_GUARDIAN_ATTACK.get(), net.minecraft.sounds.SoundSource.PLAYERS, 0.4F, 1 + server.random.nextFloat() * 0.1F);
                    warp(player, focus, 20);
                }
                case "vis_shard" -> {
                    var shard = MagicProjectile.create(server, player, 1, 2 + potency * 0.5F, 0, false, target, isUpgradedWith(focus, Foci.PERSISTENT));
                    server.addFreshEntity(shard);
                    Effects.send(server, Effects.SHARD_CAST, 18, shard.getX(), shard.getY(), shard.getZ());
                    server.playSound(null, player.blockPosition(), Content.SHARD_SOUND.get(), net.minecraft.sounds.SoundSource.PLAYERS, 0.3F, 1.1F + server.random.nextFloat() * 0.1F);
                }
                case "taint_swarm" -> {
                    SummonedSwarm swarm = new SummonedSwarm(Content.SWARM.get(), server);
                    swarm.setPos(player.getEyePosition().add(player.getLookAngle().scale(0.5)));
                    swarm.bind(player, target, 5 + potency);
                    server.addFreshEntity(swarm);
                    if (!isUpgradedWith(focus, Foci.ANTIBODY) && server.random.nextInt(3) == 0) player.addEffect(new MobEffectInstance(thaumcraft.registry.ModEffects.TAINT_POISON, 40, 2));
                }
                case "shockwave" -> {
                    double radius = 15 + Wands.getFocusEnlarge(wand);
                    Events.shockwave(player, radius, 0, 5 + potency, 1.5 + potency * 0.1);
                    for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(7), e -> Events.canHurt(player, e) && e.distanceToSqr(player) < 49)) victim.hurt(server.damageSources().indirectMagic(player, player), 2 + potency);
                }
                case "lumos" -> {
                    server.setBlockAndUpdate(light, Content.LUMOS.get().defaultBlockState());
                    server.playSound(null, player.blockPosition(), thaumcraft.registry.ModSounds.ICE.get(), net.minecraft.sounds.SoundSource.PLAYERS, 0.3F, 1.1F + server.random.nextFloat() * 0.1F);
                    Effects.send(server, Effects.LUMOS_CAST, 9, light.getX(), light.getY(), light.getZ());
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
    @Override public void onUsingFocusTick(ItemStack wand, Player player, int count) {
        if (!(player.level() instanceof ServerLevel level) || !(wand.getItem() instanceof WandItem item)) return;
        ItemStack focus = Wands.getFocusItem(wand);
        if (!item.consumeAllVis(wand, player, getVisCost(focus), true, false)) { player.stopUsingItem(); return; }
        if (player.tickCount % 15 == 0) level.playSound(null, player.blockPosition(), thaumcraft.registry.ModSounds.WIND.get(), net.minecraft.sounds.SoundSource.PLAYERS, 0.3F + level.random.nextFloat() * 0.2F, 0.75F + level.random.nextFloat() * 0.25F);
        for (int i = 0; i < 2 + Wands.getFocusPotency(wand); i++) level.addFreshEntity(MagicProjectile.create(level, player, 2, 12 + Wands.getFocusPotency(wand), Wands.getFocusEnlarge(wand), isUpgradedWith(focus, Foci.CORROSIVE), null, false));
        warp(player, focus, 1000);
    }
    private void warp(Player player, ItemStack focus, int chance) {
        if (!isUpgradedWith(focus, Foci.SANITY) && player instanceof ServerPlayer serverPlayer && player.getRandom().nextInt(chance) == 0) Warp.add(serverPlayer, 1, WarpType.STICKY);
    }
}
