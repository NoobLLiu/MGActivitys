package cn.gmzc.mgactivitys.data;

import cn.gmzc.mgactivitys.model.ActivityData;
import cn.gmzc.mgactivitys.model.ListenerConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.function.Predicate;

public class ActivityManager {
    public synchronized double getGrowthMultiplier(String playerName) {
        return getPlayerData(playerName).getGrowthMultiplier();
    }

    public synchronized boolean setGrowthMultiplier(String playerName, double value) {
        if (playerName == null || playerName.isBlank() || !Double.isFinite(value) || value < 0) {
            return false;
        }
        ActivityData data = getPlayerData(resolvePlayerName(playerName));
        data.setGrowthMultiplier(Math.max(data.getGrowthMultiplier(), value));
        dirty = true;
        save();
        return true;
    }

    public synchronized boolean resetGrowthMultiplier(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return false;
        }
        ActivityData data = getPlayerData(resolvePlayerName(playerName));
        data.setGrowthMultiplier(1.0);
        dirty = true;
        save();
        return true;
    }

    public synchronized double getExperienceMultiplier(String playerName) {
        return getPlayerData(playerName).getExperienceMultiplier();
    }

    public synchronized boolean setExperienceMultiplier(String playerName, double value) {
        if (playerName == null || playerName.isBlank() || !Double.isFinite(value) || value < 0) {
            return false;
        }
        ActivityData data = getPlayerData(resolvePlayerName(playerName));
        data.setExperienceMultiplier(Math.max(data.getExperienceMultiplier(), value));
        dirty = true;
        save();
        return true;
    }

    public synchronized boolean resetExperienceMultiplier(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return false;
        }
        ActivityData data = getPlayerData(resolvePlayerName(playerName));
        data.setExperienceMultiplier(1.0);
        dirty = true;
        save();
        return true;
    }

    public synchronized int getMaxHp(String playerName) {
        return getPlayerData(playerName).getMaxHp();
    }

    public synchronized int setMaxHp(String playerName, int value) {
        if (playerName == null || playerName.isBlank()) {
            return -1;
        }
        ActivityData data = getPlayerData(resolvePlayerName(playerName));
        int clamped = Math.max(30, Math.min(50, value));
        data.setMaxHp(clamped);
        dirty = true;
        save();
        return clamped;
    }

    public synchronized boolean addStreakBreak(String playerName, int amount) {
        if (playerName == null || playerName.isBlank() || !Double.isFinite(amount) || amount <= 0) {
            return false;
        }
        ActivityData data = getPlayerData(resolvePlayerName(playerName));
        // KBBSToper 负责"每日下降"的算法与节奏，这里仅按派发值即时扣减并持久化，MGActivity 不自行排期跨天扣减。
        data.setTotalActivity(floorActivity(data.getTotalActivity() - amount));
        data.setDynamicActivity(floorActivity(data.getDynamicActivity() - amount));
        data.setStreakBreakCount(data.getStreakBreakCount() + 1);
        dirty = true;
        save();
        return true;
    }
    private final File dataFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final ConfigManager configManager;
    private final Logger logger;
    private LinkedHashMap<String, ActivityData> playersData;
    private boolean dirty;

    public ActivityManager(File dataFolder, ConfigManager configManager, Logger logger) {
        this.dataFile = new File(dataFolder, "playerdata.json");
        this.configManager = configManager;
        this.logger = Objects.requireNonNull(logger, "logger");
        this.playersData = new LinkedHashMap<>();
    }

    public synchronized void load() {
        if (dataFile.exists()) {
            try (FileReader reader = new FileReader(dataFile)) {
                Type type = new TypeToken<LinkedHashMap<String, ActivityData>>() {}.getType();
                LinkedHashMap<String, ActivityData> loaded = gson.fromJson(reader, type);
                if (loaded != null) {
                    this.playersData = loaded;
                }
            } catch (IOException e) {
                this.playersData = new LinkedHashMap<>();
            }
        } else {
            this.playersData = new LinkedHashMap<>();
        }
        dirty = false;
    }

    public synchronized void save() {
        try (FileWriter writer = new FileWriter(dataFile)) {
            gson.toJson(playersData, writer);
            dirty = false;
        } catch (IOException e) {
            logger.log(Level.WARNING, "Unable to save activity data: " + dataFile, e);
        }
    }

    public synchronized void saveIfDirty() {
        if (dirty) {
            save();
        }
    }

    public synchronized ActivityData getPlayerData(String playerName) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd yyyy", Locale.US);
        String today = sdf.format(new Date());

        ActivityData playerData = playersData.get(playerName);
        if (playerData == null) {
            playerData = new ActivityData();
            playerData.setTotalActivity(0);
            playerData.setDynamicActivity(0);
            playerData.setTodayActivity(new LinkedHashMap<>());
            playerData.setLastActiveDate(today);
            playersData.put(playerName, playerData);
            dirty = true;
            return playerData;
        }

        if (playerData.getLastActiveDate() == null || !playerData.getLastActiveDate().equals(today)) {
            // 成长/经验倍率仅在当日由 KBBSToper 等来源生效，次日自动恢复为默认(1x)，不跨日叠加。
            playerData.setGrowthMultiplier(1.0);
            playerData.setExperienceMultiplier(1.0);
            if (configManager.isDailyDecayEnabled()) {
                int decayMode = configManager.getDailyDecayMode();
                double decayAmount = configManager.getDailyDecayAmount();

                double totalActivity = playerData.getTotalActivity();
                double dynamicActivity = playerData.getDynamicActivity();

                if (decayMode == 0) {
                    totalActivity = Math.floor((totalActivity - totalActivity * decayAmount / 100.0) * 10) / 10.0;
                    dynamicActivity = Math.floor((dynamicActivity - dynamicActivity * decayAmount / 100.0) * 10) / 10.0;
                } else {
                    totalActivity = Math.floor((totalActivity - decayAmount) * 10) / 10.0;
                    dynamicActivity = Math.floor((dynamicActivity - decayAmount) * 10) / 10.0;
                }

                if (totalActivity < 0.1) {
                    totalActivity = 0;
                }
                if (dynamicActivity < 0.1) {
                    dynamicActivity = 0;
                }

                playerData.setTotalActivity(totalActivity);
                playerData.setDynamicActivity(dynamicActivity);
            }

            playerData.setTodayActivity(new LinkedHashMap<>());
            playerData.setLastActiveDate(today);
            dirty = true;
        }

        return playerData;
    }

    private static double floorActivity(double value) {
        double result = Math.floor(value * 10) / 10.0;
        return result < 0.1 ? 0 : result;
    }

    public synchronized boolean addActivity(String playerName, String listenerType) {
        ListenerConfig config = configManager.getListenerConfig(listenerType);
        if (config == null || !config.isEnabled()) {
            return false;
        }

        ActivityData playerData = getPlayerData(playerName);
        double currentValue = 0;
        if (playerData.getTodayActivity().containsKey(listenerType)) {
            currentValue = playerData.getTodayActivity().get(listenerType);
        }

        if (config.hasDailyLimit() && currentValue >= config.getDailyLimit()) {
            return false;
        }

        double multiplier = config.getMultiplier();
        playerData.setTotalActivity(playerData.getTotalActivity() + multiplier);
        playerData.setDynamicActivity(playerData.getDynamicActivity() + multiplier);
        playerData.getTodayActivity().put(listenerType, currentValue + multiplier);
        dirty = true;
        return true;
    }

    public synchronized String resolvePlayerName(String requestedName) {
        if (requestedName == null || requestedName.isBlank()) {
            return requestedName;
        }
        for (String storedName : playersData.keySet()) {
            if (storedName.equalsIgnoreCase(requestedName)) {
                return storedName;
            }
        }
        return requestedName;
    }

    public synchronized boolean setGrowthValue(String playerName, double value) {
        if (playerName == null || playerName.isBlank() || !Double.isFinite(value) || value < 0) {
            return false;
        }

        ActivityData playerData = getPlayerData(resolvePlayerName(playerName));
        playerData.setTotalActivity(value);
        playerData.setDynamicActivity(value);
        dirty = true;
        save();
        return true;
    }

    public synchronized List<Map.Entry<String, ActivityData>> getRankedPlayers() {
        return getRankedPlayers(ignored -> true);
    }

    public synchronized List<Map.Entry<String, ActivityData>> getRankedPlayers(
        Predicate<String> includePlayer
    ) {
        Predicate<String> filter = includePlayer == null ? ignored -> true : includePlayer;
        List<Map.Entry<String, ActivityData>> entries = new ArrayList<>(playersData.entrySet());
        entries.removeIf(entry -> !filter.test(entry.getKey()));
        entries.sort((e1, e2) -> Double.compare(e2.getValue().getTotalActivity(), e1.getValue().getTotalActivity()));
        return entries;
    }

    public synchronized void resetAll() {
        playersData.clear();
        dirty = true;
        save();
    }

    public synchronized LinkedHashMap<String, ActivityData> getPlayersData() {
        return playersData;
    }

    public synchronized Map<String, Double> getTotalActivitySnapshot() {
        Map<String, Double> snapshot = new LinkedHashMap<>();
        for (String playerName : new ArrayList<>(playersData.keySet())) {
            snapshot.put(playerName, getPlayerData(playerName).getTotalActivity());
        }
        return Collections.unmodifiableMap(snapshot);
    }
}
