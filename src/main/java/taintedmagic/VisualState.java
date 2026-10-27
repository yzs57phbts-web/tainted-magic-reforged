package taintedmagic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public record VisualState(int player, int blade, boolean charm) implements CustomPacketPayload {
    public static final Type<VisualState> TYPE = new Type<>(TaintedMagic.id("visual_state"));
    public static final StreamCodec<ByteBuf, VisualState> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, VisualState::player, ByteBufCodecs.VAR_INT, VisualState::blade, ByteBufCodecs.BOOL, VisualState::charm, VisualState::new);
    public static final Map<Integer, VisualState> PLAYERS = new ConcurrentHashMap<>();
    @Override public Type<VisualState> type() { return TYPE; }
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(TYPE, CODEC, (payload, context) -> PLAYERS.put(payload.player, payload));
    }
}
