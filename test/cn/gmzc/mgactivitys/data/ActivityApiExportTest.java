package cn.gmzc.mgactivitys.data;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

public final class ActivityApiExportTest {
    private ActivityApiExportTest() {
    }

    public static void main(String[] args) {
        File dataFolder = new File(System.getProperty("java.io.tmpdir"), "mgactivitys-api-export-test");
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new IllegalStateException("cannot create " + dataFolder);
        }
        ConfigManager configManager = new ConfigManager(dataFolder, Logger.getLogger("ActivityApiExportTest"));
        configManager.load();
        ActivityManager manager = new ActivityManager(dataFolder, configManager, Logger.getLogger("ActivityApiExportTest"));

        // 成长倍率非叠加：只取最高值，不累乘。
        manager.setGrowthMultiplier("Steve", 1.25);
        manager.setGrowthMultiplier("Steve", 1.5);
        manager.setGrowthMultiplier("Steve", 1.1);
        assert manager.getGrowthMultiplier("Steve") == 1.5 : "growth multiplier should take max (non-stacking)";

        // 经验倍率非叠加。
        manager.setExperienceMultiplier("Alex", 2.0);
        manager.setExperienceMultiplier("Alex", 1.25);
        assert manager.getExperienceMultiplier("Alex") == 2.0 : "experience multiplier should take max";

        // reset 命令恢复默认 1x。
        assert manager.resetGrowthMultiplier("Steve") : "reset should succeed";
        assert manager.getGrowthMultiplier("Steve") == 1.0 : "reset growth multiplier to default 1x";
        assert manager.resetExperienceMultiplier("Alex") : "reset should succeed";
        assert manager.getExperienceMultiplier("Alex") == 1.0 : "reset experience multiplier to default 1x";

        // 生命值上限：基础 20，硬顶 50。
        assert manager.setMaxHp("Steve", 60) == 50 : "maxhp hard cap at 50";
        assert manager.getMaxHp("Steve") == 50 : "getMaxHp should return cap 50";
        assert manager.setMaxHp("Steve", 10) == 20 : "maxhp floor at 20";
        assert manager.setMaxHp("Steve", 40) == 40 : "maxhp accepts 40";

        // 次日自动恢复默认倍率：把 lastActiveDate 手动改成其它日期以触发日切换。
        manager.setGrowthMultiplier("Miner", 1.25);
        assert manager.getGrowthMultiplier("Miner") == 1.25 : "set should hold today";
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd yyyy", Locale.US);
        long dayMs = 24L * 60 * 60 * 1000;
        String yesterday = sdf.format(new Date(System.currentTimeMillis() - dayMs));
        manager.getPlayersData().get("Miner").setLastActiveDate(yesterday);
        double after = manager.getGrowthMultiplier("Miner");
        assert after == 1.0 : "growth multiplier should auto-reset to default next day, got " + after;

        // 断签：KBBSToper 派发即按下降值即时扣减并持久化，不再由 MGActivity 自行跨天扣减。
        manager.setGrowthValue("Bob", 100.0);
        assert manager.addStreakBreak("Bob", 2) : "addStreakBreak should succeed";
        assert manager.getPlayerData("Bob").getTotalActivity() == 98.0 : "streak break deducts immediately, got "
            + manager.getPlayerData("Bob").getTotalActivity();
        assert manager.getPlayerData("Bob").getDynamicActivity() == 98.0 : "dynamic activity also deducted";

        System.out.println("ActivityApiExportTest PASSED");
    }
}
