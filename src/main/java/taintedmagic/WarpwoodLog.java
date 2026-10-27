package taintedmagic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public final class WarpwoodLog extends RotatedPillarBlock {
    public static final BooleanProperty KNOT = BooleanProperty.create("knot");
    public WarpwoodLog(Properties p) { super(p); registerDefaultState(defaultBlockState().setValue(KNOT, false)); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { super.createBlockStateDefinition(builder); builder.add(KNOT); }
    @Override protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) { return super.getDestroyProgress(state, player, level, pos) * (state.getValue(KNOT) ? 0.1F : 1F); }
    @Override public void destroy(net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockState state) {
        super.destroy(level, pos, state);
        if (state.getValue(KNOT) && level instanceof net.minecraft.server.level.ServerLevel server) {
            Effects.send(server, Effects.WARPWOOD, 15, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5);
            server.playSound(null, pos, thaumcraft.registry.ModSounds.CRAFTFAIL.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1, 1);
        }
    }
}
