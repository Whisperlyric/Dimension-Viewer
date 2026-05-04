package dev.stick_stack.dimensionviewer;

import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.PlayerManager;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.EnumSet;

public class FabricUtils {

    public static void refreshDisplayNames(PlayerManager players) {
        EnumSet<PlayerListS2CPacket.Action> actions = EnumSet.of(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME);
        players.sendToAll(new PlayerListS2CPacket(actions, players.getPlayerList()));
    }


    public static String generateMd5Hash(Path path) {
        try {
            byte[] hash = MessageDigest.getInstance("MD5").digest(Files.readAllBytes(path));
            return new BigInteger(1, hash).toString(16);
        } catch (NoSuchAlgorithmException | IOException exception) {
            Constants.LOG.error(exception.toString());
            Constants.LOG.error(exception.getMessage());
        }

        return null;
    }

}
