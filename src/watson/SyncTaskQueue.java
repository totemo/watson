package watson;

import java.util.concurrent.ConcurrentLinkedQueue;

// ----------------------------------------------------------------------------
/**
 * A queue of tasks that must be run synchronously to the main thread.
 * 
 * This minimises locking and allows various structures such as BlockEditSet to
 * be modified at an appropriate time, when not being traversed, thus avoiding a
 * ConcurrentModificationException.
 */
public class SyncTaskQueue
{
  // --------------------------------------------------------------------------
  /**
   * Single instance.
   */
  public static SyncTaskQueue instance = new SyncTaskQueue();

  // --------------------------------------------------------------------------
  /**
   * Add a task to the queue.
   * 
   * @param task the task.
   */
  public void addTask(Runnable task)
  {
    _taskQueue.add(task);
  }

  // --------------------------------------------------------------------------
  /**
   * Run and dequeue all tasks.
   */
  public void runTasks()
  {
    for (;;)
    {
      Runnable task = _taskQueue.poll();
      if (task == null)
      {
        break;
      }
      task.run();
    }
  }

  // --------------------------------------------------------------------------
  /**
   * Queue of tasks to execute in the order that they should run.
   */
  protected ConcurrentLinkedQueue<Runnable> _taskQueue = new ConcurrentLinkedQueue<Runnable>();
} // class SyncTaskQueue