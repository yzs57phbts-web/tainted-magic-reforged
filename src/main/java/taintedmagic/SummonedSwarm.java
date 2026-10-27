package taintedmagic;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import thaumcraft.entity.TaintedMob;
import thaumcraft.fx.Fx;

public final class SummonedSwarm extends Monster implements TaintedMob {
    private UUID ownerId;
    private UUID targetId;
    private int cooldown;
    public SummonedSwarm(EntityType<? extends SummonedSwarm> type, Level level) { super(type, level); setNoGravity(true); }
    public static AttributeSupplier.Builder attributes() { return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 30).add(Attributes.MOVEMENT_SPEED, 0).add(Attributes.ATTACK_DAMAGE, 5).add(Attributes.FOLLOW_RANGE, 32); }
    @Override protected void registerGoals() { }
    @Override public boolean isPushable() { return false; }
    @Override protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource source) { return thaumcraft.registry.ModSounds.SWARM_ATTACK.get(); }
    @Override protected net.minecraft.sounds.SoundEvent getDeathSound() { return thaumcraft.registry.ModSounds.SWARM_ATTACK.get(); }
    @Override protected float getSoundVolume() { return 0.1F; }
    public void bind(Player owner, LivingEntity target, int damage) {
        ownerId = owner.getUUID(); targetId = target.getUUID();
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
    }
    @Override public void tick() {
        if (level() instanceof ServerLevel level) {
            Entity owner = ownerId == null ? null : level.getEntity(ownerId);
            Entity target = targetId == null ? null : level.getEntity(targetId);
            if (!(target instanceof LivingEntity living) || !living.isAlive() || living instanceof TaintedMob || owner instanceof Player player && !Events.canHurt(player, target)) {
                hurt(damageSources().generic(), 5);
            } else {
                Vec3 delta = living.getEyePosition().subtract(position());
                Vec3 motion = getDeltaMovement();
                setDeltaMovement(motion.x + (Math.signum(delta.x) * 0.5 - motion.x) * 0.025, motion.y + (Math.signum(delta.y) * 0.7 - motion.y) * 0.1, motion.z + (Math.signum(delta.z) * 0.5 - motion.z) * 0.025);
                if (cooldown-- <= 0 && distanceTo(living) < 3 && living.getBoundingBox().maxY > getBoundingBox().minY && living.getBoundingBox().minY < getBoundingBox().maxY) {
                    cooldown = 10 + random.nextInt(5);
                    Vec3 previous = living.getDeltaMovement();
                    if (living.hurt(damageSources().source(thaumcraft.damage.ModDamageTypes.SWARM, this, owner), (float)getAttributeValue(Attributes.ATTACK_DAMAGE))) living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100));
                    level.playSound(null, blockPosition(), thaumcraft.registry.ModSounds.SWARM_ATTACK.get(), net.minecraft.sounds.SoundSource.HOSTILE, 0.3F, 0.9F + random.nextFloat() * 0.2F);
                    living.setDeltaMovement(previous);
                }
            }
        }
        super.tick();
        if (level().isClientSide()) Fx.get().taintSwarm(level(), this, 25, 0.22F, 15F, 0.08F);
        setDeltaMovement(getDeltaMovement().multiply(1, 0.6, 1));
    }
    @Override public boolean causeFallDamage(float height, float multiplier, DamageSource source) { return false; }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerId != null) tag.putUUID("owner", ownerId);
        if (targetId != null) tag.putUUID("target", targetId);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("owner")) ownerId = tag.getUUID("owner");
        if (tag.hasUUID("target")) targetId = tag.getUUID("target");
    }
}
