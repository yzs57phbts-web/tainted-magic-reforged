package taintedmagic;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class WarpwoodLeaves extends LeavesBlock {
    public WarpwoodLeaves(Properties properties) { super(properties); }
    @Override public MapCodec<WarpwoodLeaves> codec() { return simpleCodec(WarpwoodLeaves::new); }
    @Override protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) { return 0; }
}
