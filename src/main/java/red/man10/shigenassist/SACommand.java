package red.man10.shigenassist;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import red.man10.shigenassist.logic.SARemarks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.stream.Stream;

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
                if (SAControl.ASSIST.isDisable()) {
                    sender.sendMessage(SAPREFIX + "§cこのプラグインは現在オフになっています。");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(SAPREFIX + "§cプレイヤー側からのみ使用できます");
                    sender.sendMessage(SAPREFIX + "§7/sa help からコマンドが見れます");
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
                if (SAControl.ELYTRA.hasPermission(sender)) {
                    sender.sendMessage("</ee | /sa elytra> : エリトラ補助のエフェクト設定を開きます。");
                }
                if (sender.isOp()) {
                    sender.sendMessage("§7</sa on [logic]> : システムを起動します。");
                    sender.sendMessage("§7</sa off [logic]> : システムを停止します。");
                    sender.sendMessage("§7</sa reload> : config.ymlを再読み込みします。");
                    sender.sendMessage("§7</sa load> : config.ymlを読み込みします。");
                    sender.sendMessage("§7</sa save> : config.ymlに保存します。");
                    sender.sendMessage("§7</sa remarks add [text]> : 備考に新しいテキストを追加します。");
                    sender.sendMessage("§7</sa remarks remove [text]> : 備考のテキストを削除します。");
                    sender.sendMessage("§7</sa remarks list> : 備考をまとめて確認します。");
                }
                sender.sendMessage("Created By " + author);
                sender.sendMessage("Ver " + description.getVersion() + " Released on 2021/08/21");
                sender.sendMessage("=======================" + ShigenAssist.SATITLE + "=======================");
                return true;
            }
            if (List.of("switch", "on", "enable", "off", "disable").contains(arg) && sender.isOp()) {
                if (args.length == 1) {
                    sender.sendMessage(SAPREFIX + "§cコマンドが足りないみたいです");
                    sender.sendMessage(SAPREFIX + "§7/sa help からコマンドが見れます");
                    return true;
                }
                var name = args[1].toLowerCase(Locale.ROOT);
                var control = Stream.of(SAControl.values())
                        .filter(sa -> sa.name().toLowerCase(Locale.ROOT).equals(name)).findFirst().orElse(null);
                if (control == null) {
                    sender.sendMessage(SAPREFIX + "§cそのコマンドはないみたいです");
                    sender.sendMessage(SAPREFIX + "§7/sa help からコマンドが見れます");
                    return true;
                }
                var result = switch (arg) {
                    case "on", "enable" -> true;
                    case "off", "disable" -> false;
                    default -> control.isDisable();
                };
                if (result == control.isEnable()) {
                    sender.sendMessage(SAPREFIX + "§c" + name + " はすでに " + result + " に変更されています");
                    return true;
                }
                control.setEnable(result);
                sender.sendMessage(SAPREFIX + "§a" + name + " を " + result + " に変更しました");
                return true;
            }
            if (arg.equals("elytra") && SAControl.ELYTRA.hasPermission(sender)) {
                if (SAControl.ASSIST.isDisable()) {
                    sender.sendMessage(SAPREFIX + "§cこのプラグインは現在オフになっています。");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(EEPREFIX + "§cプレイヤー側からのみ使用できます");
                    return true;
                }
                ShigenAssist.getStatus((Player) sender).openElytraInventory();
                return true;
            }
            if (arg.equals("remarks") && SAControl.REMARKS.hasPermission(sender) && sender.isOp()) {
                if (SAControl.REMARKS.isDisable()) {
                    sender.sendMessage(SAPREFIX + "§cこのプラグインは現在オフになっています。");
                    return true;
                }
                if (args.length == 1) {
                    sender.sendMessage(SAPREFIX + "§cコマンドが足りないみたいです");
                    sender.sendMessage(SAPREFIX + "§7/sa help からコマンドが見れます！");
                    return true;
                }
                arg = args[1].toLowerCase(Locale.ROOT);
                if (arg.equals("next")) {
                    SARemarks.next();
                    sender.sendMessage(SAPREFIX + "§a次のテキストに変更しました");
                    return true;
                }
                if (arg.equals("add")) {
                    if (args.length == 2) {
                        sender.sendMessage(SAPREFIX + "§cコマンドが足りないみたいです");
                        sender.sendMessage(SAPREFIX + "§7/sa help からコマンドが見れます！");
                        return true;
                    }
                    arg = ChatColor.translateAlternateColorCodes('&', args[2]);
                    for (int i = 3; i < args.length; i++) {
                        arg += ' ' + ChatColor.translateAlternateColorCodes('&', args[i]);
                    }
                    if (SARemarks.addColumn(arg)) sender.sendMessage(SAPREFIX + arg + "§a を追加しました！");
                    else sender.sendMessage(SAPREFIX + arg + "§c はすでに存在しているみたいです");
                    return true;
                }
                if (arg.equals("remove")) {
                    if (args.length == 2) {
                        sender.sendMessage(SAPREFIX + "§cコマンドが足りないみたいです");
                        sender.sendMessage(SAPREFIX + "§7/sa help からコマンドが見れます！");
                        return true;
                    }
                    arg = ChatColor.translateAlternateColorCodes('&', args[2]);
                    for (int i = 3; i < args.length; i++) {
                        arg += ' ' + ChatColor.translateAlternateColorCodes('&', args[i]);
                    }
                    if (SARemarks.removeColumn(arg)) sender.sendMessage(SAPREFIX + arg + "§a を削除しました！");
                    else sender.sendMessage(SAPREFIX + arg + "§c は存在していないみたいです");
                    return true;
                }
                if (arg.equals("list")) {
                    sender.sendMessage(SAPREFIX + "§lRemarksList===");
                    for (var remark : SARemarks.getColumn()) sender.sendMessage(SAPREFIX + " - " + remark);
                    sender.sendMessage(SAPREFIX + "§lRemarksList===");
                    return true;
                }
            }
            if (arg.equals("reload") && sender.isOp()) {
                assist.reload();
                sender.sendMessage(SAPREFIX + "§a再読み込みしました！");
                return true;
            }
            if (arg.equals("load") && sender.isOp()) {
                assist.load();
                sender.sendMessage(SAPREFIX + "§a読み込みました！");
                return true;
            }
            if (arg.equals("save") && sender.isOp()) {
                assist.save();
                sender.sendMessage(SAPREFIX + "§a保存しました！");
                return true;
            }
            sender.sendMessage(SAPREFIX + "§cそのコマンドはないみたいです");
            sender.sendMessage(SAPREFIX + "§7/sa help からコマンドが見れます！");
            return true;
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
            var commands = new ArrayList<String>();
            if (!sender.hasPermission(PERM_COMMAND)) return commands;
            Arrays.setAll(args, i -> args[i].toLowerCase(Locale.ROOT));

            switch (args.length) {
                case 1 -> {
                    commands.add("help");
                    if (SAControl.ELYTRA.canUse(sender)) commands.add("elytra");
                    if (sender.isOp()) {
                        commands.addAll(List.of("reload", "load", "save", "on", "off"));
                        if (!args[0].isBlank()) commands.addAll(List.of("switch", "enable", "disable"));
                        if (SAControl.REMARKS.canUse(sender)) commands.add("remarks");
                    }
                }
                case 2 -> {
                    if (sender.isOp()) {
                        if (args[0].equals("remarks") && SAControl.REMARKS.canUse(sender)) {
                            commands.addAll(List.of("next", "add", "remove", "list"));
                        }
                        if (List.of("switch", "on", "enable", "off", "disable").contains(args[0])) {
                            Stream.of(SAControl.values())
                                    .map(type -> type.name().toLowerCase(Locale.ROOT)).forEach(commands::add);
                        }
                    }
                }
                case 3 -> {
                    if (args[0].equals("remarks") && args[1].equals("remove") && SAControl.REMARKS.canUse(sender) && sender.isOp()) {
                        SARemarks.getColumn().stream()
                                .map(remarks -> remarks.replace(ChatColor.COLOR_CHAR, '&')).forEach(commands::add);
                    }
                }
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
            if (SAControl.ELYTRA.isDisable()) {
                sender.sendMessage(EEPREFIX + "§cこのプラグインは現在オフになっています。");
                return true;
            }
            if (!SAControl.ELYTRA.hasPermission(sender)) {
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
        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
            return List.of();
        }
    }
}