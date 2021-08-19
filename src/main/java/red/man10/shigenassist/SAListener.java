package red.man10.shigenassist;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import red.man10.shigenassist.data.SAData;
import red.man10.shigenassist.data.SAType;
import red.man10.shigenassist.event.SAElytraJumpEvent;
import red.man10.shigenassist.logic.SANightVision;
import red.man10.shigenassist.logic.SANotice;
import red.man10.shigenassist.logic.SAScoreboard;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static red.man10.shigenassist.ShigenAssist.EETITLE;
import static red.man10.shigenassist.ShigenAssist.SATITLE;

public class SAListener implements Listener {

    private static ShigenAssist assist;
    private static SAConfig config;

    private SAListener() {}

    public static void registerEvents() {
        assist = ShigenAssist.getInstance();
        config = assist.getSAConfig();
        assist.getServer().getPluginManager().registerEvents(new SAListener(), assist);
    }

    @EventHandler(ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        var status = ShigenAssist.getStatus(player);
        if (status.getData(SAType.SCOREBOARD).isEnable()) SAScoreboard.apply(status);
        if (status.getData(SAType.NIGHT_VISION).isEnable()) SANightVision.apply(player);
        status.sendMessage(ShigenAssist.SAPREFIX + "/sa help でコマンドが確認できます！");
    }
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onQuit(PlayerQuitEvent event) {
        var status = ShigenAssist.getStatus(event.getPlayer());
        status.cancelElytraJump();
        ShigenAssist.getPlayers().remove(status);
    }

    @EventHandler(ignoreCancelled = true)
    private void onDeath(PlayerDeathEvent event) {
        ShigenAssist.getStatus(event.getEntity()).cancelElytraJump();
    }
    @EventHandler(ignoreCancelled = true)
    private void onRespawn(PlayerRespawnEvent event) {
        var player = event.getPlayer();
        var status = ShigenAssist.getStatus(player);
        if (status.getData(SAType.NIGHT_VISION).isEnable()) SANightVision.apply(player);
    }

    @EventHandler(ignoreCancelled = true)
    private void onClick(InventoryClickEvent event) {
        var title = event.getView().getTitle();
        if (!List.of(SATITLE, EETITLE).contains(title)) return;
        event.setCancelled(true);
        var player = (Player) event.getWhoClicked();
        var status = ShigenAssist.getStatus(player);
        var inventory = event.getView().getTopInventory();
        var item = event.getCurrentItem();
        if (item == null) return;
        var meta = item.getItemMeta();
        if (meta == null) return;
        int slot = event.getRawSlot();
        if (slot >= inventory.getSize()) return;
        if (title.equals(SATITLE)) {
            SAData datum;
            var lore = meta.getLore();
            if (lore == null || lore.size() < 2) return;
            var name = lore.get(1);
            for (var type : SAType.values()) {
                if (!name.equals("§0" + type.getName())) continue;
                datum = status.getData(type);
                if (!datum.canUse()) {
                    status.playSound(Sound.ENTITY_ITEM_BREAK, 1, 0);
                    status.sendMessage(ShigenAssist.EEPREFIX + "§c使用できません");
                    break;
                }
                datum.setEnable(datum.isDisable());
                SAScoreboard.apply(status);
                status.playSound(Sound.UI_BUTTON_CLICK, 1, 0);
                if (datum.getType() == SAType.NIGHT_VISION) {
                    if (datum.isEnable()) SANightVision.apply(player);
                    else SANightVision.remove(player);
                }
                break;
            }
            status.createAssistInventory();
            player.updateInventory();
            return;
        }
        if (!status.getData(SAType.ELYTRA).isDisable()) {
            status.playSound(Sound.ENTITY_ITEM_BREAK, 1, 0);
            status.sendMessage(ShigenAssist.EEPREFIX + "§c使用できません");
            status.createElytraInventory();
            player.updateInventory();
            return;
        }
        int size = config.getElytras().size();
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
            default -> status.setElytra(assist.getElytra(meta.getDisplayName()));
        }
        status.playSound(Sound.UI_BUTTON_CLICK, 1, 2);
        status.createElytraInventory();
        player.updateInventory();
    }
    @EventHandler(ignoreCancelled = true)
    private void onClose(InventoryCloseEvent event) {
        var player = (Player) event.getPlayer();
        var inventory = event.getInventory();
        if (inventory == player.getInventory()) return;
        var title = event.getView().getTitle();
        if (!List.of(SATITLE, EETITLE).contains(title)) return;
        var status = ShigenAssist.getStatus(player);
        // もし、インベントリにアイテムが入った場合に行う処理です。
        Consumer<ItemStack> consumer = item -> {
            var playerInventory = player.getInventory();
            if (playerInventory.firstEmpty() > -1 || playerInventory.contains(item)) playerInventory.addItem(item);
            else player.getWorld().dropItem(player.getEyeLocation(), item);
        };
        if (title.equals(SATITLE)) {
            status.playSound(Sound.BLOCK_CHEST_CLOSE, 1, 2);
            Stream.of(inventory.getContents()).skip(SAType.values().length)
                    .filter(item -> item != null && !item.getType().isAir()).forEach(consumer);
        } else {
            status.playSound(Sound.BLOCK_ENDER_CHEST_CLOSE, 1, 2);
            Stream.of(inventory.getContents()).skip(inventory.firstEmpty()).limit(Math.min(inventory.getSize(), 45))
                    .filter(item -> item != null && !item.getType().isAir()).forEach(consumer);
        }
        status.savePersistentDataContainer();
    }

    @EventHandler(ignoreCancelled = true)
    private void onDamage(BlockBreakEvent event) {
        var player = event.getPlayer();
        var status = ShigenAssist.getStatus(player);
        if (status.getData(SAType.NOTICE).isDisable()) return;
        var item = player.getInventory().getItemInMainHand();
        var meta = item.getItemMeta();
        if (!config.containsItemDamageNotices(item.getType()) || meta == null) return;
        int max = item.getType().getMaxDurability() - 1;
        int now = max - ((Damageable)meta).getDamage();
        SANotice notice = null;
        for (var sa : config.getNotices()) {
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
    private void onSneak(PlayerToggleSneakEvent event) {
        var player = event.getPlayer();
        var status = ShigenAssist.getStatus(player);
        if (status.getData(SAType.ELYTRA).isEnable() && event.isSneaking() && status.canElyTraJump()
                && !config.containsCannotUseWorlds(player.getWorld())) status.elytraJump();
    }
    @EventHandler(ignoreCancelled = true)
    private void onElytra(SAElytraJumpEvent event) {
        var player = event.getPlayer();
        var chest = player.getInventory().getChestplate();
        if (chest == null || chest.getType() != Material.ELYTRA || !player.isSneaking()) {
            event.setCancelled(true);
            return;
        }
        var status = ShigenAssist.getStatus(player);
        var datum = status.getData(SAType.ELYTRA);
        if (datum.isDisable()) {
            event.setCancelled(true);
            return;
        }
        var elytra = event.getElytra();
        elytra.playSound(status);
        elytra.playEffect(player);
    }
}