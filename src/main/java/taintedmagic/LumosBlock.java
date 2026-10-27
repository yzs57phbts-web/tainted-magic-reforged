package taintedmagic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

public final class LumosBlock extends Block {
    public LumosBlock(Properties p) { super(p); }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }
    @Override protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) { return box(4.8, 4.8, 4.8, 11.2, 11.2, 11.2); }
    @Override public void animateTick(BlockState state, Level level, BlockPos pos, net.minecraft.util.RandomSource random) {
        if (random.nextInt(3) == 0) Effects.client.accept(new Effects(Effects.LUMOS_IDLE,2,pos.getX()+0.5,pos.getY()+0.5,pos.getZ()+0.5),level);
    }
    @Override protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (level instanceof net.minecraft.server.level.ServerLevel server) {
            level.removeBlock(pos, false);
            Effects.send(server, Effects.LUMOS_BREAK,9,pos.getX(),pos.getY(),pos.getZ());
            level.playSound(null, pos, thaumcraft.registry.ModSounds.ICE.get(), net.minecraft.sounds.SoundSource.PLAYERS, 0.3F, 1.1F+level.random.nextFloat()*0.1F);
        }
    }
}
