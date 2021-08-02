package red.man10.shigenassist.data;

import me.staartvin.statz.datamanager.player.PlayerStat;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import red.man10.shigenassist.ShigenAssist;
import red.man10.shigenassist.data.logic.SAElytra;
import red.man10.shigenassist.data.logic.SANightVision;
import red.man10.shigenassist.event.SAElytraJumpEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Stream;

import static red.man10.shigenassist.ShigenAssist.EETITLE;
import static red.man10.shigenassist.ShigenAssist.SATITLE;

public class SAStatus {

    private static final BukkitScheduler SCHEDULER = Bukkit.getScheduler();

    private final EnumMap<SAType, SAData> data;
    private final Player player;
    private int mining;
    private Inventory saInv, eeInv;
    private SARank rank, next;
    private SAElytra elytra;
    private int nowPage;
    private BukkitTask scoreTask, elytraTask, nightTask;
    private boolean charge, boost;

    public SAStatus(Player player, int mining) {
        this.player = player;
        this.data = new EnumMap<>(SAType.class);
        for (var datum : SAType.values()) data.put(datum, new SAData(datum));
        this.mining = mining;
        this.nowPage = 0;
        this.charge = this.boost = false;
        setRank(ShigenAssist.getRank(this));
    }

    public SAData getData(SAType type) {
        return data.get(type);
    }
    public Player getPlayer() {
        return player;
    }
    public void plusMining() {
        mining++;
    }
    public int getMining() {
        return mining;
    }
    public void openAssistInventory() {
        if (saInv == null) {
            int length = data.size();
            saInv = Bukkit.createInventory(player, (length / 9 + (length % 9 == 0 ? 0 : 1)) * 9, SATITLE);
            for (var type : SAType.values()) saInv.addItem(data.get(type).toItemStack());
            Stream.of(SAType.values()).filter(SAType.getLogics()::contains).forEach(type -> {
                saInv.setItem(List.of(SAType.values()).indexOf(type), data.get(type).toItemStack());
            });
        }
        updatePermission();
        for (var type : SAType.getLogics()) {
            saInv.setItem(List.of(SAType.values()).indexOf(type), data.get(type).toItemStack());
        }
        if (data.get(SAType.NIGHT_VISION).getEnable() == -1) removeNightVision();
        player.openInventory(saInv);
        playSound(Sound.BLOCK_CHEST_OPEN, 1, 2);
    }
    public void openElytraInventory() {
        if (eeInv == null) createElytraInventory();
        player.openInventory(eeInv);
        playSound(Sound.BLOCK_ENDER_CHEST_OPEN, 1, 2);
    }
    public boolean closeInventory() {
        var title = player.getOpenInventory().getTitle();
        if (title.equals(SATITLE) || title.equals(EETITLE)) {
            player.closeInventory();
            return true;
        }
        return false;
    }
    public void setRank(SARank rank) {
        this.rank = rank;
        if (rank == null) {
            next = null;
            return;
        }
        var ranks = List.copyOf(ShigenAssist.getRanks());
        int index = ranks.indexOf(rank) + 1;
        next = index == ranks.size() ? null : ranks.get(index);
    }
    public SARank getRank() {
        return rank;
    }
    public SARank getNextRank() {
        return next;
    }
    public Integer getNextConditionMining() {
        return next == null ? null : next.getConditionsMining() - (mining - rank.getConditionsMining());
    }
    public void setElytra(SAElytra elytra) {
        this.elytra = elytra;
    }
    public SAElytra getElytra() {
        return elytra;
    }
    public void setNowPage(int nowPage) {
        this.nowPage = nowPage;
    }
    public int getNowPage() {
        return nowPage;
    }

