package watson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraft.src.ModLoader;
import net.minecraft.src.Tessellator;
import net.minecraft.src.Vec3;
import net.minecraft.src.Vec3Pool;

import org.lwjgl.opengl.GL11;

// ----------------------------------------------------------------------------
/**
 * Maintains a time-ordered list of all of the BlockEdit instances corresponding
 * to LogBlock results, ordered from oldest to most recent.
 */
public class BlockEditSet
{
  // --------------------------------------------------------------------------
  /**
   * Default constructor.
   */
  public BlockEditSet()
  {
    // Nothing.
  }

  // --------------------------------------------------------------------------
  /**
   * Load additional entries from the specified file.
   * 
   * @param file the file to load.
   * @return the number of edits loaded.
   */
  public int load(File file)
    throws Exception
  {
    BufferedReader reader = new BufferedReader(new FileReader(file));

    try
    {
      Pattern editPattern = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})\\|(\\d{2}):(\\d{2}):(\\d{2})\\|(\\w+)\\|([cd])\\|(\\d+)\\|(\\d+)\\|(-?\\d+)\\|(\\d+)\\|(-?\\d+)");
      Pattern annoPattern = Pattern.compile("#(-?\\d+)\\|(\\d+)\\|(-?\\d+)\\|(.*)");
      Calendar time = Calendar.getInstance();
      String line;
      int edits = 0;
      while ((line = reader.readLine()) != null)
      {
        Matcher edit = editPattern.matcher(line);
        if (edit.matches())
        {
          int year = Integer.parseInt(edit.group(1));
          int month = Integer.parseInt(edit.group(2)) - 1;
          int day = Integer.parseInt(edit.group(3));
          int hour = Integer.parseInt(edit.group(4));
          int minute = Integer.parseInt(edit.group(5));
          int second = Integer.parseInt(edit.group(6));
          time.set(year, month, day, hour, minute, second);

          String player = edit.group(7);
          boolean created = edit.group(8).equals("c");
          int id = Integer.parseInt(edit.group(9));
          int data = Integer.parseInt(edit.group(10));
          int x = Integer.parseInt(edit.group(11));
          int y = Integer.parseInt(edit.group(12));
          int z = Integer.parseInt(edit.group(13));

          BlockType type = BlockTypeRegistry.instance.getBlockTypeByIdData(id,
            data);
          BlockEdit blockEdit = new BlockEdit(time.getTimeInMillis(), player,
            created, x, y, z, type);
          addBlockEdit(blockEdit);
          ++edits;
        } // if
        else
        {
          // Is the line an annotation?
          Matcher anno = annoPattern.matcher(line);
          if (anno.matches())
          {
            int x = Integer.parseInt(anno.group(1));
            int y = Integer.parseInt(anno.group(2));
            int z = Integer.parseInt(anno.group(3));
            String text = anno.group(4);
            _annotations.add(new Annotation(x, y, z, text));
          }
        }
      } // while
      return edits;
    }
    finally
    {
      reader.close();
    }
  } // load

  // --------------------------------------------------------------------------
  /**
   * Save all {@link BlockEdit}s to the specified file.
   * 
   * Each line is of the form:
   * 
   * <pre>
   * YYYY-MM-DD|hh:mm:ss|action|id|data|x|y|z
   * </pre>
   * 
   * Where action is c (created) or d (destroyed) and id is the numeric block
   * type.
   * 
   * @param file the file to load.
   * @return the number of edits saved.
   */
  public int save(File file)
    throws IOException
  {
    PrintWriter writer = new PrintWriter(new BufferedWriter(
      new FileWriter(file)));
    try
    {
      // Save edits.
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

      // Save annotations.
      for (Annotation annotation : _annotations)
      {
        writer.format("#%d|%d|%d|%s\n", annotation.getX(), annotation.getY(),
          annotation.getZ(), annotation.getText());
      }
      return editCount;
    }
    finally
    {
      writer.close();
    }
  } // save
  // --------------------------------------------------------------------------
  /**
   * Remove all entries from the list.
   */
  public void clear()
  {
    _edits.clear();
    _annotations.clear();
  }

  // --------------------------------------------------------------------------
  /**
   * Find an edit with the specified coordinates and, optionally, player.
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
   * @param player the player name (can be null for a wildcard).
   * @return the matching edit, or null if not found.
   */
  public BlockEdit findEdit(int x, int y, int z, String player)
  {
    // Warning: O(N) algorithm. Avert your delicate eyes.
    for (BlockEdit edit : _edits)
    {
      if (edit.x == x && edit.y == y && edit.z == z
          && (player == null || edit.player.equals(player)))
      {
        return edit;
      }
    }
    return null;
  } // findEdit

  // --------------------------------------------------------------------------
  /**
   * Add the specified edit to the list.
   * 
   * @param edit the BlockEdit describing an edit to add.
   */
  public void addBlockEdit(BlockEdit edit)
  {
    _edits.add(edit);
  }

  // --------------------------------------------------------------------------
  /**
   * Draw wireframe outlines of all blocks.
   */
  public void drawOutlines()
  {
    if (Controller.instance.getDisplaySettings().isOutlineShown())
    {
      for (BlockEdit edit : _edits)
      {
        edit.drawOutline();
      }
    }
  }

  // --------------------------------------------------------------------------
  /**
   * Draw direction vectors indicating motion of the miner.
   */
  public void drawVectors()
  {
    DisplaySettings settings = Controller.instance.getDisplaySettings();
    if (settings.areVectorsShown() && !_edits.isEmpty())
    {
      final Vec3Pool pool = ModLoader.getMinecraftInstance().theWorld.getWorldVec3Pool();
      final Tessellator tess = Tessellator.instance;
      tess.startDrawing(GL11.GL_LINES);

      // TODO: Make the vector colour and thickness configurable.
      tess.setColorRGBA(192, 192, 192, (int) (0.8 * 255));
      GL11.glLineWidth(0.5f);

      // Unit X and Y vectors used for cross products to get arrow axes.
      Vec3 unitX = pool.getVecFromPool(1, 0, 0);
      Vec3 unitY = pool.getVecFromPool(0, 1, 0);

      // We only need to draw vectors if there are at least 2 edits.
      Iterator<BlockEdit> it = _edits.iterator();
      if (it.hasNext())
      {
        BlockEdit prev = it.next();
        while (it.hasNext())
        {
          BlockEdit next = it.next();

          // Work out whether to link edits with vectors.
          boolean show = (next.creation && settings.isLinkedCreations())
                         || (!next.creation && settings.isLinkedDestructions());
          if (show)
          {
            Vec3 pPos = pool.getVecFromPool(0.5 + prev.x, 0.5 + prev.y,
              0.5 + prev.z);
            Vec3 nPos = pool.getVecFromPool(0.5 + next.x, 0.5 + next.y,
              0.5 + next.z);
            // Vector difference, from prev to next.
            Vec3 diff = nPos.subtract(pPos);

            // Compute length. We want to scale the arrow heads by the length,
            // so can't avoid the sqrt() here.
            double length = diff.lengthVector();
            if (length >= settings.getMinVectorLength())
            {
              // Draw the vector.
              tess.addVertex(pPos.xCoord, pPos.yCoord, pPos.zCoord);
              tess.addVertex(nPos.xCoord, nPos.yCoord, nPos.zCoord);

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
              Vec3 tip = pool.getVecFromPool(
                pPos.xCoord * (0.5 - arrowScale) + nPos.xCoord
                  * (0.5 + arrowScale), pPos.yCoord * (0.5 - arrowScale)
                                        + nPos.yCoord * (0.5 + arrowScale),
                pPos.zCoord * (0.5 - arrowScale) + nPos.zCoord
                  * (0.5 + arrowScale));
              Vec3 tail = pool.getVecFromPool(pPos.xCoord * (0.5 + arrowScale)
                                              + nPos.xCoord
                                              * (0.5 - arrowScale),
                pPos.yCoord * (0.5 + arrowScale) + nPos.yCoord
                  * (0.5 - arrowScale), pPos.zCoord * (0.5 + arrowScale)
                                        + nPos.zCoord * (0.5 - arrowScale));

              // Fin axes, perpendicular to vector. Scale by vector length.
              // If the vector is colinear with the Y axis, use the X axis for
              // the cross products to derive the fin directions.
              Vec3 fin1;
              if (unitY.dotProduct(diff) > 0.9 * length)
              {
                fin1 = unitX.crossProduct(diff).normalize();
              }
              else
              {
                fin1 = unitY.crossProduct(diff).normalize();
              }

              Vec3 fin2 = fin1.crossProduct(diff).normalize();
              fin1.xCoord *= arrowScale * length;
              fin1.yCoord *= arrowScale * length;
              fin1.zCoord *= arrowScale * length;
              fin2.xCoord *= arrowScale * length;
              fin2.yCoord *= arrowScale * length;
              fin2.zCoord *= arrowScale * length;

              // Draw four fins.
              tess.addVertex(tip.xCoord, tip.yCoord, tip.zCoord);
              tess.addVertex(tail.xCoord + fin1.xCoord, tail.yCoord
                                                        + fin1.yCoord,
                tail.zCoord + fin1.zCoord);
              tess.addVertex(tip.xCoord, tip.yCoord, tip.zCoord);
              tess.addVertex(tail.xCoord - fin1.xCoord, tail.yCoord
                                                        - fin1.yCoord,
                tail.zCoord - fin1.zCoord);
              tess.addVertex(tip.xCoord, tip.yCoord, tip.zCoord);
              tess.addVertex(tail.xCoord + fin2.xCoord, tail.yCoord
                                                        + fin2.yCoord,
                tail.zCoord + fin2.zCoord);
              tess.addVertex(tip.xCoord, tip.yCoord, tip.zCoord);
              tess.addVertex(tail.xCoord - fin2.xCoord, tail.yCoord
                                                        - fin2.yCoord,
                tail.zCoord - fin2.zCoord);
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
   * Draw all of the annotations associated with this BlockEditSet.
   */
  public void drawAnnotations()
  {
    DisplaySettings settings = Controller.instance.getDisplaySettings();
    if (settings.areAnnotationsShown() && !_annotations.isEmpty())
    {
      Minecraft mc = ModLoader.getMinecraftInstance();
      mc.entityRenderer.disableLightmap(0.0);
      GL11.glDisable(GL11.GL_LIGHTING);
      GL11.glDisable(GL11.GL_FOG);

      for (Annotation annotation : _annotations)
      {
        annotation.draw();
      }

      GL11.glEnable(GL11.GL_FOG);
      GL11.glEnable(GL11.GL_LIGHTING);
      mc.entityRenderer.enableLightmap(0.0);
    } // if drawing annotations
  } // drawAnnotations

  // --------------------------------------------------------------------------
  /**
   * Return the list of {@link Annotation}s.
   * 
   * @return the list of {@link Annotation}s.
   */
  public ArrayList<Annotation> getAnnotations()
  {
    return _annotations;
  }

  // --------------------------------------------------------------------------
  /**
   * Start animating all of the edits in the list.
   */
  public void startAnimating()
  {
    // Record current time as start time.
    // Set cursor in _edits to oldest edit position.
  }

  // --------------------------------------------------------------------------
  /**
   * A set of BlockEdit instances, ordered from oldest (lowest time value) to
   * most recent.
   */
  protected TreeSet<BlockEdit>    _edits                 = new TreeSet<BlockEdit>(
                                                           new BlockEditComparator());

  /**
   * The list of Annotations associated with this set of edits.
   */
  protected ArrayList<Annotation> _annotations           = new ArrayList<Annotation>();

  /**
   * Size of the arrow on a unit length vector.
   */
  protected static final double   UNIT_VECTOR_ARROW_SIZE = 0.025;

  /**
   * Maximum size of an arrow in world units.
   */
  protected static final double   MAX_ARROW_SIZE         = 0.5;

} // class BlockEditList

