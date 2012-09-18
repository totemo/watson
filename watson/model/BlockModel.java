package watson.model;

import watson.BlockType;

// ----------------------------------------------------------------------------
/**
 * Abstract base of classes that draw the 3-D model of a block.
 */
public abstract class BlockModel
{
  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * @param name the name of this instance, referenced in "blocks.yml".
   */
  public BlockModel(String name)
  {
    _name = name;
  }

  // --------------------------------------------------------------------------
  /**
   * Draw the model.
   * 
   * @param blockType a description of the type of block, which includes line
   *          colour and thickness and cuboid bounds.
   * @param x world X coordinate.
   * @param y world Y coordinate.
   * @param z world Z coordinate.
   */
  public abstract void render(BlockType blockType, int x, int y, int z);

  // --------------------------------------------------------------------------
  /**
   * Return the name of this BlockModel instance.
   * 
   * @return the name of this BlockModel instance.
   */
  public String getName()
  {
    return _name;
  }

  // --------------------------------------------------------------------------
  /**
   * The friendly name of this BlockModel instance, as referenced by
   * "blocks.yml".
   */
  private String _name;
} // abstract class BlockModel
