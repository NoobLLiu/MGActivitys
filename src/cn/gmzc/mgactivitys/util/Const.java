package cn.gmzc.mgactivitys.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

public class Const {
    public static final String PREFIX = "\u00a77[\u00a76\u6210\u957f\u503c\u00a77]\u00a7r ";

    public static String formatActivity(double activity) {
        if (!Double.isFinite(activity)) {
            return "0.0";
        }
        return BigDecimal.valueOf(activity)
                .setScale(1, RoundingMode.HALF_UP)
                .toPlainString();
    }

    public static final List<String> FOOD_ITEMS = Arrays.asList(
        "APPLE", "BAKED_POTATO", "BEEF", "BEETROOT", "BEETROOT_SOUP", "BREAD",
        "CARROT", "CHICKEN", "CHORUS_FRUIT", "COOKED_BEEF", "COOKED_CHICKEN", "COOKED_COD",
        "COOKED_MUTTON", "COOKED_PORKCHOP", "COOKED_RABBIT", "COOKED_SALMON", "COOKIE", "DRIED_KELP",
        "ENCHANTED_GOLDEN_APPLE", "GOLDEN_APPLE", "GOLDEN_CARROT", "HONEY_BOTTLE", "MELON_SLICE",
        "MUSHROOM_STEW", "MUTTON", "PORKCHOP", "PUFFERFISH", "PUMPKIN_PIE", "RABBIT", "RABBIT_STEW",
        "ROTTEN_FLESH", "SALMON", "SPIDER_EYE", "SUSPICIOUS_STEW", "SWEET_BERRIES", "TROPICAL_FISH"
    );
}
