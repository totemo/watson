package watson.analysis;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import watson.Configuration;
import watson.Controller;
import watson.chat.ChatLine;
import watson.chat.ChatProcessor;
import watson.chat.MethodChatHandler;
import watson.chat.TagDispatchChatHandler;

// ----------------------------------------------------------------------------
/**
 * An {@link Analysis} that runs /region info region name for all of the regions
 * listed in chat (tag wg.regions) when you right click with a wooden sword.
 * 
 * To minimise spam, the rate at which /region info commands can be issued to
 * the server is limited by a timeout.
 */
public class RegionInfoAnalysis extends Analysis
{
  // --------------------------------------------------------------------------
  /**
   * Register a handler for the wg.regions chat category.
   */
  @Override
  public void registerAnalysis(TagDispatchChatHandler tagDispatchChatHandler)
  {
    tagDispatchChatHandler.setChatHandler("wg.regions", new MethodChatHandler(
      this, "wgRegions"));
    _wgRegions = ChatProcessor.getInstance().getChatClassifier().getChatCategoryById(
      "wg.regions").getFullPattern();
    _regionNames = Pattern.compile("[a-zA-Z0-9_-]+");
  }

  // --------------------------------------------------------------------------
  /**
   * Respond to wg.regions by issuing the corresponding /region info commands.
   */
  public void wgRegions(ChatLine line)
  {
    long now = System.currentTimeMillis();
    if (now - _lastCommandTime > (long) (Configuration.instance.getRegionInfoTimeoutSeconds() * 1000))
    {
      Matcher m = _wgRegions.matcher(line.getUnformatted());
      if (m.matches())
      {
        int regionCount = 0;

        // Group 1 contains the comma-delimited list of region names.
        // We need to pull that apart with another regexp because nesting a
        // capturing group in a non-capturing group was not working for me.
        Matcher names = _regionNames.matcher(m.group(1));
        while (names.find())
        {
          Controller.instance.serverChat("/region info " + names.group());
          ++regionCount;
        }

        // Controller.serverChat() queues up commands and issues them at the
        // rate of one per second. Avoid queueing up many "/region info"s
        // when the user is spamming wood sword where there are multiple
        // overlapping regions. Add the corresponding delay for each additional
        // region queried *after the first*.
        _lastCommandTime = now + Controller.CHAT_TIMEOUT_MILLIS
                           * Math.max(0, regionCount - 1);

      } // if line is valid
    } // if timeout has expired
  } // wgRegions

  // --------------------------------------------------------------------------
  /**
   * The last time that the /region info command was automatically issued.
   */
  protected long    _lastCommandTime;

  /**
   * The pattern of the full wg.regions lines.
   */
  protected Pattern _wgRegions;

  /**
   * Extracts region names from the captured group, which is of the form:
   * "name, name2, name3".
   */
  protected Pattern _regionNames;
} // class RegionInfoAnalysis