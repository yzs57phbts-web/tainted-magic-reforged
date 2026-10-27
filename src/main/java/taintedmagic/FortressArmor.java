package taintedmagic;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.aspect.Aspect;
import thaumcraft.item.armor.FortressArmorItem;
import thaumcraft.item.gear.WarpingGear;
import thaumcraft.platform.*;
import thaumcraft.wand.VisDiscountGear;

public final class FortressArmor extends FortressArmorItem implements WarpingGear, VisDiscountGear {
    public final boolean shadow;
    public FortressArmor(TcArmorMaterial material, ArmorSlot slot, boolean shadow, Properties p) {
        super(material, slot, p);
        this.shadow = shadow;
    }
    @Override public int getWarp(ItemStack stack, Player player) { return shadow ? 5 : 3; }
    @Override public int getVisDiscount(ItemStack stack, Player player, Aspect aspect) { return 5; }
    @Override public Protection protection(ItemStack stack, net.minecraft.world.entity.LivingEntity wearer, net.minecraft.world.damagesource.DamageSource source, net.minecraft.world.entity.EquipmentSlot slot) {
        Protection original = super.protection(stack, wearer, source, slot);
        if (!shadow || !(wearer instanceof Player)) return original;
        double multiplier = 0.75;
        for (var armorSlot : new net.minecraft.world.entity.EquipmentSlot[] {net.minecraft.world.entity.EquipmentSlot.HEAD, net.minecraft.world.entity.EquipmentSlot.CHEST, net.minecraft.world.entity.EquipmentSlot.LEGS}) {
            ItemStack piece = wearer.getItemBySlot(armorSlot);
            if (piece.getItem() instanceof FortressArmorItem) {
                multiplier += 0.15;
                if (FortressArmorItem.hasMask(piece)) multiplier += 0.05;
            }
        }
        return new Protection(original.priority(), original.ratio() / thaumcraft.item.armor.ArmorEvents.fortressSet(wearer) * multiplier, original.maximum());
    }
    @Override public boolean isValidRepairItem(ItemStack stack, ItemStack ingredient) { return ingredient.is(Content.tc("thaumium_ingot")) || (shadow ? Content.is(ingredient, "shadowmetal_ingot") : ingredient.is(Content.tc("void_metal_ingot"))); }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<net.minecraft.network.chat.Component> lines, net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        thaumcraft.item.armor.ArmorSupport.visDiscountLine(lines, 5);
    }
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide() && entity.tickCount % 20 == 0 && stack.isDamaged()) stack.setDamageValue(stack.getDamageValue() - 1);
    }
}
