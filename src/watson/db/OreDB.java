package watson.db;

import java.util.LinkedHashMap;
import java.util.logging.Level;

import watson.BlockEdit;
import watson.BlockEditSet;
import watson.BlockType;
import watson.BlockTypeRegistry;
import watson.Controller;
import watson.TimeStamp;
import watson.chat.Colour;
import watson.debug.Log;

// ----------------------------------------------------------------------------
/**
 * A simple spatial database, mapping 3-D coordinates ({@link IntCoord}) to the
 * destruction of the original ore at that location and the corresponding
 * {@link OreDeposit} that was there.
 * 
 * This class currently only stores the coordinates of ores (block IDs 14, 15,
 * 16, 21, 56, 73, 74, 129). The complete editing history retrieved by LogBlock
 * is instead stored in {@link BlockEditSet}.
 */
public class OreDB
{
  /**
   * Constructor.
   */
  public OreDB()
  {
    BlockTypeRegistry types = BlockTypeRegistry.instance;

    // Add the TypedOreDB instances in the order that we would like to list
    // them to the user, i.e. ddiamonds, then emeralds, then iron...
    _db.put(types.getBlockTypeById(56), new TypedOreDB(200));
    _db.put(types.getBlockTypeById(129), new TypedOreDB(200));
    _db.put(types.getBlockTypeById(15), new TypedOreDB(400));
    _db.put(types.getBlockTypeById(14), new TypedOreDB(200));
    _db.put(types.getBlockTypeById(21), new TypedOreDB(200));
    // Merge redstone ore (73) and glowing redstone ore (74)
    _db.put(types.getBlockTypeById(74), new TypedOreDB(200));
    _db.put(types.getBlockTypeById(16), new TypedOreDB(800));

    _chatColours.put(types.getBlockTypeById(56), Colour.lightblue);
    _chatColours.put(types.getBlockTypeById(129), Colour.lightgreen);
    _chatColours.put(types.getBlockTypeById(15), Colour.orange);
    _chatColours.put(types.getBlockTypeById(14), Colour.yellow);
    _chatColours.put(types.getBlockTypeById(21), Colour.blue);
    _chatColours.put(types.getBlockTypeById(74), Colour.red);
    _chatColours.put(types.getBlockTypeById(16), Colour.grey);
  } // OreDB

  // --------------------------------------------------------------------------
  /**
   * Clear all ore information from the database.
   */
  public void clear()
  {
    for (TypedOreDB db : _db.values())
    {
      db.clear();
    }

    // The first call to tpNex() will increment this to 1.
    _tpIndex = 0;
  } // clear

  // --------------------------------------------------------------------------
  /**
   * List all of the ore deposits in the database in chat.
   * 
   * TODO: add support for paging?
   */
  public void listDeposits()
  {
    int depositCount = getOreDepositCount();
    if (depositCount == 1)
    {
      Controller.instance.localOutput("There is 1 ore deposit.");
    }
    else
    {
      Controller.instance.localOutput(String.format(
        "There are %d ore deposits.", depositCount));
    }
    for (int i = 1; i <= depositCount; ++i)
    {
      OreDeposit deposit = getOreDeposit(i);
      long time = deposit.getTimeStamp();
      OreBlock block = deposit.getKeyOreBlock();
      BlockType type = block.getEdit().type;
      String player = block.getEdit().player;
      String line = String.format(
        "\247%c(%3d) %s (% 5d % 3d % 5d) %2d [%2d] %s",
        _chatColours.get(type).getCode(), i,
        TimeStamp.formatMonthDayTime(time), block.getLocation().getX(),
        block.getLocation().getY(), block.getLocation().getZ(), type.getId(),
        deposit.getBlockCount(), player);
      Controller.instance.localChat(line);
    }
  } // listDeposits

