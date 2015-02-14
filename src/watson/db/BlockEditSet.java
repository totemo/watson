package watson.db;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import watson.Configuration;
import watson.Controller;
import watson.DisplaySettings;
import watson.chat.Chat;
import watson.model.ARGB;

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
      BlockEdit blockEdit = null;
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
          blockEdit = new BlockEdit(time.getTimeInMillis(), player, created, x,
                                    y, z, type);
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

      // If there was at least one BlockEdit, select it.
      if (blockEdit != null)
      {
        Controller.instance.selectBlockEdit(blockEdit);
      }
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
   * @param file the file to save.
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
      int editCount = 0;
      for (PlayerEditSet editsForPlayer : _playerEdits.values())
      {
        editCount += editsForPlayer.save(writer);
      }

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
    _playerEdits.clear();
    _annotations.clear();
    _oreDB.clear();
  }

  // --------------------------------------------------------------------------
  /**
   * Find an edit with the specified coordinates and, optionally, player.
   * 
   * @param x the x coordinate of the block
   * @param y the y coordinate of the block
   * @param z the z coordinate of the block
   * @param player the player name (can be null for a wildcard).
   * @return the matching edit, or null if not found.
   */
  public BlockEdit findEdit(int x, int y, int z, String player)
  {
    if (player != null)
    {
      PlayerEditSet editsForPlayer = _playerEdits.get(player.toLowerCase());
      return (editsForPlayer != null) ? editsForPlayer.findEdit(x, y, z) : null;
    }
    else
    {
      // Player is null (wildcard).
      for (PlayerEditSet editsForPlayer : _playerEdits.values())
      {
        BlockEdit edit = editsForPlayer.findEdit(x, y, z);
        if (edit != null)
        {
          return edit;
        }
      }
      return null;
    }
  } // findEdit

  // --------------------------------------------------------------------------
  /**
   * Add the specified edit to the list.
   * 
   * State variables describing the most recent edit (player, time, etc.) are
   * updated.
   * 
   * @param edit the BlockEdit describing an edit to add.
   * @return true if the edit passes the currently set filters.
   */
  public boolean addBlockEdit(BlockEdit edit)
  {
    return addBlockEdit(edit, true);
  }

  // --------------------------------------------------------------------------
  /**
   * Add the specified edit to the list.
   * 
   * @param edit the BlockEdit describing an edit to add.
   * @param updateVariables update the state variables for the most recent edit.
   * @return true if the edit passes the currently set filters.
   */
  public boolean addBlockEdit(BlockEdit edit, boolean updateVariables)
  {
    if (Controller.instance.getFilters().isAcceptedPlayer(edit.player))
    {
      if (updateVariables)
      {
        Controller.instance.selectBlockEdit(edit);
      }

      // Add a new PlayerEditSet if there isn't one for this player.
      String lowerName = edit.player.toLowerCase();
      PlayerEditSet editsForPlayer = _playerEdits.get(lowerName);
      if (editsForPlayer == null)
      {
        editsForPlayer = new PlayerEditSet(edit.player);
        _playerEdits.put(lowerName, editsForPlayer);
      }
      editsForPlayer.addBlockEdit(edit);

      // Only cluster edits into ore deposits on non-creative (survival,
      // adventure) games. I assume this will not stuff up for admins etc whose
      // gamemode is creative, but just in case, allow a configuration override.
      Minecraft mc = Minecraft.getMinecraft();
      if (!mc.theWorld.getWorldInfo().getGameType().isCreative()
          || Configuration.instance.isGroupingOresInCreative())
      {
        _oreDB.addBlockEdit(edit);
      }
      return true;
    }
    else
    {
      return false;
    }
  } // addBlockEdit

  // --------------------------------------------------------------------------
  /**
   * List the number and visibility of stored edits on a per player basis in the
   * dimension to which this BlockEditSet applies.
   */
  public void listEdits()
  {
    if (_playerEdits.size() == 0)
    {
      Chat.localOutput("There are no stored edits for this world.");
    }
    else
    {
      Chat.localOutput("Listing number and visibility of edits in this world:");
      for (PlayerEditSet editsByPlayer : _playerEdits.values())
      {
        Chat.localOutput(String.format(Locale.US,
          "  %s - %d edits %s", editsByPlayer.getPlayer(),
          editsByPlayer.getBlockEditCount(),
          (editsByPlayer.isVisible() ? "shown" : "hidden")));
      }
    }
  } // listEdits

  // --------------------------------------------------------------------------
  /**
   * Set the visibility of the edits for the specified player.
   * 
   * @param player the name of the player.
   * @param visible if true, edits are shown.
   */
  public void setEditVisibility(String player, boolean visible)
  {
    player = player.toLowerCase();
    PlayerEditSet editsByPlayer = _playerEdits.get(player);
    if (editsByPlayer != null)
    {
      editsByPlayer.setVisible(visible);
      Chat.localOutput(String.format(Locale.US,
        "%d edits by %s are now %s.", editsByPlayer.getBlockEditCount(),
        editsByPlayer.getPlayer(), (editsByPlayer.isVisible() ? "shown"
          : "hidden")));
    }
    else
    {
      Chat.localError(String.format(Locale.US,
        "There are no stored edits for %s.", player));
    }
  } // setEditVisibility

  // --------------------------------------------------------------------------
  /**
   * @param player the name of the player.
   */
  public void removeEdits(String player)
  {
    player = player.toLowerCase();
    PlayerEditSet editsByPlayer = _playerEdits.get(player);
    if (editsByPlayer != null)
    {
      _playerEdits.remove(player.toLowerCase());
      getOreDB().removeDeposits(player);
      Chat.localOutput(String.format(Locale.US,
        "%d edits by %s were removed.", editsByPlayer.getBlockEditCount(),
        editsByPlayer.getPlayer()));
    }
    else
    {
      Chat.localError(String.format(Locale.US,
        "There are no stored edits for %s.", player));
    }
  } // removeEdits

  // --------------------------------------------------------------------------
  /**
   * Draw wireframe outlines of all blocks.
   */
  public void drawOutlines()
  {
    if (Controller.instance.getDisplaySettings().isOutlineShown())
    {
      for (PlayerEditSet editsForPlayer : _playerEdits.values())
      {
        editsForPlayer.drawOutlines();
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
    if (settings.areVectorsShown())
    {
      int colourIndex = 0;
      for (PlayerEditSet editsForPlayer : _playerEdits.values())
      {
        editsForPlayer.drawVectors(_vectorColours[colourIndex]);
        colourIndex = (colourIndex + 1) % _vectorColours.length;
      }
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
      for (Annotation annotation : _annotations)
      {
        annotation.draw();
      }
    } // if drawing annotations
  } // drawAnnotations

  // --------------------------------------------------------------------------
  /**
   * Experimental: draw a HUD overlay listing ores.
   */
  public void drawHUD()
  {
    try
    {
      GlStateManager.pushMatrix();

      Minecraft mc = Minecraft.getMinecraft();
      ScaledResolution scaledResolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);

    }
    finally
    {
      GlStateManager.popMatrix();
    }
  } // drawHUD

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
   * Return the spatial database of ore deposits.
   * 
   * @return the spatial database of ore deposits.
   */
  public OreDB getOreDB()
  {
    return _oreDB;
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
   * A map from lowercase player name to {@link PlayerEditSet} containing that
   * player's edits, iterated in the order that the individual players were
   * first encountered in query results.
   */
  protected LinkedHashMap<String, PlayerEditSet> _playerEdits   = new LinkedHashMap<String, PlayerEditSet>();

  /**
   * The list of Annotations associated with this set of edits.
   */
  protected ArrayList<Annotation>                _annotations   = new ArrayList<Annotation>();

  /**
   * The spatial database indexing the edits.
   */
  protected OreDB                                _oreDB         = new OreDB();

  /**
   * The cycle of colours used to draw vectors for different players.
   */
  protected static final ARGB[]                  _vectorColours = {
                                                                // Formatters...
    new ARGB(204, 255, 255, 140), // Pale yellow.
    new ARGB(204, 140, 158, 255), // Light blue.
    new ARGB(204, 255, 140, 140), // Salmon.
    new ARGB(204, 121, 255, 140), // Mint.
    new ARGB(204, 255, 140, 255), // Pink.
    new ARGB(204, 192, 192, 192), // Old fashioned grey.
                                                                };
} // class BlockEditList
