package watson;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.yaml.snakeyaml.Yaml;

import watson.debug.Log;
import watson.model.BlockModel;
import watson.model.BlockModelRegistry;

// ----------------------------------------------------------------------------
/**
 * Allow block types to be looked up by ID, (ID,data) tuple or name (from a
 * LogBlock query result).
 * 
 * Where a block is listed with a name containing spaces, synonyms are
 * automatically generated for the same name:
 * <ul>
 * <li>with the spaces removed.</li>
 * <li>with the spaces replaced by underscores.</li>
 * </ul>
 */
public final class BlockTypeRegistry
{
  /**
   * The single instance of this class.
   * 
   * Note: the registry is not useable until it has been initialised by calling
   * the loadBlockTypes() method.
   */
  public static final BlockTypeRegistry instance = new BlockTypeRegistry();

  // --------------------------------------------------------------------------
  /**
   * Load the BlockType instances from a configuration file or resource in the
   * minecraft.jar file.
   */
  public void loadBlockTypes()
  {
    try
    {
      BlockTypeRegistry registry = BlockTypeRegistry.instance;
      InputStream in = Controller.getConfigurationStream(BLOCK_TYPES_FILE);
      try
      {
        registry.loadBlockTypes(in);
      }
      finally
      {
        in.close();
      }
    }
    catch (Exception ex)
    {
      Log.exception(Level.SEVERE, "error loading block types: ", ex);
    }
  } // loadBlockTypes

  // --------------------------------------------------------------------------
  /**
   * Load the BlockType definitions in "blocks.yml" YAML format from an
   * InputStream.
   */
  @SuppressWarnings("unchecked")
  public void loadBlockTypes(InputStream in)
  {
    Yaml yaml = new Yaml();
    HashMap<String, Object> root = (HashMap<String, Object>) yaml.load(in);
    ArrayList<Object> blocks = (ArrayList<Object>) root.get("blocks");
    if (blocks == null)
    {
      Log.severe("the top level node of \"blocks.yml\" should be \"blocks\" but isn't; nothing will be drawn");
    }
    else
    {
      for (Object entry : blocks)
      {
        loadBlockType((HashMap<String, Object>) entry);
      }

      // Set all uninitialised BlockType entries to reference the "unknown"
      // block type. If one has not been defined by the stream, set that up
      // first.
      BlockType unknown = getBlockTypeByIndex(MAX_INDEX);
      if (unknown == null)
      {
        unknown = new BlockType();
        unknown.setIndex(MAX_INDEX);
        unknown.addName("unknown");
        unknown.setARGB(new ARGB(255, 255, 0, 255));
        unknown.setBlockModel(BlockModelRegistry.instance.getBlockModel("cuboid"));
        addBlockType(unknown);
      }

      // Note: array size is MAX_INDEX+1.
      for (int i = 0; i < MAX_INDEX; ++i)
      {
        if (_byIndex[i] == null)
        {
          _byIndex[i] = unknown;
        }
      }
    } // (blocks != null)
  } // loadBlockTypes

