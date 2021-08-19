package red.man10.shigenassist.logic;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import red.man10.shigenassist.ShigenAssist;
import red.man10.shigenassist.data.SAStatus;
import red.man10.shigenassist.data.SAType;

import java.util.ArrayList;

public class SAScoreboard extends SAThreader {

    public static int MAX_LENGTH = 40;

    public static void apply(SAStatus status) {
        if (!status.getData(SAType.SCOREBOARD).isEnable()) return;
        var player = status.getPlayer();
        var pluginName = ShigenAssist.getPluginName();
        var scoreboard = player.getScoreboard();

        var objective = scoreboard.getObjective(pluginName);
        if (objective != null) objective.unregister();
        objective = scoreboard.registerNewObjective(pluginName, "Dummy", ShigenAssist.SATITLE);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        var scores = new ArrayList<String>();
        var block = player.getLocation().getBlock();
        if (status.getData(SAType.LOCATION).isEnable()) scores.add("§a§lX §e" + block.getX() +
                " §a§lY §e" + block.getY() + " §a§lZ §e" + block.getZ());
        if (status.getData(SAType.BIOME).isEnable()) {
            var biome = block.getBiome().getKey().getKey();
            scores.add("§a§l" + (biome.length() > 10 ? "バ" : "バイオーム") + " §e" + biome);
        }
        if (status.getData(SAType.WORLD).isEnable()) {
            var world = block.getWorld().getName();
            scores.add("§a§l" + (world.length() > 10 ? "ワ" : "ワールド") + " §e" + world);
        }
        if (status.getData(SAType.RANK).isEnable()) scores.add("§a§lランク§f§l: §e§l" + status.getRank().getDisplay());
        if (status.getData(SAType.NEXT_RANK).isEnable()) scores.add("§a§l次まで§f§l: §e§l" + status.getNextRankConditionsMining());
        if (status.getData(SAType.ALL_MINED).isEnable()) scores.add("§a§l総採掘量§f§l: §e§l" + ShigenAssist.getBlocksBroken(player));
        if (status.getData(SAType.NOTICE_DISPLAY).isEnable()) scores.add("§a§l破壊警告§f§l: " + status.getData(SAType.NOTICE).getColorText());
        if (status.getData(SAType.NIGHT_VISION_DISPLAY).isEnable()) scores.add("§a§l暗視§f§l: " + status.getData(SAType.NIGHT_VISION).getColorText());
        if (status.getData(SAType.ELYTRA_DISPLAY).isEnable()) scores.add("§a§lエリトラ補助§f§l: " + status.getData(SAType.ELYTRA).getColorText());
        if (status.getData(SAType.SOUND_DISPLAY).isEnable()) scores.add("§a§lサウンド§f§l: " + status.getData(SAType.SOUND).getColorText());
        if (status.getData(SAType.REMARKS).isEnable()) scores.addAll(SARemarks.getNowRemarks());

        for (int i = 0; i < scores.size(); i++) objective.getScore(scores.get(i)).setScore(scores.size() - i);
        player.setScoreboard(scoreboard);
    }
    public static void remove(Player player) {
        var scoreboard = player.getScoreboard();
        var objective = scoreboard.getObjective(ShigenAssist.SATITLE);
        if (objective != null) scoreboard.clearSlot(DisplaySlot.SIDEBAR);
    }
    public static void run() {
        run(() -> {
            for (var status : ShigenAssist.getPlayers()) {
                status.updateRank();
                SAScoreboard.apply(status);
            }
        }, 0L, 40L);
    }
}
