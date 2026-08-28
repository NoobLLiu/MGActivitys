package cn.gmzc.mgactivitys.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class ActivityData {
    private double growthMultiplier = 1.0;
    private double experienceMultiplier = 1.0;
    private int maxHp = 30;
    private int streakBreakCount = 0;
    private long starlightPoints = 0L;

    public long getStarlightPoints() { return starlightPoints; }
    public void setStarlightPoints(long v) { this.starlightPoints = v; }

    public double getGrowthMultiplier() { return growthMultiplier; }
    public void setGrowthMultiplier(double v) { this.growthMultiplier = v; }

    public double getExperienceMultiplier() { return experienceMultiplier; }
    public void setExperienceMultiplier(double v) { this.experienceMultiplier = v; }

    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int v) { this.maxHp = v; }

    public int getStreakBreakCount() { return streakBreakCount; }
    public void setStreakBreakCount(int v) { this.streakBreakCount = v; }
    private double totalActivity;
    private LinkedHashMap<String, Double> todayActivity;
    private String lastActiveDate;
    private double dynamicActivity;

    public ActivityData() {
        this.todayActivity = new LinkedHashMap<>();
    }

    public double getTotalActivity() {
        return totalActivity;
    }

    public void setTotalActivity(double totalActivity) {
        this.totalActivity = totalActivity;
    }

    public LinkedHashMap<String, Double> getTodayActivity() {
        return todayActivity;
    }

    public void setTodayActivity(LinkedHashMap<String, Double> todayActivity) {
        this.todayActivity = todayActivity;
    }

    public String getLastActiveDate() {
        return lastActiveDate;
    }

    public void setLastActiveDate(String lastActiveDate) {
        this.lastActiveDate = lastActiveDate;
    }

    public double getDynamicActivity() {
        return dynamicActivity;
    }

    public void setDynamicActivity(double dynamicActivity) {
        this.dynamicActivity = dynamicActivity;
    }
}