  // --------------------------------------------------------------------------
  /**
   * Load a BlockType instance from a YAML map node.
   */
  private void loadBlockType(Map<String, Object> map)
  {
    BlockType blockType = new BlockType();
    Integer id = loadScalar(map, "id", Integer.class, null);
    if (id == null)
    {
      Log.severe("a block type was specified without a valid id: value");
      return;
    }
    else
    {
      blockType.setId(id);
    }

    Integer data = loadScalar(map, "data", Integer.class, null);
    if (data != null)
    {
      blockType.setData(data);
    }
    else
    {
      data = 0;
    }

    if (getBlockTypeByIdData(id, data) != null)
    {

      Log.severe("block type " + blockType.getId() + ':' + blockType.getData()
                 + " has a duplicate definition; only the first counts");
      return;
    }

    ArrayList<String> names = loadArray(map, "names", String.class, null);
    if (names == null || names.size() == 0)
    {
      Log.severe("block type " + blockType.getId() + ':' + blockType.getData()
                 + " could not be loaded because it needs a list of names");
      return;
    }
    else
    {
      for (String name : names)
      {
        blockType.addName(name);
      }
    }

    Double lineWidth = loadScalar(map, "lineWidth", Double.class, 3.0);
    if (lineWidth == null)
    {
      Log.warning("block type "
                  + blockType.getId()
                  + ':'
                  + blockType.getData()
                  + " had an invalid linewidth; did you use an integer where a double was required?");
      lineWidth = 3.0;
    }
    blockType.setLineWidth((float) lineWidth.doubleValue());

    // Use null as the default to detect errors in the rgba value.
    ArrayList<Integer> rgba = loadArray(map, "rgba", Integer.class, null);
    if (rgba == null || rgba.size() < 3 || rgba.size() > 4)
    {
      Log.warning("block type "
                  + blockType.getId()
                  + ':'
                  + blockType.getData()
                  + " had a malformed colour value and was set to the default colour");
    }
    else
    {
      int red = rgba.get(0);
      int green = rgba.get(1);
      int blue = rgba.get(2);
      int alpha = rgba.size() == 4 ? rgba.get(3) : DEFAULT_ALPHA;
      blockType.setARGB(new ARGB(alpha, red, green, blue));
    }

    String modelName = loadScalar(map, "model", String.class, "cuboid");
    if (modelName == null)
    {
      Log.warning("block type "
                  + blockType.getId()
                  + ':'
                  + blockType.getData()
                  + " had an invalid model name and will be drawn as a simple cuboid");
      modelName = "cuboid";
    }
    BlockModel model = BlockModelRegistry.instance.getBlockModel(modelName);
    if (model == null)
    {
      model = BlockModelRegistry.instance.getBlockModel("cuboid");
    }
    blockType.setBlockModel(model);

    // Bounds.
    ArrayList<Double> bounds = loadArray(map, "bounds", Double.class,
      DEFAULT_BOUNDS);
    if (bounds == null || bounds.size() != 6)
    {
      Log.warning("block type "
                  + blockType.getId()
                  + ':'
                  + blockType.getData()
                  + " had a badly formed bounds setting; the default will be used");
      bounds = DEFAULT_BOUNDS;
    }
    blockType.setBounds((float) bounds.get(0).doubleValue(),
      (float) bounds.get(1).doubleValue(), (float) bounds.get(2).doubleValue(),
      (float) bounds.get(3).doubleValue(), (float) bounds.get(4).doubleValue(),
      (float) bounds.get(5).doubleValue());

    addBlockType(blockType);
  } // loadBlockType
  // --------------------------------------------------------------------------
  /**
   * A helper method for extracting scalar attributes from a map loaded by
   * SnakeYAML.
   * 
   * @param map the Map loaded by SnakeYAML.
   * @param name the name of the key.
   * @param cls the expected Class of the value.
   * @param defaultValue a default value to use if the key is to specified.
   * @return the attribute value, the default value if unspecified, or null if
   *         there is an error.
   */
  private <T> T loadScalar(Map<String, Object> map, String name, Class<T> cls,
                           T defaultValue)
  {
    Object value = map.get(name);
    if (value == null)
    {
      return defaultValue;
    }
    else
    {
      if (value.getClass() != cls)
      {
        Log.warning("for " + name + " expected " + cls.getName() + " but got "
                    + value.getClass());
        return null;
      }
      else
      {
        return (T) value;
      }
    }
  } // loadScalar

  // --------------------------------------------------------------------------
  /**
   * A helper method for extracting array attributes from a map loaded by
   * SnakeYAML.
   * 
   * @param map the Map loaded by SnakeYAML.
   * @param name the name of the key.
   * @param cls the expected Class of the value.
   * @param defaultValue a default value to use if the key is to specified.
   * @return the attribute value, the default value if unspecified, or null if
   *         there is an error.
   */
  @SuppressWarnings("unchecked")
  private <E> ArrayList<E> loadArray(Map<String, Object> map, String name,
                                     Class<E> elementClass,
                                     ArrayList<E> defaultValue)
  {
    Object value = map.get(name);
    if (value == null)
    {
      return defaultValue;
    }
    else
    {
      if (value.getClass() != ArrayList.class)
      {
        Log.warning("for " + name + " expected ArrayList<> but got "
                    + value.getClass());
        return null;
      }
      else
      {
        // Check that the elements are of the expected type.
        ArrayList<Object> result = (ArrayList<Object>) value;
        for (int i = 0; i < result.size(); ++i)
        {
          if (result.get(i).getClass() != elementClass)
          {
            Log.warning("for " + name + " expected array elements of type "
                        + elementClass.getName() + "but got "
                        + result.get(i).getClass());
            result = null;
            break;
          }
        }
        return (ArrayList<E>) result;
      }
    }
  } // loadArray

