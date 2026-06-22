package dev.stick_stack.dimensionviewer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import java.util.EnumSet;

public class DimensionViewerFabric implements ModInitializer {

    @Override
    public void onInitialize() {
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

        ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, origin, destination) -> {
            refreshDisplayNames(player);
        });

        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> CustomCommands.RegisterCommands(dispatcher)));
    }

    private static void refreshDisplayNames(ServerPlayer player) {
        if (player != null && player.level() != null && player.level().getServer() != null) {
            PlayerList playerManager = player.level().getServer().getPlayerList();
            EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions = EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME);
            playerManager.broadcastAll(new ClientboundPlayerInfoUpdatePacket(actions, playerManager.getPlayers()));
        }
    }
}
