package red.man10.shigenassist;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import red.man10.shigenassist.data.SAType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

import static red.man10.shigenassist.ShigenAssist.EEPREFIX;
import static red.man10.shigenassist.ShigenAssist.SAPREFIX;

@SuppressWarnings("SpellCheckingInspection")
public abstract class SACommand implements CommandExecutor, TabCompleter {

    public static final String PERM_COMMAND = "shigenassist.command";
    protected static ShigenAssist assist;

    private SACommand() {}

    public static void registerCommands() {
        assist = ShigenAssist.getInstance();
        var sa = assist.getCommand("shigenassist");
        if (sa != null) sa.setExecutor(new ShigenAssistCommand());
        var ee = assist.getCommand("elytraeffect");
        if (ee != null) ee.setExecutor(new ElytraEffectCommand());
    }

    public static final class ShigenAssistCommand extends SACommand {

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if (!sender.hasPermission(PERM_COMMAND)) {
                sender.sendMessage(SAPREFIX + "§c権限がありません");
                return true;
            }
            if (args.length == 0) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(SAPREFIX + "§cプレイヤー側からのみ使用できます");
                    sender.sendMessage(SAPREFIX + "§7/sa help からコマンドが見れます！");
                    return true;
                }
                ShigenAssist.getStatus((Player) sender).openAssistInventory();
                return true;
            }
            var arg = args[0].toLowerCase(Locale.ROOT);
            if (arg.equals("help")) {
                var description = assist.getDescription();
                var authors = description.getAuthors();
                var joiner = new StringJoiner(", ");
                authors.forEach(joiner::add);
                var last = authors.get(authors.size() - 1);
                var author = joiner.toString().replace(", " + last, " and " + last);
                sender.sendMessage("=======================" + ShigenAssist.SATITLE + "=======================");
                sender.sendMessage("</sa> : スコアボードの表示設定を開きます。");
                sender.sendMessage("</sa help> : この説明画面を開きます。");
                if (SAType.ELYTRA.hasPermission(sender)) {
                    sender.sendMessage("</ee | /sa elytra> : エリトラ補助のエフェクト設定を開きます。");
                }
                if (sender.isOp()) sender.sendMessage("§7</sa reload> : config.ymlを再読み込みします。");
                sender.sendMessage("Created By " + author);
                sender.sendMessage("Ver " + description.getVersion() + " Released on 2021/07/28");
                sender.sendMessage("=======================" + ShigenAssist.SATITLE + "=======================");
                return true;
            }
            if (arg.equals("elytra") && SAType.ELYTRA.hasPermission(sender)) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(EEPREFIX + "§cプレイヤー側からのみ使用できます");
                    return true;
                }
                ShigenAssist.getStatus((Player) sender).openElytraInventory();
                return true;
            }
            if (arg.equals("reload") && sender.isOp()) {
                assist.reload();
                sender.sendMessage(SAPREFIX + "再読み込みしました！");
                return true;
            }
            sender.sendMessage(SAPREFIX + "§cそのコマンドはないみたいです");
            sender.sendMessage(SAPREFIX + "§7/sa help からコマンドが見れます！");
            return true;
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
            var commands = new ArrayList<String>();

            if (args.length == 1 && sender.hasPermission(PERM_COMMAND)) {
                commands.add("help");
                if (SAType.ELYTRA.hasPermission(sender)) commands.add("elytra");
                if (sender.isOp()) commands.addAll(List.of("reload"));
            }

            if (commands.isEmpty()) return commands;
            var completes = new ArrayList<String>();
            StringUtil.copyPartialMatches(args[args.length - 1], commands, completes);
            return completes;
        }
    }

    public static final class ElytraEffectCommand extends SACommand {

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if (!sender.hasPermission(PERM_COMMAND) || !SAType.ELYTRA.hasPermission(sender)) {
                sender.sendMessage(EEPREFIX + "§c権限がありません");
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage(EEPREFIX + "§cプレイヤー側からのみ使用できます");
                return true;
            }
            ShigenAssist.getStatus((Player) sender).openElytraInventory();
            return true;
        }

        @NotNull
        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
            return List.of();
        }
    }
}