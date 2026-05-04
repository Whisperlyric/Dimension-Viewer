package dev.stick_stack.dimensionviewer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class ConfigFabric {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static ConfigFabric INSTANCE;
    private static boolean isFileWatcherRunning = false;

    @Expose
    @SerializedName("listFormat")
    public String LIST_FORMAT = ConfigCommon.DEFAULT_LIST_FORMAT;

    @Expose
    @SerializedName("dimensionPosition")
    public CommonUtils.DimensionPosition DIM_POSITION = CommonUtils.DimensionPosition.APPEND;

    @Expose
    @SerializedName("defaultColor")
    public String DEFAULT_COLOR = ConfigCommon.DEFAULT_COLOR;

    @Expose
    @SerializedName("overworldColor")
    public String OVERWORLD_COLOR = ConfigCommon.OVERWORLD_COLOR;

    @Expose
    @SerializedName("netherColor")
    public String NETHER_COLOR = ConfigCommon.NETHER_COLOR;

    @Expose
    @SerializedName("endColor")
    public String END_COLOR = ConfigCommon.END_COLOR;

    @Expose
    @SerializedName("perDimColor")
    public boolean PER_DIM_COLOR = ConfigCommon.PER_DIM_COLOR;

    @Expose
    @SerializedName("dimInChatName")
    public boolean DIM_IN_CHAT_NAME = ConfigCommon.DIM_IN_CHAT_NAME;

    @Expose
    @SerializedName("chatDimHover")
    public boolean CHAT_DIM_HOVER = ConfigCommon.CHAT_DIM_HOVER;

    @Expose
    @SerializedName("enableAliases")
    public boolean ENABLE_ALIASES = ConfigCommon.ENABLE_ALIASES;

    @Expose
    @SerializedName("moddedDimensions")
    public List<String> MODDED_DIMS = new ArrayList<>();

    @Expose
    @SerializedName("dimensionAliases")
    public List<String> DIM_ALIASES = new ArrayList<>();

    @Expose
    @SerializedName("customColors")
    public List<String> CUSTOM_COLORS = new ArrayList<>();

    @NotNull
    public static ConfigFabric get() {
        if (INSTANCE == null) {
            loadConfig();
        }

        return INSTANCE;
    }

    @Override
    public String toString() {
        return "List Format: %s\n".formatted(LIST_FORMAT) +
                "Dimension Position: %s\n".formatted(DIM_POSITION.toString()) +
                "Default Color: %s\n".formatted(DEFAULT_COLOR) +
                "Overworld Color: %s\n".formatted(OVERWORLD_COLOR) +
                "Nether Color: %s\n".formatted(NETHER_COLOR) +
                "End Color: %s\n".formatted(END_COLOR) +
                "Per Dimension Colors: %s\n".formatted(PER_DIM_COLOR) +
                "Dimension in chat name: %s\n".formatted(DIM_IN_CHAT_NAME) +
                "Chat Hover: %s\n".formatted(CHAT_DIM_HOVER) +
                "Enable Aliases: %s\n".formatted(ENABLE_ALIASES) +
                "Modded Dimensions: %s\n".formatted(MODDED_DIMS.toString()) +
                "Dimension Aliases: %s\n".formatted(DIM_ALIASES.toString()) +
                "Custom Colors: %s".formatted(CUSTOM_COLORS.toString());
    }

    public static void loadConfig() {
        File file = getConfigFile();
        try (FileReader reader = new FileReader(file)) {
            INSTANCE = GSON.fromJson(reader, ConfigFabric.class);
        } catch (IOException exception) {
            Constants.LOG.warn("Failed to load Dimension Viewer config file. Regenerating...");
            INSTANCE = new ConfigFabric();
            saveConfig();
        } finally {

            if (!isFileWatcherRunning) {
                startFileWatcher();
                isFileWatcherRunning = true;
            }
        }
    }

    public static void saveConfig() {
        try (FileWriter writer = new FileWriter(getConfigFile())) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException exception) {
            Constants.LOG.error(exception.getMessage());
        }
    }

    private static File getConfigFile() {
        File file = new File("config", Constants.MOD_ID + ".json");

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdir();
        }

        return file;
    }

    private static void startFileWatcher() {
        if (isFileWatcherRunning) return;

        Thread thread = new Thread(() -> {
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                String checksum = "";
                Path configDir = getConfigFile().getParentFile().toPath();
                configDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

                while (true) {
                    WatchKey key = watchService.take();

                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();

                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                            continue;
                        }

                        if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                            Path changed = (Path) event.context();
                            if (changed.toString().endsWith(Constants.MOD_ID + ".json")) {
                                Path config = Path.of("config", changed.toString());

                                assert checksum != null;
                                if (checksum.equals(FabricUtils.generateMd5Hash(config))) {
                                    break;
                                }

                                checksum = FabricUtils.generateMd5Hash(config);

                                Constants.LOG.info("Dimension Viewer config file changed! Updating player info...");
                                loadConfig();

                                if (!PlayerListHandler.playerList.isEmpty()) {
                                    ServerPlayerEntity firstPlayer = (ServerPlayerEntity) PlayerListHandler.playerList.getFirst();
                                    FabricUtils.refreshDisplayNames(
                                            firstPlayer.getEntityWorld().getServer().getPlayerManager()
                                    );
                                } else {
                                    Constants.LOG.info("Skipping display name refresh as no players are detected on the server");
                                }

                                break;
                            }
                        }
                    }

                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                }
            } catch (IOException | InterruptedException | NullPointerException exception) {
                Constants.LOG.error(exception.getMessage());
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

}
