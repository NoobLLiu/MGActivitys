package cn.gmzc.mgactivitys.data;

import cn.gmzc.mgactivitys.model.ListenerConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigManager {
    private final File configFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Logger logger;
    private ConfigData configData;

    private static class ConfigData {
        Map<String, ListenerConfig> listeners = new LinkedHashMap<>();
        boolean dailyDecayEnabled = false;
        int dailyDecayMode = 0;
        double dailyDecayAmount = 5;

        public Map<String, ListenerConfig> getListeners() {
            return listeners;
        }

        public void setListeners(Map<String, ListenerConfig> listeners) {
            this.listeners = listeners;
        }

        public int getDailyDecayMode() {
            return dailyDecayMode;
        }

        public boolean isDailyDecayEnabled() {
            return dailyDecayEnabled;
        }

        public void setDailyDecayEnabled(boolean dailyDecayEnabled) {
            this.dailyDecayEnabled = dailyDecayEnabled;
        }

        public void setDailyDecayMode(int dailyDecayMode) {
            this.dailyDecayMode = dailyDecayMode;
        }

        public double getDailyDecayAmount() {
            return dailyDecayAmount;
        }

        public void setDailyDecayAmount(double dailyDecayAmount) {
            this.dailyDecayAmount = dailyDecayAmount;
        }
    }

    public ConfigManager(File dataFolder, Logger logger) {
        this.configFile = new File(dataFolder, "config.json");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    public void load() {
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                Type type = new TypeToken<ConfigData>() {}.getType();
                ConfigData loaded = gson.fromJson(reader, type);
                if (loaded != null) {
                    this.configData = loaded;
                } else {
                    this.configData = createDefaults();
                }
            } catch (IOException e) {
                this.configData = createDefaults();
            }
        } else {
            this.configData = createDefaults();
        }

        if (configData.listeners == null) {
            configData.listeners = new LinkedHashMap<>();
        }

        Map<String, ListenerConfig> defaults = getDefaultListeners();
        for (Map.Entry<String, ListenerConfig> entry : defaults.entrySet()) {
            if (!configData.listeners.containsKey(entry.getKey())) {
                configData.listeners.put(entry.getKey(), entry.getValue());
            }
        }

        save();
    }

    private ConfigData createDefaults() {
        ConfigData data = new ConfigData();
        data.listeners = new LinkedHashMap<>(getDefaultListeners());
        data.dailyDecayEnabled = false;
        data.dailyDecayMode = 0;
        data.dailyDecayAmount = 5;
        return data;
    }

    private Map<String, ListenerConfig> getDefaultListeners() {
        Map<String, ListenerConfig> defaults = new LinkedHashMap<>();
        defaults.put("playerJoin", new ListenerConfig(true, 150, 150, "\u73a9\u5bb6\u52a0\u5165\u6e38\u620f"));
        defaults.put("totemUsed", new ListenerConfig(true, 100, 300, "\u6d88\u8017\u56fe\u817e"));
        defaults.put("foodEaten", new ListenerConfig(true, 5, 300, "\u5403\u4e0b\u98df\u7269"));
        defaults.put("blockBreak", new ListenerConfig(false, 0.1, 600, "\u7834\u574f\u65b9\u5757"));
        defaults.put("blockPlace", new ListenerConfig(true, 0.2, 1800, "\u653e\u7f6e\u65b9\u5757"));
        defaults.put("experienceGained", new ListenerConfig(true, 2, 450, "\u83b7\u53d6\u7ecf\u9a8c"));
        defaults.put("entityAttack", new ListenerConfig(false, 0.5, 300, "\u653b\u51fb\u5b9e\u4f53"));
        defaults.put("damageTaken", new ListenerConfig(true, 0.2, 400, "\u53d7\u5230\u4f24\u5bb3"));
        defaults.put("entityKill", new ListenerConfig(true, 1, 300, "\u51fb\u6740\u5b9e\u4f53"));
        defaults.put("levelUp", new ListenerConfig(true, 10, 300, "\u63d0\u5347\u7b49\u7ea7"));
        defaults.put("advancement", new ListenerConfig(true, 100, -1, "\u5b8c\u6210\u666e\u901a\u8fdb\u5ea6"));
        defaults.put("advancementChallenge", new ListenerConfig(true, 1000, -1, "\u5b8c\u6210\u6311\u6218\u8fdb\u5ea6"));
        defaults.put("playerDeath", new ListenerConfig(true, 20, 100, "\u6b7b\u4ea1"));
        defaults.put("chatMessage", new ListenerConfig(true, 5, 450, "\u53d1\u9001\u804a\u5929\u4fe1\u606f"));
        defaults.put("playerRespawn", new ListenerConfig(true, 10, 90, "\u73a9\u5bb6\u91cd\u751f"));
        defaults.put("onlineTime", new ListenerConfig(true, 1, 29997, "\u6bcf\u5728\u7ebf\u4e00\u5206\u949f"));
        defaults.put("itemPickup", new ListenerConfig(false, 0.1, 90, "\u6361\u8d77\u7269\u54c1"));
        defaults.put("moveDistance", new ListenerConfig(true, 5, 150, "\u6bcf\u79fb\u52a8100\u683c"));
        defaults.put("sellItem", new ListenerConfig(true, 50, 300, "\u51fa\u552e\u7269\u54c1"));
        defaults.put("api_direct", new ListenerConfig(true, 0, -1, "API\u76f4\u63a5\u589e\u52a0"));
        return defaults;
    }

    public void save() {
        try (FileWriter writer = new FileWriter(configFile)) {
            gson.toJson(configData, writer);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Unable to save activity configuration: " + configFile, e);
        }
    }

    public ListenerConfig getListenerConfig(String key) {
        return configData.listeners.get(key);
    }

    public Map<String, ListenerConfig> getAllListeners() {
        return configData.listeners;
    }

    public int getDailyDecayMode() {
        return configData.dailyDecayMode;
    }

    public boolean isDailyDecayEnabled() {
        return configData.dailyDecayEnabled;
    }

    public double getDailyDecayAmount() {
        return configData.dailyDecayAmount;
    }

    public void setListenerEnabled(String key, boolean enabled) {
        ListenerConfig config = configData.listeners.get(key);
        if (config != null) {
            config.setEnabled(enabled);
            save();
        }
    }

    public void setListenerMultiplier(String key, double multiplier) {
        ListenerConfig config = configData.listeners.get(key);
        if (config != null) {
            config.setMultiplier(multiplier);
            save();
        }
    }

    public void setListenerDailyLimit(String key, int dailyLimit) {
        ListenerConfig config = configData.listeners.get(key);
        if (config != null) {
            config.setDailyLimit(dailyLimit);
            save();
        }
    }

    public void setDailyDecay(int mode, double amount) {
        this.configData.dailyDecayMode = mode;
        this.configData.dailyDecayAmount = amount;
        save();
    }

    public void setDailyDecayEnabled(boolean enabled) {
        this.configData.dailyDecayEnabled = enabled;
        save();
    }

    public void resetAll() {
        this.configData = createDefaults();
        save();
    }
}

