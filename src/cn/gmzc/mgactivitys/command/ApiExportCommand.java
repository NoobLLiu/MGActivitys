package cn.gmzc.mgactivitys.command;

import cn.gmzc.mgactivitys.MGActivitysPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ApiExportCommand implements CommandExecutor {

    private static final char Q = 34; // double-quote character for Java string literals

    private final MGActivitysPlugin plugin;

    public ApiExportCommand(MGActivitysPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args == null || args.length == 0) {
            printUsage(sender);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "setgrowthmultiplier": handleSetGrowthMultiplier(sender, args); break;
            case "getgrowthmultiplier": handleGetGrowthMultiplier(sender, args); break;
            case "resetgrowthmultiplier":handleResetGrowthMultiplier(sender, args); break;
            case "setexperiencemultiplier": handleSetExperienceMultiplier(sender, args); break;
            case "getexperiencemultiplier": handleGetExperienceMultiplier(sender, args); break;
            case "resetexperiencemultiplier": handleResetExperienceMultiplier(sender, args); break;
            case "setmaxhp": handleSetMaxHp(sender, args); break;
            case "getmaxhp": handleGetMaxHp(sender, args); break;
            case "addstreakbreak": handleAddStreakBreak(sender, args); break;
            default: printUsage(sender); break;
        }
        return true;
    }

    private String resolveName(String[] args) {
        if (args.length < 2 || args[1] == null || args[1].isBlank()) {
            return "";
        }
        return plugin.getActivityManager().resolvePlayerName(args[1]);
    }

    private boolean parseDouble(String[] args, int idx) {
        try {
            double v = Double.parseDouble(args[idx]);
            return Double.isFinite(v);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void printUsage(CommandSender sender) {
        String usage = "usage: mgactivity <set|get|reset>growthmultiplier|experiencemultiplier|maxhp|addstreakbreak %PLAYER% [value]";
        sender.sendMessage(usage);
    }

    private void handleSetGrowthMultiplier(CommandSender sender, String[] args) {
        if (args.length < 3 || !parseDouble(args, 2)) {
            printUsage(sender);
            return;
        }
        double value = Double.parseDouble(args[2]);
        String name = resolveName(args);
        boolean ok = plugin.getActivityManager().setGrowthMultiplier(name, value);
        if (!ok) {
            sender.sendMessage("Set growth multiplier failed for " + Q + name + Q);
            return;
        }
        sender.sendMessage("Set growth multiplier for " + Q + name + Q + " to " + value);
    }

    private void handleGetGrowthMultiplier(CommandSender sender, String[] args) {
        if (args.length < 2 || args[1] == null || args[1].isBlank()) {
            printUsage(sender);
            return;
        }
        double value = plugin.getActivityManager().getGrowthMultiplier(resolveName(args));
        sender.sendMessage(String.valueOf(value));
    }

    private void handleResetGrowthMultiplier(CommandSender sender, String[] args) {
        if (args.length < 2 || args[1] == null || args[1].isBlank()) {
            printUsage(sender);
            return;
        }
        String name = resolveName(args);
        boolean ok = plugin.getActivityManager().resetGrowthMultiplier(name);
        sender.sendMessage(ok ? "Reset growth multiplier for " + Q + name + Q + " to default (next day)" : "Reset failed");
    }

    private void handleSetExperienceMultiplier(CommandSender sender, String[] args) {
        if (args.length < 3 || !parseDouble(args, 2)) {
            printUsage(sender);
            return;
        }
        double value = Double.parseDouble(args[2]);
        String name = resolveName(args);
        boolean ok = plugin.getActivityManager().setExperienceMultiplier(name, value);
        if (!ok) {
            sender.sendMessage("Set experience multiplier failed for " + Q + name + Q);
            return;
        }
        sender.sendMessage("Set experience multiplier for " + Q + name + Q + " to " + value);
    }

    private void handleGetExperienceMultiplier(CommandSender sender, String[] args) {
        if (args.length < 2 || args[1] == null || args[1].isBlank()) {
            printUsage(sender);
            return;
        }
        double value = plugin.getActivityManager().getExperienceMultiplier(resolveName(args));
        sender.sendMessage(String.valueOf(value));
    }

    private void handleResetExperienceMultiplier(CommandSender sender, String[] args) {
        if (args.length < 2 || args[1] == null || args[1].isBlank()) {
            printUsage(sender);
            return;
        }
        String name = resolveName(args);
        boolean ok = plugin.getActivityManager().resetExperienceMultiplier(name);
        sender.sendMessage(ok ? "Reset experience multiplier for " + Q + name + Q + " to default" : "Reset failed");
    }

    private void handleSetMaxHp(CommandSender sender, String[] args) {
        if (args.length < 3 || !parseDouble(args, 2)) {
            printUsage(sender);
            return;
        }
        int applied = plugin.getActivityManager().setMaxHp(resolveName(args), (int) Math.round(Double.parseDouble(args[2])));
        sender.sendMessage("Set max hp for " + Q + resolveName(args) + Q + " to " + applied);
    }

    private void handleGetMaxHp(CommandSender sender, String[] args) {
        if (args.length < 2 || args[1] == null || args[1].isBlank()) {
            printUsage(sender);
            return;
        }
        int value = plugin.getActivityManager().getMaxHp(resolveName(args));
        sender.sendMessage(String.valueOf(value));
    }

    private void handleAddStreakBreak(CommandSender sender, String[] args) {
        if (args.length < 3 || !parseDouble(args, 2)) {
            printUsage(sender);
            return;
        }
        int value = (int) Math.round(Double.parseDouble(args[2]));
        String name = resolveName(args);
        boolean ok = plugin.getActivityManager().addStreakBreak(name, value);
        sender.sendMessage(ok ? "Recorded streak break for " + Q + name + Q + " (-2/day)" : "Add streak break failed");
    }
}