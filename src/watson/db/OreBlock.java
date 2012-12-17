package watson.db;

import watson.BlockEdit;

// ----------------------------------------------------------------------------
/**
 * Associates the block at a given {@link IntCoord} with the earliest
 * destruction at that location, returned by LogBlock and the {@link OreDeposit}
 * of which it is a part.
 * 
 * Not every {@link BlockEdit} needs a reference to a containing
 * {@link OreDeposit}. Nor does every {@link BlockEdit} need the overhead of a
 * separate object on the heap for its coordinates. Thus, it makes sense to have
 * {@link OreBlock} and {@link BlockEdit} as separate classes.
 */
public class OreBlock implements Comparable<OreBlock>
{
  // --------------------------------------------------------------------------

  public OreBlock(IntCoord location, BlockEdit edit)
  {
    _location = location;
    setEdit(edit);
  }

  // --------------------------------------------------------------------------
  /**
   * Return the location of this block.
   * 
   * @return the location of this block.
   */
  public IntCoord getLocation()
  {
    return _location;
  }

  // --------------------------------------------------------------------------
  /**
   * Set the earliest destruction edit of the block type corresponding to
   * {@link OreDeposit#getBlockType()}.
   * 
   * @param edit the destruction.
   */
  public void setEdit(BlockEdit edit)
  {
    _edit = edit;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the earliest destruction edit of the block type corresponding to
   * {@link OreDeposit#getBlockType()}.
   * 
   * @return the earliest destruction edit of the block type corresponding to
   *         {@link OreDeposit#getBlockType()}.
   */
  public BlockEdit getEdit()
  {
    return _edit;
  }

  // --------------------------------------------------------------------------
  /**
   * Set the {@link OreDeposit} that includes this block.
   * 
   * @param deposit the deposit.
   */
  public void setDeposit(OreDeposit deposit)
  {
    _deposit = deposit;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the {@link OreDeposit} that includes this block.
   * 
   * @return the {@link OreDeposit} that includes this block.
   */
  public OreDeposit getDeposit()
  {
    return _deposit;
  }

  // --------------------------------------------------------------------------
  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   * 
   *      The "least" {@link OreBlock} (which will be indexed into the
   *      {@link OreDeposit} first. After that, blocks with the same Y
   *      coordinate should be ordered in ascending order by timestamp, and
   *      after that, compare X and Z to disambiguate.
   */
  @Override
  public int compareTo(OreBlock other)
  {
    if (getLocation().getY() != other.getLocation().getY())
    {
      return getLocation().getY() - other.getLocation().getY();
    }
    else if (getEdit().time != other.getEdit().time)
    {
      return Long.signum(getEdit().time - other.getEdit().time);
    }
    else if (getLocation().getX() != other.getLocation().getX())
    {
      return getLocation().getX() - other.getLocation().getX();
    }
    else if (getLocation().getZ() != other.getLocation().getZ())
    {
      return getLocation().getZ() - other.getLocation().getZ();
    }
    else
    {
      // Then I guess it's the same one. Probably this series of tests won't
      // get past the time comparison in most cases anyway unless it truly is
      // equal.
      return 0;
    }
  } // compareTo

  // --------------------------------------------------------------------------
  /**
   * The 3-D location of the block.
   */
  protected IntCoord   _location;

  /**
   * The earliest destruction edit of the block type corresponding to
   * {@link OreDeposit#getBlockType()}.
   */
  protected BlockEdit  _edit;

  /**
   * The {@link OreDeposit} that includes this block.
   */
  protected OreDeposit _deposit;

} // class OreBlock