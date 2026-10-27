package taintedmagic;

import net.minecraft.world.item.Item;

public class TMItem extends Item {
    public TMItem(Properties properties) { super(properties); }
    @Override public void appendHoverText(net.minecraft.world.item.ItemStack stack, TooltipContext context, java.util.List<net.minecraft.network.chat.Component> lines, net.minecraft.world.item.TooltipFlag flag) {
        if (Content.is(stack, "creation_shard")) lines.add(net.minecraft.network.chat.Component.translatable("text.shard.1").withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
    }
}
