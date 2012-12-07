package watson.db;

// ----------------------------------------------------------------------------
/**
 * A 3-D position class with integer coordinate values.
 */
public class IntCoord
{
  // --------------------------------------------------------------------------
  /**
   * Default constructor; initialises to (0,0,0).
   */
  public IntCoord()
  {
    // Default Java initialisation.
  }

  // --------------------------------------------------------------------------
  /**
   * Copy constructor.
   * 
   * @param coord the coordinate to copy.
   */
  public IntCoord(IntCoord coord)
  {
    setXYZ(coord._x, coord._y, coord._z);
  }

  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * @param x the x coordinate.
   * @param y the y coordinate.
   * @param z the z coordinate.
   */
  public IntCoord(int x, int y, int z)
  {
    setXYZ(x, y, z);
  }

  // --------------------------------------------------------------------------

  public void setXYZ(int x, int y, int z)
  {
    _x = x;
    _y = y;
    _z = z;
  }

  // --------------------------------------------------------------------------
  /**
   * Set the x coordinate.
   * 
   * @Param x the x coordinate.
   */
  public void setX(int x)
  {
    _x = x;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the x coordinate.
   * 
   * @return the x coordinate.
   */
  public int getX()
  {
    return _x;
  }

  // --------------------------------------------------------------------------
  /**
   * Set the y coordinate.
   * 
   * @Param y the y coordinate.
   */
  public void setY(int y)
  {
    _y = y;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the y coordinate.
   * 
   * @return the y coordinate.
   */
  public int getY()
  {
    return _y;
  }

  // --------------------------------------------------------------------------
  /**
   * Set the z coordinate.
   * 
   * @Param z the z coordinate.
   */
  public void setZ(int z)
  {
    _z = z;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the z coordinate.
   * 
   * @return the z coordinate.
   */
  public int getZ()
  {
    return _z;
  }

  // --------------------------------------------------------------------------
  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj)
  {
    if (obj == this)
    {
      return true;
    }
    else if (obj != null && obj instanceof IntCoord)
    {
      IntCoord c = (IntCoord) obj;
      return (c._x == _x && c._y == _y && c._z == _z);
    }
    else
    {
      return false;
    }
  } // equals

  // --------------------------------------------------------------------------
  /**
   * @see java.lang.Object#hashCode()
   * 
   *      _y < 256, so left shifting 24 will lose no precision. abs(_z) is most
   *      likely much less than (2^(32-15)), so again, there will be no loss of
   *      precision.
   */
  @Override
  public int hashCode()
  {
    return (_y << 24) ^ _x ^ (_z << 15);
  }

  // --------------------------------------------------------------------------
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return "(" + _x + "," + _y + "," + _z + ")";
  }

  // --------------------------------------------------------------------------
  /**
   * The x coordinate.
   */
  protected int _x;

  /**
   * The y coordinate.
   */
  protected int _y;

  /**
   * The z coordinate.
   */
  protected int _z;
} // class IntCoord