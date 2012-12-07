package watson.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import watson.BlockEdit;

// ----------------------------------------------------------------------------
/**
 * A spatial (3-D) database of ores of the same type.
 * 
 * {@link OreDB} maintains a separate instance of this class for each of the
 * Minecraft ore types.
 */
public class TypedOreDB
{
  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * @param initialCapacity the initial capacity of the HashMap<> that maps
   *          {@link IntCoord}s to {@link OreBlock} instances. This value should
   *          be chosen to minimise hash collisions and the need to rehash in
   *          typical mining investigations.
   */
  public TypedOreDB(int initialCapacity)
  {
    _oreBlocks = new HashMap<IntCoord, OreBlock>(initialCapacity);
  }

  // --------------------------------------------------------------------------
  /**
   * Clear all ore information from the database.
   */
  public void clear()
  {
    _oreBlocks.clear();
    _oreDeposits.clear();
  }

  // --------------------------------------------------------------------------
  /**
   * Return the number of {@link OreDeposit}s in the database.
   * 
   * @return the number of {@link OreDeposit}s in the database.
   */
  public int getOreDepositCount()
  {
    return _oreDeposits.size();
  }

  // --------------------------------------------------------------------------
  /**
   * Return a reference to the {@link OreDeposit} with the specified 1-based
   * index.
   * 
   * An index less than one wraps around to the maximum, and an index greater
   * than the maximum wraps around to 1.
   * 
   * @param index the 1-based index of the {@link OreDeposit}.
   * @return the {@link OreDeposit} with the specifed index.
   */
  public OreDeposit getOreDeposit(int index)
  {
    if (index < 1)
    {
      index = getOreDepositCount();
    }
    else if (index > getOreDepositCount())
    {
      index = 1;
    }

    // Currently, indexing is a linear operation. We can at least improve the
    // performance by iterating from the back when the index is past half way.
    if (index > getOreDepositCount() / 2)
    {
      int currentIndex = getOreDepositCount();
      Iterator<OreDeposit> it = _oreDeposits.descendingIterator();
      while (it.hasNext())
      {
        OreDeposit deposit = it.next();
        if (currentIndex == index)
        {
          return deposit;
        }
        --currentIndex;
      }
      // The only way we can really get here is if the collection is empty.
      throw new IllegalArgumentException(
        "index > TypedOreDB.getOreDepositCount()");
    }
    else
    {
      int currentIndex = 1;
      Iterator<OreDeposit> it = _oreDeposits.iterator();
      while (it.hasNext())
      {
        OreDeposit deposit = it.next();
        if (currentIndex == index)
        {
          return deposit;
        }
        ++currentIndex;
      }
      // This should NEVER happen.
      throw new IllegalStateException("shouldn't happen");
    }
  } // getOreDeposit

  // --------------------------------------------------------------------------
  /**
   * Examine the edit to see if it is an ore, and if it is, add it to the
   * database.
   * 
   * @param edit the edit to examine.
   */
  public void addBlockEdit(BlockEdit edit)
  {
    // Look up the OreBlock at the edit location.
    IntCoord coord = new IntCoord(edit.x, edit.y, edit.z);
    OreBlock block = getOreBlock(coord);
    if (block == null)
    {
      // Create a new OreBlock at the coordinate.
      block = new OreBlock(coord, edit);
      _oreBlocks.put(coord, block);

      // Find all adjacent OreDeposits.
      TreeSet<OreDeposit> deposits = getAdjacentDeposits(coord);
      if (deposits.size() == 0)
      {
        // Create a new deposit containing the block.
        OreDeposit deposit = new OreDeposit();
        deposit.addOreBlock(block);
        _oreDeposits.add(deposit);
      }
      else if (deposits.size() == 1)
      {
        // Remove and re-add the OreDeposit to reindex it according to the newly
        // added block.
        OreDeposit deposit = deposits.first();
        _oreDeposits.remove(deposit);
        deposit.addOreBlock(block);
        _oreDeposits.add(deposit);
      }
      else
      {
        // There are MULTIPLE adjacent ore deposits. Combine them. Remove each
        // from _oreDeposits so that the merged deposit is indexed correctly.
        ArrayList<OreBlock> blocks = new ArrayList<OreBlock>();
        for (OreDeposit deposit : deposits)
        {
          _oreDeposits.remove(deposit);
          blocks.addAll(deposit.getOreBlocks());
        }

        OreDeposit merged = new OreDeposit();
        merged.addOreBlock(block);
        for (OreBlock b : blocks)
        {
          merged.addOreBlock(b);
        }
        _oreDeposits.add(merged);
      }
    }
  } // addBlockEdit

  // --------------------------------------------------------------------------
  /**
   * Return the {@link OreBlock} at the specified location, or null if there is
   * none.
   * 
   * @param location the location.
   * @return the {@link OreBlock} at the specified location, or null if there is
   *         none.
   */
  protected OreBlock getOreBlock(IntCoord location)
  {
    return _oreBlocks.get(location);
  }

  // --------------------------------------------------------------------------
  /**
   * Return the set of all OreDeposits that are "adjacent" to the specified
   * location, ordered from oldest to newest.
   * 
   * @return the set of all OreDeposits that are "adjacent" to the specified
   *         location, ordered from oldest to newest.
   */
  protected TreeSet<OreDeposit> getAdjacentDeposits(IntCoord location)
  {
    TreeSet<OreDeposit> deposits = new TreeSet<OreDeposit>();
    IntCoord adjacent = new IntCoord();
    for (int dx = -1; dx <= 1; ++dx)
    {
      for (int dy = -1; dy <= 1; ++dy)
      {
        for (int dz = -1; dz <= 1; ++dz)
        {
          if (dx == 0 && dy == 0 && dz == 0)
          {
            continue;
          }
          else
          {
            adjacent.setX(location.getX() + dx);
            adjacent.setY(location.getY() + dy);
            adjacent.setZ(location.getZ() + dz);
            OreBlock neighbour = getOreBlock(adjacent);
            if (neighbour != null)
            {
              deposits.add(neighbour.getDeposit());
            }
          }
        } // z
      } // y
    } // x
    return deposits;
  } // getAdjacentDeposits

  // --------------------------------------------------------------------------
  /**
   * Maps 3-D coordinates of ore destructions to OreBlock instances.
   */
  protected HashMap<IntCoord, OreBlock> _oreBlocks;

  /**
   * The set of all OreDeposits, in ascending order by timestamp (oldest first).
   */
  protected TreeSet<OreDeposit>         _oreDeposits = new TreeSet<OreDeposit>();

} // class TypedOreDB