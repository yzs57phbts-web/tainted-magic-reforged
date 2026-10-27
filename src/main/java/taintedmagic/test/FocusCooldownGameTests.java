package taintedmagic.test;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.*;
import taintedmagic.*;
import thaumcraft.aspect.Aspect;
import thaumcraft.wand.*;

@GameTestHolder("taintedmagic")
@PrefixGameTestTemplate(false)
public final class FocusCooldownGameTests {
    private static ServerPlayer player(GameTestHelper helper) {
        var player = thaumcraft.test.TestSupport.mockServerPlayer(helper);
        player.setGameMode(GameType.SURVIVAL);
        player.setPos(helper.absolutePos(new BlockPos(3, 2, 3)).getCenter());
        player.setYRot(0);
        player.setXRot(0);
        return player;
    }

    private static ItemStack wand(String kind) {
        var wand = Wands.create(WandRegistry.CAP_GOLD, WandRegistry.ROD_SILVERWOOD, false);
        Wands.fill(wand);
        Wands.setFocus(wand, new ItemStack(Content.item("focus_" + kind)));
        return wand;
    }

    private static void swap(ServerPlayer player, ItemStack wand, String kind) {
        ItemStack focus = new ItemStack(Content.item("focus_" + kind));
        WandManager.changeFocus(wand, player.level(), player, ((MagicFocus)focus.getItem()).getSortingHelper(focus));
    }

    @GameTest(template = "test/platform")
    public static void shockwaveCooldownDoesNotBlockShardAfterRadialSwap(GameTestHelper helper) {
        var player = player(helper);
        var wand = wand("shockwave");
        player.setItemInHand(InteractionHand.MAIN_HAND, wand);
        player.getInventory().setItem(1, new ItemStack(Content.item("focus_vis_shard")));
        wand.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(WandManager.isOnCooldown(player, wand) && WandManager.getCooldown(player, wand) > 9, "Shockwave must start its ten-second cooldown");
        swap(player, wand, "vis_shard");
        helper.assertTrue(!WandManager.isOnCooldown(player, wand), "Shockwave cooldown leaked to the shard after focus selection");
        var target = EntityType.ZOMBIE.create(helper.getLevel());
        target.setPos(player.position().add(0, 0, 4));
        target.setNoAi(true);
        helper.getLevel().addFreshEntity(target);
        int before = Wands.getVis(wand, Aspect.FIRE);
        wand.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(Wands.getVis(wand, Aspect.FIRE) < before, "Shard could not cast while shockwave was recharging");
        helper.assertTrue(WandManager.getCooldown(player, wand) > 0 && WandManager.getCooldown(player, wand) <= 0.3F, "Shard must have only its own short cooldown");
        swap(player, wand, "shockwave");
        helper.assertTrue(WandManager.getCooldown(player, wand) > 8, "Returning to shockwave must retain its original timer");
        int air = Wands.getVis(wand, Aspect.AIR);
        wand.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(Wands.getVis(wand, Aspect.AIR) == air, "Swapping must not bypass shockwave recharge");
        target.discard();
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void duplicateWandsCannotBypassFocusCooldown(GameTestHelper helper) {
        var player = player(helper);
        var first = wand("shockwave");
        var second = wand("shockwave");
        var shard = wand("vis_shard");
        player.setItemInHand(InteractionHand.MAIN_HAND, first);
        WandManager.setCooldown(player, first, 10000);
        helper.assertTrue(WandManager.isOnCooldown(player, second), "A second wand with the same focus bypasses recharge");
        player.setItemInHand(InteractionHand.MAIN_HAND, shard);
        WandManager.setCooldown(player, 300);
        helper.assertTrue(WandManager.getCooldown(player) <= 0.3F, "Legacy addon API must follow the equipped focus");
        WandManager.setCooldown(player, -1);
        helper.assertTrue(!WandManager.isOnCooldown(player, shard) && WandManager.isOnCooldown(player, first), "Clearing a continuous focus must not reset another focus's timer");
        var other = player(helper);
        helper.assertTrue(!WandManager.isOnCooldown(other, first), "Cooldown leaked to a different player");
        helper.succeed();
    }

    @GameTest(template = "test/platform", timeoutTicks = 5000)
    public static void shortCooldownExpiresWhileShockwaveKeepsRecharging(GameTestHelper helper) {
        var player = player(helper);
        var wave = wand("shockwave");
        var shard = wand("vis_shard");
        WandManager.setCooldown(player, wave, 10000);
        WandManager.setCooldown(player, shard, 300);
        helper.succeedWhen(() -> {
            helper.assertTrue(!WandManager.isOnCooldown(player, shard) && WandManager.getCooldown(player, shard) == 0, "Expired shard timer did not clear");
            helper.assertTrue(WandManager.isOnCooldown(player, wave), "Short focus timer overwrote shockwave recharge");
        });
    }
}
