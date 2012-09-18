package watson.analysis;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import watson.BlockEdit;
import watson.BlockType;
import watson.BlockTypeRegistry;
import watson.Controller;
import watson.chat.ChatClassifier;
import watson.chat.ChatProcessor;
import watson.chat.MethodChatHandler;
import watson.chat.TagDispatchChatHandler;

// --------------------------------------------------------------------------
/**
 * An {@link Analysis} implementation that extracts {@link BlockEdit} instances
 * from lb.coord lines.
 */
public class LbCoordsAnalysis extends watson.analysis.Analysis
{
  // --------------------------------------------------------------------------
  /**
   * Implements inherited method.
   */
  @Override
  public void registerAnalysis(TagDispatchChatHandler tagDispatchChatHandler)
  {
    // Set up some test handling of lb.coord:
    tagDispatchChatHandler.setChatHandler("lb.coord", new MethodChatHandler(
      this, "lbCoord"));
    tagDispatchChatHandler.setChatHandler("lb.coordreplaced",
      new MethodChatHandler(this, "lbCoordReplaced"));

    // No longer automatically calling /lb next
    // _chatHandler.setChatHandler("lb.page",
    // new MethodChatHandler(this, "lbPage"));
    _lbCoord = ChatProcessor.getInstance().getChatClassifier().getChatCategoryById(
      "lb.coord").getFullPattern();
    _lbCoordReplaced = ChatProcessor.getInstance().getChatClassifier().getChatCategoryById(
      "lb.coordreplaced").getFullPattern();

    // _lbPage =
    // ChatProcessor.getInstance().getChatClassifier().getChatCategoryById("lb.page").getFullPattern();
  } // registerAnalysis

  // --------------------------------------------------------------------------
  /**
   * This method is called by the {@link ChatClassifier} when a chat line is
   * assigned the "lb.coord" category.
   */
  @SuppressWarnings("unused")
  private void lbCoord(watson.chat.ChatLine line)
  {
    try
    {
      // TODO: describe Matcher groups and their conversions in a config file.
      // Provide a way to get a set of named properties of a line.
      // Use reflection or JavaBeans Statement/Expression to create the
      // BlockEdit as directed by config file.

      Matcher m = _lbCoord.matcher(line.getUnformatted());
      if (m.matches())
      {
        int index = Integer.parseInt(m.group(1));
        int month = Integer.parseInt(m.group(2));
        int day = Integer.parseInt(m.group(3));
        int hour = Integer.parseInt(m.group(4));
        int minute = Integer.parseInt(m.group(5));
        int second = Integer.parseInt(m.group(6));
        // Who the FUCK uses 0-based months?!?
        _time.set(_now.get(Calendar.YEAR), month - 1, day, hour, minute, second);

        String player = m.group(7);
        String action = m.group(8);
        String block = m.group(9);
        int x = Integer.parseInt(m.group(10));
        int y = Integer.parseInt(m.group(11));
        int z = Integer.parseInt(m.group(12));

        BlockType type = BlockTypeRegistry.instance.getBlockTypeByName(block);
        Controller.instance.getVariables().put("time", _time.getTimeInMillis());
        Controller.instance.getVariables().put("player", player);
        Controller.instance.getVariables().put("block", type.getId());
        Controller.instance.getVariables().put("x", x);
        Controller.instance.getVariables().put("y", y);
        Controller.instance.getVariables().put("z", z);

        boolean created = action.equals("created");
        Controller.instance.getBlockEditSet().addBlockEdit(
          new BlockEdit(_time.getTimeInMillis(), player, created, x, y, z, type));

        // TODO: fix this :) Have a class that allows dynamic control of
        // filtered coords.
        // Hacked in re-echoing of coords so we can see TP targets.
        if (index < 150 && type.getId() != 1)
        {
          String target = String.format(
            "(%2d) %02d-%02d %02d:%02d:%02d (%d,%d,%d) %C%d %s", index, month,
            day, hour, minute, second, x, y, z, (created ? '+' : '-'),
            type.getId(), player);
          Controller.instance.localChat(target);
        }

        // // Having found a valid coordinate, also ask for the next page.
        // // Put a limit of 200 pages on it.
        // if (_currentPage != 0 && _currentPage < _pageCount
        // && _currentPage <= 200)
        // {
        // // Remember that we don't need to do this again until next page is
        // // parsed.
        // _currentPage = 0;
        // Packet3Chat chat = new Packet3Chat("/lb next");
        // ModLoader.clientSendPacket(chat);
        // }
      }
    }
    catch (Exception ex)
    {
      // System.out.println(ex);
    }
  } // lbCoord

