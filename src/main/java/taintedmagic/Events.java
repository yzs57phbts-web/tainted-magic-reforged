package taintedmagic;

import java.util.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import thaumcraft.aspect.*;
import thaumcraft.platform.Baubles;
import thaumcraft.research.*;
import thaumcraft.wand.*;

public final class Events {
    private static final ResourceLocation SPEED = TaintedMagic.id("voidwalker_speed");
    private static final ResourceLocation STEP = TaintedMagic.id("voidwalker_step");
    private static final Set<UUID> FLIGHT = new HashSet<>();

    public static void init() {
        NeoForge.EVENT_BUS.addListener(Events::tick);
        NeoForge.EVENT_BUS.addListener(Events::damage);
        NeoForge.EVENT_BUS.addListener(Events::attack);
        NeoForge.EVENT_BUS.addListener(Events::attributes);
        NeoForge.EVENT_BUS.addListener(Events::crafted);
        NeoForge.EVENT_BUS.addListener(Events::logout);
        NeoForge.EVENT_BUS.addListener(Events::jump);
        NeoForge.EVENT_BUS.addListener(Events::tooltip);
    }

    public static boolean canHurt(Player player, Entity target) {
        return target != player && target.isAlive() && !target.isAlliedTo(player) && (!(target instanceof Player other) || player.canHarmPlayer(other) && (player.getServer() == null || player.getServer().isPvpAllowed()));
    }

