package cn.gmzc.mgactivitys.data;

import cn.gmzc.mgactivitys.model.ActivityData;
import cn.gmzc.mgactivitys.model.ListenerConfig;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public final class AdvancementGrowthRewardsTest {

    private AdvancementGrowthRewardsTest() {
    }

    public static void main(String[] args) throws Exception {
        Path tempDirectory = Files.createTempDirectory("mgactivitys-advancement-growth-");
        try {
            ConfigManager configManager = new ConfigManager(
                    tempDirectory.toFile(),
                    Logger.getLogger(AdvancementGrowthRewardsTest.class.getName()));
            configManager.load();

            assertUnlimitedReward(configManager.getListenerConfig("advancement"), 100.0, "普通进度");
            assertUnlimitedReward(configManager.getListenerConfig("advancementChallenge"), 1000.0, "挑战进度");

            ActivityManager activityManager = new ActivityManager(
                    tempDirectory.toFile(),
                    configManager,
                    Logger.getLogger(AdvancementGrowthRewardsTest.class.getName()));
            activityManager.load();

            require(activityManager.addActivity("AdvancementTester", "advancement"), "first normal advancement");
            require(activityManager.addActivity("AdvancementTester", "advancement"), "second normal advancement");
            require(activityManager.addActivity("AdvancementTester", "advancementChallenge"), "first challenge advancement");
            require(activityManager.addActivity("AdvancementTester", "advancementChallenge"), "second challenge advancement");

            ActivityData data = activityManager.getPlayerData("AdvancementTester");
            require(data.getTotalActivity() == 2200.0, "total growth should be 2200");
            require(data.getDynamicActivity() == 2200.0, "dynamic growth should be 2200");
            require(data.getTodayActivity().get("advancement") == 200.0, "normal advancement today total");
            require(data.getTodayActivity().get("advancementChallenge") == 2000.0, "challenge advancement today total");
        } finally {
            deleteRecursively(tempDirectory.toFile());
        }
    }

    private static void assertUnlimitedReward(ListenerConfig config, double multiplier, String label) {
        require(config != null, label + " listener is present");
        require(config.isEnabled(), label + " listener is enabled");
        require(config.getMultiplier() == multiplier, label + " multiplier");
        require(!config.hasDailyLimit(), label + " has no daily limit");
        require("不限".equals(config.getDailyLimitDisplay()), label + " displays as unlimited");
    }

    private static void deleteRecursively(File file) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteRecursively(child);
            }
        }
        file.delete();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
