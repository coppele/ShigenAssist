package red.man10.shigenassist.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import red.man10.shigenassist.logic.SAElytra;
import red.man10.shigenassist.data.SAStatus;

public class SAElytraJumpEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean canceled = false;
    private final SAStatus status;

    public SAElytraJumpEvent(@NotNull SAStatus status) {
        super(status.getPlayer());
        this.status = status;
    }

    public SAStatus getStatus() {
        return status;
    }
    public SAElytra getElytra() {
        return status.getElytra();
    }
    @Override
    public boolean isCancelled() {
        return canceled;
    }
    @Override
    public void setCancelled(boolean cancel) {
        canceled = cancel;
    }
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
