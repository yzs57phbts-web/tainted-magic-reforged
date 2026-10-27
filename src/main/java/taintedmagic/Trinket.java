package taintedmagic;

import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import thaumcraft.platform.*;
import thaumcraft.item.gear.*;

public final class Trinket extends Item implements Bauble, RunicArmor, WarpingGear {
    public final boolean sash;
    public Trinket(boolean sash, Properties p) { super(p); this.sash = sash; }
    @Override public BaubleType getBaubleType(ItemStack stack) { return sash ? BaubleType.BELT : BaubleType.RING; }
    @Override public int getRunicCharge(ItemStack stack) { return sash ? 20 : 0; }
    @Override public int getWarp(ItemStack stack, Player player) { return sash ? 2 : 0; }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> lines, TooltipFlag flag) {
        if (sash) lines.add(Component.translatable(Data.integer(stack, "speed", 1) == 0 ? "text.sash.speed.off" : "text.sash.speed.on").withStyle(Data.integer(stack, "speed", 1) == 0 ? net.minecraft.ChatFormatting.RED : net.minecraft.ChatFormatting.GREEN));
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (sash && player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                boolean enable = Data.integer(stack, "speed", 1) == 0;
                Data.setInt(stack, "speed", enable ? 1 : 0);
                player.displayClientMessage(Component.translatable(enable ? "text.sash.speed.on" : "text.sash.speed.off"), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return super.use(level, player, hand);
    }
}
