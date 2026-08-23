package cn.gmzc.mgactivitys.gui;

import cn.gmzc.mgactivitys.MGActivitysPlugin;
import cn.gmzc.mgactivitys.data.ActivityManager;
import cn.gmzc.mgactivitys.data.ConfigManager;
import cn.gmzc.mgactivitys.data.ShopManager;
import cn.gmzc.mgactivitys.model.ActivityData;
import cn.gmzc.mgactivitys.model.ListenerConfig;
import cn.gmzc.mgactivitys.model.ShopItem;
import cn.gmzc.mgactivitys.util.Const;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.response.CustomFormResponse;
import org.geysermc.cumulus.response.SimpleFormResponse;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BedrockForms {

    private static FloodgateApi getApi() {
        return FloodgateApi.getInstance();
    }

    public static void openAdminGUI(MGActivitysPlugin plugin, Player player) {
        try {
            ConfigManager configManager = plugin.getConfigManager();
            Map<String, ListenerConfig> listeners = configManager.getAllListeners();
            List<Map.Entry<String, ListenerConfig>> listenerList = new ArrayList<>(listeners.entrySet());

            SimpleForm.Builder builder = SimpleForm.builder()
                    .title("\u00a76\u6210\u957f\u503c\u63a7\u5236\u9762\u677f")
                    .content("\u00a7e\u9009\u62e9\u76d1\u542c\u5668\u8fdb\u884c\u914d\u7f6e\uff1a");

            for (Map.Entry<String, ListenerConfig> entry : listenerList) {
                ListenerConfig lc = entry.getValue();
                builder.button("\u00a7f" + lc.getName() + " " + (lc.isEnabled() ? "\u00a7a\u542f\u7528" : "\u00a7c\u7981\u7528")
                        + "\n\u00a71\u4e00\u6b21\u83b7\u5f97:" + lc.getMultiplier() + " \u6bcf\u65e5\u4e0a\u9650:" + lc.getDailyLimitDisplay());
            }

            builder.button("\u00a7e\u6bcf\u65e5\u524a\u51cf\u8bbe\u7f6e")
                   .button("\u00a76\u91cd\u7f6e\u6240\u6709\u6570\u636e");

            builder.validResultHandler(response -> {
                int id = response.clickedButtonId();
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (!player.isOnline()) return;
                    if (id < listenerList.size()) {
                        openListenerConfig(plugin, player, listenerList.get(id).getKey());
                    } else if (id == listenerList.size()) {
                        openDecayConfig(plugin, player);
                    } else {
                        openConfirmReset(plugin, player);
                    }
                });
            });

            getApi().sendForm(player.getUniqueId(), builder);
        } catch (Throwable t) {
            JavaMenus.openAdminGUI(plugin, player);
        }
    }

    public static void openRankGUI(MGActivitysPlugin plugin, Player player) {
        try {
            ActivityManager activityManager = plugin.getActivityManager();
            List<Map.Entry<String, ActivityData>> ranked =
                activityManager.getRankedPlayers(plugin::isRealPlayerName);

            SimpleForm.Builder builder = SimpleForm.builder()
                    .title("\u00a76\u6210\u957f\u503c\u6392\u884c\u699c");

            StringBuilder content = new StringBuilder("\u00a7e=== \u6210\u957f\u503c\u6392\u884c\u699c ===\n");
            int shown = Math.min(ranked.size(), 30);
            for (int i = 0; i < shown; i++) {
                Map.Entry<String, ActivityData> entry = ranked.get(i);
                String name = entry.getKey();
                double activity = entry.getValue().getTotalActivity();
                int rank = i + 1;
                String rankStr;
                if (rank == 1) rankStr = "\u00a76";
                else if (rank == 2) rankStr = "\u00a77";
                else if (rank == 3) rankStr = "\u00a7e";
                else rankStr = "\u00a7f";
                boolean isSelf = name.equalsIgnoreCase(player.getName());
                content.append(rankStr).append("#").append(rank).append(" \u00a7f").append(name);
                if (isSelf) content.append(" \u00a77(\u4f60)");
                content.append(": \u00a7a").append(Const.formatActivity(activity)).append("\n");
            }
            if (ranked.isEmpty()) {
                content.append("\u00a77\u6682\u65e0\u6570\u636e");
            }
            builder.content(content.toString());
            builder.button("\u00a7c\u5173\u95ed");

            getApi().sendForm(player.getUniqueId(), builder);
        } catch (Throwable t) {
            JavaMenus.openRankGUI(plugin, player);
        }
    }

    public static void openShopMain(MGActivitysPlugin plugin, Player player) {
        try {
            ShopManager shopManager = plugin.getShopManager();
            ActivityManager activityManager = plugin.getActivityManager();
            shopManager.checkDailyReset();

            List<ShopItem> items = shopManager.getDisplayItems();
            ActivityData data = activityManager.getPlayerData(player.getName());
            double dynamicActivity = data.getDynamicActivity();
            int playerLevel = plugin.getPlayerLevel(player);
            String restockCountdown = ShopManager.getRestockCountdown();

            SimpleForm.Builder builder = SimpleForm.builder()
                    .title("\u00a76\u6210\u957f\u5546\u5e97")
                    .content("\u00a7e\u4f60\u7684\u52a8\u6001\u6210\u957f\u503c: \u00a7a" + Const.formatActivity(dynamicActivity)
                            + "\n\u00a77\u5f53\u524d\u7b49\u7ea7: " + playerLevel);

            for (int i = 0; i < items.size(); i++) {
                ShopItem item = items.get(i);
                int remaining = item.getDailyLimit() - item.getPurchasedToday();
                if (ShopManager.isUnlocked(i, playerLevel)) {
                    builder.button("\u00a7f" + item.getName()
                            + "\n\u00a7e\u4ef7\u683c: " + item.getPrice() + " \u00a77| \u5269\u4f59: " + remaining + "/" + item.getDailyLimit()
                            + "\n\u00a7b\u8865\u8d27\u5012\u8ba1\u65f6: " + restockCountdown);
                } else {
                    builder.button("\u00a7c\u672a\u89e3\u9501\u5546\u54c1: " + item.getName()
                            + "\n\u00a7e\u5728\u73a9\u5bb6\u7b49\u7ea7\u8fbe\u5230" + ShopManager.requiredLevelForIndex(i) + "\u7ea7\u540e\u89e3\u9501");
                }
            }

            builder.button("\u00a7e\u8fd4\u56de\u4ea4\u6613\u5e02\u573a");

            if (player.isOp()) {
                builder.button("\u00a7c\u7f16\u8f91\u5546\u5e97");
            }

            builder.validResultHandler(response -> {
                int id = response.clickedButtonId();
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (!player.isOnline()) return;
                    if (id < items.size()) {
                        int currentLevel = plugin.getPlayerLevel(player);
                        if (!ShopManager.isUnlocked(id, currentLevel)) {
                            player.sendMessage(Const.PREFIX + "\u00a7c\u8be5\u5546\u54c1\u5c1a\u672a\u89e3\u9501\uff0c\u9700\u8981\u7b49\u7ea7\u8fbe\u5230"
                                    + ShopManager.requiredLevelForIndex(id) + "\u7ea7");
                            openShopMain(plugin, player);
                            return;
                        }
                        executePurchase(plugin, player, id, 1);
                        openShopMain(plugin, player);
                    } else if (id == items.size()) {
                        player.performCommand("se gui");
                    } else if (player.isOp() && id == items.size() + 1) {
                        openShopEdit(plugin, player);
                    }
                });
            });

            getApi().sendForm(player.getUniqueId(), builder);
        } catch (Throwable t) {
            JavaMenus.openShopMain(plugin, player);
        }
    }

    public static void openListenerConfig(MGActivitysPlugin plugin, Player player, String listenerKey) {
        try {
            ConfigManager configManager = plugin.getConfigManager();
            ListenerConfig config = configManager.getListenerConfig(listenerKey);
            if (config == null) {
                player.sendMessage(Const.PREFIX + "\u00a7c\u76d1\u542c\u5668\u4e0d\u5b58\u5728");
                openAdminGUI(plugin, player);
                return;
            }

            String statusLine = "\u00a7e\u5f53\u524d\u72b6\u6001: " + (config.isEnabled() ? "\u00a7a\u542f\u7528" : "\u00a7c\u7981\u7528")
                    + "  \u00a71\u4e00\u6b21\u83b7\u5f97:" + config.getMultiplier()
                    + "  \u00a71\u6bcf\u65e5\u4e0a\u9650:" + config.getDailyLimitDisplay();

            SimpleForm.Builder builder = SimpleForm.builder()
                    .title("\u00a76" + config.getName() + " \u914d\u7f6e")
                    .content(statusLine)
                    .button((config.isEnabled() ? "\u00a7c\u7981\u7528" : "\u00a7a\u542f\u7528") + " \u5f53\u524d\u76d1\u542c\u5668")
                    .button("\u00a7e\u4fee\u6539\u4e00\u6b21\u83b7\u5f97\u7684\u6210\u957f\u503c")
                    .button("\u00a7e\u4fee\u6539\u6bcf\u65e5\u4e0a\u9650")
                    .button("\u00a7e\u8fd4\u56de\u4e0a\u7ea7");

            builder.validResultHandler(response -> {
                int id = response.clickedButtonId();
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (!player.isOnline()) return;
                    if (id == 0) {
                        configManager.setListenerEnabled(listenerKey, !config.isEnabled());
                        openListenerConfig(plugin, player, listenerKey);
                    } else if (id == 1) {
                        openEditMultiplierForm(plugin, player, listenerKey);
                    } else if (id == 2) {
                        openEditLimitForm(plugin, player, listenerKey);
                    } else {
                        openAdminGUI(plugin, player);
                    }
                });
            });

            getApi().sendForm(player.getUniqueId(), builder);
        } catch (Throwable t) {
            JavaMenus.openListenerConfig(plugin, player, listenerKey);
        }
    }

    private static void openEditMultiplierForm(MGActivitysPlugin plugin, Player player, String listenerKey) {
        ConfigManager configManager = plugin.getConfigManager();
        ListenerConfig config = configManager.getListenerConfig(listenerKey);
        if (config == null) return;

        CustomForm.Builder builder = CustomForm.builder()
                .title("\u00a76\u4fee\u6539\u4e00\u6b21\u83b7\u5f97\u7684\u6210\u957f\u503c")
                .input("\u5f53\u524d\u503c: " + config.getMultiplier() + ", \u8bf7\u8f93\u5165" + multiplierRange(config),
                        String.valueOf(config.getMultiplier()), String.valueOf(config.getMultiplier()));

        builder.validResultHandler(response -> {
            String input = response.asInput(0);
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;
                try {
                    double val = Double.parseDouble(input.trim());
                    if (val < 0.1 || (config.hasDailyLimit() && val > config.getDailyLimit())) {
                        player.sendMessage(Const.PREFIX + "\u00a7c\u503c\u5fc5\u987b" + multiplierRange(config));
                    } else {
                        configManager.setListenerMultiplier(listenerKey, val);
                        player.sendMessage(Const.PREFIX + "\u00a7a\u5df2\u4fee\u6539\u4e3a: " + val);
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(Const.PREFIX + "\u00a7c\u8bf7\u8f93\u5165\u6709\u6548\u7684\u6570\u5b57");
                }
                openListenerConfig(plugin, player, listenerKey);
            });
        });
        builder.closedResultHandler(() -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) openListenerConfig(plugin, player, listenerKey);
            });
        });

        getApi().sendForm(player.getUniqueId(), builder);
    }

    private static String multiplierRange(ListenerConfig config) {
        return config.hasDailyLimit()
                ? "在 0.1~" + config.getDailyLimit() + " 之间"
                : "不低于 0.1";
    }

    private static boolean isValidDailyLimit(int dailyLimit) {
        return dailyLimit == -1 || (dailyLimit >= 1 && dailyLimit <= 10000);
    }

    private static void openEditLimitForm(MGActivitysPlugin plugin, Player player, String listenerKey) {
        ConfigManager configManager = plugin.getConfigManager();
        ListenerConfig config = configManager.getListenerConfig(listenerKey);
        if (config == null) return;

        CustomForm.Builder builder = CustomForm.builder()
                .title("\u00a76\u4fee\u6539\u6bcf\u65e5\u4e0a\u9650")
                .input("\u5f53\u524d\u4e0a\u9650: " + config.getDailyLimitDisplay() + ", \u8bf7\u8f93\u5165 -1(\u4e0d\u9650) \u6216 1~10000",
                        String.valueOf(config.getDailyLimit()), String.valueOf(config.getDailyLimit()));

        builder.validResultHandler(response -> {
            String input = response.asInput(0);
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;
                try {
                    int val = Integer.parseInt(input.trim());
                    if (!isValidDailyLimit(val)) {
                        player.sendMessage(Const.PREFIX + "\u00a7c\u4e0a\u9650\u5fc5\u987b\u4e3a -1(\u4e0d\u9650) \u6216 1~10000");
                    } else {
                        configManager.setListenerDailyLimit(listenerKey, val);
                        player.sendMessage(Const.PREFIX + "\u00a7a\u5df2\u4fee\u6539\u4e0a\u9650\u4e3a: "
                                + (val == -1 ? "\u4e0d\u9650" : val));
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(Const.PREFIX + "\u00a7c\u8bf7\u8f93\u5165\u6709\u6548\u7684\u6574\u6570");
                }
                openListenerConfig(plugin, player, listenerKey);
            });
        });
        builder.closedResultHandler(() -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) openListenerConfig(plugin, player, listenerKey);
            });
        });

        getApi().sendForm(player.getUniqueId(), builder);
    }

    public static void openDecayConfig(MGActivitysPlugin plugin, Player player) {
        try {
            ConfigManager configManager = plugin.getConfigManager();
            int mode = configManager.getDailyDecayMode();
            double amount = configManager.getDailyDecayAmount();
            String currentStr = (mode == 0 ? "\u767e\u5206\u6bd4" : "\u76f4\u63a5\u6263\u9664") + ": " + amount + (mode == 0 ? "%" : "");

            CustomForm.Builder builder = CustomForm.builder()
                    .title("\u00a76\u6bcf\u65e5\u6210\u957f\u503c\u524a\u51cf\u8bbe\u7f6e")
                    .toggle("\u542f\u7528\u6bcf\u65e5\u524a\u51cf", configManager.isDailyDecayEnabled())
                    .input("\u5f53\u524d: " + currentStr + "  \u8f93\u5165\u65b0\u503c (\u52a0% = \u767e\u5206\u6bd4\u6a21\u5f0f, \u4e0d\u52a0 = \u76f4\u63a5\u6263\u9664)",
                            String.valueOf(amount) + (mode == 0 ? "%" : ""),
                            String.valueOf(amount) + (mode == 0 ? "%" : ""));

            builder.validResultHandler(response -> {
                boolean enabled = response.asToggle(0);
                String rawInput = response.asInput(1);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (!player.isOnline()) return;
                    configManager.setDailyDecayEnabled(enabled);
                    String input = rawInput;
                    if (input == null || input.trim().isEmpty()) {
                        openAdminGUI(plugin, player);
                        return;
                    }
                    input = input.trim();
                    try {
                        int newMode;
                        double newAmount;
                        if (input.endsWith("%")) {
                            newMode = 0;
                            newAmount = Double.parseDouble(input.substring(0, input.length() - 1).trim());
                        } else {
                            newMode = 1;
                            newAmount = Double.parseDouble(input);
                        }
                        if (newAmount <= 0) {
                            player.sendMessage(Const.PREFIX + "\u00a7c\u6570\u503c\u5fc5\u987b\u5927\u4e8e0");
                        } else {
                            configManager.setDailyDecay(newMode, newAmount);
                            player.sendMessage(Const.PREFIX + "\u00a7a\u5df2\u66f4\u65b0\u524a\u51cf\u8bbe\u7f6e");
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(Const.PREFIX + "\u00a7c\u8bf7\u8f93\u5165\u6709\u6548\u7684\u6570\u5b57");
                    }
                    openAdminGUI(plugin, player);
                });
            });
            builder.closedResultHandler(() -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (player.isOnline()) openAdminGUI(plugin, player);
                });
            });

            getApi().sendForm(player.getUniqueId(), builder);
        } catch (Throwable t) {
            JavaMenus.openDecayConfig(plugin, player);
        }
    }

    public static void openConfirmReset(MGActivitysPlugin plugin, Player player) {
        try {
            SimpleForm.Builder builder = SimpleForm.builder()
                    .title("\u00a7c\u786e\u8ba4\u91cd\u7f6e")
                    .content("\u00a7e\u786e\u8ba4\u8981\u6e05\u9664\u6240\u6709\u73a9\u5bb6\u7684\u6210\u957f\u503c\u6570\u636e\u5417\uff1f\n\u00a7c\u6b64\u64cd\u4f5c\u4e0d\u53ef\u64a4\u9500\uff01")
                    .button("\u00a7a\u53d6\u6d88")
                    .button("\u00a7c\u786e\u8ba4\u91cd\u7f6e");

            builder.validResultHandler(response -> {
                int id = response.clickedButtonId();
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (!player.isOnline()) return;
                    if (id == 1) {
                        plugin.getActivityManager().resetAll();
                        player.sendMessage(Const.PREFIX + "\u00a7a\u5df2\u91cd\u7f6e\u6240\u6709\u6210\u957f\u503c\u6570\u636e");
                    }
                    openAdminGUI(plugin, player);
                });
            });

            getApi().sendForm(player.getUniqueId(), builder);
        } catch (Throwable t) {
            JavaMenus.openConfirmReset(plugin, player);
        }
    }

    public static void openPurchaseConfirm(MGActivitysPlugin plugin, Player player, int shopItemIndex) {
        try {
            ShopManager shopManager = plugin.getShopManager();
            ActivityManager activityManager = plugin.getActivityManager();
            shopManager.checkDailyReset();
            List<ShopItem> items = shopManager.getDisplayItems();

            if (shopItemIndex < 0 || shopItemIndex >= items.size()) {
                player.sendMessage(Const.PREFIX + "\u00a7c\u5546\u54c1\u4e0d\u5b58\u5728");
                openShopMain(plugin, player);
                return;
            }

            int playerLevel = plugin.getPlayerLevel(player);
            if (!ShopManager.isUnlocked(shopItemIndex, playerLevel)) {
                player.sendMessage(Const.PREFIX + "\u00a7c\u8be5\u5546\u54c1\u5c1a\u672a\u89e3\u9501\uff0c\u9700\u8981\u7b49\u7ea7\u8fbe\u5230"
                        + ShopManager.requiredLevelForIndex(shopItemIndex) + "\u7ea7");
                openShopMain(plugin, player);
                return;
            }

            ShopItem item = items.get(shopItemIndex);
            int remaining = item.getDailyLimit() - item.getPurchasedToday();
            ActivityData data = activityManager.getPlayerData(player.getName());
            double dynamicActivity = data.getDynamicActivity();

            CustomForm.Builder builder = CustomForm.builder()
                    .title("\u00a76\u8d2d\u4e70 - " + item.getName())
                    .input("\u00a7e\u4ef7\u683c: " + item.getPrice() + "  \u00a77\u5269\u4f59: " + remaining + "/" + item.getDailyLimit()
                            + "  \u00a7b\u8865\u8d27\u5012\u8ba1\u65f6: " + ShopManager.getRestockCountdown()
                            + "  \u00a7a\u4f60\u7684\u6210\u957f\u503c: " + Const.formatActivity(dynamicActivity)
                            + "\n\u00a7e\u8bf7\u8f93\u5165\u8d2d\u4e70\u6570\u91cf", "1", "1");

            builder.validResultHandler(response -> {
                String input = response.asInput(0);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (!player.isOnline()) return;
                    try {
                        int quantity = Integer.parseInt(input.trim());
                        executePurchase(plugin, player, shopItemIndex, quantity);
                    } catch (NumberFormatException e) {
                        player.sendMessage(Const.PREFIX + "\u00a7c\u8bf7\u8f93\u5165\u6709\u6548\u7684\u6574\u6570");
                    }
                    openShopMain(plugin, player);
                });
            });
            builder.closedResultHandler(() -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (player.isOnline()) openShopMain(plugin, player);
                });
            });

            getApi().sendForm(player.getUniqueId(), builder);
        } catch (Throwable t) {
            JavaMenus.openPurchaseConfirm(plugin, player, shopItemIndex);
        }
    }

    public static void openShopEdit(MGActivitysPlugin plugin, Player player) {
        try {
            SimpleForm.Builder builder = SimpleForm.builder()
                    .title("\u00a76\u7f16\u8f91\u5546\u5e97")
                    .content("\u00a7e\u9009\u62e9\u64cd\u4f5c\uff1a")
                    .button("\u00a7a\u65b0\u589e\u7269\u54c1")
                    .button("\u00a7c\u4e0b\u67b6\u7269\u54c1")
                    .button("\u00a7e\u8fd4\u56de");

            builder.validResultHandler(response -> {
                int id = response.clickedButtonId();
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (!player.isOnline()) return;
                    if (id == 0) {
                        openShopAdd(plugin, player);
                    } else if (id == 1) {
                        openShopRemove(plugin, player);
                    } else {
                        openShopMain(plugin, player);
                    }
                });
            });

            getApi().sendForm(player.getUniqueId(), builder);
        } catch (Throwable t) {
            JavaMenus.openShopEdit(plugin, player);
        }
    }

    public static void openShopAdd(MGActivitysPlugin plugin, Player player) {
        try {
            ItemStack held = player.getInventory().getItemInMainHand();
            if (held == null || held.getType() == Material.AIR) {
                player.sendMessage(Const.PREFIX + "\u00a7c\u8bf7\u624b\u6301\u8981\u6dfb\u52a0\u7684\u7269\u54c1\u518d\u4f7f\u7528\u6b64\u529f\u80fd");
                openShopEdit(plugin, player);
                return;
            }

            String typeStr = held.getType().name();
            String displayName = held.hasItemMeta() && held.getItemMeta().hasDisplayName()
                    ? held.getItemMeta().getDisplayName() : typeStr;

            CustomForm.Builder builder = CustomForm.builder()
                    .title("\u00a76\u65b0\u589e\u5546\u54c1")
                    .label("\u00a7e\u624b\u6301\u7269\u54c1: \u00a7f" + displayName)
                    .input("\u00a7e\u8bf7\u8f93\u5165\u5355\u4ef7", "\u4f8b: 100", "")
                    .input("\u00a7e\u8bf7\u8f93\u5165\u6bcf\u65e5\u9650\u8d2d\u6570\u91cf", "\u4f8b: 10", "");

            builder.validResultHandler(response -> {
                String priceStr = response.asInput(0);
                String limitStr = response.asInput(1);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (!player.isOnline()) return;
                    try {
                        double price = Double.parseDouble(priceStr.trim());
                        if (price <= 0) {
                            player.sendMessage(Const.PREFIX + "\u00a7c\u4ef7\u683c\u5fc5\u987b\u5927\u4e8e0");
                            openShopEdit(plugin, player);
                            return;
                        }
                        try {
                            int limit = Integer.parseInt(limitStr.trim());
                            if (limit < 1) {
                                player.sendMessage(Const.PREFIX + "\u00a7c\u6bcf\u65e5\u9650\u8d2d\u6570\u91cf\u5fc5\u987b\u5927\u4e8e0");
                                openShopEdit(plugin, player);
                                return;
                            }
                            plugin.getShopManager().addItem(new ShopItem(typeStr, displayName, price, limit, 0));
                            player.sendMessage(Const.PREFIX + "\u00a7a\u5df2\u6dfb\u52a0\u5546\u54c1: " + displayName);
                        } catch (NumberFormatException e) {
                            player.sendMessage(Const.PREFIX + "\u00a7c\u8bf7\u8f93\u5165\u6709\u6548\u7684\u6574\u6570");
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(Const.PREFIX + "\u00a7c\u8bf7\u8f93\u5165\u6709\u6548\u7684\u6570\u5b57");
                    }
                    openShopEdit(plugin, player);
                });
            });
            builder.closedResultHandler(() -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (player.isOnline()) openShopEdit(plugin, player);
                });
            });

            getApi().sendForm(player.getUniqueId(), builder);
        } catch (Throwable t) {
            JavaMenus.openShopAdd(plugin, player);
        }
    }

    public static void openShopRemove(MGActivitysPlugin plugin, Player player) {
        try {
            ShopManager shopManager = plugin.getShopManager();
            List<ShopItem> items = shopManager.getItems();

            SimpleForm.Builder builder = SimpleForm.builder()
                    .title("\u00a76\u4e0b\u67b6\u5546\u54c1")
                    .content("\u00a7e\u70b9\u51fb\u5546\u54c1\u5373\u53ef\u4e0b\u67b6\uff1a");

            for (int i = 0; i < items.size(); i++) {
                ShopItem item = items.get(i);
                builder.button("\u00a7f" + item.getName() + "  \u00a7e\u4ef7\u683c:" + item.getPrice()
                        + "  \u00a77\u9650\u8d2d:" + item.getDailyLimit());
            }

            builder.button("\u00a7e\u8fd4\u56de");

            builder.validResultHandler(response -> {
                int id = response.clickedButtonId();
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (!player.isOnline()) return;
                    if (id >= 0 && id < items.size()) {
                        String removedName = items.get(id).getName();
                        plugin.getShopManager().removeItem(id);
                        player.sendMessage(Const.PREFIX + "\u00a7a\u5df2\u4e0b\u67b6\u5546\u54c1: " + removedName);
                        openShopRemove(plugin, player);
                    } else {
                        openShopEdit(plugin, player);
                    }
                });
            });

            getApi().sendForm(player.getUniqueId(), builder);
        } catch (Throwable t) {
            JavaMenus.openShopRemove(plugin, player);
        }
    }

    static boolean executePurchase(MGActivitysPlugin plugin, Player player, int shopItemIndex, int quantity) {
        ShopManager shopManager = plugin.getShopManager();
        ActivityManager activityManager = plugin.getActivityManager();

        shopManager.checkDailyReset();
        List<ShopItem> items = shopManager.getDisplayItems();
        if (shopItemIndex < 0 || shopItemIndex >= items.size()) {
            player.sendMessage(Const.PREFIX + "\u00a7c\u5546\u54c1\u4e0d\u5b58\u5728");
            return false;
        }
        if (!ShopManager.isUnlocked(shopItemIndex, plugin.getPlayerLevel(player))) {
            player.sendMessage(Const.PREFIX + "\u00a7c\u8be5\u5546\u54c1\u5c1a\u672a\u89e3\u9501\uff0c\u9700\u8981\u7b49\u7ea7\u8fbe\u5230"
                    + ShopManager.requiredLevelForIndex(shopItemIndex) + "\u7ea7");
            return false;
        }
        ShopItem item = items.get(shopItemIndex);
        int remaining = item.getDailyLimit() - item.getPurchasedToday();

        if (quantity <= 0 || quantity > remaining) {
            player.sendMessage(Const.PREFIX + "\u00a7c\u8d2d\u4e70\u6570\u91cf\u65e0\u6548 (\u5269\u4f59\u53ef\u8d2d: " + remaining + ")");
            return false;
        }

        double cost = item.getPrice() * quantity;
        ActivityData data = activityManager.getPlayerData(player.getName());
        double dynamicActivity = data.getDynamicActivity();

        if (dynamicActivity < cost) {
            player.sendMessage(Const.PREFIX + "\u00a7c\u6210\u957f\u503c\u4e0d\u8db3! \u9700\u8981: " + Const.formatActivity(cost)
                    + " \u4f60\u6709: " + Const.formatActivity(dynamicActivity));
            return false;
        }

        Material mat = Material.matchMaterial(item.getType());
        if (mat == null) mat = Material.matchMaterial(item.getType().replace("minecraft:", ""));
        if (mat == null) {
            player.sendMessage("\u00a7c\u7269\u54c1\u7c7b\u578b\u4e0d\u652f\u6301: " + item.getType());
            return false;
        }

        Map<Integer, ItemStack> overflow = player.getInventory().addItem(new ItemStack(mat, quantity));
        if (!overflow.isEmpty()) {
            player.getWorld().dropItem(player.getLocation(), overflow.values().iterator().next());
        }

        data.setDynamicActivity(dynamicActivity - cost);
        item.setPurchasedToday(item.getPurchasedToday() + quantity);
        activityManager.save();
        shopManager.save();

        player.sendMessage(Const.PREFIX + "\u00a7a\u6210\u529f\u8d2d\u4e70 " + quantity + "x " + item.getName()
                + " \u82b1\u8d39 " + Const.formatActivity(cost) + " \u6210\u957f\u503c");
        return true;
    }
}
