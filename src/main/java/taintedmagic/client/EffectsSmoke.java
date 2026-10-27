package taintedmagic.client;

import java.util.*;
import java.util.function.Consumer;
import net.minecraft.client.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.sound.PlaySoundSourceEvent;
import taintedmagic.*;
import thaumcraft.registry.ModSounds;

/** Opt-in isolated client test: real payload delivery, world renderer and OpenAL channels. */
@EventBusSubscriber(modid=TaintedMagic.MODID,value=Dist.CLIENT)
public final class EffectsSmoke {
    private static final Set<Integer> received=new HashSet<>();
    private static final Set<String> channels=new HashSet<>();
    private static int captures;
    private static List<SoundEvent> sounds(){return List.of(Content.SHOCKWAVE_SOUND.get(),Content.SHARD_SOUND.get(),
        ModSounds.ICE.get(),ModSounds.CRAFTFAIL.get(),ModSounds.ELDRITCH_GUARDIAN_IDLE.get(),ModSounds.HEARTBEAT.get(),
        ModSounds.WAND.get(),ModSounds.WIND.get(),ModSounds.SWING.get(),ModSounds.BRAIN.get(),ModSounds.ROOTS.get(),
        ModSounds.ELDRITCH_GUARDIAN_ATTACK.get(),ModSounds.ZAP.get(),ModSounds.SWARM_ATTACK.get(),
        SoundEvents.GRAVEL_STEP,SoundEvents.ENDERMAN_TELEPORT,SoundEvents.FIRE_EXTINGUISH);}
    public static void received(Effects effect){received.add(effect.kind());System.out.println("TM_EFFECT_RECEIVED kind="+effect.kind()+" count="+effect.count());}
    @SubscribeEvent static void sound(PlaySoundSourceEvent event){
        if(Boolean.getBoolean("taintedmagic.effectsSmoke")){channels.add(event.getSound().getLocation().toString());System.out.println("TM_EFFECT_AUDIO_CHANNEL "+event.getSound().getLocation());}
    }
    private static void server(Consumer<ServerPlayer> action){
        var mc=Minecraft.getInstance();var server=mc.getSingleplayerServer();var id=mc.player.getUUID();
        server.execute(()->{var player=server.getPlayerList().getPlayer(id);if(player!=null)action.accept(player);});
    }
    private static void group(int... kinds){server(player->{
        for(int i=0;i<kinds.length;i++){
            int kind=kinds[i];int count=kind==Effects.WARPWOOD?15:kind==Effects.HEAL||kind==Effects.SHARD_CAST?18:
                kind==Effects.SALT_WEATHER_BURST||kind==Effects.SALT_TIME_BURST?200:kind==Effects.DIFFUSION?1:kind==Effects.SHOCK_SPARKS?5:9;
            Effects.send(player.serverLevel(),kind,count,player.getX()+(i-(kinds.length-1)/2.0)*1.6,101,player.getZ()-4);
        }
    });}
    private static void capture(String name){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,name+".png",mc.getMainRenderTarget(),message->captures++);}
    public static void step(int tick){
        var mc=Minecraft.getInstance();
        if(tick==35){mc.setScreen(null);mc.options.setCameraType(CameraType.FIRST_PERSON);}
        if(tick>=40 && tick<40+sounds().size()*4 && (tick-40)%4==0){
            int index=(tick-40)/4;server(player->player.serverLevel().playSound(null,player.blockPosition(),sounds().get(index),SoundSource.PLAYERS,0.6F,1));
        }
        if(tick==50)group(Effects.WARPWOOD,Effects.HEAL,Effects.SHARD_CAST);
        if(tick==53)capture("effects-01-wisps-heal-shard");
        if(tick==85)group(Effects.LUMOS_CAST,Effects.LUMOS_BREAK,Effects.GATE_BIND,Effects.GATE_CHARGE,Effects.LUMOS_IDLE);
        if(tick==88)capture("effects-02-lumos-key");
        if(tick==120)group(Effects.SALT_WEATHER_IDLE,Effects.SALT_TIME_IDLE,Effects.SALT_WEATHER_BURST,Effects.SALT_TIME_BURST);
        if(tick==125)capture("effects-03-salt-bursts");
        if(tick==155)group(Effects.DIFFUSION,Effects.SHOCK_SPARKS);
        if(tick==158)capture("effects-04-diffusion-shock");
        if(tick==175){
            for(int kind=0;kind<=Effects.LUMOS_IDLE;kind++)if(!received.contains(kind))throw new IllegalStateException("Effect payload missing "+kind);
            for(var sound:sounds())if(!channels.contains(sound.getLocation().toString()))throw new IllegalStateException("No actual sound channel "+sound.getLocation());
            if(captures<4)throw new IllegalStateException("Missing screenshots "+captures);
            System.out.println("TM_EFFECTS_CLIENT_PASS payloadKinds="+received.size()+" actualAudioChannels="+sounds().size()+" screenshots="+captures);
            mc.stop();
        }
        if(tick>250)throw new IllegalStateException("Effect client check stalled");
    }
}
