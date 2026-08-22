package cn.gmzc.mgactivitys.data;

import cn.gmzc.mgactivitys.model.ActivityData;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public final class ActivityManagerRankingFilterTest {
    private ActivityManagerRankingFilterTest() {
    }

    public static void main(String[] args) {
        File dataFolder = new File(System.getProperty("java.io.tmpdir"), "mgactivitys-ranking-filter-test");
        ActivityManager manager = new ActivityManager(
            dataFolder,
            new ConfigManager(dataFolder, Logger.getLogger("ActivityManagerRankingFilterTest")),
            Logger.getLogger("ActivityManagerRankingFilterTest")
        );
        manager.getPlayersData().put("RealHigh", activity(300.0D));
        manager.getPlayersData().put("Bot_example", activity(999.0D));
        manager.getPlayersData().put("RealLow", activity(100.0D));

        List<Map.Entry<String, ActivityData>> ranked =
            manager.getRankedPlayers(name -> !"Bot_example".equalsIgnoreCase(name));

        assert ranked.size() == 2;
        assert ranked.get(0).getKey().equals("RealHigh");
        assert ranked.get(1).getKey().equals("RealLow");
    }

    private static ActivityData activity(double total) {
        ActivityData data = new ActivityData();
        data.setTotalActivity(total);
        return data;
    }
}
