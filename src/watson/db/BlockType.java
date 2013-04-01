package watson;

import java.util.ArrayList;

import watson.model.BlockModel;

// --------------------------------------------------------------------------
/**
 * Describes the name(s) (for the purpose of LogBlock queries), ID and data
 * value modifier, and wireframe colour and line thickness of a block.
 * 
 * This class is similar in purpose to the Minecraft class Block, but includes
 * additional information. All BlockType instances are initialised by loading
 * them from the YAML file "blocks.yml". See {@link BlockTypeLexicon} for more
 * on loading and lookup of BlockType instances.
 * 
 * This class is declared final to facilitate inlining.
 */
public final class BlockType
{
  /**
   * Set the block type ID of the block.
   * 
   * All Minecraft blocks have an ID in the range [0,255]. Watson uses ID 256 to
   * denote unknown blocks whose style has not been set up in "blocks.yml".
   * 
   * @param id the id.
   */
  public void setId(int id)
  {
    _index = (id << 4) | (_index & 0x0000000F);
  }

  // --------------------------------------------------------------------------
  /**
   * Return the block type ID of the block.
   * 
   * @return the block type ID of the block.
   */
  public int getId()
  {
    return (_index >> 4);
  }

  // --------------------------------------------------------------------------
  /**
   * Set the 4-bit extra data associated with the block.
   * 
   * @param data the data value in the range [0,15].
   */
  public void setData(int data)
  {
    _index = (_index & 0xFFFFFFF0) | (data & 0x0000000F);
  }

  // --------------------------------------------------------------------------
  /**
   * Return the 4-bit extra data associated with the block.
   * 
   * @return the 4-bit extra data associated with the block.
   */
  public int getData()
  {
    return _index & 0x0000000F;
  }

