package taintedmagic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class NightshadeBush extends BushBlock {
    @Override protected com.mojang.serialization.MapCodec<? extends BushBlock> codec() { return simpleCodec(NightshadeBush::new); }
    public NightshadeBush(Properties p) { super(p); }
    @Override protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide() && entity instanceof LivingEntity living) {
            living.hurt(level.damageSources().sweetBerryBush(), 1F);
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 140));
        }
    }
}
