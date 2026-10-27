package taintedmagic;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import thaumcraft.aspect.Aspect;
import thaumcraft.item.gear.*;
import thaumcraft.node.Revealer;
import thaumcraft.platform.*;
import thaumcraft.wand.VisDiscountGear;

public class MagicArmor extends ArmorItem implements Goggles, Revealer, Repairable, WarpingGear, VisDiscountGear, RunicArmor {
    private final int warp;
    private final int discount;
    public final boolean repairs;
    public MagicArmor(TcArmorMaterial material, ArmorSlot slot, int warp, int discount, boolean repairs, Properties p) {
        super(Registration.armorMaterial(material), Registration.armorType(slot), p);
        this.warp = warp;
        this.discount = discount;
        this.repairs = repairs;
    }
    @Override public boolean showNodes(ItemStack stack, LivingEntity entity) { return getType() == Type.HELMET; }
    @Override public boolean showIngamePopups(ItemStack stack, LivingEntity entity) { return getType() == Type.HELMET; }
    @Override public int getWarp(ItemStack stack, Player player) { return warp; }
    @Override public int getRunicCharge(ItemStack stack) { return 0; }
    @Override public int getVisDiscount(ItemStack stack, Player player, Aspect aspect) { return discount; }
    @Override public boolean isValidRepairItem(ItemStack stack, ItemStack ingredient) { return warp == 1 ? Content.is(ingredient, "shadowmetal_ingot") || ingredient.is(Items.GOLD_INGOT) : getType() == Type.BOOTS ? ingredient.is(Content.tc("void_metal_ingot")) : ingredient.is(Items.GOLD_INGOT); }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<net.minecraft.network.chat.Component> lines, TooltipFlag flag) { thaumcraft.item.armor.ArmorSupport.visDiscountLine(lines, discount); }
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (repairs && !level.isClientSide() && entity.tickCount % 20 == 0 && stack.isDamaged()) stack.setDamageValue(stack.getDamageValue() - 1);
    }
}