    public void createElytraInventory() {
        var elytras = List.copyOf(ShigenAssist.getElytras());
        int size = elytras.size();
        int height = size / 9 + (size % 9 == 0 ? 0 : 1);
        int max = size / 45 + (size % 45 == 0 ? 0 : 1);
        if (height > 5) {
            height = 5;
            if (eeInv == null) eeInv = Bukkit.createInventory(player, 54, EETITLE);
            else eeInv.clear();
            if (nowPage != 0) {
                eeInv.setItem(45, createButton(Material.BOOK, "§7§l最初へ"));
                eeInv.setItem(46, createButton(Material.PAPER, "§7§l前へ"));
                eeInv.setItem(47, createButton(Material.PAPER, "§7§l前へ"));
            }
            eeInv.setItem(49, createButton(Material.COMPASS, "§f§l情報", "§7現在: " + (nowPage + 1) + "ページ目", "§7見返し: " + max + "ページ"));
            if (nowPage + 1 != max) {
                eeInv.setItem(51, createButton(Material.PAPER, "§7§l次へ"));
                eeInv.setItem(52, createButton(Material.PAPER, "§7§l次へ"));
                eeInv.setItem(53, createButton(Material.BOOK, "§7§l最後へ"));
            }
        } else if (eeInv == null) eeInv = Bukkit.createInventory(player, height * 9, EETITLE);
        else eeInv.clear();
        for (var elytra : elytras.subList(height * 9 * nowPage, Math.min(height * 9 * (nowPage + 1), size))) {
            var item = elytra.toItemStack();
            if (this.elytra.equals(elytra)) {
                var meta = item.getItemMeta();
                if (meta != null) {
                    meta.addEnchant(Enchantment.LUCK, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    item.setItemMeta(meta);
                }
            }
            eeInv.addItem(item);
        }
    }
    private ItemStack createButton(Material type, String display, String... lore) {
        var item = new ItemStack(type);
        var meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(display);
        if (lore.length != 0) meta.setLore(List.of(lore));
        item.setItemMeta(meta);
        return item;
    }
    public void playSound(Sound sound, float volume, float pitch) {
        if (sound == null || !(volume > -1) || !(pitch > -1)) return;
        if (data.get(SAType.SOUND).isDisable()) return;
        player.playSound(player.getLocation(), sound, SoundCategory.AMBIENT, volume, pitch);
    }
    public void playSound(String sound, float volume, float pitch) {
        if (sound == null || !(volume > -1) || !(pitch > -1)) return;
        if (data.get(SAType.SOUND).isDisable()) return;
        player.playSound(player.getLocation(), sound, SoundCategory.AMBIENT, volume, pitch);
    }
    public void applyScoreboardOnly() {
        var pluginName = ShigenAssist.getPluginName();
        if (data.get(SAType.SCOREBOARD).isDisable()) {
            var scoreboard = player.getScoreboard();
            if (scoreboard.getObjective(pluginName) != null) scoreboard.clearSlot(DisplaySlot.SIDEBAR);
            return;
        }
        var manager = Bukkit.getScoreboardManager();
        if (manager == null) return;
        var scoreboard = manager.getNewScoreboard();
        var objective = scoreboard.getObjective(pluginName);
        if (objective == null) objective = scoreboard.registerNewObjective(pluginName, "Dummy", SATITLE);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        var scores = new ArrayList<String>();
        var block = player.getLocation().getBlock();
        if (data.get(SAType.LOCATION).isEnable()) {
            scores.add("§a§lX §e" + block.getX() + " §a§lY §e" + block.getY() + " §a§lZ §e" + block.getZ());
        }
        if (data.get(SAType.BIOME).isEnable()) scores.add("§a§lW §e" + block.getWorld().getName() + " §a§lB §e" + block.getBiome().getKey().getKey());
        if (data.get(SAType.RANK).isEnable()) scores.add("§a§lランク§f§l: §e§l" + rank.getDisplay());
        if (data.get(SAType.NEXT_RANK).isEnable()) {
            var mining = getNextConditionMining();
            scores.add("§a§l次まで§f§l: §e§l" + (mining == null ? "N/A" : mining.toString()).replace(".0", ""));
        }
        if (data.get(SAType.ALL_MINED).isEnable()) scores.add("§a§l総採掘量§f§l: §e§l" + mining);
        if (data.get(SAType.NOTICE_DISPLAY).isEnable()) scores.add("§a§l破壊警告§f§l: " + data.get(SAType.NOTICE).getText());
        if (data.get(SAType.NIGHT_VISION_DISPLAY).isEnable()) scores.add("§a§l暗視§f§l: " + data.get(SAType.NIGHT_VISION).getText());
        if (data.get(SAType.ELYTRA_DISPLAY).isEnable()) scores.add("§a§lエリトラ補助§f§l: " + data.get(SAType.ELYTRA).getText());
        if (data.get(SAType.SOUND_DISPLAY).isEnable()) scores.add("§a§lサウンド§f§l: " + data.get(SAType.SOUND).getText());
        for(int i = 0; i < scores.size(); i++) objective.getScore(scores.get(i)).setScore(scores.size() - i);

        player.setScoreboard(scoreboard);
    }
    public void applyScoreboard() {
        if (scoreTask != null && !scoreTask.isCancelled()) return;
        scoreTask = SCHEDULER.runTaskLater(ShigenAssist.getInstance(), () -> {
            updatePermission();
            applyScoreboardOnly();
            scoreTask.cancel();
        }, 10L);
    }
    public void applyNightVision() {
        if (data.get(SAType.NIGHT_VISION).isDisable()) {
            removeNightVision();
            return;
        }
        cancel(nightTask);
        nightTask = SCHEDULER.runTaskTimer(ShigenAssist.getInstance(), () -> {
            if (data.get(SAType.NIGHT_VISION).isDisable()) {
                removeNightVision();
                return;
            }
            if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            }
            player.addPotionEffect(SANightVision.EFFECT);
        }, 0L, SANightVision.EFFECT.getDuration() - 300);
    }
    public void removeNightVision() {
        cancel(nightTask);
        if (data.get(SAType.NIGHT_VISION).isDisable() && player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }
    }
    public boolean canElyTraJump() {
        var chest = player.getInventory().getChestplate();
        var datum = data.get(SAType.ELYTRA);
        return datum.isEnable() && chest != null && chest.getType() == Material.ELYTRA;
    }
    public void elytraJump() {
        if (boost) return;
        cancel(elytraTask);
        charge = true;
        elytraTask = new BukkitRunnable() {
            private final Particle particle = elytra.getParticle();
            private final int standby = ShigenAssist.getElytraStandby() * 20;
            private final int oneBar = standby / 3 + (standby % 3 == 0 ? 0 : 1);
            private int count = 0;

            @SuppressWarnings("deprecation")
            public void run() {
                var gauge = "8888888888".toCharArray();
                Arrays.fill(gauge, 0, Math.min(count, 10), 'c');
                if (count > oneBar) Arrays.fill(gauge, 0, Math.min(count - oneBar, 10), '6');
                if (count > oneBar * 2) Arrays.fill(gauge, 0, Math.min(count - oneBar * 2, 10), 'e');
                var text = "§7▋";
                for (int i = 0; i < gauge.length; i++) {
                    var c = gauge[i];
                    text += (i != 0 && gauge[i - 1] == c ? "" : "§" + c) + '▋';
                }
                sendActionbar(text + "§7▋");
                if (charge) {
                    if (!player.isSneaking() || !canElyTraJump()) charge = false;
                    else if (++count == standby) {
                        charge = false;
                        boost = true;
                    } else return;
                }
                if (boost) {
                    if (count == standby) {
                        var event = new SAElytraJumpEvent(SAStatus.this);
                        Bukkit.getPluginManager().callEvent(event);
                        if (event.isCancelled()) {
                            boost = false;
                            sendActionbar();
                            return;
                        }
                        var eye = player.getEyeLocation();
                        if (player.isOnGround()) eye.setPitch(-90);
                        if (player.isFlying()) player.setFlying(false);
                        player.setVelocity(eye.getDirection().multiply(2));
                    }
                    if (count > oneBar && particle != null && player.isGliding()) player.getWorld().spawnParticle(particle, player.getLocation(), 1, 0, 0, 0, 0);
                    if (count == standby - 2) player.setGliding(true);
                }
                if (count == 0) {
                    boost = false;
                    if (player.isSneaking() && canElyTraJump()) {
                        elytraJump();
                        return;
                    }
                    sendActionbar();
                    cancel();
                    return;
                }
                count--;
            }
        }.runTaskTimer(ShigenAssist.getInstance(), 0L, 1L);
    }
    public void cancelElytraJump() {
        boost = false;
        sendActionbar();
        cancel(elytraTask);
    }
    public void cancelAll() {
        cancel(scoreTask);
        cancelElytraJump();
        removeNightVision();
    }
    public void sendMessage(String text) {
        player.sendMessage(text);
    }
    public void sendActionbar() {
        sendActionbar(new TextComponent());
    }
    public void sendActionbar(String text) {
        sendActionbar(new TextComponent(text));
    }
    public void sendActionbar(TextComponent text) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, text);
    }
    public void updatePermission() {
        for (var type : SAType.getLogics()) {
            var datum = data.get(type);
            var enable = datum.getEnable();
            if (type.hasPermission(player)) {
                if (datum.getEnable() == -1) enable = 0;
            } else enable = -1;
            datum.setEnable(enable);
        }
    }

    private void cancel(BukkitTask task) {
        if (task != null && !task.isCancelled()) {
            task.cancel();
            SCHEDULER.getPendingTasks().remove(task);
        }
    }
    public void savePersistentDataContainer() {
        var player = getPlayer();
        var elytra = getElytra();
        var persistent = player.getPersistentDataContainer();
        for (var type : SAType.values()) {
            persistent.set(type.getKey(), PersistentDataType.INTEGER, data.get(type).getEnable());
        }
        var biome = ShigenAssist.createKey("Biome");
        if (persistent.has(biome, PersistentDataType.STRING)) persistent.remove(biome);
        if (elytra == null) elytra = SAElytra.NONE;
        persistent.set(ShigenAssist.createKey("SelectElytra"), PersistentDataType.STRING, elytra.getDisplay());
    }
    public static SAStatus loadPersistentDataContainer(Player player) {
        var status = new SAStatus(player, ShigenAssist.getBlocksBroken(player));
        var persistent = player.getPersistentDataContainer();
        for (var type : SAType.values()) {
            var datum = status.getData(type);
            var enable = persistent.get(type.getKey(), PersistentDataType.INTEGER);
            if (enable != null) datum.setEnable(enable);
        }
        var display = persistent.get(ShigenAssist.createKey("SelectElytra"), PersistentDataType.STRING);
        status.setElytra(ShigenAssist.getElytra(display));
        status.updatePermission();
        return status;

    }
}