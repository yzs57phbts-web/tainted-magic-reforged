package taintedmagic.client;

import java.nio.file.*;
import java.util.function.Consumer;
import net.minecraft.client.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import taintedmagic.*;
import thaumcraft.aspect.Aspect;
import thaumcraft.client.wand.WandKeyHandler;
import thaumcraft.infusion.InfusionKeys;
import thaumcraft.wand.*;

@EventBusSubscriber(modid = TaintedMagic.MODID, value = Dist.CLIENT)
public final class VisualSmoke {
    private static boolean started;
    private static int ticks;
    private static int captures;
    private static int frames;
    private static void server(Consumer<ServerPlayer> action) {
        Minecraft mc = Minecraft.getInstance();
        var server = mc.getSingleplayerServer();
        if (server != null && mc.player != null) {
            var id = mc.player.getUUID();
            server.execute(() -> { var player = server.getPlayerList().getPlayer(id); if (player != null) action.accept(player); });
        }
    }
    private static ItemStack blade() {
        var stack = new ItemStack(Content.item("shadow_fortress_blade"));
        InfusionKeys.TAGS.set(stack, java.util.Map.of("inscription", "2"));
        return stack;
    }
    private static ItemStack wand() {
        var stack = Wands.create(WandRegistry.CAP_GOLD, WandRegistry.ROD_SILVERWOOD, false);
        Wands.fill(stack);
        return stack;
    }
    private static void capture(String name) {
        Minecraft mc = Minecraft.getInstance();
        Screenshot.grab(mc.gameDirectory, name + ".png", mc.getMainRenderTarget(), message -> {
            try {
                Files.writeString(Path.of(mc.gameDirectory.toString(), "visual-smoke-result.txt"), name + ": " + message.getString() + System.lineSeparator(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                captures++;
            } catch (java.io.IOException exception) { throw new RuntimeException(exception); }
        });
    }
    @SubscribeEvent static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("taintedmagic.visualSmoke")) return;
        Minecraft mc = Minecraft.getInstance();
        mc.options.pauseOnLostFocus = false;
        if (++frames % 100 == 1) System.out.println("TM visual smoke: screen=" + (mc.screen == null ? "null" : mc.screen.getClass().getSimpleName()) + ", overlay=" + mc.getOverlay() + ", stage=" + ticks);
        if (!started && mc.getOverlay() == null && mc.screen != null) {
            started = true;
            var rules = new GameRules();
            rules.getRule(GameRules.RULE_SPAWN_CHUNK_RADIUS).set(0, null);
            mc.options.renderDistance().set(4);
            mc.options.simulationDistance().set(5);
            mc.createWorldOpenFlows().createFreshLevel("tm-audit-" + System.currentTimeMillis(), new LevelSettings("Tainted Magic visual audit", GameType.SURVIVAL, false, Difficulty.PEACEFUL, true, rules, net.minecraft.world.level.WorldDataConfiguration.DEFAULT), new WorldOptions(12345L, false, false), registry -> registry.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(), mc.screen);
        }
        if (mc.player == null || mc.level == null || mc.getOverlay() != null) return;
        ticks++;
        if (ticks == 10) server(player -> {
            var level = player.serverLevel();
            for (BlockPos pos : BlockPos.betweenClosed(-10, 99, -10, 10, 99, 10)) level.setBlockAndUpdate(pos, Blocks.STONE_BRICKS.defaultBlockState());
            level.setDayTime(6000);
            player.connection.teleport(0.5, 100, 0.5, 180, 0);
            player.getInventory().clearContent();
            player.getInventory().selected = 0;
            player.setItemInHand(InteractionHand.MAIN_HAND, blade());
            player.getInventory().setItem(8, wand());
            for (var slot : new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS}) {
                var armor = new ItemStack(Content.item("shadow_fortress_" + (slot == EquipmentSlot.HEAD ? "helmet" : slot == EquipmentSlot.CHEST ? "chestplate" : "leggings")));
                if (slot == EquipmentSlot.CHEST) InfusionKeys.HARDEN.set(armor, 20);
                player.setItemSlot(slot, armor);
            }
            player.setItemSlot(EquipmentSlot.FEET, new ItemStack(Content.item("voidwalker_boots")));
        });
        if (Boolean.getBoolean("taintedmagic.effectsSmoke")) {
            EffectsSmoke.step(ticks);
            return;
        }
        if (Boolean.getBoolean("taintedmagic.sanitySmoke")) {
            if (ticks == 35) {
                mc.setScreen(null);
                mc.options.setCameraType(CameraType.FIRST_PERSON);
                server(player -> {
                    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(thaumcraft.registry.ModItems.SANITY_CHECKER.get()));
                    thaumcraft.research.Warp.set(player, 20, thaumcraft.research.WarpType.PERMANENT);
                    thaumcraft.research.Warp.set(player, 30, thaumcraft.research.WarpType.STICKY);
                    thaumcraft.research.Warp.set(player, 50, thaumcraft.research.WarpType.TEMPORARY);
                });
            }
            if (ticks == 80) capture("sanity-01-three-warp-types");
            if (ticks == 95) server(player -> {
                for (var type : thaumcraft.research.WarpType.values()) thaumcraft.research.Warp.set(player, 0, type);
            });
            if (ticks == 125) capture("sanity-02-empty");
            if (ticks == 140) server(player -> {
                for (var type : thaumcraft.research.WarpType.values()) thaumcraft.research.Warp.set(player, 100, type);
            });
            if (ticks == 170) capture("sanity-03-overflow");
            if (ticks == 185 && captures >= 3) mc.stop();
            if (ticks > 250) throw new IllegalStateException("Sanity smoke did not complete: " + captures + " screenshots");
            return;
        }
        if (Boolean.getBoolean("taintedmagic.sheathSmoke")) {
            if (ticks == 35) { mc.setScreen(null); mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT); }
            if (ticks == 65) capture("sheath-01-shadow");
            if (ticks == 75) server(player -> player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Content.item("thaumium_fortress_blade"))));
            if (ticks == 105) capture("sheath-02-thaumium");
            if (ticks == 115) server(player -> player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Content.item("void_fortress_blade"))));
            if (ticks == 145) capture("sheath-03-void");
            if (ticks == 165 && captures >= 3) mc.stop();
            if (ticks > 250) throw new IllegalStateException("Sheath smoke did not complete: " + captures + " screenshots");
            return;
        }
        if (ticks == 35) { mc.setScreen(null); mc.options.setCameraType(CameraType.FIRST_PERSON); }
        if (ticks == 65) capture("audit-01-first-person");
        if (ticks == 75) mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
        if (ticks == 105) capture("audit-02-third-person");
        if (ticks == 115) { mc.options.setCameraType(CameraType.FIRST_PERSON); mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND); mc.options.keyUse.setDown(true); }
        if (ticks == 140) capture("audit-03-katana-charge");
        if (ticks == 145) { mc.options.keyUse.setDown(false); mc.gameMode.releaseUsingItem(mc.player); }
        if (ticks == 165) capture("audit-04-katana-cooldown");
        if (ticks == 180) server(player -> {
            player.setItemInHand(InteractionHand.MAIN_HAND, wand());
            String[] names = {"taint_swarm", "dark_matter", "mage_mace", "shockwave", "vis_shard", "lumos"};
            for (int i = 0; i < names.length; i++) player.getInventory().setItem(i + 1, new ItemStack(Content.item("focus_" + names[i])));
        });
        if (ticks >= 205 && ticks <= 245) WandKeyHandler.CHANGE_FOCUS.setDown(true);
        if (ticks == 235) capture("audit-05-six-foci-and-runic-shield");
        if (ticks == 246) WandKeyHandler.CHANGE_FOCUS.setDown(false);
        if (ticks == 265) server(player -> {
            ItemStack stack = wand();
            Wands.setFocus(stack, new ItemStack(Content.item("focus_shockwave")));
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        });
        if (ticks == 285) mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        if (ticks == 305) capture("audit-06-focus-cooldown");
        if (ticks == 320) server(player -> {
            var level = player.serverLevel();
            for (int mode : new int[] {0, 1}) {
                var orb = MagicProjectile.create(level, player, mode, mode == 0 ? 16 : 4, 0, false, player, false);
                orb.setPos(mode == 0 ? -0.3 : 1.3, 101.5, -2.5);
                orb.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                orb.tickCount = 10;
                level.addFreshEntity(orb);
            }
        });
        if (ticks == 325) capture("audit-07-projectile-effects");
        if (ticks == 335) server(player -> {
            var focus = new ItemStack(Content.item("focus_dark_matter"));
            WandManager.changeFocus(player.getMainHandItem(), player.level(), player, ((MagicFocus)focus.getItem()).getSortingHelper(focus));
        });
        if (ticks == 355) capture("audit-08-swap-clears-displayed-wave-cooldown");
        if (ticks == 360) mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        if (ticks == 365) capture("audit-09-dark-matter-own-short-cooldown");
        if (ticks == 375) server(player -> {
            var focus = new ItemStack(Content.item("focus_shockwave"));
            WandManager.changeFocus(player.getMainHandItem(), player.level(), player, ((MagicFocus)focus.getItem()).getSortingHelper(focus));
        });
        if (ticks == 395) capture("audit-10-original-wave-cooldown-restored");
        if (ticks == 415 && captures >= 10) mc.stop();
        if (ticks > 500) throw new IllegalStateException("Visual smoke did not complete: " + captures + " screenshots");
    }
}
