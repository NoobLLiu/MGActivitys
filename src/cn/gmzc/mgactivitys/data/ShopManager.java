package cn.gmzc.mgactivitys.data;

import cn.gmzc.mgactivitys.model.ShopItem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShopManager {
    private static final String RETIRED_LEGACY_ITEM_TYPE = "pa:yeshi";
    private static final String RETIRED_LEGACY_ITEM_NAME = "\u591c\u89c6\u773c\u955c";
    private static final Map<String, Integer> DISPLAY_ORDER = Map.ofEntries(
            Map.entry("bread", 0),
            Map.entry("arrow", 10),
            Map.entry("firework_rocket", 20),
            Map.entry("diamond", 30),
            Map.entry("golden_apple", 40),
            Map.entry("netherite_upgrade_smithing_template", 50),
            Map.entry("shulker_shell", 60),
            Map.entry("enchanted_golden_apple", 70),
            Map.entry("elytra", 80)
    );

    private final File dataFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Logger logger;
    private ShopData shopData;

    static class ShopData {
        List<ShopItem> items = new ArrayList<>();
        String lastResetDate = "";

        public List<ShopItem> getItems() {
            return items;
        }

        public void setItems(List<ShopItem> items) {
            this.items = items;
        }

        public String getLastResetDate() {
            return lastResetDate;
        }

        public void setLastResetDate(String lastResetDate) {
            this.lastResetDate = lastResetDate;
        }
    }

    public ShopManager(File dataFolder, Logger logger) {
        this.dataFile = new File(dataFolder, "shopdata.json");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.shopData = new ShopData();
    }

    public void load() {
        if (dataFile.exists()) {
            try (FileReader reader = new FileReader(dataFile)) {
                Type type = new TypeToken<ShopData>() {}.getType();
                ShopData loaded = gson.fromJson(reader, type);
                if (loaded != null) {
                    this.shopData = loaded;
                }
            } catch (IOException e) {
                this.shopData = new ShopData();
            }
        } else {
            this.shopData = new ShopData();
        }

        if (shopData.items == null) {
            shopData.items = new ArrayList<>();
        }

        int beforeCount = shopData.items.size();
        shopData.items.removeIf(ShopManager::isRetiredLegacyItem);
        if (shopData.items.size() != beforeCount) {
            save();
        }
    }

    private static boolean isRetiredLegacyItem(ShopItem item) {
        return item != null
            && (RETIRED_LEGACY_ITEM_TYPE.equalsIgnoreCase(item.getType())
                || RETIRED_LEGACY_ITEM_NAME.equals(item.getName()));
    }

    public void save() {
        try (FileWriter writer = new FileWriter(dataFile)) {
            gson.toJson(shopData, writer);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Unable to save activity shop data: " + dataFile, e);
        }
    }

    public List<ShopItem> getItems() {
        return shopData.items;
    }

    public List<ShopItem> getDisplayItems() {
        List<ShopItem> items = new ArrayList<>(shopData.items);
        items.sort(Comparator
                .comparingInt(ShopManager::displayOrder)
                .thenComparing(item -> safeText(item.getName()), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(item -> normalizeType(item.getType()), String.CASE_INSENSITIVE_ORDER));
        return items;
    }

    public ShopItem getItem(int index) {
        return shopData.items.get(index);
    }

    public ShopItem getDisplayItem(int index) {
        return getDisplayItems().get(index);
    }

    public static int requiredLevelForIndex(int index) {
        return Math.max(0, index) * 10;
    }

    public static boolean isUnlocked(int index, int playerLevel) {
        return index >= 0 && playerLevel >= requiredLevelForIndex(index);
    }

    public static String getRestockCountdown() {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime nextRestock = now.toLocalDate().plusDays(1).atStartOfDay(now.getZone());
        long seconds = Math.max(0, Duration.between(now, nextRestock).getSeconds());
        if (seconds < 60) {
            return "\u5c11\u4e8e1\u5206\u949f";
        }

        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        StringBuilder result = new StringBuilder();
        if (days > 0) {
            result.append(days).append("\u5929");
        }
        if (hours > 0) {
            result.append(hours).append("\u5c0f\u65f6");
        }
        if (minutes > 0 || result.length() == 0) {
            result.append(minutes).append("\u5206\u949f");
        }
        return result.toString();
    }

    public void addItem(ShopItem item) {
        shopData.items.add(item);
        save();
    }

    public void removeItem(int index) {
        shopData.items.remove(index);
        save();
    }

    public void checkDailyReset() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd yyyy", Locale.US);
        String today = sdf.format(new Date());

        if (!today.equals(shopData.lastResetDate)) {
            for (ShopItem item : shopData.items) {
                item.setPurchasedToday(0);
            }
            shopData.lastResetDate = today;
            save();
        }
    }

    private static int displayOrder(ShopItem item) {
        return DISPLAY_ORDER.getOrDefault(normalizeType(item.getType()), 1000);
    }

    private static String normalizeType(String type) {
        if (type == null) {
            return "";
        }
        String normalized = type.toLowerCase(Locale.ROOT);
        return normalized.startsWith("minecraft:") ? normalized.substring("minecraft:".length()) : normalized;
    }

    private static String safeText(String value) {
        return value == null ? "" : value;
    }
}
