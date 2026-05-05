package dev.stick_stack.dimensionviewer;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public abstract class PlayerListHandler {

    public static final List<Player> playerList = new ArrayList<>();

    public MutableComponent makeDimensionComponent(Player player, String format) {
        Identifier dimension = player.level().dimension().identifier();

        return extractTokensFromFormat(format, dimension);
    }

    private MutableComponent extractTokensFromFormat(String format, Identifier dimension) {
        Style style = checkTokens(null, format);
        format = replaceTokens(format);

        String aliasedDim = checkForAliases(dimension.toString());
        style = checkTokens(style, aliasedDim);
        aliasedDim = replaceTokens(aliasedDim);

        aliasedDim = aliasedDim.replace("%d", CommonUtils.toTitleCase(
                CommonUtils.splitResourceLocation(dimension, 1)
        ));

        format = format.replace("%d", aliasedDim);
        return MutableComponent.create(new PlainTextContents.LiteralContents(format)).setStyle(style);
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
                .withUnderlined(inStyle.isUnderlined() || useUnderline)
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
