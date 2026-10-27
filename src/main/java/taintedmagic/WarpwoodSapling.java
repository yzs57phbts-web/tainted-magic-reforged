package taintedmagic;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class WarpwoodSapling extends BushBlock implements BonemealableBlock {
    @Override protected com.mojang.serialization.MapCodec<? extends BushBlock> codec() { return simpleCodec(WarpwoodSapling::new); }
    public WarpwoodSapling(Properties p) { super(p); }
    @Override public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) { return true; }
    @Override public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) { return random.nextFloat() < 0.45F; }
    @Override public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) { grow(level, pos, random); }
    @Override protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getMaxLocalRawBrightness(pos.above()) >= 9 && random.nextInt(7) == 0) grow(level, pos, random);
    }
    public static boolean grow(ServerLevel level, BlockPos pos, RandomSource random) {
        int height = 7 + random.nextInt(5);
        if (level.isOutsideBuildHeight(pos.above(height + 5)) || !level.getBlockState(pos.below()).is(net.minecraft.tags.BlockTags.DIRT)) return false;
        for (int y = 1; y <= height + 1; y++) {
            int radius = y >= height - 1 ? 3 : 1;
            for (int x = -radius; x <= radius; x++) for (int z = -radius; z <= radius; z++) {
                BlockState state = level.getBlockState(pos.offset(x, y, z));
                if (!state.canBeReplaced() && !state.is(net.minecraft.tags.BlockTags.LEAVES)) return false;
            }
        }
        int top = height + 3 + random.nextInt(3);
        for (int y = height - 5; y <= top; y++) {
            int centre = Math.clamp(y, height - 3, height);
            for (int x = -5; x <= 5; x++) for (int z = -5; z <= 5; z++) {
                if (x * x + (y - centre) * (y - centre) + z * z >= 10 + random.nextInt(8)) continue;
                BlockPos leaf = pos.offset(x, y, z);
                if (level.getBlockState(leaf).canBeReplaced()) level.setBlock(leaf, Content.LEAVES.get().defaultBlockState().setValue(BlockStateProperties.DISTANCE, 1), 3);
            }
        }
        int chance = height;
        boolean previousKnot = false;
        for (int y = 0; y < height; y++) {
            boolean knot = y > 0 && !previousKnot && random.nextInt(Math.max(1, chance)) == 0;
            level.setBlock(pos.above(y), Content.LOG.get().defaultBlockState().setValue(WarpwoodLog.KNOT, knot), 3);
            if (!knot) chance--;
            previousKnot = knot;
            for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.Plane.HORIZONTAL) level.setBlock(pos.above(y).relative(direction), Content.LOG.get().defaultBlockState(), 3);
        }
        level.setBlock(pos.above(height), Content.LOG.get().defaultBlockState(), 3);
        for (int x : new int[] {-1, 1}) for (int z : new int[] {-1, 1}) {
            level.setBlock(pos.offset(x, 0, z), Content.LOG.get().defaultBlockState(), 3);
            if (random.nextInt(3) != 0) level.setBlock(pos.offset(x, 1, z), Content.LOG.get().defaultBlockState(), 3);
            level.setBlock(pos.offset(x, height - 4, z), Content.LOG.get().defaultBlockState(), 3);
            if (random.nextInt(3) == 0) level.setBlock(pos.offset(x, height - 5, z), Content.LOG.get().defaultBlockState(), 3);
        }
        for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.Plane.HORIZONTAL) {
            var log = Content.LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, direction.getAxis());
            level.setBlock(pos.relative(direction, 2), log, 3);
            level.setBlock(pos.relative(direction, 2).below(), Content.LOG.get().defaultBlockState(), 3);
            level.setBlock(pos.relative(direction, 2).above(height - 4), log, 3);
        }
        for (int i = 0; i < 64; i++) {
            BlockPos bush = pos.offset(random.nextInt(8) - random.nextInt(8), random.nextInt(4) - random.nextInt(4), random.nextInt(8) - random.nextInt(8));
            if (level.isEmptyBlock(bush) && Content.NIGHTSHADE.get().defaultBlockState().canSurvive(level, bush)) level.setBlock(bush, Content.NIGHTSHADE.get().defaultBlockState(), 3);
        }
        return true;
    }
}
