package watson.model;

import net.minecraft.src.Tessellator;

import org.lwjgl.opengl.GL11;

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
    Tessellator tess = Tessellator.instance;

    double x1 = x + blockType.getX1();
    double y1 = y + blockType.getY1();
    double z1 = z + blockType.getZ1();
    double x2 = x + blockType.getX2();
    double y2 = y + blockType.getY2();
    double z2 = z + blockType.getZ2();

    // Bottom face.
    tess.startDrawing(GL11.GL_LINE_LOOP);
    tess.setColorRGBA(blockType.getRed(), blockType.getGreen(),
      blockType.getBlue(), blockType.getAlpha());
    GL11.glLineWidth(blockType.getLineWidth());
    tess.addVertex(x1, y1, z1);
    tess.addVertex(x2, y1, z1);
    tess.addVertex(x2, y1, z2);
    tess.addVertex(x1, y1, z2);
    tess.draw();

    // Top face.
    tess.startDrawing(GL11.GL_LINE_LOOP);
    tess.setColorRGBA(blockType.getRed(), blockType.getGreen(),
      blockType.getBlue(), blockType.getAlpha());
    GL11.glLineWidth(blockType.getLineWidth());
    tess.addVertex(x1, y2, z1);
    tess.addVertex(x2, y2, z1);
    tess.addVertex(x2, y2, z2);
    tess.addVertex(x1, y2, z2);
    tess.draw();

    // Vertical lines joining top and bottom.
    tess.startDrawing(GL11.GL_LINES);
    tess.setColorRGBA(blockType.getRed(), blockType.getGreen(),
      blockType.getBlue(), blockType.getAlpha());
    GL11.glLineWidth(blockType.getLineWidth());

    tess.addVertex(x1, y1, z1);
    tess.addVertex(x1, y2, z1);

    tess.addVertex(x2, y1, z1);
    tess.addVertex(x2, y2, z1);

    tess.addVertex(x2, y1, z2);
    tess.addVertex(x2, y2, z2);

    tess.addVertex(x1, y1, z2);
    tess.addVertex(x1, y2, z2);
    tess.draw();
  } // render
} // class CuboidBlockModel