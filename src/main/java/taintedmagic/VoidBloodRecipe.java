package taintedmagic;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public final class VoidBloodRecipe extends CustomRecipe {
    public VoidBloodRecipe(CraftingBookCategory category) { super(category); }
    private static ItemStack target(CraftingInput input) {
        ItemStack armor = ItemStack.EMPTY;
        boolean blood = false;
        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) continue;
            if (Content.is(stack, "void_blood") && !blood) blood = true;
            else if (stack.getItem() instanceof ArmorItem && armor.isEmpty() && Data.integer(stack, "voidtouched", 0) == 0) armor = stack;
            else return ItemStack.EMPTY;
        }
        return blood ? armor : ItemStack.EMPTY;
    }
    @Override public boolean matches(CraftingInput input, Level level) { return !target(input).isEmpty(); }
    @Override public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack armor = target(input);
        if (armor.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = armor.copyWithCount(1);
        Data.setInt(result, "voidtouched", 1);
        return result;
    }
    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= 2; }
    @Override public RecipeSerializer<?> getSerializer() { return Content.SERIALIZERS.getEntries().iterator().next().get(); }
}
