package nl.enjarai.doahackedroll.net;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.MathHelper;
import nl.enjarai.doahackedroll.DoAHackedRoll;
import nl.enjarai.doahackedroll.api.RollEntity;

public class RollSyncServer {
    public static void startListening(ServerPlayNetworkHandler handler) {
        ServerPlayNetworking.registerReceiver(handler, DoAHackedRoll.ROLL_CHANNEL, (server, player, handler1, buf, responseSender) -> {
            var rollPlayer = (RollEntity) player;

            var isRolling = buf.readBoolean();
            var roll = buf.readFloat();

            rollPlayer.doABarrelRoll$setRolling(isRolling);
            rollPlayer.doABarrelRoll$setRoll(isRolling ? MathHelper.wrapDegrees(roll) : 0);
        });
    }

    public static void sendUpdates(Entity entity) {
        var rollEntity = (RollEntity) entity;
        var isRolling = rollEntity.doABarrelRoll$isRolling();
        var roll = rollEntity.doABarrelRoll$getRoll();

        var buf = PacketByteBufs.create();
        buf.writeInt(entity.getId());
        buf.writeBoolean(isRolling);
        buf.writeFloat(roll);

        PlayerLookup.tracking(entity).stream()
                .filter(player -> DoAHackedRoll.HANDSHAKE_SERVER.getHandshakeState(player).state == HandshakeServer.HandshakeState.ACCEPTED)
                .filter(player -> player != entity)
                .forEach(player -> ServerPlayNetworking.send(player, DoAHackedRoll.ROLL_CHANNEL, buf));
    }
}
