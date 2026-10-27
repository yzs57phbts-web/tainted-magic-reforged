package taintedmagic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.item.gear.Repairable;

public final class ShadowTool extends Item implements Repairable {
    private final String kind;
    public ShadowTool(String kind, Properties p) { super(p); this.kind = kind; }
    public boolean isSword() { return kind.equals("sword"); }
    @Override public int getUseDuration(ItemStack stack, LivingEntity entity) { return 72000; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.BLOCK; }
    @Override public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level level, Player player, net.minecraft.world.InteractionHand hand) {
        if (!isSword()) return super.use(level, player, hand);
        player.startUsingItem(hand);
        return net.minecraft.world.InteractionResultHolder.consume(player.getItemInHand(hand));
    }
    @Override public int getEnchantmentValue() { return 30; }
    @Override public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!kind.equals("hoe")) stack.hurtAndBreak(kind.equals("sword") ? 1 : 2, attacker, EquipmentSlot.MAINHAND);
        return true;
    }
    @Override public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (!kind.equals("hoe") && !level.isClientSide() && state.getDestroySpeed(level, pos) != 0) stack.hurtAndBreak(kind.equals("sword") ? 2 : 1, entity, EquipmentSlot.MAINHAND);
        return true;
    }
    @Override public boolean isValidRepairItem(ItemStack stack, ItemStack ingredient) { return Content.is(ingredient, "shadowmetal_ingot"); }
    @Override public InteractionResult useOn(UseOnContext context) {
        if (kind.equals("hoe")) {
            var level = context.getLevel();
            var pos = context.getClickedPos();
            var state = level.getBlockState(pos);
            var player = context.getPlayer();
            if (player == null || !player.mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand()) || !level.isEmptyBlock(pos.above())) return InteractionResult.PASS;
            if (state.is(Blocks.FARMLAND) || state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK)) {
                if (!level.isClientSide()) {
                    var sound=Blocks.DIRT.defaultBlockState().getSoundType();
                    level.playSound(null,pos,sound.getStepSound(),net.minecraft.sounds.SoundSource.BLOCKS,(sound.getVolume()+1)/2,sound.getPitch()*0.8F);
                    level.setBlockAndUpdate(pos, state.is(Blocks.FARMLAND) ? Blocks.DIRT.defaultBlockState() : Blocks.FARMLAND.defaultBlockState());
                    context.getItemInHand().hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        return super.useOn(context);
    }
}
