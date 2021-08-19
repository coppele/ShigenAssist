package red.man10.shigenassist.data;

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
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import red.man10.shigenassist.SAControl;
import red.man10.shigenassist.ShigenAssist;
import red.man10.shigenassist.event.SAElytraJumpEvent;
import red.man10.shigenassist.logic.SAElytra;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Stream;

import static red.man10.shigenassist.ShigenAssist.EETITLE;
import static red.man10.shigenassist.ShigenAssist.SATITLE;

public class SAStatus {

    private static final BukkitScheduler SCHEDULER = Bukkit.getScheduler();

    private final ShigenAssist assist;
    private final EnumMap<SAType, SAData> data;
    private final Player player;
    private Inventory saInv, eeInv;
    private SARank rank, next;
    private SAElytra elytra;
    private int nowPage;
    private BukkitTask elytraTask;
    private boolean charge, boost;

    public SAStatus(Player player) {
        this.assist = ShigenAssist.getInstance();
        this.player = player;
        this.data = new EnumMap<>(SAType.class);
        for (var type : SAType.values()) data.put(type, new SAData(type));
        this.nowPage = 0;
        this.charge = this.boost = false;
        setRank(assist.getRank(player));
    }

    public SAData getData(SAType type) {
        return data.get(type);
    }
    public Player getPlayer() {
        return player;
    }
    public void setRank(SARank rank) {
        this.rank = rank;
        if (rank == null) {
            next = null;
            return;
        }
        var ranks = List.copyOf(assist.getSAConfig().getRanks());
        int index = ranks.indexOf(rank) + 1;
        next = index == ranks.size() ? null : ranks.get(index);
    }
    public SARank getRank() {
        return rank;
    }
    public SARank getNextRank() {
        return next;
    }
    public String getNextRankConditionsMining() {
        return next == null ? "N/A" : String.valueOf(next.getConditionsMining() - ShigenAssist.getBlocksBroken(player));
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

    public void createAssistInventory() {
        if (saInv == null) {
            int length = data.size(), size = (length / 9 + (length % 9 == 0 ? 0 : 1)) * 9;
            saInv = Bukkit.createInventory(player, size, SATITLE);
        }
        saInv.clear();
        Stream.of(SAType.values()).filter(type -> data.get(type).canUse())
                .forEach(type -> saInv.addItem(data.get(type).toItemStack()));
    }
    public void createElytraInventory() {
        var elytras = List.copyOf(assist.getSAConfig().getElytras());
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
    public void openAssistInventory() {
        createAssistInventory();
        player.openInventory(saInv);
        playSound(Sound.BLOCK_CHEST_OPEN, 1, 2);
    }
    public void openElytraInventory() {
        if (eeInv == null) createElytraInventory();
        player.openInventory(eeInv);
        playSound(Sound.BLOCK_ENDER_CHEST_OPEN, 1, 2);
    }
    public boolean isOpenAssistInventory() {
        return player.getOpenInventory().getTitle().equals(SATITLE);
    }
    public boolean isOpenElytraInventory() {
        return player.getOpenInventory().getTitle().equals(EETITLE);
    }
    public boolean closeInventory() {
        if (isOpenAssistInventory() || isOpenElytraInventory()) {
            player.closeInventory();
            return true;
        }
        return false;
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
    public void updateRank() {
        if (ShigenAssist.getBlocksBroken(player) >= next.getConditionsMining()) setRank(next);
    }
    public boolean canElyTraJump() {
        var chest = player.getInventory().getChestplate();
        if (chest == null || chest.getType() != Material.ELYTRA) return false;
        return data.get(SAType.ELYTRA).isEnable();
    }
    public void elytraJump() {
        if (boost) return;
        cancel(elytraTask);
        charge = true;
        elytraTask = SCHEDULER.runTaskTimer(ShigenAssist.getInstance(), new Runnable() {
            private final Particle particle = elytra.getParticle();
            private final int standby = ShigenAssist.getInstance().getSAConfig().getElytraStandby() * 20;
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
                    elytraTask.cancel();
                    return;
                }
                count--;
            }
        }, 0L, 1L);
    }
    public void cancelElytraJump() {
        boost = false;
        sendActionbar();
        cancel(elytraTask);
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

    private void cancel(BukkitTask task) {
        if (task != null && !task.isCancelled()) task.cancel();
    }
    public void savePersistentDataContainer() {
        var player = getPlayer();
        var elytra = getElytra();
        var persistent = player.getPersistentDataContainer();
        for (var type : SAType.values()) {
            persistent.set(type.getKey(), PersistentDataType.INTEGER, data.get(type).isEnable() ? 1 : 0);
        }
        var biome = ShigenAssist.createKey("BiomeWorld");
        if (persistent.has(biome, PersistentDataType.STRING)) persistent.remove(biome);
        if (elytra == null) elytra = SAElytra.NONE;
        persistent.set(ShigenAssist.createKey("SelectElytra"), PersistentDataType.STRING, elytra.getDisplay());
    }
    public static SAStatus loadPersistentDataContainer(Player player) {
        var status = new SAStatus(player);
        var persistent = player.getPersistentDataContainer();
        for (var type : SAType.values()) {
            var datum = status.getData(type);
            var enable = persistent.get(type.getKey(), PersistentDataType.INTEGER);
            if (enable != null) datum.setEnable(enable == 1);
        }
        for (var control : SAControl.getLogics()) {
            var use = control.canUse(player);
            status.getData(control.getLogic()).setUse(use);
            var display = control.getLogic().getLogicOrDisplay();
            if (display != null) status.getData(display).setUse(use);
        }
        var display = persistent.get(ShigenAssist.createKey("SelectElytra"), PersistentDataType.STRING);
        status.setElytra(ShigenAssist.getInstance().getElytra(display));
        return status;
    }
}