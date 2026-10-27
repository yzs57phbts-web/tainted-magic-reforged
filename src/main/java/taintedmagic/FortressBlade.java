package taintedmagic;

import net.minecraft.world.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import thaumcraft.entity.projectile.ExplosiveOrb;

public final class FortressBlade extends Weapon {
    public final int variant;
    public FortressBlade(int variant, float damage, Properties p) { super("fortress", damage, p); this.variant = variant; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.BOW; }
    @Override public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        if (!level.isClientSide() && entity.tickCount % 5 == 0) {
            float pitch=0.75F+level.random.nextFloat()*0.25F;
            level.playSound(null, entity.blockPosition(), thaumcraft.registry.ModSounds.WIND.get(), net.minecraft.sounds.SoundSource.PLAYERS, pitch*0.1F, pitch);
        }
    }
    @Override public int getWarp(ItemStack stack, Player player) { return variant == 0 ? 0 : variant == 1 ? 3 : 7; }
    @Override public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level().isClientSide()) {
            target.level().playSound(null, attacker.blockPosition(), thaumcraft.registry.ModSounds.SWING.get(), net.minecraft.sounds.SoundSource.PLAYERS, 0.5F + attacker.getRandom().nextFloat(), 0.5F + attacker.getRandom().nextFloat());
            if (variant > 0) target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60));
            if (variant > 1) target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 120));
            switch (Data.integer(stack, "inscription", -1)) {
                case 0 -> target.igniteForSeconds(3);
                case 1 -> target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 140));
                case 2 -> target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60));
            }
        }
        return true;
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (cooldown(stack, level) > 0) return InteractionResultHolder.fail(stack);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }
    public static long cooldown(ItemStack stack, Level level) { return Math.clamp(Data.tag(stack).getLong("cooldownEnd") - level.getGameTime(), 0, 140); }
    @Override public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) {
        if (level.isClientSide() || !(entity instanceof Player player) || getUseDuration(stack, entity) - remaining < 20 || cooldown(stack, level) > 0) return;
        int inscription = Data.integer(stack, "inscription", -1);
        if (inscription < 0 || player.isShiftKeyDown()) {
            level.playSound(null, player.blockPosition(), thaumcraft.registry.ModSounds.SWING.get(), net.minecraft.sounds.SoundSource.PLAYERS, 0.5F+level.random.nextFloat(), 0.5F+level.random.nextFloat());
            LivingEntity target = Events.target(player, player.isCreative() ? 6 : 3);
            if (target != null) {
                var type = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolderOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE, TaintedMagic.id("charged_strike")));
                target.hurt(new net.minecraft.world.damagesource.DamageSource(type, player), damage * (level.random.nextInt(10) == 0 ? 2.5F : 1.5F));
            }
        } else {
            switch (inscription) {
                case 0 -> {
                    ExplosiveOrb orb = ExplosiveOrb.create(level, player);
                    orb.strength = damage * 0.25F;
                    level.addFreshEntity(orb);
                }
                case 1 -> Events.shockwave(player, 10, damage * 0.25F, 5, 1.5);
                case 2 -> {
                    player.heal((player.getMaxHealth() - player.getHealth()) * 0.5F);
                    for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(5), e -> Events.canHurt(player, e))) victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 1));
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 120, 1));
                    if (level instanceof net.minecraft.server.level.ServerLevel server) Effects.send(server, Effects.HEAL, 18, player.getX(), player.getY() + 1, player.getZ());
                    level.playSound(null, player.blockPosition(), thaumcraft.registry.ModSounds.WAND.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1, 0.9F + level.random.nextFloat() * 0.1F);
                }
            }
            Data.update(stack, tag -> tag.putLong("cooldownEnd", level.getGameTime() + 140));
        }
        player.swing(player.getUsedItemHand(), true);
    }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<net.minecraft.network.chat.Component> lines, TooltipFlag flag) {
        if (variant > 0) lines.add(net.minecraft.network.chat.Component.translatable(variant == 1 ? "enchantment.special.sapless" : "enchantment.special.sapgreat").withStyle(net.minecraft.ChatFormatting.GOLD));
        int inscription = Data.integer(stack, "inscription", -1);
        if (inscription >= 0) lines.add(net.minecraft.network.chat.Component.translatable("text.katana.inscription." + inscription).withStyle(net.minecraft.ChatFormatting.GOLD));
    }
}