  // --------------------------------------------------------------------------
  /**
   * Return the number of {@link OreDeposit}s in the database.
   * 
   * @return the number of {@link OreDeposit}s in the database.
   */
  public int getOreDepositCount()
  {
    int count = 0;
    for (TypedOreDB db : _db.values())
    {
      count += db.getOreDepositCount();
    }
    return count;
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
   * @return the {@link OreDeposit} with the specified index.
   */
  public OreDeposit getOreDeposit(int index)
  {
    index = limitOreDepositIndex(index);
    for (TypedOreDB db : _db.values())
    {
      if (index <= db.getOreDepositCount())
      {
        return db.getOreDeposit(index);
      }
      else
      {
        index -= db.getOreDepositCount();
      }
    } // for
    throw new IllegalStateException("shouldn't happen");
  } // getOreDeposit

  // --------------------------------------------------------------------------
  /**
   * Teleport to the next ore deposit.
   */
  public void tpNext()
  {
    tpIndex(_tpIndex + 1);
  }

  // --------------------------------------------------------------------------
  /**
   * Teleport to the previous ore deposit.
   */
  public void tpPrev()
  {
    tpIndex(_tpIndex - 1);
  }

  // --------------------------------------------------------------------------
  /**
   * Teleport to the ore deposit with the specified 1-based index.
   * 
   * @return the index teleported to.
   */
  public void tpIndex(int index)
  {
    if (getOreDepositCount() == 0)
    {
      Controller.instance.localError("There are no ore deposits to teleport to.");
    }
    else
    {
      _tpIndex = index = limitOreDepositIndex(index);
      OreDeposit deposit = getOreDeposit(index);
      IntCoord coord = deposit.getKeyOreBlock().getLocation();

      // Add 0.5 to the x and z coordinates to put us dead centre.
      String command = String.format("/tppos %g %d %g", coord.getX() + 0.5,
        coord.getY(), coord.getZ() + 0.5);
      Controller.instance.serverChat(command);
      Controller.instance.localOutput(String.format(
        "Teleporting you to ore #%d", index));

      // "Select" the tp target so that /w pre will work.
      Controller.instance.selectBlockEdit(deposit.getKeyOreBlock().getEdit());

    }
  } // tpIndex

  // --------------------------------------------------------------------------
  /**
   * Examine the edit to see if it is an ore, and if it is, add it to the
   * database.
   * 
   * @param edit the edit to examine.
   */
  public void addBlockEdit(BlockEdit edit)
  {
    try
    {
      // If the edit corresponds to destruction of an ore, then we are
      // interested.
      // Otherwise not.
      if (!edit.creation && isOre(edit.type))
      {
        TypedOreDB db = getDB(edit.type);
        db.addBlockEdit(edit);
      }
    }
    catch (Exception ex)
    {
      Log.exception(Level.SEVERE, "error in OreDB.addBlockEdit()", ex);
    }
  } // addBlockEdit

  // --------------------------------------------------------------------------
  /**
   * Return the {@link TypedOreDB} instance applicable to the specified ore
   * type.
   * 
   * @param type the {@link BlockType} of the ore.
   * @return the {@link TypedOreDB} that stores information about
   *         {@link OreDeposits} of that type.
   */
  protected TypedOreDB getDB(BlockType type)
  {
    return _db.get(type);
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if the specified {@link BlockType} is an ore.
   * 
   * @param type the block type.
   * @return true if the specified {@link BlockType} is an ore.
   */
  protected boolean isOre(BlockType type)
  {
    return _db.containsKey(type);
  }

  // --------------------------------------------------------------------------
  /**
   * Limit the specified {@link OreDeposit} index to the valid range.
   * 
   * @param index the 1-based index.
   * @return an index in the valid range [1,getOreDepositCount()].
   */
  protected int limitOreDepositIndex(int index)
  {
    if (index < 1)
    {
      return getOreDepositCount();
    }
    else if (index > getOreDepositCount())
    {
      return 1;
    }
    else
    {
      return index;
    }
  } // limitOreDepositIndex

  // --------------------------------------------------------------------------
  /**
   * If the specified BlockType is redstone ore, return (merge it with) glowing
   * redstone ore.
   * 
   * @param type the type of ore.
   * @return redstone ore for both redstone ore types, or the original type
   *         parameter if not redstone.
   */
  static BlockType getMergedBlockType(BlockType type)
  {
    if (type.getId() == 73)
    {
      return BlockTypeRegistry.instance.getBlockTypeById(74);
    }
    else
    {
      return type;
    }
  } // getMergedBlockType

  // --------------------------------------------------------------------------
  /**
   * Map from {@link BlockType} to {@link TypedOreDB}, linked in the order that
   * we would like to list ore deposits to the user, i.e. diamonds first.
   */
  protected LinkedHashMap<BlockType, TypedOreDB> _db          = new LinkedHashMap<BlockType, TypedOreDB>();

  /**
   * A map from the {@link BlockType} to the {@link Colour} to use when listing
   * ores of that type in chat.
   */
  protected LinkedHashMap<BlockType, Colour>     _chatColours = new LinkedHashMap<BlockType, Colour>();

  /**
   * The index of the most recently teleported to {@link OreDeposit}.
   */
  protected int                                  _tpIndex     = 0;
} // class OreDB
