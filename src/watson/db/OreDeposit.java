package watson.db;

import java.util.TreeSet;

// --------------------------------------------------------------------------
/**
 * Represents a grouping of multiple adjacent blocks of the same ore type to
 * make an ore deposit.
 *
 * In this context, we use the word "adjacent" to mean that one or more of the
 * x, y and z coordinates differ by at most 1.0. That is, the maximum distance
 * between any two "adjacent" blocks of the same type of ore is sqrt(1^2 + 1^2 +
 * 1^2), i.e. sqrt(3). That is because the Minecraft map generator can generate
 * deposits of ores where not all blocks are axis-adjacent - they can be
 * diagonally adjacent.
 *
 * The OreDB class maintains lists of OreDeposits for iron, gold, coal, lapis,
 * diamond, redstone, and emerald. Note that redstone can appear as two block
 * IDs: glowing and non-glowing redstone ore.
 *
 * Invariant: an OreDeposit will always have at least one {@link BlockEdit} in
 * its set, and they will be sorted in ascending order by Y coordinate, and then
 * by timestamp.
 */
public class OreDeposit implements Comparable<OreDeposit>
{
  // ---------------------------------------------------------------------------
  /**
   * Add the specified {@link OreBlock} to this deposit.
   *
   * @param block the block.
   */
  public void addOreBlock(OreBlock block)
  {
    block.setDeposit(this);
    _oreBlocks.add(block);

    // Bookkeeping: oldest and newest edits in the deposit.
    if (_earliestOreBlock == null || block.getEdit().time < _earliestOreBlock.getEdit().time)
    {
      _earliestOreBlock = block;
    }
    if (_latestOreBlock == null || block.getEdit().time > _latestOreBlock.getEdit().time)
    {
      _latestOreBlock = block;
    }
  }

  // ---------------------------------------------------------------------------
  /**
   * Return the timestamp determining the indexing order of this deposit.
   *
   * @return the timestamp determining the indexing order of this deposit.
   */
  public long getTimeStamp()
  {
    return getKeyOreBlock().getEdit().time;
  }

  // ---------------------------------------------------------------------------
  /**
   * Return the OreBlock that will act as a teleport target when heading to this
   * ore.
   *
   * The corresponding edit will have the earliest timestamp of all edits with
   * the least Y coordinate in the deposit. Since {@link OreBlock}s within a
   * deposit are indexed into ascending order by Y and then timestamp, this is
   * just the first element in the collection.
   *
   * @return the OreBlock that will act as a teleport target when heading to
   *         this ore.
   */
  public OreBlock getKeyOreBlock()
  {
    return _oreBlocks.first();
  }

  // ---------------------------------------------------------------------------
  /**
   * Return the earliest edit in the deposit.
   *
   * This is not the same as the key deposit, which is the earliest of those
   * edits with the lowest Y coordinate.
   *
   * @return the earliest edit in the deposit.
   */
  public BlockEdit getEarliestEdit()
  {
    return _earliestOreBlock.getEdit();
  }

  // ---------------------------------------------------------------------------
  /**
   * Return the latest edit in the deposit.
   *
   * @return the latest edit in the deposit.
   */
  public BlockEdit getLatestEdit()
  {
    return _latestOreBlock.getEdit();
  }

  // ---------------------------------------------------------------------------
  /**
   * Return the {@link BlockType} of this deposit.
   *
   * @return the {@link BlockType} of this deposit.
   */
  public BlockType getBlockType()
  {
    return OreDB.getMergedBlockType(getKeyOreBlock().getEdit().type);
  }

  // ---------------------------------------------------------------------------
  /**
   * Return the number of blocks in this deposit.
   *
   * @return the number of blocks in this deposit.
   */
  public int getBlockCount()
  {
    return _oreBlocks.size();
  }

  // ---------------------------------------------------------------------------
  /**
   * Return a reference to the collection of {@link OreBlocks}.
   *
   * The returned collection should be treated as read-only/immutable.
   *
   * @return a reference to the collection of {@link OreBlocks}.
   */
  protected TreeSet<OreBlock> getOreBlocks()
  {
    return _oreBlocks;
  }

  // --------------------------------------------------------------------------
  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   *
   *      Since OreDeposits are compared on the basis of the timestamp of their
   *      key OreBlock, they should not be compared (added to TreeSet<>, etc)
   *      when no OreBlocks have been added to the deposit.
   */
  @Override
  public int compareTo(OreDeposit other)
  {
    return Long.signum(getTimeStamp() - other.getTimeStamp());
  }

  // ---------------------------------------------------------------------------
  /**
   * The set of {@link OreBlock}s making up this deposit, in ascending order by
   * edit timestamp.
   */
  protected TreeSet<OreBlock> _oreBlocks = new TreeSet<OreBlock>();

  /**
   * The {@link OreBlock} in _oreBlocks with the earliest/oldest timestamp.
   */
  protected OreBlock          _earliestOreBlock;

  /**
   * The {@link OreBlock} in _oreBlocks with the latest/newest timestamp.
   */
  protected OreBlock          _latestOreBlock;
} // class OreDeposit