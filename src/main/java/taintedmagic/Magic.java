package taintedmagic;

import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.aspect.*;
import thaumcraft.platform.Baubles;
import thaumcraft.research.*;
import thaumcraft.wand.*;

public final class Magic {
    public static AspectList primals(int amount) {
        AspectList list = new AspectList();
        for (Aspect aspect : Aspect.getPrimalAspects()) list.add(aspect, amount);
        return list;
    }

    public static boolean consume(Player player, AspectList cost, boolean execute) {
        if (player.isCreative()) return true;
        java.util.ArrayList<ItemStack> stacks = new java.util.ArrayList<>(Baubles.stacks(player));
        stacks.addAll(player.getInventory().items);
        for (ItemStack stack : stacks) {
            if (!VisStorage.isStorage(stack)) continue;
            if (stack.getItem() instanceof WandItem wand) {
                if (wand.consumeAllVis(stack, player, cost, execute, true)) return true;
                continue;
            }
            boolean enough = true;
            for (Aspect aspect : cost.getAspects()) if (VisStorage.getVis(stack, aspect) < cost.getAmount(aspect)) enough = false;
            if (enough) {
                if (execute) for (Aspect aspect : cost.getAspects()) VisStorage.storeVis(stack, aspect, VisStorage.getVis(stack, aspect) - cost.getAmount(aspect));
                return true;
            }
        }
        return false;
    }

    public static void init() {
        var texture = TaintedMagic.id("textures/models/modelwand_rod_warpwood.png");
        RodUpdate update = (wand, player) -> {
            if (player.level().isClientSide() || player.hasEffect(thaumcraft.registry.ModEffects.WARP_WARD)) return;
            int warp = Warp.get(player, WarpType.PERMANENT);
            if (warp <= 0) return;
            int period = Math.max(1, Math.round(Config.WARP_REFRESH.get() / (float) warp));
            if (player.tickCount % period == 0) for (Aspect aspect : Aspect.getPrimalAspects()) Wands.addVis(wand, aspect, 1, true);
        };
        WandRegistry.registerRod("warpwood", 250, TaintedMagic.id("warpwood_rod"), 16, update, true, false, false, texture);
        WandRegistry.registerRod("warpwood", 500, TaintedMagic.id("warpwood_staff_core"), 20, update, true, true, true, texture);
        String[] tags = {"shadowmetal", "cloth", "crimsoncloth", "shadowcloth"};
        String[] names = {"shadowmetal", "cloth", "crimson_cloth", "shadow_cloth"};
        float[] discount = {0.65F, 0.97F, 0.93F, 0.93F};
        int[] craft = {12, 2, 3, 4};
        for (int i = 0; i < tags.length; i++) WandRegistry.registerCap(tags[i], discount[i], List.of(), 0F, TaintedMagic.id(names[i] + "_wand_cap"), craft[i], null, TaintedMagic.id("textures/models/modelwand_cap_" + names[i] + ".png"));
        AspectSync.addRegistrar(registry -> {
            registry.register(Content.item("warpwood_log"), registry.getObjectTags(Content.tc("greatwood_log")));
            registry.register(Content.item("warpwood_sapling"), registry.getObjectTags(Content.tc("greatwood_sapling")));
            registry.register(Content.item("warpwood_leaves"), registry.getObjectTags(Content.tc("greatwood_leaves")));
        });
    }
}
