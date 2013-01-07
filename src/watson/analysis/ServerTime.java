package watson.analysis;

import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import watson.Controller;
import watson.TimeStamp;
import watson.chat.ChatClassifier;
import watson.chat.ChatProcessor;
import watson.chat.MethodChatHandler;
import watson.chat.TagDispatchChatHandler;
import watson.debug.Log;

// ----------------------------------------------------------------------------
/**
 * An Analysis implementation that (ab)uses a LogBlock query to get the local
 * time at the server.
 * 
 * The query is executed once, at server login. It uses a player name longer
 * than 16 characters to be certain that no results will be found and puts a
 * tight limit on the time span in an attempt to ensure that the database does
 * essentially no work.
 */
public class ServerTime extends Analysis
{
  // --------------------------------------------------------------------------
  /**
   * The single instance of this class.
   */
  public static ServerTime instance = new ServerTime();

  // --------------------------------------------------------------------------
  /**
   * @see watson.analysis.Analysis#registerAnalysis(watson.chat.TagDispatchChatHandler)
   */
  @Override
  public void registerAnalysis(TagDispatchChatHandler tagDispatchChatHandler)
  {
    tagDispatchChatHandler.addChatHandler("lb.header.timecheck",
      new MethodChatHandler(this, "lbHeaderTimeCheck"));
    tagDispatchChatHandler.addChatHandler("lb.header.noresults",
      new MethodChatHandler(this, "lbHeaderNoResults"));
    _lbHeaderTimeCheck = ChatProcessor.getInstance().getChatClassifier().getChatCategoryById(
      "lb.header.timecheck").getFullPattern();
  }

  // --------------------------------------------------------------------------
  /**
   * Return the number of minutes that local time is ahead of the server local
   * time.
   * 
   * NOTE: queryLocalTime() must be called and the server must return a result
   * before this method can work correctly. If that is not done, the returned
   * time difference will be zero minutes.
   * 
   * @return the number of minutes that local time is ahead of the server local
   *         time; a negative number signifies that the server's clock is ahead
   *         of local time.
   */
  public int getLocalMinusServerMinutes()
  {
    String serverIP = Controller.instance.getServerIP();
    if (serverIP == null)
    {
      return 0;
    }
    else
    {
      Integer offsetMinutes = _localMinusServerMinutes.get(serverIP);
      return offsetMinutes != null ? offsetMinutes : 0;
    }
  } // getLocalMinusServerMinutes

  // --------------------------------------------------------------------------
  /**
   * Issue a LogBlock query to determine the difference between the local time
   * and time at the server. The query will generate a result header of the
   * form:
   * 
   * Block changes from player watsonservertimecheck between 1552 and 1552
   * minutes ago in world:
   * 
   * The query is only issued if we are connected to a server and we have not
   * already stored a time difference for that server.
   * 
   * @param showServerTime if true, the time at the server will be displayed as
   *          soon as it is known.
   */
  public void queryServerTime(boolean showServerTime)
  {
    String serverIP = Controller.instance.getServerIP();
    if (serverIP != null)
    {
      if (_localMinusServerMinutes.get(serverIP) == null)
      {
        Calendar pastTime = getPastTime();
        String date = String.format("%d.%d.%d",
          pastTime.get(Calendar.DAY_OF_MONTH),
          pastTime.get(Calendar.MONTH) + 1, pastTime.get(Calendar.YEAR));
        String query = String.format(
          "/lb player watsonservertimecheck since %s 00:00:00 before %s 00:00:01 limit 1",
          date, date);
        Log.debug("Server time query for " + serverIP + ": " + query);
        _showServerTime = showServerTime;
        Controller.instance.serverChat(query);
      }
      // Server time is already known. Show it now if required.
      else if (showServerTime)
      {
        showCurrentServerTime();
      }
    }
  } // queryServerTime

