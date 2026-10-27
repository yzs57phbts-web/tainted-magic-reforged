package taintedmagic;

import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.effect.*;
import thaumcraft.item.gear.*;

public class Weapon extends Item implements WarpingGear, Repairable {
    public final String kind;
    public final float damage;
    public Weapon(String kind, float damage, Properties p) { super(p); this.kind = kind; this.damage = damage; }
    @Override public int getEnchantmentValue() { return kind.equals("hollow") ? 5 : kind.equals("primal") ? 30 : 0; }
    public static Properties attributes(Properties p, float damage) {
        return p.attributes(ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, damage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build());
    }
    @Override public int getWarp(ItemStack stack, Player player) { return kind.equals("primal") ? 5 : 0; }
    @Override public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (stack.isDamageableItem()) stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
        if (!target.level().isClientSide() && kind.equals("primal")) {
            target.level().playSound(null, attacker.blockPosition(), thaumcraft.registry.ModSounds.SWING.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1, 1);
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
        }
        return true;
    }
    @Override public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (kind.equals("primal") && !level.isClientSide() && entity.tickCount % 20 == 0 && stack.isDamaged()) stack.setDamageValue(stack.getDamageValue() - 1);
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (kind.equals("primal") || kind.equals("hollow")) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(player.getItemInHand(hand));
        }
        return super.use(level, player, hand);
    }
    @Override public int getUseDuration(ItemStack stack, LivingEntity entity) { return 72000; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.BLOCK; }
    @Override public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        if (!kind.equals("primal") || level.isClientSide() || !(entity instanceof Player player)) return;
        if (player.tickCount % 10 == 0) level.playSound(null, player.blockPosition(), thaumcraft.registry.ModSounds.BRAIN.get(), net.minecraft.sounds.SoundSource.PLAYERS, 0.05F, 0.5F);
        for (Entity target : level.getEntities(player, player.getBoundingBox().inflate(15))) {
            if (!Events.canHurt(player, target)) continue;
            var direction = player.position().add(0, 0.5, 0).subtract(target.position());
            double length = direction.length();
            if (length > 0.001 && length < 20) {
                double force = Math.pow(1 - length / 20, 2) * 0.2;
                target.push(direction.x / length * force, direction.y / length * force * 1.5, direction.z / length * force);
                target.hurtMarked = true;
            }
            if (target.distanceToSqr(player) < 4) target.hurt(level.damageSources().indirectMagic(player, player), 3F);
        }
    }
}
