package cn.gmzc.mgactivitys.api;

import cn.gmzc.mgactivitys.MGActivitysPlugin;
import cn.gmzc.mgactivitys.data.ActivityManager;

/**
 * MGActivity API entry class
 * For KBBSToper and other external plugins to call
 */
public class MGActivityApi {
    private static MGActivityApi instance;
    private final MGActivitysPlugin plugin;
    private final ActivityManager activityManager;
    
    private MGActivityApi(MGActivitysPlugin plugin) {
        this.plugin = plugin;
        this.activityManager = plugin.getActivityManager();
    }
    
    /**
     * Get API instance
     * @return API instance, returns null when plugin is not loaded
     */
    public static MGActivityApi getInstance() {
        return instance;
    }
    
    /**
     * Initialize API (call in plugin onEnable)
     * @param plugin plugin instance
     */
    public static void init(MGActivitysPlugin plugin) {
        instance = new MGActivityApi(plugin);
    }
    
    // ===== Growth multiplier interface =====
    
    /**
     * Set player's daily growth value multiplier
     * @param playerName player game name
     * @param multiplier multiplier (>=0), multiple sources take highest, not cumulative
     * @return whether set successfully
     */
    public boolean setGrowthMultiplier(String playerName, double multiplier) {
        return activityManager.setGrowthMultiplier(playerName, multiplier);
    }
    
    /**
     * Get player's current growth multiplier
     * @param playerName player game name
     * @return current multiplier (default 1.0)
     */
    public double getGrowthMultiplier(String playerName) {
        return activityManager.getGrowthMultiplier(playerName);
    }
    
    /**
     * Reset player's growth multiplier to default
     * @param playerName player game name
     * @return whether reset successfully
     */
    public boolean resetGrowthMultiplier(String playerName) {
        return activityManager.resetGrowthMultiplier(playerName);
    }
    
    // ===== Experience multiplier interface =====
    
    /**
     * Set player's daily experience value multiplier
     * @param playerName player game name
     * @param multiplier multiplier (>=0), multiple sources take highest, not cumulative
     * @return whether set successfully
     */
    public boolean setExperienceMultiplier(String playerName, double multiplier) {
        return activityManager.setExperienceMultiplier(playerName, multiplier);
    }
    
    /**
     * Get player's current experience multiplier
     * @param playerName player game name
     * @return current multiplier (default 1.0)
     */
    public double getExperienceMultiplier(String playerName) {
        return activityManager.getExperienceMultiplier(playerName);
    }
    
    /**
     * Reset player's experience multiplier to default
     * @param playerName player game name
     * @return whether reset successfully
     */
    public boolean resetExperienceMultiplier(String playerName) {
        return activityManager.resetExperienceMultiplier(playerName);
    }
    
    // ===== Max HP interface =====
    
    /**
     * Set player's max HP
     * @param playerName player game name
     * @param maxHp max HP (hard cap 50, base floor 30)
     * @return actual value set (after clamp)
     */
    public int setMaxHp(String playerName, int maxHp) {
        return activityManager.setMaxHp(playerName, maxHp);
    }
    
    /**
     * Get player's current max HP
     * @param playerName player game name
     * @return current max HP (default 30)
     */
    public int getMaxHp(String playerName) {
        return activityManager.getMaxHp(playerName);
    }
    
    // ===== Streak break interface =====
    
    /**
     * Record a streak break
     * @param playerName player game name
     * @param breakCount break count
     * @return whether recorded successfully
     */
    public boolean addStreakBreak(String playerName, int breakCount) {
        return activityManager.addStreakBreak(playerName, breakCount);
    }
    
    // ===== Growth points / Starlight points interface (optional) =====
    
    /**
     * Add growth points for player
     * @param playerName player game name
     * @param points growth points amount
     * @return whether added successfully
     */
    public boolean addGrowthPoints(String playerName, double points) {
        if (playerName == null || playerName.isBlank() || !Double.isFinite(points) || points <= 0) {
            return false;
        }
        // Directly add growth points
        cn.gmzc.mgactivitys.model.ActivityData data = activityManager.getPlayerData(playerName);
        data.setTotalActivity(data.getTotalActivity() + points);
        data.setDynamicActivity(data.getDynamicActivity() + points);
        activityManager.save();
        return true;
    }
    
    /**
     * Add starlight points for player
     * @param playerName player game name
     * @param points starlight points amount
     * @return whether added successfully
     */
    public boolean addStarlightPoints(String playerName, long points) {
        return activityManager.addStarlightPoints(playerName, points);
    }
}