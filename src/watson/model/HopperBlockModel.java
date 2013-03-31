package watson.model;

import watson.BlockType;

// --------------------------------------------------------------------------
/**
 * Render the stylised wireframe outline of a hopper.
 */
public class HopperBlockModel extends BlockModel
{
  // --------------------------------------------------------------------------
  /**
   * Default constructor.
   */
  public HopperBlockModel()
  {
    super("hopper");
  }

  // --------------------------------------------------------------------------
  /**
   * @see watson.model.BlockModel#render(watson.BlockType, int, int, int)
   */
  @Override
  public void render(BlockType blockType, int x, int y, int z)
  {
    renderTaperedBox(x + 0.375, z + 0.375, x + 0.675, z + 0.675, y, x, z,
      x + 1, z + 1, y + 1, blockType.getARGB(), blockType.getLineWidth());
  } // render
} // class HopperBlockModel