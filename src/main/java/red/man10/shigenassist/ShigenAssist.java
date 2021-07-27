package red.man10.shigenassist;

import me.staartvin.statz.Statz;
import me.staartvin.statz.api.API;
import me.staartvin.statz.datamanager.player.PlayerStat;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import red.man10.shigenassist.data.SARank;
import red.man10.shigenassist.data.SAStatus;
import red.man10.shigenassist.data.SAType;
import red.man10.shigenassist.data.logic.SAElytra;
import red.man10.shigenassist.data.logic.SANotice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@SuppressWarnings("SpellCheckingInspection")
public final class ShigenAssist extends JavaPlugin {

    public static final String SATITLE = "§f§l[§a§lShigenAssist§f§l]§r", SAPREFIX = SATITLE + ' ';
    public static final String EETITLE = "§f§l[§d§lElytraEffect§f§l]§r", EEPREFIX = SATITLE + ' ';
    public static final int PATH_MAX_LENGTH = 31;
    private static FileConfiguration config;
    private static ShigenAssist instance;
    public static API api;
    private static final Set<SAStatus> players = new HashSet<>();
    private static Set<Material> noticeItems;
    private static Set<SANotice> notices;
    private static Set<SARank> ranks;
    private static Set<SAElytra> elytras;
    private static int elytraStandby;
    // privateやpublicがないともやもやする病にかかってます(´･ω･`)

