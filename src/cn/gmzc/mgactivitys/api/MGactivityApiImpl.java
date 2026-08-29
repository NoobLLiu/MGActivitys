package cn.gmzc.mgactivitys.api;

import cn.gmzc.mgactivitys.data.ActivityManager;
import mc233.fun.kbbstoper.core.platform.MGactivityApi;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * KBBSToper 原生对接接口实现，由 {@link MGactivityApiService} 注册到 Bukkit
 * ServicesManager。所有语义与既有的 {@code mgactivity} 控制台命令完全一致，
 * 直接委托 {@link ActivityManager}：
 * <ul>
 *   <li>倍率：取最大值、不叠加，次日自动归位 1.0；</li>
 *   <li>生命值上限：绝对值写入、即时应用到游戏内属性、持久化、跨天保留；KBBSToper A6 起下发 [20,50] 绝对值，不再设 30 下限，仅做 [1,50] 硬保险（默认 20）；</li>
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
}