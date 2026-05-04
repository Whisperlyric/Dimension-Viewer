package dev.stick_stack.dimensionviewer;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.PlainTextContent;
import net.minecraft.util.Identifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class PlayerListHandler {

    public static final List<PlayerEntity> playerList = new ArrayList<>();

    public MutableText makeDimensionComponent(PlayerEntity player, String format) {
        Identifier dimension = player.getEntityWorld().getRegistryKey().getValue();

        return extractTokensFromFormat(format, dimension);
    }

    private MutableText extractTokensFromFormat(String format, Identifier dimension) {
        Style style = checkTokens(null, format);
        format = replaceTokens(format);

        String aliasedDim = checkForAliases(dimension.toString());
        style = checkTokens(style, aliasedDim);
        aliasedDim = replaceTokens(aliasedDim);

        aliasedDim = aliasedDim.replace("%d", CommonUtils.toTitleCase(
                CommonUtils.splitResourceLocation(dimension, 1)
        ));

        format = format.replace("%d", aliasedDim);
        return MutableText.of(new PlainTextContent.Literal(format)).setStyle(style);
    }


    private Style checkTokens(@Nullable Style inStyle, String inString) {
        inStyle = inStyle == null ? Style.EMPTY : inStyle;

        boolean useItalic = inString.contains("%i");
        boolean useBold = inString.contains("%b");
        boolean useUnderline = inString.contains("%u");
        boolean useStrikethrough = inString.contains("%s");
        boolean useObfuscate = inString.contains("%o");

        inStyle = inStyle
                .withItalic(inStyle.isItalic() || useItalic)
                .withBold(inStyle.isBold() || useBold)
                .withUnderline(inStyle.isUnderlined() || useUnderline)
                .withStrikethrough(inStyle.isStrikethrough() || useStrikethrough)
                .withObfuscated(inStyle.isObfuscated() || useObfuscate);

        return inStyle;
    }

    private String replaceTokens(String inString) {
        for (String token : new String[] {"%i", "%b", "%u", "%s", "%o"}) {
            inString = inString.replace(token, "");
        }

        inString = inString.replaceAll("%[^%d].*?", "");

        inString = inString.replace("%%", "%");

        return inString;
    }

    public abstract String checkForAliases(String dimension);

}
