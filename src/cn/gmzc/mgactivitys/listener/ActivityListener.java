package cn.gmzc.mgactivitys.listener;

import cn.gmzc.mgactivitys.MGActivitysPlugin;
import cn.gmzc.mgactivitys.data.ActivityManager;
import cn.gmzc.mgactivitys.util.Const;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class ActivityListener implements Listener {

    private final MGActivitysPlugin plugin;
    private final ActivityManager activityManager;
    private final Map<String, Location> lastPositions = new HashMap<>();
    private BukkitRunnable periodicTask;

    public ActivityListener(MGActivitysPlugin plugin) {
        this.plugin = plugin;
        this.activityManager = plugin.getActivityManager();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (plugin.getConfigManager().getListenerConfig("playerJoin").isEnabled()) {
            activityManager.addActivity(player.getName(), "playerJoin");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        lastPositions.remove(e.getPlayer().getName());
    }

    @EventHandler
    public void onTotemUse(EntityResurrectEvent e) {
        if (e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();
            if (plugin.getConfigManager().getListenerConfig("totemUsed").isEnabled()) {
                activityManager.addActivity(player.getName(), "totemUsed");
            }
        }
    }

    @EventHandler
    public void onEat(PlayerItemConsumeEvent e) {
        if (Const.FOOD_ITEMS.contains(e.getItem().getType().name())) {
            Player player = e.getPlayer();
            if (plugin.getConfigManager().getListenerConfig("foodEaten").isEnabled()) {
                activityManager.addActivity(player.getName(), "foodEaten");
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        if (plugin.getConfigManager().getListenerConfig("blockBreak").isEnabled()) {
            activityManager.addActivity(player.getName(), "blockBreak");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        if (plugin.getConfigManager().getListenerConfig("blockPlace").isEnabled()) {
            activityManager.addActivity(player.getName(), "blockPlace");
        }
    }

    @EventHandler
    public void onExpGain(PlayerExpChangeEvent e) {
        Player player = e.getPlayer();
        if (plugin.getConfigManager().getListenerConfig("experienceGained").isEnabled()) {
            activityManager.addActivity(player.getName(), "experienceGained");
        }
    }

    @EventHandler
    public void onLevelUp(PlayerLevelChangeEvent e) {
        int levelsGained = e.getNewLevel() - e.getOldLevel();
        if (levelsGained <= 0
                || !plugin.getConfigManager().getListenerConfig("levelUp").isEnabled()) {
            return;
        }

        Player player = e.getPlayer();
        for (int i = 0; i < levelsGained; i++) {
            if (!activityManager.addActivity(player.getName(), "levelUp")) {
                break;
            }
        }
    }

    @EventHandler
    public void onAdvancementDone(PlayerAdvancementDoneEvent e) {
        if (e.getAdvancement().getDisplay() == null) {
            return;
        }

        String listenerType = e.getAdvancement().getDisplay().frame()
                == io.papermc.paper.advancement.AdvancementDisplay.Frame.CHALLENGE
                ? "advancementChallenge" : "advancement";
        if (plugin.getConfigManager().getListenerConfig(listenerType).isEnabled()) {
            activityManager.addActivity(e.getPlayer().getName(), listenerType);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onAttackEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            Player player = (Player) e.getDamager();
            if (plugin.getConfigManager().getListenerConfig("entityAttack").isEnabled()) {
                activityManager.addActivity(player.getName(), "entityAttack");
            }
        }
    }

    @EventHandler
    public void onEntityKill(EntityDeathEvent e) {
        Player killer = e.getEntity().getKiller();
        if (killer != null
                && plugin.getConfigManager().getListenerConfig("entityKill").isEnabled()) {
            activityManager.addActivity(killer.getName(), "entityKill");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamageTaken(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();
            if (e.getFinalDamage() > 0
                    && plugin.getConfigManager().getListenerConfig("damageTaken").isEnabled()) {
                activityManager.addActivity(player.getName(), "damageTaken");
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        if (plugin.getConfigManager().getListenerConfig("playerDeath").isEnabled()) {
            activityManager.addActivity(player.getName(), "playerDeath");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        if (plugin.getConfigManager().getListenerConfig("chatMessage").isEnabled()) {
            activityManager.addActivity(player.getName(), "chatMessage");
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        if (plugin.getConfigManager().getListenerConfig("playerRespawn").isEnabled()) {
            activityManager.addActivity(player.getName(), "playerRespawn");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();
            if (plugin.getConfigManager().getListenerConfig("itemPickup").isEnabled()) {
                activityManager.addActivity(player.getName(), "itemPickup");
            }
        }
    }

    public void startTimer() {
        periodicTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    String name = player.getName();
                    boolean onlineEnabled = plugin.getConfigManager().getListenerConfig("onlineTime").isEnabled();
                    boolean moveEnabled = plugin.getConfigManager().getListenerConfig("moveDistance").isEnabled();
                    if (onlineEnabled) {
                        activityManager.addActivity(name, "onlineTime");
                    }
                    if (moveEnabled) {
                        Location cur = player.getLocation();
                        Location prev = lastPositions.get(name);
                        if (prev != null && cur.getWorld().equals(prev.getWorld())) {
                            double dist = cur.distance(prev);
                            if (dist >= 100) {
                                int times = (int) (dist / 100);
                                for (int j = 0; j < times; j++) {
                                    if (!activityManager.addActivity(name, "moveDistance")) break;
                                }
                            }
                        }
                        lastPositions.put(name, cur.clone());
                    }
                }
            }
        };
        periodicTask.runTaskTimer(plugin, 1200L, 1200L);
    }

    public void stopTimer() {
        if (periodicTask != null) {
            periodicTask.cancel();
            periodicTask = null;
        }
    }
}
