package taintedmagic;

import java.util.function.Consumer;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import thaumcraft.infusion.InfusionKeys;

public final class Data {
    public static CompoundTag tag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    public static int integer(ItemStack stack, String key, int fallback) {
        String infused = InfusionKeys.TAGS.getOrDefault(stack, Map.of()).get(key);
        if (infused != null) {
            try { return Integer.parseInt(infused); } catch (NumberFormatException ignored) { }
        }
        CompoundTag tag = tag(stack);
        return tag.contains(key) ? tag.getInt(key) : fallback;
    }

    public static void update(ItemStack stack, Consumer<CompoundTag> change) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, change);
    }

    public static void setInt(ItemStack stack, String key, int value) {
        if (integer(stack, key, Integer.MIN_VALUE) != value) update(stack, tag -> tag.putInt(key, value));
    }
}
