package dev.stick_stack.dimensionviewer;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.EnumSet;

public class DimensionViewerFabric implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTED.register((server -> ConfigFabric.get()));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            PlayerListHandler.playerList.add(handler.getPlayer());
            refreshDisplayNames(handler.getPlayer());
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                PlayerListHandler.playerList.remove(handler.getPlayer())
        );

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, isAlive) -> {
            refreshDisplayNames(newPlayer);
        });

        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            refreshDisplayNames(player);
        });

        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> CustomCommands.RegisterCommands(dispatcher)));
    }

    private static void refreshDisplayNames(ServerPlayerEntity player) {
        if (player != null && player.getEntityWorld() != null && player.getEntityWorld().getServer() != null) {
            PlayerManager playerManager = player.getEntityWorld().getServer().getPlayerManager();
            EnumSet<PlayerListS2CPacket.Action> actions = EnumSet.of(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME);
            playerManager.sendToAll(new PlayerListS2CPacket(actions, playerManager.getPlayerList()));
        }
    }
}
