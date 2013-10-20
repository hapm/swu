package de.hapm.swu;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.FutureTask;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * A FutureTaskRunnable can be used to queue a FutureTask for execution
 * in the main thread.
 * 
 * @author hapm
 */
public class FutureTaskRunnable extends BukkitRunnable {
    /**
     * Saves the queue for the FutureTasks
     */
    private ConcurrentLinkedQueue<FutureTask<?>> taskQueue = new ConcurrentLinkedQueue<FutureTask<?>>();

    /**
     * Saves the maximal time used to execute tasks from the queue without 
     */
    private long maxTimePerTick = 20;

    /**
     * Saves the associated plugin for this instance.
     */
    private JavaPlugin plugin;
    
    /**
     * Initializes a new instance of the FutureTaskRunnable class.
     * 
     * @param plugin The plugin instance to associate with the FutureTaskRunnable.
     */
    public FutureTaskRunnable(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Sets the maximal time used in one tick to execute queued tasks.
     * 
     * @param timeInMillis Time in milliseconds.
     */
    public void setMaxTimePerTick(long timeInMillis) {
        maxTimePerTick = timeInMillis;
    }
    
    /**
     * Gets the maximal time used in one tick to execute queued tasks.
     * 
     * @return The time in milliseconds.
     */
    public long getMaxTimePerTick() {
        return maxTimePerTick;
    }

    public void run() {
        FutureTask<?> currentTask = taskQueue.poll();
        if (currentTask == null)
            return;
        
        long started = System.currentTimeMillis();
        // using do while to have at least one task being executed.
        do 
        {
            currentTask.run();
        } while (System.currentTimeMillis() - started < maxTimePerTick && (currentTask = taskQueue.poll()) != null);
    }
    
    /**
     * Starts processing by scheduling the FutureTaskRunnable. 
     */
    public void start() {
        runTaskTimer(plugin, 0, 1L);
    }
    
    /**
     * Stops processing by remove the FutureTaskRunnable from the schedule.
     */
    public void stop() {
        cancel();
    }
    
    /**
     * Queues the given task for execution.
     * 
     * @param task The task to queue.
     */
    public void add(FutureTask<?> task) {
        taskQueue.add(task);
    }
}
