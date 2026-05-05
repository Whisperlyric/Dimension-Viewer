package dev.stick_stack.dimensionviewer.platform.services;

import dev.stick_stack.dimensionviewer.CommonUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.server.players.PlayerList;

public interface IConfigHelper {

    String BaseDefaultColor();
    String BaseOverworldColor();
    String BaseNetherColor();
    String BaseEndColor();
    String BaseListFormat();

    String DefaultColor();
    String OverworldColor();
    String NetherColor();
    String EndColor();
    String ListFormat();

    List<String> GetAllCustomColors();
    void AddCustomColor(String name, String color);
    boolean RemoveCustomColor(String name);

    @Nullable String GetAlias(String dimId);
    @Nullable String GetCustomColor(String dimId);

    void SetAlias(String dimId, String alias);
    void SetColor(String dimId, String color);
    void SetBoolSetting(String setting, boolean value);

    void ResetAlias(String dimId);
    void ResetColor(String dimId);

    void SetFormat(String format);
    void SetPlacement(CommonUtils.DimensionPosition position);
    void SetPosition(String position);

    void RemoveColor(String dimId);
    void RemoveAlias(String dimId);

    void Reload();

    void RefreshPlayerData(PlayerList players);

    boolean HasAlias(String dimId);
}