  // --------------------------------------------------------------------------
  /**
   * This method is called by the {@link ChatClassifier} when a chat line is
   * assigned the "lb.coord" category.
   */
  @SuppressWarnings("unused")
  private void lbCoordReplaced(watson.chat.ChatLine line)
  {
    try
    {
      // TODO: describe Matcher groups and their conversions in a config file.
      // Provide a way to get a set of named properties of a line.
      // Use reflection or JavaBeans Statement/Expression to create the
      // BlockEdit as directed by config file.

      Matcher m = _lbCoordReplaced.matcher(line.getUnformatted());
      if (m.matches())
      {
        int index = Integer.parseInt(m.group(1));
        int month = Integer.parseInt(m.group(2));
        int day = Integer.parseInt(m.group(3));
        int hour = Integer.parseInt(m.group(4));
        int minute = Integer.parseInt(m.group(5));
        int second = Integer.parseInt(m.group(6));
        _time.set(_now.get(Calendar.YEAR), month - 1, day, hour, minute, second);

        String player = m.group(7);
        String oldBlock = m.group(8);
        // UNUSED: String newBlock = m.group(9);
        int x = Integer.parseInt(m.group(10));
        int y = Integer.parseInt(m.group(11));
        int z = Integer.parseInt(m.group(12));

        BlockType type = BlockTypeRegistry.instance.getBlockTypeByName(oldBlock);
        Controller.instance.getVariables().put("time", _time.getTimeInMillis());
        Controller.instance.getVariables().put("player", player);
        Controller.instance.getVariables().put("block", type.getId());
        Controller.instance.getVariables().put("x", x);
        Controller.instance.getVariables().put("y", y);
        Controller.instance.getVariables().put("z", z);

        // Store the destruction but don't bother with the creation.
        Controller.instance.getBlockEditSet().addBlockEdit(
          new BlockEdit(_time.getTimeInMillis(), player, false, x, y, z, type));

        // TODO: fix this :)
        // Hacked in re-echoing of coords so we can see TP targets.
        if (index < 150 && type.getId() != 1)
        {
          String target = String.format(
            "(%2d) %02d-%02d %02d:%02d:%02d (%d,%d,%d) %C%d %s", index, month,
            day, hour, minute, second, x, y, z, '-', type.getId(), player);
          Controller.instance.localChat(target);
        }
      }
    }
    catch (Exception ex)
    {
      // System.out.println(ex);
    }
  } // lbCoordReplaced

  // --------------------------------------------------------------------------
  /**
   * This method is called by the {@link ChatClassifier} when a chat line is
   * assigned the "lb.coord" category.
   */
  @SuppressWarnings("unused")
  private void lbPage(watson.chat.ChatLine line)
  {
    try
    {
      Matcher m = _lbPage.matcher(line.getUnformatted());
      if (m.matches())
      {
        int currentPage = Integer.parseInt(m.group(1));
        int pageCount = Integer.parseInt(m.group(2));

        _currentPage = currentPage;
        _pageCount = pageCount;
      }
    }
    catch (Exception ex)
    {
      // System.out.println(ex);
    }
  } // lbPage

  // --------------------------------------------------------------------------
  /**
   * A reusable Calendar instance used to interpret any time stamps found in
   * LogBlock results.
   */
  protected Calendar _time        = Calendar.getInstance();

  /**
   * Used to infer the implicit (absent) year in LogBlock timestamps.
   */
  protected Calendar _now         = Calendar.getInstance();

  // --------------------------------------------------------------------------
  /**
   * Current page number extracted from lb.page lines.
   */
  protected int      _currentPage = 0;

  /**
   * Total number of pages of results, from lb.page lines.
   */
  protected int      _pageCount   = 0;

  /**
   * The Pattern of full lines with the ID (not tag) lb.coord.
   */
  protected Pattern  _lbCoord;

  /**
   * The Pattern of full lines with the ID (not tag) lb.coordreplaced.
   */
  protected Pattern  _lbCoordReplaced;

  /**
   * The Pattern of full lines with the ID (not tag) lb.page.
   */
  protected Pattern  _lbPage;
} // class LbCoordsAnalysis