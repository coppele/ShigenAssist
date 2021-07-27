package red.man10.shigenassist.data.logic;

import org.bukkit.Sound;
import red.man10.shigenassist.data.SAStatus;

import java.util.Locale;

public abstract class SASounder {

    protected String key = null;
    protected Sound sound = null;
    protected float volume = -1, pitch = -1;

    public void setSound(String sound, float volume, float pitch) {
        this.key = sound;
        for (Sound value : Sound.values()) {
            if (!value.name().equals(sound.toUpperCase(Locale.ROOT)) && !value.getKey().getKey().equals(sound)) continue;
            this.sound = value;
            break;
        }
        this.volume = volume;
        this.pitch = pitch;
    }
    public String getKey() {
        return key;
    }
    public Sound getSound() {
        return sound;
    }
    public float getVolume() {
        return volume;
    }
    public float getPitch() {
        return pitch;
    }
    public void playSound(SAStatus status) {
        if (sound == null) status.playSound(key, volume, pitch);
        status.playSound(sound, volume, pitch);
    }
}
