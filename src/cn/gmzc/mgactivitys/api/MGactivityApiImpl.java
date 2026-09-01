package cn.gmzc.mgactivitys.api;

import cn.gmzc.mgactivitys.data.ActivityManager;
import mc233.fun.kbbstoper.core.platform.MGactivityApi;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * KBBSToper 原生对接接口实现，由 {@link MGactivityApiService} 注册到 Bukkit
 * ServicesManager。所有语义与既有的 {@code mgactivity} 控制台命令完全一致，
 * 直接委托 {@link ActivityManager}：
 * <ul>
 *   <li>倍率：取最大值、不叠加，次日自动归位 1.0；</li>
 *   <li>生命值上限：绝对值写入、持久化、跨天保留，防御性钳制 [20, 50]；</li>
 *   <li>连签中断：按派发值即时扣减成长值并累加中断计数，立即生效。</li>
 * </ul>
 * 玩家名支持中文/特殊字符，按 {@code ActivityManager.resolvePlayerName} 解析。
 */
public class MGactivityApiImpl implements MGactivityApi {

    private final ActivityManager activityManager;
    private final Logger logger;

    public MGactivityApiImpl(ActivityManager activityManager, Logger logger) {
        this.activityManager = Objects.requireNonNull(activityManager, "activityManager");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    @Override
    public void setGrowthMultiplier(String player, double value) {
        if (!activityManager.setGrowthMultiplier(player, value)) {
            logger.warning("[MGactivityApi] setGrowthMultiplier rejected: player=" + player + ", value=" + value);
        }
    }

    @Override
    public void setExperienceMultiplier(String player, double value) {
        if (!activityManager.setExperienceMultiplier(player, value)) {
            logger.warning("[MGactivityApi] setExperienceMultiplier rejected: player=" + player + ", value=" + value);
        }
    }

    @Override
    public void setMaxHp(String player, int value) {
        int applied = activityManager.setMaxHp(player, value);
        if (applied < 0) {
            logger.warning("[MGactivityApi] setMaxHp rejected: player=" + player + ", value=" + value);
            return;
        }
        // 立即应用到在线玩家（KBBSToper 上线/维度切换刷新时需要即时生效）
        Player online = Bukkit.getPlayerExact(player);
        if (online != null && online.isOnline()) {
            applyMaxHealth(online, applied);
        }
    }

    /** 把持久化的血量上限应用到玩家的 Minecraft 属性。 */
    private void applyMaxHealth(Player player, int maxHp) {
        try {
            var attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (attr != null) {
                attr.setBaseValue(maxHp);
            }
            if (player.getHealth() > maxHp) {
                player.setHealth(maxHp);
            }
        } catch (Exception e) {
            logger.warning("[MGactivityApi] applyMaxHealth failed for " + player.getName() + ": " + e.getMessage());
        }
    }

    @Override
    public void addStreakBreak(String player, int value) {
        if (!activityManager.addStreakBreak(player, value)) {
            logger.warning("[MGactivityApi] addStreakBreak rejected: player=" + player + ", value=" + value);
        }
    }

    @Override
    public void addStarlightPoints(String player, long value) {
        if (!activityManager.addStarlightPoints(player, value)) {
            logger.warning("[MGactivityApi] addStarlightPoints rejected: player=" + player + ", value=" + value);
        }
    }

    @Override
    public void addGrowthPoints(String player, double value) {
        if (!activityManager.addGrowthPoints(player, value)) {
            logger.warning("[MGactivityApi] addGrowthPoints rejected: player=" + player + ", value=" + value);
        }
    }

    @Override
    public double getGrowthValue(String player) {
        if (player == null || player.isBlank()) {
            return -1;
        }
        String resolved = activityManager.resolvePlayerName(player);
        if (!activityManager.getPlayersData().containsKey(resolved)) {
            return -1;
        }
        return activityManager.getPlayerData(resolved).getTotalActivity();
    }
}
