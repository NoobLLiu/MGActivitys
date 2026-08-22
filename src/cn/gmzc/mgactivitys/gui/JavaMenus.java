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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import java.util.function.Consumer;

public class JavaMenus {

    private static final Map<UUID, Consumer<String>> chatCallbacks = new HashMap<>();

    private static ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack filler() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }

    public static void requestChatInput(Player player, String prompt, Consumer<String> callback) {
        chatCallbacks.put(player.getUniqueId(), callback);
        player.closeInventory();
        player.sendMessage(prompt);
    }

    public static Consumer<String> consumeChatInput(UUID uuid) {
        return chatCallbacks.remove(uuid);
    }

    private static ItemStack buildShopItemStack(ShopItem item) {
        return buildShopItemStack(item, ShopManager.getRestockCountdown());
    }

    private static ItemStack buildShopItemStack(ShopItem item, String restockCountdown) {
        String type = item.getType();
        Material mat = Material.matchMaterial(type);
        if (mat == null) mat = Material.matchMaterial(type.replace("minecraft:", ""));
        if (mat == null) mat = Material.CHEST;
        int remaining = item.getDailyLimit() - item.getPurchasedToday();
        return createItem(mat, "\u00a7f" + item.getName(),
                Arrays.asList("\u00a7e\u4ef7\u683c: " + item.getPrice(),
                        "\u00a77\u5269\u4f59: " + remaining + "/" + item.getDailyLimit(),
                        "\u00a7b\u8865\u8d27\u5012\u8ba1\u65f6: " + restockCountdown));
    }

    private static ItemStack buildLockedShopItemStack(ShopItem item, int requiredLevel) {
        return createItem(Material.BARRIER, "\u00a7c\u672a\u89e3\u9501\u5546\u54c1",
                Arrays.asList("\u00a77" + item.getName(),
                        "\u00a7e\u5728\u73a9\u5bb6\u7b49\u7ea7\u8fbe\u5230" + requiredLevel + "\u7ea7\u540e\u89e3\u9501"));
    }

    public static void openAdminGUI(MGActivitysPlugin plugin, Player player) {
        ConfigManager configManager = plugin.getConfigManager();
        Map<String, ListenerConfig> listeners = configManager.getAllListeners();
        List<Map.Entry<String, ListenerConfig>> listenerList = new ArrayList<>(listeners.entrySet());

        Inventory inv = Bukkit.createInventory(null, 54, "\u00a7l\u00a76\u6210\u957f\u503c\u63a7\u5236\u9762\u677f");
        ItemStack glass = filler();
        for (int i = 0; i < 54; i++) inv.setItem(i, glass);

        int[] listenerSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34
        };
        Map<Integer, Integer> slotToIndex = new HashMap<>();
        int shown = Math.min(listenerList.size(), listenerSlots.length);
        for (int i = 0; i < shown; i++) {
            int slot = listenerSlots[i];
            slotToIndex.put(slot, i);
            ListenerConfig lc = listenerList.get(i).getValue();
            ItemStack item = createItem(lc.isEnabled() ? Material.LIME_DYE : Material.GRAY_DYE,
                    "\u00a7f" + lc.getName() + " " + (lc.isEnabled() ? "\u00a7a\u542f\u7528" : "\u00a7c\u7981\u7528"),
                    Collections.singletonList("\u00a7e\u4e00\u6b21\u83b7\u5f97: " + lc.getMultiplier() + " | \u6bcf\u65e5\u4e0a\u9650: " + lc.getDailyLimitDisplay()));
            inv.setItem(slot, item);
        }

        int decayMode = configManager.getDailyDecayMode();
        double decayAmount = configManager.getDailyDecayAmount();
        String decayStr = decayMode == 0 ? decayAmount + "%" : String.valueOf(decayAmount);
        inv.setItem(47, createItem(Material.PAPER, "\u00a7e\u6bcf\u65e5\u524a\u51cf\u8bbe\u7f6e",
                Arrays.asList(
                        "\u00a77\u72b6\u6001: " + (configManager.isDailyDecayEnabled() ? "\u00a7a\u5df2\u5f00\u542f" : "\u00a7c\u5df2\u5173\u95ed"),
                        "\u00a77\u5f53\u524d\u6570\u503c: " + decayStr)));

        inv.setItem(51, createItem(Material.BARRIER, "\u00a7c\u91cd\u7f6e\u6240\u6709\u6570\u636e",
                Collections.singletonList("\u00a77\u6e05\u9664\u6240\u6709\u73a9\u5bb6\u6210\u957f\u503c")));

        inv.setItem(53, createItem(Material.BARRIER, "\u00a7c\u5173\u95ed", null));

        plugin.openGui(player, inv, clickedSlot -> {
            if (slotToIndex.containsKey(clickedSlot)) {
                String key = listenerList.get(slotToIndex.get(clickedSlot)).getKey();
                openListenerConfig(plugin, player, key);
            } else if (clickedSlot == 47) {
                openDecayConfig(plugin, player);
            } else if (clickedSlot == 51) {
                openConfirmReset(plugin, player);
            }
        });
    }

    public static void openRankGUI(MGActivitysPlugin plugin, Player player) {
        ActivityManager activityManager = plugin.getActivityManager();
        List<Map.Entry<String, ActivityData>> ranked =
            activityManager.getRankedPlayers(plugin::isRealPlayerName);

        Inventory inv = Bukkit.createInventory(null, 54, "\u00a7l\u00a76\u6210\u957f\u503c\u6392\u884c\u699c");
        ItemStack glass = filler();
        for (int i = 0; i < 54; i++) inv.setItem(i, glass);

        inv.setItem(4, createItem(Material.BOOK, "\u00a76=== \u6210\u957f\u503c\u6392\u884c\u699c ===", null));

        int slot = 9;
        for (int i = 0; i < ranked.size() && slot < 53; i++, slot++) {
            Map.Entry<String, ActivityData> entry = ranked.get(i);
            String name = entry.getKey();
            double activity = entry.getValue().getTotalActivity();
            int rank = i + 1;
            boolean isSelf = name.equalsIgnoreCase(player.getName());

            String rankStr;
            if (rank == 1) rankStr = "\u00a76#1";
            else if (rank == 2) rankStr = "\u00a77#2";
            else if (rank == 3) rankStr = "\u00a7e#3";
            else rankStr = "\u00a7f#" + rank;

            String displayName = rankStr + " \u00a7f" + name + (isSelf ? " \u00a77(\u4f60)" : "");
            List<String> lore = Collections.singletonList("\u00a7a\u6210\u957f\u503c: " + Const.formatActivity(activity));

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
            plugin.getPlayerSkinService().applyByName(skullMeta, name);
            skullMeta.setDisplayName(displayName);
            skullMeta.setLore(lore);
            head.setItemMeta(skullMeta);
            inv.setItem(slot, head);
        }

        inv.setItem(53, createItem(Material.BARRIER, "\u00a7c\u5173\u95ed", null));

        plugin.openGui(player, inv, clickedSlot -> {});
    }

    public static void openShopMain(MGActivitysPlugin plugin, Player player) {
        ShopManager shopManager = plugin.getShopManager();
        ActivityManager activityManager = plugin.getActivityManager();
        shopManager.checkDailyReset();

        List<ShopItem> items = shopManager.getDisplayItems();
        ActivityData data = activityManager.getPlayerData(player.getName());
        double dynamicActivity = data.getDynamicActivity();
        int playerLevel = plugin.getPlayerLevel(player);
        String restockCountdown = ShopManager.getRestockCountdown();

        Inventory inv = Bukkit.createInventory(null, 54, "\u00a7l\u00a76\u6210\u957f\u5546\u5e97");
        ItemStack glass = filler();
        for (int i = 0; i < 54; i++) inv.setItem(i, glass);

        inv.setItem(4, createItem(Material.PAPER, "\u00a7e\u4f60\u7684\u52a8\u6001\u6210\u957f\u503c: \u00a7a" + Const.formatActivity(dynamicActivity),
                Collections.singletonList("\u00a77\u5f53\u524d\u7b49\u7ea7: " + playerLevel)));

        for (int i = 0; i < items.size() && i < 36; i++) {
            ShopItem item = items.get(i);
            inv.setItem(9 + i, ShopManager.isUnlocked(i, playerLevel)
                    ? buildShopItemStack(item, restockCountdown)
                    : buildLockedShopItemStack(item, ShopManager.requiredLevelForIndex(i)));
        }

        if (player.isOp()) {
            inv.setItem(49, createItem(Material.ANVIL, "\u00a7c\u7f16\u8f91\u5546\u5e97", null));
        }

        inv.setItem(45, createItem(Material.CHEST, "\u00a7e\u8fd4\u56de\u4ea4\u6613\u5e02\u573a",
                Arrays.asList("\u00a77\u70b9\u51fb\u8fd4\u56de\u5168\u7403\u5e02\u573a",
                        "\u00a77\u603b\u6210\u957f\u503c: \u00a7a" + Const.formatActivity(data.getTotalActivity()),
                        "\u00a77\u52a8\u6001\u6210\u957f\u503c: \u00a7a" + Const.formatActivity(data.getDynamicActivity()))));

        plugin.openGui(player, inv, clickedSlot -> {
            if (clickedSlot >= 9 && clickedSlot < 45) {
                int itemIndex = clickedSlot - 9;
                if (itemIndex < items.size()) {
                    int currentLevel = plugin.getPlayerLevel(player);
                    if (!ShopManager.isUnlocked(itemIndex, currentLevel)) {
                        player.sendMessage(Const.PREFIX + "\u00a7c\u8be5\u5546\u54c1\u5c1a\u672a\u89e3\u9501\uff0c\u9700\u8981\u7b49\u7ea7\u8fbe\u5230"
                                + ShopManager.requiredLevelForIndex(itemIndex) + "\u7ea7");
                        return;
                    }
                    BedrockForms.executePurchase(plugin, player, itemIndex, 1);
                    openShopMain(plugin, player);
                }
            } else if (clickedSlot == 49 && player.isOp()) {
                openShopEdit(plugin, player);
            } else if (clickedSlot == 45) {
                player.closeInventory();
                player.performCommand("se gui");
            }
        });
    }

    public static void openListenerConfig(MGActivitysPlugin plugin, Player player, String listenerKey) {
        ConfigManager configManager = plugin.getConfigManager();
        ListenerConfig config = configManager.getListenerConfig(listenerKey);
        if (config == null) {
            player.sendMessage(Const.PREFIX + "\u00a7c\u76d1\u542c\u5668\u4e0d\u5b58\u5728");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 9, "\u00a7l\u00a76" + config.getName() + " \u914d\u7f6e");
        for (int i = 0; i < 9; i++) inv.setItem(i, filler());

        inv.setItem(2, createItem(config.isEnabled() ? Material.LIME_DYE : Material.GRAY_DYE,
                "\u00a7f\u5f53\u524d\u72b6\u6001: " + (config.isEnabled() ? "\u00a7a\u542f\u7528" : "\u00a7c\u7981\u7528") + " \u00a77(\u70b9\u51fb\u5207\u6362)", null));

        inv.setItem(4, createItem(Material.PAPER, "\u00a7e\u4fee\u6539\u4e00\u6b21\u83b7\u5f97\u7684\u6210\u957f\u503c",
                Collections.singletonList("\u00a77\u5f53\u524d\u503c: " + config.getMultiplier())));

        inv.setItem(6, createItem(Material.PAPER, "\u00a7e\u4fee\u6539\u6bcf\u65e5\u4e0a\u9650",
                Collections.singletonList("\u00a77\u5f53\u524d\u4e0a\u9650: " + config.getDailyLimitDisplay())));

        inv.setItem(8, createItem(Material.ARROW, "\u00a7e\u8fd4\u56de\u4e0a\u7ea7", null));

        plugin.openGui(player, inv, clickedSlot -> {
            if (clickedSlot == 2) {
                configManager.setListenerEnabled(listenerKey, !config.isEnabled());
                openListenerConfig(plugin, player, listenerKey);
            } else if (clickedSlot == 4) {
                requestChatInput(player,
                        "\u00a7e\u5f53\u524d\u503c: " + config.getMultiplier() + ", \u8bf7\u8f93\u5165" + multiplierRange(config),
                        msg -> {
                            try {
                                double val = Double.parseDouble(msg.trim());
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
            } else if (clickedSlot == 6) {
                requestChatInput(player,
                        "\u00a7e\u5f53\u524d\u4e0a\u9650: " + config.getDailyLimitDisplay() + ", \u8bf7\u8f93\u5165 -1(\u4e0d\u9650) \u6216 1~10000",
                        msg -> {
                            try {
                                int val = Integer.parseInt(msg.trim());
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
            } else if (clickedSlot == 8) {
                openAdminGUI(plugin, player);
            }
        });
    }

    private static String multiplierRange(ListenerConfig config) {
        return config.hasDailyLimit()
                ? "在 0.1~" + config.getDailyLimit() + " 之间"
                : "不低于 0.1";
    }

    private static boolean isValidDailyLimit(int dailyLimit) {
        return dailyLimit == -1 || (dailyLimit >= 1 && dailyLimit <= 10000);
    }

    public static void openDecayConfig(MGActivitysPlugin plugin, Player player) {
        ConfigManager configManager = plugin.getConfigManager();
        int mode = configManager.getDailyDecayMode();
        double amount = configManager.getDailyDecayAmount();
        String modeStr = mode == 0 ? "\u767e\u5206\u6bd4" : "\u76f4\u63a5\u6263\u9664";
        boolean enabled = configManager.isDailyDecayEnabled();

        Inventory inv = Bukkit.createInventory(null, 9, "\u00a7l\u00a76\u6bcf\u65e5\u6210\u957f\u503c\u524a\u51cf\u8bbe\u7f6e");
        for (int i = 0; i < 9; i++) inv.setItem(i, filler());

        inv.setItem(0, createItem(enabled ? Material.LIME_DYE : Material.GRAY_DYE,
                "\u00a7f\u529f\u80fd\u72b6\u6001: " + (enabled ? "\u00a7a\u5df2\u5f00\u542f" : "\u00a7c\u5df2\u5173\u95ed"),
                Collections.singletonList("\u00a77\u70b9\u51fb\u5207\u6362")));
        inv.setItem(2, createItem(Material.PAPER, "\u00a7e\u5f53\u524d\u6a21\u5f0f: " + modeStr + " \u00a7f\u503c: " + amount + (mode == 0 ? "%" : ""), null));
        inv.setItem(4, createItem(Material.PAPER, "\u00a7e\u4fee\u6539\u524a\u51cf\u503c",
                Collections.singletonList("\u00a77\u52a0% = \u767e\u5206\u6bd4\u6a21\u5f0f, \u4e0d\u52a0 = \u76f4\u63a5\u6263\u9664")));
        inv.setItem(8, createItem(Material.ARROW, "\u00a7e\u8fd4\u56de", null));

        plugin.openGui(player, inv, clickedSlot -> {
            if (clickedSlot == 0) {
                configManager.setDailyDecayEnabled(!enabled);
                openDecayConfig(plugin, player);
            } else if (clickedSlot == 4) {
                requestChatInput(player,
                        "\u00a7e\u8f93\u5165\u65b0\u503c (\u52a0% = \u767e\u5206\u6bd4, \u4e0d\u52a0 = \u76f4\u63a5\u6263\u9664):",
                        msg -> {
                            String input = msg.trim();
                            if (input.isEmpty()) {
                                openDecayConfig(plugin, player);
                                return;
                            }
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
                            openDecayConfig(plugin, player);
                        });
            } else if (clickedSlot == 8) {
                openAdminGUI(plugin, player);
            }
        });
    }

    public static void openConfirmReset(MGActivitysPlugin plugin, Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "\u00a7l\u00a7c\u786e\u8ba4\u91cd\u7f6e");
        for (int i = 0; i < 9; i++) inv.setItem(i, filler());

        inv.setItem(2, createItem(Material.LIME_DYE, "\u00a7a\u53d6\u6d88", null));
        inv.setItem(6, createItem(Material.BARRIER, "\u00a7c\u786e\u8ba4\u91cd\u7f6e",
                Collections.singletonList("\u00a77\u6e05\u9664\u6240\u6709\u73a9\u5bb6\u6210\u957f\u503c\u6570\u636e")));

        plugin.openGui(player, inv, clickedSlot -> {
            if (clickedSlot == 6) {
                plugin.getActivityManager().resetAll();
                player.sendMessage(Const.PREFIX + "\u00a7a\u5df2\u91cd\u7f6e\u6240\u6709\u6210\u957f\u503c\u6570\u636e");
            }
            openAdminGUI(plugin, player);
        });
    }

    public static void openPurchaseConfirm(MGActivitysPlugin plugin, Player player, int shopItemIndex) {
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

        Inventory inv = Bukkit.createInventory(null, 9, "\u00a7l\u00a76\u8d2d\u4e70\u786e\u8ba4");
        for (int i = 0; i < 9; i++) inv.setItem(i, filler());

        inv.setItem(2, buildShopItemStack(item));
        inv.setItem(4, createItem(Material.PAPER, "\u00a7e\u70b9\u51fb\u8f93\u5165\u8d2d\u4e70\u6570\u91cf",
                Arrays.asList("\u00a7e\u4ef7\u683c: " + item.getPrice(),
                        "\u00a77\u5269\u4f59: " + remaining + "/" + item.getDailyLimit(),
                        "\u00a7b\u8865\u8d27\u5012\u8ba1\u65f6: " + ShopManager.getRestockCountdown(),
                        "\u00a7a\u4f60\u7684\u6210\u957f\u503c: " + Const.formatActivity(dynamicActivity))));

        inv.setItem(8, createItem(Material.ARROW, "\u00a7e\u8fd4\u56de\u5546\u5e97", null));

        plugin.openGui(player, inv, clickedSlot -> {
            if (clickedSlot == 4) {
                requestChatInput(player,
                        "\u00a7e\u8bf7\u8f93\u5165\u8d2d\u4e70\u6570\u91cf (\u5269\u4f59: " + remaining + "):",
                        msg -> {
                            try {
                                int quantity = Integer.parseInt(msg.trim());
                                BedrockForms.executePurchase(plugin, player, shopItemIndex, quantity);
                            } catch (NumberFormatException e) {
                                player.sendMessage(Const.PREFIX + "\u00a7c\u8bf7\u8f93\u5165\u6709\u6548\u7684\u6574\u6570");
                            }
                            openShopMain(plugin, player);
                        });
            } else if (clickedSlot == 8) {
                openShopMain(plugin, player);
            }
        });
    }

    public static void openShopEdit(MGActivitysPlugin plugin, Player player) {
        if (!player.isOp()) {
            player.sendMessage(Const.PREFIX + "\u00a7c\u4f60\u6ca1\u6709\u6743\u9650\u4f7f\u7528\u6b64\u529f\u80fd");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 9, "\u00a7l\u00a76\u7f16\u8f91\u5546\u5e97");
        for (int i = 0; i < 9; i++) inv.setItem(i, filler());

        inv.setItem(2, createItem(Material.LIME_DYE, "\u00a7a\u65b0\u589e\u7269\u54c1",
                Collections.singletonList("\u00a77\u624b\u6301\u7269\u54c1\u540e\u70b9\u51fb")));
        inv.setItem(4, createItem(Material.BARRIER, "\u00a7c\u4e0b\u67b6\u7269\u54c1", null));
        inv.setItem(8, createItem(Material.ARROW, "\u00a7e\u8fd4\u56de\u5546\u5e97", null));

        plugin.openGui(player, inv, clickedSlot -> {
            if (clickedSlot == 2) {
                openShopAdd(plugin, player);
            } else if (clickedSlot == 4) {
                openShopRemove(plugin, player);
            } else if (clickedSlot == 8) {
                openShopMain(plugin, player);
            }
        });
    }

    public static void openShopAdd(MGActivitysPlugin plugin, Player player) {
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held == null || held.getType() == Material.AIR) {
            player.sendMessage(Const.PREFIX + "\u00a7c\u8bf7\u624b\u6301\u8981\u6dfb\u52a0\u7684\u7269\u54c1\u518d\u4f7f\u7528\u6b64\u529f\u80fd");
            openShopEdit(plugin, player);
            return;
        }

        String typeStr = held.getType().name();
        String displayName = held.hasItemMeta() && held.getItemMeta().hasDisplayName()
                ? held.getItemMeta().getDisplayName() : typeStr;

        Inventory inv = Bukkit.createInventory(null, 9, "\u00a7l\u00a76\u65b0\u589e\u5546\u54c1");
        for (int i = 0; i < 9; i++) inv.setItem(i, filler());

        inv.setItem(2, held.clone());
        inv.setItem(4, createItem(Material.PAPER, "\u00a7e\u70b9\u51fb\u786e\u8ba4\u6dfb\u52a0",
                Collections.singletonList("\u00a77\u624b\u6301: " + displayName)));
        inv.setItem(8, createItem(Material.ARROW, "\u00a7e\u8fd4\u56de", null));

        plugin.openGui(player, inv, clickedSlot -> {
            if (clickedSlot == 4) {
                requestChatInput(player,
                        "\u00a7e\u624b\u6301\u7269\u54c1: \u00a7f" + displayName + " \u00a7e\u8bf7\u8f93\u5165\u5355\u4ef7:",
                        msg -> {
                            try {
                                double price = Double.parseDouble(msg.trim());
                                if (price <= 0) {
                                    player.sendMessage(Const.PREFIX + "\u00a7c\u4ef7\u683c\u5fc5\u987b\u5927\u4e8e0");
                                    openShopEdit(plugin, player);
                                    return;
                                }
                                requestChatInput(player,
                                        "\u00a7e\u8bf7\u8f93\u5165\u6bcf\u65e5\u9650\u8d2d\u6570\u91cf:",
                                        msg2 -> {
                                            try {
                                                int limit = Integer.parseInt(msg2.trim());
                                                if (limit < 1) {
                                                    player.sendMessage(Const.PREFIX + "\u00a7c\u6bcf\u65e5\u9650\u8d2d\u6570\u91cf\u5fc5\u987b\u5927\u4e8e0");
                                                } else {
                                                    plugin.getShopManager().addItem(new ShopItem(typeStr, displayName, price, limit, 0));
                                                    player.sendMessage(Const.PREFIX + "\u00a7a\u5df2\u6dfb\u52a0\u5546\u54c1: " + displayName);
                                                }
                                            } catch (NumberFormatException e) {
                                                player.sendMessage(Const.PREFIX + "\u00a7c\u8bf7\u8f93\u5165\u6709\u6548\u7684\u6574\u6570");
                                            }
                                            openShopEdit(plugin, player);
                                        });
                            } catch (NumberFormatException e) {
                                player.sendMessage(Const.PREFIX + "\u00a7c\u8bf7\u8f93\u5165\u6709\u6548\u7684\u6570\u5b57");
                                openShopEdit(plugin, player);
                            }
                        });
            } else if (clickedSlot == 8) {
                openShopEdit(plugin, player);
            }
        });
    }

    public static void openShopRemove(MGActivitysPlugin plugin, Player player) {
        List<ShopItem> items = plugin.getShopManager().getItems();
        int size = Math.max(9, Math.min((int) Math.ceil((items.size() + 1) / 9.0) * 9, 54));

        Inventory inv = Bukkit.createInventory(null, size, "\u00a7l\u00a76\u4e0b\u67b6\u5546\u54c1");
        for (int i = 0; i < size; i++) inv.setItem(i, filler());

        for (int i = 0; i < items.size() && i < size - 1; i++) {
            inv.setItem(i, buildShopItemStack(items.get(i)));
        }

        int backSlot = size - 1;
        inv.setItem(backSlot, createItem(Material.ARROW, "\u00a7e\u8fd4\u56de", null));

        plugin.openGui(player, inv, clickedSlot -> {
            if (clickedSlot >= 0 && clickedSlot < items.size()) {
                String removedName = items.get(clickedSlot).getName();
                plugin.getShopManager().removeItem(clickedSlot);
                player.sendMessage(Const.PREFIX + "\u00a7a\u5df2\u4e0b\u67b6\u5546\u54c1: " + removedName);
                openShopRemove(plugin, player);
            } else if (clickedSlot == backSlot) {
                openShopEdit(plugin, player);
            }
        });
    }
}
