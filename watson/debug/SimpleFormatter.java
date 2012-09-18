package watson.debug;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

// --------------------------------------------------------------------------
/**
 * Formats log messages one per line.
 */
public class SimpleFormatter extends Formatter
{
  // --------------------------------------------------------------------------
  /**
   * Format the LogRecord.
   * 
   * @param log the record to format.
   */
  @Override
  public String format(LogRecord log)
  {
    StringBuilder builder = new StringBuilder();
    builder.append(_timeFormat.format(Long.valueOf(log.getMillis())));
    builder.append(" [");
    builder.append(log.getLevel().getName());
    builder.append("] ");
    builder.append(log.getMessage());
    builder.append('\n');

    Throwable thrown = log.getThrown();
    if (thrown != null)
    {
      StringWriter stringwriter = new StringWriter();
      thrown.printStackTrace(new PrintWriter(stringwriter));
      builder.append(stringwriter.toString());
    }

    return builder.toString();
  } // format

  // --------------------------------------------------------------------------
  /**
   * Formatter for time stamps.
   */
  private SimpleDateFormat _timeFormat = new SimpleDateFormat(
                                         "yyyy-MM-dd HH:mm:ss");

} // class SimpleFormatter