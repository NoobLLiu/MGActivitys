package cn.gmzc.mgactivitys.model;

public class ListenerConfig {
    private boolean enabled;
    private double multiplier;
    private int dailyLimit;
    private String name;

    public ListenerConfig() {}

    public ListenerConfig(boolean enabled, double multiplier, int dailyLimit, String name) {
        this.enabled = enabled;
        this.multiplier = multiplier;
        this.dailyLimit = dailyLimit;
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public int getDailyLimit() {
        return dailyLimit;
    }

    public boolean hasDailyLimit() {
        return dailyLimit >= 0;
    }

    public String getDailyLimitDisplay() {
        return hasDailyLimit() ? String.valueOf(dailyLimit) : "\u4e0d\u9650";
    }

    public void setDailyLimit(int dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
