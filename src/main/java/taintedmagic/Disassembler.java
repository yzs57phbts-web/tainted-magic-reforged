package taintedmagic;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.aspect.*;

public final class Disassembler extends Item {
    public static final int CAPACITY = 50000;
    public Disassembler(Properties p) { super(p); }
    public static int charge(ItemStack stack) { return Math.clamp(Data.integer(stack, "charge", 0), 0, CAPACITY); }
    public static int mode(ItemStack stack) { return Math.clamp(Data.integer(stack, "mode", 0), 0, 3); }
    public static int efficiency(ItemStack stack) { return new int[] {20, 8, 128, 0}[mode(stack)]; }
    public static void consume(ItemStack stack, int amount) { Data.setInt(stack, "charge", Math.max(0, charge(stack) - amount)); }
    @Override public float getDestroySpeed(ItemStack stack, BlockState state) { return charge(stack) > 0 ? efficiency(stack) : 1F; }
    @Override public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) { return !state.is(Blocks.BEDROCK); }
    @Override public boolean isBarVisible(ItemStack stack) { return true; }
    @Override public int getBarWidth(ItemStack stack) { return Math.round(13F * charge(stack) / CAPACITY); }
    @Override public int getBarColor(ItemStack stack) { return 0x706090; }
    @Override public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (!level.isClientSide() && state.getDestroySpeed(level, pos) != 0) consume(stack, (int)(5 * efficiency(stack) / 8F));
        return true;
    }
    @Override public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) { consume(stack, 50); return true; }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) Data.setInt(stack, "mode", (mode(stack) + 1) % 4);
            player.displayClientMessage(Component.translatable("text.disassembler.mode").append(": ").append(modeName(stack)), true);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return super.use(level, player, hand);
    }
    @Override public InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();
        var stack = context.getItemInHand();
        var level = context.getLevel();
        if (player == null || player.isShiftKeyDown() || mode(stack) == 3 || context.getClickedFace() == net.minecraft.core.Direction.DOWN) return InteractionResult.PASS;
        int radius = mode(stack) == 1 ? 0 : mode(stack) == 0 ? 1 : 2;
        boolean changed = false;
        for (BlockPos pos : BlockPos.betweenClosed(context.getClickedPos().offset(-radius, 0, -radius), context.getClickedPos().offset(radius, 0, radius))) {
            if (!player.mayUseItemAt(pos, context.getClickedFace(), stack) || !level.isEmptyBlock(pos.above()) || !player.isCreative() && charge(stack) < 25) continue;
            var state = level.getBlockState(pos);
            if (state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK)) {
                if (!level.isClientSide()) {
                    var sound=Blocks.FARMLAND.defaultBlockState().getSoundType();
                    level.playSound(null,pos,sound.getStepSound(),net.minecraft.sounds.SoundSource.BLOCKS,(sound.getVolume()+1)/2,sound.getPitch()*0.8F);
                    level.setBlockAndUpdate(pos, Blocks.FARMLAND.defaultBlockState());
                    if (!player.isCreative()) consume(stack, 25);
                }
                changed = true;
            }
        }
        return changed ? InteractionResult.sidedSuccess(level.isClientSide()) : InteractionResult.PASS;
    }
    @Override public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide() && entity instanceof Player player && player.tickCount % 20 == 0 && charge(stack) < CAPACITY) {
            int amount = Math.min(100, CAPACITY - charge(stack));
            if (thaumcraft.wand.WandManager.consumeVisFromInventory(player, new AspectList().add(Aspect.ENTROPY, amount))) Data.setInt(stack, "charge", charge(stack) + amount);
        }
    }
    private static Component modeName(ItemStack stack) { return Component.translatable("text.disassembler." + new String[] {"normal", "slow", "fast", "off"}[mode(stack)]); }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("text.disassembler.charge").append(": " + charge(stack) / 100F + "/500"));
        lines.add(Component.translatable("text.disassembler.mode").append(": ").append(modeName(stack)));
        lines.add(Component.translatable("text.disassembler.efficiency").append(": " + efficiency(stack)));
    }
}
