package taintedmagic;

import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;

public final class MagicProjectile extends ThrowableProjectile implements ItemSupplier {
    private static final EntityDataAccessor<Integer> MODE = SynchedEntityData.defineId(MagicProjectile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STRENGTH = SynchedEntityData.defineId(MagicProjectile.class, EntityDataSerializers.INT);
    private float damage = 16;
    private int enlarge;
    private boolean corrosive;
    private boolean persistent;
    private UUID target;
    private String targetType = "";
    public MagicProjectile(EntityType<? extends MagicProjectile> type, Level level) { super(type, level); }
    public static MagicProjectile create(ServerLevel level, Player owner, int mode, float damage, int enlarge, boolean corrosive, LivingEntity target, boolean persistent) {
        MagicProjectile entity = new MagicProjectile(Content.PROJECTILE.get(), level);
        entity.setOwner(owner);
        entity.setPos(owner.getEyePosition().add(owner.getLookAngle().scale(0.5)));
        entity.entityData.set(MODE, mode);
        entity.damage = damage;
        entity.entityData.set(STRENGTH, mode == 1 ? Math.max(0, Math.round((damage - 2) * 2)) : 0);
        entity.enlarge = enlarge;
        entity.corrosive = corrosive;
        entity.persistent = persistent;
        if (target != null) { entity.target = target.getUUID(); entity.targetType = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString(); }
        entity.shootFromRotation(owner, owner.getXRot(), owner.getYRot(), 0, mode == 1 ? 0.5F : 1F, mode == 2 ? (enlarge > 0 ? 12 + enlarge : 9) : mode == 1 ? 15 : 0);
        return entity;
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { builder.define(MODE, 0); builder.define(STRENGTH, 0); }
    @Override protected double getDefaultGravity() { return 0; }
    public int mode() { return entityData.get(MODE); }
    public int strength() { return entityData.get(STRENGTH); }
    @Override public ItemStack getItem() { return new ItemStack(Content.item(mode() == 1 ? "warped_shard" : "primordial_mote")); }
    @Override public void tick() {
        if (!level().isClientSide() && mode() == 1 && level() instanceof ServerLevel server) {
            Entity destination = target == null ? null : server.getEntity(target);
            if ((destination == null || !destination.isAlive() || distanceToSqr(destination) > 1250) && persistent) {
                destination = server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(16), e -> e != getOwner() && e.isAlive() && net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString().equals(targetType) && (!(getOwner() instanceof Player p) || Events.canHurt(p, e))).stream().findFirst().orElse(null);
                target = destination == null ? null : destination.getUUID();
            }
            if (destination == null || !destination.isAlive()) { server.broadcastEntityEvent(this, (byte)16); discard(); return; }
            if (tickCount % 10 == 0) {
                setDeltaMovement(destination.position().add(0, destination.getBbHeight() * 0.6, 0).subtract(position()).normalize());
                server.playSound(null, getX(), getY(), getZ(), Content.SHARD_SOUND.get(), net.minecraft.sounds.SoundSource.PLAYERS, 0.1F, 2F * random.nextFloat());
            }
            setDeltaMovement(getDeltaMovement().scale(0.85));
        }
        super.tick();
        if (level().isClientSide() && mode() == 1) thaumcraft.fx.Fx.get().sparkle(level(), (float)getX() + (random.nextFloat() - random.nextFloat()) * 0.25F, (float)getY() + (random.nextFloat() - random.nextFloat()) * 0.25F, (float)getZ() + (random.nextFloat() - random.nextFloat()) * 0.25F, 0.5F, 0, 0);
        if (mode() == 2) setDeltaMovement(getDeltaMovement().scale(0.95));
        if (!level().isClientSide() && tickCount > (mode() == 1 ? 300 : mode() == 2 ? 20 : 100)) {
            if (mode() == 1) ((ServerLevel)level()).broadcastEntityEvent(this, (byte)16);
            discard();
        }
    }
    @Override protected void onHit(HitResult hit) {
        if (!(level() instanceof ServerLevel server)) return;
        if (mode() == 1 && hit instanceof BlockHitResult block) {
            var state = level().getBlockState(block.getBlockPos());
            if (state.is(net.minecraft.tags.BlockTags.LEAVES) || state.canBeReplaced() || state.is(net.minecraft.world.level.block.Blocks.COBWEB) || state.is(net.minecraft.tags.BlockTags.PORTALS)) return;
            Vec3 motion = getDeltaMovement();
            Vec3 normal = Vec3.atLowerCornerOf(block.getDirection().getNormal());
            setDeltaMovement(motion.subtract(normal.scale(2 * motion.dot(normal))).scale(0.8));
            return;
        }
        if (mode() == 2 && !(hit instanceof EntityHitResult)) { server.broadcastEntityEvent(this, (byte)16); discard(); return; }
        double radius = mode() == 0 ? 1.5 + enlarge * 0.5 : mode() == 2 ? 1 : 0;
        if (mode() == 1 && hit instanceof EntityHitResult entity) hurtTarget(entity.getEntity());
        else for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius))) hurtTarget(victim);
        server.broadcastEntityEvent(this, (byte)16);
        if (mode() != 2) server.playSound(null, getX(), getY(), getZ(), mode() == 1 ? thaumcraft.registry.ModSounds.ZAP.get() : net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH, net.minecraft.sounds.SoundSource.PLAYERS, mode() == 1 ? 1F : 0.5F, mode() == 1 ? 1F + (random.nextFloat()-random.nextFloat())*0.2F : 2.6F + (random.nextFloat()-random.nextFloat())*0.8F);
        discard();
    }
    @Override public void handleEntityEvent(byte event) {
        if (event != 16) { super.handleEntityEvent(event); return; }
        if (mode() == 0) thaumcraft.fx.Fx.get().eldritchOrbBurst(level(), this);
        else if (mode() == 1) thaumcraft.fx.Fx.get().shockBurst(level(), getX(), getY(), getZ(), 0.3F);
        else Effects.client.accept(new Effects(Effects.DIFFUSION, 1, getX(), getY(), getZ()), level());
    }
    private void hurtTarget(Entity target) {
        if (!(target instanceof LivingEntity living) || target == getOwner() || getOwner() instanceof Player player && !Events.canHurt(player, target)) return;
        living.hurt(level().damageSources().indirectMagic(this, getOwner()), damage);
        if (mode() != 1) {
            int duration = mode() == 2 ? 40 : 160;
            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 1));
            if (corrosive) living.addEffect(new MobEffectInstance(MobEffects.WITHER, duration, 1));
        }
    }
    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("mode", mode()); tag.putFloat("damage", damage); tag.putInt("enlarge", enlarge); tag.putBoolean("corrosive", corrosive); tag.putBoolean("persistent", persistent); tag.putString("targetType", targetType);
        if (target != null) tag.putUUID("target", target);
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(MODE, tag.getInt("mode")); damage = tag.getFloat("damage"); enlarge = tag.getInt("enlarge"); corrosive = tag.getBoolean("corrosive"); persistent = tag.getBoolean("persistent"); targetType = tag.getString("targetType");
        entityData.set(STRENGTH, mode() == 1 ? Math.max(0, Math.round((damage - 2) * 2)) : 0);
        if (tag.hasUUID("target")) target = tag.getUUID("target");
    }
}
