package cn.gmzc.mgactivitys.command;

import cn.gmzc.mgactivitys.MGActivitysPlugin;
import cn.gmzc.mgactivitys.gui.GuiRouter;
import cn.gmzc.mgactivitys.util.Const;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;

public class ActiCommand implements CommandExecutor {

    private final MGActivitysPlugin plugin;

    public ActiCommand(MGActivitysPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("debug")) {
            return handleDebugCommand(sender, args);
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§c该命令只能由玩家执行！");
            return true;
        }
        Player player = (Player) sender;
        if (!hasAdminPermission(player)) {
            player.sendMessage(Const.PREFIX + "§c你没有权限使用此命令！");
            return true;
        }
        GuiRouter.openAdminGUI(plugin, player);
        return true;
    }

    private boolean handleDebugCommand(CommandSender sender, String[] args) {
        if (!hasAdminPermission(sender)) {
            sender.sendMessage(Const.PREFIX + "§c你没有权限使用此命令！");
            return true;
        }
        if (args.length != 4 || !args[1].equalsIgnoreCase("growth")) {
            sender.sendMessage("§e用法: /acti debug growth <玩家名> <成长值>");
            return true;
        }

        double value;
        try {
            value = Double.parseDouble(args[3]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(Const.PREFIX + "§c成长值必须是非负数字。");
            return true;
        }
        if (!Double.isFinite(value) || value < 0) {
            sender.sendMessage(Const.PREFIX + "§c成长值必须是非负数字。");
            return true;
        }

        String playerName = plugin.getActivityManager().resolvePlayerName(args[2]);
        if (!plugin.getActivityManager().setGrowthValue(playerName, value)) {
            sender.sendMessage(Const.PREFIX + "§c成长值设置失败。");
            return true;
        }

        sender.sendMessage(Const.PREFIX + "§a已将 " + playerName + " 的总成长值和动态成长值设置为 §e"
                + Const.formatActivity(value) + "§a。");
        return true;
    }

    private boolean hasAdminPermission(CommandSender sender) {
        return sender.isOp() || sender.hasPermission("mgactivitys.admin");
    }
}
