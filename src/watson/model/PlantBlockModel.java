package watson.model;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;

import org.lwjgl.opengl.GL11;

import watson.db.BlockType;

// ----------------------------------------------------------------------------
/**
 * A {@link BlockModel implementation that draws a block as two perpendicular
 * intersecting rectangles at 45 degrees to the X and Z coordinate axes.
 * 
 * This is, in essence, the wireframe incarnation of the billboard onto which
 * Minecraft vegetation textures are rendered.
 */
public class PlantBlockModel extends BlockModel
{
  // --------------------------------------------------------------------------
  /**
   * Default constructor.
   */
  public PlantBlockModel()
  {
    super("plant");
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

    double x1 = x + blockType.getX1();
    double y1 = y + blockType.getY1();
    double z1 = z + blockType.getZ1();
    double x2 = x + blockType.getX2();
    double y2 = y + blockType.getY2();
    double z2 = z + blockType.getZ2();

    // First rectangle.
    wr.startDrawing(GL11.GL_LINE_LOOP);
    wr.setColorRGBA(blockType.getARGB().getRed(),
      blockType.getARGB().getGreen(), blockType.getARGB().getBlue(),
      blockType.getARGB().getAlpha());
    GL11.glLineWidth(blockType.getLineWidth());
    wr.addVertex(x1, y1, z1);
    wr.addVertex(x2, y1, z2);
    wr.addVertex(x2, y2, z2);
    wr.addVertex(x1, y2, z1);
    tess.draw();

    // Second rectangle.
    wr.startDrawing(GL11.GL_LINE_LOOP);
    wr.setColorRGBA(blockType.getARGB().getRed(),
      blockType.getARGB().getGreen(), blockType.getARGB().getBlue(),
      blockType.getARGB().getAlpha());
    GL11.glLineWidth(blockType.getLineWidth());
    wr.addVertex(x1, y1, z2);
    wr.addVertex(x2, y1, z1);
    wr.addVertex(x2, y2, z1);
    wr.addVertex(x1, y2, z2);
    tess.draw();
  } // render
} // class PlantBlockModel