  // --------------------------------------------------------------------------
  /**
   * This method is called by the {@link ChatClassifier} when a chat line is
   * assigned the "lb.header.timecheck" category.
   */
  @SuppressWarnings("unused")
  private void lbHeaderTimeCheck(watson.chat.ChatLine line)
  {
    String serverIP = Controller.instance.getServerIP();
    if (serverIP != null && _localMinusServerMinutes.get(serverIP) == null)
    {
      Matcher m = _lbHeaderTimeCheck.matcher(line.getUnformatted());
      if (m.matches())
      {
        int serverMinutes = Integer.parseInt(m.group(1));

        // Express getPastTime() as a certain number of minutes behind local
        // time.
        Calendar now = Calendar.getInstance();
        Calendar pastTime = getPastTime();
        int localMinutes = (int) ((now.getTimeInMillis() - pastTime.getTimeInMillis()) / MINUTES_TO_MILLISECONDS);

        // This number is positive if local time is ahead of the server.
        int localMinusServer = localMinutes - serverMinutes;
        _localMinusServerMinutes.put(serverIP, localMinusServer);
        Log.debug("Past time was " + serverMinutes
                  + " minutes ago on the server and " + localMinutes
                  + " minutes ago on the client.");
        Log.debug("Client is " + localMinusServer
                  + " minutes ahead of the server.");

        // Have we scheduled echoing of the server time?
        if (_showServerTime)
        {
          showCurrentServerTime();
          _showServerTime = false;
        }

        // Suppress the subsequent "No results found.".
        _echoNextNoResults = false;
      } // pattern matched
    } // need to compute the offset
  } // lbHeaderTimeCheck

  // --------------------------------------------------------------------------
  /**
   * This method is called by the {@link ChatClassifier} when a chat line is
   * assigned the "lb.header.noresults" category.
   */
  @SuppressWarnings("unused")
  private void lbHeaderNoResults(watson.chat.ChatLine line)
  {
    Log.debug("lbHeaderNoResults() " + line.getCategory().getTag());
    if (_echoNextNoResults)
    {
      Controller.instance.localChat(line.getFormatted());
      Log.debug("Echoed " + line.getUnformatted());
    }
    _echoNextNoResults = true;
  }

  // --------------------------------------------------------------------------
  /**
   * Show the current time at the server.
   * 
   * This method is only called when the time at the server is actually known.
   */
  private void showCurrentServerTime()
  {
    String serverIP = Controller.instance.getServerIP();
    Integer localMinusServerMinutes = _localMinusServerMinutes.get(serverIP);
    long serverMillis = System.currentTimeMillis() - localMinusServerMinutes
                        * MINUTES_TO_MILLISECONDS;
    Controller.instance.localOutput(TimeStamp.formatMonthDayTime(serverMillis));
  }

  // --------------------------------------------------------------------------
  /**
   * Return a timestamp that is definitely in the server's past no matter how
   * far ahead of server time the local clock is.
   * 
   * The time of midnight two days ago (local time) was chosen for this purpose.
   * It is far enough in the past that inaccuracies in the local or server
   * clocks should not matter.
   * 
   * @return a timestamp that is definitely in the server's past no matter how
   *         far ahead of server time the local clock is.
   * 
   */
  private Calendar getPastTime()
  {
    Calendar pastTime = Calendar.getInstance();
    pastTime.add(Calendar.DAY_OF_MONTH, -2);
    pastTime.set(Calendar.HOUR_OF_DAY, 0);
    pastTime.set(Calendar.MINUTE, 0);
    return pastTime;
  }

  // --------------------------------------------------------------------------
  /**
   * Conversion gactor to convert minutes to milliseconds.
   */
  private static final int           MINUTES_TO_MILLISECONDS  = 60 * 1000;

  /**
   * The pattern of full lb.header.timechecklines.
   */
  protected Pattern                  _lbHeaderTimeCheck;

  /**
   * A map from server IP or DNS name to number of minutes that local time is
   * ahead of the server time (negative for behind).
   */
  protected HashMap<String, Integer> _localMinusServerMinutes = new HashMap<String, Integer>();

  /**
   * If true, the next "No results found." chat line is re-echoed. We use this
   * facility to suppress that line for the time check query. Don't look at me
   * like that. :P
   */
  protected boolean                  _echoNextNoResults       = true;

  /**
   * If true, the result of querying the current server time will be displayed
   * in chat.
   */
  protected boolean                  _showServerTime          = false;
} // class ServerTime