    public static LivingEntity target(Player player, double range) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(range));
        var block = player.level().clip(new net.minecraft.world.level.ClipContext(start, end, net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, player));
        double distance = block.getType() == HitResult.Type.MISS ? range * range : block.getLocation().distanceToSqr(start);
        LivingEntity closest = null;
        for (LivingEntity candidate : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().expandTowards(player.getLookAngle().scale(range)).inflate(1), e -> canHurt(player, e))) {
            Optional<Vec3> hit = candidate.getBoundingBox().inflate(0.35).clip(start, end);
            if (hit.isPresent() && hit.get().distanceToSqr(start) < distance) {
                closest = candidate;
                distance = hit.get().distanceToSqr(start);
            }
        }
        return closest;
    }

    public static void shockwave(Player player, double radius, float damage, double force, double lift) {
        if (!(player.level() instanceof ServerLevel level)) return;
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius), e -> canHurt(player, e))) {
            if (entity.distanceToSqr(player) <= radius * radius) {
                if (damage > 0) entity.hurt(level.damageSources().indirectMagic(player, player), damage);
                Vec3 away = entity.position().subtract(player.position()).normalize();
                entity.push(away.x * force, lift, away.z * force);
                entity.hurtMarked = true;
                thaumcraft.platform.ModNetwork.sendToNear(level, null, player.getX(), player.getY(), player.getZ(), 64, new thaumcraft.network.ShockZapPayload(player.getId(), entity.getId()));
                Effects.send(level, Effects.SHOCK_SPARKS, 5, entity.getX(), entity.getY(), entity.getZ());
            }
        }
        level.playSound(null, player.blockPosition(), Content.SHOCKWAVE_SOUND.get(), net.minecraft.sounds.SoundSource.PLAYERS, 5F, 1.5F * level.random.nextFloat());
    }

    public static void tick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        boolean boots = Content.is(player.getItemBySlot(EquipmentSlot.FEET), "voidwalker_boots");
        boolean sash = Baubles.stacks(player).stream().anyMatch(stack -> Content.is(stack, "voidwalker_sash") && Data.integer(stack, "speed", 1) != 0);
        if (boots && player.fallDistance > 3) player.fallDistance = 1;
        boolean forward = thaumcraft.item.armor.ArmorEvents.movingForward(player);
        modifier(player, Attributes.MOVEMENT_SPEED, SPEED, 0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        modifier(player, Attributes.STEP_HEIGHT, STEP, boots && forward && !player.isShiftKeyDown() ? 0.4 : 0, AttributeModifier.Operation.ADD_VALUE);
        if (boots && forward) {
            float impulse = player.onGround() || player.getAbilities().flying ? (sash ? 0.24F : 0.12F) * (player.getAbilities().flying ? 0.75F : 1) : thaumcraft.item.armor.Hover.isHovering(thaumcraft.item.armor.Hover.getHarness(player)) ? 0.03F : player.isSprinting() ? 0.045F : 0.04F;
            player.moveRelative(impulse, new Vec3(0, 0, 1));
        }
        if (boots && player.level().isClientSide() && (Math.abs(player.getDeltaMovement().x)+Math.abs(player.getDeltaMovement().z)+Math.abs(player.getDeltaMovement().y*0.5) > 0.1 || !player.onGround()) && player.getRandom().nextInt(3) == 0) Effects.bootTrail.accept(player);
        if (player.level().isClientSide()) return;
        if (player instanceof ServerPlayer serverPlayer && player.tickCount % 20 == 0 && ResearchManager.isComplete(player, "ELDRITCHMAJOR") && !ResearchManager.isComplete(player, "TMELDRITCHMAJOR")) ResearchManager.complete(serverPlayer, "TMELDRITCHMAJOR", false);
        if (player instanceof ServerPlayer serverPlayer && player.tickCount % 20 == 0) {
            int blade = player.getInventory().items.stream().filter(stack -> stack.getItem() instanceof FortressBlade).mapToInt(stack -> ((FortressBlade) stack.getItem()).variant).findFirst().orElse(-1);
            boolean circle = player.getInventory().items.stream().anyMatch(stack -> Content.is(stack, "flyte_charm"));
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new VisualState(player.getId(), blade, circle));
        }
        if (player.tickCount % 20 == 0) {
            for (ItemStack stack : player.getInventory().items) repair(stack);
            for (ItemStack stack : player.getArmorSlots()) repair(stack);
        }
        boolean lumos = Baubles.stacks(player).stream().anyMatch(stack -> Content.is(stack, "lumos_ring")) || Wands.getFocus(player.getMainHandItem()) == Content.item("focus_lumos");
        MobEffectInstance current = player.getEffect(MobEffects.NIGHT_VISION);
        if (lumos && (current == null || current.getAmplifier() == -1 && player.tickCount % 20 == 0)) player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, -1, false, false));
        else if (!lumos && current != null && current.getAmplifier() == -1) player.removeEffect(MobEffects.NIGHT_VISION);
        boolean charm = player.getInventory().items.stream().anyMatch(stack -> Content.is(stack, "flyte_charm"));
        boolean fuel = charm && Magic.consume(player, new AspectList().add(Aspect.AIR, 15), false);
        if (!player.isCreative() && !player.isSpectator()) {
            if (fuel) {
                if (!player.getAbilities().mayfly) {
                    FLIGHT.add(player.getUUID());
                    player.getAbilities().mayfly = true;
                    player.onUpdateAbilities();
                }
                if (player.getAbilities().flying) Magic.consume(player, new AspectList().add(Aspect.AIR, 15), true);
                else if (player.isShiftKeyDown() && !player.onGround() && player.fallDistance > 0.5 && Magic.consume(player, new AspectList().add(Aspect.AIR, 5), true)) {
                    Vec3 look = player.getLookAngle();
                    player.setDeltaMovement(player.getDeltaMovement().add(look.x * 0.1, 0, look.z * 0.1).multiply(1, 0, 1).add(0, -0.1, 0));
                    player.hurtMarked = true;
                }
            } else if (FLIGHT.remove(player.getUUID())) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
        }
        if (player instanceof ServerPlayer serverPlayer && Content.is(player.getMainHandItem(), "creation_shard") && ResearchManager.isComplete(player, "CREATIONSHARD")) creation(serverPlayer);
    }

    private static void repair(ItemStack stack) {
        if (stack.isDamaged() && Data.integer(stack, "voidtouched", 0) != 0) stack.setDamageValue(stack.getDamageValue() - 1);
    }

    private static void modifier(Player player, net.minecraft.core.Holder<Attribute> attribute, ResourceLocation id, double value, AttributeModifier.Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        AttributeModifier old = instance.getModifier(id);
        if (old != null && old.amount() != value) instance.removeModifier(id);
        if (value != 0 && !instance.hasModifier(id)) instance.addTransientModifier(new AttributeModifier(id, value, operation));
    }

    private static void attack(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player defender && defender.isUsingItem() && (defender.getUseItem().getItem() instanceof Weapon weapon && !weapon.kind.equals("fortress") || defender.getUseItem().getItem() instanceof ShadowTool tool && tool.isSword()) && !event.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR)) event.setAmount((1 + event.getAmount()) * 0.5F);
        if (!(event.getSource().getEntity() instanceof Player player) || event.getSource().getDirectEntity() != player) return;
        ItemStack stack = player.getMainHandItem();
        if (Wands.getFocus(stack) instanceof MagicFocus focus && focus.kind.equals("mage_mace") && stack.getItem() instanceof WandItem wand && !wand.consumeAllVis(stack, player, focus.getVisCost(Wands.getFocusItem(stack)), true, false)) event.setCanceled(true);
    }

    private static void damage(LivingDamageEvent.Post event) {
        if (event.getNewDamage() <= 0 || !(event.getSource().getEntity() instanceof Player player) || event.getSource().getDirectEntity() != player || !Content.is(player.getMainHandItem(), "hollow_dagger")) return;
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack phial = player.getInventory().getItem(i);
            if (phial.is(Content.tc("glass_phial"))) {
                phial.shrink(1);
                ItemStack blood = new ItemStack(Content.item("crimson_blood"));
                if (!player.getInventory().add(blood)) player.drop(blood, false);
                break;
            }
        }
    }

    private static void attributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (Wands.getFocus(stack) instanceof MagicFocus focus && focus.kind.equals("mage_mace")) {
            event.removeModifier(Attributes.ATTACK_DAMAGE, Item.BASE_ATTACK_DAMAGE_ID);
            event.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, Config.MACE_DAMAGE.get() + Wands.getFocusPotency(stack) + (Wands.isStaff(stack) ? WandItem.STAFF_MELEE_DAMAGE - 1 : 0), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
            event.removeModifier(Attributes.ATTACK_SPEED, Item.BASE_ATTACK_SPEED_ID);
            event.addModifier(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, Wands.isStaff(stack) ? WandItem.STAFF_MELEE_SPEED : -2.4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        } else if (stack.getItem() instanceof Disassembler && Disassembler.charge(stack) <= 0) {
            event.removeModifier(Attributes.ATTACK_DAMAGE, Item.BASE_ATTACK_DAMAGE_ID);
            event.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        }
    }

    private static void crafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && Content.is(event.getCrafting(), "creation_shard")) creation(player);
    }

    private static void creation(ServerPlayer player) {
        if (!ResearchManager.isComplete(player, "CREATIONSHARD")) return;
        if (!ResearchManager.isComplete(player, "CREATION")) {
            ResearchManager.complete(player, "CREATION");
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable("text.creation"), false);
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200));
            player.level().playSound(null, player.blockPosition(), thaumcraft.registry.ModSounds.ELDRITCH_GUARDIAN_IDLE.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1, 1);
            player.level().playSound(null, player.blockPosition(), thaumcraft.registry.ModSounds.HEARTBEAT.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1, 1);
        }
        if (!ResearchManager.isComplete(player, "OUTERREV")) ResearchManager.complete(player, "OUTERREV");
    }

    private static void logout(PlayerEvent.PlayerLoggedOutEvent event) { FLIGHT.remove(event.getEntity().getUUID()); }
    private static void jump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        double multiplier = Content.is(player.getItemBySlot(EquipmentSlot.FEET), "voidwalker_boots") ? 1.25 : 1;
        if (Baubles.stacks(player).stream().anyMatch(stack -> Content.is(stack, "voidwalker_sash") && Data.integer(stack, "speed", 1) != 0)) multiplier *= 1.05;
        if (multiplier != 1) player.setDeltaMovement(player.getDeltaMovement().multiply(1, multiplier, 1));
    }
    private static void tooltip(net.neoforged.neoforge.event.entity.player.ItemTooltipEvent event) {
        if (Data.integer(event.getItemStack(), "voidtouched", 0) != 0) event.getToolTip().add(net.minecraft.network.chat.Component.translatable("text.voidtouched").withStyle(net.minecraft.ChatFormatting.DARK_PURPLE));
    }
}
