package taintedmagic.client;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import taintedmagic.*;
import thaumcraft.client.fx.*;
import thaumcraft.client.platform.WorldFxRenderer;

@EventBusSubscriber(modid=TaintedMagic.MODID, value=Dist.CLIENT)
public final class LegacyEffects {
    @SubscribeEvent static void setup(FMLClientSetupEvent event) { event.enqueueWork(() -> { Effects.client = LegacyEffects::spawn; Effects.bootTrail = LegacyEffects::boots; }); }
    public static void spawn(Effects effect, Level level) {
        if (Boolean.getBoolean("taintedmagic.effectsSmoke")) EffectsSmoke.received(effect);
        var random = level.random;
        for (int i=0; i<Math.clamp(effect.count(), 0, 200); i++) {
            double x=effect.x(), y=effect.y(), z=effect.z();
            switch (effect.kind()) {
                case Effects.WARPWOOD -> {
                    double dx=(random.nextDouble()-random.nextDouble())*0.5, dy=(random.nextDouble()-random.nextDouble())*0.5, dz=(random.nextDouble()-random.nextDouble())*0.5;
                    var fx=new WispFx(random,x+dx,y+dy,z+dz,0.25F+random.nextFloat()*0.25F,5,null);
                    fx.gravity(0.01F).motion(dx*0.025,0,dz*0.025); WorldFxRenderer.add(fx);
                }
                case Effects.HEAL -> {
                    double px=x+random.nextGaussian()*0.25, py=y+random.nextGaussian()*0.5, pz=z+random.nextGaussian()*0.25;
                    WorldFxRenderer.add(new WispFx(random,px,py,pz,0.25F+random.nextFloat()*0.25F,3,null).gravity(0.02F));
                    WorldFxRenderer.add(new SparkleFx(random,px,py,pz,1,5,6));
                }
                case Effects.SALT_WEATHER_IDLE, Effects.SALT_TIME_IDLE, Effects.SALT_WEATHER_BURST, Effects.SALT_TIME_BURST -> {
                    boolean burst=effect.kind()==Effects.SALT_WEATHER_BURST || effect.kind()==Effects.SALT_TIME_BURST;
                    int color=effect.kind()==Effects.SALT_WEATHER_IDLE || effect.kind()==Effects.SALT_WEATHER_BURST ? 7 : 6;
                    double theta=random.nextDouble()*Math.PI, phi=random.nextDouble()*Math.PI*2;
                    double dx=Math.cos(phi)*Math.sin(theta)*0.25, dy=Math.sin(phi)*Math.sin(theta)*0.25, dz=Math.cos(theta)*0.25;
                    var fx=new SparkleFx(random,x+dx,y+dy,z+dz,1.75F,color,burst?30+random.nextInt(5):3+random.nextInt(2));
                    fx.gravity(burst?0:-0.1F); if(burst)fx.motion(dx*3,dy*3,dz*3); WorldFxRenderer.add(fx);
                }
                case Effects.DIFFUSION -> {
                    double dx=(random.nextFloat()-random.nextFloat())*0.3, dy=(random.nextFloat()-random.nextFloat())*0.3, dz=(random.nextFloat()-random.nextFloat())*0.3;
                    WorldFxRenderer.add(new WispFx(random,x+dx,y+dy,z+dz,0.3F,5,null).shrink(true).moveTo(x+dx*8,y+dy*8,z+dz*8).gravity(0.02F));
                }
                default -> {
                    int color=effect.kind()==Effects.SHARD_CAST?0:effect.kind()==Effects.SHOCK_SPARKS?2:6;
                    float size=effect.kind()==Effects.SHOCK_SPARKS?2+random.nextFloat():1.75F;
                    int life=3+random.nextInt(effect.kind()==Effects.LUMOS_IDLE?2:3);
                    double px=x+random.nextFloat(), py=y+random.nextFloat(), pz=z+random.nextFloat();
                    if(effect.kind()==Effects.GATE_BIND || effect.kind()==Effects.GATE_CHARGE){px=x+random.nextGaussian()*0.33;py=y+0.5+random.nextFloat();pz=z+random.nextGaussian()*0.33;}
                    if(effect.kind()==Effects.SHOCK_SPARKS){px=x+(random.nextFloat()-random.nextFloat())*0.6;py=y+(random.nextFloat()-random.nextFloat())*0.6;pz=z+(random.nextFloat()-random.nextFloat())*0.6;}
                    if(effect.kind()==Effects.LUMOS_IDLE){px=x+random.nextGaussian()*0.1;py=y+random.nextGaussian()*0.1;pz=z+random.nextGaussian()*0.1;}
                    var fx=new SparkleFx(random,px,py,pz,size,color,life);
                    fx.gravity(effect.kind()==Effects.LUMOS_IDLE?-0.5F:effect.kind()==Effects.LUMOS_BREAK?0.5F:effect.kind()==Effects.SHOCK_SPARKS?0.05F+random.nextFloat()*0.05F:0.1F);
                    if(effect.kind()==Effects.LUMOS_BREAK)fx.motion(random.nextGaussian()*0.1,random.nextGaussian()*0.1,random.nextGaussian()*0.1);
                    WorldFxRenderer.add(fx);
                }
            }
        }
    }
    public static void boots(Entity player) {
        var random=player.level().random;
        WorldFxRenderer.add(new WispFx(random,player.getX()+(random.nextDouble()-random.nextDouble())*0.5,
            player.getY()+0.05+(random.nextDouble()-random.nextDouble())*0.1,player.getZ()+(random.nextDouble()-random.nextDouble())*0.5,0.4F,5,null)
            .color(random.nextFloat()*0.05F,random.nextFloat()*0.05F,random.nextFloat()*0.05F)
            .motion(random.nextGaussian()*0.03,-0.05,random.nextGaussian()*0.03));
    }
}
