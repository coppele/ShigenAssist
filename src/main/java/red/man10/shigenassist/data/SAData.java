package red.man10.shigenassist.data;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class SAData {

    private int enable = 0;
    private final SAType type;
    private final String enableText, disableText;

    protected SAData(SAType type) {
        this.type = type;
        this.enableText = type.permission == null ? "§a§l表示" : "§a§l有効";
        this.disableText = type.permission == null ? "§c§l非表示" : "§c§l無効";
    }

    public SAType getType() {
        return type;
    }
    public void setEnable(int enable) {
        this.enable = enable;
    }
    public void setEnable(boolean enable) {
        this.enable = enable ? 1 : 0;
    }
    public boolean isEnable() {
        return enable == 1;
    }
    public boolean isDisable() {
        return enable == 0;
    }
    public int getEnable() {
        return enable;
    }
    public String getText() {
        return isEnable() ? enableText : disableText;
    }
    public ItemStack toItemStack() {
        var item = new ItemStack(switch (enable) {
            case -1 -> Material.COAL_BLOCK;
            case 1 -> Material.EMERALD_BLOCK;
            default -> Material.REDSTONE_BLOCK;
        });
        var meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName("§l" + type.display + ": " + getText());
        if (enable == -1) meta.setLore(List.of("§4使用できません"));
        else meta.setLore(List.of("§8ここをクリックで表示切り替え"));
        item.setItemMeta(meta);
        return item;
    }
}
