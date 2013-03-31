package watson.model;

import watson.BlockType;

// --------------------------------------------------------------------------
/**
 * Render a stylised wireframe anvil.
 */
public class AnvilBlockModel extends BlockModel
{
  // --------------------------------------------------------------------------
  /**
   * Default constructor.
   */
  public AnvilBlockModel()
  {
    super("anvil");
  }

  // --------------------------------------------------------------------------
  /**
   * @see watson.model.BlockModel#render(watson.BlockType, int, int, int)
   */
  @Override
  public void render(BlockType blockType, int x, int y, int z)
  {
    // Top, middle and bottom sections of the anvil, respectively:
    renderBox(x, y + 0.625, z + 0.1875, x + 1, y + 1, z + 0.8125,
      blockType.getARGB(), blockType.getLineWidth());
    renderBox(x + 0.25, y + 0.25, z + 0.375, x + 0.75, y + 0.625, z + 0.625,
      blockType.getARGB(), blockType.getLineWidth());
    renderBox(x + 0.125, y, z + 0.125, x + 0.875, y + 0.25, z + 0.875,
      blockType.getARGB(), blockType.getLineWidth());
  }
} // class AnvilBlockModel