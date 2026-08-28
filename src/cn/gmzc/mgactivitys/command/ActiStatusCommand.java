package cn.gmzc.mgactivitys.command;

import cn.gmzc.mgactivitys.MGActivitysPlugin;
import cn.gmzc.mgactivitys.data.ActivityManager;
import cn.gmzc.mgactivitys.model.ActivityData;
import cn.gmzc.mgactivitys.model.ListenerConfig;
import cn.gmzc.mgactivitys.util.Const;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * 玩家个人状态查询指令 {@code /actistatus}。
 *
 * <p>玩家调用后，以聊天文本展示自己当前的各项状态：总成长值、动态成长值、
 * 今日成长明细，以及来自 KBBSToper 的奖励汇报（成长/经验倍率、生命值上限、
 * 连签中断次数、星光点）。纯只读查询，不修改任何数据。</p>
 */
public class ActiStatusCommand implements CommandExecutor {

    private final MGActivitysPlugin plugin;

    public ActiStatusCommand(MGActivitysPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c该命令只能由玩家执行！");
            return true;
        }

        ActivityManager activityManager = plugin.getActivityManager();
        String resolvedName = activityManager.resolvePlayerName(player.getName());
        ActivityData data = activityManager.getPlayerData(resolvedName);

        player.sendMessage(Const.PREFIX + "§6===== 我的成长状态 §7(" + resolvedName + "§7) §6=====");

        // ---- 成长值信息 ----
        player.sendMessage(Const.PREFIX + "§6总成长值: §a" + Const.formatActivity(data.getTotalActivity()));
        player.sendMessage(Const.PREFIX + "§6动态成长值: §a" + Const.formatActivity(data.getDynamicActivity()));

        // ---- 今日成长明细 ----
        Map<String, Double> today = data.getTodayActivity();
        if (today.isEmpty()) {
            player.sendMessage(Const.PREFIX + "§6今日成长明细: §7今日暂无成长");
        } else {
            player.sendMessage(Const.PREFIX + "§6今日成长明细: §7" + today.size() + " 项");
            for (Map.Entry<String, Double> entry : today.entrySet()) {
                String labelName = listenerDisplayName(entry.getKey());
                player.sendMessage(Const.PREFIX + "  §f" + labelName + ": §a" + Const.formatActivity(entry.getValue()));
            }
        }

        // ---- KBBSToper 奖励汇报 ----
        player.sendMessage(Const.PREFIX + "§e----- §6KBBSToper 奖励汇报 §e-----");
        player.sendMessage(Const.PREFIX + "§6成长倍率: §a" + Const.formatActivity(data.getGrowthMultiplier()) + " §7(次日自动回 1.0)");
        player.sendMessage(Const.PREFIX + "§6经验倍率: §a" + Const.formatActivity(data.getExperienceMultiplier()) + " §7(次日自动回 1.0)");
        player.sendMessage(Const.PREFIX + "§6生命值上限: §a" + data.getMaxHp());
        player.sendMessage(Const.PREFIX + "§6连签中断次数: §a" + data.getStreakBreakCount());
        player.sendMessage(Const.PREFIX + "§6星光点: §b" + data.getStarlightPoints());
        return true;
    }

    private String listenerDisplayName(String listenerKey) {
        ListenerConfig config = plugin.getConfigManager().getListenerConfig(listenerKey);
        return config != null && config.getName() != null && !config.getName().isBlank()
            ? config.getName()
            : listenerKey;
    }
}