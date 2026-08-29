package cn.gmzc.mgactivitys.command;

import cn.gmzc.mgactivitys.MGActivitysPlugin;
import cn.gmzc.mgactivitys.model.ActivityData;
import cn.gmzc.mgactivitys.util.Const;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ActiStatusCommand implements CommandExecutor {

    private final MGActivitysPlugin plugin;

    public ActiStatusCommand(MGActivitysPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c该命令只能由玩家执行！");
            return true;
        }

        Player player = (Player) sender;
        String playerName = player.getName();
        ActivityData data = plugin.getActivityManager().getPlayerData(playerName);

        sender.sendMessage(Const.PREFIX + "§e=== 我的活动状态 ===");
        sender.sendMessage(Const.PREFIX + "§7成长值: §a" + Const.formatActivity(data.getTotalActivity()));
        sender.sendMessage(Const.PREFIX + "§7成长倍率: §a" + formatMultiplier(data.getGrowthMultiplier()));
        sender.sendMessage(Const.PREFIX + "§7经验倍率: §a" + formatMultiplier(data.getExperienceMultiplier()));
        sender.sendMessage(Const.PREFIX + "§7生命上限: §c" + data.getMaxHp());
        sender.sendMessage(Const.PREFIX + "§7连签中断: §c" + data.getStreakBreakCount());
        return true;
    }

    private String formatMultiplier(double multiplier) {
        if (multiplier == 1.0) {
            return "1.0x";
        }
        return Const.formatActivity(multiplier) + "x";
    }
}
