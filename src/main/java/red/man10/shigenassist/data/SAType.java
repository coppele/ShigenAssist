package red.man10.shigenassist.data;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import red.man10.shigenassist.ShigenAssist;

public enum SAType implements Keyed {

    SCOREBOARD           ("スコアボード", "Scoreboard", SACategory.DISPLAY),
    LOCATION             ("座標", "Location", SACategory.DISPLAY),
    BIOME                ("バイオーム", "Biome", SACategory.DISPLAY),
    WORLD                ("ワールド", "World", SACategory.DISPLAY),
    RANK                 ("ランク", "Rank", SACategory.DISPLAY),
    NEXT_RANK            ("次のランク", "NextRank", SACategory.DISPLAY),
    ALL_MINED            ("総採掘量", "AllMined", SACategory.DISPLAY),
    NOTICE               ("破壊警告", "Notice", SACategory.LOGIC),
    NOTICE_DISPLAY       ("破壊警告表示", "NoticeDisplay", SACategory.LOGIC_DISPLAY),
    NIGHT_VISION         ("暗視", "NightVision", SACategory.LOGIC),
    NIGHT_VISION_DISPLAY ("暗視表示", "NightVisionDisplay", SACategory.LOGIC_DISPLAY),
    ELYTRA               ("エリトラ補助", "Elytra", SACategory.LOGIC),
    ELYTRA_DISPLAY       ("エリトラ補助表示", "ElytraDisplay", SACategory.LOGIC_DISPLAY),
    SOUND                ("サウンド", "Sound", SACategory.NOT_PERMISSION_LOGIC),
    SOUND_DISPLAY        ("サウンド表示", "SoundDisplay", SACategory.LOGIC_DISPLAY),
    REMARKS              ("備考", "Remarks", SACategory.LOGIC);

    protected final String display, name;
    protected final SACategory category;

    SAType(String display, String name, SACategory category) {
        this.display = display;
        this.name = name;
        this.category = category;
    }

    public String getName() {
        return name;
    }
    public String getDisplay() {
        return display;
    }
    public SACategory getCategory() {
        return category;
    }

    public boolean isDisplay() {
        return category == SACategory.DISPLAY;
    }
    public boolean isLogic() {
        return category == SACategory.LOGIC;
    }
    public boolean isLogicDisplay() {
        return category == SACategory.LOGIC_DISPLAY;
    }
    public SAType getLogicOrDisplay() {
        return switch (this) {
            case NOTICE -> NOTICE_DISPLAY;
            case NOTICE_DISPLAY -> NOTICE;
            case ELYTRA -> ELYTRA_DISPLAY;
            case ELYTRA_DISPLAY -> ELYTRA;
            case NIGHT_VISION -> NIGHT_VISION_DISPLAY;
            case NIGHT_VISION_DISPLAY -> NIGHT_VISION;
            case SOUND -> SOUND_DISPLAY;
            case SOUND_DISPLAY -> SOUND;
            default -> null;
        };
    }

    @Override
    public NamespacedKey getKey() {
        return ShigenAssist.createKey(name);
    }

    public enum SACategory {
        DISPLAY, LOGIC, LOGIC_DISPLAY, NOT_PERMISSION_LOGIC;
    }
}