package red.man10.shigenassist.logic;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import red.man10.shigenassist.ShigenAssist;

public class SAThreader {

    private static final BukkitScheduler scheduler = Bukkit.getScheduler();
    private static BukkitTask task;

    public static void run(Runnable runnable, long delay, long period) {
        task = scheduler.runTaskTimer(ShigenAssist.getInstance(), runnable, delay, period);
    }
    public static boolean isRunning() {
        return task != null && !task.isCancelled();
    }
    public static void cancel() {
        if (isRunning()) task.cancel();
    }
}
