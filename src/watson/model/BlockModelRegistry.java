package watson.model;

import java.util.HashMap;
import java.util.Map;

// --------------------------------------------------------------------------
/**
 * Allows BlockModel implementations to be looked up by name.
 */
public final class BlockModelRegistry
{
  /**
   * The single instance of this class.
   */
  public static final BlockModelRegistry instance = new BlockModelRegistry();

  // --------------------------------------------------------------------------
  /**
   * Return the BlockModel associated with the specified name, or null if not
   * found.
   * 
   * @return the BlockModel associated with the specified name, or null if not
   *         found.
   */
  public BlockModel getBlockModel(String name)
  {
    return _models.get(name);
  }

  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * Private to enforce Singleton pattern.
   */
  private BlockModelRegistry()
  {
    _models.put("cuboid", new CuboidBlockModel());
    _models.put("billboard", new PlantBlockModel());
    _models.put("stair", new StairBlockModel());
    _models.put("hopper", new HopperBlockModel());
    _models.put("anvil", new AnvilBlockModel());
  }

  // --------------------------------------------------------------------------
  /**
   * Maps BlockModel name to BlockModel implementation.
   * 
   * BlockModel instances are referenced by name in "blocks.yml". If no model
   */
  private Map<String, BlockModel> _models = new HashMap<String, BlockModel>();
} // class BlockModelRegistry