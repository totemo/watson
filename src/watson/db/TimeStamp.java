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
   * @param hour the hour of the day, from 0 to 23.
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
   * @param hour the hour of the day, from 0 to 23.
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
   * Convert a given date and time into milliseconds since epoch.
   *
   * @see #toMillis(int, int, int, int, int)
   * @see #toMillis(int, int, int, int, int, int)
   *
   * @param ymd year, month and day as integers; if year is 0, it was not
   *          specified and must be guessed.
   * @param hour the hour of the day, from 0 to 23.
   * @param minute the minute from 0 to 59.
   * @param second the second from 0 to 59.
   */
  public static long toMillis(int[] ymd, int hour, int minute, int second)
  {
    // Two-digit years, if specified, are in the 21st century.
    if (ymd[0] != 0 && ymd[0] < 100)
    {
      ymd[0] += 2000;
    }
    return (ymd[0] == 0)
      ? toMillis(ymd[1], ymd[2], hour, minute, second)
      : toMillis(ymd[0], ymd[1], ymd[2], hour, minute, second);
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
   * Parse a date in the format [year-]month-day.
   *
   * This method handles LogBlock dates in the default format and adds support
   * for an optional year to be specified if LogBlock is configured for that.
   *
   * @param date a string in the form (YY(YY)?-)?MM-dd; that is, an optional 2
   *          or 4 digit year preceding 2 digit month and day.
   * @return an array of 3 ints: year, month and day. Year is 0 if not
   *         explicitly specified in the date String.
   */
  public static int[] parseYMD(String date)
  {
    int[] ymd = {0, 0, 0};
    String[] parts = date.split("-");
    if (parts.length == 2)
    {
      ymd[1] = Integer.parseInt(parts[0]);
      ymd[2] = Integer.parseInt(parts[1]);
    }
    else if (parts.length == 3)
    {
      ymd[0] = Integer.parseInt(parts[0]);
      ymd[1] = Integer.parseInt(parts[1]);
      ymd[2] = Integer.parseInt(parts[2]);
    }
    return ymd;
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