  // --------------------------------------------------------------------------
  /**
   * Add the specified BlockType to all of the collections.
   * 
   * @param blockType the BlockType to add.
   */
  private void addBlockType(BlockType blockType)
  {
    Log.debug("block type: " + blockType.toString());

    _byIndex[blockType.getIndex()] = blockType;
    for (int i = 0; i < blockType.getNameCount(); ++i)
    {
      String name = blockType.getName(i);
      _byName.put(name, blockType);

      // Where a name contains spaces, add a synonym with the spaces omitted.
      // Where synonyms are explicitly listed anyway, the map will collapse
      // duplicates.
      String noSpaces = name.replaceAll(" ", "");
      if (!name.equals(noSpaces))
      {
        _byName.put(noSpaces, blockType);
      }
      String underscores = name.replaceAll(" ", "_");
      _byName.put(underscores, blockType);
    } // for
  } // addBlockType

  // --------------------------------------------------------------------------
  /**
   * Return the BlockType with the specified ID and a data value of 0.
   * 
   * @param id the ID, which will be in the range [0,255] for Minecraft blocks.
   *          The special ID, 256, is used for the "unknown" block type.
   */
  public BlockType getBlockTypeById(int id)
  {
    return _byIndex[id << 4];
  }

  // --------------------------------------------------------------------------
  /**
   * Return the BlockType with the specified ID and data value.
   * 
   * @param id the ID, which will be in the range [0,255] for Minecraft blocks.
   *          The special ID, 256, is used for the "unknown" block type.
   * @param data the data value in the range [0,15].
   */
  public BlockType getBlockTypeByIdData(int id, int data)
  {
    return _byIndex[(id << 4) | (data & 0x0000000F)];
  }

  // --------------------------------------------------------------------------
  /**
   * Return the BlockType with the specified index.
   * 
   * The index is computed as (id * 16 + data).
   * 
   * @param index the index, in the range [0,4095] for Minecraft blocks, or 4096
   *          for the special "unknown" block.
   */
  public BlockType getBlockTypeByIndex(int index)
  {
    return _byIndex[index];
  }

  // --------------------------------------------------------------------------
  /**
   * Return the block type with the specified name (case insensitive).
   * 
   * @param name the name of the block as returned by LogBlock queries, e.g.
   *          "diamond ore".
   */
  public BlockType getBlockTypeByName(String name)
  {
    BlockType result = _byName.get(name.toLowerCase());
    if (result == null)
    {
      // Return the "unknown" BlockType.
      return getBlockTypeByIndex(MAX_INDEX);
    }
    return result;
  } // getBlockTypeByName

  // --------------------------------------------------------------------------
  /**
   * Private constructor to enforce Singleton pattern.
   */
  private BlockTypeRegistry()
  {
    // Do nothing.
  }

  // --------------------------------------------------------------------------
  /**
   * The basename of the file containing the YAML descriptions of all BlockType
   * instances.
   */
  private static final String            BLOCK_TYPES_FILE = "blocks.yml";

  /**
   * The maximum allowable index into the _byIndex array.
   * 
   * Index 4096 (id 256, data 0) is reserved for the "unknown" block type. Any
   * elements that are still null after reading in "blocks.yml" are set to
   * reference this element.
   */
  private static final int               MAX_INDEX        = 4096;

  /**
   * Default alpha colour component if not specified in "blocks.yml".
   */
  private static final int               DEFAULT_ALPHA    = (int) (0.8 * 255);

  // --------------------------------------------------------------------------
  /**
   * Default cuboid bounds when loaded from "blocks.yml".
   */
  private static final ArrayList<Double> DEFAULT_BOUNDS   = new ArrayList<Double>(
                                                            Arrays.asList(0.01,
                                                              0.01, 0.01, 0.99,
                                                              0.99, 0.99));

  /**
   * An array of BlockType instances accessed by index.
   */
  private BlockType[]                    _byIndex         = new BlockType[MAX_INDEX + 1];

  /**
   * A map from name (primary or alias) to BlockType instance.
   */
  private Map<String, BlockType>         _byName          = new HashMap<String, BlockType>();
} // class BlockTypeRegistry