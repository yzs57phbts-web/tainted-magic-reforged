package taintedmagic;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import thaumcraft.item.gear.WarpingGear;

public class UtilityItem extends Item implements WarpingGear {
    public final String kind;

    public UtilityItem(String kind, Properties p) { super(p); this.kind = kind; }
    @Override public boolean hasCraftingRemainingItem(ItemStack stack) { return kind.equals("crimson_blood") || kind.equals("void_blood") || super.hasCraftingRemainingItem(stack); }
    @Override public ItemStack getCraftingRemainingItem(ItemStack stack) { return kind.equals("crimson_blood") || kind.equals("void_blood") ? new ItemStack(Content.tc("glass_phial")) : super.getCraftingRemainingItem(stack); }

    @Override
    public int getWarp(ItemStack stack, Player player) { return kind.equals("gate_key") ? 3 : kind.equals("flyte_charm") ? 5 : 0; }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.mayUseItemAt(context.getClickedPos(), context.getClickedFace(), context.getItemInHand())) return InteractionResult.FAIL;
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        if (kind.equals("warp_fertilizer") && level.getBlockState(pos).is(thaumcraft.registry.ModBlocks.SILVERWOOD_SAPLING.get())) {
            if (level instanceof ServerLevel server) {
                server.setBlockAndUpdate(pos, Content.SAPLING.get().defaultBlockState());
                Effects.send(server, Effects.WARPWOOD, 10+level.random.nextInt(11), pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5);
                server.playSound(null, pos, thaumcraft.registry.ModSounds.ROOTS.get(), SoundSource.PLAYERS, 1, 1);
                stack.consume(1, player);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (kind.equals("gate_key") && !Data.tag(stack).contains("bound")) {
            if (!level.isClientSide()) {
                Data.update(stack, tag -> {
                    tag.putBoolean("bound", true);
                    tag.putLong("position", pos.above().asLong());
                    tag.putString("dimension", level.dimension().location().toString());
                    tag.putInt("colour", level.random.nextInt(360) + 1);
                });
                level.playSound(null, pos, thaumcraft.registry.ModSounds.WAND.get(), SoundSource.PLAYERS, 0.5F + level.random.nextFloat() * 0.5F, 1);
                if (level instanceof ServerLevel server) Effects.send(server, Effects.GATE_BIND, 9, pos.getX()+0.5, pos.getY(), pos.getZ()+0.5);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (kind.equals("gate_key") && Data.tag(stack).getBoolean("bound")) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        return super.use(level, player, hand);
    }

    @Override public int getUseDuration(ItemStack stack, LivingEntity entity) { return 40; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return kind.equals("gate_key") ? UseAnim.BOW : UseAnim.NONE; }
    @Override public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        if (!kind.equals("gate_key")) return;
        if (level.isClientSide()) Effects.client.accept(new Effects(Effects.GATE_CHARGE,1,entity.getX(),entity.getY(),entity.getZ()),level);
        else if (entity.tickCount % 5 == 0) {
            float pitch=1+level.random.nextFloat()*0.25F;
            level.playSound(null, entity.blockPosition(), thaumcraft.registry.ModSounds.WIND.get(), SoundSource.PLAYERS, pitch*0.1F, pitch);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player) || !kind.equals("gate_key")) return stack;
        var tag = Data.tag(stack);
        BlockPos pos = BlockPos.of(tag.getLong("position"));
        if (!tag.getBoolean("bound")) return stack;
        if (!level.dimension().location().toString().equals(tag.getString("dimension"))) {
            player.displayClientMessage(Component.translatable("key.invaliddim"), true);
        } else if (level.getWorldBorder().isWithinBounds(pos) && !level.isOutsideBuildHeight(pos) && level.noCollision(player, player.getBoundingBox().move(pos.getX() + 0.5 - player.getX(), pos.getY() - player.getY(), pos.getZ() + 0.5 - player.getZ()))) {
            player.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160));
            level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 5F, 1F);
        } else player.displayClientMessage(Component.translatable("key.error"), true);
        return stack;
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (!kind.startsWith("salis_") || entity.level().dimension() != Level.OVERWORLD) return false;
        if (!(entity.level() instanceof ServerLevel level)) {
            Effects.client.accept(new Effects(kind.equals("salis_tempestas")?Effects.SALT_WEATHER_IDLE:Effects.SALT_TIME_IDLE,1,entity.getX(),entity.getBoundingBox().maxY,entity.getZ()),entity.level());
            return false;
        }
        if (entity.getAge() < 100) {
            if (entity.tickCount % 30 == 1) level.playSound(null, entity.blockPosition(), thaumcraft.registry.ModSounds.WIND.get(), SoundSource.PLAYERS, 0.1F, 1 + level.random.nextFloat() * 0.5F);
            return false;
        }
        if (kind.equals("salis_tempestas")) {
            boolean rain = !level.isRaining();
            level.setWeatherParameters(rain ? 0 : 24000, rain ? 24000 : 0, rain, rain && level.random.nextInt(10) == 0);
        } else {
            long time = level.getDayTime();
            long next = Math.floorDiv(time, 24000) * 24000 + (level.isDay() ? 14000 : 24000);
            level.setDayTime(next > time ? next : next + 24000);
        }
        Effects.send(level, kind.equals("salis_tempestas")?Effects.SALT_WEATHER_BURST:Effects.SALT_TIME_BURST, 200, entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
        level.playSound(null, entity.blockPosition(), thaumcraft.registry.ModSounds.ICE.get(), SoundSource.PLAYERS, 0.3F, 1 + level.random.nextFloat() * 0.25F);
        entity.discard();
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        if (kind.equals("gate_key")) {
            var tag = Data.tag(stack);
            lines.add(Component.translatable(tag.getBoolean("bound") ? "key.bound" : "key.unbound"));
            if (tag.getBoolean("bound")) {
                lines.add(Component.literal(tag.getString("dimension")));
                lines.add(Component.literal(BlockPos.of(tag.getLong("position")).toShortString()));
            }
        }
    }
}
