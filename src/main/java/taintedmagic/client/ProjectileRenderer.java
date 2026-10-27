package taintedmagic.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.client.fx.ParticleFx;
import thaumcraft.client.platform.Billboards;
import thaumcraft.client.platform.TcRenderTypes;
import thaumcraft.client.render.entity.eldritch.EldritchOrbRenderer;
import taintedmagic.MagicProjectile;

public final class ProjectileRenderer extends EntityRenderer<MagicProjectile> {
    public ProjectileRenderer(EntityRendererProvider.Context context) { super(context); }
    @Override public ResourceLocation getTextureLocation(MagicProjectile entity) { return ParticleFx.PARTICLES; }
    @Override public void render(MagicProjectile entity, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        if (entity.mode() == 0) {
            EldritchOrbRenderer.renderOrb(entity, partial, pose, buffers);
        } else {
            boolean shard = entity.mode() == 1;
            float u = (shard ? 8 + entity.tickCount % 8 : entity.tickCount % 13) / 16F;
            float v = shard ? 0.25F : 0.1875F;
            float size = shard ? 0.4F + 0.1F * entity.strength() : 0.5F;
            int color = shard ? 0xFF671386 : Math.clamp(Math.round((20 - entity.tickCount - partial) / 40F * 255), 0, 255) << 24 | 0x191919;
            Billboards.submit(pose, buffers, Minecraft.getInstance().gameRenderer.getMainCamera(), TcRenderTypes.translucent(ParticleFx.PARTICLES), u, u + 0.0625F, v, v + 0.0625F, size, color, 0xF000F0);
        }
        super.render(entity, yaw, partial, pose, buffers, light);
    }
}
