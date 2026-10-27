package taintedmagic;

import io.netty.buffer.ByteBuf;
import java.util.function.BiConsumer;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Distinct original effect shapes; common code never loads the client renderer on a server. */
public record Effects(int kind, int count, double x, double y, double z) implements CustomPacketPayload {
    public static final int WARPWOOD = 0, HEAL = 1, SHARD_CAST = 2, LUMOS_CAST = 3, LUMOS_BREAK = 4,
        GATE_BIND = 5, SALT_WEATHER_IDLE = 6, SALT_TIME_IDLE = 7, SALT_WEATHER_BURST = 8,
        SALT_TIME_BURST = 9, DIFFUSION = 10, SHOCK_SPARKS = 11, GATE_CHARGE = 12, LUMOS_IDLE = 13;
    public static final Type<Effects> TYPE = new Type<>(TaintedMagic.id("legacy_effect"));
    public static final StreamCodec<ByteBuf, Effects> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, Effects::kind, ByteBufCodecs.VAR_INT, Effects::count,
        ByteBufCodecs.DOUBLE, Effects::x, ByteBufCodecs.DOUBLE, Effects::y, ByteBufCodecs.DOUBLE, Effects::z, Effects::new);
    public static BiConsumer<Effects, Level> client = (effect, level) -> {};
    public static java.util.function.Consumer<net.minecraft.world.entity.Entity> bootTrail = entity -> {};
    @Override public Type<Effects> type() { return TYPE; }
    public static void send(ServerLevel level, int kind, int count, double x, double y, double z) {
        PacketDistributor.sendToPlayersNear(level, null, x, y, z, 64, new Effects(kind, count, x, y, z));
    }
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(TYPE, CODEC, (effect, context) -> client.accept(effect, context.player().level()));
    }
}
