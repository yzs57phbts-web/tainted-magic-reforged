package taintedmagic.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import taintedmagic.*;

public final class BladeRenderer extends BlockEntityWithoutLevelRenderer {
    private final ModelPart blade = BladeModel.blade();
    private final ModelPart saya = BladeModel.saya();
    public BladeRenderer() { super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels()); }
    public static ResourceLocation texture(int variant) { return TaintedMagic.id("textures/models/modelkatana" + new String[] {"thaumium", "voidmetal", "shadowmetal"}[Math.clamp(variant, 0, 2)] + ".png"); }
    @Override public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (!(stack.getItem() instanceof FortressBlade item)) return;
        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        boolean held = context.firstPerson() || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        if (held) {
            var player = Minecraft.getInstance().player;
            if (context.firstPerson()) {
                boolean charging = player != null && player.isUsingItem() && ItemStack.isSameItem(player.getUseItem(), stack);
                if (charging) pose.mulPose(Axis.ZP.rotationDegrees(context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND ? 150 : -150));
                pose.translate(0, -0.25, 0);
                if (charging) pose.mulPose(Axis.ZP.rotationDegrees(180));
            }
            pose.mulPose(Axis.YP.rotationDegrees(90));
            pose.scale(0.55F, 0.55F, 0.55F);
            pose.translate(0, 2.875, 0);
        } else {
            pose.scale(0.28F, 0.28F, 0.28F);
            pose.translate(0, 1.375, 0);
        }
        var consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(texture(item.variant)));
        if (context == ItemDisplayContext.GUI || context == ItemDisplayContext.GROUND || context == ItemDisplayContext.FIXED) saya.render(pose, consumer, light, overlay);
        blade.render(pose, consumer, light, overlay);
        if (Data.integer(stack, "inscription", -1) >= 0) runes(pose, buffers);
        pose.popPose();
    }

    private static void runes(PoseStack pose, MultiBufferSource buffers) {
        var consumer = buffers.getBuffer(RenderType.entityTranslucentEmissive(net.minecraft.resources.ResourceLocation.parse("thaumcraft:textures/misc/script.png")));
        float age = Minecraft.getInstance().player == null ? 0 : Minecraft.getInstance().player.tickCount;
        for (int side : new int[] {-1, 1}) for (int index = 0; index < 14; index++) {
            int rune = (index * 3 + (side == 1 ? 3 : 0)) % 16;
            float alpha = net.minecraft.util.Mth.sin((age + rune * 5) / 10F) * 0.3F + 0.6F;
            int color = Math.round(alpha * 255) << 24 | 0xE1A033;
            float y = -2.35F + index * 0.14F;
            float x = side * 0.019F;
            var matrix = pose.last().pose();
            consumer.addVertex(matrix, x, y, -0.06F).setColor(color).setUv(rune / 16F, 1).setOverlay(0).setLight(LightTexture.FULL_BRIGHT).setNormal(pose.last(), side, 0, 0);
            consumer.addVertex(matrix, x, y + 0.12F, -0.06F).setColor(color).setUv((rune + 1) / 16F, 1).setOverlay(0).setLight(LightTexture.FULL_BRIGHT).setNormal(pose.last(), side, 0, 0);
            consumer.addVertex(matrix, x, y + 0.12F, 0.06F).setColor(color).setUv((rune + 1) / 16F, 0).setOverlay(0).setLight(LightTexture.FULL_BRIGHT).setNormal(pose.last(), side, 0, 0);
            consumer.addVertex(matrix, x, y, 0.06F).setColor(color).setUv(rune / 16F, 0).setOverlay(0).setLight(LightTexture.FULL_BRIGHT).setNormal(pose.last(), side, 0, 0);
        }
    }
}
