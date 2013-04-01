package watson.db;

// ----------------------------------------------------------------------------
/**
 * Represents the information about one single edit action to a particular
 * block, e.g. creation or destruction.
 */
public class BlockEdit
{
  /**
   * Time stamp of the edit as returned by LogBlock.
   */
  public long          time;

  /**
   * The name of the player who performed the edit.
   */
  public String        player;

  /**
   * True if the edit was creation of a block, false if destruction.
   */
  public boolean       creation;

  /**
   * X coordinate of the block.
   */
  public int           x;

  /**
   * Y coordinate of the block.
   */
  public int           y;

  /**
   * Z coordinate of the block.
   */
  public int           z;

  /**
   * A reference to the BlockType denoting the type of the block that was the
   * subject of this edit (whether constructed or destroyed).
   */
  public BlockType     type;

  /**
   * The {@link PlayerEditSet} that contains these edits.
   */
  public PlayerEditSet playerEditSet;

  // --------------------------------------------------------------------------
  /**
   * Constructor.
   */
  public BlockEdit(long time, String player, boolean creation, int x, int y,
                   int z, BlockType type)
  {
    this.time = time;
    this.player = player;
    this.creation = creation;
    this.x = x;
    this.y = y;
    this.z = z;
    this.type = type;
  }

  // --------------------------------------------------------------------------
  /**
   * Draw a wireframe outline of this block.
   */
  public void drawOutline()
  {
    type.getBlockModel().render(type, x, y, z);
  }

} // class BlockEdit