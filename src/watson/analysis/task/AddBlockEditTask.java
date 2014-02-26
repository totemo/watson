package watson.analysis.task;

import watson.Controller;
import watson.db.BlockEdit;

// ----------------------------------------------------------------------------
/**
 * A synchronous task to add a {@link BlockEdit} to the current stored set.
 */
public class AddBlockEditTask implements Runnable
{
  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * @param edit the edit to add when the task is run.
   * @param true if true, the state variables signifying the selected edit are
   *        updated.
   */
  public AddBlockEditTask(BlockEdit edit, boolean updateVariables)
  {
    _edit = edit;
    _updateVariables = updateVariables;
  }

  // --------------------------------------------------------------------------
  /**
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run()
  {
    // TODO: potentially wrong if the server/dimension changes for queued tasks.
    Controller.instance.getBlockEditSet().addBlockEdit(_edit, _updateVariables);
  }

  // --------------------------------------------------------------------------
  /**
   * Edit to store.
   */
  protected BlockEdit _edit;

  /**
   * Whether to update variables signifying the current selection.
   */
  protected boolean   _updateVariables;
} // class AddBlockEditTask