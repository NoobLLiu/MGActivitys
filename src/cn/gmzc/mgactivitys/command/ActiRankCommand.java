package cn.gmzc.mgactivitys.command;

import cn.gmzc.mgactivitys.MGActivitysPlugin;
import cn.gmzc.mgactivitys.gui.GuiRouter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ActiRankCommand implements CommandExecutor {

    private final MGActivitysPlugin plugin;

    public ActiRankCommand(MGActivitysPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c该命令只能由玩家执行！");
            return true;
        }
        GuiRouter.openRankGUI(plugin, (Player) sender);
        return true;
    }
}
