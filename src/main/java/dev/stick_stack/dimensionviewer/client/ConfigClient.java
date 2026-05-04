package dev.stick_stack.dimensionviewer.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import dev.stick_stack.dimensionviewer.CommonUtils;
import dev.stick_stack.dimensionviewer.Constants;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigClient {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static ConfigClient INSTANCE;

    @Expose
    @SerializedName("listFormat")
    public String LIST_FORMAT = "[%d]";

    @Expose
    @SerializedName("dimensionPosition")
    public CommonUtils.DimensionPosition DIM_POSITION = CommonUtils.DimensionPosition.APPEND;

    @Expose
    @SerializedName("defaultColor")
    public String DEFAULT_COLOR = "GOLD";

    @Expose
    @SerializedName("overworldColor")
    public String OVERWORLD_COLOR = "DARK_GREEN";

    @Expose
    @SerializedName("netherColor")
    public String NETHER_COLOR = "DARK_RED";

    @Expose
    @SerializedName("endColor")
    public String END_COLOR = "DARK_PURPLE";

    @Expose
    @SerializedName("perDimColor")
    public boolean PER_DIM_COLOR = true;

    @Expose
    @SerializedName("dimInChatName")
    public boolean DIM_IN_CHAT_NAME = false;

    @Expose
    @SerializedName("chatDimHover")
    public boolean CHAT_DIM_HOVER = true;

    @Expose
    @SerializedName("enableAliases")
    public boolean ENABLE_ALIASES = false;

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
    public static ConfigClient get() {
        if (INSTANCE == null) {
            loadConfig();
        }
        return INSTANCE;
    }

    public static void loadConfig() {
        File file = getConfigFile();
        try (FileReader reader = new FileReader(file)) {
            INSTANCE = GSON.fromJson(reader, ConfigClient.class);
        } catch (IOException exception) {
            Constants.LOG.warn("Failed to load Dimension Viewer client config file. Regenerating...");
            INSTANCE = new ConfigClient();
            saveConfig();
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
        File file = new File("config", Constants.MOD_ID + "_client.json");

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdir();
        }

        return file;
    }
}
