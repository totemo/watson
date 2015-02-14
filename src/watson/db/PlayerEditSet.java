package watson.db;

import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Iterator;
import java.util.TreeSet;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.Vec3;

import org.lwjgl.opengl.GL11;

import watson.Controller;
import watson.DisplaySettings;
import watson.model.ARGB;

// ----------------------------------------------------------------------------
/**
 * Maintains a time-ordered list of all of the BlockEdit instances corresponding
 * to LogBlock results for one player only, ordered from oldest to most recent.
 */
public class PlayerEditSet
{
  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * @param player name of the player who did these edits.
   */
  public PlayerEditSet(String player)
  {
    _player = player;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the name of the player who did these edits.
   * 
   * @return the name of the player who did these edits.
   */
  public String getPlayer()
  {
    return _player;
  }

  // --------------------------------------------------------------------------
  /**
   * Find an edit with the specified coordinates.
   * 
   * Currently, this method does an inefficient linear search, walking, on
   * average, half the collection. The edits are searched from oldest to newest,
   * meaning that the oldest edit at that coordinate will be retrieved.
   * 
   * TODO: implement an efficient spatial search.
   * 
   * @param x the x coordinate of the block
   * @param y the y coordinate of the block
   * @param z the z coordinate of the block
   * @return the matching edit, or null if not found.
   */
  public BlockEdit findEdit(int x, int y, int z)
  {
    // Warning: O(N) algorithm. Avert your delicate eyes.
    for (BlockEdit edit : _edits)
    {
      if (edit.x == x && edit.y == y && edit.z == z)
      {
        return edit;
      }
    }
    return null;
  }

  // --------------------------------------------------------------------------
  /**
   * Add the specified edit to the list.
   * 
   * @param edit the BlockEdit describing an edit to add.
   */
  public void addBlockEdit(BlockEdit edit)
  {
    _edits.add(edit);

    // Reference container for fast visibility toggling of ore deposit labels.
    edit.playerEditSet = this;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the number of edits stored.
   * 
   * @return the number of edits stored.
   */
  int getBlockEditCount()
  {
    return _edits.size();
  }

  // --------------------------------------------------------------------------
  /**
   * Set the visibility of this player's edits in the dimension to which this
   * PlayerEditSet applies.
   * 
   * @param visible if true, edits are visible.
   */
  public void setVisible(boolean visible)
  {
    _visible = visible;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the visibility of this player's edits in the dimension to which this
   * PlayerEditSet applies.
   * 
   * @return the visibility of this player's edits in the dimension to which
   *         this PlayerEditSet applies.
   */
  public boolean isVisible()
  {
    return _visible;
  }

  // --------------------------------------------------------------------------
  /**
   * Draw wireframe outlines of all blocks.
   */
  public void drawOutlines()
  {
    if (isVisible())
    {
      if (Controller.instance.getDisplaySettings().isOutlineShown())
      {
        for (BlockEdit edit : _edits)
        {
          edit.drawOutline();
        }
      }
    }
  }

  // --------------------------------------------------------------------------
  /**
   * Draw direction vectors indicating motion of the miner.
   * 
   * @param colour the colour to draw the vectors.
   */
  public void drawVectors(ARGB colour)
  {
    DisplaySettings settings = Controller.instance.getDisplaySettings();
    if (settings.areVectorsShown() && isVisible() && !_edits.isEmpty())
    {
      final Tessellator tess = Tessellator.getInstance();
      final WorldRenderer wr = tess.getWorldRenderer();
      wr.startDrawing(GL11.GL_LINES);

      // TODO: Make the vector colour and thickness configurable.
      wr.setColorRGBA_I(colour.getRGB(), colour.getAlpha());
      GL11.glLineWidth(0.5f);

      // Unit X and Y vectors used for cross products to get arrow axes.
      Vec3 unitX = new Vec3(1, 0, 0);
      Vec3 unitY = new Vec3(0, 1, 0);

      // We only need to draw vectors if there are at least 2 edits.
      Iterator<BlockEdit> it = _edits.iterator();
      if (it.hasNext())
      {
        BlockEdit prev = it.next();
        while (it.hasNext())
        {
          BlockEdit next = it.next();

          // Work out whether to link edits with vectors.
          boolean show = (next.creation && settings.isLinkedCreations()) ||
                         (!next.creation && settings.isLinkedDestructions());
          if (show)
          {
            Vec3 pPos = new Vec3(0.5 + prev.x, 0.5 + prev.y, 0.5 + prev.z);
            Vec3 nPos = new Vec3(0.5 + next.x, 0.5 + next.y, 0.5 + next.z);
            // Vector difference, from prev to next.
            Vec3 diff = nPos.subtract(pPos);

            // Compute length. We want to scale the arrow heads by the length,
            // so can't avoid the sqrt() here.
            double length = diff.lengthVector();
            if (length >= settings.getMinVectorLength())
            {
              // Draw the vector.
              wr.addVertex(pPos.xCoord, pPos.yCoord, pPos.zCoord);
              wr.addVertex(nPos.xCoord, nPos.yCoord, nPos.zCoord);

              // Length from arrow tip to midpoint of vector as a fraction of
              // the total vector length. Scale the arrow in proportion to the
              // square root of the length up to a maximum size.
              double arrowSize = UNIT_VECTOR_ARROW_SIZE * Math.sqrt(length);
              if (arrowSize > MAX_ARROW_SIZE)
              {
                arrowSize = MAX_ARROW_SIZE;
              }
              double arrowScale = arrowSize / length;

              // Position of the tip and tail of the arrow, sitting in the
              // middle of the vector.
              Vec3 tip = new Vec3(
                pPos.xCoord * (0.5 - arrowScale) + nPos.xCoord
                  * (0.5 + arrowScale), pPos.yCoord * (0.5 - arrowScale)
                                        + nPos.yCoord * (0.5 + arrowScale),
                pPos.zCoord * (0.5 - arrowScale) + nPos.zCoord
                  * (0.5 + arrowScale));
              Vec3 tail = new Vec3(
                pPos.xCoord * (0.5 + arrowScale) + nPos.xCoord * (0.5 - arrowScale),
                pPos.yCoord * (0.5 + arrowScale) + nPos.yCoord * (0.5 - arrowScale),
                pPos.zCoord * (0.5 + arrowScale) + nPos.zCoord * (0.5 - arrowScale));

              // Fin axes, perpendicular to vector. Scale by vector length.
              // If the vector is colinear with the Y axis, use the X axis for
              // the cross products to derive the fin directions.
              Vec3 fin1;
              if (Math.abs(unitY.dotProduct(diff)) > 0.9 * length)
              {
                fin1 = unitX.crossProduct(diff).normalize();
              }
              else
              {
                fin1 = unitY.crossProduct(diff).normalize();
              }

              Vec3 fin2 = fin1.crossProduct(diff).normalize();

              Vec3 draw1 = new Vec3(fin1.xCoord * arrowScale * length, fin1.yCoord * arrowScale * length, fin1.zCoord
                                                                                                          * arrowScale * length);
              Vec3 draw2 = new Vec3(fin2.xCoord * arrowScale * length, fin2.yCoord * arrowScale * length, fin2.zCoord
                                                                                                          * arrowScale * length);

              // Draw four fins.
              wr.addVertex(tip.xCoord, tip.yCoord, tip.zCoord);
              wr.addVertex(tail.xCoord + draw1.xCoord, tail.yCoord + draw1.yCoord, tail.zCoord + draw1.zCoord);
              wr.addVertex(tip.xCoord, tip.yCoord, tip.zCoord);
              wr.addVertex(tail.xCoord - draw1.xCoord, tail.yCoord - draw1.yCoord, tail.zCoord - draw1.zCoord);
              wr.addVertex(tip.xCoord, tip.yCoord, tip.zCoord);
              wr.addVertex(tail.xCoord + draw2.xCoord, tail.yCoord + draw2.yCoord, tail.zCoord + draw2.zCoord);
              wr.addVertex(tip.xCoord, tip.yCoord, tip.zCoord);
              wr.addVertex(tail.xCoord - draw2.xCoord, tail.yCoord - draw2.yCoord, tail.zCoord - draw2.zCoord);
            } // if we are drawing this vector
            prev = next;
          } // if
        } // while
        tess.draw();
      } // if
    } // if drawing
  } // drawVectors

  // --------------------------------------------------------------------------
  /**
   * Write the edits for this player to the specified PrintWriter.
   * 
   * @param writer the PrintWriter.
   * @return the number of edits saved.
   */
  public int save(PrintWriter writer)
  {
    Calendar calendar = Calendar.getInstance();
    int editCount = 0;
    for (BlockEdit edit : _edits)
    {
      calendar.setTimeInMillis(edit.time);
      int year = calendar.get(Calendar.YEAR);
      int month = calendar.get(Calendar.MONTH) + 1;
      int day = calendar.get(Calendar.DAY_OF_MONTH);
      int hour = calendar.get(Calendar.HOUR_OF_DAY);
      int minute = calendar.get(Calendar.MINUTE);
      int second = calendar.get(Calendar.SECOND);
      char action = edit.creation ? 'c' : 'd';
      writer.format("%4d-%02d-%02d|%02d:%02d:%02d|%s|%c|%d|%d|%d|%d|%d\n",
        year, month, day, hour, minute, second, edit.player, action,
        edit.type.getId(), edit.type.getData(), edit.x, edit.y, edit.z);
      ++editCount;
    } // for
    return editCount;
  } // save

  // --------------------------------------------------------------------------
  /**
   * The name of the player who did these edits.
   */
  protected String              _player;

  /**
   * A set of BlockEdit instances, ordered from oldest (lowest time value) to
   * most recent.
   */
  protected TreeSet<BlockEdit>  _edits                 = new TreeSet<BlockEdit>(
                                                         new BlockEditComparator());

  /**
   * True if this player's edits are visible.
   */
  protected boolean             _visible               = true;

  /**
   * Size of the arrow on a unit length vector.
   */
  protected static final double UNIT_VECTOR_ARROW_SIZE = 0.025;

  /**
   * Maximum size of an arrow in world units.
   */
  protected static final double MAX_ARROW_SIZE         = 0.5;
} // class PlayerEditSet