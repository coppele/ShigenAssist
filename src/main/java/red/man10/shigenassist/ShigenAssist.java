package red.man10.shigenassist;

import me.staartvin.statz.Statz;
import me.staartvin.statz.datamanager.DataManager;
import me.staartvin.statz.datamanager.player.PlayerStat;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import red.man10.shigenassist.data.SARank;
import red.man10.shigenassist.data.SAStatus;
import red.man10.shigenassist.data.SAType;
import red.man10.shigenassist.logic.SAElytra;
import red.man10.shigenassist.logic.SANightVision;
import red.man10.shigenassist.logic.SARemarks;
import red.man10.shigenassist.logic.SAScoreboard;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@SuppressWarnings("SpellCheckingInspection")
public final class ShigenAssist extends JavaPlugin {

    public static final String SATITLE = "§f§l[§a§lShigenAssist§f§l]§r", SAPREFIX = SATITLE + ' ';
    public static final String EETITLE = "§f§l[§d§lElytraEffect§f§l]§r", EEPREFIX = SATITLE + ' ';
    private static ShigenAssist instance;
    private SAConfig config;
    private static DataManager dataManager;
    private static final Set<SAStatus> players = new HashSet<>();

    public ShigenAssist() {
        instance = this;
    }

    @Override
    public void onEnable() {
        config = new SAConfig(true);
        if (!config.isLoaded()) return;
        config.getElytras().add(SAElytra.NONE);
        var statz = getStatz();
        if (statz == null) {
            SAControl.ASSIST.setEnable(false);
            return;
        }
        dataManager = statz.getDataManager();
        SAListener.registerEvents();
        SACommand.registerCommands();
        SAScoreboard.run();
        SANightVision.run();
        SARemarks.run();
        if (SARemarks.getNowRemarks() == null) SARemarks.next();
        for (var player : Bukkit.getOnlinePlayers()) {
            var status = ShigenAssist.getStatus(player);
            if (status.getData(SAType.SCOREBOARD).isEnable()) SAScoreboard.apply(status);
            if (status.getData(SAType.NIGHT_VISION).isEnable()) SANightVision.apply(player);
            status.sendMessage(ShigenAssist.SAPREFIX + "/sa help でコマンドが確認できます！");
        }
    }
    @Override
    public void onDisable() {
        SAControl.ASSIST.setEnable(false);
    }

    void load() {
        config = new SAConfig(false);
        SAScoreboard.cancel();
        SANightVision.cancel();
        SARemarks.cancel();
        if (config.isLoaded()) {
            config.getElytras().add(SAElytra.NONE);
            SAScoreboard.run();
            SANightVision.run();
            SARemarks.run();
        }
    }
    void save() {
        saveDefaultConfig();
        config.save();
    }
    void reload() {
        save();
        load();
    }

    private Statz getStatz() {
        var plugin = getServer().getPluginManager().getPlugin("Statz");
        if (plugin == null) {
            getLogger().severe("前提プラグインが導入されていません >> Statz");
            return null;
        }
        if (!plugin.isEnabled()) {
            getLogger().severe("前提プラグインが起動されていません >> Statz");
            return null;
        }
        return (Statz) plugin;
    }
    public static ShigenAssist getInstance() {
        return instance;
    }
    public SAConfig getSAConfig() {
        return config;
    }
    public static SAStatus getStatus(Player player) {
        var uuid = player.getUniqueId();
        if (!dataManager.isPlayerLoaded(uuid)) dataManager.loadPlayerData(uuid, PlayerStat.BLOCKS_BROKEN);
        for (var status : players) {
            if (!player.isOnline()) players.remove(status);
            else if (status.getPlayer().getUniqueId().equals(uuid)) return status;
        }
        var status = SAStatus.loadPersistentDataContainer(player);
        if (status.getData(SAType.SCOREBOARD).isEnable()) SAScoreboard.apply(status);
        if (status.getData(SAType.NIGHT_VISION).isEnable()) SANightVision.apply(player);
        players.add(status);
        return status;
    }
    public static Set<SAStatus> getPlayers() {
        return players;
    }
    public SARank getRank(Player player) {
        var ranks = config.getRanks();
        var mined = getBlocksBroken(player);
        var iterator = List.copyOf(ranks).listIterator(ranks.size());
        while (iterator.hasPrevious()) {
            var rank = iterator.previous();
            if (rank.getConditionsMining() >= mined) continue;
            return iterator.next();
        }
        return null;
    }
    public static int getBlocksBroken(Player player) {
        var info = dataManager.getPlayerInfo(player.getUniqueId(), PlayerStat.BLOCKS_BROKEN);
        var results = info.getDataOfPlayerStat(PlayerStat.BLOCKS_BROKEN);
        var value = 0;
        if (results != null && !results.isEmpty()) for (var result : results) {
            value += result.getDoubleValue("value");
        }
        return value;

    }
    public SAElytra getElytra(String name) {
        return config.getElytras().stream().filter(elytra -> elytra.getDisplay().equals(name)).findFirst().orElse(SAElytra.NONE);
    }
    public static String getPluginName() {
        return instance.getName();
    }
    public static NamespacedKey createKey(String key) {
        return new NamespacedKey(instance, key);
    }
}
// 負の遺産です(´･ω･`)
// CountMiningBlocks: [ "barrel", "beacon", "bell", "bookshelf", "budding_amethyst", "calcite", "cauldron", "chain",
//          "clay", "composter", "crying_obsidian", "cobblestone", "daylight_detector", "dispenser", "dropper", "gravel",
//          "grindstone", "hopper", "iron_bars", "jukebox", "lectern", "lightning_rod", "lodestone", "loom", "melon",
//          "netherrack", "observer", "redstone_lamp", "respawn_anchor", "shroomlight", "stonecuttor", "tuff",
//          "^bee(_nest|hive)$", "^chorus_(plant|flower)$", "^smooth\\w+$", "^(blast_)?furnace$", "^(blue_|packed_)?ice$",
//          "^(carved_)?pumpkin$", "^(chipped_|damaged_)?anvil$", "^(crimson|warped)_nylium$", "^(ender_|trapped_)?chest$",
//          "^(polished_)?basalt$", "^\\w+banner$", "^\\w+bed$", "^\\w+block$", "^\\w+concrete(_powder)?$", "^\\w+door$",
//          "^\\w+hyphae$", "^\\w+log$", "^\\w+ore$", "^\\w+planks$", "^\\w+shulker_box$", "^\\w+sign$", "^\\w+slab$",
//          "^\\w+stairs$", "^\\w+table$", "^\\w+wall$", "^\\w+wool$", "^\\w*andesite$", "^\\w*brick\\w*$", "^\\w*campfire$",
//          "^\\w*chiseled\\+$", "^\\w*deepslate\\w*$", "^\\w*diorite$", "^\\w*dirt\\w*$", "^\\w*fence(_gate)?$",
//          "^\\w*glass(_pane)?$", "^\\w*granite$", "^\\w*lantern$", "^\\w*piston$", "^\\w*terracotta$", "^\\w*rail$",
//          "^\\w*sand\\w*$" ]