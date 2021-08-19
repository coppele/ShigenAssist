package red.man10.shigenassist.data;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class SAData {

    private boolean enable, use;
    private final SAType type;
    private final String enableText, disableText;

    protected SAData(SAType type) {
        this.enable = this.use = true;
        this.type = type;
        this.enableText = type.isLogic() ? "表示" : "有効";
        this.disableText = type.isLogic() ? "非表示" : "無効";
    }

    public SAType getType() {
        return type;
    }
    public void setEnable(boolean enable) {
        this.enable = enable;
    }
    public boolean isEnable() {
        return type.isDisplay() ? enable : enable && use;
    }
    public boolean isDisable() {
        return !isEnable();
    }
    public void setUse(boolean use) {
        this.use = use;
    }
    public boolean canUse() {
        return use;
    }
    public String getColor() {
        return enable ? "§a§l" : "§c§l";
    }
    public String getText() {
        return enable ? enableText : disableText;
    }
    public String getColorText() {
        return getColor() + getText();
    }
    public ItemStack toItemStack() {
        var item = new ItemStack(enable ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK);
        var meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName("§l" + type.display + ": " + getColorText());
        meta.setLore(List.of("§8ここをクリックで" + getText() + "に切り替え", "§0" + type.getName()));
        item.setItemMeta(meta);
        return item;
    }
}
