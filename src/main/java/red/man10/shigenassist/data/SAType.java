package red.man10.shigenassist.data;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import red.man10.shigenassist.ShigenAssist;

import java.util.List;

@SuppressWarnings("SpellCheckingInspection")
public enum SAType implements Keyed {

    SCOREBOARD           ("スコアボード", "Scoreboard", null),
    LOCATION             ("座標", "Location", null),
    BIOME                ("バイオームとワールド", "BiomeWorld", null),
    RANK                 ("ランク", "Rank", null),
    NEXT_RANK            ("次のランク", "NextRank", null),
    ALL_MINED            ("総採掘量", "AllMined", null),
    NOTICE               ("破壊警告", "Notice", "shigenassist.assist.notice"),
    NOTICE_DISPLAY       ("破壊警告表示", "NoticeDisplay", null),
    NIGHT_VISION         ("暗視", "NightVision", "shigenassist.assist.night_vision"),
    NIGHT_VISION_DISPLAY ("暗視表示", "NightVisionDisplay", null),
    ELYTRA               ("エリトラ補助", "Elytra", "shigenassist.assist.elytra"),
    ELYTRA_DISPLAY       ("エリトラ補助表示", "ElytraDisplay", null),
    SOUND                ("サウンド", "Sound", ""),
    SOUND_DISPLAY        ("サウンド表示", "SoundDisplay", null);

    private static final List<SAType> logics = List.of(SAType.NOTICE, SAType.ELYTRA, SAType.NIGHT_VISION);
    protected final String display, name, permission;

    SAType(String display, String name, String permission) {
        this.display = display;
        this.name = name;
        this.permission = permission;
    }

    public String getName() {
        return name;
    }
    public String getDisplay() {
        return display;
    }
    public String getPermission() {
        return permission;
    }

    public boolean hasPermission(CommandSender sender) {
        return permission != null && sender.hasPermission(permission);
    }
    public boolean isLogic() {
        return logics.contains(this);
    }
    public static List<SAType> getLogics() {
        return logics;
    }
    @Override
    public NamespacedKey getKey() {
        return ShigenAssist.createKey(name);
    }
}