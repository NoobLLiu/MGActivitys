package cn.gmzc.mgactivitys.api;

import cn.gmzc.mgactivitys.data.ActivityManager;
import cn.gmzc.mgactivitys.data.ConfigManager;
import mc233.fun.kbbstoper.core.platform.MGactivityApi;

import java.io.File;
import java.util.logging.Logger;

/**
 * 验证 KBBSToper 原生对接接口 MGactivityApi 的实现委托语义，
 * 与既有 mgactivity 控制台命令路径保持一致。
 */
public final class MGactivityApiImplTest {
    private MGactivityApiImplTest() {
    }

    public static void main(String[] args) {
        File dataFolder = new File(System.getProperty("java.io.tmpdir"), "mgactivitys-api-impl-test");
        deleteRecursively(dataFolder);
        if (!dataFolder.mkdirs()) {
            throw new IllegalStateException("cannot create " + dataFolder);
        }
        Logger logger = Logger.getLogger("MGactivityApiImplTest");
        ConfigManager configManager = new ConfigManager(dataFolder, logger);
        configManager.load();
        ActivityManager manager = new ActivityManager(dataFolder, configManager, logger);
        MGactivityApi api = new MGactivityApiImpl(manager, logger);

        // 成长倍率：取最大值、不叠加。
        api.setGrowthMultiplier("Steve", 1.25);
        api.setGrowthMultiplier("Steve", 1.5);
        api.setGrowthMultiplier("Steve", 1.1);
        assert manager.getGrowthMultiplier("Steve") == 1.5 : "growth multiplier should take max, got "
            + manager.getGrowthMultiplier("Steve");

        // 经验倍率：取最大值。
        api.setExperienceMultiplier("Alex", 2.0);
        api.setExperienceMultiplier("Alex", 1.25);
        assert manager.getExperienceMultiplier("Alex") == 2.0 : "experience multiplier should take max, got "
            + manager.getExperienceMultiplier("Alex");

        // 生命值上限：绝对值写入，防御性钳制 [30, 50]，跨天保留（不在每日清零范围内）。
        api.setMaxHp("Steve", 999);
        assert manager.getMaxHp("Steve") == 50 : "maxhp hard cap at 50, got " + manager.getMaxHp("Steve");
        api.setMaxHp("Steve", 1);
        assert manager.getMaxHp("Steve") == 30 : "maxhp floor at 30, got " + manager.getMaxHp("Steve");
        api.setMaxHp("Steve", 40);
        assert manager.getMaxHp("Steve") == 40 : "maxhp accepts 40";

        // 连签中断：即时扣减成长值并累加中断计数。
        manager.setGrowthValue("Bob", 100.0);
        api.addStreakBreak("Bob", 5);
        assert manager.getPlayerData("Bob").getTotalActivity() == 95.0 : "streak break deducts immediately, got "
            + manager.getPlayerData("Bob").getTotalActivity();
        assert manager.getPlayerData("Bob").getStreakBreakCount() == 1 : "streak break count increments";
        api.addStreakBreak("Bob", 5);
        assert manager.getPlayerData("Bob").getStreakBreakCount() == 2 : "streak break count accumulates";

        // 星光点：增量累加、立即生效，且必须实际到账（覆盖 default 空实现）。
        api.addStarlightPoints("Steve", 300);
        assert manager.getStarlightPoints("Steve") == 300L : "starlight points accrue, got "
            + manager.getStarlightPoints("Steve");
        api.addStarlightPoints("Steve", 150);
        assert manager.getStarlightPoints("Steve") == 450L : "starlight points accumulate on repeat, got "
            + manager.getStarlightPoints("Steve");

        // 非法参数（空名/负值）不得抛异常，静默拒绝（void 契约）。
        api.setGrowthMultiplier(null, 1.0);
        api.setGrowthMultiplier("  ", Double.NaN);
        api.setExperienceMultiplier(null, 1.0);
        api.setMaxHp("", 40);
        api.addStreakBreak("Bob", -1);
        api.addStarlightPoints(null, 10);
        api.addStarlightPoints("Steve", -1);
        assert manager.getStarlightPoints("Steve") == 450L : "invalid addStarlightPoints must not change";
        assert manager.getPlayerData("Bob").getTotalActivity() == 90.0 : "invalid addStreakBreak must not deduct";

        System.out.println("MGactivityApiImplTest PASSED");
    }

    private static void deleteRecursively(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        if (!file.delete()) {
            throw new IllegalStateException("cannot delete " + file);
        }
    }
}