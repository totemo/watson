package watson.db;

import java.util.Calendar;
import java.util.Locale;

// ----------------------------------------------------------------------------
/**
 * Centralises common code for time parsing and deals intelligently with the
 * lack of a year on timestamps in LogBlock output.
 */
public class TimeStamp
{
  // --------------------------------------------------------------------------
  /**
   * Convert a given month, day of the month, hour, minute and second into
   * milliseconds since epoch.
   * 
   * Since the time components passed as parameters to this method originate on
   * the server, in a different time zone, the resulting timestamp is not the
   * actual local time of the event, but it will convert between the two forms
   * consistently.
   * 
   * @param month the month, from 1 to 12.
   * @param dayOfMonth the day of the month, from 1 to 31.
   * @param hourOfDay the hour of the day, from 0 to 23.
   * @param minute the minute from 0 to 59.
   * @param second the second from 0 to 59.
   */
  public static long toMillis(int month, int dayOfMonth, int hour, int minute,
                              int second)
  {
    // Try assuming that the year is the same as the reference.
    _time.set(_reference.get(Calendar.YEAR), month - 1, dayOfMonth, hour,
      minute, second);

    // If the resulting time is more into the future than the reference, then
    // we guessed the wrong year (assuming the LogBlock retention is NOT 51
    // weeks).
    if (_time.getTimeInMillis() > _reference.getTimeInMillis())
    {
      _time.add(Calendar.YEAR, -1);
    }
    return _time.getTimeInMillis();
  } // toMillis

  // --------------------------------------------------------------------------
  /**
   * Convert a given year, month, day of the month, hour, minute and second into
   * milliseconds since epoch.
   * 
   * Since the time components passed as parameters to this method originate on
   * the server, in a different time zone, the resulting timestamp is not the
   * actual local time of the event, but it will convert between the two forms
   * consistently.
   * 
   * @param year the four digit year.
   * @param month the month, from 1 to 12.
   * @param dayOfMonth the day of the month, from 1 to 31.
   * @param hourOfDay the hour of the day, from 0 to 23.
   * @param minute the minute from 0 to 59.
   * @param second the second from 0 to 59.
   */
  public static long toMillis(int year, int month, int dayOfMonth, int hour,
                              int minute, int second)
  {
    _time.set(year, month - 1, dayOfMonth, hour, minute, second);
    return _time.getTimeInMillis();
  }

  // --------------------------------------------------------------------------
  /**
   * Format a millisecond time into the "MM-DD hh:mm:ss" format typically used
   * in LogBlock query results in chat.
   * 
   * @param millis a timestamp in the form of milliseconds since epoch.
   * @return a String suitable for use in chat.
   */
  public static String formatMonthDayTime(long millis)
  {
    _time.setTimeInMillis(millis);
    return String.format(Locale.US, "%02d-%02d %02d:%02d:%02d",
      _time.get(Calendar.MONTH) + 1, _time.get(Calendar.DAY_OF_MONTH),
      _time.get(Calendar.HOUR_OF_DAY), _time.get(Calendar.MINUTE),
      _time.get(Calendar.SECOND));
  }

  // --------------------------------------------------------------------------
  /**
   * Format a millisecond time into the format "DD.MM.YYYY hh:mm:ss" so that it
   * can be used as a "since" or "before" parameter value in LogBlock queries.
   * 
   * @param millis a timestamp in the form of milliseconds since epoch.
   * @return a the formatted time.
   */
  public static String formatQueryTime(long millis)
  {
    _time.setTimeInMillis(millis);
    return String.format(Locale.US, "%d.%d.%d %02d:%02d:%02d",
      _time.get(Calendar.DAY_OF_MONTH), _time.get(Calendar.MONTH) + 1,
      _time.get(Calendar.YEAR), _time.get(Calendar.HOUR_OF_DAY),
      _time.get(Calendar.MINUTE), _time.get(Calendar.SECOND));
  }

  // --------------------------------------------------------------------------
  /**
   * A reusable Calendar instance used to interpret any time stamps found in
   * LogBlock results.
   */
  protected static Calendar _time = Calendar.getInstance();

  /**
   * Used to infer the implicit (absent) year in LogBlock timestamps.
   */
  protected static Calendar _reference;
  static
  {
    // Set the reference timestamp to the client's local time, plus one week
    // into the future.
    _reference = Calendar.getInstance();
    _reference.add(Calendar.WEEK_OF_YEAR, 1);
  }
} // class TimeStamp