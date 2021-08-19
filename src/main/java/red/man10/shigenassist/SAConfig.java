package red.man10.shigenassist;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import red.man10.shigenassist.data.SARank;
import red.man10.shigenassist.logic.SAElytra;
import red.man10.shigenassist.logic.SANotice;
import red.man10.shigenassist.logic.SARemarks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SAConfig {

    public static final int PATH_MAX_LENGTH = 31;

    private final ShigenAssist assist;
    private final FileConfiguration config;
    private Set<Material> noticeItems;
    private Set<SANotice> notices;
    private Set<SARank> ranks;
    private Set<SAElytra> elytras;
    private Set<World> cannotUseWorlds;
    private int elytraStandby;
    private boolean loaded = false;

    public SAConfig(boolean infoLogger) {
        assist = ShigenAssist.getInstance();
        assist.saveDefaultConfig();
        assist.reloadConfig();
        config = assist.getConfig();

        SAControl.ASSIST.setEnable(get("Enables.Assist", false, true));
        if (infoLogger) informationControl("Assist", SAControl.ASSIST.isEnable());
        if (SAControl.ASSIST.isDisable()) return;
        for (var logic : SAControl.getLogics()) {
            logic.setEnable(get("Enables.Logics." + logic.getLogic().getName(), false, true));
            if (infoLogger) informationControl("Logics." + logic.getLogic().getName(), logic.isEnable());
        }
        noticeItems = getMaterialSet("ItemDamageNotice.NoticeItems", this::isTool, infoLogger);
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
        }, Comparator.comparingInt(SANotice::getPercentage), infoLogger);
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
        }, Comparator.comparing(SAElytra::getDisplay), infoLogger);
        ranks = getSortedSet("Ranks", (section, key) -> {
            if (!section.isInt(key)) throw new IllegalArgumentException("#value は符号なし整数でなければなりません");
            return new SARank(key, section.getInt(key));
        }, Comparator.comparingInt(SARank::getConditionsMining), infoLogger);
        var list = get("Remarks.RemarksColumn", (List<String>) new ArrayList<String>(), false);
        SARemarks.getColumn().clear();
        list.forEach(SARemarks::addColumn);
        informationList("Remarks.RemarksColumn", list.size(), SARemarks.getColumnSize());
        SARemarks.setPeriod(get("Remarks.Period", 300, true));
        elytraStandby = get("Elytra.Standby", 3, true);
        cannotUseWorlds = get("Elytra.CannotUseWorlds", (List<String>) new ArrayList<String>(), false)
                .stream().map(Bukkit::getWorld).collect(Collectors.toSet());
        loaded = true;
        if (infoLogger) assist.getLogger().info("読み込みました！");
    }
    public void save() {
        config.set("Enables.Assist", SAControl.ASSIST.isEnable());
        for (var logic : SAControl.getLogics()) {
            config.set("Enables.Logics." + logic.getLogic().getName(), logic.isEnable());
        }
        config.set("Remarks.RemarksColumn", SARemarks.getColumn());
        assist.saveConfig();
    }

    public boolean containsItemDamageNotices(Material type) {
        return noticeItems.contains(type);
    }
    public Set<SARank> getRanks() {
        return ranks;
    }
    public Set<SANotice> getNotices() {
        return notices;
    }
    public Set<SAElytra> getElytras() {
        return elytras;
    }
    public int getElytraStandby() {
        return elytraStandby;
    }
    public boolean containsCannotUseWorlds(World world) {
        return cannotUseWorlds.contains(world);
    }
    public boolean isLoaded() {
        return loaded;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String path, T def, boolean saveDefault) {
        if (!config.contains(path)) {
            if (saveDefault) config.set(path, def);
            return def;
        }
        var obj = config.getObject(path, (Class<T>) def.getClass(), def);
        if (!obj.getClass().equals(def.getClass())) {
            if (saveDefault) config.set(path, def);
            return def;
        }
        return obj;
    }
    public Set<Material> getMaterialSet(String path, Predicate<Material> predicate, boolean logger) {
        var set = new HashSet<String>();
        if (config.contains(path)) {
            if (config.isString(path)) set.add(config.getString(path));
            else if (config.isList(path)) set.addAll(config.getStringList(path));
            else if (logger) assist.getLogger().warning(path + " からの値の取得に失敗しました >> config.yml");
        }
        var types = set.stream().flatMap(regex -> {
            var material = Material.matchMaterial(regex);
            if (material != null) return Stream.of(material);
            return Stream.of(Material.values()).filter(type -> type.getKey().getKey().matches(regex));
        }).filter(Objects::nonNull).filter(predicate).collect(Collectors.toSet());
        if (logger) informationList(path, set.size(), types.size());
        return types;
    }
    public <T> Set<T> getSortedSet(String path, BiFunction<ConfigurationSection, String, T> function, Comparator<T> comparator, boolean logger) {
        var keys = new HashSet<String>();
        var sorted = new LinkedHashSet<T>();
        if (config.contains(path) && config.isConfigurationSection(path)) {
            var section = config.getConfigurationSection(path);
            if (section != null) {
                keys.addAll(section.getKeys(false));
                keys.stream().map(key -> {
                    try {
                        return function.apply(section, key);
                    } catch (IllegalArgumentException e) {
                        assist.getLogger().warning(section.getCurrentPath() + "." + key + e.getMessage() + " >> config.yml");
                        return null;
                    }
                }).filter(Objects::nonNull).sorted(comparator).forEachOrdered(sorted::add);
            }
        }
        if (logger) informationList(path, keys.size(), sorted.size());
        return sorted;
    }
    public String getSpace(String text, int length, char space) {
        var chars = new char[length - text.length()];
        Arrays.fill(chars, space);
        return String.valueOf(chars);
    }
    public void informationList(String path, long before, long after) {
        assist.getLogger().info(path + ' ' + getSpace(path, PATH_MAX_LENGTH, '.') + "@ " + before + " / " + after);
    }
    public void informationControl(String name, boolean enable) {
        var path = "Enables." + name;
        assist.getLogger().info(path + ' ' + getSpace(path, PATH_MAX_LENGTH, '.') + "@ " + enable);
    }
    public boolean isTool(Material type) {
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
}
