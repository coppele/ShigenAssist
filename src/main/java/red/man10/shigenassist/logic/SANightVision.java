package red.man10.shigenassist.logic;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import red.man10.shigenassist.ShigenAssist;
import red.man10.shigenassist.data.SAType;

public class SANightVision extends SAThreader {

    // Note: 27分18秒(1638秒) はエフェクトで付けれる最大の秒数です。
    public static final PotionEffect EFFECT = new PotionEffect(PotionEffectType.NIGHT_VISION, 32760, 0, false, false);
    public static final String PERMISSION = "ShigenAssist.assist.night_vision";

    private SANightVision() {}

    public static void apply(LivingEntity entity) {
        EFFECT.apply(entity);
    }
    public static void remove(Player player) {
        if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }
    }
    public static void run() {
        run(() -> {
            for (var status : ShigenAssist.getPlayers()) {
                if (status.getData(SAType.NIGHT_VISION).isEnable()) apply(status.getPlayer());
                else remove(status.getPlayer());
            }
        }, 0L, EFFECT.getDuration() - 300L);
    }
}
