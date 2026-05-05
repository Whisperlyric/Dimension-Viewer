package dev.stick_stack.dimensionviewer;

import java.util.Locale;
import net.minecraft.resources.Identifier;

public class CommonUtils {

    public enum DimensionPosition {
        PREPEND,
        APPEND
    }

    public static String toTitleCase(String inputString) {
        inputString = inputString.replace("_", " ");

        if (inputString.length() <= 2 || (inputString.startsWith(" ") || inputString.endsWith(" "))) {
            inputString = inputString.toUpperCase(Locale.ROOT);
        } else {
            String[] splitString = inputString.split(" ");
            StringBuilder builder = new StringBuilder();
            int count = 0;

            for (var sub : splitString) {
                splitString[count] = (Character.toUpperCase(sub.charAt(0)) + sub.substring(1));
                builder.append(splitString[count]);

                if (count != splitString.length - 1) builder.append(" ");
                count++;
            }

            inputString = builder.toString();
        }

        return inputString;
    }

    public static String splitResourceLocation(Identifier key, int pos) {
        final String txt = key.toString();
        return splitResourceLocation(txt, pos);
    }

    public static String splitResourceLocation(String key, int pos) {
        return key.split(":")[pos];
    }

    public static String dimensionToString(Identifier key) {
        return CommonUtils.toTitleCase(CommonUtils.splitResourceLocation(key, 1));
    }

    public static String dimensionToString(String key) {
        return CommonUtils.toTitleCase(CommonUtils.splitResourceLocation(key, 1));
    }

    public static int rgbToInt(int r, int g, int b) {
        return ((r & 0x0ff) << 16) | ((g & 0x0ff) << 8) | (b & 0x0ff);
    }

    public static int hexToInt(String hex) {
        int r = Integer.parseInt(hex.substring(1, 3), 16);
        int g = Integer.parseInt(hex.substring(3, 5), 16);
        int b = Integer.parseInt(hex.substring(5, 7), 16);

        return rgbToInt(r, g, b);
    }

    public static int customColorToInt(String[] value) {
        if (value[1].startsWith("#")) {
            return hexToInt(value[1]);
        } else {
            int r = Integer.parseInt(value[1].substring(1));
            int g = Integer.parseInt(value[2].substring(1));
            int b = Integer.parseInt(value[3].substring(1));

            return rgbToInt(r, g, b);
        }
    }
}
