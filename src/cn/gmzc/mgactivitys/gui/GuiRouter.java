package cn.gmzc.mgactivitys.gui;

import cn.gmzc.mgactivitys.MGActivitysPlugin;
import org.bukkit.entity.Player;

public class GuiRouter {

    public static void openAdminGUI(MGActivitysPlugin plugin, Player player) {
        JavaMenus.openAdminGUI(plugin, player);
    }

    public static void openRankGUI(MGActivitysPlugin plugin, Player player) {
        JavaMenus.openRankGUI(plugin, player);
    }

    public static void openShopMain(MGActivitysPlugin plugin, Player player) {
        JavaMenus.openShopMain(plugin, player);
    }

    public static void openListenerConfig(MGActivitysPlugin plugin, Player player, String listenerKey) {
        JavaMenus.openListenerConfig(plugin, player, listenerKey);
    }

    public static void openDecayConfig(MGActivitysPlugin plugin, Player player) {
        JavaMenus.openDecayConfig(plugin, player);
    }

    public static void openConfirmReset(MGActivitysPlugin plugin, Player player) {
        JavaMenus.openConfirmReset(plugin, player);
    }

    public static void openPurchaseConfirm(MGActivitysPlugin plugin, Player player, int shopItemIndex) {
        JavaMenus.openPurchaseConfirm(plugin, player, shopItemIndex);
    }

    public static void openShopEdit(MGActivitysPlugin plugin, Player player) {
        JavaMenus.openShopEdit(plugin, player);
    }

    public static void openShopAdd(MGActivitysPlugin plugin, Player player) {
        JavaMenus.openShopAdd(plugin, player);
    }

    public static void openShopRemove(MGActivitysPlugin plugin, Player player) {
        JavaMenus.openShopRemove(plugin, player);
    }
}
