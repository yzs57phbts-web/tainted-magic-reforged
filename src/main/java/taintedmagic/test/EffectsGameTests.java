package taintedmagic.test;

import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;
import net.neoforged.neoforge.gametest.*;
import taintedmagic.*;
import thaumcraft.registry.ModSounds;

@GameTestHolder("taintedmagic") @PrefixGameTestTemplate(false)
public final class EffectsGameTests {
    private static List<PlayLevelSoundEvent> hear(GameTestHelper helper, Runnable action) {
        var sounds=new ArrayList<PlayLevelSoundEvent>();
        Consumer<PlayLevelSoundEvent> listener=event->{if(event.getLevel()==helper.getLevel())sounds.add(event);};
        NeoForge.EVENT_BUS.addListener(listener);
        try {action.run();} finally {NeoForge.EVENT_BUS.unregister(listener);}
        return sounds;
    }
    private static long count(List<PlayLevelSoundEvent> sounds, SoundEvent type) {
        return sounds.stream().filter(event->event.getSound()!=null && event.getSound().value()==type).count();
    }
    @GameTest(template="test/platform") public static void chargedStrikePlaysSwingAndCooldownDoesNot(GameTestHelper helper) {
        var player=thaumcraft.test.TestSupport.mockServerPlayer(helper);
        player.moveTo(helper.absolutePos(new BlockPos(2,2,2)).getCenter());
        var blade=new ItemStack(Content.item("thaumium_fortress_blade"));
        player.setItemInHand(InteractionHand.MAIN_HAND,blade);
        var item=(FortressBlade)blade.getItem();
        helper.assertTrue(count(hear(helper,()->item.releaseUsing(blade,helper.getLevel(),player,71980)),ModSounds.SWING.get())==1,"Charged swing missing/duplicated");
        Data.update(blade,tag->tag.putLong("cooldownEnd",helper.getLevel().getGameTime()+140));
        helper.assertTrue(hear(helper,()->item.releaseUsing(blade,helper.getLevel(),player,71980)).isEmpty(),"Cooldown played a successful strike");
        helper.succeed();
    }
    private static void hoe(GameTestHelper helper,String name) {
        var player=thaumcraft.test.TestSupport.mockServerPlayer(helper);
        player.setGameMode(GameType.SURVIVAL);
        var pos=helper.absolutePos(new BlockPos(3,2,3));
        player.moveTo(pos.getCenter().add(0,1,0));
        var stack=new ItemStack(Content.item(name));
        if(stack.getItem() instanceof Disassembler)Data.setInt(stack,"charge",50000);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        helper.getLevel().setBlockAndUpdate(pos,Blocks.DIRT.defaultBlockState());
        helper.getLevel().removeBlock(pos.above(),false);
        var context=new UseOnContext(player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));
        var sounds=hear(helper,()->stack.useOn(context));
        helper.assertTrue(helper.getLevel().getBlockState(pos).is(Blocks.FARMLAND),"Actual tool did not till dirt");
        helper.assertTrue(count(sounds,Blocks.DIRT.defaultBlockState().getSoundType().getStepSound())>=1,"Tilling material sound missing");
        helper.getLevel().setBlockAndUpdate(pos,Blocks.STONE.defaultBlockState());
        helper.assertTrue(hear(helper,()->stack.useOn(context)).isEmpty(),"Invalid tilling played sound");
        helper.succeed();
    }
    @GameTest(template="test/platform") public static void shadowmetalHoePlaysOriginalMaterialSound(GameTestHelper helper){hoe(helper,"shadowmetal_hoe");}
    @GameTest(template="test/platform") public static void disassemblerPlaysOriginalMaterialSound(GameTestHelper helper){hoe(helper,"thaumic_disassembler");}
    @GameTest(template="test/platform") public static void homingShardHasPeriodicFlightSoundAndExpires(GameTestHelper helper) {
        var player=thaumcraft.test.TestSupport.mockServerPlayer(helper);
        player.moveTo(helper.absolutePos(new BlockPos(2,3,2)).getCenter());
        var target=helper.spawn(EntityType.COW,new BlockPos(6,3,6)); target.setNoAi(true); target.setInvulnerable(true);
        var shard=MagicProjectile.create(helper.getLevel(),player,1,2,0,false,target,false);
        var sounds=hear(helper,()->{for(int i=0;i<20 && shard.isAlive();i++)shard.tick();});
        helper.assertTrue(count(sounds,Content.SHARD_SOUND.get())>=2,"Homing retarget ticks lost flight audio");
        target.discard(); shard.tick();
        helper.assertTrue(shard.isRemoved(),"Orphan shard did not terminate");
        helper.succeed();
    }
    @GameTest(template="test/platform") public static void diffusionDoesNotPlayDarkMatterFizz(GameTestHelper helper) {
        var player=thaumcraft.test.TestSupport.mockServerPlayer(helper);
        var projectile=MagicProjectile.create(helper.getLevel(),player,2,12,0,false,null,false);
        try {
            var hit=MagicProjectile.class.getDeclaredMethod("onHit",HitResult.class);hit.setAccessible(true);
            helper.assertTrue(hear(helper,()->{try{hit.invoke(projectile,new BlockHitResult(Vec3.ZERO,Direction.UP,BlockPos.ZERO,false));}catch(Exception e){throw new RuntimeException(e);}}).isEmpty(),"Diffusion acquired an impact sound absent from original");
            helper.assertTrue(projectile.isRemoved(),"Diffusion did not expire on block impact");
        }catch(ReflectiveOperationException e){throw new IllegalStateException(e);}
        helper.succeed();
    }
}
