package watson.model;

import net.minecraft.client.renderer.Tessellator;

import net.minecraft.client.renderer.WorldRenderer;
import org.lwjgl.opengl.GL11;

import watson.db.BlockType;

// --------------------------------------------------------------------------
/**
 * Render a wireframe model of a stair.
 */
public class StairBlockModel extends BlockModel
{
  // --------------------------------------------------------------------------
  /**
   * Default constructor.
   */
  public StairBlockModel()
  {
    super("stair");
  }

  // --------------------------------------------------------------------------
  /**
   * @see watson.model.BlockModel#render(watson.db.BlockType, int, int, int)
   */
  @Override
  public void render(BlockType blockType, int x, int y, int z)
  {
    Tessellator tess = Tessellator.getInstance();
      WorldRenderer wr = tess.getWorldRenderer();

    // Opposite corners.
    double x1 = x + blockType.getX1();
    double y1 = y + blockType.getY1();
    double z1 = z + blockType.getZ1();
    double x2 = x + blockType.getX2();
    double y2 = y + blockType.getY2();
    double z2 = z + blockType.getZ2();

    // Concave corner, mid-stair.
    double yMid = y + 0.5 * (blockType.getY1() + blockType.getY2());
    double zMid = z + 0.5 * (blockType.getZ1() + blockType.getZ2());

    // x1 side.
      wr.startDrawing(GL11.GL_LINE_LOOP);
      wr.setColorRGBA(blockType.getARGB().getRed(),
      blockType.getARGB().getGreen(), blockType.getARGB().getBlue(),
      blockType.getARGB().getAlpha());
    GL11.glLineWidth(blockType.getLineWidth());
      wr.addVertex(x1, y1, z1);
      wr.addVertex(x1, y1, z2);
      wr.addVertex(x1, y2, z2);
      wr.addVertex(x1, y2, zMid);
      wr.addVertex(x1, yMid, zMid);
      wr.addVertex(x1, yMid, z1);
    tess.draw();

    // x2 side.
      wr.startDrawing(GL11.GL_LINE_LOOP);
      wr.setColorRGBA(blockType.getARGB().getRed(),
      blockType.getARGB().getGreen(), blockType.getARGB().getBlue(),
      blockType.getARGB().getAlpha());
    GL11.glLineWidth(blockType.getLineWidth());
      wr.addVertex(x2, y1, z1);
      wr.addVertex(x2, y1, z2);
      wr.addVertex(x2, y2, z2);
      wr.addVertex(x2, y2, zMid);
      wr.addVertex(x2, yMid, zMid);
      wr.addVertex(x2, yMid, z1);
    tess.draw();

    // Horizontal lines joining the two sides.
      wr.startDrawing(GL11.GL_LINES);
      wr.setColorRGBA(blockType.getARGB().getRed(),
      blockType.getARGB().getGreen(), blockType.getARGB().getBlue(),
      blockType.getARGB().getAlpha());
    GL11.glLineWidth(blockType.getLineWidth());

      wr.addVertex(x1, y1, z1);
      wr.addVertex(x2, y1, z1);

      wr.addVertex(x1, y1, z2);
      wr.addVertex(x2, y1, z2);

      wr.addVertex(x1, y2, z2);
      wr.addVertex(x2, y2, z2);

      wr.addVertex(x1, y2, zMid);
      wr.addVertex(x2, y2, zMid);

      wr.addVertex(x1, yMid, zMid);
      wr.addVertex(x2, yMid, zMid);

      wr.addVertex(x1, yMid, z1);
      wr.addVertex(x2, yMid, z1);
    tess.draw();
  } // render
} // class StairBlockModel