  // --------------------------------------------------------------------------
  /**
   * Set the index value.
   * 
   * @param index the index.
   */
  public void setIndex(int index)
  {
    _index = index;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the index value, which is (16 * ID) + data.
   */
  public int getIndex()
  {
    return _index;
  }

  // --------------------------------------------------------------------------
  /**
   * Append the specified name to the list of names of this block type.
   * 
   * @param name the new name.
   */
  public void addName(String name)
  {
    _names.add(name);
  }

  // --------------------------------------------------------------------------
  /**
   * Return the name with the specified array index.
   * 
   * @return the name with the specified array index.
   */
  public String getName(int index)
  {
    return _names.get(index);
  }

  // --------------------------------------------------------------------------
  /**
   * Return the number of names listed for this block type.
   * 
   * @return the number of names listed for this block type.
   */
  public int getNameCount()
  {
    return _names.size();
  }

  // --------------------------------------------------------------------------
  /**
   * Set the colour of this BlockType as an ARGB quad.
   * 
   * @param argb the ARGB (alpha, red, green, blue) value.
   */
  public void setARGB(ARGB argb)
  {
    _argb = argb;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the colour of this BlockType as an ARGB quad.
   * 
   * @return the colour of this BlockType as an ARGB quad.
   */
  public ARGB getARGB()
  {
    return _argb;
  }

  // --------------------------------------------------------------------------
  /**
   * Set the bounding cuboid of this block's wireframe {@link BlockModel}.
   * 
   * A simple cube that is coincident with the solid blocks drawn by the game
   * would have (x1,y1,z1) = (0.0,0.0,0.0) and (x2,y2,z2) = (1.0,1.0,1.0). A
   * cube could be drawn smaller than a normal block by moving both of these
   * points in towards (0.5, 0.5, 0.5). It's also possible to make a cuboid
   * larger than a game block by setting {x1 < 0, y1 < 0, z1 < 0, x2 > 1, y2 >
   * 1, z2 > 1}.
   */
  public void setBounds(float x1, float y1, float z1, float x2, float y2,
                        float z2)
  {
    _x1 = x1;
    _y1 = y1;
    _z1 = z1;
    _x2 = x2;
    _y2 = y2;
    _z2 = z2;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the minimum X coordinate of the bounding cuboid.
   * 
   * @return the minimum X coordinate of the bounding cuboid.
   */
  public float getX1()
  {
    return _x1;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the minimum Y coordinate of the bounding cuboid.
   * 
   * @return the minimum Y coordinate of the bounding cuboid.
   */
  public float getY1()
  {
    return _y1;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the minimum Z coordinate of the bounding cuboid.
   * 
   * @return the minimum Z coordinate of the bounding cuboid.
   */
  public float getZ1()
  {
    return _z1;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the maximum X coordinate of the bounding cuboid.
   * 
   * @return the maximum X coordinate of the bounding cuboid.
   */
  public float getX2()
  {
    return _x2;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the maximum Y coordinate of the bounding cuboid.
   * 
   * @return the maximum Y coordinate of the bounding cuboid.
   */
  public float getY2()
  {
    return _y2;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the maximum Z coordinate of the bounding cuboid.
   * 
   * @return the maximum Z coordinate of the bounding cuboid.
   */
  public float getZ2()
  {
    return _z2;
  }

  // --------------------------------------------------------------------------
  /**
   * Set the wireframe line thickness.
   * 
   * @param lineWidth the wireframe line thickness.
   */
  public void setLineWidth(float lineWidth)
  {
    _lineWidth = lineWidth;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the wireframe line thickness.
   * 
   * @return the wireframe line thickness.
   */
  public float getLineWidth()
  {
    return _lineWidth;
  }

  // --------------------------------------------------------------------------
  /**
   * Set the 3-D model used to draw this type of block.
   * 
   * @param blockModel the 3-D model.
   */
  public void setBlockModel(BlockModel blockModel)
  {
    _blockModel = blockModel;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the 3-D model used to draw this type of block.
   * 
   * @return the 3-D model used to draw this type of block.
   */
  public BlockModel getBlockModel()
  {
    return _blockModel;
  }

  // --------------------------------------------------------------------------
  /**
   * Return a String representation, for debugging only.
   */
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append(getIndex());
    builder.append(" (");
    builder.append(getId());
    builder.append(':');
    builder.append(getData());
    builder.append("), {");
    for (int i = 0; i < getNameCount(); ++i)
    {
      builder.append(getName(i));
      if (i < getNameCount() - 1)
      {
        builder.append(',');
      }
    }
    builder.append("} [");
    builder.append(getARGB().getRed());
    builder.append(',');
    builder.append(getARGB().getGreen());
    builder.append(',');
    builder.append(getARGB().getBlue());
    builder.append(',');
    builder.append(getARGB().getAlpha());
    builder.append("] ");
    builder.append(getLineWidth());
    builder.append(", ");
    builder.append(getBlockModel().getName());
    builder.append(", (");
    builder.append(getX1());
    builder.append(',');
    builder.append(getY1());
    builder.append(',');
    builder.append(getZ1());
    builder.append(") - (");
    builder.append(getX2());
    builder.append(',');
    builder.append(getY2());
    builder.append(',');
    builder.append(getZ2());
    builder.append(')');
    return builder.toString();
  } // toString

  // --------------------------------------------------------------------------
  /**
   * The index value is (16 * ID) + (data & 0xF). Normal Minecraft blocks
   * therefore have indices in the range [0,4095]. I use index 4096 (ID 256,
   * data 0) as the default block type index for unknown/new blocks. The
   * "blocks.yml" file sets them to a bright shade of pink, so hopefully I will
   * notice them and add them as they come up. :)
   */
  private int               _index;

  /**
   * The list of names by which this block can be referenced in Watson queries.
   * The first name in the list should always be the name that LogBlock returns
   * in query results.
   */
  private ArrayList<String> _names     = new ArrayList<String>();

  /**
   * The 3-D model used to draw blocks of this type. If unspecified by
   * "blocks.yml", the model defaults to CuboidBlockModel. Most blocks are drawn
   * as simple cuboids.
   */
  private BlockModel        _blockModel;

  /**
   * ARGB colour to draw blocks of this type.
   */
  private ARGB              _argb;

  /**
   * The width of wireframe lines used to draw the block.
   */
  private float             _lineWidth = 3.0f;

  /**
   * Minimum x coordinate of the bounding box.
   */
  private float             _x1        = 0.0f;

  /**
   * Maximum x coordinate of the bounding box.
   */
  private float             _x2        = 1.0f;

  /**
   * Minimum y coordinate of the bounding box.
   */
  private float             _y1        = 0.0f;

  /**
   * Maximum y coordinate of the bounding box.
   */
  private float             _y2        = 1.0f;

  /**
   * Minimum z coordinate of the bounding box.
   */
  private float             _z1        = 0.0f;

  /**
   * Maximum z coordinate of the bounding box.
   */
  private float             _z2        = 1.0f;
} // class BlockType
