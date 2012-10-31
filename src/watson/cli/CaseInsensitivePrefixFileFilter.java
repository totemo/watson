package watson.cli;

import java.io.File;
import java.io.FileFilter;

// ----------------------------------------------------------------------------
/**
 * A FileFilter information that matches readable, ordinary files whose name
 * begins with a specifed case insensitive prefix.
 */
public class CaseInsensitivePrefixFileFilter implements FileFilter
{
  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * @param prefix the prefix to match. Null is treated as the empty string and
   *          matches any name.
   */
  public CaseInsensitivePrefixFileFilter(String prefix)
  {
    _lowerPrefix = (prefix == null) ? "" : prefix.toLowerCase();
  }

  // --------------------------------------------------------------------------
  /**
   * @see java.io.FileFilter#accept(java.io.File)
   */
  @Override
  public boolean accept(File file)
  {
    return file.isFile()
           && file.canRead()
           && (_lowerPrefix.length() == 0 || file.getName().toLowerCase().startsWith(
             _lowerPrefix));
  }
  // --------------------------------------------------------------------------
  /**
   * The lower-case prefix. The empty string is used as the wildcard.
   */
  protected String _lowerPrefix;
} // class CaseInsensitivePrefixFileFilter