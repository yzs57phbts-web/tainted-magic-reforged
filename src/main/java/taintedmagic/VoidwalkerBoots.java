package taintedmagic;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import thaumcraft.item.armor.*;
import thaumcraft.platform.*;

public final class VoidwalkerBoots extends MagicArmor implements SpecialArmor {
    public VoidwalkerBoots(TcArmorMaterial material, Properties p) { super(material, ArmorSlot.BOOTS, 5, 5, true, p); }
    @Override public Protection protection(ItemStack stack, LivingEntity entity, DamageSource source, EquipmentSlot slot) {
        boolean special = source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC) || source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_EXPLOSION);
        double ratio = !special && source.is(DamageTypeTags.BYPASSES_ARMOR) ? 0 : ArmorSupport.defense(stack, slot) / (special ? 80.0 : 90.0);
        return new Protection(special ? 1 : 0, ratio, (stack.getMaxDamage() + 1.0 - stack.getDamageValue()) / 25.0);
    }
}
