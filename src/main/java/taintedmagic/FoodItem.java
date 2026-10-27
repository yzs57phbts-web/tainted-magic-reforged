package taintedmagic;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import thaumcraft.aspect.Aspect;
import thaumcraft.research.Knowledge;

public final class FoodItem extends Item {
    private final boolean poisonous;
    public FoodItem(boolean poisonous, Properties p) { super(p); this.poisonous = poisonous; }
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (entity instanceof ServerPlayer player) {
            if (poisonous) player.hurt(level.damageSources().magic(), Short.MAX_VALUE);
            else {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 1));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 2));
                var primals = Aspect.getPrimalAspects();
                Knowledge.get(player).addAspectPool(primals.get(level.random.nextInt(primals.size())), 1);
                Knowledge.sync(player);
            }
        }
        return result;
    }
}
