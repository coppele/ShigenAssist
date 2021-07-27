package red.man10.shigenassist;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.meta.Damageable;
import red.man10.shigenassist.data.SAType;
import red.man10.shigenassist.data.logic.SANotice;
import red.man10.shigenassist.event.SAElytraJumpEvent;

import java.util.List;

import static red.man10.shigenassist.ShigenAssist.EETITLE;
import static red.man10.shigenassist.ShigenAssist.SATITLE;

public class SAEvent implements Listener {

    public static void registerEvents() {
        var assist = ShigenAssist.getInstance();
        assist.getServer().getPluginManager().registerEvents(new SAEvent(), assist);
    }

    @EventHandler(ignoreCancelled = true)
    private void onJoin(PlayerJoinEvent event) {
        var status = ShigenAssist.getStatus(event.getPlayer());
        status.addNightVision();
        status.sendMessage(ShigenAssist.SAPREFIX + "/sa help でコマンドを確認できます！");
    }
    @EventHandler(ignoreCancelled = true)
    private void onQuit(PlayerQuitEvent event) {
        ShigenAssist.getStatus(event.getPlayer()).cancelAll();
    }

    @EventHandler(ignoreCancelled = true)
    private void onDeath(PlayerDeathEvent event) {
        ShigenAssist.getStatus(event.getEntity()).cancelElytraJump();
    }
    @EventHandler(ignoreCancelled = true)
    private void onRespawn(PlayerRespawnEvent event) {
        ShigenAssist.getStatus(event.getPlayer()).addNightVision();
    }

    @EventHandler(ignoreCancelled = true)
    private void onClick(InventoryClickEvent event) {
        var title = event.getView().getTitle();
        if (!List.of(SATITLE, EETITLE).contains(title)) return;
        var player = (Player) event.getWhoClicked();
        var status = ShigenAssist.getStatus(player);
        var inventory = event.getClickedInventory();
        if (inventory == null) return;
        if (inventory == player.getInventory()) return;
        if (event.getHotbarButton() != -1) {
            event.setCancelled(true);
            return;
        }
        var item = event.getCurrentItem();
        if (item == null) return;
        int slot = event.getRawSlot();
        event.setCancelled(true);
        if (title.equals(SATITLE)) {
            if (item.getType() == Material.COAL_BLOCK) {
                status.playSound(Sound.ENTITY_ITEM_BREAK, 1, 0);
                status.sendMessage(ShigenAssist.SAPREFIX + "§c権限がありません");
                return;
            }
            var datum = status.getData(SAType.values()[slot]);
            datum.setEnable(datum.isDisable());
            inventory.setItem(slot, datum.toItemStack());
            status.applyScoreboardOnly();
            status.playSound(Sound.UI_BUTTON_CLICK, 1, 0);
            if (datum.getType() != SAType.NIGHT_VISION) return;
            if (datum.isEnable()) status.addNightVision();
            else status.removeNightVision();
            return;
        }
        if (!SAType.ELYTRA.hasPermission(player)) {
            status.playSound(Sound.ENTITY_ITEM_BREAK, 1, 0);
            status.sendMessage(ShigenAssist.SAPREFIX + "§c権限がありません");
            status.closeInventory();
            return;
        }
        var meta = item.getItemMeta();
        if (meta == null) return;
        int size = ShigenAssist.getElytras().size();
        int max = size / 45 + (size % 45 == 0 ? 0 : 1);
        int now = status.getNowPage();
        switch (slot) {
            case 53 -> status.setNowPage(max - 1);
            case 52, 51 -> {
                if (now + 1 == max) return;
                status.setNowPage(now + 1);
            }
            case 49 -> {
                return;
            }
            case 46, 47 -> {
                if (now - 1 == -1) return;
                status.setNowPage(now - 1);
            }
            case 45 -> status.setNowPage(0);
            default -> status.setElytra(ShigenAssist.getElytra(meta.getDisplayName()));
        }
        status.playSound(Sound.UI_BUTTON_CLICK, 1, 2);
        status.createElytraInventory();
    }
    @EventHandler(ignoreCancelled = true)
    private void onClose(InventoryCloseEvent event) {
        var player = (Player) event.getPlayer();
        var status = ShigenAssist.getStatus(player);
        var title = event.getView().getTitle();
        if (event.getInventory() == player.getInventory()) return;
        if (title.equals(SATITLE)) status.playSound(Sound.BLOCK_CHEST_CLOSE, 1, 2);
        else if (title.equals(EETITLE)) status.playSound(Sound.BLOCK_ENDER_CHEST_CLOSE, 1, 2);
        else return;
        status.savePersistentDataContainer();
    }

    @EventHandler(ignoreCancelled = true)
    private void onBreak(BlockBreakEvent event) {
        var status = ShigenAssist.getStatus(event.getPlayer());
        status.plusMining();
        var mining = status.getNextConditionMining();
        if (mining != null && 0 >= mining) status.setRank(status.getNextRank());
        status.applyScoreboard();
    }
    @EventHandler(ignoreCancelled = true)
    private void onDamage(BlockBreakEvent event) {
        var player = event.getPlayer();
        var status = ShigenAssist.getStatus(player);
        if (status.getData(SAType.NOTICE).isDisable()) return;
        var item = player.getInventory().getItemInMainHand();
        var meta = item.getItemMeta();
        if (!ShigenAssist.containsItemDamageNotices(item.getType()) || meta == null) return;
        int max = item.getType().getMaxDurability() - 1;
        int now = max - ((Damageable)meta).getDamage();
        SANotice notice = null;
        for (var sa : ShigenAssist.getNotices()) {
            if (sa.getPercentage() * 0.01 * max >= now) {
                notice = sa;
                break;
            }
        }
        if (notice == null) return;
        var format = notice.getFormat();
        var display = item.getType().getKey().getKey();
        if (meta.hasLocalizedName()) display = meta.getLocalizedName();
        if (meta.hasDisplayName()) display = meta.getDisplayName();
        format = format.replace("%item%", display);
        format = format.replace("%now%", String.valueOf(now)).replace("%max%", String.valueOf(max));
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(format));
        notice.playSound(status);
    }

    @EventHandler(ignoreCancelled = true)
    private void onMove(PlayerMoveEvent event) {
        ShigenAssist.getStatus(event.getPlayer()).applyScoreboard();
    }

    @EventHandler(ignoreCancelled = true)
    private void onSneak(PlayerToggleSneakEvent event) {
        var player = event.getPlayer();
        var status = ShigenAssist.getStatus(player);
        if (!event.isSneaking()) {
            status.cancelElytraJump();
            return;
        }
        var chest = player.getInventory().getChestplate();
        var datum = status.getData(SAType.ELYTRA);
        if (datum.isEnable() && chest != null && chest.getType() == Material.ELYTRA) {
            status.elytraJump();
        }
    }
    @EventHandler(ignoreCancelled = true)
    private void onElytra(SAElytraJumpEvent event) {
        var player = event.getPlayer();
        var status = ShigenAssist.getStatus(player);
        var datum = status.getData(SAType.ELYTRA);
        if (datum.getEnable() == -1) {
            status.sendMessage(ShigenAssist.EEPREFIX + "§c権限がありません");
            event.setCancelled(true);
            return;
        }
        var chest = player.getInventory().getChestplate();
        if (!datum.isEnable() || chest == null || chest.getType() != Material.ELYTRA || !player.isSneaking()) {
            event.setCancelled(true);
            return;
        }
        var elytra = event.getElytra();
        elytra.playSound(status);
        elytra.playEffect(player);
    }
}