package watson.debug;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import watson.Controller;

// ----------------------------------------------------------------------------
/**
 * A simple facade over the Java logging facilities that coordinates all of
 * Watson's logging.
 * 
 * Messages are logged to ".minecraft/mods/watson/log.txt". Since the vanilla
 * client now logs chat to the console, Watson no longer logs there to avoid
 * duplication.
 */
public class Log
{
  // --------------------------------------------------------------------------
  /**
   * Log a debug message.
   * 
   * The debug logging level needs to be enabled by calling Log.setDebug(true)
   * for this level of logging to reach the log file.
   * 
   * @param msg the message.
   */
  public static void debug(String msg)
  {
    _logger.fine(msg);
  }

  // --------------------------------------------------------------------------
  /**
   * Log a configuration message.
   * 
   * @param msg the message.
   */
  public static void config(String msg)
  {
    _logger.config(msg);
  }

  // --------------------------------------------------------------------------
  /**
   * Log an informational message.
   * 
   * @param msg the message.
   */
  public static void info(String msg)
  {
    _logger.info(msg);
  }

  // --------------------------------------------------------------------------
  /**
   * Log a warning message.
   * 
   * @param msg the message.
   */
  public static void warning(String msg)
  {
    _logger.warning(msg);
  }

  // --------------------------------------------------------------------------
  /**
   * Log a severe error message.
   * 
   * @param msg the message.
   */
  public static void severe(String msg)
  {
    _logger.severe(msg);
  }

  // --------------------------------------------------------------------------
  /**
   * Log an exception.
   * 
   * @param level the log level, e.g. Level.FINE (for debug), Level.INFO, etc.
   * @param msg the message.
   */
  public static void exception(Level level, String msg, Throwable t)
  {
    _logger.log(level, msg, t);
  }

  // --------------------------------------------------------------------------
  /**
   * Enable or disable debug log messages.
   * 
   * @param enabled if true, debug messages are logged.
   */
  public static void setDebug(boolean enabled)
  {
    _logger.setLevel(enabled ? Level.FINE : Level.CONFIG);
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if debug messages are logged.
   * 
   * @return true if debug messages are logged.
   */
  public static boolean isDebug()
  {
    return _logger.getLevel().intValue() <= Level.FINE.intValue();
  }

  // --------------------------------------------------------------------------
  /**
   * Close the log file.
   */
  public static void close()
  {
    if (_fileHandler != null)
    {
      _fileHandler.close();
    }
  }

  // --------------------------------------------------------------------------
  /**
   * The file name of the log file relative to the Watson's ModLoader directory.
   */
  private static final String LOG_FILE = "log.txt";

  /**
   * The Logger through which all logs are issued.
   */
  private static final Logger _logger;

  /**
   * The Handler that exports to the log file.
   */
  private static FileHandler  _fileHandler;

  static
  {
    SimpleFormatter formatter = new SimpleFormatter();

    _logger = Logger.getLogger("watson");;
    _logger.setUseParentHandlers(false);
    Log.setDebug(false);

    try
    {
      File logFile = new File(Controller.getModDirectory(), LOG_FILE);
      _fileHandler = new FileHandler(logFile.getAbsolutePath());
      _fileHandler.setFormatter(formatter);
      _logger.addHandler(_fileHandler);
    }
    catch (IOException e)
    {
      e.printStackTrace(System.err);
    }
  } // static initialisation block
} // class Log

