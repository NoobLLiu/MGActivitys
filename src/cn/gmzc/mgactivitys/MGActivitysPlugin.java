package cn.gmzc.mgactivitys;

import cn.gmzc.fakeplayermanager.api.FakePlayerIdentityService;
import cn.gmzc.mgactivitys.command.ActiCommand;
import cn.gmzc.mgactivitys.command.ActiRankCommand;
import cn.gmzc.mgactivitys.command.ActiShopCommand;
import cn.gmzc.mgactivitys.command.ApiExportCommand;
import cn.gmzc.mgactivitys.data.ActivityManager;
import cn.gmzc.mgactivitys.data.ConfigManager;
import cn.gmzc.mgactivitys.data.ShopManager;
import cn.gmzc.mgactivitys.gui.JavaMenus;
import cn.gmzc.mgactivitys.listener.ActivityListener;
import cn.gmzc.skincache.api.PlayerSkinService;
import cn.gmzc.titles.api.TitleLevelService;
import cn.gmzc.titles.api.TitleLevelServices;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class MGActivitysPlugin extends JavaPlugin implements Listener {

    private static MGActivitysPlugin instance;

    private ConfigManager configManager;
    private ActivityManager activityManager;
    private ShopManager shopManager;
    private ActivityListener activityListener;
    private PlayerSkinService playerSkinService;
    private final Map<UUID, GuiContext> guiContexts = new HashMap<>();
    private BukkitTask activitySaveTask;
    private boolean fakePlayerWarningLogged;

    public static MGActivitysPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        configManager = new ConfigManager(dataFolder, getLogger());
        configManager.load();

        activityManager = new ActivityManager(dataFolder, configManager, getLogger());
        activityManager.load();

        shopManager = new ShopManager(dataFolder, getLogger());
        shopManager.load();
        playerSkinService = Bukkit.getServicesManager().load(PlayerSkinService.class);
        if (playerSkinService == null) {
            throw new IllegalStateException("GMZCSkinCache service is unavailable");
        }

        activityListener = new ActivityListener(this);
        activityListener.startTimer();
        activitySaveTask = Bukkit.getScheduler().runTaskTimer(
            this,
            activityManager::saveIfDirty,
            200L,
            200L
        );

        Bukkit.getPluginManager().registerEvents(activityListener, this);
        Bukkit.getPluginManager().registerEvents(this, this);

        regCommand("acti", new ActiCommand(this));
        regCommand("actirank", new ActiRankCommand(this));
        regCommand("actishop", new ActiShopCommand(this));
        regCommand("mgactivity", new ApiExportCommand(this));

        getLogger().info("\u00a7a[\u6210\u957f\u503c\u63d2\u4ef6] \u5df2\u52a0\u8f7d\uff01\u7248\u672c 1.0.0");
        getLogger().info("\u00a7a[\u6210\u957f\u503c\u63d2\u4ef6] \u7ba1\u7406\u5458\u4f7f\u7528 /acti \u6253\u5f00\u63a7\u5236\u9762\u677f");
        getLogger().info("\u00a7a[\u6210\u957f\u503c\u63d2\u4ef6] \u73a9\u5bb6\u4f7f\u7528 /actirank \u67e5\u770b\u6392\u884c\u699c");
        getLogger().info("\u00a7a[\u6210\u957f\u503c\u63d2\u4ef6] \u73a9\u5bb6\u4f7f\u7528 /actishop \u6253\u5f00\u6210\u957f\u5546\u5e97");
    }

    @Override
    public void onDisable() {
        if (activitySaveTask != null) {
            activitySaveTask.cancel();
            activitySaveTask = null;
        }
        if (activityListener != null) {
            activityListener.stopTimer();
        }
        if (activityManager != null) {
            activityManager.save();
        }
        if (shopManager != null) {
            shopManager.save();
        }
        if (configManager != null) {
            configManager.save();
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        GuiContext context = guiContexts.get(player.getUniqueId());
        if (context == null || e.getView().getTopInventory() != context.inventory()) return;

        e.setCancelled(true);
        if (e.getRawSlot() >= 0 && e.getRawSlot() < context.inventory().getSize()) {
            context.handler().accept(e.getRawSlot());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();
        GuiContext context = guiContexts.get(uuid);
        if (context != null && e.getView().getTopInventory() == context.inventory()) {
            guiContexts.remove(uuid, context);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        guiContexts.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Consumer<String> callback = JavaMenus.consumeChatInput(e.getPlayer().getUniqueId());
        if (callback != null) {
            e.setCancelled(true);
            Bukkit.getScheduler().runTask(this, () -> callback.accept(e.getMessage()));
        }
    }

    public void openGui(Player player, Inventory inventory, Consumer<Integer> handler) {
        if (player == null || inventory == null || handler == null) {
            throw new IllegalArgumentException("player, inventory and handler must be non-null");
        }
        guiContexts.put(player.getUniqueId(), new GuiContext(inventory, handler));
        player.openInventory(inventory);
    }

    private record GuiContext(Inventory inventory, Consumer<Integer> handler) {}

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ActivityManager getActivityManager() {
        return activityManager;
    }

    /**
     * Returns the player's current total activity after applying the activity
     * system's normal daily rollover/decay rules.
     */
    public double getTotalActivity(String playerName) {
        if (activityManager == null || playerName == null || playerName.isBlank()) {
            return 0.0D;
        }
        return activityManager.getPlayerData(playerName).getTotalActivity();
    }

    /**
     * Returns an immutable name-to-total-activity snapshot for integrations.
     */
    public Map<String, Double> getTotalActivitySnapshot() {
        if (activityManager == null) {
            return Map.of();
        }
        return activityManager.getTotalActivitySnapshot();
    }

    /**
     * Adds growth activity for a player through a configured listener method.
     * The method must exist in the listener config (enabled, multiplier,
     * daily limit). Returns true only when growth was actually granted.
     */
    public boolean addActivity(String playerName, String listenerType) {
        if (activityManager == null || playerName == null || playerName.isBlank()
            || listenerType == null || listenerType.isBlank()) {
            return false;
        }
        return activityManager.addActivity(
            activityManager.resolvePlayerName(playerName),
            listenerType
        );
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public PlayerSkinService getPlayerSkinService() {
        return playerSkinService;
    }

    /**
     * Returns whether a name is eligible for player-facing activity rankings.
     * When FakePlayerManager is unavailable, preserve the legacy ranking
     * behavior instead of blocking the growth system.
     */
    public boolean isRealPlayerName(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return false;
        }
        try {
            FakePlayerIdentityService identities =
                Bukkit.getServicesManager().load(FakePlayerIdentityService.class);
            return identities == null || !identities.isFakePlayer(playerName);
        } catch (LinkageError | RuntimeException exception) {
            if (!fakePlayerWarningLogged) {
                fakePlayerWarningLogged = true;
                getLogger().warning(
                    "Unable to load FakePlayerManager identity service; keeping the original growth ranking: "
                        + exception.getMessage()
                );
            }
            return true;
        }
    }

    public int getPlayerLevel(Player player) {
        if (player == null) {
            return 0;
        }
        TitleLevelService service = TitleLevelServices.get();
        if (service == null) {
            return 0;
        }
        try {
            return Math.max(0, service.getLevel(player.getUniqueId()));
        } catch (RuntimeException ignored) {
            return 0;
        }
    }

    private void regCommand(String name, CommandExecutor executor) {
        PluginCommand cmd = getCommand(name);
        if (cmd != null) {
            cmd.setExecutor(executor);
        }
    }
}
