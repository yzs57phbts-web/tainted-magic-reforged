package taintedmagic.client;

import java.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.*;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.*;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import thaumcraft.client.render.armor.*;
import taintedmagic.*;

@EventBusSubscriber(modid = TaintedMagic.MODID, value = Dist.CLIENT)
public final class Client {
    @SubscribeEvent static void logout(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) { VisualState.PLAYERS.clear(); }
    private static BladeRenderer bladeRenderer;
    @SubscribeEvent static void extensions(RegisterClientExtensionsEvent event) {
        IClientItemExtensions blade = new IClientItemExtensions() {
            @Override public boolean applyForgeHandTransform(PoseStack pose, net.minecraft.client.player.LocalPlayer player, HumanoidArm arm, ItemStack stack, float partial, float equipped, float swing) {
                if (!player.isUsingItem() || !ItemStack.isSameItem(player.getUseItem(), stack)) return false;
                int side = arm == HumanoidArm.RIGHT ? 1 : -1;
                pose.translate(side * 0.24F, -0.35F - equipped * 0.6F, -0.8F);
                pose.mulPose(Axis.YP.rotationDegrees(side * 20));
                pose.mulPose(Axis.ZP.rotationDegrees(side * 35));
                return true;
            }
            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (bladeRenderer == null) bladeRenderer = new BladeRenderer();
                return bladeRenderer;
            }
        };
        for (String name : new String[] {"thaumium_fortress_blade", "void_fortress_blade", "shadow_fortress_blade"}) event.registerItem(blade, Content.item(name));
        Map<EquipmentSlot, TcArmorModel> models = new EnumMap<>(EquipmentSlot.class);
        IClientItemExtensions fortress = new IClientItemExtensions() {
            @Override public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
                return models.computeIfAbsent(slot, key -> new TcArmorModel((key == EquipmentSlot.CHEST ? ArmorKind.FORTRESS.createOuter() : ArmorKind.FORTRESS.createInner()).bakeRoot(), ArmorKind.FORTRESS, key));
            }
            @Override public void setupModelAnimations(LivingEntity entity, ItemStack stack, EquipmentSlot slot, Model model, float swing, float amount, float partial, float age, float yaw, float pitch) {
                if (model instanceof TcArmorModel armor) armor.animateArmor(entity, swing, amount, age);
            }
        };
        Content.ALL.values().stream().map(value -> value.get()).filter(FortressArmor.class::isInstance).forEach(item -> event.registerItem(fortress, item));
    }
    @SubscribeEvent static void entities(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(Content.PROJECTILE.get(), ProjectileRenderer::new);
        event.registerEntityRenderer(Content.SWARM.get(), NoopRenderer::new);
    }
    @SubscribeEvent static void colors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, layer) -> {
            if (layer != 1) return -1;
            int color = Data.tag(stack).getInt("colour");
            return color == 0 ? -1 : java.awt.Color.HSBtoRGB(color / 360F, 1, 0.85F);
        }, Content.item("gate_key"));
    }
    @SubscribeEvent static void gui(RegisterGuiLayersEvent event) { event.registerAbove(VanillaGuiLayers.PLAYER_HEALTH, TaintedMagic.id("held_item"), Client::hud); }
    private static void hud(GuiGraphics graphics, DeltaTracker delta) {
        var player = Minecraft.getInstance().player;
        if (player == null || Minecraft.getInstance().screen != null) return;
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof FortressBlade)) return;
        long cooldown = FortressBlade.cooldown(held, player.level());
        float fill = cooldown > 0 ? cooldown / 140F : Math.min(1F, player.getTicksUsingItem() / 20F);
        graphics.pose().pushPose();
        graphics.pose().translate(graphics.guiWidth() / 2F + 9.45F, graphics.guiHeight() - (player.isCreative() ? 31.5F : 47.25F), 210);
        graphics.pose().scale(0.315F, 0.315F, 1);
        var texture = net.minecraft.resources.ResourceLocation.parse("thaumcraft:textures/misc/script.png");
        for (int rune = 0; rune < 16; rune++) {
            float pulse = net.minecraft.util.Mth.sin((player.tickCount + rune * 5) / 10F) * 0.3F;
            int red = Math.round((net.minecraft.util.Mth.sin((player.tickCount + rune * 5) / 5F) * 0.1F + 0.8F) * 255);
            int green = Math.round((net.minecraft.util.Mth.sin((player.tickCount + rune * 5) / 7F) * 0.1F + 0.7F) * 255);
            int color = Math.round((pulse + 0.7F) * 255) << 24 | red << 16 | green << 8 | 102;
            if (rune < Math.ceil(16 * fill)) color = 0xFF000000 | java.awt.Color.HSBtoRGB((cooldown > 0 ? 16F - rune : rune) / 16F * 0.3F, 1, 1) & 0xFFFFFF;
            thaumcraft.client.platform.GuiBlit.blit(graphics, texture, rune * 16, 0, 16, 16, rune * 16, 0, 16, 16, 256, 16, color);
        }
        graphics.pose().popPose();
    }
    @SubscribeEvent static void layers(EntityRenderersEvent.AddLayers event) {
        for (var skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) renderer.addLayer(new Accessories(renderer));
        }
    }
    private static final class Accessories extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
        private final ModelPart saya = BladeModel.saya();
        private final ModelPart blade = BladeModel.blade();
        Accessories(PlayerRenderer renderer) { super(renderer); }
        @Override public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, float swing, float amount, float partial, float age, float yaw, float pitch) {
            if (player.isInvisible()) return;
            VisualState state = VisualState.PLAYERS.get(player.getId());
            if (state == null) return;
            if (state.blade() >= 0) {
                pose.pushPose();
                getParentModel().body.translateAndRotate(pose);
                pose.translate(0.32, 0.62, 0.12);
                pose.mulPose(Axis.XP.rotationDegrees(55));
                pose.mulPose(Axis.YP.rotationDegrees(180));
                pose.scale(0.5F, 0.5F, 0.5F);
                pose.translate(0, 2.5, 0);
                saya.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(BladeRenderer.texture(state.blade()))), light, OverlayTexture.NO_OVERLAY);
                if (!(player.getMainHandItem().getItem() instanceof FortressBlade item && item.variant == state.blade()) && !(player.getOffhandItem().getItem() instanceof FortressBlade off && off.variant == state.blade())) blade.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(BladeRenderer.texture(state.blade()))), light, OverlayTexture.NO_OVERLAY);
                pose.popPose();
            }
            if (state.charm()) {
                pose.pushPose();
                pose.translate(0, 1.42, 0);
                pose.mulPose(Axis.YP.rotationDegrees(player.tickCount + partial));
                var vertex = buffers.getBuffer(RenderType.entityTranslucentEmissive(TaintedMagic.id("textures/misc/circle.png")));
                var matrix = pose.last().pose();
                vertex.addVertex(matrix, -0.4F, 0, -0.4F).setColor(0xCCFFFFFF).setUv(0, 0).setOverlay(0).setLight(LightTexture.FULL_BRIGHT).setNormal(pose.last(), 0, 1, 0);
                vertex.addVertex(matrix, -0.4F, 0, 0.4F).setColor(0xCCFFFFFF).setUv(0, 1).setOverlay(0).setLight(LightTexture.FULL_BRIGHT).setNormal(pose.last(), 0, 1, 0);
                vertex.addVertex(matrix, 0.4F, 0, 0.4F).setColor(0xCCFFFFFF).setUv(1, 1).setOverlay(0).setLight(LightTexture.FULL_BRIGHT).setNormal(pose.last(), 0, 1, 0);
                vertex.addVertex(matrix, 0.4F, 0, -0.4F).setColor(0xCCFFFFFF).setUv(1, 0).setOverlay(0).setLight(LightTexture.FULL_BRIGHT).setNormal(pose.last(), 0, 1, 0);
                pose.popPose();
            }
        }
    }
}
