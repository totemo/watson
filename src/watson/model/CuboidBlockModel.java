package watson.model;

import watson.BlockType;

// --------------------------------------------------------------------------
/**
 * Render a cuboid wireframe model of a block. Most blocks can be be drawn as
 * some variation on a cuboid. For example, a pressure plate is just a very low,
 * flat cuboid.
 */
public class CuboidBlockModel extends BlockModel
{
  // --------------------------------------------------------------------------
  /**
   * Default constructor.
   */
  public CuboidBlockModel()
  {
    super("cuboid");
  }

  // --------------------------------------------------------------------------
  /**
   * Render a cuboid at the specified world coordinates, (x,y,z), using the
   * colour, line width and bounds associated with the BlockType.
   * 
   * @param blockType a description of the type of block, which includes line
   *          colour and thickness and cuboid bounds.
   * @param x world X coordinate.
   * @param y world Y coordinate.
   * @param z world Z coordinate.
   */
  @Override
  public void render(BlockType blockType, int x, int y, int z)
  {
    renderBox(x + blockType.getX1(), y + blockType.getY1(),
      z + blockType.getZ1(), x + blockType.getX2(), y + blockType.getY2(),
      z + blockType.getZ2(), blockType.getARGB(), blockType.getLineWidth());
  } 
} // class CuboidBlockModel