package watson;

import java.util.Random;
import java.util.TreeSet;

// ----------------------------------------------------------------------------
/**
 * Maintains a time-ordered list of all of the BlockEdit instances corresponding
 * to LogBlock results, ordered from oldest to most recent.
 */
public class BlockEditSet
{
  // --------------------------------------------------------------------------
  /**
   * Remove all entries from the list.
   */
  public BlockEditSet()
  {
    // Nothing.
  }

  // --------------------------------------------------------------------------
  /**
   * Remove all entries from the list.
   */
  public void clear()
  {
    _edits.clear();
  }

  // --------------------------------------------------------------------------
  /**
   * Find an edit with the specified coordinates and, optionally, player.
   * 
   * Currently, this method does an inefficient linear search, walking, on
   * average, half the collection. The edits are searched from oldest to newest,
   * meaning that the oldest edit at that coordinate will be retrieved.
   * 
   * TODO: implement an efficient spatial search.
   * 
   * @param x the x coordinate of the block
   * @param y the y coordinate of the block
   * @param z the z coordinate of the block
   * @param player the player name (can be null for a wildcard).
   * @return the matching edit, or null if not found.
   */
  public BlockEdit findEdit(int x, int y, int z, String player)
  {
    // Warning: O(N) algorithm. Avert your delicate eyes.
    for (BlockEdit edit : _edits)
    {
      if (edit.x == x && edit.y == y && edit.z == z
          && (player == null || edit.player.equals(player)))
      {
        return edit;
      }
    }
    return null;
  } // findEdit

  // --------------------------------------------------------------------------
  /**
   * Add the specified edit to the list.
   * 
   * @param edit the BlockEdit describing an edit to add.
   */
  public void addBlockEdit(BlockEdit edit)
  {
    _edits.add(edit);
  }

  // --------------------------------------------------------------------------
  /**
   * Draw wireframe outlines of all blocks.
   */
  public void drawOutlines()
  {
    if (Controller.instance.isOutlineShown())
    {
      for (BlockEdit edit : _edits)
      {
        edit.drawOutline();
      }
    }
  }

  // --------------------------------------------------------------------------
  /**
   * Draw direction vectors indicating motion of the miner.
   */
  public void drawVectors()
  {
  }

  // --------------------------------------------------------------------------
  /**
   * Create one of each distinct block, for testing.
   */
  public void createTestGallery()
  {
    final int y = 4; // For superflat worlds.

    BlockType unknown = BlockTypeRegistry.instance.getBlockTypeByName("unknown");
    long startTime = System.currentTimeMillis() - 3600 * 1000;
    int madeSoFar = 0;
    for (int i = 0; i < 4096; ++i)
    {
      BlockType type = BlockTypeRegistry.instance.getBlockTypeByIndex(i);
      if (type != unknown)
      {
        addBlockEdit(new BlockEdit(startTime + 1000 * madeSoFar, "totemo",
          true, madeSoFar, y, 0, type));
        ++madeSoFar;
      }
    }
    addBlockEdit(new BlockEdit(startTime + 1000 * madeSoFar, "totemo", true,
      madeSoFar, y, 0, unknown));
  } // createTestGallery

  // --------------------------------------------------------------------------
  /**
   * Create a set of random BlockEdit instances clustered around (0, 64, 0),
   * useful for testing.
   */
  public void createTestData(int count)
  {
    // Random number generator with fixed seed for repeatability.
    Random r = new Random(1);

    // Total range of random coordinate values.
    final int xzCoordRange = count / 50;
    final int yCoordRange = 50;

    // Generate time stamps starting one hour ago, and getting progressively
    // newer by one second for each block added.
    long startTime = System.currentTimeMillis() - 3600 * 1000;
    for (int i = 0; i < count; ++i)
    {
      int x = r.nextInt(xzCoordRange) - xzCoordRange / 2;
      int y = 64 + r.nextInt(yCoordRange) - yCoordRange / 2;
      int z = r.nextInt(xzCoordRange) - xzCoordRange / 2;
      int id = r.nextInt(135);
      addBlockEdit(new BlockEdit(startTime + 1000 * i, "totemo", true, x, y, z,
        BlockTypeRegistry.instance.getBlockTypeById(id)));
    }
  } // createTestData

  // --------------------------------------------------------------------------
  /**
   * Start animating all of the edits in the list.
   */
  public void startAnimating()
  {
    // Record current time as start time.
    // Set cursor in _edits to oldest edit position.
  }

  // --------------------------------------------------------------------------
  /**
   * A set of BlockEdit instances, ordered from oldest (lowest time value) to
   * most recent.
   */
  TreeSet<BlockEdit> _edits = new TreeSet<BlockEdit>(new BlockEditComparator());
} // class BlockEditList

