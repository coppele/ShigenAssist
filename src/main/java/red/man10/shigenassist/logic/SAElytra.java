package red.man10.shigenassist.logic;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SAElytra extends SASounder {

    public static final SAElytra NONE = new SAElytra(null, Material.BARRIER, "無し");
    public static final String PERMISSION = "ShigenAssist.assist.elytra";

    private final Particle particle;
    private final String display;
    private final Material type;
    private List<String> lore;
    private int cmd, damage, amount;
    private float radius;

    public SAElytra(@Nullable Particle particle, @NotNull Material type, @NotNull String display) {
        this.particle = particle;
        this.type = type;
        this.display = display;
        this.lore = new ArrayList<>();
        this.cmd = this.damage = this.amount = -1;
        this.radius = 0;
    }

    @Nullable
    public Particle getParticle() {
        return particle;
    }
    public Material getType() {
        return type;
    }
    public String getDisplay() {
        return display;
    }
    public void setLore(@NotNull List<String> lore) {
        this.lore = lore;
    }
    public List<String> getLore() {
        return lore;
    }
    public void setCustomModelData(int cmd) {
        this.cmd = cmd;
    }
    public int getCustomModelData() {
        return cmd;
    }
    public void setDamage(int damage) {
        this.damage = damage;
    }
    public int getDamage() {
        return damage;
    }
    public void setRadius(float radius) {
        this.radius = radius;
    }
    public float getRadius() {
        return radius;
    }
    public void setAmount(int amount) {
        this.amount = amount;
    }
    public int getAmount() {
        return amount;
    }

    public void playEffect(@NotNull Player player) {
        if (particle == null) return;
        var center = player.getLocation().add(0, -0.4, 0);
        var world = player.getWorld();
        double increment = (2 * Math.PI) / amount;
        for (int i = 0; i < amount; i++) {
            double angle = i * increment;
            double x = center.getX() + (radius * Math.cos(angle));
            double z = center.getZ() + (radius * Math.sin(angle));
            world.spawnParticle(particle, x, center.getY(), z, 1);
        }
        world.spawnParticle(particle, center, amount * 15, 1, 1, 1, 0.3);
    }
    public ItemStack toItemStack() {
        var item = new ItemStack(type);
        var meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(display);
        var lore = new ArrayList<>(this.lore);
        lore.addAll(List.of("§8ここをクリックして選択します"));
        meta.setLore(lore);
        if (cmd > -1) meta.setCustomModelData(cmd);
        if (damage > -1 && meta instanceof Damageable) {
            var damageable = (Damageable) meta;
            damageable.setDamage(damage);
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        }
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }
}
