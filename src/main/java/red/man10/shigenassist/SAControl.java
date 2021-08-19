package red.man10.shigenassist;

import org.bukkit.command.CommandSender;
import red.man10.shigenassist.data.SAType;
import red.man10.shigenassist.logic.SAElytra;
import red.man10.shigenassist.logic.SANightVision;
import red.man10.shigenassist.logic.SANotice;
import red.man10.shigenassist.logic.SARemarks;
import red.man10.shigenassist.logic.SAScoreboard;

import java.util.List;

import static red.man10.shigenassist.ShigenAssist.EEPREFIX;
import static red.man10.shigenassist.ShigenAssist.SAPREFIX;

public enum SAControl {
    ASSIST() {
        @Override
        public void setEnable(boolean enable) {
            super.setEnable(enable);
            if (enable) return;
            for (var status : ShigenAssist.getPlayers()) {
                var player = status.getPlayer();
                status.sendMessage(ShigenAssist.SAPREFIX + "§cプラグインが停止されました。");
                status.closeInventory();
                SAScoreboard.remove(player);
                SANightVision.remove(player);
            }
        }
        @Override
        public boolean canUse(CommandSender sender) {
            return isEnable();
        }
    },
    NOTICE(SAType.NOTICE, SANotice.PERMISSION),
    NIGHT_VISION(SAType.NIGHT_VISION, SANightVision.PERMISSION) {
        @Override
        public void setEnable(boolean enable) {
            super.setEnable(enable);
            for (var status : ShigenAssist.getPlayers()) {
                var datum = status.getData(SAType.NIGHT_VISION);
                datum.setUse(enable);
                if (datum.isEnable()) SANightVision.apply(status.getPlayer());
                else SANightVision.remove(status.getPlayer());
            }
            if (enable) SANightVision.run();
            else SANightVision.cancel();
        }
    },
    ELYTRA(SAType.ELYTRA, SAElytra.PERMISSION),
    REMARKS(SAType.REMARKS, SARemarks.PERMISSION);

    private final String permission;
    private final SAType logic;
    private boolean enable;

    SAControl() {
        this(null, null);
    }
    SAControl(SAType logic, String permission) {
        this.logic = logic;
        this.permission = permission;
    }

    public SAType getLogic() {
        return logic;
    }
    public void setEnable(boolean enable) {
        this.enable = enable;
        for (var status : ShigenAssist.getPlayers()) {
            if (logic != null) {
                status.getData(logic).setUse(enable);
                var display = logic.getLogicOrDisplay();
                if (display != null) status.getData(logic.getLogicOrDisplay()).setUse(enable);
            }
            if (enable) continue;
            if (status.isOpenAssistInventory()) status.sendMessage(SAPREFIX + "§c仕様の変更により閉じられました");
            if (status.isOpenElytraInventory()) status.sendMessage(EEPREFIX + "§c仕様の変更により閉じられました");
            status.closeInventory();
        }
    }
    public boolean isEnable() {
        return enable;
    }
    public boolean isDisable() {
        return !enable;
    }
    public boolean hasPermission(CommandSender sender) {
        return logic != null && sender.hasPermission(permission);
    }
    public boolean canUse(CommandSender sender) {
        return ASSIST.enable && enable && hasPermission(sender);
    }

    public static List<SAControl> getLogics() {
        return List.of(SAControl.NOTICE, SAControl.NIGHT_VISION, SAControl.ELYTRA, SAControl.REMARKS);
    }
}