    public ShigenAssist() {
        instance = this;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();
        config = getConfig();
        if (!config.contains("enable") || !config.isBoolean("enable")) config.set("enable", false);
        if (!config.getBoolean("enable")) {
            getLogger().severe("enable が false です >> config.yml");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        var plugin = getServer().getPluginManager().getPlugin("Statz");
        if (plugin == null) {
            getLogger().severe("前提プラグインが導入されていません >> Statz");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!plugin.isEnabled()) {
            getLogger().severe("前提プラグインが起動されていません >> Statz");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        var statz = (Statz) plugin;
        api = statz.getStatzAPI();
        setupConfig();
        SAEvent.registerEvents();
        SACommand.registerCommands();
        var manager = statz.getDataManager();
        getServer().getOnlinePlayers().forEach(player -> {
            var uuid = player.getUniqueId();
            if (!manager.isPlayerLoaded(uuid)) manager.loadPlayerData(uuid, PlayerStat.BLOCKS_BROKEN);
            getStatus(player);
        });
    }

    @Override
    public void onDisable() {
        var text = SAPREFIX + "§cプラグインが停止されました。";
        for (var status : players) {
            var player = status.getPlayer();
            player.sendMessage(text);
            status.closeInventory();
            if (status.getData(SAType.NIGHT_VISION).isEnable()) status.removeNightVision();
            var scoreboard = player.getScoreboard();
            var objective = scoreboard.getObjective(SATITLE);
            if (objective != null) scoreboard.clearSlot(DisplaySlot.SIDEBAR);
            status.cancelAll();
        }
    }

    public void reload() {
        saveDefaultConfig();
        reloadConfig();
        config = getConfig();
        if (!config.contains("enable") || !config.isBoolean("enable")) config.set("enable", false);
        if (!config.getBoolean("enable")) {
            getLogger().severe("enable が false です >> config.yml");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        setupConfig();
    }

    private void setupConfig() {
        noticeItems = getMaterialSet("ItemDamageNotice.NoticeItems", ShigenAssist::isTool);
        notices = getSortedSet("ItemDamageNotice.Percentages", (section, key) -> {
            section = section.getConfigurationSection(key);
            if (section == null) {
                throw new IllegalArgumentException(" の中に何もありません");
            }
            if (!key.matches("^(100|[1-9]?\\d)$")) {
                throw new IllegalArgumentException(" は0〜100までの符号なし整数でなければなりません");
            }
            var notice = new SANotice(Integer.parseInt(key));
            if (!section.contains("Format") || !section.isString("Format")) {
                throw new IllegalArgumentException(".Format が必須です");
            }
            notice.setFormat(section.getString("Format"));
            if (section.contains("Sound") && section.isString("Sound")) {
                var split = section.getString("Sound", "").split(":");
                if (split.length != 3) {
                    throw new IllegalArgumentException(".Sound Sound:volume:pitchでなければなりません");
                }
                if (!split[1].matches("^\\d+(\\.\\d+)?$")) {
                    throw new IllegalArgumentException(".Sound#volume は符号なし整数または小数点でなければなりません");
                }
                if (!split[2].matches("^\\d+(\\.\\d+)?$")) {
                    throw new IllegalArgumentException(".Sound#pitch は符号なし整数または小数点でなければなりません");
                }
                notice.setSound(split[0], NumberUtils.toInt(split[1], 1), NumberUtils.toInt(split[2], 1));
            }
            return notice;
        }, Comparator.comparingInt(SANotice::getPercentage));
        elytras = getSortedSet("Elytra.Effects", (section, key) -> {
            section = section.getConfigurationSection(key);
            if (section == null) {
                throw new IllegalArgumentException(" の中に何もありません");
            }
            if (!section.contains("Effect") && !section.isString("Effect")) {
                throw new IllegalArgumentException(".Effect が必須です");
            }
            if (!section.contains("Type") && !section.isString("Type")) {
                throw new IllegalArgumentException(".Type が必須です");
            }
            var split = section.getString("Effect", "").split(":");
            if (split.length != 3) {
                throw new IllegalArgumentException(".Effect はParticle:radius:amountでなければなりません");
            }
            split[0] = split[0].toUpperCase(Locale.ROOT);
            if (Arrays.stream(Particle.values()).map(Enum::name).noneMatch(split[0]::equals)) {
                throw new IllegalArgumentException(".Effect がParticle内に該当しませんでした");
            }
            if (!split[1].matches("^\\d+(\\.\\d+)?$")) {
                throw new IllegalArgumentException(".Effect#radius は符号なし整数または小数点でなければなりません");
            }
            if (!split[2].matches("^\\d+$")) {
                throw new IllegalArgumentException(".Effect#amount は符号なし整数でなければなりません");
            }
            var type = Material.matchMaterial(section.getString("Type", ""));
            if (type == null) {
                throw new IllegalArgumentException(".Type がMaterial内に該当しませんでした");
            }
            var particle = Particle.valueOf(split[0]);
            var elytra = new SAElytra(particle, type, key);
            elytra.setRadius(Float.parseFloat(split[1]));
            elytra.setAmount(Integer.parseInt(split[2]));
            if (section.contains("Lore") && section.isList("Lore")) {
                elytra.setLore(section.getStringList("ore"));
            }
            if (section.contains("CustomModelData") && section.isInt("CustomModelData")) {
                elytra.setCustomModelData(section.getInt("CustomModelData"));
            }
            if (section.contains("Damage") && section.isInt("Damage")) {
                elytra.setDamage(section.getInt("Damage"));
            }
            if (section.contains("Sound") && section.isString("Sound")) {
                split = section.getString("Sound", "").split(":");
                if (split.length != 3) {
                    throw new IllegalArgumentException(".Sound はSound:volume:pitchでなければなりません");
                }
                if (!split[1].matches("^\\d+(\\.\\d+)?$")) {
                    throw new IllegalArgumentException(".Sound#volume は符号なし整数または小数点でなければなりません");
                }
                if (!split[2].matches("^\\d+(\\.\\d+)?$")) {
                    throw new IllegalArgumentException(".Sound#pitch は符号なし整数または小数点でなければなりません");
                }
                elytra.setSound(split[0], NumberUtils.toInt(split[1], 1), NumberUtils.toInt(split[2], 1));
            }
            return elytra;
        }, Comparator.comparing(SAElytra::getDisplay));
        ranks = getSortedSet("Ranks", ((section, key) -> {
            if (!section.isInt(key)) {
                getLogger().warning(section.getCurrentPath() + " は符号なし整数でなければなりません >> config.yml");
                return null;
            }
            return new SARank(key, section.getInt(key));
        }), Comparator.comparingInt(SARank::getConditionsMining));
        if (config.contains("Elytra.Standby") && config.isInt("Elytra.Standby")) {
            elytraStandby = config.getInt("Elytra.Standby", 3);
        }
        getLogger().info("読み込みました！");
        elytras.add(SAElytra.NONE);
    }
    public Set<Material> getMaterialSet(String path, Predicate<Material> predicate) {
        var set = new HashSet<Material>();
        int loadSize = 0;
        if (config.contains(path)) {
            if (config.isString(path)) {
                loadSize = 1;
                getMaterial(config.getString(path)).stream().filter(predicate).forEach(set::add);
            } else if (config.isList(path)) {
                var values = config.getStringList(path);
                loadSize = values.size();
                values.forEach(value -> getMaterial(value).stream().filter(predicate).forEach(set::add));
            }
        }
        set.remove(null);
        getLogger().info(path + ' ' + getSpace(path, PATH_MAX_LENGTH, '.') + "@ " + set.size() + " / " + loadSize);
        return set;
    }
    public List<Material> getMaterial(String regex) {
        var material = Material.matchMaterial(regex);
        if (material != null) return List.of(material);
        var pattern = Pattern.compile(regex);
        var list = new ArrayList<Material>();
        for (var type : Material.values()) {
            if (pattern.matcher(type.getKey().getKey()).find()) list.add(type);
        }
        return list;
    }
    public <T> Set<T> getSortedSet(String path, BiFunction<ConfigurationSection, String, T> function, Comparator<T> comparator) {
        var set = new LinkedHashSet<T>();
        int loadSize = 0;
        if (config.contains(path) && config.isConfigurationSection(path)) {
            var section = config.getConfigurationSection(path);
            if (section != null) {
                var keys = section.getKeys(false);
                loadSize = keys.size();
                for (var key : keys) {
                    try {
                        T item = function.apply(section, key);
                        if (item != null) set.add(item);
                    } catch (IllegalArgumentException e) {
                        getLogger().warning(section.getCurrentPath() + "." + key + e.getMessage() + " >> config.yml");
                    }
                }
                set = set.stream().sorted(comparator).collect(Collectors.toCollection(LinkedHashSet::new));
            }
        }
        getLogger().info(path + ' ' + getSpace(path, PATH_MAX_LENGTH, '.') + "@ " + set.size() + " / " + loadSize);
        return set;
    }

    public static ShigenAssist getInstance() {
        return instance;
    }
    static SAStatus getStatus(Player player) {
        var uuid = player.getUniqueId();
        return players.stream().filter(status -> status.getPlayer().getUniqueId().equals(uuid)).findFirst().orElseGet(() -> {
            var status = SAStatus.loadPersistentDataContainer(player);
            status.applyScoreboard();
            if (status.getData(SAType.NIGHT_VISION).isEnable()) status.addNightVision();
            players.add(status);
            return status;
        });
    }
    static boolean containsItemDamageNotices(Material type) {
        return noticeItems.contains(type);
    }
    public static SARank getRank(SAStatus status) {
        var ranks = ShigenAssist.getRanks();
        var mined = status.getMining();
        var iterator = List.copyOf(ranks).listIterator(ranks.size());
        while (iterator.hasPrevious()) {
            var rank = iterator.previous();
            if (rank.getConditionsMining() >= mined) continue;
            return iterator.next();
        }
        return null;
    }
    public static Set<SARank> getRanks() {
        return ranks;
    }
    public static Set<SANotice> getNotices() {
        return notices;
    }
    public static SAElytra getElytra(String display) {
        return elytras.stream().filter(elytra -> elytra.getDisplay().equals(display)).findFirst().orElse(SAElytra.NONE);
    }
    public static Set<SAElytra> getElytras() {
        return elytras;
    }
    public static int getElytraStandby() {
        return elytraStandby;
    }

    public static String getPluginName() {
        return instance.getName();
    }
    public static String getSpace(String text, int length, char space) {
        var chars = new char[length - text.length()];
        Arrays.fill(chars, space);
        return String.valueOf(chars);
    }
    public static boolean isTool(Material type) {
        return switch (type) {
            case NETHERITE_SWORD, NETHERITE_PICKAXE, NETHERITE_AXE, NETHERITE_SHOVEL, NETHERITE_HOE:
            case NETHERITE_HELMET, NETHERITE_CHESTPLATE, NETHERITE_LEGGINGS, NETHERITE_BOOTS:
            case DIAMOND_SWORD, DIAMOND_PICKAXE, DIAMOND_AXE, DIAMOND_SHOVEL, DIAMOND_HOE:
            case DIAMOND_HELMET, DIAMOND_CHESTPLATE, DIAMOND_LEGGINGS, DIAMOND_BOOTS:
            case GOLDEN_SWORD, GOLDEN_PICKAXE, GOLDEN_AXE, GOLDEN_SHOVEL, GOLDEN_HOE:
            case GOLDEN_HELMET, GOLDEN_CHESTPLATE, GOLDEN_LEGGINGS, GOLDEN_BOOTS:
            case IRON_SWORD, IRON_PICKAXE, IRON_AXE, IRON_SHOVEL, IRON_HOE:
            case IRON_HELMET, IRON_CHESTPLATE, IRON_LEGGINGS, IRON_BOOTS:
            case STONE_SWORD, STONE_PICKAXE, STONE_AXE, STONE_SHOVEL, STONE_HOE:
            case CHAINMAIL_HELMET, CHAINMAIL_CHESTPLATE, CHAINMAIL_LEGGINGS, CHAINMAIL_BOOTS:
            case WOODEN_SWORD, WOODEN_PICKAXE, WOODEN_AXE, WOODEN_SHOVEL, WOODEN_HOE:
            case LEATHER_HELMET, LEATHER_CHESTPLATE, LEATHER_LEGGINGS, LEATHER_BOOTS:
            case BOW, CROSSBOW, ELYTRA, FLINT_AND_STEEL, SHEARS, SHIELD, TRIDENT, TURTLE_HELMET:
            case FISHING_ROD, CARROT_ON_A_STICK, WARPED_FUNGUS_ON_A_STICK:
                yield true;
            default:
                yield false;
        };
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
//          "^\\w+hyphae$", "^\\w+log$", "^\\w+planks$", "^\\w+ore$", "^\\w+shulker_box$", "^\\w+sign$", "^\\w+slab$",
//          "^\\w+stairs$", "^\\w+table$", "^\\w+wall$", "^\\w+wool$", "^\\w*andesite$", "^\\w*brick\\w*$", "^\\w*campfire$",
//          "^\\w*chiseled\\+$", "^\\w*deepslate\\w*$", "^\\w*diorite$", "^\\w*dirt\\w*$", "^\\w*fence(_gate)?$",
//          "^\\w*glass(_pane)?$", "^\\w*granite$", "^\\w*lantern$", "^\\w*piston$", "^\\w*terracotta$", "^\\w*rail$",
//          "^\\w*sand\\w*$" ]