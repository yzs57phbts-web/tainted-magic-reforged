package taintedmagic;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public record Sparkles(int color, int count, double x, double y, double z) implements CustomPacketPayload {
    public static final Type<Sparkles> TYPE = new Type<>(TaintedMagic.id("sparkles"));
    public static final StreamCodec<ByteBuf, Sparkles> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, Sparkles::color, ByteBufCodecs.VAR_INT, Sparkles::count, ByteBufCodecs.DOUBLE, Sparkles::x, ByteBufCodecs.DOUBLE, Sparkles::y, ByteBufCodecs.DOUBLE, Sparkles::z, Sparkles::new);
    @Override public Type<Sparkles> type() { return TYPE; }
    public static void send(ServerLevel level, double x, double y, double z, int color, int count) { PacketDistributor.sendToPlayersNear(level, null, x, y, z, 64, new Sparkles(color, count, x, y, z)); }
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(TYPE, CODEC, (payload, context) -> {
            var level = context.player().level();
            for (int i = 0; i < Math.clamp(payload.count, 0, 600); i++) thaumcraft.fx.Fx.get().sparkle(level, (float)(payload.x + level.random.nextGaussian() * 0.25), (float)(payload.y + level.random.nextGaussian() * 0.25), (float)(payload.z + level.random.nextGaussian() * 0.25), 1.75F, payload.color, 0.1F);
        });
    }
}
